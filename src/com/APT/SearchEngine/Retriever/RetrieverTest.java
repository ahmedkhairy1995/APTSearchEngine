package com.APT.SearchEngine.Retriever;

import javafx.util.Pair;

import java.util.ArrayList;

public class RetrieverTest {

    public static void main(String[] args) {
        Retriever ret = new Retriever();
        boolean phrase = true;
        ArrayList<Pair<String,Integer>>IndexOfPhrase = new ArrayList<>();
        Pair<String,Integer> x = new Pair<>("computer",1);
        Pair<String,Integer> y = new Pair<>("funny",3);
        IndexOfPhrase.add(x);
        IndexOfPhrase.add(y);
        ArrayList<String> stemmed = new ArrayList<>();
        stemmed.add("comput");
        stemmed.add("funni");
        ArrayList<String>rowkey = new ArrayList<>();
        rowkey.add("computer");
        rowkey.add("funny");
        ArrayList<ArrayList<String>> out = ret.getResults (rowkey,stemmed, IndexOfPhrase, phrase);

    }
}
