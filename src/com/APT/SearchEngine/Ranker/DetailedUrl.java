package com.APT.SearchEngine.Ranker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DetailedUrl implements Serializable {


    private String name;
    private HashSet<DetailedUrl> outGoing = new HashSet();
    private HashSet<DetailedUrl> inGoing = new HashSet();
    private double rank;


    public String getName() {
        return name;

    }

    public double getRankOverOutGoing()
    {
        return rank/outGoing.size();
    }
    public void setName(String name) {
        this.name = name;
        this.rank = 0;
    }

    public DetailedUrl(String URL)
    {
        name = URL;
    }
    public HashSet<DetailedUrl> getOutGoing() {
        return outGoing;
    }

    public void setOutGoing(HashSet<DetailedUrl> outGoing) {
        this.outGoing = outGoing;
    }

    public HashSet<DetailedUrl> getInGoing() {
        return inGoing;
    }

    public void setInGoing(HashSet<DetailedUrl> inGoing) {
        this.inGoing = inGoing;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }





}
