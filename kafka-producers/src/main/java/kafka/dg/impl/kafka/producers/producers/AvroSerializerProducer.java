package kafka.dg.impl.kafka.producers.producers;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import kafka.dg.impl.kafka.producers.domain.Customer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class AvroSerializerProducer extends AbstractProducer<String, GenericRecord> {

    /**
     * Example with specific schema. This can also use objects generated by an Avro plugin.
     *
     * @param topic Topic name
     * @param key message key
     * @param customer Customer
     */
    public void producer(final String topic, final String key, final Customer customer) {
        String schemaString = "{\"namespace\": \"kafka.dg.impl.kafka.producers.domain\"," +
            "\"type\": \"record\", " +
            "\"name\": \"Customer\"," +
            "\"fields\": [" +
                "{\"name\": \"name\", \"type\": \"string\"}," +
                "{\"name\": \"ssn\", \"type\": \"string\"}" +
            "]}";
        final Schema.Parser parser = new Schema.Parser();
        final Schema schema = parser.parse(schemaString);

        final GenericRecord genericRecord = new GenericData.Record(schema);
        genericRecord.put("name", customer.getName());
        genericRecord.put("ssn", customer.getSsn());

        final ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord(topic, key, genericRecord);
        try {
            final var recordMetadata = this.producer.send(producerRecord).get();
            System.out.println("Message send to topic " + recordMetadata.topic() + " and partition " + recordMetadata.partition() +
                    " with offset " + recordMetadata.offset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Properties createProperties() {
        final var properties = super.createProperties();
        properties.setProperty("key.serializer", KafkaAvroSerializer.class.getName());
        properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
        properties.setProperty("schema.registry.url", "http://127.0.0.1:8081");
        return properties;
    }

}
