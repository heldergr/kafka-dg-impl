package kafka.dg.impl.kafka.consumers.consumers;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AsyncManualCommitConsumer extends StringStringConsumer {
    public static final String ASYNCH_MANUAL_OFFSET = "consumer.asynch.manual.offset";

    public AsyncManualCommitConsumer() {
        super("group.asynch-manual-commit");
    }

    public void consume() {
        try (final var consumer = this.createConsumer()) {
            consumer.subscribe(Collections.singletonList(ASYNCH_MANUAL_OFFSET));
            System.out.println("Starting consuming messages for asynch manual commit...");

            while (this.keepRunning) {
                final var consumeRecords = consumer.poll(Duration.ofMillis(100));
                consumeRecords.forEach(record -> {
                    System.out.println("Message " + record.value() + " consumed from offset " + record.offset() +
                            " of partition " + record.partition());
                });
                consumer.commitAsync();
            }
        } finally {
            System.out.println("Closing asynch manual consumer...");
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
        return AsyncManualCommitConsumer.ASYNCH_MANUAL_OFFSET;
    }
}
