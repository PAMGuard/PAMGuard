package PamguardMVC.dataSelector;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

public class DataSelectorSettings implements Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private Hashtable<String, DataSelectParams> selectorParams = new Hashtable<>();
	
	/**
	 * Set data select params within the master list. 
	 * @param name Name of data selector
	 * @param params selection parameters or null.
	 */
	public void setParams(String name, DataSelectParams params) {
		if (selectorParams == null) {
			selectorParams = new Hashtable<>();
		}
		selectorParams.put(name, params);
	}
	
	/**
	 * Retrieve data selector params for a given name. 
	 * @param name Name of data selector
	 * @return Params or null if they don't exist. 
	 */
	public DataSelectParams getParams(String name) {
		if (selectorParams == null) {
			return null;
		}
		return selectorParams.get(name);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("selectorParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return selectorParams;
				}

				@Override
				public boolean setData(Object data) throws IllegalArgumentException, IllegalAccessException {
					// TODO Auto-generated method stub
					try {
						selectorParams = (Hashtable<String, DataSelectParams>) data;
					}
					catch (Exception e) {
						throw new IllegalArgumentException(e.getMessage());
					}
					return true;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
