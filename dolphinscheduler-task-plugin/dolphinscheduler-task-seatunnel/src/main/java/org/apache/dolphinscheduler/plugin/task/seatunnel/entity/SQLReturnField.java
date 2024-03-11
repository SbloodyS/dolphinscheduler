package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SQLReturnField {
    private String fieldName;
    private String fieldType;
}
