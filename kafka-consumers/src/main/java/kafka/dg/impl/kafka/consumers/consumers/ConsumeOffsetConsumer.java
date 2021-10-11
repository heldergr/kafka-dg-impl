package kafka.dg.impl.kafka.consumers.consumers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;

/**
 * This class is used for implementation example and should not be instantiated.
 */
public abstract class ConsumeOffsetConsumer extends StringStringConsumer {
    public static final String CONSUMER_SPECIFIC_OFFSET = "consumer.specific.offset";

    public ConsumeOffsetConsumer() {
        super("group.specific-offset");
    }

    public void consume() {
        final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        try (final var consumer = this.createConsumer()) {
            final var saveOffsetsAndRebalance = new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                    commitDBTransaction();
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                    collection.forEach(topicPartition -> {
                        consumer.seek(topicPartition, getOffsetFromDB(topicPartition));
                    });
                }
            };
            consumer.subscribe(Collections.singletonList(CONSUMER_SPECIFIC_OFFSET), saveOffsetsAndRebalance);
            consumer.poll(Duration.ofMillis(0));
            for (TopicPartition topicPartition: consumer.assignment()) {
                consumer.seek(topicPartition, getOffsetFromDB(topicPartition));
            }

            System.out.println("Starting consuming messages for specific location...");

            while (this.keepRunning) {
                final var consumeRecords = consumer.poll(Duration.ofMillis(100));
                consumeRecords.forEach(record -> {
                    System.out.println("Message " + record.value() + " consumed from offset " + record.offset() +
                            " of partition " + record.partition());
                    storeRecordInDB(record);
                    storeOffsetInDB(record.topic(), record.partition(), record.offset());
                });
                commitDBTransaction();
            }

            consumer.commitSync(offsets);
        } finally {
            System.out.println("Closing rebalance listener consumer...");
        }
    }

    protected abstract void storeOffsetInDB(String topic, int partition, long offset);

    protected abstract void storeRecordInDB(ConsumerRecord<String, String> record);

    private OffsetAndMetadata getOffsetFromDB(final TopicPartition partition) {
        return null;
    }

    private void commitDBTransaction() {
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
        return ConsumeOffsetConsumer.CONSUMER_SPECIFIC_OFFSET;
    }
}
