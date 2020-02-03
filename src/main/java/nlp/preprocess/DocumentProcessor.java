package nlp.preprocess;

import nlp.utils.Sentence;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentProcessor {
    private Stemmer stemmer;

    public DocumentProcessor() {
        stemmer = new Stemmer();
    }

    public String convertToString(String fileName) {
        StringBuilder document = new StringBuilder();

        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileName))) {
            String line;

            while ((line = lineNumberReader.readLine()) != null) {
                line = line.trim();

                if (!line.isEmpty()) {
                    line = line.replaceAll("&#?[0-9 a-z A-Z][0-9 a-z A-Z][0-9 a-z A-Z]?;", "");
                    document.append(line).append(" ");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(DocumentProcessor.class.getName()).log(Level.SEVERE, "Problem reading from file.", e);
        }

        return document.toString();
    }

    public List<Sentence> retrieveSentencesFromArticle(String article) {
        List<Sentence> sentencesList = new ArrayList<>();
        String[] sentences = article.split("\\. ");

        int sentenceNumber = 0;
        for (String sentenceValue : sentences) {
            Sentence sentence = new Sentence(sentenceNumber, sentenceValue);
            sentencesList.add(sentence);
            sentenceNumber++;
        }

        return sentencesList;
    }

    public Stemmer getStemmer() {
        return stemmer;
    }
}