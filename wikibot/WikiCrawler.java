package wikibot;

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
    String destUrl;
    RelevanceScore relevanceScore;
    List<String> topWords;
    ArrayList<String> robotUrls;

//    public WikiCrawler(String seedUrl, String destUrl, String[] keywords, int max, String fileName) throws IOException {
//        this.seedUrl = seedUrl;
//        this.destUrl = destUrl;
//        this.keywords = keywords;
//        this.max = max;
//        this.fileName = fileName;
//        baseUrl = "https://en.wikipedia.org";
//        String destinationUrl = baseUrl + destUrl;
//        this.topWords = getTopWordsFromUrl(destinationUrl, 10);
//    }


//    public void crawl() throws IOException {
//        Queue<String> queue = new LinkedList<>();
//        Set<String> visited = new LinkedHashSet<>();
//        Set<String> urlsWeveBeenOn = new HashSet<>();
//        urlsWeveBeenOn.add(baseUrl+seedUrl);
//        queue.add(baseUrl + seedUrl);
//        int requestCount = 0;
//        while (!queue.isEmpty() && visited.size() < max) {
//            String url = queue.poll();
//            URL tempURL = new URL(url);
//            String urlPath = tempURL.getPath();
//            requestCount++;
//            if (isInRobots(url)) {
//                continue;
//            }
//            Document doc = Jsoup.connect(url).get();
//            Elements links = doc.select("p a[href]");
//            for (Element link : links) {
//                String absUrl = link.attr("abs:href");
//                URL tempAbsURL = new URL(absUrl);
//                String absURLPath = tempAbsURL.getPath();
//                if (absUrl.indexOf(":", 7) != -1 || absUrl.contains("#")) {
//                    continue;
//                }
//                if (!urlsWeveBeenOn.contains(absUrl) && !visited.contains(urlPath + " " + absURLPath) && absUrl.startsWith(baseUrl) && isPageRelevant(absURLPath)) {
//                    if(visited.size() >= max){
//                        break;
//                    }
//                    visited.add(urlPath + " " + absURLPath);
//                    urlsWeveBeenOn.add(absUrl);
//                    queue.add(absUrl);
//                }
//            }
//            if (requestCount % 10 == 0) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        writeGraphToFile(visited);
//    }

//    private void writeGraphToFile(Set<String> visited) throws IOException {
//        File file = new File(fileName);
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//        writer.write(visited.size() + "\n");
//
//        for (String url : visited) {
//            writer.write(url + "\n");
//        }
//
//        writer.close();
//    }

//    public boolean isPageRelevant(String url){
//        String pageTitle = url.split("/")[2].toLowerCase();
//        for (String keyword : keywords) {
//            String curr = keyword.toLowerCase();
//            if (pageTitle.contains(curr)) {
//                return true;
//            }
//        }
//        return false;

//    }

    public WikiCrawler() {
        this("https://en.wikipedia.org");
    }

    public WikiCrawler(String baseUrl) {
        this.baseUrl = baseUrl;
        try {
            robotUrls = generateRobots(baseUrl + "/robots.txt");
        }
        catch (IOException e) {
            robotUrls = new ArrayList<String>();
        }
    }

    public ArrayList<PageNode> find(String startUrl, String endUrl) throws IOException {
        endUrl = baseUrl + '/' + endUrl;
        relevanceScore = new RelevanceScore();
        Document endDoc = Jsoup.connect(endUrl).get();
        relevanceScore.calculateRelevance(endDoc.text());
        topWords = relevanceScore.getTopRelevantWords(10);
        int requestCount = 0;
        LinkedList<PageNode> queue = new LinkedList<>();
        Set<PageNode> visited = new LinkedHashSet<>();
        queue.add(new PageNode(baseUrl + '/' + startUrl));
        int currentDepth = 0;
        System.out.println(topWords);
        while (!queue.isEmpty()) {
            PageNode page = queue.poll();
            if (isInRobots(page.url)) {
                continue;
            }
            requestCount++;
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
                    if (containsTopWord(absPage.url)) {
//                        System.out.println(absPage);
                        queue.addFirst(absPage);
                    } else {
//                        System.out.println("last: " + absPage);
                        queue.addLast(absPage);
                    }
                }
            }
            if (requestCount > 20) {
                try {
                    Thread.sleep(1000);
                    requestCount = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (page.depth != currentDepth) {
                currentDepth = page.depth;
                System.out.println("Current depth: " + currentDepth);
            }
        }
        return null;
    }

    public ArrayList<String> generateRobots(String robots) throws IOException {
        ArrayList<String> disallowedUrls = new ArrayList<String>();
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



//    public List<String> getTopWordsFromUrl(String url, int n) throws IOException {
//        Document doc = Jsoup.connect(url).get();
//        wikibot.RelevanceScore relevanceScore = new wikibot.RelevanceScore();
//        relevanceScore.calculateRelevance(doc.text());
//        System.out.println(relevanceScore.getTopRelevantWords(n));
//        return relevanceScore.getTopRelevantWords(n);
//    }

    public boolean containsTopWord(String url) {
        String endpoint = url.substring(url.indexOf("/wiki/") + 6).toLowerCase();
        for (String word : topWords) {
            if (endpoint.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}