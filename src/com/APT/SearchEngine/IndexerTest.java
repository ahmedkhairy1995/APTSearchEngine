package com.APT.SearchEngine;
//Shaalan
import com.APT.SearchEngine.Indexer.Indexer;

public class IndexerTest {
    public static void main(String args[]) {
        Indexer indexer = Indexer.getInstance();
        indexer.setNumThreads(8);
        indexer.beginIndexing();
    }
}