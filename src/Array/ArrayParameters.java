package Array;

import java.io.Serializable;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Parameters wrapper for array data. Shockingly, the current system just serializes
 * an array list of recent arrays - ArrayList of PamArray's. Are now going to wrap that in a new class so that 
 * the stored class can do something creative with parameter management with the new PamParameterData classes.
 * @author dg50
 *
 */
public class ArrayParameters implements Serializable, ManagedParameters {

	public final static long serialVersionUID = 0L;
	
	private ArrayList<PamArray> arrayList;

	/**
	 * @param arrayList
	 */
	public ArrayParameters(ArrayList<PamArray> arrayList) {
		super();
		this.arrayList = arrayList;
	}

	/**
	 * @return the arrayList
	 */
	public ArrayList<PamArray> getArrayList() {
		return arrayList;
	}

	/**
	 * We keep a list of all the old arrays, but we only want to export the current array (index=0)
	 */
	@Override
	public PamParameterSet getParameterSet() {
		if (arrayList == null || arrayList.size() < 1) {
			return null;
		}
		return arrayList.get(0).getParameterSet();
	}
	
	
}
