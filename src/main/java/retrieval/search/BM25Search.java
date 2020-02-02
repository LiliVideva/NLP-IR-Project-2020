package retrieval.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BM25Search {
    private List<String> queryWords;
    private Map<String, Map<String, Integer>> queryWordsCount;
    private File[] files;
    private Map<String, Integer> filesWithWordsCount;
    private Map<String, Long> filesLength;
    private Map<String, Double> scores;
    private int filesCount;

    private BM25Search() {
        queryWordsCount = new HashMap<>();
        filesWithWordsCount = new HashMap<>();
        filesLength = new HashMap<>();
        scores = new HashMap<>();
    }

    public BM25Search(String searchQuery, File directory) {
        this();

        queryWords = Arrays.asList(searchQuery.split(" "));
        for (String word : queryWords) {
            filesWithWordsCount.put(word, 0);
        }

        files = directory.listFiles();

        if (files != null) {
            filesCount = files.length;
            queryWordsCount = initQueryWordsCount(files);

            for (File file : files) {
                filesLength.put(file.getName(), file.length());
            }
        }
    }

    private Map<String, Map<String, Integer>> initQueryWordsCount(File[] files) {
        Map<String, Map<String, Integer>> queryWordsCount = new HashMap<>();

        for (File file : files) {
            Map<String, Integer> amountInCurrentFile = new HashMap<>();
            for (String word : queryWords) {
                amountInCurrentFile.put(word, 0);
            }
            queryWordsCount.put(file.getName(), amountInCurrentFile);
        }

        return queryWordsCount;
    }

    public Map<String, Double> calculateScore() throws IOException {
        int fileWordCount;
        long fileLength;
        double idf;
        double calculation;

        float b = 2.0f;
        float k = 1.2f;
        double score;
        double averageFileLength = calculateAverageFileLength();

        countQueryWordsInFiles();

        for (File file : files) {
            score = 0.0;

            for (String word : queryWords) {
                fileWordCount = queryWordsCount.get(file.getName()).get(word);
                fileLength = filesLength.get(file.getName());
                idf = calculateIdf(word);

                calculation = (fileWordCount * (k + 1)) / (k * ((1 - b) + b * (fileLength / averageFileLength)) + fileWordCount);
                score += Math.abs(idf) * calculation;
            }
            scores.put(file.getName(), score);
        }

        return scores;
    }

    private void countQueryWordsInFiles() throws IOException {
        for (File file : files) {
            String line;

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                while ((line = bufferedReader.readLine()) != null) {
                    line = line.replaceAll("[,;:!.?\"]", "");

                    String[] wordsInText = line.split(" ");
                    for (String word : wordsInText) {
                        if (queryWords.contains(word)) {
                            Map<String, Integer> amountInCurrentFile = queryWordsCount.get(file.getName());

                            if (amountInCurrentFile.get(word) == 0) {
                                filesWithWordsCount.put(word, filesWithWordsCount.get(word) + 1);
                            }
                            amountInCurrentFile.put(word, amountInCurrentFile.get(word) + 1);
                        }
                    }
                }
            }
        }
    }

    private double calculateIdf(String word) {
        int filesWithWordCount = filesWithWordsCount.get(word);

        return Math.log10((filesCount - filesWithWordCount + 0.5) / (filesWithWordCount + 0.5));
    }

    private double calculateAverageFileLength() {
        long totalLength = 0L;
        for (String word : filesLength.keySet()) {
            totalLength += filesLength.get(word);
        }

        return (double) totalLength / filesCount;
    }
}
