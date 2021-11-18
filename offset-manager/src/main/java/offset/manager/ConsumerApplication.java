package offset.manager;

import offset.manager.consumer.SimpleConsumer;
import offset.manager.offset.OffsetManager;

public class ConsumerApplication {

    public static final String OFFSET_MANAGER = "offset-manager";

    public static void main(String[] args) {
        final var simpleConsumer = new SimpleConsumer();
        simpleConsumer.consumeAll(OFFSET_MANAGER);

        final var offsetManager = new OffsetManager();
        offsetManager.seekToBeginning(OFFSET_MANAGER);
    }
}
