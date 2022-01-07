package generalDatabase;

import java.sql.Types;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Standard logging class for any PamDetection, but you'll want to extend it for any 
 * additional information your detector outputs. 
 * <p>
 * Handles data from all fields in the PamDetection class.
 * <p>
 * Handles data from expected fields in associated localisation data.
 * <p>
 * N.B. Not all databases support huge (64 bit) integers, so the sample number is
 * written as a 32 bit integer, but the detection time is also written as a double
 * number of seconds from the start of the run, so any overflow should show up clearly
 * (and seconds are much easier to deal with in any offline analysis of data from the database).
 * 
 * @author Doug Gillespie
 *
 */
public class PamDetectionLogging extends SQLLogging {

	PamTableDefinition tableDefinition;

	// basic items available in any PamDetection.
	PamTableItem channelMap, startSample, startSeconds, duration, lowFreq, highFreq, amplitude, detectionType;
	// some extra items if Localisation data are available.
	PamTableItem bearingAmbiguity;
	PamTableItem[] bearing, range, depth, bearingError, rangeError, depthError;
	PamTableItem[] latitude, longitude;
	PamTableItem[] parallelError, perpError, referenceAngle;

	LocalisationInfo localisationFlags;
	
	int nSides;

	public PamDetectionLogging(PamDataBlock pamDataBlock, int updatePolicy) {

		super(pamDataBlock);

		makeStandardTableDefinition(updatePolicy);

	}

