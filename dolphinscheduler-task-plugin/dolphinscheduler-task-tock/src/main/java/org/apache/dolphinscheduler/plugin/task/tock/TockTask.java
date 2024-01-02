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

package org.apache.dolphinscheduler.plugin.task.tock;

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
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;

/**
 * shell task
 */
public class TockTask extends AbstractTaskExecutor {

    /**
     * tock parameters
     */
    private TockParameters tockParameters;

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
    public TockTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        logger.info("tock task params {}", taskExecutionContext.getTaskParams());

        tockParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), TockParameters.class);

        if (!tockParameters.checkParameters()) {
            throw new RuntimeException("tock task params is not valid");
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
            tockParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("tock task error", e);
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
        List<Property> ckAdvancedParams = tockParameters.getCkAdvancedParams();
        String toCkScriptParams = buildToCkScriptParams(ckAdvancedParams);

        return String.format("${TOCK_HOME} %s", toCkScriptParams);
    }

    @Override
    public AbstractParameters getParameters() {
        return tockParameters;
    }

    private String buildToCkScriptParams(List<Property> ckAdvancedParams) {
        String script;

        StringBuffer params = new StringBuffer();

        String hiveTableName = "";
        String ckTableName = "";
        String incColumnName = "";
        String condition = "";  //1day、 2day、 month、 year

        for (Property pro : ckAdvancedParams) {
            switch (pro.getProp()) {
                case "hive.table.name":
                    hiveTableName = pro.getValue();
                    logger.info("hive.table.name {}", hiveTableName);
                    break;
                case "clickhouse.table.name":
                    ckTableName = pro.getValue();
                    logger.info("clickhouse.table.name {}", hiveTableName);
                    break;
                case "table.inc.column.name":
                    incColumnName = pro.getValue();
                    logger.info("table.inc.column.name {}", hiveTableName);
                    break;
                case "push.clickhouse.date.condition":
                    condition = pro.getValue();
                    logger.info("push.clickhouse.date.condition {}", hiveTableName);
                    break;
                default:
                    logger.info("no configure Params {}:{}", pro.getProp(), pro.getValue());
                    break;
            }
        }

        if(hiveTableName!=null && !hiveTableName.trim().isEmpty()){
            params.append("--hive.table.name ").append(hiveTableName.trim());
        }
        if(ckTableName!=null && !ckTableName.trim().isEmpty()){
            params.append(" --clickhouse.table.name ").append(ckTableName.trim());
        }
        if(incColumnName!=null && !incColumnName.trim().isEmpty()){
            params.append(" --table.inc.column.name ").append(incColumnName.trim());
        }
        if(condition!=null && !condition.trim().isEmpty()){
            params.append(" --push.clickhouse.date.condition ").append(condition.trim());
        }

        return params.toString();

    }
}
