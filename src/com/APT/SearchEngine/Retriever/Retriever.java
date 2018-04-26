package com.APT.SearchEngine.Retriever;

import com.APT.SearchEngine.Database.Database;
import javafx.util.Pair;
import org.jcodings.util.Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Retriever {

    private Database databaseConnection = Database.GetInstance();
    ///// call ranker functions inside this function
    public ArrayList<ArrayList<String>> getResults (String tablename, ArrayList<String> rowkey, ArrayList<String>stemmed, String rankcolumnfamily, String positioncolumnfamily, boolean phrase)
    {
        ArrayList<ArrayList<String>> output = new ArrayList<>();
        if (phrase)
        {
            ArrayList<HashMap<String,Pair<Integer,ArrayList<String>>>> result = new ArrayList<>();
            try {
                result = databaseConnection.getPhraseLinks(tablename,rowkey,rankcolumnfamily,positioncolumnfamily);
                /// will call phrase ranker
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            ArrayList<HashMap<String,Integer>> result = new ArrayList<>();
            ArrayList<HashMap<String,Integer>> stemmedresult = new ArrayList<>();
            if (rowkey.size() >1)
            {
                try {
                    result= databaseConnection.getOriginalMultipleWordsLinks(tablename,rowkey,rankcolumnfamily);
                    /// call normal ranker
                    /// call database function that gets documents
                    stemmedresult = databaseConnection.getStemmedMultipleWordsLinks(tablename,stemmed,rankcolumnfamily);
                    //  call normal ranker
                    // call database function that gets documents
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    result= databaseConnection.getOriginalWordLinks(tablename,rowkey.get(0),rankcolumnfamily);
                    //calll normal search
                    // call database function that gets documents
                    stemmedresult = databaseConnection.getStemmedWordsLinks(tablename,rankcolumnfamily,stemmed.get(0));
                    /// call normal ranker
                    /// call database that gets documents
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /// concatenate and send
        return output;
    }
}
