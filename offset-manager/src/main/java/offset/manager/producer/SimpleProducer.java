package offset.manager.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;

public class SimpleProducer extends AbstractProducer<String, String> {

    public void producer(final String topic, final String key, final String value) throws ExecutionException, InterruptedException {
        final var record = new ProducerRecord<>(topic, key, value);
        // callback handler will handle response when message is sent or an error is thrown
        final var recordMetadata = this.producer.send(record).get();
        System.out.println("Message send to topic " + recordMetadata.topic() + " and partition " + recordMetadata.partition() +
                " with offset " + recordMetadata.offset());
    }
}
