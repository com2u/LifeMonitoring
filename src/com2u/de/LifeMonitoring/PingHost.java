package com2u.de.LifeMonitoring;

public class PingHost {
	public String ServerName;
	public int missed = 0;
	public int repeatCount = 0;
	public int pingTime = 0;
	public java.sql.Timestamp lastSeen = new java.sql.Timestamp(new java.util.Date().getTime());

	PingHost(String name){
		ServerName = name;
	}
}
