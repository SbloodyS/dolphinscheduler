package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeaTunnelConfig {
    private Env env;

    private List<Object> source;

    private List<Object> sink;
}
