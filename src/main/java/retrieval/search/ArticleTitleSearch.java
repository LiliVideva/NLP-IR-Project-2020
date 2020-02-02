package retrieval.search;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArticleTitleSearch {
    private List<String> queryWords;
    private Map<String, Integer> scores;
    private File[] files;

    private ArticleTitleSearch() {
        queryWords = new ArrayList<>();
        scores = new HashMap<>();
    }

    public ArticleTitleSearch(String searchQuery, File directory) {
        this();

        queryWords = Arrays.asList(searchQuery.split(" "));
        files = directory.listFiles();
    }

    public Map<String, Integer> calculateScore() throws IOException {
        String fileName;
        String[] titleWords;
        int count;

        for (File file : files) {
            count = 0;
            fileName = file.getName();
            titleWords = fileName.split(".txt")[0].split("_");

            for (String word : titleWords) {
                if (queryWords.contains(word)) {
                    count++;
                }
            }

            scores.put(fileName, count);
        }

        return scores;
    }
}
