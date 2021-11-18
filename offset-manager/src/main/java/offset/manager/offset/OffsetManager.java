package offset.manager.offset;

import offset.manager.properties.ConsumerProperties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.stream.Collectors;

public class OffsetManager {
    public void seekToBeginning(final String topic) {
        System.out.println("Resetting offset to beginning...");
        final var props = ConsumerProperties.createProperties();
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
            final var partitionInfos = consumer.partitionsFor(topic);
            final var topicPartitions = partitionInfos.stream().map(this::createTopicPartition).collect(Collectors.toList());
            consumer.seekToBeginning(topicPartitions);
        }
    }

    private TopicPartition createTopicPartition(final PartitionInfo partitionInfo) {
        return new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
    }
}
