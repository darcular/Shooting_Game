package game;
import java.io.IOException;
import java.net.*;

import network.Message;
import network.MsgType;

import com.jme3.renderer.Camera;

public class Caster extends Thread{
	Character player = null;
    public Caster(Character player){
        super();
        this.player =player;
    }
    public void run(){
        while(Console.isPlaying){
            try {
              boardCastMov(player);
                this.sleep(20);
            } catch (Exception ex) {
            	if(Console.isPlaying)
            		ex.printStackTrace();
            	else
            		break;
            }
        }              
    }
    public static void boardCastMov(Character player) throws IOException{
//        String msg=new StringBuilder(Console.ip+"#"+"mov"+"#"+player.getPhysicsLocation()+"#"+player.getViewDirection()+"#"+player.getWalkDirection()).toString();
//        DatagramPacket packet_send = new DatagramPacket(msg.getBytes(), msg.length(), Console.group, 6789);
//        Console.multiS.send(packet_send);
    	String[] content = new String[3];
    	content[0]= player.getPhysicsLocation().toString();
    	content[1]= player.getViewDirection().toString();
    	content[2]= player.getWalkDirection().toString();
    	Message msg = new Message(MsgType.MOVE, content, Console.ip);
    	Console.peer.multicast(msg);
    }
    
    public void boardCastShoot(Character player, Camera cam) throws IOException {
//    	String msg=new StringBuilder(Console.ip+"#"+"shoot"+"#"+player.getControl().getPhysicsLocation()+"#"+cam.getDirection()).toString();
//    	DatagramPacket packet_send = new DatagramPacket(msg.getBytes(), msg.length(), Console.group, 6789);
//    	Console.multiS.send(packet_send);
    	String[] content = new String[2];
    	content[0]= player.getControl().getPhysicsLocation().toString();
    	content[1]= cam.getDirection().toString().toString();
    	Message msg = new Message(MsgType.SHOOT, content, Console.ip);
    	Console.peer.multicast(msg);
    }
    
    public void boardCastHit(Character player, String murder, String victims){
    	int index = player.getHitCount();
    	String[] content = new String[2];
    	content[0]=murder;
    	content[1]=victims;
    	Message msg = new Message(MsgType.HIT, index, content, Console.ip);
    	player.setHitCount(index+1);
    	Console.peer.multicast(msg);
    }
}
