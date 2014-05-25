package network;
import java.net.*;
import java.util.Enumeration;

public class Toolkit {
    
    public static String[] getIps(){
	String[] ips = new String[2];
	try {
            Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements() && ips[1] == null)
            {
	        NetworkInterface nf = e.nextElement();
	        if(nf.isLoopback() || nf .isPointToPoint() || nf.isVirtual() || !nf.isUp()){
                    continue;
	        }
	        Enumeration<InetAddress> ipAddresses = nf.getInetAddresses();
	        while (ipAddresses.hasMoreElements()){
	            ips[0] = ipAddresses.nextElement().getHostAddress();
	            ips[1] = ipAddresses.nextElement().getHostAddress();
	        }   
            }
	} catch (SocketException  e) {
            e.printStackTrace();
	}
        return ips;
    }

    public static String getIpv4(){
        if (getIps()[1].length()> 16)
        	return getIps()[0];
        else 
            return getIps()[1];
    }

    public static String getIpv6(){
        return getIps()[0];
    }
           
}
