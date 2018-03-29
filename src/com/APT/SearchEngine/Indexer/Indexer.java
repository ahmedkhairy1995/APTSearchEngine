package com.APT.SearchEngine.Indexer;

import com.APT.SearchEngine.Data.Data;
import com.APT.SearchEngine.Models.WordModel;
import opennlp.tools.stemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer {
    private static Indexer indexer=null;
    private static PrintWriter  writer;
    private ArrayList<Thread> threads = new ArrayList<>();
    private String[] documents={"https://en.wikipedia.org/wiki/Kinetics_(physics)" ,
            "https://en.wikipedia.org/wiki/Kinematics" ,
            "https://en.wikipedia.org/wiki/Second_law_of_motion" ,
            "https://en.wikipedia.org/wiki/Statistical_mechanics" ,
            "https://en.wikipedia.org/wiki/Statics" ,
            "https://en.wikipedia.org/wiki/Couple_(mechanics)" ,
            "https://en.wikipedia.org/wiki/Angular_momentum" ,
            "https://en.wikipedia.org/wiki/Acceleration" ,
            "https://en.wikipedia.org/wiki/Potential_energy" ,
            "https://en.wikipedia.org/wiki/Kinetic_energy" ,
            "https://en.wikipedia.org/wiki/D%27Alembert%27s_principle" ,
            "https://en.wikipedia.org/wiki/Frame_of_reference" ,
            "https://en.wikipedia.org/wiki/Impulse_(physics)" ,
            "https://en.wikipedia.org/wiki/Inertia" ,
            "https://en.wikipedia.org/wiki/Mass" ,
            "https://en.wikipedia.org/wiki/Inertial_frame_of_reference" ,
            "https://en.wikipedia.org/wiki/Work_(physics)" ,
            "https://en.wikipedia.org/wiki/Moment_of_inertia" ,
            "https://en.wikipedia.org/wiki/Power_(physics)" ,
            "https://en.wikipedia.org/wiki/Space" ,
            "https://en.wikipedia.org/wiki/Momentum" ,
            "https://en.wikipedia.org/wiki/Moment_(physics)" ,
            "https://en.wikipedia.org/wiki/Torque" ,
            "https://en.wikipedia.org/wiki/Time" ,
            "https://en.wikipedia.org/wiki/Speed" ,
            "https://en.wikipedia.org/wiki/Virtual_work" ,
            "https://en.wikipedia.org/wiki/Velocity" ,
            "https://en.wikipedia.org/wiki/Newton%27s_laws_of_motion" ,
            "https://en.wikipedia.org/wiki/Analytical_mechanics" ,
            "https://en.wikipedia.org/wiki/Lagrangian_mechanics" ,
            "https://en.wikipedia.org/wiki/Routhian_mechanics" ,
            "https://en.wikipedia.org/wiki/Hamilton%E2%80%93Jacobi_equation" ,
            "https://en.wikipedia.org/wiki/Udwadia%E2%80%93Kalaba_equation" ,
            "https://en.wikipedia.org/wiki/Koopman%E2%80%93von_Neumann_classical_mechanics" ,
            "https://en.wikipedia.org/wiki/Hamiltonian_mechanics" ,
            "https://en.wikipedia.org/wiki/Appell%27s_equation_of_motion" ,
            "https://en.wikipedia.org/wiki/Equations_of_motion" ,
            "https://en.wikipedia.org/wiki/Damping_ratio" ,
            "https://en.wikipedia.org/wiki/Damping" ,
            "https://en.wikipedia.org/wiki/Friction" ,
            "https://en.wikipedia.org/wiki/Euler%27s_laws_of_motion" ,
            "https://en.wikipedia.org/wiki/Harmonic_oscillator" ,
            "https://en.wikipedia.org/wiki/Non-inertial_reference_frame" ,
            "https://en.wikipedia.org/wiki/Fictitious_force" ,
            "https://en.wikipedia.org/wiki/Linear_motion" ,
            "https://en.wikipedia.org/wiki/Relative_velocity" ,
            "https://en.wikipedia.org/wiki/Newton%27s_law_of_universal_gravitation" ,
            "https://en.wikipedia.org/wiki/Mechanics_of_planar_particle_motion" ,
            "https://en.wikipedia.org/wiki/Euler%27s_equations_(rigid_body_dynamics)" ,
            "https://en.wikipedia.org/wiki/Rigid_body_dynamics" ,
            "https://en.wikipedia.org/wiki/Rigid_body" ,
            "https://en.wikipedia.org/wiki/Rotation_around_a_fixed_axis" ,
            "https://en.wikipedia.org/wiki/Vibration" ,
            "https://en.wikipedia.org/wiki/Simple_harmonic_motion" ,
            "https://en.wikipedia.org/wiki/Centripetal_force" ,
            "https://en.wikipedia.org/wiki/Rotating_reference_frame" ,
            "https://en.wikipedia.org/wiki/Circular_motion" ,
            "https://en.wikipedia.org/wiki/Coriolis_force" ,
            "https://en.wikipedia.org/wiki/Reactive_centrifugal_force" ,
            "https://en.wikipedia.org/wiki/Centrifugal_force" ,
            "https://en.wikipedia.org/wiki/Angular_acceleration" ,
            "https://en.wikipedia.org/wiki/Rotational_speed" ,
            "https://en.wikipedia.org/wiki/Pendulum_(mathematics)" ,
            "https://en.wikipedia.org/wiki/Angular_velocity" ,
            "https://en.wikipedia.org/wiki/Angular_frequency" ,
            "https://en.wikipedia.org/wiki/Angular_displacement" ,
            "https://en.wikipedia.org/wiki/Jeremiah_Horrocks" ,
            "https://en.wikipedia.org/wiki/Christiaan_Huygens" ,
            "https://en.wikipedia.org/wiki/Galileo_Galilei" ,
            "https://en.wikipedia.org/wiki/Jean_le_Rond_d%27Alembert" ,
            "https://en.wikipedia.org/wiki/Leonhard_Euler" ,
            "https://en.wikipedia.org/wiki/Edmond_Halley" ,
            "https://en.wikipedia.org/wiki/Pierre-Simon_Laplace" ,
            "https://en.wikipedia.org/wiki/Joseph-Louis_Lagrange" ,
            "https://en.wikipedia.org/wiki/Alexis_Clairaut" ,
            "https://en.wikipedia.org/wiki/Daniel_Bernoulli" ,
            "https://en.wikipedia.org/wiki/Sim%C3%A9on_Denis_Poisson" ,
            "https://en.wikipedia.org/wiki/William_Rowan_Hamilton" ,
            "https://en.wikipedia.org/wiki/Template:Classical_mechanics" ,
            "https://en.wikipedia.org/wiki/Augustin-Louis_Cauchy" ,
            "https://en.wikipedia.org/wiki/Johann_Bernoulli" ,
            "https://en.wikipedia.org/wiki/Quantum_mechanics" ,
            "https://en.wikipedia.org/w/index.php?title=Template:Classical_mechanics&action=edit" ,
            "https://en.wikipedia.org/wiki/Template_talk:Classical_mechanics" ,
            "https://en.wikipedia.org/wiki/Glossary_of_elementary_quantum_mechanics" ,
            "https://en.wikipedia.org/wiki/Introduction_to_quantum_mechanics" ,
            "https://en.wikipedia.org/wiki/Schr%C3%B6dinger_equation" ,
            "https://en.wikipedia.org/wiki/Bra%E2%80%93ket_notation" ,
            "https://en.wikipedia.org/wiki/Old_quantum_theory" ,
            "https://en.wikipedia.org/wiki/History_of_quantum_mechanics" ,
            "https://en.wikipedia.org/wiki/Complementarity_(physics)" ,
            "https://en.wikipedia.org/wiki/Interference_(wave_propagation)" ,
            "https://en.wikipedia.org/wiki/Hamiltonian_(quantum_mechanics)" ,
            "https://en.wikipedia.org/wiki/Energy_level" ,
            "https://en.wikipedia.org/wiki/Quantum_entanglement" ,
            "https://en.wikipedia.org/wiki/Quantum_decoherence" ,
            "https://en.wikipedia.org/wiki/Quantum_number" ,
            "https://en.wikipedia.org/wiki/Quantum_nonlocality" ,
            "https://en.wikipedia.org/wiki/Measurement_in_quantum_mechanics" ,
            "https://en.wikipedia.org/wiki/Symmetry_in_quantum_mechanics" ,
            "https://en.wikipedia.org/wiki/Quantum_superposition"};
    private int numThreads;

    private Indexer(){
    }

    public static Indexer getInstance(){
        if(indexer==null){
            try {
                writer = new PrintWriter("MyConsole.txt", "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
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
        for(int i=0;i<numThreads;i++){
            final int offset=i;
            threads.add(new Thread(new Runnable() {
                PorterStemmer porterStemmer;
                ArrayList<WordModel> processedWords;
                HashMap<String,Integer> wordCount;
                HashMap<String,Integer> wordRank;
                int indexCounter=offset;
                @Override
                public void run() {
                    porterStemmer=new PorterStemmer();
                    processedWords = new ArrayList<>();
                    wordCount = new HashMap<>();
                    wordRank = new HashMap<>();
                    index(indexCounter,porterStemmer,processedWords,wordCount,wordRank);
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

    private void index(int index,PorterStemmer porterStemmer,ArrayList<WordModel> processedWords,HashMap<String,Integer> wordCount,HashMap<String,Integer> wordRank){
        if(index>=documents.length)
            return;

        Document document = null;
        try{
            //Get the whole document in HTML Format
            try {
                document = Jsoup.connect(documents[index]).get();

                if(document==null)
                    //Recursive call
                    index(index+numThreads,porterStemmer,processedWords,wordCount,wordRank);
            }
            catch (UnsupportedMimeTypeException e){
                e.printStackTrace();
                //Recursive call
                index(index+numThreads,porterStemmer,processedWords,wordCount,wordRank);
            }


            //Delete all previous entries for that specific link


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

                        //Word did not exist before
                        if(!wordCount.containsKey(stemmedWord)){
                            wordCount.put(stemmedWord,1);
                            wordRank.put(stemmedWord,type);

                            WordModel processedWord=new WordModel(stemmedWord,documents[index]);

                            //Add this word to my list to save it to my Database
                            processedWords.add(processedWord);
                        }
                        //Word is existing
                        else{
                            wordCount.computeIfPresent(stemmedWord, (k, v) -> v + 1);
                            wordRank.computeIfPresent(stemmedWord, (k, v) -> v + type);
                        }
                    }
                }
            }

            //Save processed words to my database
            //....
            for (WordModel processedWord : processedWords) {
                try {
                    //Calculating word frequency
                    int countOfWord = wordCount.get(processedWord.getWord());
                    float wordFrequency = ((float) countOfWord) / totalNumWords;

                    //Set both word's frequency and ranking (both values are <=1)
                    processedWord.setFrequency(wordFrequency);
                    processedWord.setRank(wordRank.get(processedWord.getWord())/(countOfWord*50));

                    synchronized (writer){
                        writer.println(processedWord.getWord() + "  , Link: " + documents[index] +"  , at Index: "+index);
                    }
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
        processedWords.clear();
        wordCount.clear();
        wordRank.clear();

        //Recursive call
        index(index+numThreads,porterStemmer,processedWords,wordCount,wordRank);
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

        //get index for the final closing tag
        occurrence=items.lastIndexOf("</"+tagName+">");

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