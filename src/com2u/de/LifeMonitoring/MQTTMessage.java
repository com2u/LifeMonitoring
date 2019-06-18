package com2u.de.LifeMonitoring;

public class MQTTMessage {
	
	public String topic = "";
	public String content = "";
	public java.sql.Timestamp sqlDate;
	
	public MQTTMessage(java.sql.Timestamp _timestamp, String _topic, String _content) {
		topic = _topic;
		content = _content;
		sqlDate = _timestamp;
	}

}
