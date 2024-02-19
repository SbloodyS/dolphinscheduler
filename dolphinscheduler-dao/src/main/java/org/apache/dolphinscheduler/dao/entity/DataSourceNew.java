package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName(schema = "data_center", value = "datasource_info")
public class DataSourceNew {
  @TableId(value="id", type=IdType.AUTO)
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
  private Integer isAble;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
  @TableField(value = "createtime")
  private Date createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
  @TableField(value = "updatetime")
  private Date updateTime;
}
