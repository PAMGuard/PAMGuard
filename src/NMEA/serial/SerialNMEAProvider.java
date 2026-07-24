package NMEA.serial;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import NMEA.NMEAProvider;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialogPanel;
import serialComms.jserialcomm.PJSerialComm;
import serialComms.jserialcomm.PJSerialException;
import serialComms.jserialcomm.PJSerialLineListener;

public class SerialNMEAProvider extends NMEAProvider {
	
	public static final String name = "Serial NMEA data";

	private PJSerialComm pjSerialComm;

	private Timer autoPortTimer;

	private String autoComPortName;

	private volatile long lastValidStringTime;

	public SerialNMEAProvider(NMEAControl nmeaControl) {
		super(nmeaControl);
		autoPortTimer = new Timer(5000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkAutoComPort();				
			}

		});
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PamDialogPanel getDialogPanel(Window frame) {
		return new SerialNMEAPanel(this);
	}

	@Override
	public boolean startAcquisition() {
		stopAcquisition();
		NMEAParameters params = getNmeaControl().getNmeaParameters();
		try {
			pjSerialComm = PJSerialComm.openSerialPort(getComPortName(), params.serialPortBitsPerSecond);
		} catch (PJSerialException e) {
		 getNMEAProcess().sayErrorString("PJSerialException in AcquireNMEAData" + e.getMessage());
//			checkAutoComPort(); /// just do off timer. Slower, but less manic
			return false;
		}
		pjSerialComm.addLineListener(new SerialListener());
		if (params.autoSerialPort) {
			autoPortTimer.start();
		}
		return true;
	}

	@Override
	public void stopAcquisition() {
		autoPortTimer.stop();
		if (pjSerialComm != null) {
			pjSerialComm.closePort();
			pjSerialComm = null;
		}
	}

	private String getComPortName() {
		if (getNmeaControl().getNmeaParameters().autoSerialPort == false) {
			return getNmeaControl().getNmeaParameters().serialPortName;
		}
		if (autoComPortName == null) {
			// first time around, it will get current selected port, then it will scan. 
			autoComPortName = getNmeaControl().getNmeaParameters().serialPortName;
		}
		return autoComPortName;
	}

	private class SerialListener implements PJSerialLineListener {

		@Override
		public void newLine(String aLine) {
			/*
			 * Do a couple more checks, now that we're allowing auto port detection
			 * to be sure that it's NMEA data coming through, and not some other junk. 
			 */
			// check the first character is a $
			if (aLine == null || aLine.length() == 0) {
				return;
			}
			if (aLine.startsWith("$") == false && aLine.startsWith("!") == false) {
				sayErrorString("Invalid NMEA string (no $ or !): " + aLine);
				return;
			}
			StringBuffer sb = new StringBuffer(aLine);
			boolean ok = checkStringCheckSum(sb);
			if (ok == false) {
				sayErrorString("Invalid NMEA string checksum: " + aLine);
				return;
			}
			lastValidStringTime = PamCalendar.getTimeInMillis();
			getNMEAProcess().processNmeaString(sb);
		}

		@Override
		public void portClosed() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void readException(Exception e) {
			// TODO Auto-generated method stub
			
		}
	}

	private void checkAutoComPort() {
		if (getNmeaControl().getNmeaParameters().autoSerialPort == false) {
			return;
		}
		// current com port is failing, so try a different one and see if things improve. 
		long pause = System.currentTimeMillis() - lastValidStringTime;
		if (pause > 5000) {
			String newPort = findAnotherPort();
			if (newPort != null) {
				autoComPortName = newPort;
				startAcquisition();
			}
		}
	}
	
	private String findAnotherPort() {
		String[] commPortIds = PJSerialComm.getSerialPortNames();
		if (commPortIds == null || commPortIds.length == 0) {
			return null;
		}
		// first find the index of the current port. 
		if (autoComPortName == null) {
			return commPortIds[0];
		}
		int ind = 0;
		for (int i = 0; i < commPortIds.length; i++) {
			if (commPortIds[i].equals(autoComPortName)) {
				ind = i;
				break;
			}
		}
		// go to the next one. looping back to the first if at end of list. 
		ind++;
		if (ind >= commPortIds.length) {
			ind = 0; 
		}
		return commPortIds[ind];
	}


}
