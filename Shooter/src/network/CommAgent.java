package network;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import de.lessvoid.nifty.controls.ListBox;

public abstract class CommAgent {
	private MulticastSocket multiSocket;
	private DatagramSocket uniSocket;
	private InetAddress group;
	private final int MULTI_PORT = 10001;
	private final int UNI_PORT = 10002;
	private byte[] inBuff;
	protected ArrayList<Message> sentMsgs;
	protected Hashtable<String, ArrayList<Message>> receivedMsgs;
	public boolean isThreadWorking = true;
	private ListBox<String> origList;
	public static String key = "key";

	public abstract void moveMsgHandler(Message msg);	

	public abstract void shootMsgHandler(Message msg);

	public abstract void hitMsgHandler(Message msg);

	public abstract void orderMsgHandler(Message msg);
	
	public void closeSocket(){
		multiSocket.close();
		uniSocket.close();
		System.out.println("Socket closed");
	}
	
	public void setOrigList(ListBox<String> origList){
		this.origList = origList;
	}

	public CommAgent(InetAddress group) throws IOException {
		
		this.multiSocket = new MulticastSocket(MULTI_PORT);
		this.uniSocket = new DatagramSocket(UNI_PORT);
		this.group = group;
		this.inBuff = new byte[1024];
		this.sentMsgs = new ArrayList<Message>();
		this.receivedMsgs = new Hashtable<String, ArrayList<Message>>();
		this.multiSocket.joinGroup(this.group);
		this.listen();			
	}

	public void multicast(Message msg) {
		try {
	    	byte[] msgData = Message.serialize(msg);
	    	DatagramPacket packetOut = 
	    		new DatagramPacket(msgData, msgData.length, this.group, MULTI_PORT);
	    	multiSocket.send(packetOut);
	    	if (msg.getType() == MsgType.HIT || 
	    		msg.getType() == MsgType.ORDER) {
		    	sentMsgs.add(msg);
	    	}
		} catch (SocketException e) {
			e.printStackTrace();
			// System.out.println("Socket Error: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			// System.out.println("IO Error: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("Exception: " + e.getMessage());
		}
	}

	private void listen() {
		Thread multiUdpThread = new Thread(new MultiUdpListener());
		Thread uniUdpThread = new Thread(new UniUdpListener());
		multiUdpThread.start();
		uniUdpThread.start();
		Thread resendThread = new Thread(new ResendListener());
		resendThread.start();
	}

	// this is only for important messages which require reliable transmission,
	// in this case, only SHOOT and ORDER messages will be processed by this function,
	// to allow re-transmission if some packets lost
	private void add2Received(Message msg) {
		String sender = msg.getSender();
		String[] content = msg.getContent();
		ArrayList<Message> list = this.receivedMsgs.get(sender);
		int id = msg.getIndex();
		if (list == null) {
			// this message is the first message from a sender
			// creat new entry for this sender
			// mark the previous messages from this sender as OMIT
			list = new ArrayList<Message>();
			receivedMsgs.put(sender, list);
			for (int i = 0; i < id; i++) {
				Message omitMsg = new Message();
				list.add(omitMsg);
			}
		}
		// if there are some messages missing before this message
//		System.out.println(id);
//		System.out.println(list.size());
		if (id > list.size()) {
			for (int i = list.size(); i < id; i++) {
				// mark the missing messages as null
				list.add(null);
			}
			list.add(msg);
		}
		// if this message is the following message
		// add to received list
		else if (id == list.size()) {
			list.add(msg);
		}
		// if the message is a recevied one or missing one
		else {
			list.set(id, msg);
		}
	}

	private void sendToPeer(InetAddress receiver, Message msg) {
		try {
			byte[] msgData = Message.serialize(msg);
	    	DatagramPacket packetOut = 
	    		new DatagramPacket(msgData, msgData.length, receiver, UNI_PORT);
	    	uniSocket.send(packetOut);
	    	// System.out.println(msg.toString());
		} catch (SocketException e) {
			e.printStackTrace();
			// System.out.println("Socket Error: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			// System.out.println("IO Error: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("Exception: " + e.getMessage());
		}
	}

	// listener to UDP transportaion
	class MultiUdpListener implements Runnable {
		public void run() {
			while (isThreadWorking) {
				try {
					DatagramPacket packetIn = 
						new DatagramPacket(inBuff, inBuff.length);
					multiSocket.receive(packetIn);
					Message msg = Message.deserialize(packetIn.getData());
		    		// System.out.println(msg.toString());
		    		switch (msg.getType()) {
		    			case MOVE: {
		    				// System.out.println("received MOVE message");		    				
		    				moveMsgHandler(msg);
		    				break;
		    			}
		    			case SHOOT: {
		    				// System.out.println("received SHOOT message");
		    				// System.out.println(msg.toString());
		    				// add2Received(msg);
		    				shootMsgHandler(msg);
		    				break;
		    			}
		    			case HIT: {
		    				if(origList!=null){
		    					synchronized(origList){
		    						origList.addItem(msg.getContent()[0]+" hit "+msg.getContent()[1]+"!");
			    					origList.setFocusItemByIndex(origList.itemCount()-1);
			    					origList.refresh();
		    					}
		    				}
		    				add2Received(msg);
		    				hitMsgHandler(msg);
		    				break;
		    			}
		    			case ORDER: {
		    				// System.out.println("received an ORDER message");
		    				// System.out.println(msg.toString());
		    				add2Received(msg);
		    				orderMsgHandler(msg);
		    				break;
		    			}
		    		}
				
				} catch (SocketException e) {
//				e.printStackTrace();
					System.out.println("Socket Exception");
				// System.out.println("Socket Error: " + e.getMessage());
				} catch (IOException e) {
					 e.printStackTrace();
				// System.out.println("IO Error: " + e.getMessage());
				 } catch (Exception e) {
					 e.printStackTrace();
				// System.out.println("Exception: " + e.getMessage());
				 }	
			}
		}
	}

