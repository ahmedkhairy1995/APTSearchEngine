package com.APT.SearchEngine.PageRanking;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RankerTest {


    public static void main(String[] args) {

        TestPhrase();

    }

    private static void TestNormal()
    {
        PageRanking pageRanking = new PageRanking();

        HashMap<String, Integer> Map1 = new HashMap<String, Integer>();
        HashMap<String, Integer> Map2 = new HashMap<String, Integer>();
        HashMap<String, Integer> Map3 = new HashMap<String, Integer>();

      //  Map1.put("y",6.0);
      //  Map1.put("fby",8);
      //  Map1.put("fb",1);
       // Map1.put("fbq",8);

      //  Map2.put("x",3);
      //  Map2.put("fbq",6);
     //   Map2.put("fb",2);
       // Map2.put("fbx",23);

     //   Map3.put("fbq",3);
      //  Map3.put("fb",3);
     //   Map3.put("fbx",10);

        ArrayList<HashMap<String, Float>> searchedQuery = new ArrayList<>();

     //   searchedQuery.add(Map1);
      //  searchedQuery.add(Map2);
      //  searchedQuery.add(Map3);

        ArrayList<String> hamada = pageRanking.RankerNormal(searchedQuery);
        for(int i=0;i<hamada.size();i++)
        {
            System.out.println(hamada.get(i));
        }

    }

    private static void TestPhrase()
    {
        ArrayList<HashMap<String, Pair<Integer, ArrayList<String>>>> searchedQuery = new ArrayList<>();
        HashMap<String, Pair<Integer, ArrayList<String>>> Word = new HashMap<String, Pair<Integer, ArrayList<String>>>();
        ArrayList<String> Numbers = new ArrayList<>();
        Numbers.add("7");
        Numbers.add("4");
        Numbers.add("5");
        Numbers.add("3");
        Pair<Integer,ArrayList<String>> pair = new Pair<>(6,new ArrayList<String>(Numbers));
        Word.put("Fbx",pair);
        searchedQuery.add(new HashMap<String, Pair<Integer, ArrayList<String>>>(Word));
        Numbers.remove(0);
        Numbers.remove(0);
        Numbers.remove(0);
        Numbers.remove(0);

        Numbers.add("2");
        Numbers.add("14");
        Numbers.add("15");
        Numbers.add("13");
        Pair<Integer,ArrayList<String>> pair1 = new Pair<>(6,new ArrayList<String>(Numbers));
        Word.put("Fb",pair1);
        searchedQuery.add(new HashMap<String, Pair<Integer, ArrayList<String>>>(Word));

       // ArrayList<Pair<String, Integer>> IndexOfPhrase= new ArrayList<>();
        //IndexOfPhrase.add(new Pair<String, Integer>("Ahmed",1));
        //IndexOfPhrase.add(new Pair<String, Integer>("Ahmed",2));
      //  PageRanking PR = new PageRanking();
       // ArrayList<String> hamada = PR.PhraseRanker(searchedQuery,IndexOfPhrase);

       // for(int i=0;i<hamada.size();i++)
       // {
       //     System.out.println(hamada.get(i));
      //  }

    }



}