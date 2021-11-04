package kafka.dg.impl.kafka.streams.v2.stateless.processing;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public class CryptoApp {
    public static void main(String[] args) {
        Topology topology = CryptoTopology.build();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");

        KafkaStreams kafkaStreams = new KafkaStreams(topology, config);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        System.out.println("Starting Twitter streams");
        kafkaStreams.start();
    }
}
