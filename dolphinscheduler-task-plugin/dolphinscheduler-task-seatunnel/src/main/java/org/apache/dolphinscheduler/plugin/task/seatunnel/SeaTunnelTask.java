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

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.ClickHouseSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.ClickHouseSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.CustomParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.DataSourceNew;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.ElasticSearchSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.ElasticSearchSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.Env;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.HiveSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.HiveSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.KafkaSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.MysqlSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.MysqlSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.OracleSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLReturnField;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLServerSinkParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SQLServerSourceParams;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SeaTunnelConfig;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.SeaTunnelConnector;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.Property;
import org.apache.dolphinscheduler.spi.task.paramparser.ParamUtils;
import org.apache.dolphinscheduler.spi.task.paramparser.ParameterUtils;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String beforeHiveSinkCommand;
    private String afterHiveSinkCommand;

    private String beforeHiveSourceCommand;

    private String sinkBeforeSql;

    private JDBCUtils sinkBeforeSqlJdbcUtils;

    private JDBCUtils sourceJdbcUtils;

    private String sourceJdbcUtilsQuerySql;

    private String yarnQueue;

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
            yarnQueue = String.format("root.query.%s", taskExecutionContext.getTenantCode());
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
                if (sinkBeforeSqlJdbcUtils.executeMultiQuery(sinkBeforeSql)) {
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
        SeaTunnelConnector sourceConnector = SeaTunnelConnector.of(sourceDataSourceInfo.getDatasourceType().toLowerCase());
        switch (sourceConnector) {
            case MYSQL:
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
                initSourceJdbcUtils(logger, sourceDataSourceInfo.getDriverName(), mysqlJdbcUrl, sourceDataSourceInfo.getUserName(), sourceDataSourceInfo.getPassword());
                sourceJdbcUtilsQuerySql = mysqlSourceParams.getQuery();
                break;
            case SQLSERVER:
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
                initSourceJdbcUtils(logger, sourceDataSourceInfo.getDriverName(), sqlServerJdbcUrl, sourceDataSourceInfo.getUserName(), sourceDataSourceInfo.getPassword());
                sourceJdbcUtilsQuerySql = sqlServerSourceParams.getQuery();
                break;
            case ORACLE:
                OracleSourceParams oracleSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), OracleSourceParams.class);
                if (oracleSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                oracleSourceParams.setDriver(sourceDataSourceInfo.getDriverName());

                String oracleJdbcUrl = String.format("jdbc:oracle:thin:@%s:%s:%s",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort(),
                        sourceDataSourceInfo.getDatabaseName());
                oracleSourceParams.setUrl(oracleJdbcUrl);

                oracleSourceParams.setUser(sourceDataSourceInfo.getUserName());
                oracleSourceParams.setPassword(sourceDataSourceInfo.getPassword());
                seaTunnelConfig.setSource(Collections.singletonList(oracleSourceParams));
                initSourceJdbcUtils(logger, sourceDataSourceInfo.getDriverName(), oracleJdbcUrl, sourceDataSourceInfo.getUserName(), sourceDataSourceInfo.getPassword());
                sourceJdbcUtilsQuerySql = oracleSourceParams.getQuery();
                break;
            case CLICKHOUSE:
                ClickHouseSourceParams clickHouseSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), ClickHouseSourceParams.class);
                if (clickHouseSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                clickHouseSourceParams.setDatabase(sourceDataSourceInfo.getDatabaseName());
                clickHouseSourceParams.setHost(String.format("%s:%s",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort()));
                clickHouseSourceParams.setUser(sourceDataSourceInfo.getUserName());
                clickHouseSourceParams.setPassword(sourceDataSourceInfo.getPassword());
                seaTunnelConfig.setSource(Collections.singletonList(clickHouseSourceParams));
                String clickhouseJdbcUrl = String.format("jdbc:clickhouse://%s:%s",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort());
                initSourceJdbcUtils(logger, sourceDataSourceInfo.getDriverName(), clickhouseJdbcUrl, sourceDataSourceInfo.getUserName(), sourceDataSourceInfo.getPassword());
                sourceJdbcUtilsQuerySql = clickHouseSourceParams.getSql();
                break;
            case HIVE:
                HiveSourceParams hiveSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), HiveSourceParams.class);
                if (hiveSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }
                hiveSourceParams.setMetastoreUri(String.format("thrift://%s:%s",
                        sourceDataSourceInfo.getHostname(),
                        sourceDataSourceInfo.getPort()));
                seaTunnelConfig.setSource(Collections.singletonList(hiveSourceParams));

                String originTable = hiveSourceParams.getSourceTable();
                String tmpTable;
                if (originTable.contains(".")) {
                    tmpTable = String.format("tmp.tmp_%s_seatunnel_%s",
                            hiveSourceParams.getSourceTable().replace(".", "_"),
                            taskExecutionContext.getTaskInstanceId());
                } else {
                    throw new RuntimeException(String.format("hive table %s is invalid", originTable));
                }
                hiveSourceParams.setSourceTable(tmpTable);

                generateHiveSourceCommand(originTable, tmpTable);
                break;
            case ELASTICSEARCH:
                ElasticSearchSourceParams elasticSearchSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), ElasticSearchSourceParams.class);
                if (elasticSearchSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }
                List<String> hosts = new ArrayList<>(Arrays.asList(sourceDataSourceInfo.getHostname().split(Constants.COMMA)));
                elasticSearchSourceParams.setQuery(JSONUtils.parseObject(elasticSearchSourceParams.getQuery().toString(), new TypeReference<Map<Object, Object>>(){}));
                elasticSearchSourceParams.setSource(JSONUtils.parseObject(elasticSearchSourceParams.getSource().toString(), new TypeReference<List<String>>(){}));

                elasticSearchSourceParams.setHosts(hosts);
                elasticSearchSourceParams.setUserName(sourceDataSourceInfo.getUserName());
                elasticSearchSourceParams.setPassword(sourceDataSourceInfo.getPassword());
                seaTunnelConfig.setSource(Collections.singletonList(elasticSearchSourceParams));
                break;
            case KAFKA:
                KafkaSourceParams kafkaSourceParams = JSONUtils.convertValue(seaTunnelParameters.getSource(), KafkaSourceParams.class);
                if (kafkaSourceParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }
                kafkaSourceParams.setBootstrapServers(sourceDataSourceInfo.getHostname());
                KafkaSourceParams.KafkaConfig kafkaConfig = kafkaSourceParams.getKafkaConfig();
                kafkaConfig.setClientId(String.format("%s_%s", kafkaSourceParams.getConsumerGroup(), (int) (Math.random() * 10000000)));
                kafkaSourceParams.setKafkaConfig(kafkaConfig);
                seaTunnelConfig.setSource(Collections.singletonList(kafkaSourceParams));
                break;
            default:
                throw new RuntimeException("Unsupported source datasource type: " + sourceDataSourceInfo.getDatasourceType());
        }

        DataSourceNew targetDataSourceInfo = JSONUtils.convertValue(seaTunnelParameters.getTargetDataSourceInfo(), DataSourceNew.class);
        SeaTunnelConnector targetConnector = SeaTunnelConnector.of(targetDataSourceInfo.getDatasourceType().toLowerCase());
        switch (targetConnector) {
            case MYSQL:
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

                initSinkBeforeSqlJdbcUtils(logger, targetDataSourceInfo.getDriverName(), mysqlJdbcUrl, targetDataSourceInfo.getUserName(), targetDataSourceInfo.getPassword());
                break;
            case SQLSERVER:
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

                initSinkBeforeSqlJdbcUtils(logger, targetDataSourceInfo.getDriverName(), sqlServerJdbcUrl, targetDataSourceInfo.getUserName(), targetDataSourceInfo.getPassword());
                break;
            case HIVE:
                HiveSinkParams hiveSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), HiveSinkParams.class);
                if (hiveSinkParams == null) {
                    throw new RuntimeException("target datasource params is invalid");
                }
                hiveSinkParams.setMetastoreUri(String.format("thrift://%s:%s",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort()));

                String originTable = hiveSinkParams.getTargetTable();
                String tmpTable;
                if (originTable.contains(".")) {
                    tmpTable = String.format("tmp.tmp_%s_seatunnel_%s",
                            hiveSinkParams.getTargetTable().replace(".", "_"),
                            taskExecutionContext.getTaskInstanceId());
                } else {
                    throw new RuntimeException(String.format("hive table %s is invalid", originTable));
                }

                hiveSinkParams.setTargetTable(tmpTable);

                seaTunnelConfig.setSink(Collections.singletonList(hiveSinkParams));

                // 兼容旧逻辑
                if (seaTunnelParameters.getCustomParams() == null) {
                    CustomParams customParams = new CustomParams();
                    customParams.setAutoCreateHiveTable(false);
                    customParams.setSinkHiveTablePartition("");
                    seaTunnelParameters.setCustomParams(customParams);
                }

                generateHiveSinkCommand(originTable, tmpTable, seaTunnelParameters.getCustomParams().getAutoCreateHiveTable(),
                        seaTunnelParameters.getCustomParams().getSinkHiveTablePartition());
                break;
            case CLICKHOUSE:
                ClickHouseSinkParams clickHouseSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), ClickHouseSinkParams.class);
                if (clickHouseSinkParams == null) {
                    throw new RuntimeException("source datasource params is invalid");
                }

                String clickhouseTable = clickHouseSinkParams.getTable();
                if (clickhouseTable.contains(".")) {
                    clickHouseSinkParams.setTable(clickhouseTable.split("\\.")[1]);
                    clickHouseSinkParams.setDatabase(clickhouseTable.split("\\.")[0]);
                } else {
                    throw new RuntimeException(String.format("clickhouse table %s is invalid", clickhouseTable));
                }

                clickHouseSinkParams.setHost(String.format("%s:%s",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort()));
                clickHouseSinkParams.setUser(targetDataSourceInfo.getUserName());
                clickHouseSinkParams.setPassword(targetDataSourceInfo.getPassword());
                seaTunnelConfig.setSink(Collections.singletonList(clickHouseSinkParams));

                String clickhouseJdbcUrl = String.format("jdbc:clickhouse://%s:%s",
                        targetDataSourceInfo.getHostname(),
                        targetDataSourceInfo.getPort());
                initSinkBeforeSqlJdbcUtils(logger, targetDataSourceInfo.getDriverName(), clickhouseJdbcUrl, targetDataSourceInfo.getUserName(), targetDataSourceInfo.getPassword());
                break;
            case ELASTICSEARCH:
                ElasticSearchSinkParams elasticSearchSinkParams = JSONUtils.convertValue(seaTunnelParameters.getSink(), ElasticSearchSinkParams.class);
                if (elasticSearchSinkParams == null) {
                    throw new RuntimeException("sink datasource params is invalid");
                }
                List<String> hosts = new ArrayList<>(Arrays.asList(targetDataSourceInfo.getHostname().split(Constants.COMMA)));
                if (elasticSearchSinkParams.getPrimaryKeys() != null) {
                    List<String> primaryKeys = new ArrayList<>(Arrays.asList(elasticSearchSinkParams.getPrimaryKeys().toString().split(Constants.COMMA)));
                    elasticSearchSinkParams.setPrimaryKeys(primaryKeys);
                }

                elasticSearchSinkParams.setHosts(hosts);
                elasticSearchSinkParams.setUserName(targetDataSourceInfo.getUserName());
                elasticSearchSinkParams.setPassword(targetDataSourceInfo.getPassword());
                seaTunnelConfig.setSink(Collections.singletonList(elasticSearchSinkParams));
                break;
            default:
                throw new RuntimeException("Unsupported target datasource type: " + targetDataSourceInfo.getDatasourceType());
        }
    }

    private String buildCommand() throws Exception {
        String seaTunnelConfigFilePath = buildSeaTunnelConfigFilePath();

        createSeaTunnelCommandFileIfNotExists(parseScript(JSONUtils.toJsonString(seaTunnelConfig)), seaTunnelConfigFilePath);

        if (!StringUtils.isEmpty(beforeHiveSinkCommand) && !StringUtils.isEmpty(afterHiveSinkCommand)) {
            String seatunnelCommand = String.format("${ST_HOME} --master yarn --deploy-mode cluster --config %s --name %s --queue %s",
                    seaTunnelConfigFilePath, taskExecutionContext.getTaskName(), yarnQueue);
            return String.format("set -xeuo pipefail\n%s\n%s\n%s",
                    beforeHiveSinkCommand, seatunnelCommand, afterHiveSinkCommand);
        }

        if (!StringUtils.isEmpty(beforeHiveSourceCommand)) {
            String seatunnelCommand = String.format("${ST_HOME} --master yarn --deploy-mode cluster --config %s --name %s --queue %s",
                    seaTunnelConfigFilePath, taskExecutionContext.getTaskName(), yarnQueue);
            return String.format("set -xeuo pipefail\n%s\n%s", beforeHiveSourceCommand, seatunnelCommand);
        }

        return String.format("set -xeuo pipefail\n${ST_HOME} --master yarn --deploy-mode cluster --config %s --name %s --queue %s",
                seaTunnelConfigFilePath, taskExecutionContext.getTaskName(), yarnQueue);
    }

    @Override
    public AbstractParameters getParameters() {
        return seaTunnelParameters;
    }

    @SneakyThrows
    private void generateHiveSinkCommand(String targetTable, String tmpTable, Boolean autoCreateHiveTable, String sinkHiveTablePartition) {
        String createHiveTableStatement= "";
        if (autoCreateHiveTable) {
            createHiveTableStatement = generateCreateHiveTableStatement(targetTable);
        }
        String createTmpTableStatement = String.format("CREATE TABLE %s STORED AS TEXTFILE AS SELECT * FROM %s WHERE 1=2;", tmpTable, targetTable);
        String dropTableStatement = String.format("DROP TABLE IF EXISTS %s;", tmpTable);
        String insertTableStatement;
        if (StringUtils.isEmpty(sinkHiveTablePartition)) {
            insertTableStatement = String.format("INSERT OVERWRITE TABLE %s SELECT * FROM %s;", targetTable, tmpTable);
        } else {
            insertTableStatement = String.format("set hive.exec.dynamic.partition=true;\n" +
                    "set hive.exec.dynamic.partition.mode=nonstrict;\n" +
                    "set hive.exec.max.dynamic.partitions=10000;\n" +
                    "set hive.exec.max.dynamic.partitions.pernode=10000;\n" +
                    "INSERT OVERWRITE TABLE %s PARTITION(%s) SELECT * FROM %s;", targetTable, sinkHiveTablePartition, tmpTable);
        }

        String beforeHiveSqlPath = String.format("%s/%s_before.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
        String afterHiveSqlPath = String.format("%s/%s_after.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
        createSeaTunnelCommandFileIfNotExists(String.format("%s\n%s\n%s", createHiveTableStatement,dropTableStatement, createTmpTableStatement), beforeHiveSqlPath);
        createSeaTunnelCommandFileIfNotExists(String.format("%s\n%s", insertTableStatement, dropTableStatement), afterHiveSqlPath);

        beforeHiveSinkCommand = String.format("sudo ${HIVE_CLI_HOME} -v -hiveconf mapreduce.job.name=%s -hiveconf mapreduce.job.queuename=%s -hiveconf hive.execution.engine=mr -f %s",
                taskExecutionContext.getTaskName(),
                yarnQueue,
                beforeHiveSqlPath);
        afterHiveSinkCommand = String.format("sudo ${HIVE_CLI_HOME} -v -hiveconf mapreduce.job.name=%s -hiveconf mapreduce.job.queuename=%s -hiveconf hive.execution.engine=mr -f %s",
                taskExecutionContext.getTaskName(),
                yarnQueue,
                afterHiveSqlPath);
    }

    @SneakyThrows
    private void generateHiveSourceCommand(String targetTable, String tmpTable) {
        String createTmpTableStatement = String.format("CREATE TABLE %s ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\u0007' NULL DEFINED AS '' STORED AS TEXTFILE AS SELECT * FROM %s;", tmpTable, targetTable);
        String dropTableStatement = String.format("DROP TABLE IF EXISTS %s;", tmpTable);

        String beforeHiveSourceSqlPath = String.format("%s/%s_before_hive_source.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
        createSeaTunnelCommandFileIfNotExists(String.format("%s\n%s", dropTableStatement, createTmpTableStatement), beforeHiveSourceSqlPath);

        beforeHiveSourceCommand = String.format("sudo ${HIVE_CLI_HOME} -v -hiveconf mapreduce.job.name=%s -hiveconf mapreduce.job.queuename=%s -hiveconf hive.execution.engine=mr -f %s",
                taskExecutionContext.getTaskName(),
                yarnQueue,
                beforeHiveSourceSqlPath);
    }

    private String buildSeaTunnelConfigFilePath() {
        String seaTunnelConfigStr = parseScript(JSONUtils.toJsonString(seaTunnelConfig));
        logger.info("seatunnel config json: {}", seaTunnelConfigStr);
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

    private String generateCreateHiveTableStatement(String tableName) {
        String createHiveTableStatement = "CREATE TABLE IF NOT EXISTS {tableName} ({columns}) STORED AS ORC;";
        List<SQLReturnField> sqlReturnFieldList = sourceJdbcUtils.getSQLReturnField(sourceJdbcUtilsQuerySql);
        List<String> columnsList = new ArrayList<>();
        for (SQLReturnField sqlReturnField : sqlReturnFieldList) {
            columnsList.add(sqlReturnField.getFieldName() + " " + sqlReturnField.getFieldType());
        }
        return createHiveTableStatement.replace("{tableName}", tableName).replace("{columns}", String.join(",", columnsList));
    }

    private void initSinkBeforeSqlJdbcUtils(Logger logger, String driverName, String jdbcUrl, String userName, String password) {
        sinkBeforeSqlJdbcUtils = new JDBCUtils(logger, driverName, jdbcUrl, userName, password);
    }

    private void initSourceJdbcUtils(Logger logger, String driverName, String jdbcUrl, String userName, String password) {
        sourceJdbcUtils = new JDBCUtils(logger, driverName, jdbcUrl, userName, password);
    }

    private String parseScript(String script) {
        // combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        return ParameterUtils.convertParameterPlaceholders(script, ParamUtils.convert(paramsMap));
    }
}
