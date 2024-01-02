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

package org.apache.dolphinscheduler.plugin.task.trino;

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
public class TrinoTask extends AbstractTaskExecutor {

    /**
     * shell parameters
     */
    private TrinoParameters trinoParameters;

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
    public TrinoTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        logger.info("trino task params {}", taskExecutionContext.getTaskParams());

        trinoParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), TrinoParameters.class);

        if (!trinoParameters.checkParameters()) {
            throw new RuntimeException("trino task params is not valid");
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
            trinoParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("trino task error", e);
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
        // generate the content of this trino script
        String trinoScriptContent = buildTrinoScriptContent();
        // generate the file path of this trino script
        String trinoScriptFile = buildTrinoCommandFilePath();

        createTrinoCommandFileIfNotExists(trinoScriptContent, trinoScriptFile);

        return "${JAVA_HOME}/bin/java -jar ${TRINO_HOME} -f " + trinoScriptFile;
    }

    @Override
    public AbstractParameters getParameters() {
        return trinoParameters;
    }

    /**
     * build trino command file path
     *
     * @return trino command file path
     */
    protected String buildTrinoCommandFilePath() {
        return String.format("%s/%s.sql", taskExecutionContext.getExecutePath(), taskExecutionContext.getTaskName());
    }

    /**
     * build trino script content
     *
     * @return raw trino script
     */
    private String buildTrinoScriptContent() {
        String rawTrinoScript = trinoParameters.getRawScript().replaceAll("\\r\\n", "\n");

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, trinoParameters);
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (org.apache.dolphinscheduler.plugin.task.util.MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        rawTrinoScript = ParameterUtils.convertParameterPlaceholders(rawTrinoScript, ParamUtils.convert(paramsMap));

        logger.info("raw trino script : {}", trinoParameters.getRawScript());

        return rawTrinoScript;
    }

    /**
     * create trino command file if not exists
     *
     * @param trinoScript exec trino script
     * @param trinoScriptFile trino script file
     * @throws IOException io exception
     */
    protected void createTrinoCommandFileIfNotExists(String trinoScript, String trinoScriptFile) throws IOException {
        logger.info("tenantCode: {}, task dir: {}", taskExecutionContext.getTenantCode(), taskExecutionContext.getExecutePath());

        if (!Files.exists(Paths.get(trinoScriptFile))) {
            logger.info("generate trino script file:{}", trinoScriptFile);

            // write data to file
            FileUtils.writeStringToFile(new File(trinoScriptFile),
                    trinoScript,
                    StandardCharsets.UTF_8);
        }
    }
}
