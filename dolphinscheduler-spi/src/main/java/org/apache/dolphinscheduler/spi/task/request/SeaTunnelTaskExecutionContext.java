package org.apache.dolphinscheduler.spi.task.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class SeaTunnelTaskExecutionContext implements Serializable {
    private Object sourceDataSourceInfo;
    private Object targetDataSourceInfo;
    private Object env;
    private Object source;
    private Object sink;
    private String sinkBeforeSql;
    private Boolean autoCreateHiveTable;
}
