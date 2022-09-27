package whistleClassifier.dataselect;

import java.io.Serializable;
import java.util.HashMap;

import PamguardMVC.dataSelector.DataSelectParams;

public class WslClsSelectorParams extends DataSelectParams implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	
	private HashMap<String, SppClsSelectParams> sppParamsTable = new HashMap<>();

	public WslClsSelectorParams() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public WslClsSelectorParams clone()  {
		try {
			return (WslClsSelectorParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setSppParams(String sppName, SppClsSelectParams params) {
		if (sppParamsTable == null) {
			sppParamsTable = new HashMap<String, SppClsSelectParams>();
		}
		sppParamsTable.put(sppName, params);
	}
	
	public SppClsSelectParams getSppParams(String sppName) {
		if (sppParamsTable == null) {
			sppParamsTable = new HashMap<String, SppClsSelectParams>();
		}
		SppClsSelectParams sppParams = sppParamsTable.get(sppName);
		if (sppParams == null) {
			sppParams = new SppClsSelectParams(sppName, false, 0);
		}
		return sppParams;
	}

}
