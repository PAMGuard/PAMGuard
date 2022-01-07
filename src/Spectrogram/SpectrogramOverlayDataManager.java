package Spectrogram;

import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.OverlayDataInfo;
import PamView.paneloverlay.OverlayDataManager;
import PamguardMVC.PamDataBlock;
import Spectrogram.SpectrogramDisplay.SpectrogramPanel;

public class SpectrogramOverlayDataManager extends OverlayDataManager {

	private SpectrogramDisplay spectrogramDisplay;
	
	private SpectrogramPanel spectrogramPanel;
	
	private static final ParameterType[] axisTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
	private static final ParameterUnits[] axisUnits = {ParameterUnits.SECONDS, ParameterUnits.HZ};

	public SpectrogramOverlayDataManager(SpectrogramDisplay spectrogramDisplay, SpectrogramPanel spectrogramPanel) {
		super(axisTypes, axisUnits);
		this.spectrogramDisplay = spectrogramDisplay;
		this.spectrogramPanel = spectrogramPanel;
	}

	@Override
	public void selectionChanged(PamDataBlock dataBlock, boolean selected) {
		spectrogramDisplay.getOverlayDataInfo(dataBlock, spectrogramPanel.panelId).select = selected;
		spectrogramPanel.subscribeDataBlocks();
		spectrogramPanel.repaint();
	}

	@Override
	public String getDataSelectorName() {
		return spectrogramDisplay.getDataSelectorName(spectrogramPanel.panelId);
	}

	@Override
	public OverlayDataInfo getOverlayInfo(PamDataBlock dataBlock) {
		return spectrogramDisplay.getOverlayDataInfo(dataBlock, spectrogramPanel.panelId);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.OverlayDataManager#getProjector()
	 */
	@Override
	protected GeneralProjector getProjector() {
		return spectrogramPanel.getProjector();
	}
}
