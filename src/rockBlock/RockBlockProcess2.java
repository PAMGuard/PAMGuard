package rockBlock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Timer;

import PamController.status.BaseProcessCheck;
import PamController.status.ModuleStatus;
import PamController.status.ModuleStatusManager;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;

/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import serialComms.jserialcomm.PJSerialComm;
import serialComms.jserialcomm.PJSerialLineListener;


/**
 * This version of RockBlockProcess uses the jSerialComm library instead of RXTX
 * 
 * @author mo55
 *
 */
//public class RockBlockProcess extends PamProcess implements Runnable {
public class RockBlockProcess2 extends PamProcess implements ModuleStatusManager {
	
	/** maximum number of bytes in an outgoing message */
	private static final int MAX_NUM_BYTES_OUTGOING = 340;

	/** Initiate an SBD session between the RockBlock and Iridium's Gateway Subsystem.  This
	 * command will send any messages in the RockBlock's outgoing (MO) buffer, and also
	 * download a waiting message into the RockBlock's incoming (MT) buffer.  Note that we
	 * use the SBDIXA version instead of the SBDIX version, so that any ring alerts will also
	 * automatically be cleared  
	 */
	private static final String INITIATE_SESSION = "AT+SBDIXA\r"; 
	
	/** Check the signal strength - according to the RockBlock API, a waste of time since satellites move constantly */
	private static final String CHECK_SIGNAL = "AT+CSQ\r";

	/** Turn flow control off */
	private static final String FLOWCTRL_OFF = "AT&K0\r";
	
	/** Tell the RockBlock to listen for SBD Ring Alerts */
	private static final String ENABLE_RI = "AT+SBDMTA=1\r";

	/** Tell the RockBlock to register on the network for SBD Ring Alerts */
	private static final String REGISTER_NETWORK = "AT+SBDREG\r";
	
	/** Put the ASCII text into the outgoing (MO) buffer */
	private static final String PUT_TEXT_IN_MO_BUFFER = "AT+SBDWT\r";
	
	/** Clear the outgoing (MO) buffer */
	private static final String CLEAR_MO_BUFFER = "AT+SBDD0\r";
	
	/** Clear the incoming (MT) buffer */
	private static final String CLEAR_MT_BUFFER = "AT+SBDD1\r";
	
	/** Clear both outgoing (MO) and incoming (MT) buffers */
	private static final String CLEAR_ALL_BUFFERS = "AT+SBDD2\r";
	
	/** Read text from the incoming (MT) buffer */
	private static final String GET_TEXT_FROM_MT_BUFFER = "AT+SBDRT\r";
	
	/** Constant to indicate there is no message waiting to be sent */
	private static final int NO_MESSAGE_FOUND = -1;

	/** link to control unit */
	private RockBlockControl rockBlockControl;
	
	/** communication object */
	private PJSerialComm pjSerialComm;

//	private RockBlockCom rbc;

	/** datablock containing outgoing messages */
	private RockBlockOutgoingDataBlock outgoingMessages;
	
	/** datablock containing incoming messages */
	private RockBlockDataBlock<RockBlockIncomingMessage> incomingMessages;
	
	/** flag indicating whether or not the communications object has been created */
	private boolean commReady = false;
	
	/** object containing a summary of the current RockBlock status */
	private RockBlockStatus rbStatus;

	/** flag indicating we need to wait for a response from the RockBlock */
	private boolean waitForResponse = false;
	
	/** message text to send */
	private String textToSend;

	/** RockBlock reply to command */
	private String reply;
	
	/** the index number of the message in the outgoing buffer */
	private int messageIdxInMOBuffer = NO_MESSAGE_FOUND;

	/** flag indicating that there is currently an incoming message in the RockBlock's MT buffer.  We
	 * need to retrieve the message and process it before the next session, or else it will be erased */
	private boolean incomingMessInMTBuff = false;

