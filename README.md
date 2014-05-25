Shooting_Game
=============

A multi-player shooting game based on Jmonkey library. It implement total ordering algorithm via a fixed sequencer. 


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
Add all the *.jar file at path_to_jmonkey/jmonkeyplatform/libs/ to the client project as external libraries
Export the project as a runnable jar or you can launch it via command after compiling.
$ java game.Console

Structure:
The game package contains the code running at 'application' layer. Console.java is the main class and is used for managing the client. GameSpace.java is the game space object used for initiating and rendering. Caster.java and MovListener.java are two threads class that used for sending and receiving messages (to/from communication layer). BombControl.java and Toolkit.java are two supporting classes.

The network package contains the conde running at 'communication' layer. Most of the classes are the same as that in Sequncer. This package is used to implement total ordering algorithm and shield the communication procedure from application layer.

