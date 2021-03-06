package kafka.dg.impl.kafka.streams.v2.stateful.processing.model;

public class ScoreWithPlayer {
    private ScoreEvent scoreEvent;
    private Player player;

    public ScoreWithPlayer(ScoreEvent scoreEvent, Player player) {
        this.scoreEvent = scoreEvent;
        this.player = player;
    }

    public ScoreEvent getScoreEvent() {
        return scoreEvent;
    }

    public void setScoreEvent(ScoreEvent scoreEvent) {
        this.scoreEvent = scoreEvent;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
