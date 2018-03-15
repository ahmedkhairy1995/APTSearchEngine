package com.APT.SearchEngine.Crawler;

import java.net.*;
import java.io.IOException;

public class TestIP {
    public static void main(String[] args)
    {
        String temp ="https://github.com";
        String temp2="https://goo.gl/hDSPNx";
        String temp3 = "google.com";
        String temp4 ="https://www.google.com.eg/search?q=UnknownHostException+reasons&rlz=1C1CHWA_enEG664EG664&oq=UnknownHostException+reasons&aqs=chrome..69i57j0l5.6205j0j7&sourceid=chrome&ie=UTF-8";

        try {
            InetAddress ip= InetAddress.getByName(temp3);
            InetAddress ip2= InetAddress.getByName(temp);

            if(ip == ip2)
            {
                System.out.println("I'm working");
            }
            else
            {

                System.out.println("I'm not :( working");

            }

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            System.out.println("lol " +
                    "you loser");
        }
    }
}
