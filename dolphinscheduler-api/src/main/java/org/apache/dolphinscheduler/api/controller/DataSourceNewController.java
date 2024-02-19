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

package org.apache.dolphinscheduler.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataSourceNewService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.DataSourceNew;
import org.apache.dolphinscheduler.dao.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_DATA_SOURCE_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_DATASOURCE_ERROR;

/**
 * data source new controller
 */
@Api(tags = "DATA_SOURCE_TAG")
@RestController
@RequestMapping("datasourceNew")
public class DataSourceNewController extends BaseController {

    @Autowired
    private DataSourceNewService dataSourceNewService;


    @ApiOperation(value = "createDataSource", notes = "CREATE_DATA_SOURCE_NOTES")
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result createDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @ApiParam(name = "dataSourceParam", value = "DATA_SOURCE_PARAM", required = true)
                                   @RequestBody DataSourceNew dataSourceNew) {
        return dataSourceNewService.createDataSourceNew(loginUser, dataSourceNew);
    }

    @ApiOperation(value = "updateDataSource", notes = "UPDATE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "dataSourceParam", value = "DATA_SOURCE_PARAM", required = true, dataType = "BaseDataSourceParamDTO")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result updateDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestBody DataSourceNew dataSourceNew) {
        return dataSourceNewService.updateDataSource(loginUser, dataSourceNew);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryDataSourceById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @PathVariable(value = "id") int dataSourceNewId) {
        return dataSourceNewService.queryDataSourceNewById(loginUser, dataSourceNewId);
    }

    @ApiOperation(value = "queryDataSourceListPaging", notes = "QUERY_DATA_SOURCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "20")
    })
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryDataSourceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "searchVal", required = false) String searchVal,
                                            @RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("pageSize") Integer pageSize) {
        Result result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return dataSourceNewService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
    }

    @ApiOperation(value = "deleteDataSource", notes = "DELETE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_DATA_SOURCE_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result delete(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                         @PathVariable("id") int id) {
        return dataSourceNewService.deleteDataSourceNew(loginUser, id);
    }

    @ApiOperation(value = "queryDataSourceListPaging", notes = "QUERY_DATA_SOURCE_LIST_PAGING_NOTES")
    @GetMapping(value = "/idList")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result queryDataSourceNewIdList() {
        return dataSourceNewService.queryDataSourceNewIdList();
    }

}
