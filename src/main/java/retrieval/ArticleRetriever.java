package retrieval;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArticleRetriever {
    private String fileSeparator;
    private Path articlesDirectoryPath;
    private List<String> americanPresidents;

    ArticleRetriever() {
        fileSeparator = System.getProperty("file.separator");
        articlesDirectoryPath = Paths.get(String.format(".%sarticles", fileSeparator));
        americanPresidents = new ArrayList<>();
        americanPresidents = Arrays.asList("Abraham Lincoln", "John Adams", "Chester A. Arthur", "Donald Trump",
                "Benjamin Harrison", "James Buchanan", "Jimmy Carter", "George H. W. Bush", "Bill Clinton",
                "Grover Cleveland", "Calvin Coolidge", "Dwight D. Eisenhower", "Franklin D. Roosevelt",
                "Millard Fillmore", "Gerald Ford", "Ulysses S. Grant", "James A. Garfield", "Warren G. Harding",
                "George W. Bush", "Rutherford B. Hayes", "William Henry Harrison", "Herbert Hoover", "Andrew Jackson",
                "Thomas Jefferson", "John Quincy Adams", "Andrew Johnson", "Lyndon B. Johnson", "John F. Kennedy",
                "James Madison", "William McKinley", "James Monroe", "Richard Nixon", "Barack Obama", "Franklin Pierce",
                "James K. Polk", "Ronald Reagan", "William Howard Taft", "Zachary Taylor", "Harry S. Truman",
                "Theodore Roosevelt", "John Tyler", "Martin Van Buren", "George Washington", "Woodrow Wilson");
    }

    void retrieveArticles() throws InterruptedException, IOException {
        Files.createDirectory(articlesDirectoryPath);

        for (String president : americanPresidents) {
            String presidentName = president.replace(" ", "_");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&explaintext&titles=%s", presidentName)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONObject query = json.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");

            for (String key : pages.keySet()) {
                JSONObject page = pages.getJSONObject(key);
                String text = page.getString("extract").toLowerCase();

                Path articlePath = Paths.get(String.format("%s%s%s.txt", articlesDirectoryPath, fileSeparator, presidentName));
                Files.createFile(articlePath);
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(articlePath.toString())))) {
                    bufferedWriter.write(text);
                }
            }
        }
    }

    public Path getArticlesDirectoryPath() {
        return articlesDirectoryPath;
    }
}