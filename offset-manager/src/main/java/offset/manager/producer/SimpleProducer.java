package offset.manager.producer;

import offset.manager.util.OffsetRange;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SimpleProducer extends AbstractProducer<String, String> {

    public OffsetRange produce(final String topic, final int amount) throws ExecutionException, InterruptedException {
        return this.produce(topic, amount, "value ");
    }

    public OffsetRange produce(final String topic, final int amount, final String valuePrefix) throws ExecutionException, InterruptedException {
        final List<Long> offsets = new ArrayList<>();
        for (int i=0; i<amount; i++) {
            offsets.add(this.produce(topic, "key-" + i, valuePrefix + i));
        }
        Collections.sort(offsets);
        return new OffsetRange(offsets.get(0), offsets.get(offsets.size() - 1));
    }

    public long produce(final String topic, final String key, final String value) throws ExecutionException, InterruptedException {
        final var record = new ProducerRecord<>(topic, key, value);
        final var recordMetadata = this.producer.send(record).get();
        System.out.println("Message " + value + " sent to topic " + recordMetadata.topic() + " and partition " + recordMetadata.partition() +
                " with offset " + recordMetadata.offset());
        return recordMetadata.offset();
    }
}
