import java.io.*; 
import java.net.*; 
import java.util.GregorianCalendar;

public class LifeMonitoring {

	public static void main(String[] args) {
		String pingServer[] = {"heise.de","com2u.de","denic.de","cnn.com","twitter.com","9.9.9.9"} ; 
    	
	    try {
	    	PropertiesLoader prop = new PropertiesLoader();
	    	MQTTServer mqtt = new MQTTServer(prop.get("MQTTBrokerServer"), "LifeMonitoring", prop.get("MQTTUser"), prop.get("MQTTPassword"));
	    	
	    	while(true) {
	    	for (String server : pingServer) {
	    		mqtt.sendMessage("Ping/"+server, sendPingRequest(server)+"");
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
	    	//public MQTTServer(String _broker, String _clientId, String MQTTUser, String MQTTPassword){
	    	
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
