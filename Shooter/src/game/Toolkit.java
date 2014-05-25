package game;

import java.net.*;
import java.util.Enumeration;

import com.jme3.math.Vector3f;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;


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
    public static void updatePlayer(String ip, String[] content, AssetManager assetManager){
        Vector3f physicLocation = parseVector3f(content[0]);
        Vector3f viewDirection = parseVector3f(content[1]);
        Vector3f walkDirection = parseVector3f(content[2]);
        if(Console.otherPlayers.containsKey(ip)){
            Character player = Console.otherPlayers.get(ip);
            player.updateMov(physicLocation, viewDirection);
            if(walkDirection.length()==0){
            	player.isWalking=false;
            }
            else{
            	player.isWalking=true;
            }
        }
        else{
            addPlayer(ip,physicLocation,viewDirection,assetManager);
        }
    }
    public static void addPlayer(String ip, Vector3f location, Vector3f direction, AssetManager aM){
        Character player = new Character((Node)aM.loadModel("Models/Oto/Oto.mesh.xml"),ip,location,direction);
        Console.otherPlayers.put(ip, player);
    }
    public static void setShooting(String ip, String[] parts){
    	Vector3f location = parseVector3f(parts[0]);
    	Vector3f direction = parseVector3f(parts[1]);
    	if(Console.otherPlayers.containsKey(ip)){
    		Character player = Console.otherPlayers.get(ip);
    		player.setShooEvent(location, direction);
    	}
    }
    
    public static Vector3f parseVector3f(String vector_str){
        String[] values = vector_str.split("[^0-9.-]+");
        float x = Float.parseFloat(values[1]);  // [0] is ''
        float y = Float.parseFloat(values[2]);
        float z = Float.parseFloat(values[3]);       
        return new Vector3f(x,y,z);
    }

           
}
