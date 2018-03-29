package com.APT.SearchEngine.Models;

public class WordModel {
    private String originalWord;
    private String stemmedWord;
    private String document;
    private double rank;
    private double frequency;
    private String positions;

    public WordModel(String originalWord,String stemmedWord,String document){
        this.originalWord = originalWord;
        this.stemmedWord = stemmedWord;
        this.document=document;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public String getPositions() {
        return positions;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public String getOriginalWord() {
        return originalWord;
    }

    public String getStemmedWord() {
        return stemmedWord;
    }

    public String getDocument() {
        return document;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = 0.5f*(rank * frequency);
    }
}
