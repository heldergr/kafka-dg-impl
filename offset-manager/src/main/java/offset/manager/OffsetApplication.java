package offset.manager;

import offset.manager.consumer.SimpleConsumer;
import offset.manager.offset.OffsetManager;

public class OffsetApplication {

    public static final String OFFSET_MANAGER = "offset-manager";

    public static void main(String[] args) {
        final var offsetApplication = new OffsetApplication();
//        offsetApplication.testSeekToBeginning();

        // https://stackoverflow.com/questions/41008610/kafkaconsumer-0-10-java-api-error-message-no-current-assignment-for-partition/41010594
        // https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html#seekToBeginning-java.util.Collection-
    }

    private void testSeekTo() {
        System.out.println("Not implemented yet");
    }

    private void testSeekToEnd() {
        System.out.println("Not implemented yet");
    }

    private void testSeekToBeginning() {
        final var simpleConsumer = new SimpleConsumer();
        simpleConsumer.consumeAll(OFFSET_MANAGER);

        final var offsetManager = new OffsetManager();
        offsetManager.seekToBeginning(OFFSET_MANAGER);

        simpleConsumer.consumeAll(OFFSET_MANAGER);
    }
}
