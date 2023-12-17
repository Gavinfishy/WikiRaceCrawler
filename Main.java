import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String[] topics = new String[]{"tennis", "rafael"};
        WikiCrawler w1 = new WikiCrawler("/wiki/Tennis", topics, 200, "WikiTennisGraph.txt");
        WikiCrawler w2 = new WikiCrawler("/api/", topics, 100, "Disallowed");
        try {
            w1.crawl();
            w2.crawl();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }
}