	class UniUdpListener implements Runnable {
		public void run() {
			while (isThreadWorking) {
				try {
					DatagramPacket packetIn = 
						new DatagramPacket(inBuff, inBuff.length);
					uniSocket.receive(packetIn);
					Message msg = Message.deserialize(packetIn.getData());
		    		// System.out.println(msg.toString());
		    		switch (msg.getType()) {
		    			// case MOVE: {
		    			// 	System.out.println("received MOVE message");		    				
		    			// 	moveMsgHandler(msg);
		    			// 	break;
		    			// }
		    			case SHOOT: {
		    				// System.out.println("received SHOOT message");
		    				// System.out.println(msg.toString());
		    				// add2Received(msg);
		    				shootMsgHandler(msg);
		    				break;
		    			}
		    			case HIT: {
		    				if(origList!=null){
		    					synchronized(origList){
		    						origList.addItem(msg.getContent()[0]+" hit "+msg.getContent()[1]+"!");
			    					origList.setFocusItemByIndex(origList.itemCount()-1);
			    					origList.refresh();
		    					}
		    				}
		    				add2Received(msg);
		    				hitMsgHandler(msg);
		    				break;
		    			}
		    			case ORDER: {
		    				// System.out.println("received an ORDER message");
		    				// System.out.println(msg.toString());
		    				add2Received(msg);
		    				orderMsgHandler(msg);
		    				break;
		    			}
		    			case RESEND_REQ: {
							System.out.println("==========================================");		    				
		    				System.out.println("Got a resend request");
		    				String[] content = msg.getContent();
		    				System.out.println("Ask for: " + Arrays.toString(content));
		    				System.out.println("Send back data: ");
							System.out.println("------------------------------------------");		    				
		    				for (String id_String : content) {
		    					int id = Integer.parseInt(id_String);
								sendToPeer(InetAddress.getByName(msg.getSender()), 
		    						sentMsgs.get(id));
								System.out.println(sentMsgs.get(id).toString());
								System.out.println("------------------------------------------");
		    				}
		    				System.out.println("==========================================");
		    			}
		    		}
				} catch (SocketException e) {
//				e.printStackTrace();
					System.out.println("Socket Exception");
				// System.out.println("Socket Error: " + e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				// System.out.println("IO Error: " + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				// System.out.println("Exception: " + e.getMessage());
				}
			}
		}
	}

	// schedually listen to the received message to find missed messages,
	// then to send RESEND_REQ
	class ResendListener implements Runnable {
		public void run() {
			while (isThreadWorking) {
				for (String sender: receivedMsgs.keySet()) {
					ArrayList<Message> msgList = receivedMsgs.get(sender);
					ArrayList<Integer> idList = new ArrayList<Integer>();
					for (int i = 0; i < msgList.size(); i++) {
						if (msgList.get(i) == null) {
							idList.add(i);
						}
					}
					if (idList.size() > 0) {
						String[] ids = new String[idList.size()];
						for (int i = 0; i < idList.size(); i++) {
							ids[i] = Integer.toString(idList.get(i));
						}
						String self = null;
						// System.out.println(Arrays.toString(ids));
						try {
							// self = InetAddress.getLocalHost().getHostAddress();
							self = Toolkit.getIpv4();
							Message resendReq = 
								new Message(MsgType.RESEND_REQ, ids, self);
							// System.out.println("==========================================");
							// System.out.println("Send a request to " + sender.toString() 
								// + " for resending msgs: " + Arrays.toString(ids));
							// System.out.println("Request Message is:");
							// System.out.println("------------------------------------------");							
							// System.out.println(resendReq.toString());
							// System.out.println("==========================================");
							// System.out.println(resendReq.toString());
							sendToPeer(InetAddress.getByName(sender), resendReq);
						} catch (UnknownHostException e) {
							e.printStackTrace();
							// System.out.println("Unknown Host: " + e.getMessage());
						} catch (Exception e) {
							e.printStackTrace();
							// System.out.println("Exception: " + e.getMessage());
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					// System.out.println("InterruptedException: " + e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					// System.out.println("Exception: " + e.getMessage());
				}
			}
		}
	}
}
