package kafka.dg.impl.kafka.consumers.consumers;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public abstract class AbstractConsumer<K, V> {
    private String groupId;

    public AbstractConsumer(String groupId) {
        this.groupId = groupId;
    }

    protected Properties getProperties() {
        final var properties = new Properties();
        properties.put("bootstrap.servers", "localhost:29092");

        if (groupId != null) {
            properties.put("group.id", this.groupId);
        }
        return properties;
    }

    protected KafkaConsumer<K, V> createConsumer() {
        return new KafkaConsumer<K, V>(this.getProperties());
    }
}
