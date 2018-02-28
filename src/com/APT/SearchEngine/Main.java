package com.APT.SearchEngine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import opennlp.tools.stemmer.*;

public class Main {
    public static void main(String args[]) {
        try {
            Document document = Jsoup.connect("http://en.wikipedia.org/").get();
            Elements elements = document.getAllElements();
            System.out.print( elements.text()+"\n");
            PorterStemmer stemmer=new PorterStemmer();
            System.out.print( stemmer.stem("play")+"\n");
            System.out.print( stemmer.stem("playing")+"\n");
            System.out.print( stemmer.stem("joking")+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
