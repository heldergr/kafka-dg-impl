package kafka.dg.impl.kafka.producers.producers;

import org.apache.kafka.clients.producer.ProducerRecord;

public class SimpleSynchronousProducer extends StringStringProducer {

    public void producer(final String topic, final String key, final String value) {
        final var record = new ProducerRecord<>(topic, key, value);
        try {
            // get() method waits synchronously until the message is sent or an error is risen
            final var recordMetadata = this.producer.send(record).get();
            System.out.println("Message send to topic " + recordMetadata.topic() + " and partition " + recordMetadata.partition() +
                    " with offset " + recordMetadata.offset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
