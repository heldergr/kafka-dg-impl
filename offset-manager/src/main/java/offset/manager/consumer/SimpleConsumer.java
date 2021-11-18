package offset.manager.consumer;

import offset.manager.properties.ConsumerProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class SimpleConsumer {
    public void consumeAll(final String topic) {
        final var props = ConsumerProperties.createProperties(Map.of(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"));
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            int nTries = 0;
            System.out.println("Comecando a consumir...");
            while (nTries < 10) {
                final var records = consumer.poll(Duration.ofMillis(100));
                if (records.count() > 0) {
                    records.forEach(record -> {
                        System.out.println("Message " + record.value() + " with key " + record.key() + " consumed from partition " +
                                record.partition() + " with offset " + record.offset());
                    });
                } else {
                    nTries++;
                }
                Thread.sleep(1000);
            }
            System.out.println("Parando de consumir...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
