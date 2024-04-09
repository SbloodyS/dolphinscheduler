package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElasticSearchSinkParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Elasticsearch";
    private String index;

    private List<String> hosts;

    @JsonProperty("username")
    private String userName;

    private String password;

    @JsonProperty("primary_keys")
    private Object primaryKeys;

    @JsonProperty("max_batch_size")
    private Integer maxBatchSize;

    @JsonProperty("schema_save_mode")
    private String schemaSaveMode;

    @JsonProperty("data_save_mode")
    private String dataSaveMode;
}
