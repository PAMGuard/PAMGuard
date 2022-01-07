package Spectrogram;

import PamView.GeneralProjector;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import javafx.scene.input.MouseEvent;

public class SpectrogramMarker extends OverlayMarker {

	private SpectrogramDisplay spectrogramDisplay;
	
	private int panelId;

	public SpectrogramMarker(SpectrogramDisplay spectrogramDisplay, int panelID, int markChannels, GeneralProjector projector) {
		super(spectrogramDisplay, markChannels, projector);
		this.spectrogramDisplay = spectrogramDisplay;
		this.panelId = panelID;
	}

	@Override
	public String getMarkerName() {
		/*
		 * this passes through to the frames unique name. 
		 */
		String theName = spectrogramDisplay.getUnitName() + " (panel " + String.valueOf(panelId) + ")";
		return theName;
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.OverlayMarker#completeMark(javafx.scene.input.MouseEvent)
	 */
	@Override
	protected boolean completeMark(MouseEvent e) {
		boolean ret = super.completeMark(e);
		spectrogramDisplay.repaintAll();
		return ret;
	}

}
