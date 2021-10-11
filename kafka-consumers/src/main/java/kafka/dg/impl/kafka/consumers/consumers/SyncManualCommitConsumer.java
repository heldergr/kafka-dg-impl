package kafka.dg.impl.kafka.consumers.consumers;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class SyncManualCommitConsumer extends StringStringConsumer {
    public static final String SYNCH_MANUAL_OFFSET = "consumer.synch.manual.offset";

    public SyncManualCommitConsumer() {
        super("group.synch-manual-commit");
    }

    public void consume() {
        try (final var consumer = this.createConsumer()) {
            consumer.subscribe(Collections.singletonList(SYNCH_MANUAL_OFFSET));
            System.out.println("Starting consuming messages for synch manual commit...");

            while (this.keepRunning) {
                final var consumeRecords = consumer.poll(Duration.ofMillis(100));
                consumeRecords.forEach(record -> {
                    System.out.println("Message " + record.value() + " consumed from offset " + record.offset() +
                            " of partition " + record.partition());
                });
                consumer.commitSync();
            }
        } finally {
            System.out.println("Closing synch manual consumer...");
        }
    }

    @Override
    protected Properties getProperties() {
        final var props = super.getProperties();
        props.put("enable.auto.commit", "false");
        return props;
    }

    @Override
    public String getTopic() {
        return SyncManualCommitConsumer.SYNCH_MANUAL_OFFSET;
    }
}
