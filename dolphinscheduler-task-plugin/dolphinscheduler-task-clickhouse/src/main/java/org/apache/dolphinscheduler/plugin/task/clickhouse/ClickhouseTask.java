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

package org.apache.dolphinscheduler.plugin.task.clickhouse;

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

public class ClickhouseTask extends AbstractTaskExecutor {

    private ClickhouseParameters clickhouseParameters;

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
    public ClickhouseTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        logger.info("clickhouse task params {}", taskExecutionContext.getTaskParams());

        clickhouseParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ClickhouseParameters.class);

        if (!clickhouseParameters.checkParameters()) {
            throw new RuntimeException("clickhouse task params is not valid");
        }
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
            clickhouseParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("clickhouse task error", e);
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
        // generate the content of this clickhouse script
        String clickhouseScriptContent = buildClickhouseScriptContent();
        // generate the file path of this clickhouse script
        String clickhouseScriptFile = buildClickhouseCommandFilePath();

        createClickhouseCommandFileIfNotExists(clickhouseScriptContent, clickhouseScriptFile);

        return String.format("${JAVA_HOME}/bin/java -jar ${CLICKHOUSE_HOME} -d %s -f %s",
                clickhouseParameters.getIp(), clickhouseScriptFile);
    }

    @Override
    public AbstractParameters getParameters() {
        return clickhouseParameters;
    }

    /**
     * build clickhouse command file path
     *
     * @return clickhouse command file path
     */
    protected String buildClickhouseCommandFilePath() {
        return String.format("%s/%s.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
    }

    /**
     * build clickhouse script content
     *
     * @return raw clickhouse script
     */
    private String buildClickhouseScriptContent() {
        String rawClickhouseSql = clickhouseParameters.getSql().replaceAll("\\r\\n", "\n");

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, clickhouseParameters);
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (org.apache.dolphinscheduler.plugin.task.util.MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        rawClickhouseSql = ParameterUtils.convertParameterPlaceholders(rawClickhouseSql, ParamUtils.convert(paramsMap));

        logger.info("raw clickhouse sql : {}", clickhouseParameters.getSql());

        return rawClickhouseSql;
    }

    /**
     * create clickhouse command file if not exists
     *
     * @param clickhouseScript exec clickhouse script
     * @param clickhouseScriptFile clickhouse script file
     * @throws IOException io exception
     */
    protected void createClickhouseCommandFileIfNotExists(String clickhouseScript, String clickhouseScriptFile) throws IOException {
        logger.info("tenantCode: {}, task dir: {}", taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());

        if (!Files.exists(Paths.get(clickhouseScriptFile))) {
            logger.info("generate clickhouse script file:{}", clickhouseScriptFile);

            // write data to file
            FileUtils.writeStringToFile(new File(clickhouseScriptFile),
                    clickhouseScript,
                    StandardCharsets.UTF_8);
        }
    }
}
