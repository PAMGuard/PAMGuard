package loggerForms.dataselect;

import java.util.HashMap;

public class LookupDataSelParams extends ControlDataSelParams {
	
	public static final long serialVersionUID = 1L;

	private boolean useUnassigned;

	private HashMap<String, Boolean> itemSelection = new HashMap<>();
	
	public void setItemSelection(String itemName, boolean isSelected) {
		this.itemSelection.put(itemName, isSelected);
	}
	
	public boolean getItemSelection(String itemName) {
		Boolean selection = itemSelection.get(itemName);
		return selection == null ? false : selection;
	}

	/**
	 * @return the useUnassigned
	 */
	public boolean isUseUnassigned() {
		return useUnassigned;
	}

	/**
	 * @param useUnassigned the useUnassigned to set
	 */
	public void setUseUnassigned(boolean useUnassigned) {
		this.useUnassigned = useUnassigned;
	}

}
