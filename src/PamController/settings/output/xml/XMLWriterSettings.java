package PamController.settings.output.xml;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

public class XMLWriterSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int MODULES_ALL = 0;
	public static final int MODULES_ONE = 1;
	public static final int MODULES_CHAIN = 2;
	public int moduleSelection = MODULES_ALL;
	public boolean nonModuleData = false;
	public boolean includeConstants = false;
	public boolean writeShortNames = true;
	public boolean writeToolTips = false;
	public String selectedModuleType, selectedModuleName;
	
	public boolean writeAtStart;
	

	@Override
	protected XMLWriterSettings clone() {
		try {
			return (XMLWriterSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}


}
