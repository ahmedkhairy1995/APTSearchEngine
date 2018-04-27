package com.APT.SearchEngine.Retriever;

import com.APT.SearchEngine.Database.Database;
import com.APT.SearchEngine.PageRanking.PageRanking;
import javafx.util.Pair;
import org.jcodings.util.Hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Retriever {

    private Database databaseConnection = Database.GetInstance();
    private PageRanking pageRanker  = new PageRanking();
    ///// call ranker functions inside this function
    public ArrayList<ArrayList<String>> getResults (ArrayList<String> rowkey, ArrayList<String>stemmed,ArrayList<Pair<String, Integer>> IndexOfPhrase, boolean phrase)
    {
        ArrayList<ArrayList<String>> output = new ArrayList<>();
        ArrayList<ArrayList<String>> output2 = new ArrayList<>();
        ArrayList<String> sortedlinks = new ArrayList<>();
        if (phrase)
        {
            ArrayList<HashMap<String,Pair<Float,ArrayList<String>>>> result = new ArrayList<>();

            try {
                result = databaseConnection.getPhraseLinks("InvertedIndex",rowkey,"WordRank","WordPosition");
                sortedlinks=pageRanker.PhraseRanker(result,IndexOfPhrase);
                output = databaseConnection.getSortedLinksDocuement("Crawler",sortedlinks,"Document","Text");
                /// will call phrase ranker
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            ArrayList<HashMap<String,Float>> result = new ArrayList<>();
            ArrayList<HashMap<String,Float>> stemmedresult = new ArrayList<>();
            if (rowkey.size() >1)
            {
                try {
                    result= databaseConnection.getOriginalMultipleWordsLinks("InvertedIndex",rowkey,"WordRank");
                    /// call normal ranker
                    sortedlinks=pageRanker.RankerNormal(result);
                    /// call database function that gets documents
                    output=databaseConnection.getSortedLinksDocuement("Crawler",sortedlinks,"Document","Text");
                    stemmedresult = databaseConnection.getStemmedMultipleWordsLinks("InvertedIndex",stemmed,rowkey,"WordRank");
                    //  call normal ranker
                    sortedlinks=pageRanker.RankerNormal(stemmedresult);
                    // call database function that gets documents
                    output2=databaseConnection.getSortedLinksDocuement("Crawler",sortedlinks,"Document","Text");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    result= databaseConnection.getOriginalWordLinks("InvertedIndex",rowkey.get(0),"WordRank");
                    //calll normal search
                    sortedlinks=pageRanker.RankerNormal(result);
                    /// call database function that gets documents
                    output=databaseConnection.getSortedLinksDocuement("Crawler",sortedlinks,"Document","Text");
                    stemmedresult = databaseConnection.getStemmedWordLinks("InvertedIndex","WordRank",stemmed.get(0),rowkey.get(0));
                    /// call normal ranker
                    sortedlinks=pageRanker.RankerNormal(stemmedresult);
                    // call database function that gets documents
                    output2=databaseConnection.getSortedLinksDocuement("Crawler",sortedlinks,"Document","Text");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /// concatenate and send
        output.addAll(output2);
        return output;
    }
}
