package clickDetector;

import PamView.paneloverlay.overlaymark.OverlayMarker;

public class BTOverlayMarker extends OverlayMarker {

	private ClickBTDisplay clickBTDisplay;

	public BTOverlayMarker(ClickBTDisplay clickBTDisplay, Object markSource, int markChannels) {
		super(markSource, markChannels, clickBTDisplay.getBTProjector());
		this.clickBTDisplay = clickBTDisplay;
	}

	@Override
	public String getMarkerName() {
		return clickBTDisplay.getUnitName();
	}

}
