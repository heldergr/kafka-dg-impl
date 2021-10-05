package kafka.dg.impl.kafka.producers.producers;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class SimpleAsynchronousProducer extends StringStringProducer {

    public void producer(final String topic, final String key, final String value) {
        final var record = new ProducerRecord<>(topic, key, value);
        // callback handler will handle response when message is sent or an error is thrown
        this.producer.send(record, new DemoProducerCallback());
    }

    private class DemoProducerCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                e.printStackTrace();
            } else {
                System.out.println("Message send to topic " + recordMetadata.topic() + " and partition " + recordMetadata.partition() +
                        " with offset " + recordMetadata.offset());
            }
        }
    };
}
