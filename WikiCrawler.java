import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class WikiCrawler {
    String seedUrl;
    String[] keywords;
    int max;
    String fileName;
    String baseUrl;

    public WikiCrawler(String seedUrl, String[] keywords, int max, String fileName) {
        this.seedUrl = seedUrl;
        this.keywords = keywords;
        this.max = max;
        this.fileName = fileName;
        baseUrl = "https://en.wikipedia.org";
    }

    public void crawl() throws IOException {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new LinkedHashSet<>();
        Set<String> urlsWeveBeenOn = new HashSet<>();
        urlsWeveBeenOn.add(baseUrl+seedUrl);
        queue.add(baseUrl + seedUrl);
        int requestCount = 0;

        while (!queue.isEmpty() && visited.size() < max) {
            String url = queue.poll();
            URL tempURL = new URL(url);
            String urlPath = tempURL.getPath();
            requestCount++;

            if (isInRobots(url)) {
                continue;
            }

            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("p a[href]");

            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                URL tempAbsURL = new URL(absUrl);
                String absURLPath = tempAbsURL.getPath();
                if (absUrl.indexOf(":", 7) != -1 || absUrl.contains("#")) {
                    continue;
                }
                if (!urlsWeveBeenOn.contains(absUrl) && !visited.contains(urlPath + " " + absURLPath) && absUrl.startsWith(baseUrl) && isPageRelevant(absURLPath)) {
                    if(visited.size() >= max){
                        break;
                    }
                    visited.add(urlPath + " " + absURLPath);
                    urlsWeveBeenOn.add(absUrl);
                    queue.add(absUrl);
                }
            }

            if (requestCount % 10 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        writeGraphToFile(visited);
    }

    private void writeGraphToFile(Set<String> visited) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(visited.size() + "\n");

        for (String url : visited) {
            writer.write(url + "\n");
        }

        writer.close();
    }

    public boolean isInRobots(String url) throws IOException {
        URL robotsUrl = new URL("https://en.wikipedia.org/robots.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(robotsUrl.openStream()));
        String line;
        String endpoint = new URL(url).getPath();

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Disallow:")) {
                String disallowedPage = line.substring("Disallow:".length()).trim();
                if (endpoint.equals(disallowedPage)) {
                    reader.close();
                    return true;
                }
            }
        }

        reader.close();
        return false;
    }

    public boolean isPageRelevant(String url){
        String pageTitle = url.split("/")[2].toLowerCase();

        for (int i = 0; i < keywords.length; i ++){
            String curr = keywords[i].toLowerCase();
            if (pageTitle.contains(curr)){
                return true;
            }
        }

        return false;
    }
}