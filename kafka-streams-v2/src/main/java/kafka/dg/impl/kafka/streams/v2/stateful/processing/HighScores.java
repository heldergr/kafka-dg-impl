package kafka.dg.impl.kafka.streams.v2.stateful.processing;

import kafka.dg.impl.kafka.streams.v2.stateful.processing.model.Enriched;

import java.util.TreeSet;

public class HighScores {
    private final TreeSet<Enriched> highScores = new TreeSet<>();

    public HighScores add(Enriched value) {
        highScores.add(value);
        if (highScores.size() > 3) {
            highScores.remove(highScores.last());
        }
        return this;
    }
}
