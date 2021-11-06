package kafka.dg.impl.kafka.streams.v2.stateful.processing;

import kafka.dg.impl.kafka.streams.v2.stateful.processing.model.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Properties;

public class BoardGame {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, ScoreEvent> scoreEvents =
                builder.stream(
                        "score-events",
                        Consumed.with(Serdes.ByteArray(), JsonSerdes.ScoreEvent()))
                        .selectKey((key, value) -> value.getPlayerId().toString());
        KTable<String, Player> players =
                builder.table(
                        "players",
                        Consumed.with(Serdes.String(), JsonSerdes.Player()));
        GlobalKTable<String, Product> products =
                builder.globalTable(
                        "products",
                        Consumed.with(Serdes.String(), JsonSerdes.Product()));

        // Joining

        ValueJoiner<ScoreEvent, Player, ScoreWithPlayer> valueJoiner = (scoreEvent, player) -> new ScoreWithPlayer(scoreEvent, player);

        ValueJoiner<ScoreWithPlayer, Product, Enriched> productJoiner =
                (scoreWithPlayer, product) -> new Enriched(scoreWithPlayer, product);

        Joined<String, ScoreEvent, Player> playerJoinParams = Joined.with(Serdes.String(), JsonSerdes.ScoreEvent(), JsonSerdes.Player());

        KStream<String, ScoreWithPlayer> withPlayers = scoreEvents.join(players, valueJoiner, playerJoinParams);

        KeyValueMapper<String, ScoreWithPlayer, String> keyMapper =
                (leftKey, scoreWithPlayer) -> {
                    return String.valueOf(scoreWithPlayer.getScoreEvent().getProductId());
                };

        KStream<String, Enriched> withProducts =
                withPlayers.join(products, keyMapper, productJoiner);

        // Grouping by
        KGroupedStream<String, Enriched> grouped =
                withProducts.groupBy(
                        (key, value) -> value.getProductId().toString(),
                        Grouped.with(Serdes.String(), JsonSerdes.Enriched()));

//        KGroupedStream<String, Enriched> groupedByKey =
//                withProducts.groupByKey(
//                        Grouped.with(Serdes.String(),
//                                JsonSerdes.Enriched()));

//        KGroupedTable<String, Player> groupedPlayers =
//                players.groupBy(
//                        (key, value) -> KeyValue.pair(key, value),
//                        Grouped.with(Serdes.String(), JsonSerdes.Player()));

        Initializer<HighScores> highScoresInitializer = HighScores::new;

        Aggregator<String, Enriched, HighScores> highScoresAdder = (key, value, aggregator) -> aggregator.add(value);

        KTable<String, HighScores> highScores =
                grouped.aggregate(highScoresInitializer, highScoresAdder);

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Interactive queries
        KTable<String, HighScores> highScoresMaterialized =
                grouped.aggregate(
                        highScoresInitializer,
                        highScoresAdder,
                        Materialized.<String, HighScores, KeyValueStore<Bytes, byte[]>>
                                        as("leader-boards")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(JsonSerdes.HighScores()));

        ReadOnlyKeyValueStore<String, HighScores> stateStore =
                streams.store(
                        StoreQueryParameters.fromNameAndType(
                                "leader-boards",
                                QueryableStoreTypes.keyValueStore()));

        // Point lookup
        HighScores highScores1 = stateStore.get("key");

        // Querying by range
        KeyValueIterator<String, HighScores> range = stateStore.range(1, 7);

        // All entries
        KeyValueIterator<String, HighScores> all = stateStore.all();
    }

}
