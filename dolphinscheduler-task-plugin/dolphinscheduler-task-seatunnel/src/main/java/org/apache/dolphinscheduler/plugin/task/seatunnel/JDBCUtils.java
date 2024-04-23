package org.apache.dolphinscheduler.plugin.task.seatunnel;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import lombok.SneakyThrows;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLReturnField;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SeaTunnelConnector;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class JDBCUtils {
    private final String driverName;
    private final String jdbcUrl;
    private final String userName;
    private final String password;
    private final Logger logger;
    private final SeaTunnelConnector seaTunnelConnector;

    JDBCUtils(Logger logger, String driverName, String jdbcUrl, String userName, String password, SeaTunnelConnector seaTunnelConnector) {
        this.logger = logger;
        this.driverName = driverName;
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
        this.seaTunnelConnector = seaTunnelConnector;
    }

    public boolean executeMultiQuery(String sql) {
        try {
            Connection connection = getConnector();
            Statement stmt = connection.createStatement();
            //切分sql
            String[] sqls = sql.split(";");
            for (String s : sqls) {
                if (!s.trim().isEmpty()) {
                    logger.info("Executing SQL: {}", s);
                    boolean hasResultSet = stmt.execute(s);
                    while (hasResultSet) {
                        ResultSet resultSet = stmt.getResultSet();
                        if (!resultSet.next()) {
                            hasResultSet = false;
                        }
                        Thread.sleep(1000);
                    }
                }
            }
            closeConnector(connection, stmt);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Executing SQL failed:", e);
        }
    }

    public Connection getConnector() {
        Properties properties = new Properties();
        Connection connection;
        properties.put("user", userName);
        properties.put("password", password);
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(jdbcUrl, properties);
        } catch (Exception e) {
            throw new RuntimeException("Executing SQL failed:", e);
        }
        return connection;
    }

    public  void closeConnector(Connection connection, Statement pst) {
        // 关闭连接
        if (connection != null) {
            try {
                connection.close();
                pst.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<SQLReturnField> getSQLReturnField(String sql) {
        ArrayList<SQLReturnField> arrayList = new ArrayList<>();
        try {
            Connection connection = getConnector();
            Statement stmt = connection.createStatement();
            //切分sql
            String[] sqls = sql.split(";");
            if (sqls.length > 1) {
                throw new RuntimeException("Only one SQL is allowed");
            }
            if (!sql.trim().isEmpty()) {
                logger.info("Executing SQL: {}", sql);
                stmt.setMaxRows(10);
                stmt.setQueryTimeout(30);
                boolean hasResultSet = stmt.execute(sql);
                if (hasResultSet) {
                    ResultSet resultSet = stmt.getResultSet();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    String tableName = getTableNameBySql(metaData, sql);
                    Map<String, String> columnsComment = getColumnsComment(tableName);
                    int count = metaData.getColumnCount();
                    for (int i = 1; i <= count; i++) {
                        String name = metaData.getColumnName(i).toLowerCase();
                        String columnType = toHiveType(metaData.getColumnType(i));
                        arrayList.add(new SQLReturnField(name, columnType, columnsComment.getOrDefault(name, "")));
                    }
                }
            }
            closeConnector(connection, stmt);
            return arrayList;
        } catch (Exception e) {
            throw new RuntimeException("Get SQL field failed:", e);
        }
    }

    private String toHiveType(int type) {
        switch (type) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.TIME:
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.BLOB:
            case Types.CLOB:
            case Types.ROWID:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.SQLXML:
                return "STRING";

            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return "DECIMAL(20,8)";

            case Types.BIT:
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.BIGINT:
                return "BIGINT";

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "BINARY";

            case Types.BOOLEAN:
                return "BOOLEAN";

            default:
                throw new RuntimeException("Unsupported Data type: " + type);
        }
    }

    public Map<String, String> getColumnsComment(String tableName) {
        logger.info("Get columns comment tableName: {}", tableName);
        Map<String, String> columnsComment = new HashMap<>();
        if (StringUtils.isEmpty(tableName)) {
            logger.warn("TableName is empty, fill in empty values with comments");
            return columnsComment;
        }

        try {
            Connection connection = getConnector();
            Statement stmt = connection.createStatement();
            String getCommentsSql;

            switch (seaTunnelConnector) {
                case MYSQL:
                    getCommentsSql = String.format("SHOW FULL COLUMNS FROM %s", tableName);
                    break;
                case SQLSERVER:
                    getCommentsSql = String.format("SELECT c.name as field, ex.value as comment FROM syscolumns c LEFT JOIN sys.extended_properties ex ON c.id = ex.major_id AND c.colid = ex.minor_id WHERE c.id = OBJECT_ID('%s')", tableName);
                    break;
                case ORACLE:
                    getCommentsSql = String.format("SELECT column_name as field, comments as \"comment\" FROM user_col_comments WHERE table_name = upper('%s')", tableName);
                    break;
                default:
                    logger.warn("Unsupported Data type: {}, fill in empty values with comments", seaTunnelConnector);
                    return columnsComment;
            }

            boolean hasResultSet = stmt.execute(getCommentsSql);
            if (hasResultSet) {
                ResultSet resultSet = stmt.getResultSet();
                while (resultSet.next()) {
                    String name = resultSet.getString("field").toLowerCase();
                    String value = resultSet.getString("comment");
                    if (StringUtils.isNotEmpty(value)) {
                        value = value.replace(Constants.SEMICOLON, "").replace(Constants.SINGLE_QUOTATION_MARK, "");
                    }
                    columnsComment.put(name, value);
                }
            }
            closeConnector(connection, stmt);
            return columnsComment;
        } catch (Exception e) {
            logger.warn("Get columns comment failed:", e);
        }

        return columnsComment;
    }

    @SneakyThrows
    private String getTableNameBySql(ResultSetMetaData metaData, String sql) {
        switch (seaTunnelConnector) {
            case MYSQL:
                return String.format("%s.%s", metaData.getSchemaName(1), metaData.getTableName(1));
            case SQLSERVER:
                List<String> tableList = new ArrayList<>();
                DbType dbType = JdbcConstants.SQL_SERVER;
                List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
                SQLStatement statement = statementList.get(0);
                SQLServerSchemaStatVisitor visitor = new SQLServerSchemaStatVisitor();
                statement.accept(visitor);
                Map<TableStat.Name, TableStat> tables = visitor.getTables();
                Set<TableStat.Name> tableNames = tables.keySet();
                for (TableStat.Name name : tableNames) {
                    String tableName = name.getName();
                    if (StringUtils.isNotEmpty(tableName)) {
                        tableList.add(name.getName());
                    }
                }
                if (tableList.size() == 1) {
                    return tableList.get(0);
                } else {
                    return "";
                }
            case ORACLE:
                List<String> oracleTableList = new ArrayList<>();
                DbType oracleDbType = JdbcConstants.ORACLE;
                List<SQLStatement> oracleStatementList = SQLUtils.parseStatements(sql, oracleDbType);
                SQLStatement oracleStatement = oracleStatementList.get(0);
                SQLServerSchemaStatVisitor oracleVisitor = new SQLServerSchemaStatVisitor();
                oracleStatement.accept(oracleVisitor);
                Map<TableStat.Name, TableStat> oracleTables = oracleVisitor.getTables();
                Set<TableStat.Name> oracleTableNames = oracleTables.keySet();
                for (TableStat.Name name : oracleTableNames) {
                    String tableName = name.getName();
                    if (StringUtils.isNotEmpty(tableName)) {
                        oracleTableList.add(name.getName());
                    }
                }
                if (oracleTableList.size() == 1) {
                    return oracleTableList.get(0);
                } else {
                    return "";
                }
            default:
                logger.warn("Unsupported SeaTunnel Connector: " + seaTunnelConnector);
                return "";
        }
    }

}
