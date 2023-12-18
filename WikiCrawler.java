import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class WikiCrawler {
    String baseUrl;

    public WikiCrawler() {
        this("https://en.wikipedia.org");
    }

    public WikiCrawler(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ArrayList<PageNode> find(String startUrl, String endUrl) throws IOException {
        endUrl = baseUrl + '/' + endUrl;

        LinkedList<PageNode> queue = new LinkedList<>();
        Set<PageNode> visited = new LinkedHashSet<>();
        queue.add(new PageNode(baseUrl + '/' + startUrl));
        int currentDepth = 0;
        while (!queue.isEmpty()) {
            PageNode page = queue.poll();
            if (isInRobots(page.url)) {
                continue;
            }
            visited.add(page);
            Document doc = null;
            try {
                doc = Jsoup.connect(page.url).get();
            }
            catch (HttpStatusException e) {
                System.out.println("Error 404: " + page.url);
                continue;
            }
            Elements links = doc.select("p a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                String redirectText = link.text();
                if (absUrl.indexOf(":", 7) != -1 || absUrl.contains("#") || !absUrl.startsWith(baseUrl)) {
                    continue;
                }
                PageNode absPage = new PageNode(absUrl, redirectText, page);
                if (endUrl.equals(absPage.url)) {
                    return createPath(absPage);
                }

                if (!visited.contains(absPage)) {
                    queue.add(absPage);
                }
            }
            if (page.depth != currentDepth) {
                currentDepth = page.depth;
                System.out.println("Current depth: " + currentDepth);
            }
        }
        return null;
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

    public ArrayList<PageNode> createPath(PageNode page) {
        ArrayList<PageNode> path = new ArrayList<PageNode>();
        while (page != null) {
            path.add(page);
            page = page.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static String pathToString(ArrayList<PageNode> path) {
        if (path == null) {
            return "No path found.";
        }

        StringBuilder hyperlinkPath = new StringBuilder();
        StringBuilder listPath = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            listPath.append(path.get(i));
            listPath.append('\n');

            if (i != 0) {
                hyperlinkPath.append(" -> ");
            }
            hyperlinkPath.append(path.get(i).redirectText);
        }
        return listPath.toString() + '\n' + hyperlinkPath.toString();
    }
}