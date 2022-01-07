package clickDetector.offlineFuncs.rcImport;

import java.sql.Types;


import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;

public class RainbowEventsTableDef extends EmptyTableDefinition {

	private PamTableItem startDate, startDateNum, endDateNum, duration, species, nClicks, firstFile, lastFile, comment,
	minNumber, bestNumber, maxNumber, colour;
	
	public static final double dateNumOffset = 25569; // days from 1900 to 1970
	public static final double dateNumScale = 24.*3600.*1000; // millis per day. 
	
	public RainbowEventsTableDef() {
		super("OfflineRCEvents");
		addTableItem(startDate = new PamTableItem("StartDate", Types.TIMESTAMP));
		addTableItem(startDateNum = new PamTableItem("StartDateNum", Types.DOUBLE));
		addTableItem(endDateNum = new PamTableItem("EndDateNum", Types.DOUBLE));
		addTableItem(duration = new PamTableItem("Duration", Types.DOUBLE));
		addTableItem(species = new PamTableItem("Species", Types.CHAR, 5));
		addTableItem(nClicks = new PamTableItem("nClicks", Types.INTEGER));
		addTableItem(firstFile = new PamTableItem("FirstFile", Types.CHAR, 50));
		addTableItem(lastFile = new PamTableItem("LastFile", Types.CHAR, 50));
		addTableItem(comment = new PamTableItem("Comment", Types.CHAR, 255));
		addTableItem(minNumber = new PamTableItem("MinNumber", Types.INTEGER));
		addTableItem(bestNumber = new PamTableItem("BestNumber", Types.INTEGER));
		addTableItem(maxNumber = new PamTableItem("MaxNumber", Types.INTEGER));
		addTableItem(colour = new PamTableItem("Colour", Types.INTEGER));
	}

	/**
	 * Get the start time in millis, once the data have been read in. 
	 * @return
	 */
	public Long getStartTime() {
		if (startDateNum.getValue() == null) {
			return null;
		}
		long millis1 = (long) ((startDateNum.getDoubleValue()-dateNumOffset) * dateNumScale);		
//		Timestamp ts = (Timestamp) startDate.getValue();
//		long millis2 = PamCalendar.millisFromTimeStamp(ts);
		// millis 1 will give sub second accuracy so use in preference to the other time stamp which is to a second. 
		return millis1;
	}
	
	public Double getDuration() {
		if (duration.getValue() == null) {
			return null;
		}
		return duration.getDoubleValue();
	}
	
	public String getSecies() {
		if (species.getValue() == null) {
			return null;
		}
		return species.getDeblankedStringValue();
	}
	
	public Integer getNClicks() {
		if (nClicks.getValue() == null) {
			return null;
		}
		return nClicks.getIntegerValue();
	}

	public Short getMinNumber() {
		if (minNumber.getValue() == null) {
			return null;
		}
		return (short) minNumber.getIntegerValue();
	}
	
	public Short getBestNumber() {
		if (bestNumber.getValue() == null) {
			return null;
		}
		return (short) bestNumber.getIntegerValue();
	}
	
	public Short getMaxNumber() {
		if (maxNumber.getValue() == null) {
			return null;
		}
		return (short) maxNumber.getIntegerValue();
	}

	public Integer getColour() {
		if (colour.getValue() == null) {
			return null;
		}
		return colour.getIntegerValue();
	}
	public String getComment() {
		return comment.getStringValue();
	}

	public Integer getIndex() {
		if (getIndexItem().getValue() == null) {
			return null;
		}
		return getIndexItem().getIntegerValue();
	}
}
