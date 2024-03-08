/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.ClickHouseSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.ClickHouseSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.DataSourceNew;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.Env;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.HiveSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.HiveSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.MysqlSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.MysqlSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLServerSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLServerSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SeaTunnelConfig;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;

/**
 * shell task
 */
public class SeaTunnelTask extends AbstractTaskExecutor {

    private SeaTunnelParameters seaTunnelParameters;

    private final SeaTunnelConfig seaTunnelConfig = new SeaTunnelConfig();

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    private String jobMode = "BATCH";

    private String beforeHiveCommand;
    private String afterHiveCommand;

    private String sinkBeforeSql;

    private JDBCUtils jdbcUtils;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public SeaTunnelTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        try {
            logger.info("seatunnel task params: {}", taskExecutionContext.getSeaTunnelTaskExecutionContext());

            seaTunnelParameters = JSONUtils.convertValue(taskExecutionContext.getSeaTunnelTaskExecutionContext(), SeaTunnelParameters.class);

            if (seaTunnelParameters != null && !seaTunnelParameters.checkParameters()) {
                throw new RuntimeException("seatunnel task params is not valid");
            }

            generateSeaTunnelConfig();

            this.sinkBeforeSql = seaTunnelParameters.getSinkBeforeSql();
        } catch (Exception e) {
            logger.error("seatunnel init error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }

    }

    @Override
    public void handle() throws Exception {
        try {
            // execute before sql
            if (StringUtils.isNotEmpty(sinkBeforeSql)) {
                logger.info("execute sink before sql: {}", sinkBeforeSql);
                if (jdbcUtils.executeMultiQuery(sinkBeforeSql)) {
                    logger.info("execute sink before sql success");
                }
            }

            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
            seaTunnelParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("seatunnel task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    private void generateSeaTunnelConfig() {
        Env env = JSONUtils.convertValue(seaTunnelParameters.getEnv(), Env.class);
        if (env == null) {
            throw new RuntimeException("env params is invalid");
        }
        env.setJobMode(jobMode);
        env.setJobName(taskExecutionContext.getTaskName());
        logger.info("envParams: {}", env);
        seaTunnelConfig.setEnv(env);

        DataSourceNew sourceDataSourceInfo = JSONUtils.convertValue(seaTunnelParameters.getSourceDataSourceInfo(), DataSourceNew.class);

        switch (sourceDataSourceInfo.getDatasourceType().toLowerCase()) {
            case "mysql":
                MysqlSourceParams mysqlSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), MysqlSourceParams.class);
                if (mysqlSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                mysqlSourceParams.setDriver(sourceDataSourceInfo.getDriverName());

                String mysqlJdbcUrl = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=GMT%%2b8&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&tinyInt1isBit=false&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=round",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort(),
                        sourceDataSourceInfo.getDatabaseName());
                mysqlSourceParams.setUrl(mysqlJdbcUrl);

                mysqlSourceParams.setUser(sourceDataSourceInfo.getUserName());
                mysqlSourceParams.setPassword(sourceDataSourceInfo.getPassword());
                seaTunnelConfig.setSource(Collections.singletonList(mysqlSourceParams));
                break;
            case "sqlserver":
                SQLServerSourceParams sqlServerSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), SQLServerSourceParams.class);
                if (sqlServerSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                sqlServerSourceParams.setDriver(sourceDataSourceInfo.getDriverName());

                String sqlServerJdbcUrl = String.format("jdbc:sqlserver://%s:%s;database=%s;encrypt=true;trustServerCertificate=true",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort(),
                        sourceDataSourceInfo.getDatabaseName());
                sqlServerSourceParams.setUrl(sqlServerJdbcUrl);

                sqlServerSourceParams.setUser(sourceDataSourceInfo.getUserName());
                sqlServerSourceParams.setPassword(sourceDataSourceInfo.getPassword());
                seaTunnelConfig.setSource(Collections.singletonList(sqlServerSourceParams));
                break;
            case "clickhouse":
                ClickHouseSourceParams clickHouseSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), ClickHouseSourceParams.class);
                if (clickHouseSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                clickHouseSourceParams.setDatabase(sourceDataSourceInfo.getDatabaseName());
                clickHouseSourceParams.setHost(sourceDataSourceInfo.getHostname());
                clickHouseSourceParams.setUser(sourceDataSourceInfo.getUserName());
                clickHouseSourceParams.setPassword(sourceDataSourceInfo.getPassword());
                seaTunnelConfig.setSource(Collections.singletonList(clickHouseSourceParams));
                break;
            case "hive":
                HiveSourceParams hiveSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), HiveSourceParams.class);
                if (hiveSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }
                hiveSourceParams.setMetastoreUri(String.format("thrift://%s:%s",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort()));
                seaTunnelConfig.setSource(Collections.singletonList(hiveSourceParams));
                break;
            default:
                throw new RuntimeException("Unsupported source datasource type: " + sourceDataSourceInfo.getDatasourceType());
        }

        DataSourceNew targetDataSourceInfo = JSONUtils.convertValue(seaTunnelParameters.getTargetDataSourceInfo(), DataSourceNew.class);

        switch (targetDataSourceInfo.getDatasourceType().toLowerCase()) {
            case "mysql":
                MysqlSinkParams mysqlSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), MysqlSinkParams.class);
                if (mysqlSinkParams == null) {
                    throw new RuntimeException("sink datasource params is invalid");
                }

                mysqlSinkParams.setDriver(targetDataSourceInfo.getDriverName());

                String mysqlJdbcUrl = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=GMT%%2b8&useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&tinyInt1isBit=false&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=round",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort(),
                        targetDataSourceInfo.getDatabaseName());
                mysqlSinkParams.setUrl(mysqlJdbcUrl);

                mysqlSinkParams.setUser(targetDataSourceInfo.getUserName());
                mysqlSinkParams.setPassword(targetDataSourceInfo.getPassword());
                seaTunnelConfig.setSink(Collections.singletonList(mysqlSinkParams));

                initJDBCUtils(logger, targetDataSourceInfo.getDriverName(), mysqlJdbcUrl, targetDataSourceInfo.getUserName(), targetDataSourceInfo.getPassword());
                break;
            case "sqlserver":
                SQLServerSinkParams sqlServerSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), SQLServerSinkParams.class);
                if (sqlServerSinkParams == null) {
                    throw new RuntimeException("sink datasource params is invalid");
                }

                sqlServerSinkParams.setDriver(targetDataSourceInfo.getDriverName());

                String sqlServerJdbcUrl = String.format("jdbc:sqlserver://%s:%s;database=%s;encrypt=true;trustServerCertificate=true",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort(),
                        targetDataSourceInfo.getDatabaseName());
                sqlServerSinkParams.setUrl(sqlServerJdbcUrl);

                sqlServerSinkParams.setUser(targetDataSourceInfo.getUserName());
                sqlServerSinkParams.setPassword(targetDataSourceInfo.getPassword());
                seaTunnelConfig.setSink(Collections.singletonList(sqlServerSinkParams));

                initJDBCUtils(logger, targetDataSourceInfo.getDriverName(), sqlServerJdbcUrl, targetDataSourceInfo.getUserName(), targetDataSourceInfo.getPassword());
                break;
            case "hive":
                HiveSinkParams hiveSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), HiveSinkParams.class);
                if (hiveSinkParams == null) {
                    throw new RuntimeException("target datasource params is invalid");
                }
                hiveSinkParams.setMetastoreUri(String.format("thrift://%s:%s",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort()));
                String originTable = hiveSinkParams.getTargetTable();
                String tmpTable = hiveSinkParams.getTargetTable() + "_seatunneltmp";
                hiveSinkParams.setTargetTable(tmpTable);

                seaTunnelConfig.setSink(Collections.singletonList(hiveSinkParams));
                generateHiveCommand(originTable, tmpTable);
                break;
            case "clickhouse":
                ClickHouseSinkParams clickHouseSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), ClickHouseSinkParams.class);
                if (clickHouseSinkParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                clickHouseSinkParams.setDatabase(targetDataSourceInfo.getDatabaseName());
                clickHouseSinkParams.setHost(targetDataSourceInfo.getHostname());
                clickHouseSinkParams.setUser(targetDataSourceInfo.getUserName());
                clickHouseSinkParams.setPassword(targetDataSourceInfo.getPassword());
                seaTunnelConfig.setSink(Collections.singletonList(clickHouseSinkParams));

                String clickhouseJdbcUrl = String.format("jdbc:clickhouse://%s:%s",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort());
                initJDBCUtils(logger, targetDataSourceInfo.getDriverName(), clickhouseJdbcUrl, targetDataSourceInfo.getUserName(), targetDataSourceInfo.getPassword());
                break;
            default:
                throw new RuntimeException("Unsupported target datasource type: " + targetDataSourceInfo.getDatasourceType());
        }
    }

    private String buildCommand() throws Exception {
        String seaTunnelConfigFilePath = buildSeaTunnelConfigFilePath();

        createSeaTunnelCommandFileIfNotExists(JSONUtils.toJsonString(seaTunnelConfig), seaTunnelConfigFilePath);

        if (StringUtils.isEmpty(beforeHiveCommand) && StringUtils.isEmpty(afterHiveCommand)) {
            return String.format("set -xeuo pipefail\n${ST_HOME} --master yarn --deploy-mode cluster --config %s --name %s",
                    seaTunnelConfigFilePath, taskExecutionContext.getTaskName());
        } else {
            String seatunnelCommand = String.format("${ST_HOME} --master yarn --deploy-mode cluster --config %s --name %s",
                    seaTunnelConfigFilePath, taskExecutionContext.getTaskName());
            return String.format("set -xeuo pipefail\n%s\n%s\n%s", beforeHiveCommand, seatunnelCommand, afterHiveCommand);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return seaTunnelParameters;
    }

    @SneakyThrows
    private void generateHiveCommand(String targetTable, String tmpTable) {
        String createTableStatement = String.format("CREATE TABLE %s STORED AS TEXTFILE AS SELECT * FROM %S WHERE 1=2;", tmpTable, targetTable);
        String dropTableStatement = String.format("DROP TABLE IF EXISTS %s;", tmpTable);
        String insertTableStatement = String.format("INSERT OVERWRITE TABLE %s SELECT * FROM %s;", targetTable, tmpTable);

        String beforeHiveSqlPath = String.format("%s/%s_before.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
        String afterHiveSqlPath = String.format("%s/%s_after.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
        createSeaTunnelCommandFileIfNotExists(String.format("%s\n%s", dropTableStatement, createTableStatement), beforeHiveSqlPath);
        createSeaTunnelCommandFileIfNotExists(String.format("%s\n%s", insertTableStatement, dropTableStatement), afterHiveSqlPath);

        beforeHiveCommand = String.format("sudo ${HIVE_CLI_HOME} -v -hiveconf mapreduce.job.name=%s -hiveconf mapreduce.job.queuename=%s -hiveconf hive.execution.engine=mr -f %s",
                taskExecutionContext.getTaskName(),
                "root.query.dmp",
                beforeHiveSqlPath);
        afterHiveCommand = String.format("sudo ${HIVE_CLI_HOME} -v -hiveconf mapreduce.job.name=%s -hiveconf mapreduce.job.queuename=%s -hiveconf hive.execution.engine=mr -f %s",
                taskExecutionContext.getTaskName(),
                "root.query.dmp",
                afterHiveSqlPath);
    }

    private String buildSeaTunnelConfigFilePath() {
        logger.info("seatunnel config json: {}", JSONUtils.toJsonString(seaTunnelConfig));
        return String.format("%s/%s.json", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
    }

    private void createSeaTunnelCommandFileIfNotExists(String seaTunnelConfigJson, String seaTunnelConfigFile) throws IOException {
        logger.info("tenantCode: {}, task dir: {}", taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());

        if (!Files.exists(Paths.get(seaTunnelConfigFile))) {
            logger.info("generate seatunnel script file:{}", seaTunnelConfigFile);

            // write data to file
            FileUtils.writeStringToFile(new File(seaTunnelConfigFile),
                    seaTunnelConfigJson,
                    StandardCharsets.UTF_8);
        }
    }

    private void initJDBCUtils(Logger logger, String driverName, String jdbcUrl, String userName, String password) {
        jdbcUtils = new JDBCUtils(logger, driverName, jdbcUrl, userName, password);
    }
}
