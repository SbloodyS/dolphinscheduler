package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            logger.error("Executing SQL failed:", e);
            return false;
        }
    }

    public  Connection getConnector() {
        Properties properties = new Properties();
        Connection connection = null;
        properties.put("user", userName);
        properties.put("password", password);
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(jdbcUrl, properties);
        } catch (Exception e) {
            logger.error("Executing SQL failed:", e);
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
}
