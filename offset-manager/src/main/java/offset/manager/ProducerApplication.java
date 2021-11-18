package offset.manager;

import offset.manager.producer.SimpleProducer;

import java.util.concurrent.ExecutionException;

public class ProducerApplication {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final var asp = new SimpleProducer();
        final var topic = "offset-manager";
        for(int i=0; i<10; i++) {
            asp.producer(topic, "key " + i, "value " + i);
        }
    }
}
