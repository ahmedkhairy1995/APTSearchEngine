package com.APT.SearchEngine;

import com.APT.SearchEngine.Data.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.stemmer.*;

public class Main {
    public static void main(String args[]) {
        String text = "Ahmed &nbsp.. does love May";
        ArrayList<String> purifiedList = new ArrayList<>(Arrays.asList(text.split(" ")));
        purifiedList.removeIf((String word) -> word.startsWith("&"));
        purifiedList.removeIf((String word) -> Data.getStopWords().contains(word));
    }
}