package com.APT.SearchEngine.PageRanking;

import com.kenai.jaffl.annotations.In;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PageRanking {

    private ArrayList<HashMap<String, Integer>> searchedQuery = new ArrayList<HashMap<String, Integer>>();

    public ArrayList<HashMap<String, Integer>> getSearchedQuery() {
        return searchedQuery;
    }

    public void setSearchedQuery(ArrayList<HashMap<String, Integer>> searchedQuery) {
        this.searchedQuery = searchedQuery;
    }


    public PageRanking() {
    }

    public ArrayList<String> RankerNormal(ArrayList<HashMap<String, Integer>> searchedQuery) {

        setSearchedQuery(searchedQuery);
        Map<String, Integer> Union = new HashMap<String, Integer>();

        Union.putAll(searchedQuery.get(0));


        for (int i = 1; i < searchedQuery.size(); i++) {

            for (Map.Entry<String, Integer> entry : searchedQuery.get(i).entrySet()) {
                String key = entry.getKey();
                int value = entry.getValue();

                if (!Union.containsKey(key)) {
                    Union.put(key, value);
                } else {
                    Union.put(key, Union.get(key) + value);
                }


            }
        }
        Stream<Map.Entry<String, Integer>> sorted = Union.entrySet().stream().sorted(Map.Entry.comparingByValue());


        Map<String, Integer> tempMap =
                Union.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        String keySet = tempMap.keySet().toString();
        keySet = keySet.replace("[", "");
        keySet = keySet.replace("]", "");
        return new ArrayList<>(Arrays.asList(keySet.split(", ")));
    }

    /*
     ArrayList of HashMaps, each HashMaps represents a word
     */
    public ArrayList<String> PhraseRanker(ArrayList<HashMap<String, Pair<Integer, ArrayList<String>>>> searchedQuery, ArrayList<Pair<String, Integer>> IndexOfPhrase) {

        HashMap<String, Integer> RankedList = new HashMap<>();
        HashMap<String, ArrayList<Integer>> ExpectedLocationOfWords = new HashMap<>();
        boolean firstWord = true;
        ArrayList<Integer> TempRanker = new ArrayList<>();
        //Considering each and every link alone

        HashMap<String, Pair<Integer, ArrayList<String>>> Object = searchedQuery.get(0);
        boolean found  = false;
        //Looping on the URLS of the first word to add the ArrayList of ints instead of words
        for (Map.Entry<String,Pair<Integer, ArrayList<String>>> item : Object.entrySet())
        {
            RankedList.put(item.getKey(),item.getValue().getKey());
            ArrayList<Integer> Temp= new ArrayList<>();
            for(String Number: item.getValue().getValue())
            {
                Temp.add(Integer.parseInt(Number));
            }
            ExpectedLocationOfWords.put(item.getKey(),new ArrayList<>(Temp));
        }

        int WordNum=0;
        //Looping on all the words now to make sure they match the criteria
        for(HashMap<String, Pair<Integer, ArrayList<String>>> item: searchedQuery)
        //Holding the hashmap that refers to each word alone
        {
            if(firstWord)
            {
                WordNum++;
                firstWord = false;
                continue;
            }

            //Loop on all the occurrences of this word in this URL
            for(Map.Entry<String, Pair<Integer, ArrayList<String>>> HashMapsURL: item.entrySet())
            {
                //Inside the URL i have both Rank and string of occurrences, loop on the
                //indexes and if it happens that this word is found in the correct position then add the rank to the pre-saved rank
                //and quit this URL
                for(int i=0;i<HashMapsURL.getValue().getValue().size();i++)
                {
                    //CHECK IF THIS KEY IS FOUND IN THE PREVIOUS KEYS
                    //NEEDS TO BE FOUND ONCE INORDER TO BE ACCEPTED WITH US
                    //ADD ANOTHER
                    if(ExpectedLocationOfWords.get(HashMapsURL.getKey()).contains(
                            Integer.parseInt(HashMapsURL.getValue().getValue().get(i)) + IndexOfPhrase.get(WordNum).getValue() - IndexOfPhrase.get(0).getValue()))
                    {
                        //This word is found in the right position, we will let this URL procees to the next level

                        //Updating the Rank
                        RankedList.put(HashMapsURL.getKey(),RankedList.get(HashMapsURL.getKey()) + HashMapsURL.getValue().getKey());
                        found = true;

                    }
                    if(found)
                        break;
                }
                if(!found)
                {
                    RankedList.remove(HashMapsURL.getKey());
                    ExpectedLocationOfWords.remove(HashMapsURL.getKey());
                }
                found = false;


            }
            WordNum++;
        }













        Map<String, Integer> tempMap =
                RankedList.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        String keySet = tempMap.keySet().toString();
        keySet = keySet.replace("[", "");
        keySet = keySet.replace("]", "");
        return new ArrayList<>(Arrays.asList(keySet.split(", ")));

    }


}
