package nlp.textrank;

import nlp.preprocess.DocumentProcessor;
import nlp.preprocess.IdfWordWeight;
import nlp.preprocess.Stemmer;
import nlp.preprocess.StopWords;
import nlp.utils.Score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextRank {
    private StopWords stopWords;
    private Map<Integer, List<Integer>> links;
    private List<String> sentences;
    private IdfWordWeight wordWeight;
    private DocumentProcessor documentProcessor;

    public TextRank(DocumentProcessor documentProcessor, String fileName) {
        this.stopWords = new StopWords();
        this.links = new HashMap<>();
        this.sentences = new ArrayList<>();
        this.wordWeight = new IdfWordWeight(fileName);
        this.documentProcessor = documentProcessor;
    }

    public List<Score> getRankedSentences(Map<String, List<Integer>> wordIdsMap) {
        Map<String, Double> wordsWeights = new HashMap<>();

        for (String key : wordIdsMap.keySet()) {
            wordsWeights.put(key, wordWeight.getWordWeight(key));
        }

        List<Score> neighboursScores = getNeighboursSumWeightSimilarity(wordIdsMap, wordsWeights);

        return getTextRank(neighboursScores, wordsWeights);
    }

    // Raw score is sigma wtsimilarity of neighbors..
    // Used in the denominator of the Text rank formula..
    public List<Score> getNeighboursSumWeightSimilarity(Map<String, List<Integer>> wordIdsMap, Map<String, Double> wordsWeights) {
        List<Score> neighboursScores = new ArrayList<>();
        Stemmer stemmer = documentProcessor.getStemmer();

        String nextSentence;
        for (int i = 0; i < sentences.size(); i++) {
            nextSentence = sentences.get(i);
            String[] words = nextSentence.split(" ");
            List<Integer> processedIds = new ArrayList<>();
            Score score = new Score(i);

            for (String word : words) {
                String processedWord = stemmer.stem(word).toString();

                List<Integer> wordIds = wordIdsMap.get(processedWord);
                if (wordIds == null) {
                    continue;
                }

                for (int id : wordIds) {
                    if (id != i && !processedIds.contains(id)) {
                        double currentScore = calculateSimilarity(sentences.get(i), sentences.get(id), wordsWeights);
                        score.setScore(score.getScore() + currentScore);

                        if (currentScore > 0) {
                            addLink(i, id);
                        }
                        processedIds.add(id);
                    }
                }
            }
            neighboursScores.add(score);
        }
        return neighboursScores;
    }

    private double calculateSimilarity(String firstSentence, String secondSentence, Map<String, Double> wordsWeights) {
        String[] firstWords = firstSentence.split(" ");
        String[] secondWords = secondSentence.split(" ");
        double wordsInCommon = 0;
        Map<String, Boolean> duplicates = new HashMap<>();

        Double weight;
        for (String first : firstWords) {
            first = first.trim();
            if (!duplicates.containsKey(first)) {
                duplicates.put(first, true);

                for (String second : secondWords) {
                    if (stopWords.isStopWord(first) && !first.isEmpty() && first.equals(second.trim())) {
                        wordsInCommon += (wordsWeights.containsKey(first) ? wordsWeights.get(first) : 1);
                    }
                }
            }
        }
        return wordsInCommon / (firstWords.length + secondWords.length);
    }

    private void addLink(int index, int id) {
        List<Integer> neighboursList = links.get(index);
        if (neighboursList == null) {
            neighboursList = new ArrayList<>();
        }
        neighboursList.add(id);
        links.put(index, neighboursList);
    }

    private List<Score> getTextRank(List<Score> neighboursScores, Map<String, Double> wordWeights) {
        List<Score> vertexScores = new ArrayList<>();

        for (int i = 0; i < neighboursScores.size(); i++) {
            Score textRankI = new Score(neighboursScores.get(i).getSentenceId(), (1.00 / neighboursScores.size()));
            vertexScores.add(textRankI);
        }

        for (int i = 0; i < 100; i++) {
            double totalErrors = 0;
            List<Score> newWeightScores = new ArrayList<>();

            List<Integer> neighbours;
            for (Score score : neighboursScores) {
                int sentenceId = score.getSentenceId();
                neighbours = getLinks().get(sentenceId);
                double sum = 0;
                if (neighbours != null) {
                    for (int neighbour : neighbours) {
                        double weightFromJToI = calculateSimilarity(sentences.get(sentenceId), sentences.get(neighbour), wordWeights);
                        double sumWeightsFromJtoK = getScoreFromList(neighboursScores, neighbour);
                        double weightSimilarityToJ = getScoreFromList(vertexScores, neighbour);
                        sum += (weightFromJToI / sumWeightsFromJtoK) * weightSimilarityToJ;
                    }
                }
                double d = 0.85;
                Score weightSimilarityToI = new Score(sentenceId, (1 - d) + sum * d);
                totalErrors += (weightSimilarityToI.getScore() - getScoreFromList(neighboursScores, sentenceId));
                newWeightScores.add(weightSimilarityToI);
            }
            vertexScores = newWeightScores;

            double maxError = 0.1;
            if (i > 2 && (totalErrors / neighboursScores.size()) < maxError) {
                break;
            }
        }

        for (Score vScore : vertexScores) {
            vScore.setScore(vScore.getScore() * getScoreFromList(neighboursScores, vScore.getSentenceId()));
        }

        vertexScores.sort(Score::compareTo);

        return vertexScores;
    }

    private double getScoreFromList(List<Score> scores, int id) {
        return scores.stream().filter(score -> score.getSentenceId() == id).map(Score::getScore).findFirst().orElse(1.00);
    }

    public void setSentences(List<String> sentences) {
        this.sentences = sentences;
    }

    public Map<Integer, List<Integer>> getLinks() {
        return links;
    }
}