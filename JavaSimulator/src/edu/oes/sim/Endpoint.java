/**
 * 
 */
package edu.oes.sim;
//import java.util.UUID;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

/**
 * @author Teamclark
 *
 */
public class Endpoint {
	int identifier;
	static int globalID = 0;
	int reSendTimer = 0;
	int prevnoackmsg = -1;
	long runtimer;
	long liferuntimer;
	boolean didwork = false;
	boolean worktodo = false;
	Queue<Message> ReceiveQueue = new LinkedList<Message>();
	Queue<Message> SendQueue = new LinkedList<Message>();
	Queue<Message> NoAckQueue = new LinkedList<Message>();
	Set<Integer> Peers = new TreeSet<Integer>();
	Comparator<Message> msgPriorityOrder = new PriorityDateComparator();
	PriorityQueue<Message> PriorityAckQueue = new PriorityQueue<Message>(11,msgPriorityOrder);


	// Define a static logger variable so that it references the Logger instance for this class.
	static Logger log = Logger.getLogger(Endpoint.class.getName());

	public Endpoint() {
		identifier = globalID++;	

	}
	public Endpoint(int id) {
		identifier = id;
		// When an endpoint is created, broadcast a ping
		if (Main.stat.sendPings) {
			Ping();
		}	
		if (PropStats.overideToSendToPeer) {
			
		}
		log.info("initialize endpoint " + id);
	}
	public void CheckReSend() { 	
		if (PropStats.priorityNoAckQueue) {
			if (!PriorityAckQueue.isEmpty() && PriorityAckQueue.peek().reSendTime <= System.currentTimeMillis()) {
				didwork = true;
				Message retryMessage = PriorityAckQueue.poll();
				retryMessage.reTrys++;
				retryMessage.reSendTime = (System.currentTimeMillis() + retryMessage.reTrySecs);
				log.trace("PriorityAckQueue is going to resend a message: " + retryMessage );
				//				log.debug("PriorityAckQueue: " + PriorityAckQueue.size() +" " + PriorityAckQueue.peek());
				if (retryMessage.reTrys < PropStats.maxRetry) {
					Send(retryMessage);
				}
				else {
					log.debug("Message retried " + PropStats.maxRetry + " times, dropping message " + retryMessage);
				}
			}
			else {
				//TODO deal with not yet
			}
		}
		//otherwise it is the NoAckQueue
		else {		
			if (NoAckQueue.isEmpty() == false) {
				didwork = true;
				switch (NoAckQueue.peek().type) {
				case MSG:
					int noackmsg = NoAckQueue.peek().SequenceNumber;
					if (noackmsg == prevnoackmsg) {
						reSendTimer++;
					}
					log.debug("Message " + NoAckQueue.peek().SequenceNumber + " is in the NoAckQueue of " + identifier +" for the "+reSendTimer+ " time");
					log.debug("There are "+ Main.TestQueue.size() + " messages in TestQueue");
					prevnoackmsg = noackmsg;
					if (reSendTimer > Main.TestQueue.size() + 3) {
						Message msg;
						msg=NoAckQueue.poll();
						msg.type=Message.Type.MSG;
						msg.SourceNumber=identifier;
						Send(msg);
						log.debug("just resent message " + msg.SequenceNumber);
						reSendTimer=0;
					}
					break;
				case PING:
					//TODO deal with pings	
					// fall through to default case
				default: 
					Message msg;
					msg=NoAckQueue.poll();
					log.debug(" let message fall off no ack queue: " + msg);
					PropStats.msgsLostOffNoAck++;
					break;
				}
				if (NoAckQueue.isEmpty()) {
					reSendTimer = 0;
				}
			}	
		}
	}
	public void Ping() {
		Message msg = new Message();
		msg.DestNumber=Message.SendToAll;
		msg.SequenceNumber = Message.SequenceCounter++;
		msg.SourceNumber = identifier;
		msg.type = Message.Type.PING;
		Send(msg);
		log.debug("Sending " + msg);
	}
	public void PeerPrint() {
		// get element in Iterator
		Iterator<Integer> it=Peers.iterator();

		// get descending order of elements
		//Iterator it=ts.descendingIterator();

		while(it.hasNext())
		{
			Integer value=(Integer)it.next();

			log.debug("Peer in list of "+identifier + " : "+value);
		}
	}
	public void SourceNumberAssgn(int idn) {
		identifier = idn;
	}
	public void CommsLayer(Message msg) {
		//	SetSend(msg);

	}

