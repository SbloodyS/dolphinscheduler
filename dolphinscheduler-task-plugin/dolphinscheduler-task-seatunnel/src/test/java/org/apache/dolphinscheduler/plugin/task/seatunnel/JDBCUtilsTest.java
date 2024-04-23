package org.apache.dolphinscheduler.plugin.task.seatunnel;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLReturnField;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SeaTunnelConnector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JDBCUtilsTest {
    Logger logger = LoggerFactory.getLogger(getClass());

//    @Test
//    public void executeMultiQuery() {
//        String driverName = "com.mysql.cj.jdbc.Driver";
//        String jdbcUrl = "jdbc:mysql://172.16.92.114:3306/BIDBYib?serverTimezone=GMT%2b8&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&tinyInt1isBit=false&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=round";
//        String userName = "root";
//        String password = "root123$%^";
//        String sql = "select * from BIDBYib.ods_scm_dbo_productrawmaterialrelation;";
//        Logger logger = LoggerFactory.getLogger(getClass());
//        boolean result = new JDBCUtils(logger, driverName, jdbcUrl, userName, password).executeMultiQuery( sql);
////        logger.info("result: {}", result);
//    }

//    @Test
//    public void getSQLReturnField() {
//        String driverName = "com.mysql.cj.jdbc.Driver";
//        String jdbcUrl = "jdbc:mysql://172.16.92.114:3306/BIDBYib?serverTimezone=GMT%2b8&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&tinyInt1isBit=false&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=round";
//        String userName = "root";
//        String password = "root123$%^";
//        String sql = "select * from BIDBYib.ods_scm_dbo_productrawmaterialrelation limit 10;";
//        Logger logger = LoggerFactory.getLogger(getClass());
//        List<SQLReturnField> sqlReturnFieldList = new JDBCUtils(logger, driverName, jdbcUrl, userName, password, SeaTunnelConnector.MYSQL).getSQLReturnField(sql);
//        logger.info("result: {}", sqlReturnFieldList);
//    }

//    @Test
//    public void getSQLReturnField() {
//        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//        String jdbcUrl = "jdbc:sqlserver://172.16.92.3:1433;database=BIMServer;encrypt=true;trustServerCertificate=true";
//        String userName = "shujufenxi";
//        String password = "65474CA4-A50B-4D04-8DC2-74D9DFB9A680";
//        String sql = "select * from BIMServer.dbo.t_rpt_czzx_wechat_deal_sta";
//        Logger logger = LoggerFactory.getLogger(getClass());
//        List<SQLReturnField> sqlReturnFieldList = new JDBCUtils(logger, driverName, jdbcUrl, userName, password, SeaTunnelConnector.SQLSERVER).getSQLReturnField(sql);
//        logger.info("result: {}", sqlReturnFieldList);
//    }

//    @Test
//    public void getColumnsComment() {
//        String driverName = "com.mysql.cj.jdbc.Driver";
//        String jdbcUrl = "jdbc:mysql://172.16.92.114:3306/BIDBYib?serverTimezone=GMT%2b8&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&tinyInt1isBit=false&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=round";
//        String userName = "root";
//        String password = "root123$%^";
//        Logger logger = LoggerFactory.getLogger(getClass());
//        Map<String, String> columnsComment = new JDBCUtils(logger, driverName, jdbcUrl,
//                userName, password, SeaTunnelConnector.MYSQL).getColumnsComment("BIDBYib", "ods_scm_dbo_productrawmaterialrelation");
//        logger.info("result: {}", columnsComment);
//    }

//    @Test
//    public void getColumnsComment() {
//        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
//        String jdbcUrl = "jdbc:sqlserver://172.16.92.3:1433;database=BIMServer;encrypt=true;trustServerCertificate=true";
//        String userName = "shujufenxi";
//        String password = "65474CA4-A50B-4D04-8DC2-74D9DFB9A680";
//        Logger logger = LoggerFactory.getLogger(getClass());
//        Map<String, String> columnsComment = new JDBCUtils(logger, driverName, jdbcUrl,
//                userName, password, SeaTunnelConnector.SQLSERVER).getColumnsComment("BIMServer.dbo", "WXRemitOrderLog");
//        logger.info("result: {}", columnsComment);
//    }

//    @Test
//    public void getColumnsComment() {
//        String driverName = "oracle.jdbc.OracleDriver";
//        String jdbcUrl = "jdbc:oracle:thin:@172.16.87.36:1521:LSEAS";
//        String userName = "lseas";
//        String password = "lseas";
//        Logger logger = LoggerFactory.getLogger(getClass());
//        Map<String, String> columnsComment = new JDBCUtils(logger, driverName, jdbcUrl,
//                userName, password, SeaTunnelConnector.ORACLE).getColumnsComment("LSEAS", "dw_fact_facard");
//        logger.info("result: {}", columnsComment);
//    }
}
