
package com.APT.SearchEngine.Indexer;

import com.APT.SearchEngine.Data.Data;
import com.APT.SearchEngine.Models.WordModel;
import opennlp.tools.stemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.util.*;

public class Indexer {
    private String[] documents=(String[]) Data.getMyDocuments().keySet().toArray();
    private int numThreads;

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    private void beginIndexing(){
        for(int i=0;i<numThreads;i++){
            final int offset=i;
            new Thread(new Runnable() {
                PorterStemmer porterStemmer;
                Stack<String> myStack;
                ArrayList<WordModel> processedWords;
                HashMap<String,Integer> wordCount;
                @Override
                public void run() {
                    porterStemmer=new PorterStemmer();
                    myStack=new Stack<String>();
                    processedWords=new ArrayList<>();
                    wordCount=new HashMap<>();
                    index(offset,porterStemmer,myStack,processedWords,wordCount);
                }
            }).start();
        }
    }

    private void index(int index,PorterStemmer porterStemmer,Stack<String> myStack,ArrayList<WordModel> processedWords,HashMap<String,Integer> wordCount){
        if(index>=documents.length)
            return;
        try{
            //Get the whole document in HTML Format
            Document document = Jsoup.connect(documents[index]).get();

            //Get all elements as a hierarchy in HTML Format
            Elements allElements=document.getAllElements();

            //Get total number of words in document
            int totalNumWords=getTotalNumWords(allElements);

            //Traversing from inner to outer
            for(int i=allElements.size()-1;i>=0;i++){

                //Get inner HTML content
                String innerHTML=allElements.get(i).text();

                //HTMLTag has content to insert into DB
                if (innerHTML.length()!=0){
                    //Get the tag of this element
                    Tag tag=allElements.get(i).tag();
                    int type=getTagRank(tag);

                    //Get the element as an array of strings
                    List<String> items=purifyElements(allElements.get(i).toString());
                    for (int j=0;j<items.size();j++){
                        //Get a word from my items
                        String word=items.get(i);

                        //checking if this is an inner closing tag to pop tag from my stack
                        if (!myStack.isEmpty() && isInnerClosingTag(word) && j!=(items.size()-1)) myStack.pop();

                        //checking if this is an inner opening tag to push tag to my stack
                        else if(j!=0 && isInnerOpeningTag(word)) myStack.push(word);

                        //If stack is not empty, we're allowed to process this word (since it's not a duplicate!)
                        else if (myStack.isEmpty()){
                            //Add this word to my list to save it to my Database
                            WordModel processedWord=new WordModel(porterStemmer.stem(word),documents[index],(i+2)-allElements.size(),type);
                            processedWords.add(processedWord);

                            //Update count of word in page
                            if(!wordCount.containsKey(word))
                                wordCount.put(word,1);
                            else
                                wordCount.computeIfPresent(word, (k, v) -> v + 1);

                        }
                    }
                }
            }

            //Save processed words to my database
            //....
            for (int i=0;i<processedWords.size();i++){
                //Calculating word frequency
                float wordFrequency=(float)wordCount.get(processedWords.get(i).getWord())/totalNumWords;

                try{

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        if((index+numThreads)>=documents.length) return;

        //Clear all data structures for the next document
        myStack.clear();
        processedWords.clear();
        wordCount.clear();
        index(index+numThreads,porterStemmer,myStack,processedWords,wordCount);
    }

    private List<String> purifyElements(String text){
        text = text.replace(",","");
        text = text.replace("#","");
        text = text.replace("@","");
        text = text.replace("", "");
        text = text.replace("?", "");
        text = text.replace(".", "");
        text = text.replace("(", "");
        text = text.replace(")", "");
        text = text.replace("{", "");
        text = text.replace("}", "");
        text = text.replace(":", "");
        text = text.replace("\\", "");
        text = text.replace("[", "");
        text = text.replace("]", "");
        text = text.replace("|", "");
        text = text.replace("*", "");
        text = text.replace(":", "");
        text = text.replace("+", "");
        text = text.replace("-", "");
        text = text.replace("=", "");
        text = text.replace("%", "");
        text = text.replace("~", "");
        text = text.replace(";", "");
        text = text.replace("^", "");
        ArrayList<String> purifiedList= new ArrayList<>(Arrays.asList(text.split(" ")));
        purifiedList.removeIf((String word) -> word.startsWith("&"));
        purifiedList.removeIf(this::isAStopWord);
        return purifiedList;
    }

    private boolean isInnerOpeningTag(String word){
        return (word.startsWith("<") && word.endsWith(">"));
    }

    private boolean isInnerClosingTag(String word){
        return (word.startsWith("</") && word.endsWith(">"));
    }

    private int getTagRank(Tag tag){
        String tagName=tag.getName().toLowerCase();
        int rank;
        switch (tagName) {
            case "title":
                rank = 50;
                break;
            case "header":
                rank = 40;
                break;
            case "h1":
                rank = 25;
                break;
            case "h2":
                rank = 24;
                break;
            case "h3":
                rank = 23;
                break;
            case "h4":
                rank = 22;
                break;
            case "h5":
                rank = 21;
                break;
            case "h6":
                rank = 20;
                break;
            case "mark":
                rank = 19;
                break;
            case "big":
                rank = 18;
                break;
            case "legend":
                rank = 15;
                break;
            case "b":
                rank = 13;
                break;
            case "strong":
                rank = 13;
                break;
            case "u":
                rank = 13;
                break;
            case "ins":
                rank = 13;
                break;
            case "i":
                rank = 12;
                break;
            case "em":
                rank = 12;
                break;
            case "li":
                rank = 11;
                break;
            case "th":
                rank = 11;
                break;
            case "td":
                rank = 11;
                break;
            case "dt":
                rank = 11;
                break;
            case "dd":
                rank = 11;
                break;
            case "q":
                rank = 10;
                break;
            case "blockquote":
                rank = 10;
                break;
            case "p":
                rank = 8;
                break;
            case "bdi":
                rank = 8;
                break;
            case "code":
                rank = 8;
                break;
            case "samp":
                rank = 8;
                break;
            case "kbd":
                rank = 8;
                break;
            case "var":
                rank = 8;
                break;
            case "pre":
                rank = 8;
                break;
            case "tt":
                rank = 8;
                break;
            case "figcaption":
                rank = 8;
                break;
            case "address":
                rank = 5;
                break;
            case "s":
                rank = 0;
                break;
             default:
                rank=1;
                break;
        }
        return rank;
    }

    private boolean isAStopWord(String word){
        return (Data.getStopWords().contains(word));
    }

    private int getTotalNumWords (Elements elements){
        Element mainElement=elements.get(0);
        ArrayList<String> innerHTML= new ArrayList<>(Arrays.asList(mainElement.text().split(" ")));
        return innerHTML.size();
    }
}