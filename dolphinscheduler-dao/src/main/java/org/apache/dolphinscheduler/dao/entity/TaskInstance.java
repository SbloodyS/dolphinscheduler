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

package org.apache.dolphinscheduler.dao.entity;

import static org.apache.dolphinscheduler.common.Constants.SEC_2_MINUTES_TIME_UNIT;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskInstanceFullLogPath;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.task.switchtask.SwitchParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName(value = "t_ds_task_instance")
public class TaskInstance implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * task name
     */
    private String name;


    /**
     * task type
     */
    private String taskType;

    /**
     * process instance id
     */
    private int processInstanceId;

    /**
     * task code
     */
    private long taskCode;

    /**
     * task definition version
     */
    private int taskDefinitionVersion;

    /**
     * process instance name
     */
    @TableField(exist = false)
    private String processInstanceName;

    /**
     * state
     */
    private ExecutionStatus state;

    /**
     * task first submit time.
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date firstSubmitTime;

    /**
     * task submit time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    /**
     * task start time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * task end time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * task host
     */
    private String host;

    /**
     * task shell execute path and the resource down from hdfs
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String executePath;

    /**
     * task log path
     * default path: $base_run_dir/processInstanceId/taskInstanceId/retryTimes
     */
    private String logPath;

    private String fullLogPath;

    @TableField(exist = false)
    private List<TaskInstanceFullLogPath> taskInstanceFullLogPath;

    /**
     * retry times
     */
    private int retryTimes;

    /**
     * alert flag
     */
    private Flag alertFlag;

    /**
     * process instance
     */
    @TableField(exist = false)
    private ProcessInstance processInstance;

    /**
     * process definition
     */
    @TableField(exist = false)
    private ProcessDefinition processDefine;

    /**
     * task definition
     */
    @TableField(exist = false)
    private TaskDefinition taskDefine;

    @TableField(exist = false)
    private long projectCode;

    @TableField(exist = false)
    private String projectName;

    /**
     * process id
     */
    private int pid;

    /**
     * appLink
     */
    private String appLink;

    /**
     * flag
     */
    private Flag flag;

    /**
     * dependency
     */
    @TableField(exist = false)
    private DependentParameters dependency;

    /**
     * switch dependency
     */
    @TableField(exist = false)
    private SwitchParameters switchDependency;

    /**
     * duration
     */
    @TableField(exist = false)
    private String duration;

    /**
     * max retry times
     */
    private int maxRetryTimes;

    /**
     * task retry interval, unit: minute
     */
    private int retryInterval;

    /**
     * task intance priority
     */
    private Priority taskInstancePriority;

    /**
     * process intance priority
     */
    @TableField(exist = false)
    private Priority processInstancePriority;

    /**
     * dependent state
     */
    @TableField(exist = false)
    private String dependentResult;


    /**
     * workerGroup
     */
    private String workerGroup;

    /**
     * environment code
     */
    private Long environmentCode;

    /**
     * environment config
     */
    private String environmentConfig;

    /**
     * executor id
     */
    private int executorId;

    /**
     * varPool string
     */
    private String varPool;

    /**
     * executor name
     */
    @TableField(exist = false)
    private String executorName;


    @TableField(exist = false)
    private Map<String, String> resources;

    /**
     * delay execution time.
     */
    private int delayTime;

    /**
     * task params
     */
    private String taskParams;

    /**
     * dry run flag
     */
    private int dryRun;

    public void init(String host, Date startTime, String executePath) {
        this.host = host;
        this.startTime = startTime;
        this.executePath = executePath;
    }

    public List<TaskInstanceFullLogPath> getTaskInstanceFullLogPath() {
        if (this.taskInstanceFullLogPath == null) {
            this.taskInstanceFullLogPath = JSONUtils.toList(this.fullLogPath, TaskInstanceFullLogPath.class);
        }
        return this.taskInstanceFullLogPath;
    }

    public void setTaskInstanceFullLogPath(List<TaskInstanceFullLogPath> taskInstanceFullLogPaths) {
        if (this.taskInstanceFullLogPath != null) {
            Set<String> taskInstanceFullLogPathSet = this.taskInstanceFullLogPath.stream()
                    .map(taskInstanceFullLogPath -> taskInstanceFullLogPath.getHost() + "-" + taskInstanceFullLogPath.getLogPath())
                    .collect(Collectors.toSet());
            for (TaskInstanceFullLogPath inputTaskInstanceFullLogPath : taskInstanceFullLogPaths) {
                String key = inputTaskInstanceFullLogPath.getHost() + "-" + inputTaskInstanceFullLogPath.getLogPath();
                if (!taskInstanceFullLogPathSet.contains(key)) {
                    this.taskInstanceFullLogPath.add(inputTaskInstanceFullLogPath);
                }
            }
            this.taskInstanceFullLogPath = this.taskInstanceFullLogPath.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(o -> o.getHost() + "-" + o.getLogPath()))), ArrayList::new
                    ));
        } else {
            this.taskInstanceFullLogPath = taskInstanceFullLogPaths.stream()
                    .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(o -> o.getHost() + "-" + o.getLogPath()))), ArrayList::new
                    ));
        }

        this.fullLogPath = JSONUtils.toJsonString(this.taskInstanceFullLogPath);
    }

    public DependentParameters getDependency() {
        if (this.dependency == null) {
            Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {});
            this.dependency = JSONUtils.parseObject((String) taskParamsMap.get(Constants.DEPENDENCE), DependentParameters.class);
        }
        return this.dependency;
    }

    public SwitchParameters getSwitchDependency() {
        if (this.switchDependency == null) {
            Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {});
            this.switchDependency = JSONUtils.parseObject((String) taskParamsMap.get(Constants.SWITCH_RESULT), SwitchParameters.class);
        }
        return this.switchDependency;
    }

    public void setSwitchDependency(SwitchParameters switchDependency) {
        Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {});
        taskParamsMap.put(Constants.SWITCH_RESULT,JSONUtils.toJsonString(switchDependency));
        this.setTaskParams(JSONUtils.toJsonString(taskParamsMap));
    }

    public boolean isTaskComplete() {
        return this.getState().typeIsSuccess()
                || this.getState().typeIsCancel()
                || (this.getState().typeIsFailure() && !taskCanRetry());
    }

    public boolean isSubProcess() {
        return TaskType.SUB_PROCESS.getDesc().equalsIgnoreCase(this.taskType);
    }

    public boolean isDependTask() {
        return TaskType.DEPENDENT.getDesc().equalsIgnoreCase(this.taskType);
    }

    public boolean isConditionsTask() {
        return TaskType.CONDITIONS.getDesc().equalsIgnoreCase(this.taskType);
    }

    public boolean isSwitchTask() {
        return TaskType.SWITCH.getDesc().equalsIgnoreCase(this.taskType);
    }

    /**
     * determine if you can try again
     *
     * @return can try result
     */
    public boolean taskCanRetry() {
        if (this.isSubProcess()) {
            return false;
        }
        if (this.getState() == ExecutionStatus.NEED_FAULT_TOLERANCE) {
            return true;
        } else {
            return (this.getState().typeIsFailure()
                    && this.getRetryTimes() <= this.getMaxRetryTimes());
        }
    }

    /**
     * whether the retry interval is timed out
     *
     * @return Boolean
     */
    public boolean retryTaskIntervalOverTime() {
        if (getState() != ExecutionStatus.FAILURE) {
            return false;
        }
        if (getId() == 0
                || getMaxRetryTimes() == 0
                || getRetryInterval() == 0) {
            return false;
        }
        Date now = new Date();
        long failedTimeInterval = DateUtils.differSec(now, getEndTime());
        // task retry does not over time, return false
        return (long) getRetryInterval() * SEC_2_MINUTES_TIME_UNIT < failedTimeInterval;
    }

    public boolean isFirstRun() {
        return endTime == null;
    }

}
