package kafka.dg.impl.kafka.producers;

import kafka.dg.impl.kafka.producers.domain.Customer;
import kafka.dg.impl.kafka.producers.producers.AvroSerializerProducer;
import kafka.dg.impl.kafka.producers.producers.SimpleSynchronousProducer;

public class KafkaProducerApplication {
    final boolean runAll = false;

    public static void main(String[] args) {
        final var kafkaProducerApplication = new KafkaProducerApplication();
        kafkaProducerApplication.sendAvroRecords();
        if (kafkaProducerApplication.runAll) {
            kafkaProducerApplication.sendSynchronously();
        }
    }

    private void sendAvroRecords() {
        final var avroProducer = new AvroSerializerProducer();
        avroProducer.producer("consumerContacts", "consumer-key", new Customer("my name", "my ssn"));
    }

    private void sendSynchronously() {
        final var syncProducer = new SimpleSynchronousProducer();
        syncProducer.producer("CustomerCountry", "Precision Products", "France");
    }
}
