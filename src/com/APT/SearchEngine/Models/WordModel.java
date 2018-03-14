package com.APT.SearchEngine.Models;

public class WordModel {
    private String word;
    private String document;
    private float rank;
    private float frequency;

    public WordModel(String word,String document){
        this.word=word;
        this.document=document;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public String getWord() {
        return word;
    }

    public String getDocument() {
        return document;
    }

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = 0.5f*(rank * frequency);
    }
}
