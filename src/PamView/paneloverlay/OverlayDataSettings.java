package PamView.paneloverlay;

import java.io.Serializable;
import java.util.Hashtable;

public class OverlayDataSettings implements Cloneable, Serializable {

	public static final long serialVersionUID = 0L;
	
	private Hashtable<String, OverlayDataInfo> dataInfoTable = new Hashtable<>();
	
	public void setInfo(String dataName, OverlayDataInfo dataInfo) {
		if (dataInfo != null) {
			dataInfoTable.put(dataName, dataInfo);
		}
	}
	
	public OverlayDataInfo getInfo(String dataName) {
		return dataInfoTable.get(dataName);
	}

	@Override
	protected OverlayDataSettings clone()  {
		try {
			return (OverlayDataSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
