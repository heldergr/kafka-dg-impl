package offset.manager;

import offset.manager.consumer.SimpleConsumer;
import offset.manager.offset.OffsetManager;
import offset.manager.producer.SimpleProducer;

import java.util.concurrent.ExecutionException;

public class OffsetApplication {

    public static final String OFFSET_MANAGER = "offset-manager";
    public static final String OFFSET_MANAGER_END = "offset-manager-end";
    public static final String OFFSET_MANAGER_SPECIFIC = "offset-manager-specific";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final var offsetApplication = new OffsetApplication();
        final var topic = OFFSET_MANAGER_SPECIFIC;

        if (OFFSET_MANAGER_END.equals(topic)) {
            offsetApplication.testSeekToEnd();
        } else if (OFFSET_MANAGER.equals(topic)) {
            offsetApplication.testSeekToBeginning();
        } else if (OFFSET_MANAGER_SPECIFIC.equals(topic)) {
            offsetApplication.testSeekTo();
        }

        // https://stackoverflow.com/questions/41008610/kafkaconsumer-0-10-java-api-error-message-no-current-assignment-for-partition/41010594
        // https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#seekToBeginning-java.util.Collection-

        // Criacao de topicos:
        // kafka-topics --bootstrap-server localhost:9092  --create --topic offset-manager-specific --partitions 3
        // kafka-topics --bootstrap-server localhost:9092  --create --topic offset-manager-end --partitions 3
        // kafka-topics --bootstrap-server localhost:9092  --create --topic offset-manager --partitions 3
    }

    private void testSeekTo() throws ExecutionException, InterruptedException {
        // Produces messages
        final var simpleProducer = new SimpleProducer();
        final var offsetRange = simpleProducer.produce(OFFSET_MANAGER_SPECIFIC, 12);
        System.out.println(offsetRange);

        // Seek to end
        final var offsetManager = new OffsetManager();
        offsetManager.seek(OFFSET_MANAGER_SPECIFIC, 7);

        // Consume messages
        final var simpleConsumer = new SimpleConsumer();
        simpleConsumer.consumeAll(OFFSET_MANAGER_SPECIFIC);
    }

    private void testSeekToEnd() throws ExecutionException, InterruptedException {
        // Produce messages not to be consumed
        final var simpleProducer = new SimpleProducer();
        simpleProducer.produce(OFFSET_MANAGER_END, 2, "NOT to be consumed - ");

        // Seek to end
        final var offsetManager = new OffsetManager();
        offsetManager.seekToEnd(OFFSET_MANAGER_END);

        // Produces messages to be consumed
        simpleProducer.produce(OFFSET_MANAGER_END, 2, "YES to be consumed - ");

        // Consume messages
        final var simpleConsumer = new SimpleConsumer();
        simpleConsumer.consumeAll(OFFSET_MANAGER_END);
    }

    private void testSeekToBeginning() {
        final var simpleConsumer = new SimpleConsumer();
        simpleConsumer.consumeAll(OFFSET_MANAGER);

        final var offsetManager = new OffsetManager();
        offsetManager.seekToBeginning(OFFSET_MANAGER);

        simpleConsumer.consumeAll(OFFSET_MANAGER);
    }
}
