package networkTransfer.receive;

/**
 * Information for additional paired values that will get displayed in the main 
 * RXTable view. 
 * @author dg50
 *
 */
public class PairedValueInfo {

	/**
	 * Initially doesn't have much, but may soon have some fancy 
	 * rendering options so that it can show buttons and drop down lists 
	 * in the table. 
	 */
	private String pairName;
	
	private String columnName;
	
	private Integer decimalPlaces;
	
	private String unitString; 
	
	public PairedValueInfo(String pairName) {
		super();
		this.pairName = pairName;
	}
	
	public PairedValueInfo(String pairName, String columnName) {
		this(pairName);
		setColumnName(columnName);
	}

	public PairedValueInfo(String pairName, String columnName, Integer decimalPlaces, String unitString) {
		this(pairName);
		setColumnName(columnName);
		this.decimalPlaces = decimalPlaces;
		this.unitString = unitString;
	}
	
	public String getColumnName() {
		if (columnName == null) {
			return pairName;
		}
		else {
			return columnName;
		}
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	/**
	 * Get the class for the cell renderer in the table. 
	 * @return
	 */
	public Class getTableClass() {
		return String.class;
	}

	/**
	 * 
	 * @param buoyStatusDataUnit
	 * @param value
	 * @return
	 */
	public Object formatTableData(BuoyStatusDataUnit buoyStatusDataUnit, BuoyStatusValue value) {
		if (value == null) {
			return null;
		}
		Object retVal = value.getData();
		if (decimalPlaces != null) {
			retVal = formatNumber(retVal, decimalPlaces);
		}
		if (unitString == null) {
			return retVal;
		}
		else {
			return retVal.toString() + unitString;
		}
	}
	
	public String getToolTipText(BuoyStatusDataUnit buoyStatusDataUnit, BuoyStatusValue value) {
		return getColumnName();
	}

	/**
	 * @return the pairName
	 */
	public String getPairName() {
		return pairName;
	}

	public String formatNumber(Object obj, int deciPl) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Number == false) {
			return obj.toString();
		}
		Number n = (Number) obj;
		String format = String.format("%%.%df", deciPl);
		return String.format(format, n.doubleValue());
	}

	/**
	 * @return the decimalPlaces
	 */
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * @param decimalPlaces the decimalPlaces to set
	 */
	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	/**
	 * @return the unitString
	 */
	public String getUnitString() {
		return unitString;
	}

	/**
	 * @param unitString the unitString to set
	 */
	public void setUnitString(String unitString) {
		this.unitString = unitString;
	}

	
}
