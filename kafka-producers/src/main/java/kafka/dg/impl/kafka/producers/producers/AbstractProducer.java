package kafka.dg.impl.kafka.producers.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public abstract class AbstractProducer<K, V> {
    final protected KafkaProducer<K, V> producer = this.createProducer();

    private KafkaProducer<K, V> createProducer() {
        return new KafkaProducer<>(this.createProperties());
    }

    protected Properties createProperties() {
        final var properties = new Properties();
        properties.put("bootstrap.servers", "localhost:29092");
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());
        return properties;
    }
}
