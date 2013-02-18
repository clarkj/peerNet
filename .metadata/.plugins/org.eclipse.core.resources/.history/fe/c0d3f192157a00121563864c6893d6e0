package edu.oes.sim;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;
//import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//import org.apache.log4j.BasicConfigurator;

public class Main {
	static Map<Integer, Endpoint> endptMap = new HashMap<Integer, Endpoint>();
	static Queue<Message> TestQueue = new LinkedList<Message>();
	public static boolean sustainrunning = false;
	// Define a static logger variable so that it references the Logger instance for this class.
	static Logger log = Logger.getLogger(Main.class.getName());	
	static Logger datalog = Logger.getLogger("DataLogging");		
	static PropStats stat = new PropStats();
	//	static Random rand = new Random(System.nanoTime());
	static Random rand = new Random(0);
	static long start;
	static long now;
	/**
	 * Simulator
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Thread.sleep(PropStats.boundarySleepTime);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		LoadInit(args);
		start = System.currentTimeMillis();
		//log4j configerator
//		BasicConfigurator.configure();

		log.trace("Top of main");
		Random random = new Random(); 
		// Create a set of endpoints and saving endpoints in map with an integer key.
		for(int i=0; i < Main.stat.maxEndpoint; i++) {
			log.trace("New Endpoint " + i);
			Endpoint endpt = new Endpoint(i);
			endptMap.put(i, endpt);
		}
		// Create a set of messages sourced from the simulator to a random destination and queue for processing
		for(int i = 0; i < stat.maxMessage; i++) {
			Message msg = new Message();
			msg.setSequenceNumber(Message.SequenceCounter ++);
			msg.setSourceNumber(Message.SourceSimulator);
			switch (stat.messageassignment) {
			case RANDSEND:
				msg.setDestNumber(Math.abs(random.nextInt())%Main.stat.maxEndpoint);
				break;
			case EVENSEND:
				msg.setDestNumber(i%Main.stat.maxEndpoint);
				break;
			case ALLFROMONESEND:
				msg.setDestNumber(stat.allFromOneSendDest);
				break;
			default:
				break;
			}
			msg.setType(Message.Type.MSG);
			TestQueue.add(msg);
		}
		log.debug("Initial Size of TestQueue (Messages to send) "+TestQueue.size());
		now = System.currentTimeMillis();
		stat.timeToInitalize = (now - start);
		// While there are messages in the queue or any endpoint has further work to do
		//	 Check for messages to process from TestQueue and process one message
		//	 Run each endpoint
		//	 Enqueue any messages returned by an endpoint
		while(!TestQueue.isEmpty() || sustainrunning) {
			stat.mainLoops++;
			Message msg;
			sustainrunning = false;
			// check for and process messages in the TestQueue
			if (!TestQueue.isEmpty()) {
				msg = TestQueue.poll();
				stat.msgsFromTestQueue++;
				log.trace("Next message out of TestQueue (to send) " + msg);
				log.trace("Total messages left in TestQueue: " + TestQueue.size());
				if (msg.DestNumber==Message.SendToAll) {
					stat.simBrodcastMsgs++;
					//each time you go through the for loop, local variable "key" is assigned to the next member of the deviceMap keySet.
//					for (Integer key: endptMap.keySet()) {
					Iterator<Map.Entry<Integer, Endpoint>> it = endptMap.entrySet().iterator();
					while(it.hasNext())
					{
						Map.Entry<Integer, Endpoint> thisEntry = it.next();
						Integer key = thisEntry.getKey();
//						Endpoint endpt = thisEntry.getValue();
						Message copy = new Message();
						copy = msg;
						copy.setDestNumber(key);
						log.debug("SendToAll loop sends " + msg);
						SendDest(copy);
					}			
				}
				else {
					stat.simSendToOneMsgs++;
					log.trace("SendToOne: " + msg);
					SendDest(msg);
				}
			}
			//loop through each endpoint in endptMap and run them.  
			//   If a message is returned, queue it for later processing by the simulator.
			//   if the endpoint still has work to do, insure the simulator will continue to run.
			Iterator<Map.Entry<Integer, Endpoint>> it = endptMap.entrySet().iterator();
			long maxendptperit=0;
			while(it.hasNext())
			{
				Map.Entry<Integer, Endpoint> thisEntry = it.next();
				Integer key = thisEntry.getKey();
				Endpoint endpt = thisEntry.getValue();
				log.trace("Endpoint to run: " + key);
				long start = System.currentTimeMillis();
				endpt.run();
				if (endpt.didwork) {
					long now = System.currentTimeMillis();
					long difference = Math.abs(now-start);
					stat.globalendpttimer += (difference);
					endpt.liferuntimer += (difference);
					if(difference > maxendptperit) {
						maxendptperit = difference;
					}
					if (endpt.liferuntimer > stat.longestendptrun) {
						stat.longestendptrun = endpt.liferuntimer;
					}
				}
				msg = endpt.getMessage();
				if (msg == null) {
					log.trace("endpoint sent no message");
				}
				else {
					log.debug("Endpoint " + key + " queued " + msg );
					TestQueue.add(msg);
				}
				// check if the endpoint has pending work
				if (!sustainrunning && endpt.worktodo) { //if pending work, require simulator to continue running
					sustainrunning = true;
				}
			}
			stat.accumulatedLatency += maxendptperit;
			try {
				if(PropStats.sleeptime > 0) {
				log.trace("sleeping " +PropStats.sleeptime + " milliseconds " + "\n");
				Thread.sleep(PropStats.sleeptime);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.trace("Exiting application.");
		now = System.currentTimeMillis();
		stat.timeToCompleate = (now - start);
		stat.printMe();
		//LogInit(args);
		AppendToFile(args);
		try {
			Thread.sleep(PropStats.boundarySleepTime);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	

	static private void SendDest(Message msg) {
		stat.simAttemptedMessages++;
		if(msg.DestNumber < 0 || msg.DestNumber >= Main.stat.maxEndpoint){
			log.warn("message destination " + msg.DestNumber + " out of range [0,"+Main.stat.maxEndpoint + "]");
		}
		if (PropStats.overideToSendToPeer) {
			// TODO write packet forwarding algarithm here
		} else {
			// if message is from simulator always send msg
			// or if message is within probabity to send, send msg
			if ((msg.SourceNumber == Message.SourceSimulator || rand.nextInt(100) <= stat.sendSuccessPercent)) { 
				stat.sendbytype[msg.type.ordinal()]++;
				int destID;
				destID = msg.DestNumber;
				Endpoint endpt;
				endpt = endptMap.get(destID);
				if(endpt == null) {
					log.error("no endpoint found for destID " + destID + " " + msg);
				}
				else {
					if (msg.reTrys < PropStats.maxRetry) {
						stat.retrys[msg.reTrys]++; 
						endpt.Receive(msg);
						if (msg.SourceNumber == Message.SourceSimulator) {
							stat.simSendMessage++;
						}
						else {
							stat.successfulMsgs++;
						}
					}
				}
			} else {
				log.debug("simulator dropped " + msg);
				stat.droppedMsgs++;
				stat.dropbytype[msg.type.ordinal()]++;
			}
		}



		//log.info("Sent message "+msg.SequenceNumber + " to " + destID);	
	}
	//TODO
	   public static void CreateInit( String[] args )
	    {
	    	Properties prop = new Properties();
	 
	    	try {
	    		String defaultFile = "config.properties";
	    		String fileName = defaultFile;
	    		if(args.length > 0) {
	    			fileName = args[0];
	    		}
	    		System.out.println("using properties filename " + fileName);
	    		//set the properties value
	    		prop.setProperty("maxEndpoint", "2");
	    		prop.setProperty("maxMessage", "1000");
	    		prop.setProperty("sendSuccessPercent","100");
	 
	    		//save properties to project root folder
	    		prop.store(new FileOutputStream(fileName), null);
	 
	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	        }
	    }	
	    public static void LoadInit( String[] args )
	    {
	    	Properties prop = new Properties();
    		String defaultFile = "config.properties";
    		String fileName = defaultFile;
	    	try {

	    		if(args.length > 0) {
	    			fileName = args[0];
	    		}
	    		System.out.println("using properties filename " + fileName);
	            //load a properties file
		    	prop.load(new FileInputStream(fileName));
	 
	               //get the property value and print it out
	            String val;
	            if ((val=prop.getProperty("maxEndpoint"))!=null) {
		            stat.maxEndpoint = Integer.parseInt(val);
	            }
	            if ((val=prop.getProperty("maxMessage"))!=null) {
		            stat.maxMessage = Integer.parseInt(val);
	            }
	            if ((val=prop.getProperty("sendSuccessPercent"))!=null) {
		            stat.sendSuccessPercent = Integer.parseInt(val);
	            }
	    	} catch (IOException ex) {
	    		log.warn("likely that fileName could not be read: "+ fileName);
	    		ex.printStackTrace();
	        }
    		System.out.println("maxEndpoint: " + stat.maxEndpoint);
            System.out.println("maxMessage: " + stat.maxMessage);
            System.out.println("sendSuccessPercent: " + stat.sendSuccessPercent);
	    }
		   public static void LogInit( String[] args ) {
				  String dataeval = stat.maxEndpoint+ ","+stat.maxMessage+","+stat.sendSuccessPercent+","
						  +(stat.timeToCompleate-stat.timeToInitalize) + "," + stat.accumulatedLatency+ ","
						  + stat.globalendpttimer
						  +"\n";
	            log.info("\n"+"~"+dataeval);	
		   }     
		public static void AppendToFile(String[] args) {
			String filePath;
			//filePath = "/Users/clarkj/Documents/workspace/tests/testdata/dataFile." + stat.maxMessage + "m"+stat.maxEndpoint+"e"+stat.sendSuccessPercent+"%"+".csv";
			filePath = "testdata/dataFile." + stat.maxMessage + "m"+stat.maxEndpoint+"e"+stat.sendSuccessPercent+"%"+".csv";
			try {
				  String dataeval = stat.maxEndpoint+ ","+stat.maxMessage+","+stat.sendSuccessPercent+","
						  +(stat.timeToCompleate-stat.timeToInitalize) + "," + stat.accumulatedLatency+ ","
						  + stat.globalendpttimer + ","+ stat.longestendptrun
						  +"\n";
				  FileOutputStream fos = new FileOutputStream(filePath, true);
				  String strContent = dataeval;
			      fos.write(strContent.getBytes());
			      fos.close();
			}
			catch(FileNotFoundException ex) {
			      System.out.println("FileNotFoundException : " + ex);
			}
		    catch(IOException ioe) {
		        System.out.println("IOException : " + ioe);
		    }
			

		}
	    
}
	   
