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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.DataSourceNew;
import org.apache.dolphinscheduler.dao.entity.User;

public interface DataSourceNewService {

    Result<Object> createDataSourceNew(User loginUser, DataSourceNew dataSourceNew);

    Result<Object> updateDataSource(User loginUser, DataSourceNew dataSourceNew);

    Result<Object> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    Result<Object> deleteDataSourceNew(User loginUser, int datasourceId);

    Result<Object> queryDataSourceNewById(User loginUser, int datasourceId);
}
