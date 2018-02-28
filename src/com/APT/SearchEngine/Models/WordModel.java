package com.APT.SearchEngine.Models;

public class WordModel {
    private String word;
    private int position;
    private String document;
    private int type;

    public WordModel(String word,String document,int position,int type){
        this.word=word;
        this.document=document;
        this.position=position;
        this.type=type;
    }

    public String getWord() {
        return word;
    }

    public int getPosition() {
        return position;
    }

    public String getDocument() {
        return document;
    }

    public int getType() {
        return type;
    }
}
