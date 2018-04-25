package com.APT.SearchEngine.Retriever;

import com.APT.SearchEngine.Database.Database;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Retriever {

    private Database databaseConnection = Database.GetInstance();
    ///// call ranker functions inside this function
    public ArrayList<ArrayList<String>> getResults (String tablename,ArrayList<String> rowkey,String rankcolumnfamily,String positioncolumnfamily,boolean phrase)
    {
        ArrayList<ArrayList<String>> output = new ArrayList<>();
        if (phrase)
        {
            ArrayList<HashMap<String,Pair<Integer,ArrayList<String>>>> result = new ArrayList<>();
            try {
                result = databaseConnection.getPhraseLinks(tablename,rowkey,rankcolumnfamily,positioncolumnfamily);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            ArrayList<HashMap<String,Integer>> result = new ArrayList<>();
            if (rowkey.size() >1)
            {

                try {
                    result= databaseConnection.getOriginalMultipleWordsLinks(tablename,rowkey,rankcolumnfamily);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    result= databaseConnection.getOriginalWordLinks(tablename,rowkey.get(0),rankcolumnfamily);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return output;
    }
}
