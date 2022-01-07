package PamguardMVC.dataSelector;

import java.util.HashMap;

public class CompoundParams extends DataSelectParams {

	public static final long serialVersionUID = 1L;
	
	private HashMap<String, DataSelectParams> selectorParams = new HashMap<>();
	
	public CompoundParams() {
		
	}

	public void setSelectorParams(DataSelector dataSelector, DataSelectParams params) {
		selectorParams.put(dataSelector.getLongSelectorName(), params);
	}
	
	public DataSelectParams getSelectorParams(DataSelector dataSelector) {
		return selectorParams.get(dataSelector.getLongSelectorName());
	}
}
