package org.apache.dolphinscheduler.plugin.task.seatunnel.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KafkaSourceParams {
    @JsonProperty("plugin_name")
    private String pluginName = "Kafka";

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("bootstrap.servers")
    private String bootstrapServers;

    @JsonProperty("consumer.group")
    private String consumerGroup;

    @JsonProperty("commit_on_checkpoint")
    private String commitOnCheckpoint = "false";

    @JsonProperty("kafka.config")
    private KafkaConfig kafkaConfig;

    @JsonProperty("start_mode")
    private String startMode;

    @JsonProperty("format")
    private String format = "text";

    @JsonProperty("field_delimiter")
    private String fieldDelimiter = "\\u0007";

    @JsonProperty("schema")
    private Schema schema = new Schema();

    @Data
    private static class Schema {
        private Field fields = new Field();
    }

    @Data
    private static class Field {
        @JsonProperty("kafka_value")
        private String kafkaValue = "string";
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class KafkaConfig {
        @JsonProperty("client.id")
        private String clientId;

        @JsonProperty("max.poll.records")
        private String maxPollRecords;

        @JsonProperty("auto.offset.reset")
        private String autoOffsetReset = "earliest";

        @JsonProperty("enable.auto.commit")
        private String enableAutoCommit = "true";
    }
}
