package offset.manager.offset;

import offset.manager.properties.ConsumerProperties;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;

public class OffsetManager {
    public void seekToBeginning(final String topic) {
        System.out.println("Resetting offset to beginning...");
        final var props = ConsumerProperties.createProperties();
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
//            consumer.subscribe(Collections.singletonList(topic));
            final var partitionInfos = consumer.partitionsFor(topic);
            final var topicPartitions = partitionInfos.stream().map(this::createTopicPartition).collect(Collectors.toList());
            consumer.assign(topicPartitions);

            // seek to beginning eh lazy, precisa de um poll ou position ser chamado
            consumer.seekToBeginning(topicPartitions);
            // tente com poll e nao funcionou, nada foi consumido no teste apos
//            consumer.poll(Duration.ofMillis(100));
            // tentei com position e funcionou
            topicPartitions.forEach(consumer::position);

//            topicPartitions.forEach(tp -> consumer.seek(tp, 0));
        }
    }

    private TopicPartition createTopicPartition(final PartitionInfo partitionInfo) {
        return new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
    }
}
