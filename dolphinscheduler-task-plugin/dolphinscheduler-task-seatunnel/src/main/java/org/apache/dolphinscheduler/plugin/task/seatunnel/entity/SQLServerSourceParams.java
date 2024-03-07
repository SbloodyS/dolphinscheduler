package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SQLServerSourceParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Jdbc";
    private String driver;
    private String url;
    private String user;
    private String password;
    private String query;

    @JsonProperty("partition_column")
    private String partitionColumn;

    @JsonProperty("partition_num")
    private Integer partitionNum;
}
