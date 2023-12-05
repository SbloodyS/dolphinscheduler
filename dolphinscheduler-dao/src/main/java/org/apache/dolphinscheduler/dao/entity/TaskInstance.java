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
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.task.switchtask.SwitchParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * task instance
 */
@Data
@TableName("t_ds_task_instance")
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

    public DependentParameters getDependency() {
        if (this.dependency == null) {
            Map<String, Object> taskParamsMap = JSONUtils.parseObject(this.getTaskParams(), new TypeReference<Map<String, Object>>() {});
            this.dependency = JSONUtils.parseObject((String) taskParamsMap.get(Constants.DEPENDENCE), DependentParameters.class);
        }
        return this.dependency;
    }

    public void setDependency(DependentParameters dependency) {
        this.dependency = dependency;
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

    public Map<String, String> getResources() {
        return resources;
    }

    public void setResources(Map<String, String> resources) {
        this.resources = resources;
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
        return getRetryInterval() * SEC_2_MINUTES_TIME_UNIT < failedTimeInterval;
    }

    @Override
    public String toString() {
        return "TaskInstance{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", taskType='" + taskType + '\''
                + ", processInstanceId=" + processInstanceId
                + ", processInstanceName='" + processInstanceName + '\''
                + ", state=" + state
                + ", firstSubmitTime=" + firstSubmitTime
                + ", submitTime=" + submitTime
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", host='" + host + '\''
                + ", executePath='" + executePath + '\''
                + ", logPath='" + logPath + '\''
                + ", retryTimes=" + retryTimes
                + ", alertFlag=" + alertFlag
                + ", processInstance=" + processInstance
                + ", processDefine=" + processDefine
                + ", pid=" + pid
                + ", appLink='" + appLink + '\''
                + ", flag=" + flag
                + ", dependency='" + dependency + '\''
                + ", duration=" + duration
                + ", maxRetryTimes=" + maxRetryTimes
                + ", retryInterval=" + retryInterval
                + ", taskInstancePriority=" + taskInstancePriority
                + ", processInstancePriority=" + processInstancePriority
                + ", dependentResult='" + dependentResult + '\''
                + ", workerGroup='" + workerGroup + '\''
                + ", environmentCode=" + environmentCode
                + ", environmentConfig='" + environmentConfig + '\''
                + ", executorId=" + executorId
                + ", executorName='" + executorName + '\''
                + ", delayTime=" + delayTime
                + ", dryRun=" + dryRun
                + '}';
    }

    public long getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(long taskCode) {
        this.taskCode = taskCode;
    }

    public int getTaskDefinitionVersion() {
        return taskDefinitionVersion;
    }

    public void setTaskDefinitionVersion(int taskDefinitionVersion) {
        this.taskDefinitionVersion = taskDefinitionVersion;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public boolean isFirstRun() {
        return endTime == null;
    }
}
