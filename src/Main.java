import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class Main {

    public static void main(String args[]) {
        Document doc = Jsoup.parse("https://www.google.com.eg");
//        Element content = doc.getElementById("content");
        Elements links = doc.getElementsByTag("a");
        System.out.print(links.text());
    }
}
