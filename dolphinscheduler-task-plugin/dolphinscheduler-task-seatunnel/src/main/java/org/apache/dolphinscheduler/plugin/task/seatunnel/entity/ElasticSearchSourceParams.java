package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ElasticSearchSourceParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Elasticsearch";
    private String index;

    private List<String> hosts;

    @JsonProperty("username")
    private String userName;

    private String password;
    private Object query;
    private Object source;
    private String scrollTime;
    private Integer scrollSize;
}
