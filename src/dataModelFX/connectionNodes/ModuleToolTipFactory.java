package dataModelFX.connectionNodes;

import PamController.PamControlledUnit;
import javafx.scene.control.Tooltip;

public class ModuleToolTipFactory {

	public static Tooltip getToolTip(PamControlledUnit pamControlledUnit) {
		
		if (pamControlledUnit.getPamModuleInfo()==null) return null;
		
		return new Tooltip(pamControlledUnit.getPamModuleInfo().getToolTipText());
		
	}

}
