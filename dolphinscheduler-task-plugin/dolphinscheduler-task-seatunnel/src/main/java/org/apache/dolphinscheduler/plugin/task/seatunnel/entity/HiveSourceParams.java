package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HiveSourceParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Hive";

    @JsonProperty("table_name")
    private String sourceTable;

    @JsonProperty("metastore_uri")
    private String metastoreUri;
}
