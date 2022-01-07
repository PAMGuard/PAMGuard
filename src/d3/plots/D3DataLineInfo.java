package d3.plots;

import dataPlots.data.DataLineInfo;

public class D3DataLineInfo extends DataLineInfo {

	protected int sampleRate;
	protected int dataIndex;

	public D3DataLineInfo(String name, String unit, int sampleRate, int dataIndex) {
		super(name, unit);
		this.sampleRate = sampleRate;
		this.dataIndex = dataIndex;
	}

}
