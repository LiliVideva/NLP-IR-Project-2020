package retrieval.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrievalSearch {
    public void retrieveRelevantArticles(File directory, String searchQuery, int topResultsCount) throws IOException {
        TitleSearch titleSearch = new TitleSearch(searchQuery, directory);
        Map<String, Integer> titleScores = titleSearch.calculateScore();

        BM25Search bm25Search = new BM25Search(searchQuery, directory);
        Map<String, Double> bm25Scores = bm25Search.calculateScore();

        BiwordSearch biwordSearch = new BiwordSearch(searchQuery, directory);
        Map<String, Double> biwordScores = biwordSearch.calculateScore();

        double score;
        Map<String, Double> finalScores = new HashMap<>();
        for (String fileName : titleScores.keySet()) {
            score = titleScores.get(fileName) * 3 + bm25Scores.get(fileName) + 1.5 * biwordScores.get(fileName);
            finalScores.put(fileName, score);
        }

        String resultsMessage = getTopResults(topResultsCount, finalScores);
        System.out.println(resultsMessage);
    }

    private String getTopResults(int topResultsCount, Map<String, Double> finalScores) {
        StringBuilder sb = new StringBuilder("Best suggestions, sorted in order of their relevance:\n");
        List<String> usedFiles = new ArrayList<>();

        for (int i = 0; i < topResultsCount; i++) {
            double scorePerFile;
            double maxScore = -1.0;
            String maxFilename = null;

            for (String fileName : finalScores.keySet()) {
                scorePerFile = finalScores.get(fileName);

                if (scorePerFile > maxScore && !usedFiles.contains(fileName)) {
                    maxScore = scorePerFile;
                    maxFilename = fileName;
                }
            }

            if (maxFilename == null) {
                return sb.toString();
            }

            usedFiles.add(maxFilename);
            sb.append(String.format("%s with a score of %.3f\n", maxFilename, maxScore));
        }

        return sb.toString();
    }
}
