package kafka.dg.impl.kafka.consumers.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AutomaticCommitConsumer extends StringStringConsumer {
    public static final String CONSUMER_AUTOMATIC_OFFSET = "consumer.automatic.offset";

    public AutomaticCommitConsumer() {
        super("group.automatic-commit");
    }

    public void consume() {
        try (final var consumer = this.createConsumer()) {
            consumer.subscribe(Collections.singletonList(CONSUMER_AUTOMATIC_OFFSET));
            System.out.println("Starting consuming messages for automatic commit...");

            while (this.keepRunning) {
                final var consumeRecords = consumer.poll(Duration.ofMillis(100));
                consumeRecords.forEach(record -> {
                    System.out.println("Message " + record.value() + " consumed from offset " + record.offset() +
                            " of partition " + record.partition());
                });
            }
        } finally {
            System.out.println("Closing automatic consumer...");
        }
    }

    @Override
    protected Properties getProperties() {
        final var props = super.getProperties();
        props.put("enable.auto.commit", "true"); // true is default, just to enforce it
        return props;
    }

    @Override
    public String getTopic() {
        return AutomaticCommitConsumer.CONSUMER_AUTOMATIC_OFFSET;
    }
}
