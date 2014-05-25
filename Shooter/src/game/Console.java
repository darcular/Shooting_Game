package game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import network.Message;
import network.Peer;


public class Console{
	//game state
    public static boolean isPlaying = false;
    //network
    public static String ip;
    public static InetAddress group;
    public static Peer peer;
    public static LinkedList<Message> messageQueue;   
    //game back stage
    public static Map<String,Character> otherPlayers; 
    public static MovingListener movListener;
    public static Caster caster;
    public static String player_name;
    
    
    
    public static void main(String[] args){
        //setting socket
    	ip = Toolkit.getIpv4();
    	if(args.length>0)
    		player_name = args[0];
    	String group_str = "228.5.6.7";
    	if(args.length>0)
    		group_str = args[0];
    	init_communication_layer(group_str);
//        initMultiCastSocket("228.5.6.7",6789);      
        //init maps
        otherPlayers = new HashMap<String, Character>();       
        //start game
        GameSpace game = new GameSpace();
        AppSettings appSettings = new AppSettings(true);
        appSettings.setTitle("Shooter");
        appSettings.setFrameRate(60);
//        appSettings.setFullscreen(true);
        appSettings.setVSync(true);
        appSettings.setSamples(2);
        appSettings.setResolution(800, 600);
        game.setSettings(appSettings);
        game.setPauseOnLostFocus(false);
        game.setDisplayStatView(false);
        game.setDisplayFps(false);
        game.setShowSettings(false);
        isPlaying=true;
        Logger.getLogger("").setLevel(Level.SEVERE);
        game.start();

    }
    
    public static void init_communication_layer(String group_str){
    	try{
    		messageQueue = new LinkedList<Message>();
    		group = InetAddress.getByName(group_str);
        	peer = new Peer(messageQueue, group);
    	}catch(IOException e){
    		e.printStackTrace();
    		System.exit(0);
    	}
    }
//  public static void initMultiCastSocket(String ip, int port){
//  try{
//      group = InetAddress.getByName(ip);
//      multiS = new MulticastSocket (port);
//      multiS.joinGroup(group);
//  } catch(IOException e){
//      e.printStackTrace();
//      System.exit(0);
//  }
//}
}