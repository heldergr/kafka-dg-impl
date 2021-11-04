package kafka.dg.impl.kafka.streams.v2.stateless.processing;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CryptoTopology {
    private static final List<String> currencies = Arrays.asList("crypt", "ethereum");

    public static Topology build() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<byte[], Tweet> stream = builder.stream("tweets", Consumed.with(Serdes.ByteArray(), new TweetSerdes()));
        stream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-stream"));

        KStream<byte[], Tweet> filtered = stream.filterNot((key, value) -> value.getRetweet());
        // This could be:
        // KStream<byte[], Tweet> filtered = stream.filter((key, value) -> !value.getRetweet());

        Predicate<byte[], Tweet> englishTweets = (key, value) -> value.getLang().equals("en");
        Predicate<byte[], Tweet> nonEnglishTweets = (key, value) -> !value.getLang().equals("en");

        KStream<byte[], Tweet>[] branches = filtered.branch(englishTweets, nonEnglishTweets);
        KStream<byte[], Tweet> englishStream = branches[0];
        KStream<byte[], Tweet> nonEnglishStream = branches[1];

        KStream<byte[], Tweet> translatedStream = nonEnglishStream.map((key, value) -> {
            byte[] newKey = value.getUsername().getBytes();
            Tweet translated = value; // There should have a translation step here
            return KeyValue.pair(newKey, translated);
        });

        KStream<byte[], Tweet> merged = englishStream.merge(translatedStream);

        // Enrichment with sentimental analysis
        KStream<byte[], EntitySentiment> enriched = merged.flatMapValues(tweet -> {
            // Here should have a service that generate the sentimental analysis
            List<EntitySentiment> sentiments = new ArrayList<>();
            sentiments.removeIf(entitySentiment -> !currencies.contains(entitySentiment.getEntity()));
            return sentiments;
        });

        enriched.to("crypto-sentiment",
                Produced.with(
                        Serdes.ByteArray(),
                        AvroSerdes.EntitySentiment("http://localhost:8081", false)
                ));

        return builder.build();
    }
}
