package wikibot;

public class PageNodeTest {
    public static void main(String[] args) {
        PageNode p1 = new PageNode("https://en.wikipedia.org/wiki/Tennis");
        System.out.println(p1.redirectText);
    }

}
