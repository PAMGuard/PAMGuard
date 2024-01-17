package generalDatabase;

import PamController.PamSettings;
import PamguardMVC.PamDataUnit;

/**
 * simple data unit for use with the LogXMLSettings class
 * @author dg50
 *
 */
public class LogXMLDataUnit extends PamDataUnit {

	private long processTime;
	private PamSettings pamSettings;
	private String xml;
	private Long dataEnd, processEnd;

	public LogXMLDataUnit(long timeMilliseconds, long processTime, PamSettings pamSettings, String xml) {
		super(timeMilliseconds);
		this.processTime = processTime;
		this.pamSettings = pamSettings;
		this.xml = xml;
	}

	/**
	 * @return the dataEnd
	 */
	public Long getDataEnd() {
		return dataEnd;
	}

	/**
	 * @param dataEnd the dataEnd to set
	 */
	public void setDataEnd(Long dataEnd) {
		this.dataEnd = dataEnd;
	}

	/**
	 * @return the processEnd
	 */
	public Long getProcessEnd() {
		return processEnd;
	}

	/**
	 * @param processEnd the processEnd to set
	 */
	public void setProcessEnd(Long processEnd) {
		this.processEnd = processEnd;
	}

	/**
	 * @return the processTime
	 */
	public long getProcessTime() {
		return processTime;
	}

	/**
	 * @return the pamSettings
	 */
	public PamSettings getPamSettings() {
		return pamSettings;
	}

	/**
	 * @return the xml
	 */
	public String getXml() {
		return xml;
	}


}
