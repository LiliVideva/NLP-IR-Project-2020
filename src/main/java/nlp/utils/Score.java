package nlp.utils;

public class Score implements Comparable<Score> {
    private int sentenceId;
    private double score;

    public Score(int sentenceId) {
        this.sentenceId = sentenceId;
    }

    public Score(int sentenceId, double score) {
        this.sentenceId = sentenceId;
        this.score = score;
    }

    public int getSentenceId() {
        return sentenceId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int compareTo(Score o) {
        return (Double.compare(o.getScore(), score));
    }

    public String toString() {
        return String.format("%d %.3f", sentenceId, score);
    }
}
