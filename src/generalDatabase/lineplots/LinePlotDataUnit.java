package generalDatabase.lineplots;

import java.util.List;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class LinePlotDataUnit extends PamDataUnit<PamDataUnit, PamDataUnit> {

	private Object[] data;
	private LinePlotControl linePlotControl;

	public LinePlotDataUnit(LinePlotControl linePlotControl, long utcMilliseconds, Object[] data) {
		super(new DataUnitBaseData(utcMilliseconds, 0));
		this.linePlotControl = linePlotControl;
		this.data = data;
	}

	public Object[] getData() {
		return data;
	}

	@Override
	public String getSummaryString() {
		String str = super.getSummaryString();
		List<EnhancedTableItem> cols = linePlotControl.getColumnItems();
		if (data == null || cols == null) {
			return str;
		}
		int n = Math.min(cols.size(), data.length);
		for (int i = 0; i < n; i++) {
			if (data[i] != null) {
				str += String.format("<br>%s: %s", cols.get(i), data[i].toString());
			}
		}
		return str;
	}
	
	

}
