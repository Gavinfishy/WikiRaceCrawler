package wikibot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class PageNode implements Comparable<PageNode> {
    String url;
    String redirectText;
    PageNode parent;
    int depth;

    public PageNode(String url) {
        this(url, getTitle(url), null);
    }

    public static String getTitle(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element heading = doc.getElementById("firstHeading");
            if (heading != null) {
                return heading.text();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "Title not found";
    }

    public PageNode(String url, String text, PageNode parent) {
        this.url = url;
        this.redirectText = text;
        this.parent = parent;

        this.depth = parent == null ? 1 : parent.depth + 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof PageNode && url.equals(((PageNode)obj).url);
    }

    @Override
    public String toString() {
        return url + " (" + redirectText + ")";
    }

    @Override
    public int compareTo(PageNode o) {
        return depth - o.depth;
    }
}

