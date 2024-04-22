package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import lombok.Data;

@Data
public class CustomParams {
    private Boolean autoCreateHiveTable;
    private String sinkHiveTablePartition;
}
