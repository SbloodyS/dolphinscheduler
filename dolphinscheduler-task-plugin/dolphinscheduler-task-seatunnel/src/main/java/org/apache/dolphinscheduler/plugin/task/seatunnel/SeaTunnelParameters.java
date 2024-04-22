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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.dolphinscheduler.plugin.task.seatunnel.entity.CustomParams;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class SeaTunnelParameters extends AbstractParameters {

    private Object sourceDataSourceInfo;
    private Object targetDataSourceInfo;
    private Object env;
    private Object source;
    private Object sink;
    private String sinkBeforeSql;
    private CustomParams customParams;

    @JsonIgnore
    private List<ResourceInfo> resourceList;

    @Override
    public boolean checkParameters() {
        return sourceDataSourceInfo != null && targetDataSourceInfo != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }
}
