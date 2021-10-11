package kafka.dg.impl.kafka.consumers.consumers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class StandaloneConsumer extends StringStringConsumer {
    public static final String CONSUMER_STANDALONE = "consumer.standalone";

    public StandaloneConsumer() {
        super("group.standalone");
    }

    public void consume() {
        try (final var consumer = this.createConsumer()) {
            final var partitionsInfo = consumer.partitionsFor(CONSUMER_STANDALONE);
            if (partitionsInfo != null) {
                consumer.assign(partitionsInfo.stream().map(this::getTopicPartition).collect(Collectors.toList()));

                while (this.keepRunning) {
                    final var consumeRecords = consumer.poll(Duration.ofMillis(100));
                    consumeRecords.forEach(record ->
                            System.out.println("Message " + record.value() + " consumed from offset " + record.offset() +
                            " of partition " + record.partition()));
                    consumer.commitSync();
                }
            }
        } finally {
            System.out.println("Closing standalone consumer...");
        }
    }

    private TopicPartition getTopicPartition(final PartitionInfo pi) {
        return new TopicPartition(pi.topic(), pi.partition());
    }

    @Override
    protected Properties getProperties() {
        final var props = super.getProperties();
        props.put("enable.auto.commit", "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Override
    public String getTopic() {
        return StandaloneConsumer.CONSUMER_STANDALONE;
    }
}
