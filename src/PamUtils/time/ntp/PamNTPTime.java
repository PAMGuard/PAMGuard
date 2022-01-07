package PamUtils.time.ntp;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Timer;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.time.GlobalTimeManager;
import PamUtils.time.PCTimeCorrector;
import PamUtils.time.TimeCorrection;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Class for measuring the time difference between the PC clock and an NTP server. 
 * Largely copied from 
 * http://www.avajava.com/tutorials/lessons/how-do-i-query-the-nist-internet-time-service-using-the-network-time-protocol.html
 *
 * @author Doug Gillespie
 *
 */
public class PamNTPTime implements PCTimeCorrector, PamSettings {

	private static volatile PamNTPTime singleInstance = null;

	private volatile long lastestOffset;

	private volatile long lastGoodPCTime;

	private volatile long lastGoodNTPTime;

	private volatile long lastPCClockTime;

	private volatile InetAddress inetAddress;

	private volatile NTPUDPClient timeClient;

//	private Timer ntpTimer;

	private Timer pcClockTimer;

	private static final int PC_CLOCK_CHECK_INTERVAL = 1000;
	
	private NTPTimeParameters ntpParameters = new NTPTimeParameters();
	
	private PamWarning ntpWarning = new PamWarning("PAMGuard NTP Time", "", 0);
	
	private volatile Thread ntpThread;
	
	private volatile boolean keepRunning = false;

	private GlobalTimeManager globalTimeManager;
	
	/**
	 * Private constructor
	 */
	public PamNTPTime(GlobalTimeManager globalTimeManager) {
		this.globalTimeManager = globalTimeManager;
		PamSettingManager.getInstance().registerSettings(this);
		
		pcClockTimer = new Timer(PC_CLOCK_CHECK_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkPCClock();
			}
		});
	}

	private class NTPThread implements Runnable {

		@Override
		public void run() {
			int failCount = 0;
			int okCount = 0;
			int sleepTime;
			while (keepRunning) {
				boolean ok = false;
				try {
					ok = checkNTPOffset();
				}
				catch (Exception e) {
					ok = false;
				}
				if (ok) {
					failCount = 0;
					okCount++;
				}
				else {
					failCount++;
				}
				if (okCount < 2) {
					/**
					 * Try to make the first couple of calls only 5s apart. Also if it's failed
					 * on the last attempt
					 */
					sleepTime = 5000;
				}
				else if (failCount > 0 && failCount < 3) {
					sleepTime = 5000;
				}
				else {
					sleepTime = ntpParameters.intervalSeconds*1000;
				}
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
	/**
	 * Check the PC clock hasn't jumped. This gets called off a timer once per second, so should always show
	 * a time difference of about 1000 millis. 
	 */
	protected void checkPCClock() {
		long now = System.currentTimeMillis();
		if (lastPCClockTime != 0) {
			if (Math.abs(now-lastPCClockTime-PC_CLOCK_CHECK_INTERVAL) > 1000) {
				/*
				 * this will happen if the PC time gets updated by the Windows own NTP server. If this is the case, then immediately
				 * poll the NTP server to try to get an updated time offset. 
				 */
				System.out.printf("System clock has shifted by %d milliseconds at %s\n", now-lastPCClockTime-PC_CLOCK_CHECK_INTERVAL, PamCalendar.formatDateTime(now));
//				checkNTPOffset();
			}
		}

//		lastPCClockTime = System.cu;
	}

	/**
	 * Check the offset between server time and the PC clock. 
	 */
	private synchronized boolean checkNTPOffset() {
		TimeInfo timeInfo;
		try {
			if (inetAddress == null) {
				inetAddress = InetAddress.getByName(ntpParameters.serverName);
			}
			
			long now1 = System.currentTimeMillis();
			timeClient.setDefaultTimeout(400);
			timeInfo = timeClient.getTime(inetAddress);
			long now2 = System.currentTimeMillis();
			NtpV3Packet message = timeInfo.getMessage();
			long serverTime = message.getTransmitTimeStamp().getTime();
			synchronized (this) {
				if (now2-now1 < 500) {
					lastGoodPCTime = now2;
					lastGoodNTPTime = serverTime;
					lastestOffset = serverTime - now2;
					WarningSystem.getWarningSystem().removeWarning(ntpWarning);
					globalTimeManager.updateUTCOffset(new TimeCorrection(now2, serverTime, ntpParameters.serverName));
				}
				else {
					if (now2 - lastGoodPCTime > 600000) {
						clearTimeInfo();
					}
					ntpWarning.setWarningMessage(String.format("Slow access to NTP Server %s (%d milliseconds)", ntpParameters.serverName, now2-now1));
					ntpWarning.setWarnignLevel(1);
					WarningSystem.getWarningSystem().addWarning(ntpWarning);
					return false;
				}
			}
		} catch (IOException e) {
			clearTimeInfo();
			ntpWarning.setWarningMessage(String.format("Can't access NTP Server %s", ntpParameters.serverName));
			ntpWarning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(ntpWarning);
			inetAddress = null;
			return false;
		}
//	catch (UnknownHostException e) {
//		inetAddress = null;
//		System.out.println(e.getMessage());
////		e.printStackTrace();
////		return false;
//	}
		return true;
	}

//	/**
//	 * Get the corrected time. This is based on the current PC clock time + the 
//	 * correction measured in the last call to the server. 
//	 * @return time in milliseconds
//	 * @throws PamNTPTimeException 
//	 */
//	public long getNTPTime() throws PamNTPTimeException {
//		synchronized (this) {
//			if (lastGoodNTPTime == 0) {
//				throw new PamNTPTimeException("No server offset time available");
//			}
//			return System.currentTimeMillis() + lastestOffset;
//		}
//	}

	/**
	 * Clear any time information
	 */
	private void clearTimeInfo() {
		lastGoodPCTime = lastGoodNTPTime = 0;
	}

	@Override
	public String getUnitName() {
		return "NTP Time Manager";
	}

	@Override
	public String getUnitType() {
		return "NTP Time Manager";
	}

	@Override
	public Serializable getSettingsReference() {
		return ntpParameters;
	}

	@Override
	public long getSettingsVersion() {
		return NTPTimeParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.ntpParameters = (NTPTimeParameters) (pamControlledUnitSettings.getSettings());
		return true;
	}

	@Override
	public String getName() {
		return "NTP Server Time";
	}

	@Override
	public String getSource() {
		return ntpParameters.serverName;
	}

	@Override
	public boolean showDialog(Window frame) {
		NTPTimeParameters newParams = NTPDialog.showDialog(frame, ntpParameters);
		if (newParams != null) {
			ntpParameters = newParams;
//			stop();
//			start();
		}
		return newParams != null;
	}

	@Override
	public int getUpdateInterval() {
		return ntpParameters.intervalSeconds;
	}

	@Override
	public void stop() {
//		ntpTimer.stop();
		pcClockTimer.stop();
		try {
			timeClient.close();
		} catch (Exception e) {
		}
		keepRunning = false;
		try {
			timeClient.close();
			ntpThread.interrupt();
			ntpThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		WarningSystem.getWarningSystem().removeWarning(ntpWarning);		
	}

	@Override
	public boolean start() {		
		timeClient = new NTPUDPClient();
		try {
			inetAddress = InetAddress.getByName(ntpParameters.serverName);
		} catch (UnknownHostException e) {
			inetAddress = null;
			e.printStackTrace();
//			return false;
		}
		keepRunning = true;
		ntpThread = new Thread(new NTPThread());
		ntpThread.start();

		pcClockTimer.start();
		return true;
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

}
