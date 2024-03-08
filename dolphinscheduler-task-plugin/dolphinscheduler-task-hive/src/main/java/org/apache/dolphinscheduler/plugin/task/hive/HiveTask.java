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

package org.apache.dolphinscheduler.plugin.task.hive;

import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.util.MapUtils;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.Property;
import org.apache.dolphinscheduler.spi.task.paramparser.ParamUtils;
import org.apache.dolphinscheduler.spi.task.paramparser.ParameterUtils;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;

/**
 * shell task
 */
public class HiveTask extends AbstractTaskExecutor {

    /**
     * shell parameters
     */
    private HiveParameters hiveParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public HiveTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        logger.info("hive task params {}", taskExecutionContext.getTaskParams());

        hiveParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HiveParameters.class);

        if (!hiveParameters.checkParameters()) {
            throw new RuntimeException("hive task params is not valid");
        }
        taskExecutionContext.setTaskName(taskExecutionContext.getTaskName().trim());
    }

    @Override
    public void handle() throws Exception {
        try {
            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
            hiveParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("hive task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    /**
     * create command
     *
     * @return file name
     * @throws Exception exception
     */
    private String buildCommand() throws Exception {
        // generate the content of this hive script
        String hiveScriptContent = buildHiveScriptContent();
        // generate the file path of this hive script
        String hiveScriptFile = buildHiveCommandFilePath();

        createHiveCommandFileIfNotExists(hiveScriptContent, hiveScriptFile);

        return String.format("sudo ${HIVE_CLI_HOME} -v -hiveconf mapreduce.job.name=%s -hiveconf spark.app.name=%s -hiveconf mapreduce.job.queuename=%s -hiveconf spark.yarn.queue=%s -f %s",
                taskExecutionContext.getTaskName(),
                taskExecutionContext.getTaskName(),
                "root.query.dmp",
                "root.query.dmp",
                hiveScriptFile);
    }

    @Override
    public AbstractParameters getParameters() {
        return hiveParameters;
    }

    /**
     * build hive command file path
     *
     * @return hive command file path
     */
    protected String buildHiveCommandFilePath() {
        return String.format("%s/%s.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
    }

    /**
     * build hive script content
     *
     * @return raw hive script
     */
    private String buildHiveScriptContent() {
        String rawHiveScript = hiveParameters.getSql().replaceAll("\\r\\n", "\n");

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, hiveParameters);
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (org.apache.dolphinscheduler.plugin.task.util.MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        rawHiveScript = ParameterUtils.convertParameterPlaceholders(rawHiveScript, ParamUtils.convert(paramsMap));

        logger.info("raw hive script : {}", hiveParameters.getSql());

        return rawHiveScript;
    }

    /**
     * create hive command file if not exists
     *
     * @param hiveScript exec hive script
     * @param hiveScriptFile hive script file
     * @throws IOException io exception
     */
    protected void createHiveCommandFileIfNotExists(String hiveScript, String hiveScriptFile) throws IOException {
        logger.info("tenantCode: {}, task dir: {}", taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());

        if (!Files.exists(Paths.get(hiveScriptFile))) {
            logger.info("generate hive script file:{}", hiveScriptFile);

            // write data to file
            FileUtils.writeStringToFile(new File(hiveScriptFile),
                    hiveScript,
                    StandardCharsets.UTF_8);
        }
    }
}
