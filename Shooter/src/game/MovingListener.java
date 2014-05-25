/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package game;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import de.lessvoid.nifty.controls.ListBox;

import java.net.*;
import java.io.IOException;

import network.Message;
import network.MsgType;
/**
 *
 * @author darcular
 */
public class MovingListener extends Thread{
    private AssetManager assetManager;
    private ListBox<String> myList;
    public MovingListener(AssetManager assetManager, ListBox<String> myList){
        super();
        this.assetManager=assetManager;
        this.myList=myList;
    }

    public void run(){
        while(Console.isPlaying){
//            byte[] buf = new byte[1000];
//            DatagramPacket recev = new DatagramPacket(buf, buf.length);
//            try {
////            	Console.multiS.receive(recev);
//            } catch (IOException e) {
//            	if(Console.isPlaying)
//            	e.printStackTrace();
//            	else
//            	{
//            		System.out.println("Socket closed");
//            		break;
//            	}            		
//            }
//            String[] parts = new String(recev.getData()).split("#");
        	if(!Console.messageQueue.isEmpty())
        	{
        		Message msg = Console.messageQueue.poll();
        		String ip =msg.getSender();
        		String[] content = msg.getContent();
        		MsgType type = msg.getType();
        		if(type==MsgType.MOVE && !ip.equals(Console.ip))
        			Toolkit.updatePlayer(ip,content,assetManager);
        		else if(type==MsgType.SHOOT && !ip.equals(Console.ip)){
        			Toolkit.setShooting(ip,content);
        		}
        		else if(type==MsgType.HIT){
        			System.out.println(content[0]+" hit "+content[1]);
        			myList.addItem(content[0]+" hit "+content[1]+"!");
        			myList.setFocusItemByIndex(myList.itemCount()-1);
        			myList.refresh();
        		}
        	}
//        	else{
//        		try {
//					this.wait();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        	}       		
        }
    }    
}
