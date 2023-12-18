import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        WikiCrawler wc = new WikiCrawler();

        System.out.println(WikiCrawler.pathToString(wc.find("wiki/Tennis", "wiki/Fortnite")));

    }
}