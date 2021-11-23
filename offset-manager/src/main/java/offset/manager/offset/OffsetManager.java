package offset.manager.offset;

import offset.manager.properties.ConsumerProperties;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class OffsetManager {
    public void seek(final String topic, final int offset) {
        System.out.println("Resetting to specific offset " + offset);
        final var props = ConsumerProperties.createProperties();
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
//            consumer.subscribe(Collections.singletonList(topic));
            final var partitionInfos = consumer.partitionsFor(topic);
            final var topicPartitions = partitionInfos.stream().map(this::createTopicPartition).collect(Collectors.toList());
            // Precisa fazer assign para que nao de erro ao chamar o seek to beginning, que eh lazy
            consumer.assign(topicPartitions);

            // tente com poll e nao funcionou, nada foi consumido no teste apos
//            consumer.poll(Duration.ofMillis(100));
            // tentei com position e funcionou
            topicPartitions.forEach(topicPartition -> {
                consumer.seek(topicPartition, offset);
                consumer.position(topicPartition);
            });
        }
    }

    public void seekToBeginning(final String topic) {
        System.out.println("Resetting offset to beginning...");
        final var props = ConsumerProperties.createProperties();
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
//            consumer.subscribe(Collections.singletonList(topic));
            final var partitionInfos = consumer.partitionsFor(topic);
            final var topicPartitions = partitionInfos.stream().map(this::createTopicPartition).collect(Collectors.toList());
            // Precisa fazer assign para que nao de erro ao chamar o seek to beginning, que eh lazy
            consumer.assign(topicPartitions);

            // seek to beginning eh lazy, precisa de um poll ou position ser ter efeito efetivamente
            consumer.seekToBeginning(topicPartitions);
            // tente com poll e nao funcionou, nada foi consumido no teste apos
//            consumer.poll(Duration.ofMillis(100));
            // tentei com position e funcionou
            topicPartitions.forEach(consumer::position);
        }
    }

    public void seekToEnd(final String topic) {
        System.out.println("Resetting offset to beginning to end...");
        final var props = ConsumerProperties.createProperties();
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
            final var partitionInfos = consumer.partitionsFor(topic);
            final var topicPartitions = partitionInfos.stream().map(this::createTopicPartition).collect(Collectors.toList());

            // Precisa fazer assign para que nao de erro ao chamar o seek to beginning, que eh lazy
            // Na documetnacao fala que o subscribe seria suficiente mas nao foi
            // Erro: No current assignment for partition offset-manager-end-0
//            consumer.subscribe(Collections.singletonList(topic));
            // Eh necessario que as partitions sejam atribuidas ao consumer para que o reset funcione
            consumer.assign(topicPartitions);

            // seek to beginning eh lazy, precisa de um poll ou position ser ter efeito efetivamente
            consumer.seekToEnd(topicPartitions);
            // Passar um array vazio faz com que todas as partitions atribuidas a um consumer sejam resetadas
//            consumer.seekToEnd(new ArrayList<>());

            // tente com poll e nao funcionou, nada foi consumido no teste apos
//            consumer.poll(Duration.ofMillis(100));
            // tentei com position e funcionou
            topicPartitions.forEach(consumer::position);
        }
    }

    public void seekV2(final String topic, final int offset) {
        System.out.println("Resetting to specific offset " + offset);
        final BiConsumer<Consumer<String, String>, List<TopicPartition>> partitionSeeker = (consumer, topicPartitions) -> {
            topicPartitions.forEach(topicPartition -> consumer.seek(topicPartition, offset));
        };
        this.runSeek(topic, partitionSeeker);
    }

    public void seekToBeginningV2(final String topic) {
        System.out.println("Resetting offset to beginning...");
        this.runSeek(topic, (consumer, topicPartitions) -> consumer.seekToBeginning(topicPartitions));
    }

    public void seekToEndV2(final String topic) {
        System.out.println("Resetting offset to beginning to end...");
        this.runSeek(topic, (consumer, topicPartitions) -> consumer.seekToEnd(topicPartitions));
    }

    private void runSeek(final String topic, final BiConsumer<Consumer<String, String>, List<TopicPartition>> partitionSeeker) {
        final var props = ConsumerProperties.createProperties();
        try (final var consumer = new KafkaConsumer<String, String>(props)) {
            final var partitionInfos = consumer.partitionsFor(topic);
            final var topicPartitions = partitionInfos.stream().map(this::createTopicPartition).collect(Collectors.toList());
            consumer.assign(topicPartitions);
            partitionSeeker.accept(consumer, topicPartitions);
            topicPartitions.forEach(consumer::position);
        }
    }

    private TopicPartition createTopicPartition(final PartitionInfo partitionInfo) {
        return new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
    }
}
