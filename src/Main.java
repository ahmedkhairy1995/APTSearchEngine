import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;

public class Main {
    public static void main(String args[]) {
        try {
            Document document = Jsoup.connect("http://en.wikipedia.org/").get();
            Elements elements = document.getAllElements();
            System.out.print( elements.text()+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
