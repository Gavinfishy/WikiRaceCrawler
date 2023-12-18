import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        String[] topics = new String[]{"tennis", "rafael"};
//        WikiCrawler w1 = new WikiCrawler("/wiki/Tennis", "/wiki/Exhibition_game", topics, 200, "WikiTennisGraph.txt");
//        try {
//            w1.crawl();
//        } catch (IOException var4) {
//            var4.printStackTrace();
//        }
        WikiCrawler wc = new WikiCrawler();

//        System.out.println(WikiCrawler.pathToString(wc.find("wiki/Tennis", "wiki/Fortnite")));
        System.out.println(WikiCrawler.pathToString(wc.find("wiki/Chair", "wiki/Phillippines")));
    }
}