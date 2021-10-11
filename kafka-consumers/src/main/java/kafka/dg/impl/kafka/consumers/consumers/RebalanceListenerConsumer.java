package kafka.dg.impl.kafka.consumers.consumers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;

public class RebalanceListenerConsumer extends StringStringConsumer {
    public static final String CONSUMER_REBALANCE_MANUAL_OFFSET = "consumer.rebalance.manual.offset";

    public RebalanceListenerConsumer() {
        super("group.rebalance-manual-commit");
    }

    public void consume() {
        final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        try (final var consumer = this.createConsumer()) {
            final var rebalanceListener = new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                    System.out.println("Lost partitions in rebalance. Commit offsets " + offsets);
                    consumer.commitSync(offsets);
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                }
            };
            consumer.subscribe(Collections.singletonList(CONSUMER_REBALANCE_MANUAL_OFFSET), rebalanceListener);
            System.out.println("Starting consuming messages for rebalance listener commit...");

            while (this.keepRunning) {
                final var consumeRecords = consumer.poll(Duration.ofMillis(100));
                consumeRecords.forEach(record -> {
                    System.out.println("Message " + record.value() + " consumed from offset " + record.offset() +
                            " of partition " + record.partition());
                    offsets.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1, "no metadata"));
                });
                consumer.commitAsync(offsets, null);
            }

            consumer.commitSync(offsets);
        } finally {
            System.out.println("Closing rebalance listener consumer...");
        }
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
        return RebalanceListenerConsumer.CONSUMER_REBALANCE_MANUAL_OFFSET;
    }
}
