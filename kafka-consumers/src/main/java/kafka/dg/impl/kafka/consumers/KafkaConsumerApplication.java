package kafka.dg.impl.kafka.consumers;

import kafka.dg.impl.kafka.common.producer.DummyProducer;
import kafka.dg.impl.kafka.consumers.consumers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KafkaConsumerApplication {
    final boolean runAll = false;

    public static void main(String[] args) throws InterruptedException {
        final KafkaConsumerApplication kafkaConsumerApplication = new KafkaConsumerApplication();
        final var executor = Executors.newFixedThreadPool(5);

        // starts consumer
        final var consumer = new StandaloneConsumer();
        executor.submit(() -> { consumer.consume(); });

        // produces messages
        kafkaConsumerApplication.produceMany(consumer.getTopic(), 5);

        // wait some time which we believe is enough to consume all messages
        final Future<Integer> result = runTimer(executor);
        while (!result.isDone()) {
            System.out.println("Not done yet, keep consuming...");
            Thread.sleep(1 * 1000);
        }

        // stop consumer
        consumer.setKeepRunning(false);
        System.out.println("Finished consuming, done!");
    }

    private void produceMany(final String topic, final int n) {
        final var dummyProducer = new DummyProducer<String, String>();
        for (int i = 0; i < n; i++) {
            dummyProducer.produce(topic, Integer.toString(i), "message " + i);
        }
    }

    private static Future<Integer> runTimer(ExecutorService executor) {
        final var result = executor.submit(() -> {
            try {
                final var timeMillis = 30 * 1000;
                Thread.sleep(timeMillis);
                return timeMillis;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return 0;
            }
        });
        return result;
    }
}
