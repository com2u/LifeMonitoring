package com2u.de.LifeMonitoring;

import java.io.*; 
import java.net.*; 
import java.util.GregorianCalendar;

public class LifeMonitoring {

	public static void main(String[] args) {
		System.out.println("LifeMonitoring V2");
		String pingServer[] = {"heise.de","com2u.de","denic.de","cnn.com","twitter.com","9.9.9.9"} ; 
		try {
	    	PropertiesLoader prop = new PropertiesLoader();
	    	pingServer = prop.getArray("Ping.ServerList");
	    	TwitterClient twitter = new TwitterClient();
			PostgresDB postgre = new PostgresDB(prop.get("DBURL"),prop.get("DBName"),prop.get("DBUser"),prop.get("DBPasswd"));
			postgre.init();
			
	    	MQTTServer mqtt = new MQTTServer(prop.get("MQTTBrokerServer"), "LifeMonitoring", prop.get("MQTTUser"), prop.get("MQTTPassword"));
	    	try {
	    	while(true) {
	    		java.util.Vector<MQTTMessage> messages = new java.util.Vector<MQTTMessage>();
	    		java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
				
		    	for (String server : pingServer) {
		    		messages.add(new MQTTMessage(sqlDate, "Ping/"+server, sendPingRequest(server)+""));
		    	}
		    	TwitterMeasurment titterData = twitter.checkTwitter("BreakingNews");
		    	messages.add(new MQTTMessage(sqlDate, "Twitter/"+titterData.hashtag+"/Count", titterData.count+""));
		    	messages.add(new MQTTMessage(sqlDate, "Twitter/"+titterData.hashtag+"/AvgRetweet", titterData.avgRetweet+""));
		    	messages.add(new MQTTMessage(sqlDate, "Twitter/"+titterData.hashtag+"/DistanceAvg", titterData.distanceAvg+""));
		    	messages.add(new MQTTMessage(sqlDate, "Twitter/"+titterData.hashtag+"/DistanceCount", titterData.distanceCount+""));
		    	messages.add(new MQTTMessage(sqlDate, "Twitter/"+titterData.hashtag+"/MaxRetweet", titterData.maxRetweet+""));
		    	messages.add(new MQTTMessage(sqlDate, "Twitter/"+titterData.hashtag+"/TweetCount", titterData.tweetCount+""));
  	
		    	for (MQTTMessage m : messages) {
			    	mqtt.sendMessage(m.topic,m.content );
		    		postgre.updateDB(m.sqlDate, m.topic, m.content);
		    	}
	    		
		    	for (int i = 0; i < 30; i++) {
					// 10 Seconds
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.print(".");
				}
		    	}
		    	} catch (Exception e) {
		    		e.printStackTrace();
		    		postgre.init();
		    	}
	    	//public MQTTServer(String _broker, String _clientId, String MQTTUser, String MQTTPassword){
	    	
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static int sendPingRequest(String ipAddress) 
            throws UnknownHostException, IOException 
{ 
  InetAddress geek = InetAddress.getByName(ipAddress); 
  //System.out.println("Sending Ping Request to " + ipAddress); 
  long finish = 0;
  int delayTime = 50000;
  long start = new GregorianCalendar().getTimeInMillis();
  if (geek.isReachable(delayTime))  {
	  finish = new GregorianCalendar().getTimeInMillis();
	  System.out.println("Ping RTT "+ipAddress+": " + (finish - start + "ms"));  
	  delayTime = (int) (finish - start);
  }
  else {
	  System.out.println("Sorry ! We can't reach to this host");  
  }
  return delayTime;
} 
	
	
	

}
