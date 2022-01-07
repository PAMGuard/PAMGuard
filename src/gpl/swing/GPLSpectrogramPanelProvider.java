package gpl.swing;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import gpl.GPLControlledUnit;

public class GPLSpectrogramPanelProvider implements DisplayPanelProvider {

	private GPLControlledUnit gplControlledUnit;

	public GPLSpectrogramPanelProvider(GPLControlledUnit gplControlledUnit) {
		this.gplControlledUnit = gplControlledUnit;
	}

	@Override
	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new GPLDisplayPanel(gplControlledUnit, this, displayPanelContainer);
	}

	@Override
	public String getDisplayPanelName() {
		return gplControlledUnit.getUnitName();
	}

}