	/**
	 * Make a standard table for detection and localisation data. 
	 * @param updatePolicy 
	 *
	 */
	protected void makeStandardTableDefinition(int updatePolicy) {
		tableDefinition = new PamTableDefinition(this.getPamDataBlock().getLoggingName(), updatePolicy);
		// add all the standard information fields...
		tableDefinition.addTableItem(channelMap = new PamTableItem("channelMap", Types.INTEGER));
		tableDefinition.addTableItem(startSample = new PamTableItem("startSample", Types.INTEGER));
		tableDefinition.addTableItem(startSeconds = new PamTableItem("startSeconds", Types.DOUBLE));
		tableDefinition.addTableItem(duration = new PamTableItem("duration", Types.INTEGER));
		tableDefinition.addTableItem(lowFreq = new PamTableItem("lowFreq", Types.DOUBLE));
		tableDefinition.addTableItem(highFreq = new PamTableItem("highFreq", Types.DOUBLE));
		tableDefinition.addTableItem(amplitude = new PamTableItem("amplitude", Types.DOUBLE));
		tableDefinition.addTableItem(detectionType = new PamTableItem("detectionType", Types.CHAR, 20));
		// add the localisation information only if it exists (or may exist).
		localisationFlags = getPamDataBlock().getLocalisationContents();
		tableDefinition.setUseCheatIndexing(true);

		nSides = 1;
		if (localisationFlags.hasLocContent(LocContents.HAS_AMBIGUITY)) {
			nSides = 2;
		}
		
		bearing = new PamTableItem[nSides];
		range = new PamTableItem[nSides];
		depth = new PamTableItem[nSides];
		bearingError = new PamTableItem[nSides];
		rangeError = new PamTableItem[nSides];
		depthError = new PamTableItem[nSides];
		latitude = new PamTableItem[nSides];
		longitude = new PamTableItem[nSides];
		parallelError = new PamTableItem[nSides];
		perpError = new PamTableItem[nSides];
		referenceAngle = new PamTableItem[nSides];

		String suffix = "";
		tableDefinition.addTableItem(bearingAmbiguity = new PamTableItem("bearingAmbiguity", Types.BIT));
		for (int i = 0; i < nSides; i++) {
			suffix = String.format("%d", i);
			if (localisationFlags.hasLocContent(LocContents.HAS_BEARING))  {
				tableDefinition.addTableItem(bearing[i] = new PamTableItem("bearing"+suffix, Types.DOUBLE));
			}
			range[i] = new PamTableItem("range"+suffix, Types.DOUBLE);
			if (localisationFlags.hasLocContent(LocContents.HAS_RANGE)){
				tableDefinition.addTableItem(range[i]);
			}
			if (localisationFlags.hasLocContent(LocContents.HAS_DEPTH)) {
				tableDefinition.addTableItem(depth[i] = new PamTableItem("depth"+suffix, Types.DOUBLE));
			}
			if (localisationFlags.hasLocContent(LocContents.HAS_BEARINGERROR)) {
				tableDefinition.addTableItem(bearingError[i] = new PamTableItem("bearingError"+suffix, Types.DOUBLE));
			}
			if (localisationFlags.hasLocContent(LocContents.HAS_RANGEERROR)) {
				tableDefinition.addTableItem(rangeError[i] = new PamTableItem("rangeError"+suffix, Types.DOUBLE));
			}
			if (localisationFlags.hasLocContent(LocContents.HAS_DEPTH)) {
				tableDefinition.addTableItem(depthError[i] = new PamTableItem("depthError"+suffix, Types.DOUBLE));
			}
			if (localisationFlags.hasLocContent(LocContents.HAS_LATLONG)) {
				tableDefinition.addTableItem(latitude[i] = new PamTableItem("Latitude"+suffix, Types.DOUBLE));
				tableDefinition.addTableItem(longitude[i] = new PamTableItem("Longitude"+suffix, Types.DOUBLE));
			}
			if (localisationFlags.hasLocContent(LocContents.HAS_PERPENDICULARERRORS)) {
				tableDefinition.addTableItem(parallelError[i] = new PamTableItem("ParallelError"+suffix, Types.DOUBLE));
				tableDefinition.addTableItem(perpError[i] = new PamTableItem("PerpendicularError"+suffix, Types.DOUBLE));
				tableDefinition.addTableItem(referenceAngle[i] = new PamTableItem("ErrorReferenceAngle"+suffix, Types.DOUBLE));
			}
		}

		setTableDefinition(tableDefinition);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDetection) {

		AbstractLocalisation abstractLocalisation = pamDetection.getLocalisation();

		channelMap.setValue(pamDetection.getChannelBitmap());
		Long ss = pamDetection.getStartSample();
		if (ss != null) {
			startSample.setValue(ss.intValue());
			startSeconds.setValue((double) pamDetection.getStartSample() / getPamDataBlock().getSampleRate());
		}
		else {
			startSample.setValue(null);
			startSeconds.setValue(null);
		}
		duration.setValue(pamDetection.getSampleDuration());
		double[] frequency = pamDetection.getFrequency();
		if (frequency != null && frequency.length >= 2) {
			lowFreq.setValue(frequency[0]);
			highFreq.setValue(frequency[1]);
		}
		else {
			lowFreq.setValue(0);
			highFreq.setValue(0);
		}
		amplitude.setValue(pamDetection.getAmplitudeDB());
//		detectionType.setValue(pamDetection.getDetectionType());

		LocalisationInfo unitLocalisationFlags = new LocContents(0); //could be null but then need a null check everytime we use it. 
		if (abstractLocalisation != null) {
			unitLocalisationFlags = abstractLocalisation.getLocContents();
		}
		else { // set everything to do with localisation to null and get out. 
			setNullData(bearingAmbiguity);
			setNullData(bearing);
			setNullData(range);
			setNullData(depth);
			setNullData(bearingError);
			setNullData(rangeError);
			setNullData(depthError);
			setNullData(longitude);
			setNullData(latitude);
			setNullData(parallelError);
			setNullData(perpError);
			setNullData(referenceAngle);
			return;
		}

		if (bearingAmbiguity != null && abstractLocalisation != null) {
			bearingAmbiguity.setValue(abstractLocalisation.bearingAmbiguity());
		}
		double[] angles = abstractLocalisation.getPlanarAngles();
		int nBearings = 0;
		if (angles != null) {
			nBearings = Math.min(angles.length, bearing.length);
		}
		if (bearing != null && localisationFlags.hasLocContent(LocContents.HAS_BEARING)) {
			for (int i = 0; i < nBearings; i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for bearing information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_BEARING)) {
					if (bearing[i] != null)
						bearing[i].setValue(angles[i]);
				}
				else {
					if (bearing[i] != null)
						bearing[i].setValue(null);
					if (bearingAmbiguity != null)
						bearingAmbiguity.setValue(null);
				}
			}
		}

		if (localisationFlags.hasLocContent(LocContents.HAS_RANGE)) {
			for (int i = 0; i < range.length; i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for range information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_RANGE)) {
					range[i].setValue(abstractLocalisation.getRange(i));
				}
				else {
					range[i].setValue(null);
				}
			}
		}

		if (localisationFlags.hasLocContent(LocContents.HAS_DEPTH)) {
			for (int i = 0; i < depth.length; i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for depth information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_DEPTH)) {
					depth[i].setValue(abstractLocalisation.getHeight(i));
				}
				else {
					depth[i].setValue(null);
				}
			}
		}

		if (localisationFlags.hasLocContent(LocContents.HAS_BEARINGERROR)) {
			for (int i = 0; i < bearingError.length; i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for bearing error information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_BEARINGERROR)) {
					bearingError[i].setValue(abstractLocalisation.getBearingError(i));
				}
				else {
					bearingError[i].setValue(null);
				}
			}
		}

		if (localisationFlags.hasLocContent(LocContents.HAS_RANGEERROR)) {
			for (int i = 0; i < rangeError.length; i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for range error information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_RANGEERROR)) {
					rangeError[i].setValue(abstractLocalisation.getRangeError(i));
				}
				else {
					rangeError[i].setValue(null);
				}
			}
		}

		if (localisationFlags.hasLocContent(LocContents.HAS_DEPTHERROR)) {
			for (int i = 0; i < depthError.length; i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for depth error information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_DEPTHERROR)) {
					depthError[i].setValue(abstractLocalisation.getHeightError(i));
				}
				else {
					depthError[i].setValue(null);
				}
			}
		}
		
		if (localisationFlags.hasLocContent(LocContents.HAS_LATLONG)) {
			LatLong ll;
			for (int i = 0; i < abstractLocalisation.getNumLatLong(); i++) {
				if (i >= nSides) {
					System.out.println("Incorrect database set up - no enought columns for LatLong information in table " 
							+ getTableDefinition().getTableName());
					continue;
				}
				ll = abstractLocalisation.getLatLong(i);
				if (ll == null) {
					continue;
				}
				if (latitude[i] == null) {
					System.out.println("Null latlong field " + i);
				}
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_LATLONG)) {
					if (latitude[i] != null)
					latitude[i].setValue(ll.getLatitude());
					if (longitude[i] != null)
					longitude[i].setValue(ll.getLongitude());
				}
				else {
					if (latitude[i] != null)
					latitude[i].setValue(null);
					if (longitude[i] != null)
					longitude[i].setValue(null);
				}
			}
		}
		if ((localisationFlags.hasLocContent(LocContents.HAS_PERPENDICULARERRORS))) {
			for (int i = 0; i < perpError.length; i++) {
				if (unitLocalisationFlags.hasLocContent(LocContents.HAS_PERPENDICULARERRORS)) {
					if (perpError[i] != null)
						perpError[i].setValue(abstractLocalisation.getPerpendiculaError(i));
					if (parallelError[i] != null)
						parallelError[i].setValue(abstractLocalisation.getParallelError(i));
					if (referenceAngle[i] != null)
						referenceAngle[i].setValue(abstractLocalisation.getErrorDirection(i));
				}
				else {
					if (perpError[i] != null)
						perpError[i].setValue(null);
					if (parallelError[i] != null)
						parallelError[i].setValue(null);
					if (referenceAngle[i] != null)
						referenceAngle[i].setValue(null);
				}
			}
		}

	}
	private void setNullData(PamTableItem tableItem) {
		if (tableItem == null) return;
		tableItem.setValue(null);
	}
	private void setNullData (PamTableItem[] tableItems) {
		if (tableItems == null) {
			return;
		}
		for (int i = 0; i < tableItems.length; i++) {
			setNullData(tableItems[i]);
		}
	}

	/**
	 * PamDetection is nearly always overridden (Not sure why it's not declared abstract)
	 * so it's quite difficult for createDataUnit to fill and do anything with these in the 
	 * general class. Therefore, assume that createDataUnit will be overridden in more concrete classes
	 * and just provide a function here to fill the data in to a newDataUnit from standard database columns
	 */
	protected boolean fillDataUnit(SQLTypes sqlTypes, PamDataUnit pamDetection) {
		pamDetection.setChannelBitmap((Integer) channelMap.getValue()); 
		pamDetection.setDatabaseIndex(tableDefinition.getIndexItem().getIntegerValue());
		pamDetection.setSampleDuration((Long) duration.getValue());
		pamDetection.setDatabaseUpdateOf(tableDefinition.getUpdateReference().getIntegerValue());
		double freq[] = new double[2];
		freq[0] = (Double) lowFreq.getValue();
		freq[1] = (Double) highFreq.getValue();
		pamDetection.setFrequency(freq);
		pamDetection.setStartSample((Long) startSample.getValue());
		pamDetection.setMeasuredAmplitude((Double) amplitude.getValue());		
		Object ts = tableDefinition.getTimeStampItem().getValue();
		long t = sqlTypes.millisFromTimeStamp(ts);
		pamDetection.setTimeMilliseconds(t);
		return true; 
	}

	public PamTableItem getAmplitude() {
		return amplitude;
	}

	public PamTableItem[] getBearing() {
		return bearing;
	}

	public PamTableItem getBearingAmbiguity() {
		return bearingAmbiguity;
	}

	public PamTableItem[] getBearingError() {
		return bearingError;
	}

	public PamTableItem getChannelMap() {
		return channelMap;
	}

	public PamTableItem[] getDepth() {
		return depth;
	}

	public PamTableItem[] getDepthError() {
		return depthError;
	}

	public PamTableItem getDetectionType() {
		return detectionType;
	}

	public PamTableItem getDuration() {
		return duration;
	}

	public PamTableItem getHighFreq() {
		return highFreq;
	}

	public PamTableItem[] getLatitude() {
		return latitude;
	}

	public int getLocalisationFlags() {
		return localisationFlags.getLocContent();
	}

	public PamTableItem[] getLongitude() {
		return longitude;
	}

	public PamTableItem getLowFreq() {
		return lowFreq;
	}

	public int getNSides() {
		return nSides;
	}

	public PamTableItem[] getParallelError() {
		return parallelError;
	}

	public PamTableItem[] getPerpError() {
		return perpError;
	}

	public PamTableItem[] getRange() {
		return range;
	}

	public PamTableItem[] getRangeError() {
		return rangeError;
	}

	public PamTableItem[] getReferenceAngle() {
		return referenceAngle;
	}

	public PamTableItem getStartSample() {
		return startSample;
	}

	public PamTableItem getStartSeconds() {
		return startSeconds;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#getUIDMatchClause(PamguardMVC.PamDataUnit, generalDatabase.SQLTypes)
	 */
	@Override
	public String getUIDMatchClause(PamDataUnit pamDataUnit, SQLTypes sqlTypes) {
		return super.getUIDMatchClause(pamDataUnit, sqlTypes);
	}
	
//	@Override
//	protected boolean createDataUnit() {
//		// rare fot this to get used. 
//		return super.createDataUnit();
//	}
}
