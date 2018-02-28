package com.APT.SearchEngine.Data;

import java.util.HashMap;

public class Data {
    private static HashMap<String,Integer> documents=new HashMap<>();

    public static HashMap<String, Integer> getDocuments() {
        return documents;
    }
}
