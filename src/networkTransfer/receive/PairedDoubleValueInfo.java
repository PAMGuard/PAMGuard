package networkTransfer.receive;

public class PairedDoubleValueInfo extends PairedValueInfo {

	private String format;

	public PairedDoubleValueInfo(String pairName, String columnName, String format) {
		super(pairName, columnName);
		this.format = format;
	}

	@Override
	public Object formatTableData(BuoyStatusDataUnit buoyStatusDataUnit, BuoyStatusValue statusValue) {
		if (statusValue == null) {
			return null;
		}
		Object dataVal = statusValue.getData();
		if (dataVal == null) {
			return null;
		}
		if (dataVal instanceof Double == false) {
			return dataVal;
		}
		return String.format(format, dataVal);
	}

}
