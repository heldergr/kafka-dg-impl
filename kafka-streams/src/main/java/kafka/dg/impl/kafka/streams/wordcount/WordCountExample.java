package kafka.dg.impl.kafka.streams.wordcount;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

public class WordCountExample {
    public static void main(String[] args) throws InterruptedException {
        final var props = new Properties();
        // Every kafka streams application must have an application id, which must be unique.
        // It is used to coordinate the instances of application and also naming the internal local
        // stores and  topics related to them
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Build topology
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, String> source = streamsBuilder.stream("wordcount-input");

        Pattern pattern = Pattern.compile("\\W+");

        KStream counts = source.flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
                .map((key, value) -> new KeyValue<>(value, value))
                .filter((key, value) -> !value.equals("the"))
                .groupByKey()
                .count(Named.as("CountStore"))
                .mapValues(value -> Long.toString(value))
                .toStream();
        counts.to("wordcount-output");

        // Run it
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
        kafkaStreams.start();

        // usually the stream application would be running forever, in this example we just let it run for some
        // time and stop since the input data is finite
        Thread.sleep(5000L);

        kafkaStreams.close();
    }
}
