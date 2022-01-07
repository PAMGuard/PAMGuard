package generalDatabase.lineplots;

import dataPlots.data.TDDataInfo;
import dataPlotsFX.data.TDScaleInfo;
import generalDatabase.PamTableItem;

public class EnhancedTableItem extends PamTableItem {

	private String displayName;
		
	private LinePlotScaleInfo linePlotScaleInfo;
	
	/**
	 * @param name
	 * @param sqlType
	 * @param length
	 * @param required
	 */
	public EnhancedTableItem(String name, int sqlType, int length, boolean required) {
		super(name, sqlType, length, required);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param sqlType
	 * @param length
	 */
	public EnhancedTableItem(String name, int sqlType, int length) {
		super(name, sqlType, length);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param sqlType
	 */
	public EnhancedTableItem(String name, int sqlType) {
		super(name, sqlType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		if (displayName == null) 
			return getName();
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public LinePlotScaleInfo getTdScaleInfo() {
		return linePlotScaleInfo;
	}

	public void setTdScaleInfo(LinePlotScaleInfo linePlotScaleInfo) {
		this.linePlotScaleInfo = linePlotScaleInfo;
	}

}
