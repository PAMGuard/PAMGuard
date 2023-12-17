package PamController;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Very simple class used in an ArrayList of used modules that 
 * get's saved between runs. This forms the core of the settings system
 * so don't f*** with it !
 * @author Doug
 *
 */
public class UsedModuleInfo implements Serializable, ManagedParameters {
	
	static public final long serialVersionUID = 0;

	public String className;
	
	private String unitType;
	
	public String unitName;
	
//	static private ArrayList<UsedModuleInfo> usedModules = new ArrayList<UsedModuleInfo>();

	public UsedModuleInfo(String className, String unitType, String unitName) {
		this.className = className;
		this.unitType = unitType;
		this.unitName = unitName;
	}

	/**
	 * @return the unitType
	 */
	public String getUnitType() {
		if (unitType == null) {
			return className;
		}
		return unitType;
	}

	/**
	 * @param unitType the unitType to set
	 */
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	@Override
	public String toString() {
		return unitType + " " + unitName;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
