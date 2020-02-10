package retrieval.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BiwordSearch {
    private List<String> queryWords;
    private List<Biword> searchBiwords;
    private File[] files;
    private Map<String, Double> scores;

    private static class Biword {
        private String firstWord;
        private String secondWord;

        public Biword(String firstWord, String secondWord) {
            this.firstWord = firstWord;
            this.secondWord = secondWord;
        }
    }

    private BiwordSearch() {
        queryWords = new ArrayList<>();
        searchBiwords = new LinkedList<>();
        scores = new HashMap<>();
    }

    public BiwordSearch(String searchQuery, File directory) {
        this();

        queryWords = Arrays.asList(searchQuery.split(" "));
        searchBiwords = initSearchBiwords();

        files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                scores.put(file.getName(), 0.0);
            }
        }
    }

    private List<Biword> initSearchBiwords() {
        List<Biword> searchBiwords = new LinkedList<>();

        for (int i = 0; i < queryWords.size() - 1; i++) {
            searchBiwords.add(new Biword(queryWords.get(i), queryWords.get(i + 1)));
        }

        return searchBiwords;
    }

    public Map<String, Double> calculateScore() throws IOException {
        if (queryWords.size() == 0 || queryWords.size() == 1) {
            return scores;
        }

        for (File file : files) {
            String line;
            String[] lastWords = new String[2];

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                lastWords[0] = "";
                lastWords[1] = "";

                while ((line = bufferedReader.readLine()) != null) {
                    String[] words = combineLines(lastWords, line.split(" "));

                    for (int i = 0; i < words.length - 2; i++) {
                        if (!queryWords.contains(words[i])) {
                            continue;
                        }

                        if (checkBiword(words[i], words[i + 1]) || checkBiword(words[i], words[i + 2])) {
                            scores.put(file.getName(), scores.get(file.getName()) + 1);
                        }
                    }
                    lastWords[0] = words[words.length - 2];
                    lastWords[1] = words[words.length - 1];
                }
            }
        }

        return scores;
    }

    private String[] combineLines(String[] firstLine, String[] secondLine) {
        int firstLineLength = firstLine.length;
        int secondLineLength = secondLine.length;
        String[] combinedLine = new String[firstLineLength + secondLineLength];

        System.arraycopy(firstLine, 0, combinedLine, 0, firstLineLength);
        System.arraycopy(secondLine, 0, combinedLine, firstLineLength, secondLineLength);

        return combinedLine;
    }

    private boolean checkBiword(String firstWord, String secondWord) {
        for (Biword biword : searchBiwords) {
            if (biword.firstWord.equals(firstWord) && biword.secondWord.equals(secondWord)) {
                return true;
            }
        }

        return false;
    }
}
