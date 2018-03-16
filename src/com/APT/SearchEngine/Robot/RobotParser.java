package com.APT.SearchEngine.Robot;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotParser
{
   private Map<String, ArrayList<String>> myMap = new HashMap<String, ArrayList<String>>();

   public boolean checkDissallowed (String url) throws IOException {
       URL uri = new URL(url);
       String host = uri.getHost();
       String robotUrl = "http://" + host + "/robots.txt";
       URL robotURL = new URL(robotUrl);
       HttpURLConnection connection = (HttpURLConnection) robotURL.openConnection();
       connection.connect();
       String contentType = connection.getContentType();
       if(contentType.contains("text/plain"))
       {
           String Content = readStringFromURL(robotUrl);
           Pattern pattern = Pattern.compile("(?<=User\\-agent\\: \\*).+?(?=(User-agent\\:)|$)",Pattern.MULTILINE);

           Matcher matcher = pattern.matcher(Content);
           if (matcher.find( )) {
               ArrayList<String[]> allowedanddisallowed = new ArrayList<>();
               allowedanddisallowed.add(matcher.group().split("\\n"));
           }
           else {
               System.out.println("NO MATCH");
           }
       }

       return true;
   }
    public static String readStringFromURL(String requestURL) throws IOException
    {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            String result = scanner.next();
            Pattern pattern = Pattern.compile("(^#.*$)|(^Sitemap.*$)|(^Crawl-delay.*$)|(^Host.*$)",Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(result);
            result=matcher.replaceAll("");
            return ((result.isEmpty())? "" : result);
        }
    }
}
