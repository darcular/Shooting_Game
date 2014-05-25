package network;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Message implements Serializable {
	// six tyes: MOVE, SHOOT, HIT, ORDER, RESEND_REQ, OMIT
	// MOVE, SHOOT, HIT are only sent by peers
	// ORDER is only sent by sequencer
	// OMIT is used to mark the messages transfered before a client joined
	// both peers and sequencer could send RESEND_REQ
	// 	   if they found any message missed
	private MsgType type;

	private int index;

	private String[] content;

	// the IP address of the message sender
	private String sender;


	public Message() {
		this.type = MsgType.OMIT;
		this.index = -1;
		this.content = null;
		this.sender = "";
	}

	public Message(MsgType type, String[] content, String sender) {
		this.type = type;
		this.index = -1;
		this.content = content;
		this.sender = sender;
	}

	public Message(MsgType type, int index, String[] content, String sender) {
		this.type = type;
		this.index = index;
		this.content = content;
		this.sender = sender;
	}

	public MsgType getType() {
		return this.type;
	}

	public String[] getContent() {
		return this.content;
	}

	public String getSender() {
		return this.sender;
	}

	public int getIndex() {
		return this.index;
	}

	// public int getLocalID() {
	// 	if (this.type == MsgType.SHOOT || 
	// 		this.type == MsgType.ORDER) {
	// 		return Integer.parseInt(content[0]);
	// 	}
	// 	else {
	// 		return -1;
	// 	}
	// }

	// get a shooting message's global ID from the ORDER message
	// public String[] getShootMsgID() {
	// 	if (this.type == MsgType.ORDER) {
	// 		String[] gID = new String[2];
	// 		System.arraycopy(content, 1, gID, 0, 2);
	// 		return gID;
	// 	}
	// 	else {
	// 		return null;
	// 	}
	// }

	public String[] getHitMsgID() {
		if (this.type == MsgType.ORDER) {
			return this.content;
		}
		else {
			return null;
		}
	}

	public static byte[] serialize(Message msg) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
    	ObjectOutputStream os = new ObjectOutputStream(out);
    	os.writeObject(msg);
    	byte[] result = out.toByteArray();
    	os.close();
    	out.close();
    	return result;
	}

	public static Message deserialize(byte[] data)
		throws IOException, ClassNotFoundException {
    	ByteArrayInputStream in = new ByteArrayInputStream(data);
    	ObjectInputStream is = new ObjectInputStream(in);
    	Message result =(Message)is.readObject();
    	is.close();
    	in.close();
    	return result;
	}

	public String toString() {
		String output;
		output = "Mssage Tyep: " + type + "\n" + 
			"Index: " + Integer.toString(index) + "\n" +
			"Content: " + Arrays.toString(content) + "\n" + 
			"Sender: " + sender.toString();
		return output;
	}

}