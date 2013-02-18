/**
 * 
 */
package edu.oes.sim;

import java.util.BitSet;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author Teamclark
 *
 */
public class Message {
	int SequenceNumber;
	int SourceNumber;
	int DestNumber;
	Date inceptDate = new Date();
	int reTrys = 0;
	int reTrySecs = reTrys * PropStats.priorityReSendWait;
	long reSendTime = System.currentTimeMillis() + reTrySecs;
	public BitSet peersSeen = new BitSet(Main.stat.maxEndpoint);

	static final int SendToAll=-1;
	static final int SourceSimulator=-1;
	static int SequenceCounter=0;
	// Define a static logger variable so that it references the Logger instance for this class.
	static Logger log = Logger.getLogger(Message.class.getName());

	Type type;
	enum Type {
		MSG,
		MSGACK,
		PING,
		PINGACK,
		//TODO List and list request are yet to be implemented for packet forwarding.
		LISTREQUEST,
		LISTACK

	}
	/**
	 * 
	 */
	public Message() {
		log.trace("construting message" + this);
	}

	public void setSequenceNumber(int seq) {
		SequenceNumber = seq;
	}
	public int getSequenceNumber() {
		return SequenceNumber;
	}
	public void setDestNumber(int dest) {
		DestNumber = dest;
	}
	public int getDestNumber() {
		return DestNumber;
	}
	public void setSourceNumber(int src) {
		SourceNumber = src;
	}
	public int getSourceNumber() {
		return SourceNumber;
	}
	public void setType(Type atype) {
		type = atype;
	}
	public Type getType() {
		return type;
	}
	public void swapSourceDest() {
		int temp;
		temp = SourceNumber;
		SourceNumber = DestNumber;
		DestNumber = temp;
	}
	@Override
	public String toString() {
		return "message " + type + " Sequence " + SequenceNumber + " Source " + SourceNumber + " Destination " + DestNumber + " reSendTime " + reSendTime + " currently " + System.currentTimeMillis() + " retry count " + reTrys;
	}
}
