package nlp;

import nlp.preprocess.DocumentProcessor;
import nlp.textrank.TextRankSummarizer;
import nlp.utils.Score;
import nlp.utils.Sentence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ArticleSummarizer {
    private static String fileSeparator = System.getProperty("file.separator");

    private DocumentProcessor documentProcessor;
    private TextRankSummarizer textRankSummarizer;

    public ArticleSummarizer() {
        documentProcessor = new DocumentProcessor();
        textRankSummarizer = new TextRankSummarizer();
    }

    public String summarize(String filePath, int maxWords) {
        String article = documentProcessor.convertToString(filePath);
        StringBuilder stringBuilder = new StringBuilder();
        List<Sentence> sentences = documentProcessor.retrieveSentencesFromArticle(article);
        List<Score> finalScores = calculateSentencesScores(sentences, maxWords);

        finalScores.forEach(score -> stringBuilder.append(sentences.get(score.getSentenceId()).toString().trim()).append("\n"));

        return stringBuilder.toString();
    }

    private List<Score> calculateSentencesScores(List<Sentence> sentences, int maxWords) {
        Map<Integer, Score> sentencesScores = new HashMap<>();
        List<Sentence> sentencesCopy = new ArrayList<>(sentences);
        String idfFileName = String.format(".%sresources%sus_presidents.csv", fileSeparator, fileSeparator);

        List<Score> scores = textRankSummarizer.rankSentences(sentences, documentProcessor, idfFileName);
        scores.forEach(score -> sentencesScores.put(score.getSentenceId(), score));

        List<Score> finalScores = new ArrayList<>();

        int currentWordCount = 0;
        maxWords = Math.max(maxWords, 0);
        while (!sentencesCopy.isEmpty()) {
            int sentenceId = getBestSentenceId(sentencesCopy, sentencesScores);
            if (sentenceId == -1) {
                break;
            }

            Sentence bestSentence = sentences.get(sentenceId);
            finalScores.add(sentencesScores.get(bestSentence.getSentenceId()));
            currentWordCount += bestSentence.getWordCount();
            sentencesCopy.remove(bestSentence);

            if (currentWordCount > maxWords) {
                break;
            }
        }

        return finalScores;
    }

    private int getBestSentenceId(List<Sentence> sentences, Map<Integer, Score> sentencesScores) {
        int bestSentenceId = -1;
        double bestScore = 0;

        for (Sentence sentence : sentences) {
            Score score = sentencesScores.get(sentence.getSentenceId());

            if (score != null && score.getScore() > bestScore) {
                bestSentenceId = score.getSentenceId();
                bestScore = score.getScore();
            }
        }

        return bestSentenceId;
    }

    public static void main(String[] args) {
        Path filesDirectoryPath = Paths.get(String.format(".%sarticles", fileSeparator));
        File filesDirectory = new File(filesDirectoryPath.toString());
        File[] files = filesDirectory.listFiles();
        ArticleSummarizer articleSummarizer = new ArticleSummarizer();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                Path filePath = Paths.get(String.format("%s%s%s", filesDirectoryPath, fileSeparator, fileName));
                Path summaryArticlePath = Paths.get(String.format(".%s%s%s%s", fileSeparator, filesDirectoryPath, fileSeparator, fileName.replace(".txt", "_Summary.txt")));

                try {
                    File summaryArticleFile = new File(summaryArticlePath.toString());
                    if (!summaryArticleFile.exists()) {
                        Files.createFile(summaryArticlePath);
                    }

                    String summary = articleSummarizer.summarize(filePath.toString(), 100);
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(summaryArticleFile))) {
                        bufferedWriter.write(summary);
                    }
                } catch (IOException e) {
                    Logger.getAnonymousLogger().warning(String.format("Unable to write summary into a file: %s", e.getMessage()));
                }
            }
        }
    }
}