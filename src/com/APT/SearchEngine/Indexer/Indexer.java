package com.APT.SearchEngine.Indexer;
//Shaalan
import com.APT.SearchEngine.Data.Data;
import com.APT.SearchEngine.Database.Database;
import com.APT.SearchEngine.Models.WordModel;
import opennlp.tools.stemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Indexer {
    private static Indexer indexer=null;
    private static PrintWriter  writer;
    private ArrayList<Thread> threads = new ArrayList<>();
    private Database databaseConnection = Database.GetInstance();
    private ArrayList<ArrayList<String>> documents;
    private int numThreads;

    private Indexer(){
    }

    public static Indexer getInstance(){
        if(indexer==null){
            try {
                writer = new PrintWriter("MyConsole.txt", "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            indexer=new Indexer();
        }
        return indexer;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void beginIndexing(){
        try{
            documents = databaseConnection.getDocumentDetails("Crawler","Document","Indexed");
        }
        catch (IOException e){
            e.printStackTrace();
        }

        for(int i=0;i<numThreads;i++){
            final int offset=i;
            threads.add(new Thread(new Runnable() {
                PorterStemmer porterStemmer;
                ArrayList<WordModel> processedWords;
                HashMap<String,Integer> wordCount;
                HashMap<String,Integer> wordRank;
                HashSet<String> originalWords;
                HashMap<String,String> positions;
                int indexCounter=offset;
                @Override
                public void run() {
                    porterStemmer=new PorterStemmer();
                    processedWords = new ArrayList<>();
                    wordCount = new HashMap<>();
                    wordRank = new HashMap<>();
                    originalWords = new HashSet<>();
                    positions = new HashMap<>();
                    index(indexCounter,porterStemmer,processedWords,wordCount,wordRank,originalWords,positions);
                }
            }));
        }

        for (Thread thread : threads)
            thread.start();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        writer.println("Closing!!!");
        writer.close();
    }

    private void index(int index,PorterStemmer porterStemmer,ArrayList<WordModel> processedWords,HashMap<String,Integer> wordCount,HashMap<String,Integer> wordRank, HashSet<String> originalWords, HashMap<String,String> positions){
        if(index>=documents.size())
            return;

        Document document;

        //Get the whole document in HTML Format
        try {
            document = Jsoup.parse(documents.get(index).get(2));

            if(document==null){
                index(index+numThreads,porterStemmer,processedWords,wordCount,wordRank,originalWords,positions);
                return;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            index(index+numThreads,porterStemmer,processedWords,wordCount,wordRank,originalWords,positions);
            return;
        }

        //Delete all previous entries for that specific link
        try {
            synchronized (databaseConnection){
                ArrayList<String>listOfWords= databaseConnection.getAllLinkWords("InvertedIndex","WordRank"
                        ,documents.get(index).get(0));
                databaseConnection.BulkDelete("InvertedIndex",listOfWords, "WordRank"
                        ,documents.get(index).get(0));
                databaseConnection.BulkDelete("InvertedIndex",listOfWords,"WordPosition"
                        ,documents.get(index).get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Get all document's text as an array of Strings
        ArrayList<String> documentText = new ArrayList<>(Arrays.asList(document.text().split(" ")));
        ArrayList<String> stemmedDocument = new ArrayList<>();

        //This is needed to stem each and every word in the document
        for(String word : documentText){
            word = porterStemmer.stem(word);
            stemmedDocument.add(word);
        }

        //Get all elements as a hierarchy in HTML Format
        Elements allElements = document.getAllElements();

        //Get total number of words in document
        int totalNumWords=getTotalNumWords(allElements);

        //Traversing from inner to outer
        for(int i=allElements.size()-1;i>0;i--){
            //Get the tag of this element
            Tag tag = allElements.get(i).tag();

            //Skip some tags
            if(isUnneccessaryTag(tag)) continue;

            //Get inner HTML content
            String innerHTML=allElements.get(i).text();

            //HTMLTag has content to insert into DB
            if (innerHTML.length()!=0){
                //Get tag ranking
                int type=getTagRank(tag);

                //Get the element as an array of strings
                List<String> items=purifyElements(allElements.get(i).toString());

                for (int j=0;j<items.size();j++){
                    //Get a word from my items
                    String word=items.get(j);

                    //This is needed to avoid inserting inner attributes as words
                    int occurrence;
                    if(j==0){
                        occurrence=getIndexForClosingTag(word,items,0);
                        if(occurrence!=-1){
                            items.remove(occurrence);
                            j=occurrence-1;
                            continue;
                        }
                    }
                    else{
                        //This is needed to skip inner tags
                        occurrence=isInnerOpeningTag(word,items,j);
                        if(occurrence!=-1){
                            j = occurrence;
                            continue;
                        }
                    }

                    //if it's not a word or number
                    if(!(word.matches("[A-Za-z0-9ٍ][A-Za-z0-9.]*"))) continue;

                    //Remove Non-Needed periods
                    word = removeNonNecessaryPeriods(word);

                    //convert word to lowercase
                    word = word.toLowerCase();

                    //Re-Checking if it's a stop word
                    if(isAStopWord(word)) continue;

                    //stem word using PorterStemmer
                    String stemmedWord = porterStemmer.stem(word);

                    //Stemmed Word did not exist before
                    if(!wordCount.containsKey(stemmedWord)){
                        //Inserting initial ranking for the stemmed word
                        wordCount.put(stemmedWord,1);
                        wordRank.put(stemmedWord,type);

                        WordModel processedWord = new WordModel(word,stemmedWord,documents.get(index).get(2));

                        //Adding the original word to a hash set
                        originalWords.add(word);
                        ArrayList<Integer> wordPositions = retrieveWordPositions(stemmedDocument,stemmedWord);
                        String wordPositionsText = wordPositions.stream()
                                .map( n -> n.toString() )
                                .collect( Collectors.joining( " " ) );
                        processedWord.setPositions(wordPositionsText);

                        positions.put(stemmedWord,wordPositionsText);

                        //Retrieve word positions
                        //Add this word to my list to save it to my Database
                        processedWords.add(processedWord);
                    }
                    //Stemmed Word is existing
                    else{
                        //Computing present ranking
                        wordCount.computeIfPresent(stemmedWord, (k, v) -> v + 1);
                        wordRank.computeIfPresent(stemmedWord, (k, v) -> v + type);

                        if(!originalWords.contains(word)){
                            //Adding original words to the list
                            WordModel processedWord = new WordModel(word,stemmedWord,documents.get(index).get(2));
                            processedWord.setPositions(positions.get(stemmedWord));
                            originalWords.add(word);
                            processedWords.add(processedWord);
                        }
                    }
                }
            }
        }

        //Save processed words to my database
        //....
        for (WordModel processedWord : processedWords) {
            try {
                //Calculating word frequency
                int countOfWord = wordCount.get(processedWord.getStemmedWord());
                double wordFrequency = ((double) countOfWord) / totalNumWords;

                //Set both word's frequency and ranking (both values are <=1)
                processedWord.setFrequency(wordFrequency);
                processedWord.setRank((double)wordRank.get(processedWord.getStemmedWord())/(countOfWord*50));

                synchronized (writer){
                    writer.println(processedWord.getStemmedWord() + "  , Link: " + documents.get(index).get(2) +"  , at Index: "+index);
                }

                synchronized (databaseConnection) {
                    databaseConnection.InsertAndUpdateRow("InvertedIndex",
                            processedWord.getOriginalWord(), "WordRank", processedWord.getDocument(), "" + processedWord.getRank());
                    databaseConnection.InsertAndUpdateRow("InvertedIndex",
                            processedWord.getOriginalWord(),"WordRank","StemmedWord",processedWord.getStemmedWord());
                    databaseConnection.InsertAndUpdateRow("InvertedIndex",
                            processedWord.getOriginalWord(),"WordPosition",processedWord.getDocument(),processedWord.getPositions());
                    databaseConnection.InsertAndUpdateRow("InvertedIndex",
                            processedWord.getOriginalWord(),"WordPosition","StemmedWord",processedWord.getStemmedWord());
                    databaseConnection.InsertAndUpdateRow("Crawler",
                            processedWord.getDocument(), "Document", "Indexed", "true");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if((index+numThreads)>=documents.size()) return;

        //Clear all data structures for the next document
        processedWords.clear();
        wordCount.clear();
        wordRank.clear();
        positions.clear();
        originalWords.clear();

        //Recursive call
        index(index+numThreads,porterStemmer,processedWords,wordCount,wordRank,originalWords,positions);
    }

    private ArrayList<Integer> retrieveWordPositions(ArrayList<String> stemmedDocument, String stemmedWord){
        ArrayList<Integer> positions = new ArrayList<>();
        ArrayList<String> subList;
        int occurrence;
        for (int i=0;i<stemmedDocument.size();i++){
            subList=new ArrayList<>(stemmedDocument.subList(i,stemmedDocument.size()));
            occurrence = subList.indexOf(stemmedWord);
            if(occurrence!=-1){
                positions.add(i+occurrence);
                i+=(occurrence -1);
            }
            else break;
        }
        return positions;
    }

    private int getIndexForClosingTag(String word, List<String> items,int startingPos){
        //This is case for : <p color="red" >
        int occurrence=-1;

        ArrayList<String> mySubList=new ArrayList<>(items.subList(startingPos,items.size()));

        if (word.startsWith("<") && !word.endsWith(">")){
            //Get closing tag position for tags like <img> for example

            int closingTagOccurrence = mySubList.indexOf("/>");
            if(closingTagOccurrence!=-1)
                occurrence = closingTagOccurrence + startingPos;

            if(occurrence==-1){
                closingTagOccurrence = mySubList.indexOf(">");
                if(closingTagOccurrence!=-1)
                    occurrence = closingTagOccurrence + startingPos;
            }

            if (occurrence==-1){
                for(int i=startingPos;i<items.size();i++){
                    String string = items.get(i);
                    if(string.endsWith(">") && !string.startsWith("<") && string.length()>1)
                        occurrence=i;
                }
            }
        }
        return occurrence;
    }

    private List<String> purifyElements(String text){
        //convert text to lowercase
        text=text.toLowerCase();

        //Here we're modifying the string to remove HTML entities
        text = text.replace("&", " &");
        text = text.replace(";", "; ");

        //Split the string by white spaces as delimiters
        ArrayList<String> purifiedList= new ArrayList<>(Arrays.asList(text.split(" ")));

        //First we need to remove all HTML entities
        purifiedList.removeIf((String word) -> (word.startsWith("&") && word.endsWith(";")));

        //rejoin again all array into the string
        text=String.join(" ",purifiedList);

        //remove all unnecessary characters
        text = text.replace(","," ");
        text = text.replace("#"," ");
        text = text.replace("@"," ");
        text = text.replace("?", " ");
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

        //This is needed to remove the latest closing parent tag
        purifiedList.remove(purifiedList.size()-1);
        //This is needed to remove the latest closing parent tag

        //This is needed to remove empty strings
        purifiedList.removeIf((String word)->word.length()==0);

        //Second we need to remove all stop words
        purifiedList.removeIf(this::isAStopWord);

        //return list
        return purifiedList;
    }

    private int isInnerOpeningTag(String word,List<String> items,int startingPos){
        //We have two cases of inner tags
        //1st Case is : <i> ahmed </i> , words will be <i> ahmed </i>
        //2nd Case is : <a src="" href=""> ahmed </a> , words will be <a src href > ahmed </a>

        int occurrence=-1;
        if(!(word.startsWith("<") && word.length()>1))
            return occurrence;

        //split the string into characters
        char [] letters=word.toCharArray();

        StringBuilder tagName=new StringBuilder();

        //retrieve the tag name
        for (char letter : letters) if (letter != '<' && letter != '>') tagName.append(letter);

        ArrayList<String> mySubList=new ArrayList<>(items.subList(startingPos,items.size()));

        //get index for the final closing tag
        int closingTagOccurrence = mySubList.lastIndexOf("</"+tagName+">");

        if(closingTagOccurrence!=-1)
            occurrence = closingTagOccurrence + startingPos;

        if(occurrence==-1)
            occurrence=getIndexForClosingTag(word,items,startingPos);
        return occurrence;
    }

    private boolean isUnneccessaryTag(Tag tag){
        return (tag.getName().equals("head") ||tag.getName().equals("require-auth")||tag.getName().equals("noscript")||tag.getName().equals("nav")|| tag.getName().equals("input")|| tag.getName().equals("footer")|| tag.getName().equals("form") || tag.getName().equals("html")|| tag.getName().equals("div")|| tag.getName().equals("body")|| tag.getName().equals("style")|| tag.getName().equals("mstyle")|| tag.getName().equals("script")|| tag.getName().contains("style")||tag.getName().contains("script"));
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

    private String removeNonNecessaryPeriods(String word){
        Pattern pattern = Pattern.compile("[.].+");

        Matcher matcher = pattern.matcher(word);

        word=matcher.replaceAll(".");

        //If a word ends with a dot like => "using facebook."
        if(word.endsWith(".")) word = word.substring(0, word.length() - 1);
        return word;
    }
}


