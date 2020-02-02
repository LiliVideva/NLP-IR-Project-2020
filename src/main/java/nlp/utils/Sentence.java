package nlp.utils;

import nlp.preprocess.Stemmer;
import nlp.preprocess.StopWords;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Sentence {
    private StopWords stopWords;
    private int sentenceId;
    private String sentenceValue;
    private Score pageRankScore;
    private int wordCount;
    private List<Sentence> links;

    public Sentence() {
        stopWords = new StopWords();
        links = new ArrayList<>();
    }

    public Sentence(int sentenceId, String sentenceValue) {
        this();
        this.sentenceId = sentenceId;
        this.sentenceValue = sentenceValue;
        this.wordCount = calculateWordCount();
    }

    public String stem() {
        Stemmer stemmer = new Stemmer();
        int wordStart = 0;
        StringBuilder stringBuilder = new StringBuilder();
        BreakIterator wordIterator = BreakIterator.getWordInstance(Locale.US);
        wordIterator.setText(sentenceValue);

        String word;
        for (int wordEnd = wordIterator.next(); wordEnd != BreakIterator.DONE; wordStart = wordEnd, wordEnd = wordIterator.next()) {
            word = sentenceValue.substring(wordStart, wordEnd).replaceAll("[\"']", "");

            if (stopWords.isStopWord(word)) {
                stemmer.stem(word);
                stringBuilder.append(stemmer.toString()).append(" ");
            }
        }
        return stringBuilder.toString();
    }

    private int calculateWordCount() {
        int wordCount = 0;
        String[] words = sentenceValue.split(" ");
        for (String word : words) {
            if (stopWords.isStopWord(word) && !word.startsWith("'") && !word.equals(".") && !word.equals("?")) {
                wordCount++;
            }
        }
        return wordCount;
    }

    public int getSentenceId() {
        return sentenceId;
    }

    public String getSentenceValue() {
        return sentenceValue;
    }

    public void addLink(Sentence sentence) {
        links.add(sentence);
    }

    public String toString() {
        return sentenceValue;
    }

    public int getWordCount() {
        return wordCount == 0 ? sentenceValue.split(" ").length : wordCount;
    }

    public void setPageRankScore(Score pageRankScore) {
        this.pageRankScore = pageRankScore;
    }
}
