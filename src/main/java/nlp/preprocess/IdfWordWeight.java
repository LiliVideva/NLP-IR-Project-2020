package nlp.preprocess;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class IdfWordWeight {
    private Map<String, Double> idfValues;

    public IdfWordWeight(String fileName) {
        idfValues = new HashMap<>();
        loadFile(fileName);
    }

    public double getWordWeight(String sentence) {
        if (idfValues == null) {
            return 1;
        }

        Double d = idfValues.get(sentence);
        return (d == null) ? 1 : d;
    }

    public void loadFile(String fileName) {
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(fileName))) {
            String nextLine;
            String[] tokens;
            String word;
            double idfValue;

            while ((nextLine = lineNumberReader.readLine()) != null) {
                nextLine = nextLine.trim();
                if (!nextLine.isEmpty()) {
                    tokens = nextLine.split(",");
                    word = tokens[0];
                    idfValue = Double.parseDouble(tokens[1]);
                    idfValues.put(word, idfValue);
                }
            }
        } catch (IOException e) {
            Logger.getLogger(IdfWordWeight.class.getName()).warning(String.format("Could not load the file with IDF values: %s", e.getMessage()));
        }
    }
}
