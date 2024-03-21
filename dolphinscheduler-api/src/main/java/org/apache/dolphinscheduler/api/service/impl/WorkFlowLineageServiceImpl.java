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

package org.apache.dolphinscheduler.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationTree;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * work flow lineage service impl
 */
@Service
@Slf4j
public class WorkFlowLineageServiceImpl extends BaseServiceImpl implements WorkFlowLineageService {

    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Override
    public Map<String, Object> queryWorkFlowLineageByName(long projectCode, String workFlowName, User loginUser) {
        Map<String, Object> result = new HashMap<>();
//        if (!(projectCode == 0 && loginUser.getUserType().equals(UserType.ADMIN_USER))) {
//            Project project = projectMapper.queryByCode(projectCode);
//            if (project == null) {
//                putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
//                return result;
//            }
//        }

        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryWorkFlowLineageByName(projectCode, workFlowName);
        result.put(Constants.DATA_LIST, workFlowLineageList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> queryWorkFlowLineageByCode(long projectCode, long workFlowCode, User loginUser) {
        Map<String, Object> result = new HashMap<>();
//        if (!(projectCode == 0 && loginUser.getUserType().equals(UserType.ADMIN_USER))) {
//            Project project = projectMapper.queryByCode(projectCode);
//            if (project == null) {
//                putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
//                return result;
//            }
//        }
        Map<Long, WorkFlowLineage> workFlowLineagesMap = new HashMap<>();
        Set<WorkFlowRelation> workFlowRelations = new HashSet<>();
        Set<Long> sourceWorkFlowCodes = Sets.newHashSet(workFlowCode);
        recursiveWorkFlow(workFlowLineagesMap, workFlowRelations, sourceWorkFlowCodes);

        List<WorkFlowRelationTree> workFlowRelationTreeList = createTree(new ArrayList<>(workFlowRelations), workFlowLineagesMap, 0);
        Map<String, Object> workFlowListMap = new HashMap<>();
//        workFlowListMap.put(Constants.WORKFLOW_LIST, workFlowLineagesMap.values());
//        workFlowListMap.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelations);
        workFlowListMap.put(Constants.WORKFLOW_RELATION_TREE, workFlowRelationTreeList.get(0));
        result.put(Constants.DATA_LIST, workFlowListMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<WorkFlowRelationTree> createTree(List<WorkFlowRelation> workFlowRelations,
                            Map<Long, WorkFlowLineage> workFlowLineagesMap,
                            long pid) {
        List<WorkFlowRelationTree> workFlowRelationTreeList = new ArrayList<>();
        for (WorkFlowRelation workFlowRelation : workFlowRelations) {
            if (workFlowRelation.getSourceWorkFlowCode() == pid) {
                WorkFlowRelationTree workFlowRelationTree = new WorkFlowRelationTree();
                workFlowRelationTree.setCode(workFlowRelation.getTargetWorkFlowCode());
                workFlowRelationTree.setName(workFlowLineagesMap.get(workFlowRelation.getTargetWorkFlowCode()).getWorkFlowName());
                workFlowRelationTree.setWorkFlowPublishStatus(Long.parseLong(workFlowLineagesMap.get(workFlowRelation.getTargetWorkFlowCode()).getWorkFlowPublishStatus()));
                workFlowRelationTree.setSchedulePublishStatus(workFlowLineagesMap.get(workFlowRelation.getTargetWorkFlowCode()).getSchedulePublishStatus());
                workFlowRelationTree.setChildren(createTree(workFlowRelations, workFlowLineagesMap, workFlowRelation.getTargetWorkFlowCode()));
                workFlowRelationTreeList.add(workFlowRelationTree);
            }
        }
        return workFlowRelationTreeList;

    }

    private void recursiveWorkFlow(Map<Long, WorkFlowLineage> workFlowLineagesMap,
                                   Set<WorkFlowRelation> workFlowRelations,
                                   Set<Long> sourceWorkFlowCodes) {
        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryWorkFlowLineageByCodes(new ArrayList<>(sourceWorkFlowCodes));
        Map<Long, WorkFlowLineage> workFlowLineageListMap = workFlowLineageList.stream().collect(Collectors.toMap(WorkFlowLineage::getWorkFlowCode, workFlowLineage -> workFlowLineage));
        List<ProcessLineage> processLineageList = workFlowLineageMapper.queryProcessLineageByCodes(new ArrayList<>(sourceWorkFlowCodes));
        Map<Long, List<ProcessLineage>> processLineageListMap = processLineageList.stream().collect(Collectors.groupingBy(ProcessLineage::getProcessDefinitionCode));

        for (Long workFlowCode : sourceWorkFlowCodes) {
            WorkFlowLineage workFlowLineage = workFlowLineageListMap.get(workFlowCode);
            workFlowLineagesMap.put(workFlowCode, workFlowLineage);
            List<ProcessLineage> processLineages = processLineageListMap.get(workFlowCode);
            List<TaskDefinition> taskDefinitionList = new ArrayList<>();
            for (ProcessLineage processLineage : processLineages) {
                if (processLineage.getPreTaskCode() > 0) {
                    taskDefinitionList.add(new TaskDefinition(processLineage.getPreTaskCode(), processLineage.getPreTaskVersion()));
                }
                if (processLineage.getPostTaskCode() > 0) {
                    taskDefinitionList.add(new TaskDefinition(processLineage.getPostTaskCode(), processLineage.getPostTaskVersion()));
                }
            }
            sourceWorkFlowCodes = querySourceWorkFlowCodes(workFlowCode, taskDefinitionList);
            if (sourceWorkFlowCodes.isEmpty()) {
                workFlowRelations.add(new WorkFlowRelation(0L, workFlowCode));
                return;
            } else {
                workFlowLineagesMap.get(workFlowCode).setSourceWorkFlowCode(StringUtils.join(sourceWorkFlowCodes, Constants.COMMA));
                sourceWorkFlowCodes.forEach(code -> workFlowRelations.add(new WorkFlowRelation(code, workFlowCode)));
                recursiveWorkFlow(workFlowLineagesMap, workFlowRelations, sourceWorkFlowCodes);
            }
        }
    }

    @Override
    public Map<String, Object> queryWorkFlowLineage(long projectCode, User loginUser) {
        Map<String, Object> result = new HashMap<>();
        List<Project> projectList = new ArrayList<>();
        if (projectCode == 0) {
            projectList = projectMapper.queryAllProject();
            Map<String, Object> workFlowListMap = getAllWorkFlowList(Collections.singletonList(projectList.get(projectList.size() - 1)));
            result.put(Constants.DATA_LIST, workFlowListMap);
            putMsg(result, Status.SUCCESS);
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
            return result;
        }
        projectList.add(project);
        Map<String, Object> workFlowListMap = getAllWorkFlowList(projectList);
        result.put(Constants.DATA_LIST, workFlowListMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private Map<String, Object> getAllWorkFlowList(List<Project> projectList) {
        Map<String, Object> workFlowListMap = new HashMap<>();
        Map<Long, WorkFlowLineage> workFlowLineagesMap = new HashMap<>();
        Set<WorkFlowRelation> workFlowRelations = new HashSet<>();
        for (Project project : projectList) {
            List<ProcessLineage> processLineages = workFlowLineageMapper.queryProcessLineage(project.getCode());
            if (!processLineages.isEmpty()) {
                List<WorkFlowLineage> workFlowLineages = workFlowLineageMapper.queryWorkFlowLineageByLineage(processLineages);
                workFlowLineagesMap.putAll(workFlowLineages.stream().collect(Collectors.toMap(WorkFlowLineage::getWorkFlowCode, workFlowLineage -> workFlowLineage)));
                Map<Long, List<TaskDefinition>> workFlowMap = new HashMap<>();
                for (ProcessLineage processLineage : processLineages) {
                    workFlowMap.compute(processLineage.getProcessDefinitionCode(), (k, v) -> {
                        if (v == null) {
                            v = new ArrayList<>();
                        }
                        if (processLineage.getPreTaskCode() > 0) {
                            v.add(new TaskDefinition(processLineage.getPreTaskCode(), processLineage.getPreTaskVersion()));
                        }
                        if (processLineage.getPostTaskCode() > 0) {
                            v.add(new TaskDefinition(processLineage.getPostTaskCode(), processLineage.getPostTaskVersion()));
                        }
                        return v;
                    });
                }
                for (Entry<Long, List<TaskDefinition>> workFlow : workFlowMap.entrySet()) {
                    Set<Long> sourceWorkFlowCodes = querySourceWorkFlowCodes(workFlow.getKey(), workFlow.getValue());
                    if (sourceWorkFlowCodes.isEmpty()) {
                        workFlowRelations.add(new WorkFlowRelation(0L, workFlow.getKey()));
                    } else {
                        workFlowLineagesMap.get(workFlow.getKey()).setSourceWorkFlowCode(StringUtils.join(sourceWorkFlowCodes, Constants.COMMA));
                        sourceWorkFlowCodes.forEach(code -> workFlowRelations.add(new WorkFlowRelation(code, workFlow.getKey())));
                    }
                }
            }
        }
//        workFlowListMap.put(Constants.WORKFLOW_LIST, workFlowLineagesMap.values());
//        workFlowListMap.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelations);
        List<WorkFlowRelationTree> workFlowRelationTreeList = createTree(new ArrayList<>(workFlowRelations), workFlowLineagesMap, 0);
        workFlowListMap.put(Constants.WORKFLOW_RELATION_TREE, workFlowRelationTreeList.get(0));
        return workFlowListMap;
    }

    private Set<Long> querySourceWorkFlowCodes(long workFlowCode, List<TaskDefinition> taskDefinitionList) {
        Set<Long> sourceWorkFlowCodes = new HashSet<>();
        if (taskDefinitionList == null || taskDefinitionList.isEmpty()) {
            return sourceWorkFlowCodes;
        }
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitionList);
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
//            if (taskDefinitionLog.getProjectCode() == projectCode) {
            if (taskDefinitionLog.getTaskType().equals(TaskType.DEPENDENT.getDesc())) {
                DependentParameters dependentParameters = JSONUtils.parseObject(taskDefinitionLog.getDependence(), DependentParameters.class);
                if (dependentParameters != null) {
                    List<DependentTaskModel> dependTaskList = dependentParameters.getDependTaskList();
                    for (DependentTaskModel taskModel : dependTaskList) {
                        List<DependentItem> dependItemList = taskModel.getDependItemList();
                        for (DependentItem dependentItem : dependItemList) {
                            if (dependentItem.getDefinitionCode() != workFlowCode) {
                                sourceWorkFlowCodes.add(dependentItem.getDefinitionCode());
                            }
                        }
                    }
                }
            }
        }
//        }
        return sourceWorkFlowCodes;
    }
}
