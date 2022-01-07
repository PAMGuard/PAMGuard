/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package NMEA;

import geoMag.MagneticVariation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.math3.analysis.function.Signum;

import serialComms.jserialcomm.PJSerialComm;
import serialComms.jserialcomm.PJSerialException;
import serialComms.jserialcomm.PJSerialLineListener;
import warnings.PamWarning;
import GPS.GpsData;
import NMEA.NMEAParameters.NmeaSources;
import PamController.PamController;
import PamController.status.BaseProcessCheck;
import PamController.status.ModuleStatus;
import PamController.status.ModuleStatusManager;
import PamController.status.QuickRemedialAction;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;

/**
 * @author Doug Gillespie, Dave McLaren, Paul Redmond
 *         <p>
 *         PamProcess for NMEA data acquisition.
 * 
 */
public class AcquireNmeaData extends PamProcess implements ActionListener, ModuleStatusManager {


	private NMEAControl nmeaControl;
	boolean temp = false;
	
//	SerialPortInterface serialPortInterface;
	private PJSerialComm pjSerialComm;
	
	/** the time of the latest nmea signal */
	private long lastSigTime;

	/** timer to check for incoming signal and restart if required*/
	private Timer signalCheckTimer;


	/**
	 * Ouput data block for NMEA data strings
	 */
	private NMEADataBlock outputDatablock;

	/**
	 * ArrayList to temorarily hold data read in by the captr thread before it
	 * is read out by the main thread
	 */
	private List<StringBuffer> newStrings;

	/**
	 * Timer for reading data out of newStrings
	 */
	private Timer timer;

	/**
	 * Thread references for NMEA capture.
	 */

	private Thread activeNMEAsource;
	private volatile boolean stopActiveNMEAsource = false;
	//private SerialThread serialThread;
	private PamWarning serialPortWarning;

	private BaseProcessCheck processCheck;

	AcquireNmeaData(NMEAControl nmeaControl,
			NMEAParameters nmeaParameters) {

		super(nmeaControl, null);

		serialPortWarning = new PamWarning(nmeaControl.getUnitName(), "Serial port error", 2);

		setProcessName("NMEA data acquisition");

		this.nmeaControl = nmeaControl;

//		newStrings = new ArrayList<StringBuffer>();
		newStrings = Collections.synchronizedList(new LinkedList<StringBuffer>());

		addOutputDataBlock((outputDatablock = new NMEADataBlock(
				nmeaControl.getUnitName(), this, 1)));

		timer = new Timer(100, this);
		
		setProcessCheck(processCheck = new NMEAProcessCheck(this, .2));
		processCheck.setOutputAveraging(5);
	}

	public void makeSimThread() {
		closeSerialThread();
		timer.stop();
		activeNMEAsource = null;
		activeNMEAsource = new Thread(new SimThread());
		timer.start();
		activeNMEAsource.start();
	}

	synchronized void closeSerialThread()  {
//		if(serialPortInterface!=null){
//			if (serialPortInterface.serialPortCom != null) {
//				serialPortInterface.serialPortCom.close();
//			}
//			serialPortInterface = null;
//		}
		if (pjSerialComm != null) {
			pjSerialComm.closePort();
			pjSerialComm = null;
		}
	}

	public void makeUdpThread() {
//		if(serialPortInterface!=null){
//			if (serialPortInterface.serialPortCom != null) {
//				serialPortInterface.serialPortCom.close();
//			}
//			serialPortInterface = null;
//		}
		timer.stop();
		activeNMEAsource = null;
		activeNMEAsource = new Thread(new UdpThread());
		timer.start();
		activeNMEAsource.start();
	}
	
	public void makeMulticastThread() {
//		if(serialPortInterface!=null){
//			if (serialPortInterface.serialPortCom != null) {
//				serialPortInterface.serialPortCom.close();
//			}
//			serialPortInterface = null;
//		}
		timer.stop();
		activeNMEAsource = null;
		activeNMEAsource = new Thread(new MulticastThread());
		timer.start();
		activeNMEAsource.start();
	}

