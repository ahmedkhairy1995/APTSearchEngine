package com.APT.SearchEngine.Robot;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

public class RobotParser
    {
        private static Map<String, ArrayList<String>> AllowedURLList = new HashMap<String, ArrayList<String>>();
        private static Map<String, ArrayList<String>> DisallowedURLList = new HashMap<String, ArrayList<String>>();


        public  boolean checAllowedAndkDisallowed (String urlLink ) throws IOException {
            URL url = new URL(urlLink);                                  // make url object
            String host = url.getHost();                                 // get host
            if (host == null)
            {
                return false;
            }
            String path = "/"+url.getPath();
            boolean reparse = false;
            if (!AllowedURLList.containsKey(host)&& !DisallowedURLList.containsKey(host))
            {
                reparse = true;
            }
            if (!reparse)             /// if not asked to reparse the robot.txt
            {
                if (checklongestmatch(host,path))
                    return  true;
                else
                    return false;
            }

            String robotUrl = "http://" + host + "/robots.txt";
            URL robotURL = new URL(robotUrl);
            HttpURLConnection connection = (HttpURLConnection) robotURL.openConnection();
            connection.connect();
            String contentType = connection.getContentType();            //check type to make sure it's correct
            if(contentType!=null && contentType.contains("text/plain"))
            {
                String Content = readFromRobotTXT(robotUrl);             ///get the content as string without extra unneeded comments and instructions
                Pattern pattern = Pattern.compile("(?<=User\\-agent\\: \\*).+?(?=(User-agent\\:)|$)",Pattern.DOTALL); // get the required instructions
                Matcher matcher = pattern.matcher(Content);
                if (matcher.find( )) {              // if the instructions were found
                    List<String> listOfAllowAndDisallow = new ArrayList<String>(Arrays.asList(matcher.group().split("\\n"))); // split the instructions into array
                    listOfAllowAndDisallow.removeAll(Arrays.asList("", null)); // remove nulls and empty strings
                    ArrayList<String> allowPatterns = new ArrayList<>();
                    ArrayList<String> disallowPatterns= new ArrayList<>();
                    boolean Allowed = false;
                    boolean DisAllowed =false;
                    int matchedallowedlength =0;
                    int matcheddisallowedlength =0;
                    for (int i = 0 ; i<listOfAllowAndDisallow.size();i++)
                    {
                        pattern = Pattern.compile(": (.*)");                // get the patterns
                        matcher = pattern.matcher(listOfAllowAndDisallow.get(i));
                        if (matcher.find())                     // if patterns were found
                        {
                            String escaped="^"+StringEscapeUtils.escapeJava(matcher.group(1));     // escape the pattern if needed
                            escaped=escaped.replaceAll("\\*",".*");       // replace * with .* to work as regex
                            escaped=escaped.replaceAll("\\?","\\\\?");
                            if (!escaped.endsWith("$"))
                                escaped+=".*";                                   // add .* to have all the combinations

                            pattern = Pattern.compile(escaped);
                            matcher = pattern.matcher(path);                 // check if the path matches one of the patterns
                            if (listOfAllowAndDisallow.get(i).startsWith("Allow"))
                            {
                                allowPatterns.add(escaped);             // add to allowpatterns list
                                if (matcher.find())                     // if the path matches it
                                {
                                    Allowed = true;
                                    matchedallowedlength =escaped.length();

                                }
                            }
                            else
                            {
                                disallowPatterns.add(escaped);
                                if (matcher.find())
                                {
                                    DisAllowed = true;
                                    matcheddisallowedlength = escaped.length();
                                }
                            }
                        }
                    }
                    AllowedURLList.put(host,allowPatterns);
                    DisallowedURLList.put(host,disallowPatterns);
                    if (Allowed && DisAllowed)
                    {
                        if (matchedallowedlength > matcheddisallowedlength) {
                            return true;
                        } else
                            return false;
                    }
                    else if (Allowed && !DisAllowed)
                    {
                        return true;
                    }

                    else if (!Allowed && DisAllowed)
                        return false;

                }
            }
            return true;
        }
        public  String readFromRobotTXT(String requestURL) throws IOException
        {
            try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                    StandardCharsets.UTF_8.toString()))
            {
                scanner.useDelimiter("\\A");
                if(!scanner.hasNext())
                    return "";
                String result = scanner.next();
                Pattern pattern = Pattern.compile("(^#.*$)|(^Sitemap.*$)|(^Crawl-delay.*$)|(^Host.*$)",Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(result);
                result=matcher.replaceAll("");

                return ((result.isEmpty())? "" : result);
            }
        }

        public boolean checklongestmatch (String host,String path)
        {
            boolean matchedallowed = false;
            int matchedallowedlength =0;
            boolean matcheddisallowed = false;
            int matcheddisallowedlength =0;
            if (AllowedURLList.containsKey(host))                            // check if the robots.txt has been parsed before
            {
                ArrayList<String> AllowedPatterns =AllowedURLList.get(host);  // if yes get the patterns and check if they are satisfy the given url
                Pattern pattern;
                Matcher matcher;
                for (int i=0;i<AllowedPatterns.size();i++)
                {
                    String patternstring =AllowedPatterns.get(i);
                    pattern = Pattern.compile(patternstring);
                    matcher = pattern.matcher(path);
                    if (matcher.find())
                    {
                        matchedallowed = true;
                        matchedallowedlength = patternstring.length();
                    }
                }
            }
            if (DisallowedURLList.containsKey(host))
            {
                ArrayList<String> DisallowedPatterns =DisallowedURLList.get(host);
                Pattern pattern;
                Matcher matcher;
                for (int i=0;i<DisallowedPatterns.size();i++)
                {
                    String patternstring = DisallowedPatterns.get(i);
                    pattern = Pattern.compile(patternstring);
                    matcher = pattern.matcher(path);
                    if (matcher.find())
                    {
                        matcheddisallowed = true;
                        matcheddisallowedlength = patternstring.length();
                    }
                }
            }
            if (matchedallowed && matcheddisallowed) {
                if (matchedallowedlength > matcheddisallowedlength) {
                    return true;
                } else
                    return false;
            }
            else if (matchedallowed && ! matcheddisallowed)
            {
                return true;
            }
            else if (!matchedallowed && matcheddisallowed)
                return false;

            return true;
        }


    }

