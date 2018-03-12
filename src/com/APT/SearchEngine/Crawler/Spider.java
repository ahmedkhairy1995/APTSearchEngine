package com.APT.SearchEngine.Crawler;

import com.APT.SearchEngine.Data.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

import me.jamesfrost.robotsio.*;



class Spider {
    private static final int maxPages = 5000;
    private Set<String> pagesVisited = Data.getDocuments();
    private Set<String> currentPages = new HashSet<>();
    private List<String> pagesToVisit = new LinkedList<>();
    private int numberOfThreads;
    private String bufferLine;
    private String currentPagesMemory = "Now.txt";
    private String pagesVisitedMemory = "Done.txt";
    private String pagesToVisitMemory = "Seeds.txt";
    private RobotsParser MyParser = new RobotsParser("Shaalan");
    private ArrayList<String> DisAllowedPath = new ArrayList<>();



    /*Constructor that takes # of threads and initializes all my variables */
    public Spider(int threadsNumber) throws IOException {
        numberOfThreads = threadsNumber;

        //read from the files over here

        //Reading CurrentPages

        try {
            FileReader fileReader = new FileReader(currentPagesMemory);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while( (bufferLine = bufferedReader.readLine()) != null)
            {
                currentPages.add(bufferLine);
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException ex){
            System.out.println("Unable to open file '" + currentPagesMemory + "'");

            File f = new File(currentPagesMemory);
            f.createNewFile();
        }
        catch (IOException ex)
        {
            System.out.println("Unable to read from file '" + currentPagesMemory +"'");
        }


        //Reading Visited Pages
        try {
            FileReader fileReader = new FileReader(pagesVisitedMemory);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while( (bufferLine = bufferedReader.readLine()) != null)
            {
                pagesVisited.add(bufferLine);
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException ex){
            System.out.println("Unable to open file '" + pagesVisitedMemory + "'");

            File f = new File(pagesVisitedMemory);
            f.createNewFile();
        }
        catch (IOException ex)
        {
            System.out.println("Unable to read from file '" + pagesVisitedMemory +"'");
        }

        //Reading Pages that we need to visit
        try {
            FileReader fileReader = new FileReader(pagesToVisitMemory);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while( (bufferLine = bufferedReader.readLine()) != null)
            {
                System.out.println(bufferLine);
                pagesToVisit.add(bufferLine);
            }
            bufferedReader.close();
        }
        catch (FileNotFoundException ex){
            System.out.println("Unable to open file '" + pagesToVisitMemory + "'");

            File f = new File(pagesToVisitMemory);
            f.createNewFile();
        }
        catch (IOException ex)
        {
            System.out.println("Unable to read from file '" + pagesToVisitMemory +"'");
        }

        currentPages.removeAll(pagesVisited);
        for(int i=0; i<numberOfThreads;i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Search();
                }
            }).start();
        }
    }

    /*Function that handles many threads trying to get next URL to access*/
    private String nextUrl() {
        String next;
        if(pagesToVisit.size()!=0) {
            synchronized (pagesToVisit) {
                do {
                    next = this.pagesToVisit.remove(0);
                } while (pagesVisited.contains(next));
                pagesVisited.add(next);
            }

            synchronized (currentPages) {
                this.currentPages.add(next);
            }
            synchronized (currentPagesMemory) {
                writeURL(next, currentPagesMemory);
            }
            return next;
        }
        return "";
    }

    //Given a URL, this function will return all the links inside this webpage
    //Recheck for adding urls to files while working
    private void crawl(String URL)
    {
        try{
            Connection connection = Jsoup.connect(URL);
            Document htmlDocument = connection.get();
            Elements linksFound = htmlDocument.select("a[href]");

            //Lock the next part to avoid racing conditions
            synchronized (pagesToVisit)
            {
                for(Element link: linksFound)
                {
                    this.pagesToVisit.add(link.absUrl("href"));
                }
            }

            synchronized (pagesToVisitMemory)
            {
                for(Element link: linksFound)
                {
                    writeURL(link.absUrl("href"),pagesToVisitMemory);
                }
            }

            synchronized (pagesVisitedMemory)
            {
                writeURL(URL,pagesVisitedMemory);
            }


        }
        catch(IOException ex){
            System.out.println("Error in the http request" + ex);
        }

    }

    private void writeURL(String URL,String file) {
        try {
            // Assume default encoding.
            FileWriter fileWriter = new FileWriter(file,true);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Note that write() does not automatically
            // append a newline character.
            bufferedWriter.write(URL);
            bufferedWriter.newLine();


            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex) {
            System.out.println("Error writing to file '" + file + "'");

        }
    }

    private void Search() {
        while(pagesVisited.size() < maxPages)
        {
            String currentURL = nextUrl();
            System.out.println(currentURL);
            if(!currentURL.equals("")) {
                crawl(currentURL);
            }
        }
    }

}
