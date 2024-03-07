package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import lombok.Data;

@Data
public class DataSourceNew {
  private int id;
  private String datasourceName;
  private String datasourceDesc;
  private String datasourceType;
  private String hostname;
  private Integer port;
  private String userName;
  private String password;
  private String databaseName;
  private String schemaName;
  private String driverName;
}
