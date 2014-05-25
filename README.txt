Shooting_Game
=============

A multi-player shooting game based on Jmonkey library. It implement total ordering algorithm via a fixed sequencer in local-network. 


Sequencer
=============
Launch the sequencer first via java VM. No other special dependencies.
$ java network.Sequencer

This game local-nework use mulitcast-multicast communication for implementing total ordering.
The network is expected to be reliable although the communication protocal provide some degree of resending mechanism(fault tolerant).

Game Client
=============
The game clients use Jmonkey as game library.
Download Jmonkey from http://hub.jmonkeyengine.org/downloads/
Add all the *.jar file at path_to_jmonkey/jmonkeyplatform/libs/ to the client project as external libraries.
Export the project as a runnable jar or you can launch it via command after compiling.
$ java game.Console

Structure:
=============

Game Client:
The game package contains the code running at 'application' layer. Console.java is the main class and is used for managing the client. GameSpace.java is the game space object used for initiating and rendering. Caster.java and MovListener.java are two threads class that used for sending and receiving messages (to/from communication layer). BombControl.java and Toolkit.java are two supporting classes.

The network package contains the conde running at 'communication' layer. Most of the classes are the same as that in Sequncer. The idea of this package is to implement total ordering algorithm and shield the communication procedure from application layer.


Sequencer:
It only has a network package which contains several java classes

MsgType.java
	An enum to define different messages types that will be used in this project. 
	There are six types of messages:
		MOVE, SHOOT, HIT, ORDER, RESEND_REQ, OMIT,
		the first three are game data that only send by the game client;
		ORDER is the message send by sequencer to provide order information;
		OMIT is used to mark the previous messages before the client join the game.

Message.java
	The class define the data transmitted in the communication.
	It contains following fields: message type, index, content and the ip address of sender.

CommAgent.java
	An abstract class to handle multicast communication, a resend mechanism has been applied for ensuring that sensitive data are delivered eventually.
	There are three inner classes in this file:
		MultiUdpListener: a class implements Runnable interface to listen the messages multicasted within the group;
		UniUdpListener: a class implements Runnable interface to listen the RESEND_REQ message and send back the corresponding data
		ResendListener: a class implements Runnable interface to listen the incoming data and send RESEND_REQ message if there is any important message missed.

Peer.java
	An subclass of CommAgent which used in the client side for handling network communication. All incoming data for the client will be handled in this class: directly submit to the game application or wait util an ORDER message is received.
	There is one inner class in this file:
		OrderMsgListener: a class implements Runnable interface to handle ORDER messages: commit the data specied in the OREDER message or wait util the correspond data is available.

Sequencer.java
	The Sequencer class is a subclass of CommAgent as well and the function of it is to listen to clients messages which should be ordered and multicast the ORDER messages to the group.

Toolkit.java
	An utility class for helping the system, including functions such as providing the correct IP address of client.

