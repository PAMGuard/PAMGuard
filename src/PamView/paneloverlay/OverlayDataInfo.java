package PamView.paneloverlay;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class OverlayDataInfo implements Serializable, Cloneable, ManagedParameters {
	
	static public final long serialVersionUID = 0;
	
	public OverlayDataInfo(String dataName) {
		this.dataName = dataName;
	}

	/**
	 * Data name - gets serialized, then rematched with the 
	 * data block once deserialized. Can't serialise a datablock !
	 */
	public String dataName;
	
	/**
	 * Plot theses data - if false will not plot at all. 
	 */
	public boolean select;
	
	/**
	 * Even though this class is a superclass of others, we don't need to explicitly
	 * add the fields through the 'getSuperclass' modifier because the fields are
	 * all public so will be included when the super class calls autoGenerate on it's own
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