	/** flag indicating whether or not there are incoming messages waiting to download */
	private boolean incomingMessWaiting = false;
	
	/** timer to control communications with the Iridium system */
	private Timer commTimer;

	/** timer delay settings in milliseconds, if/when communications fails.  Note that the first index holds the
	 * time delay specified by the user in the RockBlockParams object
	 */
	private int[] timeDelays = new int[]{10000, 5000, 5000, 30000, 30000, 5*60*1000};

	/** the index in the timeDelays array */
	private int delayIdx = 0;
	
	/** boolean indicating whether we are currently trying to communicate */
	private boolean commsBusy = false;

	/** boolean indicating whether or not we need to check the signal */
	private boolean checkSignalNow = false;

	/** the signal strength of the last check, from 0-5 */
	private int signalStrength = 0;

	/** flag indicating if a ring alert has been received, and that we need to initiate a session in order
	 * to download the incoming message into the RockBlock+ */
	private boolean ringAlertReceived = false;

	/** Time that we started communication */
	private long commsBusyStart;
	
	/** Elapsed time to flag that something is wrong with communication */
	private int timeOut = 30000;
	
	/**
	 * @param rockBlockControl
	 * @param parentDataBlock
	 * @param processName
	 */
	public RockBlockProcess2(RockBlockControl rockBlockControl) {
		super(rockBlockControl, null);
		this.rockBlockControl = rockBlockControl;
		this.outgoingMessages = new RockBlockOutgoingDataBlock(RockBlockOutgoingMessage.class, "RockBlock Outgoing Messages", this, 0);
		outgoingMessages.setNaturalLifetime(600);
		outgoingMessages.SetLogging(new RockBlockOutgoingLogger(outgoingMessages));
		outgoingMessages.setShouldLog(true);
		addOutputDataBlock(outgoingMessages);
		this.incomingMessages = new RockBlockDataBlock<RockBlockIncomingMessage>(RockBlockIncomingMessage.class, "RockBlock Incoming Messages", this, 0);
		incomingMessages.setNaturalLifetime(600);
		incomingMessages.SetLogging(new RockBlockIncomingLogger(incomingMessages));
		incomingMessages.setShouldLog(true);
		addOutputDataBlock(incomingMessages);
		rbStatus = new RockBlockStatus(rockBlockControl.getParams().getPortName(), false, null, 0, 0, 0);
		createCommObject();
		setProcessCheck(new RockBlockProcessCheck(this));

	}
	

	/**
	 * Return the commReady boolean, indicating whether or not we've successfully established communications
	 * with the RockBlock
	 * 
	 * @return
	 */
	public boolean isRockBlockCommReady() {
		return commReady;
	}
	
	/**
	 * Close the current serial port object and try to create a new one
	 * @return
	 */
	public void resetRockBlock() {
		Debug.out.println("Resetting RockBlock");
		this.closeComms();
		createCommObject();
	}
	
	/**
	 * Get the RockBlock status object
	 * @return
	 */
	public RockBlockStatus getRbStatus() {
		return rbStatus;
	}

	/**
	 * Returns the signal strength of the last signal check.  Note that this does
	 * NOT initiate a new signal check - for that, we need to call checkSignal() first,
	 * wait a few seconds, and then call this.
	 * @return
	 */
	public int lastSignalStrength() {
		return signalStrength;
	}
	
	/**
	 * Tell the RockBlock+ to check the signal strength.  This does not return a value,
	 * because it can take awhile before the signal strength is returned.  To get the
	 * actual value of the signal strength, call lastSignalStrength() after calling
	 * this (and waiting a bit)
	 */
	public void checkSignal() {
		checkSignalNow = true;
		
		// adjust timer to fire immediately
		commTimer.setInitialDelay(0);
		commTimer.restart();
	}
	

