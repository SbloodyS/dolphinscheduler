package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClickHouseConfig {
    @JsonProperty("nullAsDefault")
    private String nullAsDefault = "2";
}
