package com.APT.SearchEngine;

import com.APT.SearchEngine.Indexer.Indexer;

public class Main {
    public static void main(String args[]) {
        Indexer indexer = Indexer.getInstance();
        indexer.setNumThreads(8);
        indexer.beginIndexing();
    }
}