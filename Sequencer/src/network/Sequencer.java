package network;
import java.io.IOException;
import java.net.*;

public class Sequencer extends CommAgent {
	private int totalIndex;

	public Sequencer(InetAddress group) throws IOException {
		super(group);
		this.totalIndex = 0;
	}

	public void moveMsgHandler(Message msg) {}

	public void shootMsgHandler(Message msg) {}

	public synchronized void hitMsgHandler(Message msg) {
		try {
			if (!this.isProcessed(msg)) {
				String[] content = new String[2];
				// content[0] = Integer.toString(this.totalIndex);
				content[0] = msg.getSender();    //optional
				content[1] = Integer.toString(msg.getIndex());
				// String self = InetAddress.getLocalHost().getHostAddress();
				String self = Toolkit.getIpv4();
				Message order = new Message(MsgType.ORDER, this.totalIndex, content, self);
				System.out.println("sent ORDER msg " + totalIndex);
				this.totalIndex++;
				super.multicast(order);
			}
		} 
		// catch (UnknownHostException e) {
		// 	e.printStackTrace();
		// 	// System.out.println("Unknown Host: " + e.getMessage());
		// } 
		catch (Exception e) {
			e.printStackTrace();
			// System.out.println("Exception: " + e.getMessage());
		}
	}

	public void orderMsgHandler(Message msg) {}

	private boolean isProcessed(Message msg) {
		String sender = msg.getSender();
		String id = Integer.toString(msg.getIndex());
		for (Message sent : super.sentMsgs) {
			String[] info = sent.getHitMsgID();
			if(sender.equals(info[0]) && id.equals(info[1])) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		try {
			String ip = "228.5.6.7";
			if(args.length>0)
				ip=args[0];
			InetAddress group = InetAddress.getByName(ip);
			Sequencer seq = new Sequencer(group);
			System.out.println("Sequencer has been launched");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			// System.out.println("Unknown Host: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println("Exception: " + e.getMessage());
		}
	}

}
