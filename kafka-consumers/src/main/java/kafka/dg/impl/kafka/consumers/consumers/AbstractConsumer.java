package kafka.dg.impl.kafka.consumers.consumers;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public abstract class AbstractConsumer<K, V> {
    private String groupId;
    protected boolean keepRunning = true;

    public AbstractConsumer(String groupId) {
        this.groupId = groupId;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
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

    protected void consume() {}

    public String getTopic() {
        return null;
    }
}
