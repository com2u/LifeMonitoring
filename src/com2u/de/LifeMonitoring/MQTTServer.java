package com2u.de.LifeMonitoring;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTServer {
	
	//MqttClient client;
	MqttClient sampleClient;
	 MqttConnectOptions connOpts=null;
	 String broker="";
	 String clientId="";
	 int failCount=0;
	 /*
	 ActionHandler com2uActionHandler;
	 
	 
	 public void setActionHandler(ActionHandler _com2uActionHandler){
		 com2uActionHandler = _com2uActionHandler;
	 }
	 */
	
	public MQTTServer(String _broker, String _clientId, String MQTTUser, String MQTTPassword){
		broker = _broker;
		clientId = _clientId;

        
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            sampleClient = new MqttClient(broker, clientId, persistence);

            sampleClient.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable throwable) {
					failCount++;
					System.out.println("ConnectionLost -> "+ failCount);
					recoverMQTT();
					/*
					
					for (int sub = 0; sub < com2uActionHandler.settings.Subscriptions; sub++) {
						subscribe(com2uActionHandler.settings.Subscription[sub]);
						System.out.println("Subscribe :" + com2uActionHandler.settings.Subscription[sub]);
					}
					*/
					System.out.println("ConnectionLost <-");
				}

				@Override
				public void messageArrived(String topic, MqttMessage m) throws Exception {
					
					System.out.println("Topic:" + topic);
					String components[] = topic.split("/");
					//System.out.println("Topic:" + components.toString());
					String action = new String(m.getPayload());
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					System.out.println(timeStamp+" Action:" + action);
					
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken arg0) {
					// TODO Auto-generated method stub

				}
			});
            
            connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(MQTTUser);
            connOpts.setPassword(MQTTPassword.toCharArray());
            connOpts.setKeepAliveInterval(333);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            failCount=0;
			
            //sampleClient.subscribe("Com2u/Alive/#");
         
        } catch(MqttException me) {
        	failCount++;
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            System.out.println("failCount "+failCount);
            me.printStackTrace();
            recoverMQTT();
        }
		
	}
	
	public void subscribe(String subscription){
		try {
			sampleClient.subscribe(subscription);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void recoverMQTT(){
		try {
			System.out.println("Try Reconnect with delay "+(failCount*10)+" ms");
            connOpts.setCleanSession(true);
            if (failCount>3) {
            	try {
					Thread.sleep(failCount*10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            System.out.println("Connecting to broker: "+broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			failCount++;
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			failCount++;
		}
	}
	
	public void sendMessage(String topic, String content){
		try {
			int qos             = 2;
			if (content.length() < 12) {
				//System.out.println("Publish message: "+topic+" : "+content);
			} else {
				System.out.println("Publish message: "+topic+" : "+content.substring(0, 10));
			}
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(2);
            sampleClient.publish(topic, message);
            failCount=0;
            //System.out.println("Message published");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            recoverMQTT();
            me.printStackTrace();
        }
	}
	
	public void close(){
		try {
			sampleClient.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Disconnected");
        //System.exit(0);

	}
	
	
   
}
