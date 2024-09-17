package GPS;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

public class ProcessHeadingData extends PamProcess {
	/*
	 * 
$GPHDT
Heading, True.
Actual vessel heading in degrees True produced by any device or system producing true heading.
$--HDT,x.x,T
x.x = Heading, degrees True 

HCHDG - Compass output is used on Garmin etrex summit, vista , and 76S receivers 
to output the value of the internal flux-gate compass. Only the magnetic heading and 
magnetic variation is shown in the message.
  $HCHDG,101.1,,,7.1,W*3C
where:
     HCHDG    Magnetic heading, deviation, variation
     101.1    heading
     ,,       deviation (no data)
     7.1,W    v


	 */
	private GPSControl gpsControl;
	
	private NMEADataBlock headingDataSource;
	
	Double trueHeading, magneticHeading, magneticVariation;
	long trueTime, magneticTime;
	
	private static final String magString = "HDG";
	private static final String trueString = "HDT";
	
	public ProcessHeadingData(GPSControl gpsControl) {
		super(gpsControl, null, "Heading Data");
		this.gpsControl = gpsControl;
	}

	@Override
	public void noteNewSettings() {
		findDataSource();
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (!gpsControl.gpsParameters.readHeading) {
			return;
		}
		NMEADataUnit nmeaData = (NMEADataUnit) arg;
		StringBuffer nmeaString = nmeaData.getCharData();
		String stringId = NMEADataBlock.getSubString(nmeaString, 0);
		if (stringId == null || stringId.length() < 6) {
			return;
		}
		stringId = stringId.substring(3, 6);
		if (stringId.equals(trueString)) {
			processTrueData(nmeaData);
		}
		else if (stringId.equals(magString)) {
			processMagneticData(nmeaData);
		}
	}

	private void processMagneticData(NMEADataUnit nmeaData) {
		StringBuffer nmeaString = nmeaData.getCharData();
		String magString = NMEADataBlock.getSubString(nmeaString, 1);
		String varString = NMEADataBlock.getSubString(nmeaString, 3);
		String varType = NMEADataBlock.getSubString(nmeaString, 4);
		magneticTime = nmeaData.getTimeMilliseconds();
		if (magString == null) {
			magneticHeading = null;
		}
		else try {
			magneticHeading = Double.valueOf(magString);
		}
		catch (NumberFormatException e) {
			magneticHeading = null;
		}
		
		if (varString == null) {
			magneticVariation = null;
		}
		else try {
			magneticVariation = Double.valueOf(varString);
		}
		catch (NumberFormatException e) {
			magneticVariation = null;
		}
		if (magneticVariation == null) {
			return;
		}
		if (varType == null) {
			magneticVariation = null;
		}
		else if (varType.equalsIgnoreCase("W")) {
			magneticVariation *= -1;
		}
		
	}

	private void processTrueData(NMEADataUnit nmeaData) {
		StringBuffer nmeaString = nmeaData.getCharData();
		String trueString = NMEADataBlock.getSubString(nmeaString, 1);
		trueTime = nmeaData.getTimeMilliseconds();
		if (trueString == null) {
			trueHeading = null;
		}
		try {
			trueHeading = Double.valueOf(trueString);
		}
		catch (NumberFormatException e) {
			trueHeading = null;
		}
	}

	private void findDataSource() {
		if (!gpsControl.isGpsMaster()) {
			return;
		}
		NMEADataBlock newSource = (NMEADataBlock) PamController.getInstance().
		getDataBlock(NMEADataUnit.class, gpsControl.gpsParameters.headingNMEASource); 
	
		setParentDataBlock(newSource);	
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			noteNewSettings();
		}
	}

	/**
	 * @return the trueHeading
	 */
	public Double getTrueHeading() {
		return trueHeading;
	}

	/**
	 * @return the magneticHeading
	 */
	public Double getMagneticHeading() {
		return magneticHeading;
	}

	/**
	 * @return the trueTime
	 */
	public long getTrueTime() {
		return trueTime;
	}

	/**
	 * @return the magneticTime
	 */
	public long getMagneticTime() {
		return magneticTime;
	}

	/**
	 * @return the magneticVariation
	 */
	public Double getMagneticVariation() {
		return magneticVariation;
	}

	@Override
	public void pamStart() {
		trueHeading = magneticHeading = null;
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

}
