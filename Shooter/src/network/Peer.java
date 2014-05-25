package network;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.ArrayList;

public class Peer extends CommAgent {
	private LinkedList<Message> commitedMsgs;
	private String sequencer;
	private int currentOrderIndex;

	public Peer(LinkedList<Message> commitedMsgs, InetAddress group) throws IOException {
		super(group);
		this.commitedMsgs = commitedMsgs;
		this.currentOrderIndex = 0;
		this.sequencer = "";
	}

	public void multicast(Message msg) {
		super.multicast(msg);
	}

	public void moveMsgHandler(Message msg) {
		this.commitedMsgs.add(msg);
	}

	public void shootMsgHandler(Message msg) {
		this.commitedMsgs.add(msg);
	}

	public synchronized void hitMsgHandler(Message msg) {}

	public synchronized void orderMsgHandler(Message msg) {
		if (sequencer.equals("")) {
			this.sequencer = msg.getSender();
			Thread orderThread = new Thread(new OrderMsgListener());
			orderThread.start();
		}
	}

	class OrderMsgListener implements Runnable {
		public void run() {
			while (isThreadWorking) {
				ArrayList<Message> orderMsgs = Peer.super.receivedMsgs.get(sequencer);
				if (currentOrderIndex < orderMsgs.size()) {
					Message currentOrderMsg = orderMsgs.get(currentOrderIndex);
					if (currentOrderMsg != null) {
						if (currentOrderMsg.getType() == MsgType.OMIT) {
							currentOrderIndex++;
						}
						else {
							String[] hittMsgID = currentOrderMsg.getHitMsgID(); //optional
							String hitMsgSender = hittMsgID[0];
							int hittMsgIndex = Integer.parseInt(hittMsgID[1]);
							ArrayList<Message> hittMsgs;
							Message hittMsg;
							try {
								hittMsgs = Peer.super.receivedMsgs.get(hitMsgSender);
								hittMsg = hittMsgs.get(hittMsgIndex);
							} catch (NullPointerException e) {
								hittMsg = null;
							} catch (IndexOutOfBoundsException e) {
								hittMsg = null;
							}
							if (hittMsg != null) {
								if (hittMsg.getType() != MsgType.OMIT) {
									Peer.this.commitedMsgs.add(hittMsg);
									
								}
								currentOrderIndex++;
							}							
						}
					}				
				}
				try {
					Thread.sleep(100);
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
