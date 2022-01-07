package angleMeasurement;

import java.awt.Frame;
import java.io.IOException;
import java.io.Serializable;

import serialComms.jserialcomm.PJSerialComm;
import serialComms.jserialcomm.PJSerialException;
import serialComms.jserialcomm.PJSerialLineListener;
import PamController.PamControlledUnitSettings;
import PamUtils.PamUtils;

/**
 * Read out a Fluxgate World 3030 shaft encoder. 
 * @author Douglas Gillespie
 *
 */
public class FluxgateWorldAngles extends AngleMeasurement  {
	
	private FluxgateWorldParameters fluxgateWorldParameters;
	
	private PJSerialComm fgSerialPortCom;
	
	private static final int BAUDRATE = 9600;
	
	private Double latestAngle = null;
	 
	private long latestTime;
	
	private volatile boolean commandSent = false;
	
	private String name;
	
	// commands to send fluxgate
	private static final String COMMAND_DIGITAL = "$Dr"; 
		
	public FluxgateWorldAngles(String name, boolean autoStart) {
		super(name);
		if (fluxgateWorldParameters == null) {
			fluxgateWorldParameters = new FluxgateWorldParameters();
		}
		this.name = name;
		setAngleParameters(fluxgateWorldParameters);
		if (autoStart) {
			start();
		}
	}

	@Override
	public Double getRawAngle() {
		return latestAngle;
	}
	
	@Override
	public Double getCorrectedAngle() {
		if (latestAngle == null) {
			return null;
		}
		double calibratedAngle = getCalibratedAngle(latestAngle);
		Double ang =  calibratedAngle - getAngleOffset();
		ang = PamUtils.constrainedAngle(ang);
		return ang;
	}

	@Override
	public void setZero() {

		if (latestAngle != null) {
			super.setAngleOffset(latestAngle);
		}

	}

	@Override
	public boolean settings(Frame parentFrame) {

		FluxgateWorldParameters newParams = FluxgateWorldDialog.showDialog(parentFrame, this, fluxgateWorldParameters);
		if (newParams != null) {
			fluxgateWorldParameters = newParams.clone();
			setAngleParameters(fluxgateWorldParameters);
			start();
			return true;
		}
		return false;
	}

	public Serializable getSettingsReference() {
		return fluxgateWorldParameters;
	}

	public long getSettingsVersion() {
		return FluxgateWorldParameters.serialVersionUID;
	}

	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		fluxgateWorldParameters = ((FluxgateWorldParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	public boolean start() {
		stop();
		try {
			fgSerialPortCom = PJSerialComm.openSerialPort(fluxgateWorldParameters.portName, BAUDRATE);
		}
		catch (PJSerialException ex) {
			System.out.println(String.format("Serial port %s does not exist", fluxgateWorldParameters.portName));
			return false;
		}
		fgSerialPortCom.addLineListener(new SerialListener());
		
		sendCommand(COMMAND_DIGITAL, true, 2000);
		sendCommand(fluxgateWorldParameters.getRateCommand(), true, 2000);
		return true;
	}
	
	public void stop() {
		if (fgSerialPortCom == null) {
			return;
		}
		fgSerialPortCom.closePort();
		fgSerialPortCom = null;
	}
	
	private boolean sendCommand(String command, boolean waitAnswer, long timeOut) {
//		command += "\r\n"; this gets done in PJSerialComm
		commandSent = true;
		
		try {
			fgSerialPortCom.writeToPort(command,true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		long now = System.currentTimeMillis();
		while (commandSent) {
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (System.currentTimeMillis() - now > timeOut) {
				return false;
			}
		}
		return true;
	}
	
	private class SerialListener implements PJSerialLineListener {

		@Override
		public void newLine(String aLine) {
			readData(aLine);
		}

		@Override
		public void portClosed() {
		}

		@Override
		public void readException(Exception e) {
		}
	}
	
	private void interpretCommandReply(String data) {
	}
	
	public void readData(String result) {

		if (commandSent) {
			interpretCommandReply(result);
			commandSent = false;
			return;
		}
		try {
			latestAngle = Double.valueOf(result.substring(1));
			if (fluxgateWorldParameters.invertAngles) {
				latestAngle = 360. - latestAngle;
			}
			latestTime = System.currentTimeMillis();
		}
		catch (NumberFormatException e) {
			latestAngle = null;
		}
		notifyAngleMeasurementListeners();
	}
		
	public FluxgateWorldParameters getFluxgateWorldParameters() {
		return fluxgateWorldParameters;
	}

	public void setFluxgateWorldParameters(FluxgateWorldParameters fluxgateWorldParameters) {
		super.setAngleParameters(fluxgateWorldParameters);
		this.fluxgateWorldParameters = fluxgateWorldParameters;
	}
	public void setFluxgateWorldParameters(FluxgateWorldParameters fluxgateWorldParameters, boolean start) {
		super.setAngleParameters(fluxgateWorldParameters);
		this.fluxgateWorldParameters = fluxgateWorldParameters;
		if (start) {
			start();
		}
	}


}
