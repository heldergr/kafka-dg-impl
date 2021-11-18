package offset.manager;

import offset.manager.producer.SimpleProducer;

import java.util.concurrent.ExecutionException;

public class ProducerApplication {

    public static final String OFFSET_MANAGER = "offset-manager";

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final var asp = new SimpleProducer();
        asp.produce(OFFSET_MANAGER, 10);
    }
}
