import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class PageNodeTest {
    public static void main(String[] args) {
        PageNode p1 = new PageNode("https://en.wikipedia.org/wiki/Tennis", "Tennis", null);
        System.out.println(getTitle(p1.url));
    }

    public static String getTitle(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Element heading = doc.getElementById("firstHeading");
            String title = heading.text();
            if (title != null) {
                return title;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "Title not found";
    }

}