	public void makeSerialThread() {
		//System.out.println("OK making a serial thread");
		timer.stop();
		activeNMEAsource = null;
//
//		if(serialPortInterface!=null){
//
//			if(serialPortInterface.serialPortCom!=null){
//				//System.out.println("Before serialPortInterface.serialPortCom.close()");
//				//Use our local close function instead cjb
//				//serialPortInterface.serialPortCom.close();
//				closeSerialThread();
//				//System.out.println("After closeSerialThread");
//			}
//			serialPortInterface = null;
//		}
//		serialPortInterface = new SerialPortInterface();
		closeSerialThread();
		NMEAParameters params = nmeaControl.getNmeaParameters();
		try {
			pjSerialComm = PJSerialComm.openSerialPort(params.serialPortName, params.serialPortBitsPerSecond);
		} catch (PJSerialException e) {
			System.out.println(e.getMessage());
//			WarnOnce.sho
			return;
		}
		pjSerialComm.addLineListener(new SerialListener());
		timer.start();
	}
	
	private class SerialListener implements PJSerialLineListener {

		@Override
		public void newLine(String aLine) {
			processNmeaString(new StringBuffer(aLine));
		}

		@Override
		public void portClosed() {
//			Debug.out.println("NMEA serial line listener stopped");
		}

