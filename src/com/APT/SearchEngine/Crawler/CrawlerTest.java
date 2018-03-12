package com.APT.SearchEngine.Crawler;

import java.io.IOException;
import java.util.ArrayList;

import me.jamesfrost.robotsio.*;

public class CrawlerTest {

    public static void main(String[] args)
    {
        RobotsParser me = new RobotsParser("test");
        try {
            System.out.println("New stuff out here");
            me.connect("https://github.com/robots.txt");
            System.out.println("El 7akaaam");
            ArrayList<String> ok = me.getDisallowedPaths();
            for(int i=0;i<100;i++) {
                if (me.isAllowed("https://github.com")) {
                    System.out.println("ji");
                } else {
                    System.out.println("ds");
                }
            }
        }
        catch (me.jamesfrost.robotsio.RobotsDisallowedException ex)
        {
            System.out.println("lol");

        }
        catch (java.net.MalformedURLException ex)
        {
            System.out.println("lol5");
        }
        /*
        try {
            Spider spider = new Spider(4);

        }
        catch (IOException ex)
        {
            System.out.println("lol you loser");
        }*/
    }
}
