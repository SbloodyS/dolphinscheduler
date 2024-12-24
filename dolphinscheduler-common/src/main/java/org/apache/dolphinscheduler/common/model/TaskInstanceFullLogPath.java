package org.apache.dolphinscheduler.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskInstanceFullLogPath {
    private String host;
    private String logPath;
}
