import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class PageNode implements Comparable<PageNode> {
    String url;
    String redirectText;
    PageNode parent;
    private int commonLinks = 0;
    private final int clicksFromSource;
    public ArrayList<PageNode> redirects = new ArrayList<>();

    public PageNode(String url) {
        this(url, getTitle(url), null);
    }

    void compareRedirects(ArrayList<PageNode> endRedirects) {
        if (redirects == null || redirects.isEmpty()) {
            commonLinks = 0;
            return;
        }
        for (PageNode endPage : endRedirects) {
            for (PageNode page : redirects) {
                if (endPage.url.equals(page.url)) {
                    commonLinks++;
                }
            }
        }
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
        if (this.parent == null) {
            this.clicksFromSource = 0;
        } else {
            this.clicksFromSource = parent.clicksFromSource + 1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final PageNode other = (PageNode) obj;
        return (other.url.equals(this.url));
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public String toString() {
        return url + " [" + redirectText + "]" + ", common links: " + commonLinks;
    }

    @Override
    public int compareTo(PageNode o) {
        return (- this.commonLinks) - (- o.commonLinks);
    }
}

