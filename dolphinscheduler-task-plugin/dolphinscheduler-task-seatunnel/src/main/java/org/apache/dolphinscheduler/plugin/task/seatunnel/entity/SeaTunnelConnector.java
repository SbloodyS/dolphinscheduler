package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum SeaTunnelConnector {
    KAFKA("kafka"),
    HDFS("hdfs"),
    HIVE("hive"),
    MYSQL("mysql"),
    ORACLE("oracle"),
    POSTGRESQL("postgresql"),
    SQLSERVER("sqlserver"),
    CLICKHOUSE("clickHouse"),
    ELASTICSEARCH("elasticsearch"),
    REDIS("redis"),
    MONGODB("mongodb"),
    INFLUXDB("influxdb"),
    UNKNOWN("unknown");

    private final String name;

    SeaTunnelConnector(String name) {
        this.name = name;
    }

    private static final Map<String, SeaTunnelConnector> CONNECTOR_MAP = new HashMap<>();

    static {
        for (SeaTunnelConnector seaTunnelConnector : SeaTunnelConnector.values()) {
            CONNECTOR_MAP.put(seaTunnelConnector.name, seaTunnelConnector);
        }
    }

    public static SeaTunnelConnector of(String name) {
        return CONNECTOR_MAP.getOrDefault(name, SeaTunnelConnector.UNKNOWN);
    }
}
