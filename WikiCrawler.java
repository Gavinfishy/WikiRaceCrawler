import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

class WikiCrawler {
    String baseUrl;
    ArrayList<String> robotUrls;
    int requestCount = 0;
    static int totalRequests = 0;
    public WikiCrawler() {
        this("https://en.wikipedia.org");
    }

    public WikiCrawler(String baseUrl) {
        this.baseUrl = baseUrl;
        try {
            robotUrls = generateRobots(baseUrl + "/robots.txt");
        } catch (IOException e) {
            robotUrls = new ArrayList<>();
        }
    }

    private ArrayList<PageNode> getRedirects(PageNode parent) {
        ArrayList<PageNode> list = new ArrayList<>();
        try {
            if (++requestCount > 20) {
                try {
                    Thread.sleep(1000);
                    requestCount = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Document doc = Jsoup.connect(parent.url).get();
            totalRequests++;
            Elements links = doc.select("p a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (isInRobots(absUrl)) {
                    continue;
                }
                String redirectText = link.text();
                if (absUrl.indexOf(":", 7) != -1 ||
                        absUrl.contains("#") ||
                        !absUrl.startsWith(baseUrl) ||
                        absUrl.endsWith("=edit&redlink=1")) {
                    continue;
                }
                list.add(new PageNode(absUrl, redirectText, parent));
            }
        } catch (IOException e) {
            System.out.println("Error 404: " + parent.url);
            return null;
        }

        return list;
    }

    public ArrayList<PageNode> find(String startUrl, String endUrl) {
        final String searchUrl = baseUrl + "/" + endUrl;
        ArrayList<PageNode> endDocRedirects = getRedirects(new PageNode(searchUrl));

        PriorityQueue<PageNode> queue = new PriorityQueue<>();
        Set<PageNode> visited = new HashSet<>();
        PageNode startNode = new PageNode(baseUrl + '/' + startUrl);
        startNode.redirects = getRedirects(startNode);

        queue.add(startNode);
        while (!queue.isEmpty()) {
            PageNode page = queue.poll();

            System.out.println(page + ", total requests: " + totalRequests);
            visited.add(page);

            if (page.redirects == null) {
                continue;
            }
            for (PageNode absPage : page.redirects) {
                if (absPage.url.equals(searchUrl)) {
                    return createPath(absPage);
                }
            }

            for (PageNode absPage : page.redirects) {
                if (!visited.contains(absPage)) {
                    absPage.redirects = getRedirects(absPage);
                    absPage.compareRedirects(endDocRedirects);
                    queue.add(absPage);
                }
            }

        }
        return null;
    }

    public ArrayList<String> generateRobots(String robots) throws IOException {
        ArrayList<String> disallowedUrls = new ArrayList<>();
        URL robotsUrl = new URL(robots);
        BufferedReader reader = new BufferedReader(new InputStreamReader(robotsUrl.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Disallow:")) {
                String disallowedPage = line.substring("Disallow:".length()).trim();
                disallowedUrls.add(disallowedPage);
            }
        }
        reader.close();
        return disallowedUrls;
    }

    public boolean isInRobots(String url) throws IOException {
        String endpoint = new URL(url).getPath();
        for (String disallowedPage : robotUrls) {
            if (endpoint.equals(disallowedPage)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<PageNode> createPath(PageNode page) {
        ArrayList<PageNode> path = new ArrayList<>();
        while (page != null) {
            path.add(page);
            page = page.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public static String pathToString(ArrayList<PageNode> path) {
        if (path == null || path.isEmpty()) {
            return "No path found.";
        }

        StringBuilder pathString = new StringBuilder();
        pathString.append(path.get(0).redirectText);
        for (int i = 1; i < path.size(); i++) {
            pathString.append(" -> ").append(path.get(i).redirectText);
        }
        return pathString.toString();
    }
}