		@Override
		public void readException(Exception e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * Restart the NMEA thread using the Swing utilitiy
	 * invokeLater(...)
	 */
	private void restartNMEA() {
		System.out.println("Problem with NMEA signal - Restarting NMEA source...");
		SwingUtilities.invokeLater(new RestartNMEASource());
	}
	
	/**
	 * Restart the NMEA thread from the AWT thread in a
	 * call from invokeLater
	 * @author Doug
	 *
	 */
	private class RestartNMEASource implements Runnable {

		@Override
		public void run() {
			startNmeaSource(nmeaControl.nmeaParameters.sourceType);
		}
		
	}
	
	protected void stopNMEASource() {
		if(activeNMEAsource != null) {
//			Debug.out.println("active source = " + activeNMEAsource.getName());
//			Debug.out.println("Stopping Current GPS thread.");
			stopActiveNMEAsource=true;
			while(stopActiveNMEAsource)
				try {
					Thread.sleep(500);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		} 

		activeNMEAsource = null;
	}

	public synchronized void startNmeaSource(NmeaSources sourceType) {

		int runMode = PamController.getInstance().getRunMode();
		if (runMode != PamController.RUN_NORMAL && runMode != PamController.RUN_NETWORKRECEIVER) {
			return;
		}
		
		stopNMEASource();

		if(sourceType == NmeaSources.SERIAL){
			makeSerialThread();
		}else if(sourceType == NmeaSources.UDP){
			if (nmeaControl.nmeaParameters.multicast)
				makeMulticastThread();
			else
				makeUdpThread();
		}else{
			makeSimThread();
		}
		
		processCheck.reset();
	}




	// Start pdu packaging timer
	class SimThread implements Runnable {
		public void run() {

			//System.out.println("********GPS SIM THREAD*********");
			GpsData gpsSim = new GpsData();
			int timeOnCurrentHeading;
			Date date = new Date();
			
			AISDataSet aisData = new ChannelAISData();
			
			MagneticVariation magneticVariation = MagneticVariation.getInstance();
			double magVar;

//			SimpleDateFormat gpsDateFormat = new SimpleDateFormat("ddMMyy");
//			SimpleDateFormat gpsTimeFormat = new SimpleDateFormat("hhmmss");
			// NumberFormat.

			nmeaControl.nmeaParameters.simTimeInterval = 
				Math.max(nmeaControl.nmeaParameters.simTimeInterval, .5);
			int simTimeInterval = (int) (nmeaControl.nmeaParameters.simTimeInterval * 1000);
			boolean drunkCaptain = (nmeaControl.nmeaParameters.drunkenness != 0);
			boolean continousChange = nmeaControl.nmeaParameters.continousChange ;
			double courseChangeRate = 0;
			double courseRateRandom = nmeaControl.nmeaParameters.drunkenness;
			double courseRateDecay = nmeaControl.nmeaParameters.drunkenness / 3;
			double newCCR;
			Random random = new Random();
			double currentCourse;


			gpsSim.setLatitude(nmeaControl.nmeaParameters.simStartLatitude);
			gpsSim.setLongitude(nmeaControl.nmeaParameters.simStartLongitude);
			gpsSim.setSpeed(nmeaControl.nmeaParameters.simStartSpeed);
			gpsSim.setCourseOverGround(nmeaControl.nmeaParameters.simStartHeading);
			//gpsSim.setSpeed(0.0);
//			gpsSim.setTrueCourse(170.0);
//			gpsSim.setVariation(0.0);

			currentCourse = gpsSim.getCourseOverGround();
			double angle = Math.PI / 180 * (90 - currentCourse);
			double latStep = gpsSim.getSpeed() / 3600 * Math.sin(angle) / 60.;
			double longStep = gpsSim.getSpeed() / 3600 * Math.cos(angle)
			/ Math.cos(Math.abs(gpsSim.getLatitude()) * Math.PI / 180.)
			/ 60.;

			//System.out.println("gpsSim.getLongitude(): "  + gpsSim.getLongitude());

			long lastSimTime = PamCalendar.getTimeInMillis();
			long nowTime = 0;
			double stepTime;
			while (!stopActiveNMEAsource) {
				nowTime = PamCalendar.getTimeInMillis();
				stepTime = (nowTime - lastSimTime) / 1000.;
				if (stepTime < 0) {
					/* 
					 * this will happen if a wav file is started from 
					 * some time ago - and will cause a very big backwards step
					 * so set it to zero and should be OK or at least better. 
					 */
					stepTime = 0;
				}
//				if (Math.abs(stepTime) > 10) stepTime = 1;
				lastSimTime = nowTime;

				angle = Math.PI / 180 * (90 - currentCourse);
				latStep = gpsSim.getSpeed() / 3600 * Math.sin(angle) / 60.;
				longStep = gpsSim.getSpeed() / 3600 * Math.cos(angle)
				/ Math.cos(Math.abs(gpsSim.getLatitude()) * Math.PI / 180.)
				/ 60.;
				gpsSim.setCourseOverGround(currentCourse);
				gpsSim.setLatitude(gpsSim.getLatitude() + latStep * stepTime);
				gpsSim.setLongitude(gpsSim.getLongitude() + longStep * stepTime);

				gpsSim.setTimeInMillis(PamCalendar.getTimeInMillis());

				StringBuffer sb = new StringBuffer(gpsSim.gpsDataToRMC(nmeaControl.nmeaParameters.getLatLongDecimalPlaces()));
				newStrings.add(sb);
				sb = new StringBuffer(gpsSim.gpsDataToGGA(nmeaControl.nmeaParameters.getLatLongDecimalPlaces()));
				newStrings.add(sb);
				
				if (nmeaControl.nmeaParameters.simHeadingData == NMEAParameters.SIM_HEADING_MAGNETIC) {
					magVar = magneticVariation.getVariation(gpsSim);
					newStrings.add(createMagneticNMEAString(gpsSim.getCourseOverGround(), magVar));
				}
				else if (nmeaControl.nmeaParameters.simHeadingData == NMEAParameters.SIM_HEADING_TRUE) {
					newStrings.add(createTrueNMEAString(gpsSim.getCourseOverGround()));
				}

				if (drunkCaptain) {
					//either a random change or a continous change.
					if (!continousChange){
						//random change in direction with a specified amgnitude. 
						newCCR = courseChangeRate + random.nextGaussian() * courseRateRandom * stepTime;
						newCCR *= Math.exp(-courseRateDecay * stepTime);
						courseChangeRate = newCCR;
					}
					else {
						//continous change in heading. 
						courseChangeRate=courseRateRandom;
					}
					
					currentCourse += courseChangeRate * stepTime;
					
					//wrap to 360.
					while (currentCourse >= 360) {
						currentCourse -= 360;
					}
					while (currentCourse < 0) {
						currentCourse += 360;
					}
					
					
				}
				
				if (nmeaControl.nmeaParameters.generateAIS) {
					newStrings.add(new StringBuffer(aisData.getNext()));
				}
				
				try {
					Thread.sleep(simTimeInterval);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			stopActiveNMEAsource=false;
		}

		private StringBuffer createTrueNMEAString(double courseOverGround) {
			courseOverGround = PamUtils.constrainedAngle(courseOverGround);
			StringBuffer s = new StringBuffer(String.format("$GPHDT,%03.1f,T,", courseOverGround));
			int checkSum = createStringChecksum(s);
			s.append(String.format("*%02X", checkSum));
			return s;
		}

		private StringBuffer createMagneticNMEAString(double courseOverGround,
				double magVar) {
			courseOverGround = PamUtils.constrainedAngle(courseOverGround - magVar);
			StringBuffer s = new StringBuffer(String.format("$GPHDG,%03.1f,,,,", courseOverGround));
			int checkSum = createStringChecksum(s);
			s.append(String.format("*%02X", checkSum));
			return s;
		}
	}
	

//
//	class SerialPortInterface{
//
//		String           serialPort;
//		CommPortIdentifier portId;
//		SerialPortCom serialPortCom;
//
//
//		public SerialPortInterface(){
//			// get the default name of the serial port on this operating systems
//			serialPort = nmeaControl.nmeaParameters.serialPortName;
//			// parse ports and if the default port is found, initialized the serialPortCom
//			portId = SerialPortCom.findPortIdentifier(serialPort);
//			if (portId != null) {
//				serialPortCom = new NmeaSerialCom(serialPort, nmeaControl.nmeaParameters.serialPortBitsPerSecond, portId);
//			}
//			else {
//				System.out.println("port " + serialPort + " not found.");
//				JOptionPane.showMessageDialog(null,
//						serialPort + " cannot be found.\nPlease select correct port.",
//						"Serial port error",
//						JOptionPane.ERROR_MESSAGE);
//			} 
//		}
//
//		class NmeaSerialCom extends SerialPortCom {
//
//			public NmeaSerialCom(String portName, int baud, CommPortIdentifier portId) {
//				super(portName, baud, portId, nmeaControl.getUnitName());
//				String err = getPortError();
//				if (err == null) {
//					WarningSystem.getWarningSystem().removeWarning(serialPortWarning);
//				}
//				else {
//					serialPortWarning.setWarningMessage(err);
//					serialPortWarning.setWarningTip("<html>Check another instance of PAMGuard isn't already open.<br>"
//							+ "if you are using a USB serial adapter and have moved it to a different <br>"
//							+ "USB port, then it's COM port id may have changed. </html>");
//					WarningSystem.getWarningSystem().addWarning(serialPortWarning);
//				}
//				// TODO Auto-generated constructor stub
//			}
//
//			int errors = 0;
//			//Counter for when forcing restart every so often. cjb
//			//int GPSStrings= 0;
//			
////			int testCount = 0;
//			
//			@Override
//			public void readData(StringBuffer readData){
//				//System.out.println("In read data");
//				/**
//				 * Test to see that it can restart automatically ...
//				 */
////				if (++testCount == 50) {
////					System.out.println("Test restart of NMEA acquisition");
////					restartNMEA();
//////					makeSerialThread();
////				}
//				
//				if (readData.length()>7) {
//					if (checkStringCheckSum(readData)) {
////						newStrings.add(readData);
//						/*
//						 * This is in a different thread anyway, so no need to 
//						 * go into newStrings.
//						 */
//						processNmeaString(readData);
//						errors = 0;
//					}
//					else {
//						if (++errors > 20) {
//							System.out.println("Bad NMEA Strings : " + readData);
//							System.out.println("Restart serial thread");
//
//							restartNMEA();
//						}
//					}
//					//testing restarting thread repeatedly 
//					//if(GPSStrings>35) {
//					//	System.out.println("Restart serial thread GPSStrings>20");
//					//	GPSStrings=0;
//					//	timer.stop();
//					//	makeSerialThread(); // start again. 
//					//}
//					//
//					//GPSStrings++;
//				}
//			}
//		}
//
//	}



	class UdpThread implements Runnable {
		public void run() {
			/*
			 * Sit here reading data from the port. Every time a new NMEA string
			 * arrives, make it into a StringBuffer and add that StringBuffer to
			 * the newStrings array list. the timer in the main thread will take
			 * them out and pass them onto the rest of Pamguard.
			 */
			DatagramSocket GPSsocket = null;
			byte[] buffer = new byte[150];
			int nmeaCount = 1;
			StringBuffer nmeaStringBuffer = new StringBuffer();
			try {

				// Create Datagram Socket
				GPSsocket = new DatagramSocket(nmeaControl.nmeaParameters.port);
				GPSsocket.setSoTimeout(2000); // Allows GPS source to be changed even when no udp is being received.

				// Create empty Datagram Packet
				DatagramPacket GPSpacket = new DatagramPacket(buffer,
						buffer.length);
				// receive request from client and get client info
				while (!stopActiveNMEAsource) {
					// TODO: Validate NMEA content - how ?
					// Not much to go on, nmea max len is 82char starting with
					// '$'
					// Maybe also able to check the gps'server' ip
					// InetAddress NMEAServer = GPSpacket.getAddress();

					//System.out.println("nmeaControl port:"+ nmeaControl.nmeaParameters.port);		
					try {
						GPSsocket.receive(GPSpacket);
					} catch (SocketTimeoutException e) {
//						System.out.println("NMEA Time out");
						continue; // otherwise the same string jut gets sent again every 2s !
					}

					// TODO: Recode following line to avoid String creation (may
					// ArrayList newString can hold CharSequence?)
					StringBuffer sb = new StringBuffer(new String(buffer)
					.substring(0, GPSpacket.getLength()));
					// StringBuffer sb = new StringBuffer("Test" +nmeaCount++);
					newStrings.add(sb);
				}
			} catch (UnknownHostException e) {
				// System.out.println(e);
			} catch (IOException e) {
				// System.out.println(e);
			}
			stopActiveNMEAsource=false;
			// the socket must be closed on exiting the thread, otherwise, it can't
			// be started again. DG. 27/3/06
			if (GPSsocket != null) GPSsocket.close();
		}
	}

	class MulticastThread implements Runnable {
		public void run() {
			/*
			 * Sit here reading data from the port. Every time a new NMEA string
			 * arrives, make it into a StringBuffer and add that StringBuffer to
			 * the newStrings array list. the timer in the main thread will take
			 * them out and pass them onto the rest of Pamguard.
			 */
			MulticastSocket GPSsocket = null;
			byte[] buffer = new byte[150];
			int nmeaCount = 1;
			StringBuffer nmeaStringBuffer = new StringBuffer();
			InetAddress group = null;
			try {
				group = InetAddress.getByName(nmeaControl.nmeaParameters.multicastGroup);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {

				// Create Datagram Socket
				GPSsocket =  new MulticastSocket(nmeaControl.nmeaParameters.port);

				GPSsocket.setSoTimeout(2000); // Allows GPS source to be changed even when no udp is being received.
				GPSsocket.joinGroup(group);

				// Create empty Datagram Packet
				DatagramPacket GPSpacket = new DatagramPacket(buffer,
						buffer.length);
				// receive request from client and get client info
				while (!stopActiveNMEAsource) {
					// TODO: Validate NMEA content - how ?
					// Not much to go on, nmea max len is 82char starting with
					// '$'
					// Maybe also able to check the gps'server' ip
					// InetAddress NMEAServer = GPSpacket.getAddress();

					//System.out.println("nmeaControl port:"+ nmeaControl.nmeaParameters.port);		
					try {
						GPSsocket.receive(GPSpacket);
					} catch (SocketTimeoutException e) {
//						System.out.println("NMEA Time out");
						continue; // otherwise the same string jut gets sent again every 2s !
					}

					// TODO: Recode following line to avoid String creation (may
					// ArrayList newString can hold CharSequence?)
					StringBuffer sb = new StringBuffer(new String(buffer)
					.substring(0, GPSpacket.getLength()));
					// StringBuffer sb = new StringBuffer("Test" +nmeaCount++);
					newStrings.add(sb);
				}
				
			} catch (UnknownHostException e) {
				// System.out.println(e);
			} catch (IOException e) {
				// System.out.println(e);
			}
			stopActiveNMEAsource=false;
			// the socket must be closed on exiting the thread, otherwise, it can't
			// be started again. DG. 27/3/06

			if (GPSsocket != null) {
				try {
					GPSsocket.leaveGroup(group);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				GPSsocket.close();
			}
		}
	}

	public void actionPerformed(ActionEvent event) {
		/*
		 * As called by Timer this gets called every 100ms or so. Look in the
		 * newStrings array list to see if the acquisition thread has put
		 * anything there. If it has, take it out and process it.
		 */
		while (!newStrings.isEmpty()) {
			processNmeaString(newStrings.remove(0));
		}
	}

	/**
	 * Convert a string buffer into an NMEA data unit and output
	 * to the datablock. 
	 * @param stringBuffer
	 */
	private void processNmeaString(StringBuffer stringBuffer) {
//		checkStringCheckSum(stringBuffer);
		lastSigTime = System.currentTimeMillis();
		NMEADataUnit newUnit = new NMEADataUnit(PamCalendar.getTimeInMillis(), stringBuffer);
		outputDatablock.addPamData(newUnit);
	}
	
	/**
	 * Checks the checksum of an NMEA data string. 
	 * <p>the checksum is an exclusive OR of all characters
	 * between the $ or ! that starts the string and the *
	 * that preceeds the checksum.  
	 * @param nmeaString
	 * @return
	 */
	static boolean checkStringCheckSum(StringBuffer nmeaString) {
		byte sum = createStringChecksum(nmeaString);
		byte checkSum = getStringChecksum(nmeaString);
		return (checkSum == sum);
		
	}
	/**
	 * Calculate the correct string buffer for an NMEA sentence.
	 * <p>The checksum is an exclusive OR of all characters between, but 
	 * not including the first ($ or !) and the * preceding the checksum 
	 * @param nmeaString NMEA sentence
	 * @return checksum  value. 
	 */
	public static byte createStringChecksum(StringBuffer nmeaString) {
		char[] nmeaSentence = new char[nmeaString.length()];
		nmeaString.getChars(0, nmeaString.length(), nmeaSentence, 0);
		char[] checkSumChars = null;
		byte sum = (byte) nmeaSentence[1]; // ignore the 0'th character. 
		byte b;
		for (int i = 2; i < nmeaString.length(); i++) {
			b = (byte) nmeaSentence[i];
			if (b == '*') {
//				if (i < nmeaSentence.length - 2) {
//					//				int nChar = nmeaSentence.length - 1 - i;
//					checkSumChars = new char[2];
//					nmeaString.getChars(i+1, i+3, checkSumChars, 0);
//				}
				break;
			}
			sum ^= b;
		}		
		return sum;
	}
	
	/**
	 * Gets the checksum from the end of a string. 
	 * The is the two characters that follow the *
	 * @param nmeaString NMEA string
	 * @return Checksum value
	 */
	static public byte getStringChecksum(StringBuffer nmeaString) {
		int starPos = nmeaString.lastIndexOf("*");
		if (starPos < 0) {
			return 0;
		}
		char[] checkSumChars = new char[2];
		nmeaString.getChars(starPos+1, starPos+3, checkSumChars, 0);
		int checkSum;
		try {
			checkSum = Integer.parseInt(new String(checkSumChars), 16);
		}
		catch (NumberFormatException e) {
			return 0;
		}
		return (byte) checkSum;
	}
	

	@Override
	public void noteNewSettings() {
	}

	/*	public int getUpdPort() {
		return updPort;
	}

	public void setUpdPort(int updPort) {
		this.updPort = updPort;
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamObserver#FirstRequiredSample(PamguardMVC.PamObservable,
	 *      java.lang.Object) Don't need any of these !
	 */
	public long firstRequiredTime(PamObservable o, Object arg) {
		return -1;
	}

//	public void update(PamObservable o, PamDataUnit arg) {
//	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#PamStart() Probably dont need to use these
	 *      either - but need them to keep the interface happy
	 */
	@Override
	public void pamStart() {
		lastSigTime = System.currentTimeMillis();
		signalCheckTimer = new Timer(10000, new SignalTimerAction());
		signalCheckTimer.start();

	}

	@Override
	public void pamStop() {
		if (signalCheckTimer!=null) {
			signalCheckTimer.stop();
		}
	}
	
	/**
	 * Inner timer class.  If it has been more than 10 seconds since the last NMEA signal, restart
	 * 
	 * @author mo55
	 *
	 */
	private class SignalTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (System.currentTimeMillis() - lastSigTime > 10000) {
				restartNMEA();
			}
		}
	}



	/**
	 * @return Returns the outputDatablock.
	 */
	public NMEADataBlock getOutputDatablock() {
		return outputDatablock;
	}

	@Override
	public ModuleStatus getStatus() {
		ModuleStatus status = processCheck.getStatus();
		
		if (status.getStatus() == ModuleStatus.STATUS_OK && nmeaControl.nmeaParameters.sourceType == NmeaSources.SIMULATED) {
			status.setStatus(ModuleStatus.STATUS_WARNING);
			status.setMessage("Simulated NMEA Data");
		}
		if (status.getStatus() > ModuleStatus.STATUS_OK) {
			status.setRemedialAction(new QuickRemedialAction(nmeaControl, "Setup NMEA Input", new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					nmeaControl.showNMEADialog(nmeaControl.getGuiFrame());
				}
			}));
		}
		
		return status;
	}
	
	private class NMEAProcessCheck extends BaseProcessCheck {

		public NMEAProcessCheck(PamProcess pamProcess, double minOutputRate) {
			super(pamProcess, null, 0, minOutputRate);
			
		}

		@Override
		public ModuleStatus getIdleStatus() {
			return getRunningStatus();
		}
		
	}
}
