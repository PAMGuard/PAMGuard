package generalDatabase.lineplots;

import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;

public class LinePlotDataInfo extends TDDataInfoFX {

	private LinePlotDataProvider linePlotDataProvider;
	private LinePlotControl linePlotControl;

	public LinePlotDataInfo(LinePlotDataProvider linePlotDataProvider, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(linePlotDataProvider, tdGraph, pamDataBlock);
		this.linePlotDataProvider = linePlotDataProvider;
		this.linePlotControl = linePlotDataProvider.getLinePlotControl();
		List<EnhancedTableItem> colItems = linePlotControl.getColumnItems();
		for (EnhancedTableItem ti:colItems) {
			LinePlotScaleInfo scaleInfo = ti.getTdScaleInfo();
			if (scaleInfo != null) {
				addScaleInfo(scaleInfo);
			}
		}
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		TDScaleInfo scaleInfo = getCurrentScaleInfo();
		if (scaleInfo == null || scaleInfo instanceof LinePlotScaleInfo == false) {
			return null;
		}
		int dataIndex = ((LinePlotScaleInfo) scaleInfo).getDataIndex();
		Object[] datas = ((LinePlotDataUnit) pamDataUnit).getData();
		if (datas == null || datas.length <= dataIndex) {
			return null;
		}
		Object data = datas[dataIndex];
		return doubleData(data);
	}

	/**
	 * Convert the number of whatever type to a Double. 
	 * @param data
	 * @return Double representation of the data
	 */
	private Double doubleData(Object data) {
		if (data == null) {
			return null;
		}
		if (data instanceof Double) {
			return (Double) data;
		}
		if (data instanceof Float) {
			return ((Float) data).doubleValue();
		}
		if (data instanceof Integer) {
			return ((Integer) data).doubleValue();
		}
		if (data instanceof Long) {
			return ((Long) data).doubleValue();
		}
		if (data instanceof Short) {
			return ((Short) data).doubleValue();
		}
		// try via string !
		try {
			return Double.valueOf(data.toString());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

}
