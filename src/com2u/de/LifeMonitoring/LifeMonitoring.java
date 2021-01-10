package com2u.de.LifeMonitoring;

import java.io.*;
import java.net.*;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class LifeMonitoring {
	static PropertiesLoader prop;
	static int PingWaringTimeout = 12000;
	static int errorCount = 0;

	public static void main(String[] args) {
		System.out.println("LifeMonitoring V7");
		String pingServer[] = { "heise.de", "com2u.de", "denic.de", "cnn.com", "twitter.com", "9.9.9.9" };
		java.util.Vector<PingHost> hosts = new java.util.Vector<PingHost>();
		try {
			prop = new PropertiesLoader();
			pingServer = prop.getArray("Ping.ServerList");
			TwitterClient twitter = new TwitterClient();
			PostgresDB postgre = new PostgresDB(prop.get("DBURL"), prop.get("DBName"), prop.get("DBUser"),
					prop.get("DBPasswd"));
			pingServer = prop.get("PingServerList").split(",");
			for (String s : pingServer) {
				s = s.replaceAll(",", "");
				PingHost host = new PingHost(s);
				hosts.add(host);
			}
			postgre.init();
			// java.sql.Timestamp sqlDate1 = new java.sql.Timestamp(new
			// java.util.Date().getTime());
			// postgre.updateImage(sqlDate1);
			// System.out.println("Image Updated");
			MQTTServer mqtt = new MQTTServer(prop.get("MQTTBrokerServer"),
					"LifeMonitoring" + (int) (java.lang.Math.random() * 100), prop.get("MQTTUser"),
					prop.get("MQTTPassword"));
			
			HTMLReader html = new HTMLReader();
			int loopCount = 0;
			while (true) {
				try {
					java.util.Vector<MQTTMessage> messages = new java.util.Vector<MQTTMessage>();
					if (loopCount % 12 == 0) {
						html.ParseHTMLPages(messages);
					}
					java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
					PingServers(hosts, messages, sqlDate);
					if (new Integer(prop.get("LifeMonitorTwitterActive")) > 0) {
						TwitterMeasurment titterData = twitter.checkTwitter("BreakingNews");
						messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/Count",
								titterData.count + ""));
						messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/AvgRetweet",
								titterData.avgRetweet + ""));
						messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/DistanceAvg",
								titterData.distanceAvg + ""));
						messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/DistanceCount",
								titterData.distanceCount + ""));
						messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/MaxRetweet",
								titterData.maxRetweet + ""));
						messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/TweetCount",
								titterData.tweetCount + ""));
						if (titterData.topTweet.length() > 10) {
							messages.add(new MQTTMessage(sqlDate, "Twitter/" + titterData.hashtag + "/TopTweet",
									titterData.topTweet));

						}
					}
					for (MQTTMessage m : messages) {
						mqtt.sendMessage(m.topic, m.content);
						postgre.updateDB(m.sqlDate, m.topic, m.content);
					}

					for (int i = 0; i < 30; i++) {
						// 10 Seconds
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							System.out.println("Exception in LifeMonitoring while sleep "+e.toString());
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.print(".");
					}

				} catch (Exception e) {
					System.out.println("Exception in LifeMonitoring while loop "+e.toString());
					e.printStackTrace();
					if (errorCount % 60 == 0) {
						SendTimeoutMail("LifeMonitoring Loop Exception "+errorCount , 	"LifeMonitoring Loop Exception " + e.getMessage());
						
					}
					postgre.init();
					twitter = new TwitterClient();
					mqtt = new MQTTServer(prop.get("MQTTBrokerServer"),
							"LifeMonitoring" + (int) (java.lang.Math.random() * 100), prop.get("MQTTUser"),
							prop.get("MQTTPassword"));
					errorCount++;
				}
				loopCount++;
			}
			// public MQTTServer(String _broker, String _clientId, String
			// MQTTUser, String MQTTPassword){

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Exception in LifeMonitoring Main thread "+e.toString());
			e.printStackTrace();
			SendTimeoutMail("LifeMonitoring Main Exception "+errorCount ,
					"LifeMonitoring Main Exception " + e.getMessage());
			
		}
		System.out.println("LifeMonitoring Terminated !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	private static void PingServers(java.util.Vector<PingHost> hosts, java.util.Vector<MQTTMessage> messages,
			java.sql.Timestamp sqlDate) {
		try {
			int newMail = 0;
			String ServerList = "";
			for (int index = 0; index < hosts.size(); index++) {
				PingHost pHost = hosts.elementAt(index);
				pHost.pingTime = sendPingRequest(pHost.ServerName);
				messages.add(new MQTTMessage(sqlDate, "Ping/" + pHost.ServerName, pHost.pingTime + ""));
				if (pHost.pingTime > PingWaringTimeout) {
					pHost.missed++;
					if (pHost.repeatCount == 2 || pHost.repeatCount > 288) {
						if (new Integer(prop.get("LifeMonitorEMailForPingTimeout")) > 0) {
							pHost.repeatCount = 2;
							ServerList += pHost.ServerName + " Timeout " + pHost.pingTime + " missing: " + pHost.missed
									+ ", ";
							newMail++;
						}
					}
					pHost.repeatCount++;
				} else {
					pHost.lastSeen = new java.sql.Timestamp(new java.util.Date().getTime());
				}
			}
			if (newMail > 0) {
				SendTimeoutMail("LifeMonitoring Ping Timeout " + ServerList,
						"LifeMonitoring Ping Timeout " + ServerList);
			}
		} catch (Exception e) {
			System.out.println("LifeMonitor Ping Exception");
			e.printStackTrace();
		}
	}

	public static void SendTimeoutMail(String _emailSubject, String _emailBody) {
		try {
			JavaMail javaEmail = new JavaMail();
			String emailHost = prop.get("EMailHost");
			String fromUser = prop.get("EMailFromUser");// just the id alone
														// without @gmail.com
			String fromUserEmailPassword = prop.get("EMailPassword");
			String emailPort = prop.get("EMailPort");// gmail's smtp port
			String[] toEmails = prop.get("EMailtoEmails").split(",");
			for (String mail : toEmails) {
				mail = mail.replaceAll(",", "");
			}
			String emailSubject = _emailSubject;
			String emailBody = _emailBody;
			javaEmail.setMailServerProperties(emailHost, fromUser, fromUserEmailPassword, emailPort);
			javaEmail.createEmailMessage(toEmails, emailSubject, emailBody);
			javaEmail.sendEmail();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static int sendPingRequest(String ipAddress) throws UnknownHostException, IOException {
		try {
			InetAddress geek = InetAddress.getByName(ipAddress);
			// System.out.println("Sending Ping Request to " + ipAddress);
			long finish = 0;
			int delayTime = 50000;
			long start = new GregorianCalendar().getTimeInMillis();
			if (geek.isReachable(delayTime)) {
				finish = new GregorianCalendar().getTimeInMillis();
				System.out.println("Ping RTT " + ipAddress + ": " + (finish - start + "ms"));
				delayTime = (int) (finish - start);
			} else {
				System.out.println("Sorry ! We can't reach to this host: " + ipAddress);
			}
			return delayTime;
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException for " + ipAddress);
			return 99999;

		} catch (IOException e) {
			System.out.println("IOException for " + ipAddress);
			return 99999;
		} catch (Exception e) {
			e.printStackTrace();
			return 99999;
		}
	}

}
