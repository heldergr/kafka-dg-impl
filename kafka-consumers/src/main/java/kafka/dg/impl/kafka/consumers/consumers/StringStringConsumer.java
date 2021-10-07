package kafka.dg.impl.kafka.consumers.consumers;

import java.util.Properties;

public class StringStringConsumer extends AbstractConsumer<String, String> {

    public StringStringConsumer(String groupId) {
        super(groupId);
    }

    protected Properties getProperties() {
        final var props = super.getProperties();
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }
}
