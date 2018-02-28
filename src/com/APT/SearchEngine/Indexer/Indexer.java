package com.APT.SearchEngine.Indexer;

import com.APT.SearchEngine.Data.Data;
import opennlp.tools.stemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Indexer {
    private PorterStemmer porterStemmer=new PorterStemmer();


    private void index(int index){
        if(index>= Data.getDocuments().size())
            return;

        try{
            Document document = Jsoup.connect(Data.getDocuments().get(index)).get();
            Elements elements = document.getAllElements();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}
