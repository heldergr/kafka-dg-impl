package offset.manager.util;

import offset.manager.consumer.SimpleConsumer;

public class ConsumerApplication {
    public static final String OFFSET_MANAGER = "offset-manager";

    public static void main(String[] args) {
        final var simpleConsumer = new SimpleConsumer();
        simpleConsumer.consumeAll(OFFSET_MANAGER);
    }
}
