package nlp.textrank;

import nlp.preprocess.DocumentProcessor;
import nlp.utils.Score;
import nlp.utils.Sentence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextRankSummarizer {
    public List<Score> rankSentences(List<Sentence> sentences, DocumentProcessor documentProcessor, String fileName) {
        TextRank textRank = new TextRank(documentProcessor, fileName);
        List<String> sentencesValues = new ArrayList<>();
        Map<String, List<Integer>> wordIdsMap = new HashMap<>();

        for (Sentence sentence : sentences) {
            sentencesValues.add(sentence.getSentenceValue());
            String[] words = sentence.stem().split(" ");

            for (String word : words) {
                if (wordIdsMap.containsKey(word)) {
                    wordIdsMap.get(word).add(sentence.getSentenceId());
                    continue;
                }
                List<Integer> idsList = new ArrayList<>();
                idsList.add(sentence.getSentenceId());
                wordIdsMap.put(word, idsList);
            }
        }
        textRank.setSentences(sentencesValues);

        List<Score> finalScores = textRank.getRankedSentences(wordIdsMap);
        Map<Integer, List<Integer>> links = textRank.getLinks();

        Sentence sentence;
        for (int i = 0; i < sentences.size(); i++) {
            sentence = sentences.get(i);

            if (links.containsKey(i)) {
                for (int j = 0; j < i; j++) {
                    sentence.addLink(sentences.get(j));
                }
            }
        }

        for (Score score : finalScores) {
            sentence = sentences.get(score.getSentenceId());
            sentence.setRankScore(score);
        }

        return finalScores;
    }
}