	/*	public void SetSend(Message msg) {
		// TODO Auto-generated method stub
		if (!(msg.SourceNumber == Endpoint.identifier)) {
			msg.SourceNumber = Endpoint.identifier;
		}
		else {
		}
	}
	 */	
	public Message getMessage() { //TODO check
		if(SendQueue.isEmpty()) {
			return null;
		}
		Message msg;
		msg=SendQueue.poll();
		if (msg.type == Message.Type.MSG /* || msg.type == Message.Type.PING */) {
			msg.reSendTime = System.currentTimeMillis() + PropStats.priorityReSendWait;
			addToNoAck(msg); //how do we handle multiple ping acks?
		}
		log.trace(identifier + " returning " + msg);
		return msg;
	}
	public void Receive(Message msg) {
		ReceiveQueue.add(msg);
		log.trace(identifier + " recieved message " + msg);
	}
	public int getPeersSize() {
		return Peers.size();
	}
	public void Send(Message msg) {
		//		addToNoAck(msg);
		SendQueue.add(msg);
		log.trace(identifier + " sent message " + msg);
	}
	public void run() {
		log.trace(identifier + " is running ");
		// if there's a message in the receive queue, process it
		if (!ReceiveQueue.isEmpty()) {
			didwork = true;
			Message msg;
			msg=ReceiveQueue.poll();
			msg.peersSeen.set(identifier, true);
			log.trace("Received " + msg);
			if (msg.DestNumber != identifier) { // TODO Need to deal with forwarding messages
				log.trace(identifier + " recieved message intended for different destination: "+ msg);
				NotForMeSend(msg);
			}
			else {
				switch (msg.type) {
				case MSG:
					if (msg.SourceNumber==Message.SourceSimulator) { //if a message from simulator, stamp as sender and forward message to a random endpoint.
						msg.setSourceNumber(identifier);
						msg.setDestNumber((identifier + 1)%Main.stat.maxEndpoint);
						msg.peersSeen.clear();
						Send(msg);
					}
					else if (msg.SourceNumber != identifier) { //if a message not from simulator (from endpoint) acknowledge it.
						msg.swapSourceDest();
						msg.setType(Message.Type.MSGACK);
						Send(msg);
						log.trace(identifier + "acknowledged " + msg);								
					}
					else {
						log.warn("Received message from myself? dropping " + msg);
					}

					break;
				case MSGACK:
					rmAck(msg);
					break;
				case PING:
				{
					Integer peerID = msg.SourceNumber;
					Peers.add(peerID);
					msg.swapSourceDest();
					msg.setType(Message.Type.PINGACK);
					Send(msg);
				}
				break;
				case PINGACK:
				{
					Peers.add(msg.SourceNumber);
					PeerPrint();
					rmAck(msg);
				}
				break;
				default:
					log.error("No message type? " + msg);
					break;

				}
			}
		}
		CheckReSend();	
		// TODO May need more conditions (also consider dealing with enquing multaple messages)
		if (PropStats.priorityNoAckQueue) {
			if (!PriorityAckQueue.isEmpty()) {
				worktodo = true;
				log.trace("Length of PriorityAckQueue: " + PriorityAckQueue.size() + " peek " + PriorityAckQueue.peek());
			}
			else {
				log.trace("NoAckPrioirtyQueue is empty: " + PriorityAckQueue.isEmpty());
				worktodo = false;
			}
		}
		else {
			if (!NoAckQueue.isEmpty()) {
				worktodo = true;
				log.trace("Length of NoAckQueue: " + NoAckQueue.size() + NoAckQueue.peek());
			}
			else {
				log.trace("NoAckQueue is empty: " + NoAckQueue.isEmpty());
				worktodo = false;
			}
		}	
	}
	private void NotForMeSend(Message msg) {
		if (PropStats.overideToSendToPeer) {
			//TODO write forwarding here 
			//TODO
			//TODO 
			//TODO
		}
		else{
			int nextDest;
			//Scan bitvector from own identifer
			try {
				nextDest = msg.peersSeen.nextClearBit(identifier);
			}
			catch(Exception e) {
				log.warn("Message seen everyone??"+ e.getMessage() + msg.peersSeen.toString() + " " + identifier + " " + msg);
				nextDest = -1;
			}
			//retry scan from 0 if no free spots found
			if (nextDest < 0 || nextDest >= Main.stat.maxEndpoint) {
				try {
					nextDest = msg.peersSeen.nextClearBit(0);
				}
				catch(Exception e) {
					log.warn("Message now gone through try" + msg.peersSeen.toString() + " " + identifier + " " + e.getMessage() + msg);
				}	

			}
			//check for valid endpoint and send
			if (nextDest >= 0 && nextDest < Main.stat.maxEndpoint) {
				msg.setDestNumber(nextDest);
				msg.setSourceNumber(identifier);
				Send(msg);
			}
			else {
				log.warn("Message destination " + nextDest + " does not exsist? not between 0 and " + Main.stat.maxEndpoint + " dropping message " + msg);
			}
		}
	}
	public void addToNoAck(Message msg) {
		if (PropStats.priorityNoAckQueue) {
			PriorityAckQueue.add(msg);
		}
		else {
			NoAckQueue.add(msg);

		}
	}
	void rmAck(Message msg) {
		/* TODO if you override a comparitor for message class, may be able to remove with this simple meathod

		if (NoAckQueue.remove(msg)) {

			log.info("Message " + msg.getSequenceNumber() +" has been confirmed" );
		}
		 */
		if (PropStats.priorityNoAckQueue) {
			Iterator<Message> it = PriorityAckQueue.iterator();
			while (it.hasNext()) {
				Message loopmsg = (Message)it.next();
				if (loopmsg.SequenceNumber == msg.SequenceNumber) {
					log.trace("Found and removied from PriorityAckQueue sequence: " + msg.SequenceNumber);
					it.remove();
					break;
				}	
			}
		}
		else {
			Iterator<Message> it = NoAckQueue.iterator();
			while (it.hasNext()) {
				Message loopmsg = (Message)it.next();
				if (loopmsg.SequenceNumber == msg.SequenceNumber) {
					log.trace("Found and removied from NoAckQueue sequence: " + msg.SequenceNumber);
					it.remove();
					break;
				}	
			}
		}
	}
}
