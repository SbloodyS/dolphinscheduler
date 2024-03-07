package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Env {
    @JsonProperty("job.mode")
    private String jobMode;

    @JsonProperty("job.name")
    private String jobName;

    @JsonProperty("parallelism")
    private Integer parallelism;
}
