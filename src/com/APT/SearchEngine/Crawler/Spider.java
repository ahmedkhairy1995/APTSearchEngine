package com.APT.SearchEngine.Crawler;

import com.APT.SearchEngine.Data.Data;
import com.APT.SearchEngine.Ranker.DetailedUrl;
import com.APT.SearchEngine.Ranker.Ranker;
import com.APT.SearchEngine.Robot.RobotParser;
import javafx.util.Pair;
import org.jruby.RubyProcess;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.APT.SearchEngine.Database.*;

import static java.lang.Thread.sleep;

class Spider {

    private int maxPages = 5000;
    private Set<String> pagesVisited = Data.getDocuments();
    private Set<String> currentPages = new HashSet<>();
    private List<String> pagesToVisit = new LinkedList<>();
    private int numberOfThreads;
    private String bufferLine;
    private String currentPagesMemory = "Now.txt";
    private String pagesVisitedMemory = "Done.txt";
    private String pagesToVisitMemory = "Seeds.txt";
    private ArrayList<String> DisAllowedPath = new ArrayList<>();
    private ArrayList<Pair<String,Long>> DatabaseArray = new ArrayList<Pair<String,Long>>();
    private Set<String> newsPaperSites = Data.getNewsSite();
    private Database databaseConnection = Database.GetInstance() ;
    private ArrayList<Thread> threads=new ArrayList<>();
    private Ranker ranker;

