
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
    private static Indexer indexer=null;
//    private String[] documents=(String[]) Data.getMyDocuments().keySet().toArray();
    private String[] documents={"https://www.google.com.eg/?gfe_rd=cr&dcr=1&ei=2NaiWpTxL_CZX9-vrDA"};
    private int numThreads;

    private Indexer(){
    }

    public static Indexer getInstance(){
        if(indexer==null)
            indexer=new Indexer();
        return indexer;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void beginIndexing(){
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
            for(int i=allElements.size()-1;i>0;i--){

                //Get inner HTML content
                String innerHTML=allElements.get(i).text();

                //HTMLTag has content to insert into DB
                if (innerHTML.length()!=0){
                    //Get the tag of this element
                    Tag tag=allElements.get(i).tag();

                    //Skip some tags
                    if(isUnneccessaryTag(tag)) continue;
                    int type=getTagRank(tag);

                    //Get the element as an array of strings
                    List<String> items=purifyElements(allElements.get(i).toString());

                    for (int j=0;j<items.size();j++){
                        //Get a word from my items
                        String word=items.get(j);

                        //Checking if it's an empty word
                        if(word.equals("")) continue;

                        //This is needed to avoid inserting inner attributes as words
                        int occurrence;
                        if(j==0){
                            occurrence=getIndexOfClosingTagForFirstOpeningTag(word,items);
                            if(occurrence!=-1){
                                items.remove(occurrence);
                                j=occurrence-1;
                                continue;
                            }
                        }
                        else{
                            //This is needed to skip inner tags
                            occurrence=isInnerOpeningTag(word,items);
                            if(occurrence!=-1){
                                j = occurrence;
                                continue;
                            }
                        }

                        //if it's not a word or number
                        if(!(word.matches("[A-Za-z0-9ٍ]+"))) continue;

                        //Add this word to my list to save it to my Database
                        String stemmedWord=porterStemmer.stem(word);
                        WordModel processedWord=new WordModel(stemmedWord,documents[index],(i+2)-allElements.size(),type);
                        processedWords.add(processedWord);
                        System.out.print(processedWord.getWord()+"\n");

                        //Update count of word in page
                        if(!wordCount.containsKey(stemmedWord))
                            wordCount.put(stemmedWord,1);
                        else
                            wordCount.computeIfPresent(stemmedWord, (k, v) -> v + 1);
                    }
                }
            }

            //Save processed words to my database
            //....
            for (WordModel processedWord : processedWords) {
                try {
                    //Calculating word frequency
                    float wordFrequency = (float) wordCount.get(processedWord.getWord()) / totalNumWords;
                    processedWord.setFrequency(wordFrequency);



                } catch (Exception e) {
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

        //Recursive call
        index(index+numThreads,porterStemmer,myStack,processedWords,wordCount);
    }

    private int getIndexOfClosingTagForFirstOpeningTag(String word, List<String> items){
        //This is case for : <p color="red">
        int occurrence=-1;
        if(word.startsWith("<") && !word.endsWith(">"))
            occurrence=items.indexOf(">");
        return occurrence;
    }

    private List<String> purifyElements(String text){
        //Here we're modifying the string to remove HTML entities
        text = text.replace("&", " &");
        text = text.replace(";", "; ");

        //Split the string by white spaces as delimiters
        ArrayList<String> purifiedList= new ArrayList<>(Arrays.asList(text.split(" ")));

        //First we need to remove all HTML entities
        purifiedList.removeIf((String word) -> (word.startsWith("&") && word.endsWith(";")));

        //Second we need to remove all stop words
        purifiedList.removeIf(this::isAStopWord);

        //rejoin again all array into the string
        text=String.join(" ",purifiedList);

        //remove all unnecessary characters
        text = text.replace(","," ");
        text = text.replace("#"," ");
        text = text.replace("@"," ");
        text = text.replace("?", " ");
        text = text.replace(".", " ");
        text = text.replace("(", " ");
        text = text.replace(")", " ");
        text = text.replace("{", " ");
        text = text.replace("}", " ");
        text = text.replace(":", " ");
        text = text.replace("\\",  " ");
        text = text.replace("[", " ");
        text = text.replace("]", " ");
        text = text.replace("|", " ");
        text = text.replace("*", " ");
        text = text.replace("+", " ");
        text = text.replace("-", " ");
        text = text.replace("=", " ");
        text = text.replace("%", " ");
        text = text.replace("~", " ");
        text = text.replace(";", " ");
        text = text.replace("\"", " ");
        text = text.replace("$", " ");
        text = text.replace("&", " ");
        text = text.replace("!", " ");
        text = text.replace("'", " ");
        text = text.replace("^", " ");
        text = text.replace("<", " <");
        text = text.replace(">", "> ");
        text = text.replace("\n", " ");
        text = text.replace("×"," ");

        //split string again
        purifiedList= new ArrayList<>(Arrays.asList(text.split(" ")));

        //remove empty items
        purifiedList.removeIf((String word) -> word.equals(""));

        //This is needed to remove the latest closing parent tag
        purifiedList.remove(purifiedList.size()-1);

        //return list
        return purifiedList;
    }

    private int isInnerOpeningTag(String word,List<String> items){
        //We have two cases of inner tags
        //1st Case is : <i> ahmed </a> , words will be <i> ahmed </a>
        //2nd Case is : <a src="" href=""> ahmed </a> , words will be <a src href > ahmed </a>

        int occurrence=-1;
        if(!(word.startsWith("<") && word.length()>1))
            return occurrence;

        //split the string into characters
        char [] letters=word.toCharArray();

        StringBuilder tagName=new StringBuilder();

        //retrieve the tag name
        for (char letter : letters) if (letter != '<' && letter != '>') tagName.append(letter);

        //get index for the final closing tag
        occurrence=items.lastIndexOf("</"+tagName+">");

        return occurrence;
    }

    private boolean isUnneccessaryTag(Tag tag){
        return (tag.getName().equals("head") || tag.getName().equals("html")|| tag.getName().equals("div")|| tag.getName().equals("body")|| tag.getName().equals("style")|| tag.getName().equals("script"));
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

    //This method returns the total number of words in a document
    private int getTotalNumWords (Elements elements){
        Element mainElement=elements.get(0);
        ArrayList<String> innerHTML= new ArrayList<>(Arrays.asList(mainElement.text().split(" ")));
        return innerHTML.size();
    }
}