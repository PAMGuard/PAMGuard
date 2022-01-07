package dataModelFX;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The primary function of the setting class is to save the positions of the
 * modules nodes. This is done by saving a reference to the module based on unit
 * name, unit type etc. Once a module is initially added to the pane it's
 * position is set using this list.
 * 
 * @author Jamie Macaulay
 *
 */
public class DataModelPaneFXSettings implements Serializable, Cloneable {

	public static final long serialVersionUID = 2L;



	/**
	 * List of the modules which have been used in the data model- this should be
	 * the same as the PamController list but also saves the location of the module.
	 */
	public ArrayList<ConnectionNodeParams> usedModuleInfos = new ArrayList<ConnectionNodeParams>();
	
	
	public DataModelPaneFXSettings(){
		
	}


	@Override
	public DataModelPaneFXSettings clone() {
		try {
			DataModelPaneFXSettings dataModelPaneFXSettings =  (DataModelPaneFXSettings) super.clone();
			dataModelPaneFXSettings.usedModuleInfos = (ArrayList<ConnectionNodeParams>) usedModuleInfos.clone();
			return dataModelPaneFXSettings;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
