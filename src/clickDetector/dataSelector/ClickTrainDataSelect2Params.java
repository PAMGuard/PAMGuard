package clickDetector.dataSelector;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.dataSelector.DataSelectParams;

public class ClickTrainDataSelect2Params extends DataSelectParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private static final boolean DEFAULTWANT = true;
	
	private boolean includeUnclassified = true;
	
	private Hashtable<String, Boolean> wantType = new Hashtable<>();

	@Override
	protected ClickTrainDataSelect2Params clone() {
		try {
			return (ClickTrainDataSelect2Params) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set whether or not we want a type based on the String click train type
	 * @param type click type code
	 * @param want want it or not
	 */
	public void setWantType(String type, boolean want) {
		wantType.put(type, want);
	}
	
	/**
	 * Get if we want a click type
	 * @param type click type
	 * @return true if wanted
	 */
	public boolean isWantType(String type) {
		if (type==null) {
			return DEFAULTWANT;
		}
		Boolean is = wantType.get(type);
		if (is == null) {
			return DEFAULTWANT;
		}
		else {
			return is;
		}
	}

	/**
	 * @return the includeUnclassified
	 */
	public boolean isIncludeUnclassified() {
		return includeUnclassified;
	}

	/**
	 * @param includeUnclassified the includeUnclassified to set
	 */
	public void setIncludeUnclassified(boolean includeUnclassified) {
		this.includeUnclassified = includeUnclassified;
	}
	
	/**
	 * 
	 * @return an Array list of selected keys
	 */
	public ArrayList<String> getSelectedList() {
		ArrayList<String> selected = new ArrayList<>();
		Enumeration<String> keys = wantType.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (wantType.get(key))
			{
				selected.add(key);
			}
		}
		return selected;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("wantType");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return wantType;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


	
}
