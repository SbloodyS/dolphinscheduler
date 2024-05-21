package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SQLServerSinkParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Jdbc";
    private String driver;
    private String url;
    private String user;
    private String password;
    private String query;

    @JsonProperty("enable_upsert")
    private String enableUpsert = "false";
}
