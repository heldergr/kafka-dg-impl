package kafka.dg.impl.kafka.common.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class DummyProducer<K, V> {
    private final String keySerializer;
    private final String valueSerializer;
    private KafkaProducer<K, V> producer;

    public DummyProducer() {
        this(StringSerializer.class.getName(), StringSerializer.class.getName());
    }

    public DummyProducer(final String keySerializer, final String valueSerializer) {
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.producer = new KafkaProducer<K,V>(this.createProperties());
    }

    public void produce(final String topic, final K key, final V value) {
        final var producerRecord = new ProducerRecord<>(topic, key, value);
        producer.send(producerRecord, (recordMetadata, e) -> {
            if (e == null) {
                System.out.println("Mensagem enviada com sucesso para topic " + topic + ", partition " +
                        recordMetadata.partition() + " com offset " + recordMetadata.offset());
            } else {
                e.printStackTrace();
            }
        });
    }

    protected Properties createProperties() {
        final var properties = new Properties();
        properties.put("bootstrap.servers", "localhost:29092");
        properties.put("key.serializer", this.keySerializer);
        properties.put("value.serializer", this.valueSerializer);
        return properties;
    }
}