	/**
	 * Create the communications object and runs the 
	 * 
	 * @return
	 */
	private void createCommObject() {
		rbStatus.setComPort(rockBlockControl.getParams().getPortName());
		commReady = false;
		RockBlockStartup startupObject = new RockBlockStartup();
		Thread startupThread  = new Thread(startupObject);
		startupThread.start();

	}
	
	
	/**
	 * Private class to establish communications with the RockBlock device.  This class creates the communication object and
	 * runs through the initialization routine in order to prepare the RockBlock for service.  If any part of the routine fails,
	 * the thread will wait 1 minute and then try again.
	 * This was initially in the createCommObject method, but was moved to it's own thread so that it didn't completely block
	 * PAMGuard from running other modules.
	 * @author mo55
	 *
	 */
	private class RockBlockStartup implements Runnable {

		@Override
		public void run() {
			rbStatus.setComPort(rockBlockControl.getParams().getPortName());
			commReady = false;
			
			while(!commReady) {
				
				// try to establish comms with the selected port.  If this fails (say, the port no longer exists or the device is not plugged in) then retry every minute until it does work.
				// It's ok to get stuck in this loop, because if we can't establish communication we can't do anything else anyway
				try {
					pjSerialComm = PJSerialComm.openSerialPort(rockBlockControl.getParams().getPortName(), rockBlockControl.getParams().getBaud());
//					commReady = true;
					pjSerialComm.addCharListener(new SerialListener());
					textToSend = null;
					timeDelays[0] = rockBlockControl.getParams().getCommTiming();
					initializeRockBlock();
				} catch (Exception e) {
					System.out.println("Exception in rockBlockProcess: " + e.getMessage());
					rbStatus.setPortError(e.getMessage());
					rbStatus.setCommReady(false);
					commReady = false;
				}
				if (!commReady) {
					System.out.println("Error establishing communication with RockBlock - retrying in 10 seconds");
					try {
						Thread.sleep(10000);	// wait 1 minute, then try again
						closeComms();
					} catch (InterruptedException ex) {
					}
				}
			}

			// comm object ready, finish initialization
//			pjSerialComm.addCharListener(new SerialListener());
//			textToSend = null;
//			timeDelays[0] = rockBlockControl.getParams().getCommTiming();
//			initializeRockBlock();
		}
		
		/**
		 * Initialise the Rockblock.  Will set the commReady flag based on whether or not the initialization process completes
		 */
		private void initializeRockBlock() {
			commReady = false;
			rbStatus.setCommReady(false);
			rbStatus.setPortError("Waiting for response...");
			Debug.out.println("Beginning RockBlock initialization");
			
			// reset the reply buffer,in case old messages are be stuck
			reply = "";

			Debug.out.println("Starting initialization routine");
			writeToPort(FLOWCTRL_OFF, false);	// make sure flow control is off (recommendation by Rock7)
			waitForResponse = true;
			int failCount = 0;
			while (waitForResponse) {
				// do nothing - wait here until runComms clears the flag
				/*
				 * but don't wait for ever because it may never initialise. 
				 * this happens if the comm port exists but there is no Rock block,
				 * or if RockBlock suddenly loses power
				 */
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				if (++failCount >= 10) {
					String errMess = "Error initializing RockBlock Flow Control";
					rbStatus.setPortError(errMess);
					return;
				}
			}
			
			writeToPort(ENABLE_RI, false);
			waitForResponse = true;
			failCount = 0;
			while (waitForResponse) {
				// do nothing - wait here until runComms clears the flag
				/*
				 * but don't wait for ever because it may never initialise. 
				 * this happens if the comm port exists but there is no Rock block,
				 * or if RockBlock suddenly loses power
				 */
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				if (++failCount >= 20) {
					String errMess = "Error initializing RockBlock Ring Alerts";
					rbStatus.setPortError(errMess);
					return;
				}
			}
			
			writeToPort(REGISTER_NETWORK, false);
			waitForResponse = true;
			failCount = 0;
			while (waitForResponse) {
				// do nothing - wait here until runComms clears the flag
				/*
				 * but don't wait for ever because it may never initialise. 
				 * this happens if the comm port exists but there is no Rock block,
				 * or if RockBlock suddenly loses power
				 */
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				if (++failCount >= 40) {	// fairly long wait - sometimes it takes awhile to register
					String errMess = "Error registering RockBlock on Iridium Network";
					rbStatus.setPortError(errMess);
					return ;
				}
			}
			
			// If we've gotten here, the RockBlock is ready to go and we can start the timer
			commTimer = new Timer(timeDelays[0], new CommTimerAction());
			commTimer.start();
			commReady = true;
			rbStatus.setCommReady(true);
			rbStatus.setPortError(null);
			
			return ;
		}
	}

	
	/**
	 * Listener class to deal with new incoming data.
	 * 
	 * @author mo55
	 *
	 */
	private class SerialListener implements PJSerialLineListener {

