package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClickHouseSourceParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Clickhouse";
    private String database;
    private String host;

    @JsonProperty("username")
    private String user;
    private String password;
    private String sql;
}
