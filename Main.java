
public class Main {
    public static void main(String[] args) {

        WikiCrawler wc = new WikiCrawler();

        long start = System.currentTimeMillis();

        System.out.println(WikiCrawler.pathToString(wc.find("wiki/Tennis", "wiki/Fortnite")));
//        System.out.println(WikiCrawler.pathToString(wc.find("wiki/Ronald_Graham", "wiki/Steve_Butler_(mathematician)")));

        long end = System.currentTimeMillis();
        System.out.println("found in " + (end - start)/1000.0 + " s, with " + WikiCrawler.totalRequests + " requests.");
    }
}