    /*Constructor that takes # of threads and initializes all my variables */
    public Spider(int threadsNumber) throws IOException {
        numberOfThreads = threadsNumber;
        //Reading Ranker



        //read from the files over here

        //Reading Pages that we need to visit
        try {
            FileReader fileReader = new FileReader(pagesToVisitMemory);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((bufferLine = bufferedReader.readLine()) != null) {
               if (bufferLine.startsWith("http"))
               {
                   pagesToVisit.add(bufferLine);
               }

            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + pagesToVisitMemory + "'");

            File f = new File(pagesToVisitMemory);
            f.createNewFile();
        } catch (IOException ex) {
            System.out.println("Unable to read from file '" + pagesToVisitMemory + "'");
        }

        //Reading CurrentPages


        try {
            FileReader fileReader = new FileReader(currentPagesMemory);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((bufferLine = bufferedReader.readLine()) != null) {
                if (bufferLine.startsWith("http"))
                {
                    currentPages.add(bufferLine);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + currentPagesMemory + "'");

            File f = new File(currentPagesMemory);
            f.createNewFile();
        } catch (IOException ex) {
            System.out.println("Unable to read from file '" + currentPagesMemory + "'");
        }


        //Reading Visited Pages
        try {
            FileReader fileReader = new FileReader(pagesVisitedMemory);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((bufferLine = bufferedReader.readLine()) != null) {
                if (bufferLine.startsWith("http"))
                {
                    pagesVisited.add(bufferLine);
                }

            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + pagesVisitedMemory + "'");

            File f = new File(pagesVisitedMemory);
            f.createNewFile();
        } catch (IOException ex) {
            System.out.println("Unable to read from file '" + pagesVisitedMemory + "'");
        }



        currentPages.removeAll(pagesVisited);

     //   if(true || !readRanker())
       // {
       //     ranker = new Ranker();
       // }
        if (currentPages.size() == 0 && pagesVisited.size() == 0 && pagesToVisit.size() == 0) {
            //we are about to recrawl
            initializeFromDatabase();
            maxPages = DatabaseArray.size() + 1000;
        }

        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    Search(true,new RobotParser());
                }
            }));
        }

        for (Thread thread : threads)
            thread.start();

        try {
            for (Thread thread : threads)
                thread.join();

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }


        //ranker.computeRank();
        /*for(Map.Entry map: ranker.getUrlMap().entrySet())
        {
            insertRankIntoDB((DetailedUrl) map.getValue());
        }*/

    }




    /*Function that handles many threads trying to get next URL to access*/
    private String nextUrl(boolean firstTime,RobotParser Robot) {
        String next;

            if (pagesToVisit.size() != 0) {
                if (firstTime) {
                    synchronized (currentPages) {
                        if (currentPages.size() != 0) {

                            next = currentPages.iterator().next();
                            currentPages.remove(next);
                            //pagesVisited.add(next);

                            try {
                                if (!Robot.checAllowedAndkDisallowed(next)) {
                                    return "";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return next;
                        }
                    }
                }

                synchronized (pagesToVisit) {
                    do {
                        next = this.pagesToVisit.remove(0);
                    } while ((currentPages.contains(next) || pagesVisited.contains(next)) && pagesToVisit.size()!=0);
                   // pagesVisited.add(next);
                }
                try {
                    if (!Robot.checAllowedAndkDisallowed(next)) {
                        return "";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
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
    private void crawl(String URL,int NumberOfTrials)
    {
        try{
            String temp;
          Connection.Response response = Jsoup
                    .connect(URL)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .execute();
          if(response.header("Location")!= null)
          {
              URL = response.header("Location");
              if(pagesVisited.contains(URL)) {
                  return;
              }
          }
          //Test this
          //This webpage is not html so we will neglect
            if (response.header("content-type")!=null)
            {
                if(!response.header("content-type").contains("html"))
                {
                    return;
                }

            }


            Connection connection = Jsoup.connect(URL);
            Document htmlDocument = connection.get();
            Elements linksFound = htmlDocument.select("a[href]");

            //Check for the robots.txt over here :)
            //Lock the next part to avoid racing conditions
            synchronized (pagesToVisit)
            {
                ranker.insertURLFrom(URL,linksFound.size());
                for(Element link: linksFound)
                {
                    //remove hash tags
                    if(link.absUrl("href").indexOf('#') != -1)
                    {
                        continue;
                    }
                    else
                        temp = link.absUrl("href");
                    this.pagesToVisit.add(temp);
                    ranker.insertURLTo(temp);
                }
             //   updateRanker();
            }

            synchronized (pagesToVisitMemory)
            {
                for(Element link: linksFound)
                {
                    //remove hash tags
                    if(link.absUrl("href").indexOf('#') != -1)
                    {
                        continue;
                    }
                    else
                        temp = link.absUrl("href");

                    writeURL(temp,pagesToVisitMemory);
                }
            }

            synchronized (pagesVisitedMemory)
            {
                writeURL(URL,pagesVisitedMemory);
                pagesVisited.add(URL);
                //insert into db that is was visited
                insertIntoDB(URL,htmlDocument.toString(),linksFound.size());
            }


        }
        catch (UnknownHostException ex)
        {
            ex.printStackTrace();
            try {
                sleep(5000);
                if(NumberOfTrials != 3)
                    crawl(URL,NumberOfTrials + 1);
            }
            catch (InterruptedException ex1)
            {
                ex1.printStackTrace();
            }

        }
        catch(IOException ex){
            System.out.println("Error in the http request" + ex);
        }
        catch (IllegalArgumentException ex)
        {
            System.out.println("Continue, just a wrong file");
        }

    }

    private void insertIntoDB(String URL,String Doc,int Number)
    {
       synchronized(databaseConnection) {
           //call timons function twice
           try {

               databaseConnection.InsertAndUpdateRow("Crawler", URL, "Document", "Text", Doc);
               databaseConnection.InsertAndUpdateRow("Crawler", URL, "Document", "Indexed", "false");
               databaseConnection.InsertAndUpdateRow("Crawler", URL, "Document", "OutGoing", Integer.toString(Number));
               int x = 0;
           } catch (Exception ex) {
               ex.printStackTrace();
           }


    }

    private void insertRankIntoDB(Ranker target)
    {

            //call timons function twice
            for(Map.Entry map: ranker.getSimplifiedMapFrom().entrySet()) {


                try {
                    if (((String)map.getKey()).length() != 0 ) {
                        databaseConnection.InsertAndUpdateRow("Crawler", (String)map.getKey(), "Document", "InGoing",Integer.toString(ranker.getSimplifiedMapTo().get((String) map.getKey())) );

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
        catch(Exception ex) {
            System.out.println("Error writing to file '" + file + "'");

        }
    }

    public boolean ArraysAreNotEmpty()
    {
        return currentPages.size()!=0 && pagesToVisit.size()!=0 && pagesVisited.size()!=0;
    }


    private void Search(boolean firstTime,RobotParser robotParser) {

        while(pagesVisited.size() < maxPages  )
        {
            System.out.println(pagesVisited.size());
            String currentURL = nextUrl(firstTime,robotParser);
            if(!currentURL.equals("") && !currentURL.startsWith("mailto")) {
                crawl(currentURL,0);
            }
            if(!ArraysAreNotEmpty())
            {
                try
                {
                    sleep(5000);
                }
                catch(InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
            firstTime = false;
        }

        //Delete the three files
        try {
            PrintWriter writer = new PrintWriter(pagesToVisitMemory);
            writer.print("");
            writer.close();

            PrintWriter writer2 = new PrintWriter(currentPagesMemory);
            writer2.print("");
            writer2.close();

            PrintWriter writer3 = new PrintWriter(pagesVisitedMemory);
            writer3.print("");
            writer3.close();
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
    }


    private boolean isNewsPaper(String URL)
    {

        Iterator iterator = newsPaperSites.iterator();

        while (iterator.hasNext())
        {
            if(URL.toLowerCase().contains(((String)iterator.next()).toLowerCase()))
                return true;

        }
        return false;
    }




    private void initializeFromDatabase ()
    {
        //Come here if I found no seeds in the files ( first phase of crawling is done and now we are recrawling)


        //Fill the table of the database
        try
        {
            DatabaseArray = databaseConnection.getAllUrls("Crawler","Document","text");
            long millis = System.currentTimeMillis() % 1000;
            long oneDay = TimeUnit.DAYS.toMillis(1);
            long fourDays = TimeUnit.DAYS.toMillis(4);
            boolean notVisited = false;
           for (Pair<String,Long> item : DatabaseArray) {

                notVisited = false;
                if(isNewsPaper(item.getKey()))
                {
                    if(item.getValue() - millis > oneDay )
                    {
                       pagesToVisit.add(item.getKey());
                     notVisited = true;
                    }

                }
                if(item.getValue() - millis > fourDays )
                {
                    pagesToVisit.add(item.getKey());
                    notVisited = true;
                }
                if(!notVisited)
                    pagesVisited.add(item.getKey());
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

    private void updateRanker()
    {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fout = new FileOutputStream("ranker.ser", false);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(ranker);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
        finally {
            try{
                oos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    private boolean readRanker() {
        if (pagesVisited.size() != 0) {
            ObjectInputStream objectinputstream = null;
            try {
                //    FileOutputStream fout = new FileOutputStream("ranker.ser", true);
                //   ObjectOutputStream oos = new ObjectOutputStream(fout);
                //   oos.close();

                FileInputStream streamIn = new FileInputStream("ranker.ser");
                objectinputstream = new ObjectInputStream(streamIn);

                ranker = (Ranker) objectinputstream.readObject();
                if (ranker != null) {
                    objectinputstream.close();
                    return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                objectinputstream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
