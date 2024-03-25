package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLReturnField;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JDBCUtils {
    private final String driverName;
    private final String jdbcUrl;
    private final String userName;
    private final String password;
    private final Logger logger;

    JDBCUtils(Logger logger, String driverName, String jdbcUrl, String userName, String password) {
        this.logger = logger;
        this.driverName = driverName;
        this.jdbcUrl = jdbcUrl;
        this.userName = userName;
        this.password = password;
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
                boolean hasResultSet = stmt.execute(sql);
                if (hasResultSet) {
                    ResultSet resultSet = stmt.getResultSet();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int count = metaData.getColumnCount();
                    for (int i = 1; i <= count; i++) {
                        String name = metaData.getColumnName(i).toLowerCase();
                        name = toJavaField(name);
                        String columnType = toJavaType(metaData.getColumnType(i));
                        arrayList.add(new SQLReturnField(name, columnType));
                    }
                }
            }
            closeConnector(connection, stmt);
            return arrayList;
        } catch (Exception e) {
            throw new RuntimeException("Get SQL field failed:", e);
        }
    }

    private String toJavaType(int type) {
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
                return "DOUBLE";

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

    public String toJavaField(String str) {
        String[] split = str.split("_");
        StringBuilder builder = new StringBuilder();
        builder.append(split[0]);// 拼接第一个字符

        // 如果数组不止一个单词
        if (split.length > 1) {
            for (int i = 1; i < split.length; i++) {
                // 去掉下划线，首字母变为大写
                String string = split[i];
                String substring = string.substring(0, 1);
                split[i] = string.replaceFirst(substring, substring.toUpperCase());
                builder.append(split[i]);
            }
        }
        return builder.toString();
    }

}
