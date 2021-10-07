package kafka.dg.impl.kafka.consumers.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;

public class SimpleConsumer extends StringStringConsumer {

    public SimpleConsumer(String groupId) {
        super(groupId);
    }

    public void consume() {
        try (final var consumer = this.createConsumer()) {
            consumer.subscribe(Collections.singletonList("consumerCountry"));
            while (true) {
                final var records = consumer.poll(Duration.ofMillis(100));
                records.forEach(this::consumeRecord);
            }
        }
    }

    private void consumeRecord(ConsumerRecord<String, String> record) {
        System.out.println(String.format("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n",
                record.topic(), record.partition(), record.offset(),
                record.key(), record.value()));
    }
}
