package retrieval;

import retrieval.search.RetrievalSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ArticleRecommender {
    public static void main(String[] args) {
        ArticleRetriever articleRetriever = new ArticleRetriever();
        try {
            articleRetriever.retrieveArticles();
        } catch (InterruptedException | IOException e) {
            System.err.println(String.format("Unable to extract data from Wikipedia: %s", e.getMessage()));
        }

        String searchQuery;
        File articlesDirectory = new File(articleRetriever.getArticlesDirectoryPath().toString());
        RetrievalSearch retrievalSearch = new RetrievalSearch();

        System.out.println("Please enter a retrieval search query. Type `exit` to quit.");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            while ((searchQuery = bufferedReader.readLine()) != null) {
                if (searchQuery.equals("exit")) {
                    return;
                }

                searchQuery = searchQuery.toLowerCase().replaceAll("[,;:!.?\"]", "");
                retrievalSearch.retrieveArticle(articlesDirectory, searchQuery);
            }
        } catch (IOException e) {
            System.err.println(String.format("Unable to perform the retrieval.search: %s", e.getMessage()));
        }
    }
}