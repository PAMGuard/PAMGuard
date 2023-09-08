package dataPlots.layout;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import dataPlots.data.TDDataInfo;

public class GraphParameters implements Serializable, Cloneable, ManagedParameters {
	
	public static final long serialVersionUID = 1L;
	
	public boolean autoScale= true;

	public String currentAxisName;
	
	protected ArrayList<DataListInfo> dataListInfos;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GraphParameters clone() {
		try {
			return (GraphParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void  addDataListInfo(DataListInfo dataListInfo) {
		if (dataListInfos == null) {
			dataListInfos = new ArrayList<DataListInfo>();
		}
		dataListInfos.add(dataListInfo);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("dataListInfos");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return dataListInfos;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
