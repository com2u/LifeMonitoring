package com2u.de.LifeMonitoring;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TwitterClient {
	private static PropertiesLoader prop = new PropertiesLoader();
	private static String setOAuthConsumerKey = "0vPHibZTQ0jZOM9prKWxjR4CJ";
	private static String setOAuthConsumerSecret = "LGIgnzRnKEIyRta0rAy5V953SzPFL3lKXtDa5GMBliwb986X1F";
	private static String setOAuthAccessToken = "2879614206-32FAS3aOHL2d3qlR9lV0Nsnsta01noo2DjeRnEj";
	private static String setOAuthAccessTokenSecret = "aexOSRACEeM650GMG0vswlkqqePWJtP1O7vySO20YCW7C";
	
    
    public static void main(String[] args) throws Exception {
    	
    	
    }
    
    
    public TwitterMeasurment checkTwitter(String hashtag){
    	TwitterMeasurment twitt = new TwitterMeasurment();
    	twitt.hashtag = hashtag;
        try {
    	setOAuthConsumerKey = prop.get("TwitterOAuthConsumerKey");
    	setOAuthConsumerSecret = prop.get("TwitterOAuthConsumerSecret");
    	setOAuthAccessToken = prop.get("TwitterOAuthAccessToken");
    	setOAuthAccessTokenSecret = prop.get("TwitterOAuthAccessTokenSecret");
    	ConfigurationBuilder cb = new ConfigurationBuilder();
        System.out.println("Twitter search :"+hashtag);
        
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(setOAuthConsumerKey)
                .setOAuthConsumerSecret(setOAuthConsumerSecret)
                .setOAuthAccessToken(setOAuthAccessToken)
                .setOAuthAccessTokenSecret(setOAuthAccessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        Query query = new Query(hashtag);
        query.count(150);
        QueryResult result = twitter.search(query);
        //Pattern pattern = Pattern.compile("http://t.co/\\w{10}");
        //Pattern imagePattern = Pattern.compile("https\\:\\/\\/pbs\\.twimg\\.com/media/\\w+\\.(png | jpg | gif)(:large)?");
        java.util.Date now = new java.util.Date();
        for (Status status : result.getTweets()) {
        	if (status.isRetweet())
                continue;
            if ((now.getTime() - 600000) < status.getCreatedAt().getTime()) {
            	twitt.tweetCount++;
            	if (twitt.maxRetweet < status.getRetweetCount()) {
            		twitt.maxRetweet = status.getRetweetCount();
            		if (twitt.maxRetweet > 10) {
            			twitt.topTweet = status.getText();
            		}
            	}
            	twitt.avgRetweet += status.getRetweetCount();
        		System.out.println(twitt.tweetCount+" :"+status.getText());
        		System.out.println("Retweet:"+status.getRetweetCount());
        		System.out.println("Time   :"+status.getCreatedAt());
        		if (status.getGeoLocation() != null) {
        			double longPos = 11.569451 - status.getGeoLocation().getLongitude();
        			double latPos = 48.178037 - status.getGeoLocation().getLatitude();
        			double dist = java.lang.Math.sqrt(longPos*longPos+latPos*latPos);
        			System.out.println("Location   :"+dist+" -> "+status.getGeoLocation().getLatitude()+","+status.getGeoLocation().getLongitude());
        			twitt.distanceCount++;
        			twitt.distanceAvg+=dist;
        		}
        		continue;
        	} 
        }
        twitt.avgRetweet =  (twitt.avgRetweet/twitt.tweetCount);
        twitt.distanceAvg = (twitt.distanceAvg/twitt.distanceCount);
        System.out.println("TweetCount :"+twitt.tweetCount);
        System.out.println("MaxRetweet :"+twitt.maxRetweet);
        System.out.println("AvgRetweet :"+twitt.avgRetweet);
        System.out.println("TweetCount :"+twitt.tweetCount);
        System.out.println("DistaCount :"+twitt.distanceCount);
        System.out.println("AvgDistance:"+twitt.distanceAvg);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
        return twitt;
        

    }

    
    
}
