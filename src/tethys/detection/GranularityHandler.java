package tethys.detection;

import java.util.List;
import java.util.ListIterator;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.DetectionGroup;
import nilus.Detections;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.species.DataBlockSpeciesManager;

public abstract class GranularityHandler {

	protected TethysControl tethysControl;
	
	protected PamDataBlock dataBlock;
	
	protected TethysExportParams tethysExportParams;
	
	protected StreamExportParams streamExportParams;

	private DataBlockSpeciesManager speciesManager;

	/**
	 * @param tethysControl
	 * @param dataBlock
	 * @param tethysExportParams
	 * @param streamExportParams
	 */
	public GranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		this.tethysControl = tethysControl;
		this.dataBlock = dataBlock;
		this.tethysExportParams = tethysExportParams;
		this.streamExportParams = streamExportParams;
		speciesManager = dataBlock.getDatablockSpeciesManager();
	}
	
	/**
	 * Prepare to start, passing the start time of the effort 
	 * or of the first time bin for binned granularity types. 
	 * @param timeMillis
	 */
	public abstract void prepare(long timeMillis);
	
	/**
	 * Put a data unit into a Detection object. for Call granularity
	 * this will probably return every time. For binned and encounter
	 * types this will only return at the end of a bin / encounter
	 * @param dataUnit
	 * @return Detection object, but only when ready to be added to Detections
	 */
	public abstract Detection[] addDataUnit(PamDataUnit dataUnit);
	
	/**
	 * Get a grouping name for the call. This may just be the calls species code, 
	 * or it may be appended with the channel number. This is used to find bin and 
	 * encounter data in HashMaps in 
	 * @param dataUnit
	 * @return
	 */
	public String getCallGroupName(PamDataUnit dataUnit) {
		String groupName = speciesManager.getSpeciesCode(dataUnit);
		if (groupName == null) {
			groupName = "NullSpecies";
		}
		if (streamExportParams.separateChannels) {
			groupName += String.format("Chan%d", dataUnit.getChannelBitmap());
		}
		return groupName;
	}
	/**
	 * Called after end end of all data units to get the last bin / encounter. <p>
	 * 
	 * @param timeMillis end time of effort or last bin in milliseconds. 
	 * @return null for Call granularity, otherwise may be non null for binned or encounter. 
	 */
	public abstract Detection[] cleanup(long timeMillis);
	
	/**
	 * Convert a single detection to a one element array since that's what' 
	 * most functions need to return. 
	 * @param det
	 * @return
	 */
	protected Detection[] toDetectionArray(Detection det) {
		if (det == null) {
			return null;
		}
		Detection[] dets = new Detection[1];
		dets[0] = det;
		return dets;
	}
	
	/**
	 * Create the correct type of granularity handler to put individual data units into 
	 * Detection objects. 
	 * @param granularity
	 * @param tethysControl
	 * @param dataBlock
	 * @param tethysExportParams
	 * @param streamExportParams
	 * @return
	 */
	public static GranularityHandler getHandler(GranularityEnumType granularity, TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		switch (granularity) {
		case BINNED:
			return new BinnedGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		case CALL:
			return new CallGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		case ENCOUNTER:
			return new EncounterGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		case GROUPED:
			return new GroupedGranularityHandler(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		default:
			break;
		}
		return null;
	}

	/**
	 * Automatically fix mismatches between effort and detections. This will be called if a 
	 * detection or part of a detection is outside of the start and end defined by the effort. If it's a 
	 * small difference, i.e. if the detection at least overlaps the effort then it can be automatically 
	 * fixed by truncating the detection (for binned types) or by a small extension to the effort (for encounter
	 * and call types).  
	 * @param detections nilus Detections object 
	 * @param det a single detection
	 * @return true if it was fixed automatically. False otherwise. 
	 */
	protected abstract boolean autoEffortFix(Detections detections, Detection det);
	
	/**
	 * Check that the detection at least overlaps the effort period. 
	 * @param detections nilus Detections object 
	 * @param det a single detection
	 * @return true if the overlap
	 */
	protected boolean effortOverlap(Detections detections, Detection det) {
		XMLGregorianCalendar effStart = detections.getEffort().getStart();
		XMLGregorianCalendar effEnd = detections.getEffort().getEnd();
		XMLGregorianCalendar detStart = det.getStart();
		XMLGregorianCalendar detEnd = det.getEnd();
		if (effStart.compare(detEnd) == DatatypeConstants.GREATER) {
			return false;
		}
		if (effEnd.compare(detStart) == DatatypeConstants.LESSER) {
			return false;
		}
		return true;
	}

	/**
	 * Fix effort / detection problem but contracting the start / end times of the detection
	 * @param detections nilus Detections object 
	 * @param det a single detection
	 * @return true if fixed automatically
	 */
	protected boolean contractDetection(Detections detections, Detection det) {
		if (effortOverlap(detections, det) == false) {
			return false;
		}
		// at least some overlap, so fix it.
		// going to fix it my shortening the detection, and leave the effort alone. 
		XMLGregorianCalendar effStart = detections.getEffort().getStart();
		XMLGregorianCalendar effEnd = detections.getEffort().getEnd();
		XMLGregorianCalendar detStart = det.getStart();
		XMLGregorianCalendar detEnd = det.getEnd();
		

		if (effStart.compare(detStart) == DatatypeConstants.GREATER) {
			System.out.printf("Fix Detections change detection start from %s to %s\n", detStart, effStart);
			det.setStart(effStart);
		}
		if (effEnd.compare(detEnd) == DatatypeConstants.LESSER) {
			System.out.printf("Fix Detections change detection end from %s to %s\n", detEnd, effEnd);
			det.setEnd(effEnd);
		}
		return true;
	}

	/**
	 * Fix effort / detection problem but expanding the start / end times of the effort
	 * @param detections nilus Detections object 
	 * @param det a single detection
	 * @return true if fixed automatically
	 */
	protected boolean expandEffort(Detections detections, Detection det) {
		if (effortOverlap(detections, det) == false) {
			return false;
		}
		// at least some overlap, so fix it.
		// going to fix it my shortening the detection, and leave the effort alone. 
		XMLGregorianCalendar effStart = detections.getEffort().getStart();
		XMLGregorianCalendar effEnd = detections.getEffort().getEnd();
		XMLGregorianCalendar detStart = det.getStart();
		XMLGregorianCalendar detEnd = det.getEnd();
		
		if (effStart.compare(detStart) == DatatypeConstants.GREATER) {
			System.out.printf("Fix Detections change effort start from %s to %s\n", effStart, detStart);
			detections.getEffort().setStart(detStart);
		}
		if (effEnd.compare(detEnd) == DatatypeConstants.LESSER) {
			System.out.printf("Fix Detections change effort end from %s to %s\n", effEnd, detEnd);
			detections.getEffort().setEnd(detEnd);
		}
		return true;
	}
}
