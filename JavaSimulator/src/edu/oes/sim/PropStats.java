/*
Properties and statistics for test purpuses
Copyright (C) 2012   Joshua Clark
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 */
package edu.oes.sim;

//import java.util.logging.Logger;

//import edu.oes.sim.Message.Type;
import org.apache.log4j.Logger;


public class PropStats {
	// Properties	
	int maxMessage = 100;
	int maxEndpoint = 2;
	static boolean overideToSendToPeer = false;
	static boolean priorityNoAckQueue = true;
	boolean sendPings = false;
	int sendSuccessPercent = 100;
	static int sleeptime = 0;
	static int priorityReSendWait = 1000; //Milliseconds
	static int boundarySleepTime = 0;
	static int maxType = 4;
	static int maxRetry = 6;
	Type messageassignment = Type.ALLFROMONESEND;
	public int allFromOneSendDest = 0;
	// Statistics
	long mainLoops = 0;
	long droppedMsgs = 0;
	long successfulMsgs = 0;
	long timeToInitalize;
	long timeToCompleate;
	long msgsFromTestQueue;
	long simBrodcastMsgs;
	long simSendToOneMsgs;
	long simSendMessage;
	long simAttemptedMessages;
	static long msgsLostOffNoAck = 0;
	//Integer[] dropbytype = new Integer[Message.Type];	
	//other
	int[] dropbytype = new int[maxType];
	int[] sendbytype = new int[maxType];
	long[] retrys = new long[maxRetry];
	long accumulatedLatency;
	long globalendpttimer;
	long longestendptrun; 
	
	enum Type {
		RANDSEND,
		EVENSEND,
		ALLFROMONESEND
	}	
	static Logger log = Logger.getLogger(PropStats.class.getName());

	public PropStats() {
		log.warn("propstats inits");
	}

	public void dropRecord() {
		//switch 
	}
	// print for statistics and properties
	public void printMe() {
		log.info("propstat printme");
		String str = new String();
		str += "\nProperties: \n";
		str += "maxMessage = " + maxMessage + " \n";
		str += "maxEndpoint = " + maxEndpoint + " \n";
		str += "overrideToSendToPeer = " + overideToSendToPeer + " \n";
		str += "priorityNoAckQueue = " + priorityNoAckQueue + " \n";
		str += "sendSuccessPercent = " + sendSuccessPercent + "\n";
		str += "sleeptime = " + sleeptime + "\n";
		str += "\nStatistics: \n";
		str += "mainLoops = " + mainLoops + "\n";
		str += "successfulMsgs = " + successfulMsgs + "\n";
		str += "droppedMsgs = " + droppedMsgs + "\n";
		str += "timeToInitalize = " + timeToInitalize + "\n";
		str += "timeToCompleate = " + timeToCompleate + "\n";
		str += "timeAfterInitalize = " + (timeToCompleate - timeToInitalize) + "\n";
		str += "msgsFromTestQueue = " + msgsFromTestQueue + "\n";
		str += "simBrodcastMsgs = " + simBrodcastMsgs + "\n";
		str += "simSendToOneMsgs = " + simSendToOneMsgs + "\n";
		str += "simSendMessage = " + simSendMessage + "\n";
		str += "msgsLostOffNoAck = " + msgsLostOffNoAck + "\n";
		str += "simAttemptedMessages = " + simAttemptedMessages + "\n";
		str += "accumulatedLatency = " + accumulatedLatency + "\n";
		str += "globalendpttimer = " + globalendpttimer + "\n";


		for (int i = 0; i < dropbytype.length; i++) {
			str += "dropbytype["+i+"] = " + dropbytype[i] + "\n";
			//str += (dropbytype[i] + "\t");
		}
		str += "\n";
		for (int i = 0; i < sendbytype.length; i++) {
			str += "sendbytype["+i+"] = " + sendbytype[i] + "\n";    	
		}

		str += "retry histogram[0-"+retrys.length+"]\n";
		for(int i = 0; i < retrys.length; i++) {
			str += (i + "\t");
		}
		str += "\n";    	
		for (int i = 0; i < retrys.length; i++) {
			str += retrys[i] + "\t";
		}
		str += "\n";    				
		log.info(str);
	}
}