		@Override
		public void newLine(String aLine) {
			readData(aLine);
		}

		@Override
		public void portClosed() {
			Debug.out.println("   RockBlock serial char listener stopped");
		}

		@Override
		public void readException(Exception e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	/**
	 * Adds a new String object to the outgoing messages queue.  Check to make sure that the passed String
	 * is not too long - cannot be longer than MAX_NUM_BYTES_OUTGOING-2 (we subtract 2 so that
	 * we can later add the \r onto the end).
	 * 
	 * @param textMessage
	 */
	public void addOutgoingMessageToQueue(String textMessage) {
		// truncate if message too long
		if (textMessage.getBytes().length>MAX_NUM_BYTES_OUTGOING-2) {
			textMessage = textMessage.substring(0, (0+MAX_NUM_BYTES_OUTGOING/2)-1);
			System.out.println("Warning - outgoing RockBlock message too long.  Will be truncated to:"); 
			System.out.println(textMessage);
		}
		
		// put message into outgoing data block
		RockBlockOutgoingMessage outgoingMessage = new RockBlockOutgoingMessage(PamCalendar.getTimeInMillis(), textMessage);
		outgoingMessage.setDoNotSendBefore((long) (PamCalendar.getTimeInMillis()+rockBlockControl.getParams().getSendDelayMillis()));
		outgoingMessages.addPamData(outgoingMessage);
		rbStatus.incOutgoingCount();
		
		// adjust timer to fire immediately
		if (commTimer != null) {
			commTimer.setInitialDelay(0);
			commTimer.restart();
		}
	}
	
	/**
	 * Remove a message from the outgoing queue.  Note that this method is passed the RockBlockOutgoingMessage object
	 * to remove from the datablock, not simply the text contained in the message.  This method also removes the message
	 * from the database.
	 * 
	 * @param messageToRemove The RockBlockOutgoingMessage to remove
	 * @return true if successful, false if it failed
	 */
	public boolean removeOutgoingMessageFromQueue(RockBlockOutgoingMessage messageToRemove) {
		return outgoingMessages.remove(messageToRemove, true);
	}

	/**
	 * Add a new incoming message to the data block.
	 * 
	 * @param message
	 */
	private void addIncomingMessageToQueue(String message) {
		RockBlockIncomingMessage incomingMessage = new RockBlockIncomingMessage(PamCalendar.getTimeInMillis(), message);
		incomingMessages.addPamData(incomingMessage);
	}
	
	/**
	 * Close the communications channel
	 */
	public void closeComms() {
		if (pjSerialComm != null) {
			boolean success = pjSerialComm.closePort();
			if (success) Debug.out.println("Successfully closed com port");
			if (!success) Debug.out.println("Error - could not close com port");
			pjSerialComm = null;
		}
		
		// need to stop the timer here, or else it will keep trying to send 'Signal Check' messages to the RockBlock
		if (commTimer != null) {
			commTimer.stop();
			commTimer = null;
		}
		
		// reset the reply buffer, or else old messages get stuck
		reply = "";
	}
	
	
    @Override
	public ModuleStatus getStatus() {
    	if (rbStatus == null) {
    		return new ModuleStatus(2, "Bad Error");
    	}
    	ModuleStatus moduleStatus = new ModuleStatus(rbStatus.isCommReady() ? 0:2, rbStatus.getPortError());
		return moduleStatus;
	}
    
    /**
     * Write a message to the RockBlock output port, and reset the RockBlock
     * if the message fails (e.g. throws an I/O exception)
     * 
     * @param string
     */
    private void writeToPort(String string) {
    	writeToPort(string, true);
    }
    
    /**
     * Write a message to the RockBlock output port.  The boolean resetOnFail will control whether a thrown exception should
     * cause the RockBlock to reset (true) or not (false).  resetOnFail=false is used within the initializeRockBlock method
     * because we're still trying to establish initial communication with the device and so we don't want to throw a reset into
     * the mix.  During normal operation, though, we would like to reset the device if we've suddenly lost communication
     * 
     * @param string text message to send
     * @param resetOnFail reset the RockBlock if the write attempt throws an exception
     */
    private void writeToPort(String string, boolean resetOnFail) {
    	try {
			pjSerialComm.writeToPort(string);
		} catch (IOException e) {
			e.printStackTrace();
			rbStatus.setPortError(e.getMessage());
			
			// restart the RockBlock
			Debug.out.println("Error writing to RockBlock port");
			if (resetOnFail) {
				resetRockBlock();
			}
		}
    }

	/**
	 * Run communications.  If we're in the middle of comms, exit right away.  If
	 * we have a message to send, start the sending process (see below).  If there
	 * is a message to download, start the receiving process.  If we need to do a
	 * signal check, start that process.  If nothing else, do a signal check to
	 * make sure we still have good communication with the RockBlock
	 * 
	 * Sending Process:
	 * Tell RockBlock to load text into the MO buffer.  Note that we could append the
	 * message directly onto the PUT_TEXT_IN_BUFFER command, but that would limit the
	 * message to 120 bytes.  Instead, we send the PUT_TEXT_IN_BUFFER command by itself.
	 * The RockBlock will respond with READY, and that is picked up in the readData
	 * method which then follows up with the actual message.  This way, the message
	 * can be 340 bytes long.
	 * See https://docs.rockblock.rock7.com/reference#sbdwt for details
	 * 
	 */
	private synchronized void runComms() {
		
		// quick check to make sure we haven't lost the RockBlock somehow.  If we have, reset it
		commReady = pjSerialComm.checkPortStatus();	
		rbStatus.setCommReady(commReady);
		if (!commReady) {
			resetRockBlock();
			return;
		}
		
		// Check if we're already in the middle of communications.  If we are stalled, send a warning and return.  Otherwise, just return
		// Note that if we've lost communication with the RockBlock, we should have picked that up with the commReady check above.  So, in
		// this case, we can still communicate and are simply waiting for a reply to the last message sent.  No need to reset the RockBlock, but
		// we'll warn the user so that they can check to make sure nothing has happened (i.e. the RockBlock has lost it's view of the sky or
		// something).
		if (commsBusy) {
			if (System.currentTimeMillis() - commsBusyStart > timeOut) {
				rbStatus.setPortError("Warning - have been waiting longer than " + String.valueOf(timeOut/1000) + " seconds for RockBlock to respond");
				resetRockBlock();

				return;
			} else {
				return;
			}
		}
		
		commsBusy = true;
		commsBusyStart = System.currentTimeMillis();
		
		int messageIdxToBeSent = outgoingMessages.getFirstMessageToSend();
		
		// first we check if there is an incoming message sitting in the MT buffer.  If there is, read it right away or
		// else it might get overwritten the next time we initiate a session
		if (incomingMessInMTBuff) {
			writeToPort(GET_TEXT_FROM_MT_BUFFER);
		}
		
		// if there's no message currently in the buffer but there is a message waiting to be sent AND we don't have to
		// delay any longer in sending it, start the sending process
		else if (messageIdxInMOBuffer == NO_MESSAGE_FOUND &&
				messageIdxToBeSent!=NO_MESSAGE_FOUND &&
				outgoingMessages.getDataUnit(messageIdxToBeSent, PamDataBlock.REFERENCE_CURRENT).getDoNotSendBefore() <= PamCalendar.getTimeInMillis()) {
			textToSend = outgoingMessages.getDataUnit(messageIdxToBeSent, PamDataBlock.REFERENCE_CURRENT).getMessage() + "\r";
			messageIdxInMOBuffer = messageIdxToBeSent;	// this is premature since it takes awhile to actually get text into buffer, but don't want to accidentally overwrite while it's trying
			
			// Step 1 of the Sending Process: tell the RockBlock+ that we have a message we want to put into the MO buffer
			writeToPort(PUT_TEXT_IN_MO_BUFFER);
			Debug.out.println("Sending message: "+textToSend);
		}
		
		// if there was a ring alert or there are messages waiting to be downloaded, initiate a session
		else if (ringAlertReceived || incomingMessWaiting) {
			writeToPort(INITIATE_SESSION);
		}

		// if we're supposed to be doing a signal check, do it
		else if (checkSignalNow) {
			writeToPort(CHECK_SIGNAL);
		}
			
		// otherwise, do a signal check (because it doesn't cost us any money, and it's an easy way to make sure we can
		// still communicate with the RockBlock).  Also, a signal check will be able to detect if we've lost communication
		// with the RockBlock
		else {
			writeToPort(CHECK_SIGNAL);
//			commsBusy = false;
		}
	}
	

	/**
	 * Process the incoming message.  Any RockBlock+ messages we need to recognize and
	 * act on should be added to this method.
	 * 
	 * This is a synchronized method, along with the runComms method, so that
	 * you can't start a new comm session until you finish processing the
	 * most recent message
	 */
	private synchronized void readData(String aLine) {
		Debug.out.println("****************** Processing aLine - start *********************");
		Debug.out.println(aLine);
		Debug.out.println("****************** Processing aLine - end *********************");
		reply += aLine;

		// if we don't have the full message yet, exit now
		if ( ! (reply.endsWith("OK\r\n") || reply.endsWith("READY\r\n")) ) return;

		// debugging - print out the message surrounded by ***** to make it clear
		// (message can be confusing because of \r and \n characters in the middle
		Debug.out.println("****************** Processing reply - start *********************");
		Debug.out.println(reply);
		Debug.out.println("****************** Processing reply - end *********************");

		// Evaluate a session initiation return message.  This could be the last step of the Sending
		// Process, or simply a check for incoming messages initiated by the Timer object.
		// MUST test for this first, because it may have some characters in it that would throw one of
		// the other 'if' statements if they were before this one.
		if (reply.contains("+SBDIX:") || reply.contains("+SBDIXA:")) {
			boolean commsError = false;
			rbStatus.setPortError(null);

			// first split the line by \n so that we can just get the SBDIX status
			String[] brokenLine = (reply.split("\n"));
			String statusLine = null;
			for (int i=0; i<brokenLine.length; i++) {
				if (brokenLine[i].startsWith("+SBDIX")) {
					statusLine = brokenLine[i];
					break;
				}
			}

			// now split the SBDIX status into the individual components
			String[] status = (statusLine.substring(7)).split(",");
			Integer[] stats = new Integer[status.length];
			for (int i = 0; i<status.length; i++) {
				stats[i] = Integer.valueOf(status[i].trim());
			}

			// the third value indicates the status of the MT buffer.  Check
			// that first and set fields accordingly.  Also set the flag if
			// there are more messages waiting in the queue to download
			if (stats[2] == 1) {
				incomingMessInMTBuff = true;
				if (stats[5]>0) {
					incomingMessWaiting = true;
					rbStatus.setNumIncomingMess(stats[5]);
				} else {
					incomingMessWaiting = false;
					rbStatus.setNumIncomingMess(0);
				}
				ringAlertReceived = false;	// if there was a ring alert, clear the flag because we're already checking
			}
			else if (stats[2]==2) {
				commsError = true;
			}

			// if we were trying to send a message, check if it worked
			if (messageIdxInMOBuffer!=NO_MESSAGE_FOUND) {

				// the first value indicates the status of the MO buffer.  If we
				// successfully transmitted a message, clear the flag and
				// empty the buffer.  Set the initial timer delay to 0 s and
				// then restart it, so that the timer fires immediately
				// after we exit this in case another message is waiting to
				// be sent
				if (stats[0]<= 2) {
					Debug.out.println("Transmission successful");

					// update the database
					RockBlockOutgoingMessage theMessage = outgoingMessages.getDataUnit(messageIdxInMOBuffer, PamDataBlock.REFERENCE_CURRENT);
					theMessage.setMessageSent(true);
					outgoingMessages.updatePamData(theMessage, PamCalendar.getTimeInMillis());

					// update the flags and restart the timer
					//						this.clearOutgoingBuffer();
					rbStatus.decOutgoingCount();
					messageIdxInMOBuffer = NO_MESSAGE_FOUND;
					delayIdx=0;
					commTimer.setInitialDelay(0);
					commTimer.restart();
				}
				// Otherwise, set the error flag
				else {
					commsError = true;
				}
			}

			// if we weren't sending a message, don't do anything
			else {

			}

			// if there was a communication error (either reading or writing) then
			// alert the user and change the timing to try again
			if (commsError) {
				// throw an error
				System.out.format("Transmission failed, MO status = %d, MT status = %d%n",stats[0], stats[2]);
				rbStatus.setPortError(String.format("Transmission failed, MO status = %d, MT status = %d",stats[0], stats[2]));

				// update the database
				RockBlockOutgoingMessage theMessage = outgoingMessages.getDataUnit(messageIdxInMOBuffer, PamDataBlock.REFERENCE_CURRENT);
				theMessage.incAttempt();
				outgoingMessages.updatePamData(theMessage, PamCalendar.getTimeInMillis());

				// reset the flags and timer so that we can try again
				messageIdxInMOBuffer = NO_MESSAGE_FOUND;	// have to reset this, or else runComms will not try again
				commTimer.setInitialDelay(timeDelays[Math.min(++delayIdx,timeDelays.length-1)]);
				System.out.format("    Retrying in %d seconds%n",commTimer.getInitialDelay()/1000);
				commTimer.restart();
			} else {
				rbStatus.setPortError(null);
			}

			// clear the commsBusy flag
			commsBusy = false;
		}

		// Step 2 of the Sending Process.  We've just sent a AT+SBDWT to the RockBlock+,
		// and we receive back a READY.  That means we can now send the text that
		// we want to transmit.
		// Note that we're not clearing the commsBusy flag, so that we don't start
		// another send/receive until this one is done
		else if (reply.contains("READY") && textToSend != null) {
			Debug.out.println("RockBlock ready - sending message");
			writeToPort(textToSend);
			textToSend = null;
		}

		// Step 3 of the Sending Process.  We've just sent the actual text to the
		// RockBlock+, and it responds with a 0\r\n.  Now we can initiate a session
		// to actually try to transmit.
		// Note that we're not clearing the commsBusy flag, so that we don't start
		// another send/receive until this one is done
		else if (reply.contains("0\r\n") && messageIdxInMOBuffer!=NO_MESSAGE_FOUND) {
			Debug.out.println("message in buffer - initiating comm session");
			writeToPort(INITIATE_SESSION);
		}

		// If this was a response to a 'read MT buffer' message,
		// download an incoming message from the MT buffer
		else if (reply.contains("+SBDRT:")) {
			String[] fullMessage = reply.split("\r");
			addIncomingMessageToQueue(fullMessage[3]);
			incomingMessInMTBuff = false;

			// clear the commsBusy flag
			commsBusy = false;

			// set the initial Timer delay to 0 s and restart it, so that we do an
			// immediate check to see if a new message was added to the outgoing queue
			// while we were downloading the incoming message
			commTimer.setInitialDelay(0);
			commTimer.restart();
		}

		// If this was a response to a signal check, then process the info
		else if (reply.contains("+CSQ:")) {

			// first split the line by \n so that we can just get the CSQ value
			String[] brokenLine = (reply.split("\n"));
			String statusLine = null;
			for (int i=0; i<brokenLine.length; i++) {
				if (brokenLine[i].startsWith("+CSQ:")) {
					statusLine = brokenLine[i];
					break;
				}
			}

			// now get the signal strength value, and convert to integer
			String status = statusLine.substring(5);
			updateSignalStrength(Integer.valueOf(status.trim()));
			checkSignalNow = false;

			// clear the commsBusy flag
			commsBusy = false;

			// set the initial Timer delay to 0 s and restart it, so that we do an
			// immediate check to see if a new message was added to the outgoing queue
			// while we were checking signal strength
			commTimer.setInitialDelay(0);
			commTimer.restart();
		}

		// this is just an acknowledgement of a recognized command - don't need to do anything special
		else if (reply.endsWith("OK\r\n")) {
			waitForResponse = false;
			commsBusy = false;
		}

		// It was some other reply that's not recognised, so
		// we just ignore it and clear the commsBusy flag so that
		// the next time the Timer fires, it will be able to process properly.
		// Also clear the waitForResponse flag, so that we don't accidentally get caught in an endless loop
		else {
			Debug.out.println("message not recognized");
			waitForResponse = false;
			commsBusy = false;
		}

		// clear the reply String, get ready for a new message
		reply = "";

	}

	/**
	 * @param strength
	 */
	private void updateSignalStrength(Integer strength) {
		signalStrength = strength;
		rbStatus.setSignalStrength(signalStrength);
		Debug.out.println("Signal Strength: " + strength);
	}

	/**
	 * Clear the incoming (MT) buffer
	 */
	public void clearIncomingBuffer() {
		writeToPort(CLEAR_MT_BUFFER);
	}

	/**
	 * Clear the outgoing (MO) buffer
	 */
	public void clearOutgoingBuffer() {
		writeToPort(CLEAR_MO_BUFFER);
		messageIdxInMOBuffer = NO_MESSAGE_FOUND;
	}

	/**
	 * Clear both incoming and outgoing buffers
	 */
	public void clearBothBuffers() {
		writeToPort(CLEAR_ALL_BUFFERS);
		messageIdxInMOBuffer = NO_MESSAGE_FOUND;
	}

	/**
	 * Private timer class that performs status checks every so often
	 * 
	 * @author mo55
	 *
	 */
	private class CommTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			runComms();
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#pamStart()
	 */
	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	/**
	 * @return the outgoingMessages
	 */
	public RockBlockOutgoingDataBlock getOutgoingMessages() {
		return outgoingMessages;
	}

	/**
	 * @return the incomingMessages
	 */
	public RockBlockDataBlock<RockBlockIncomingMessage> getIncomingMessages() {
		return incomingMessages;
	}

	private class RockBlockProcessCheck extends BaseProcessCheck {

		public RockBlockProcessCheck(PamProcess pamProcess) {
			super(pamProcess, null, 0, 0);
		}

		@Override
		public ModuleStatus getStatus() {
			return RockBlockProcess2.this.getStatus();
		}
	}
}
