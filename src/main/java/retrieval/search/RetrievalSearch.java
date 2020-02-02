package retrieval.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RetrievalSearch {
    public void retrieveArticle(File directory, String searchQuery) throws IOException {
        ArticleTitleSearch articleTitleSearch = new ArticleTitleSearch(searchQuery, directory);
        Map<String, Integer> fileNameScores = articleTitleSearch.calculateScore();

        BM25Search bm25Search = new BM25Search(searchQuery, directory);
        Map<String, Double> bm25Scores = bm25Search.calculateScore();

        SkipBigramSearch skipBigramSearch = new SkipBigramSearch(searchQuery, directory);
        Map<String, Double> bigramScores = skipBigramSearch.calculateScore();

        double score;
        Map<String, Double> finalScores = new HashMap<>();
        for (String fileName : fileNameScores.keySet()) {
            score = fileNameScores.get(fileName) * 3 + bm25Scores.get(fileName) + 1.5 * bigramScores.get(fileName);
            finalScores.put(fileName, score);
        }

        int topResultsCount = 5;
        String resultsMessage = getTopResults(topResultsCount, finalScores);
        System.out.println(resultsMessage);
    }

    private String getTopResults(int topResultsCount, Map<String, Double> finalScores) {
        StringBuilder sb = new StringBuilder("Best suggestions, sorted in order of their relevance:\n");
        List<String> usedFiles = new LinkedList<>();

        for (int i = 0; i < topResultsCount; i++) {
            double scorePerFile;
            double maxScore = -1.0;
            String maxFilename = "Error!";

            for (String fileName : finalScores.keySet()) {
                scorePerFile = finalScores.get(fileName);

                if (scorePerFile > maxScore && !usedFiles.contains(fileName)) {
                    maxScore = scorePerFile;
                    maxFilename = fileName;
                }
            }

            if (maxFilename.equals("Error!")) {
                return sb.toString();
            }

            usedFiles.add(maxFilename);
            sb.append(String.format("%s with a score of %.3f\n", maxFilename, maxScore));
        }

        return sb.toString();
    }
}
