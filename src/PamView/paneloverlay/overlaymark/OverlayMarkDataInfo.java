package PamView.paneloverlay.overlaymark;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.paneloverlay.OverlayDataInfo;

public class OverlayMarkDataInfo extends OverlayDataInfo implements ManagedParameters {

	private static final long serialVersionUID = 1L;

	public OverlayMarkDataInfo(String dataName) {
		super(dataName);
	}

	/**
	 * Accept data units which only partially overlap a data mark. 
	 */
	public boolean acceptOverlapping;

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}
}
