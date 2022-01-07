package clickDetector.offlineFuncs.rcImport;

import java.sql.Types;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;

public class RainbowClicksTableDef extends EmptyTableDefinition {

	private PamTableItem eventId, file, section, clickNumber, date, dateNum, amplitude;
	public RainbowClicksTableDef() {
		super("OfflineRCClicks");
		removeTableItem(getIndexItem());
		addTableItem(eventId = new PamTableItem("EventId", Types.INTEGER));
		addTableItem(file = new PamTableItem("File", Types.CHAR, 50));
		addTableItem(section = new PamTableItem("Section", Types.INTEGER));
		addTableItem(clickNumber = new PamTableItem("ClickNum", Types.INTEGER));
		addTableItem(date = new PamTableItem("Date", Types.TIMESTAMP));
		addTableItem(dateNum = new PamTableItem("DateNum", Types.DOUBLE));
		addTableItem(amplitude = new PamTableItem("Amplitude", Types.FLOAT));
	}
	
	public Integer getEventId() {
		if (eventId.getValue() == null) {
			return null;
		}
		return eventId.getIntegerValue();
	}
	
	public String getFile() {
		return file.getStringValue();
	}
	
	public Integer getSection() {
		if (section.getValue() == null) {
			return null;
		}
		return section.getIntegerValue();
	}
	
	public Integer getClickNumber() {
		if (clickNumber.getValue() == null) {
			return null;
		}
		return clickNumber.getIntegerValue();
	}
	
	public Long getMillis() {
		if (dateNum.getValue() == null) {
			return null;
		}
		long millis1 = (long) ((dateNum.getDoubleValue()-RainbowEventsTableDef.dateNumOffset) *
				RainbowEventsTableDef.dateNumScale);		
		return millis1;
	}
	
	Float getAmplitude() {
		if (amplitude.getValue() == null) {
			return null;
		}
		return amplitude.getFloatValue();
	}

}
