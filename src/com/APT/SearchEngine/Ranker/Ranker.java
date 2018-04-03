package com.APT.SearchEngine.Ranker;

import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Ranker implements Serializable {


    private HashMap<String, DetailedUrl> urlMap = new HashMap<String, DetailedUrl>();
    private double dampingFactor = 0.5;


    public HashMap<String, DetailedUrl> getUrlMap() {
        return urlMap;
    }


    private DetailedUrl GetLink(String URL)
    {
        if(!urlMap.containsKey(URL))
        {
            urlMap.put(URL,new DetailedUrl(URL));
        }
        return urlMap.get(URL);
    }

    public void insertURL(String from,String to)
    {

        DetailedUrl first = GetLink(from);
        DetailedUrl second = GetLink(to);

        first.getOutGoing().add(second);
        second.getInGoing().add(first);
    }

    public void computeRank()
    {
        double temp=0;
        DetailedUrl tempUrlObject;
        System.out.println("starting to compute popularity ");
        for(int i=0;i<20;i++)
        {
            for (Map.Entry item:urlMap.entrySet())
            {
                temp = 1;
                tempUrlObject = (DetailedUrl)item.getValue();
                for (DetailedUrl inGoingLink : tempUrlObject.getInGoing()) {
                    temp += (inGoingLink.getRankOverOutGoing());
                }
                temp *= dampingFactor;
                tempUrlObject.setRank(temp);

                //System.out.println(temp);
            }
        }
    }

}



