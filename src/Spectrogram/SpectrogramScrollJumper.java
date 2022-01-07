package Spectrogram;

import java.awt.Component;
import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import Spectrogram.SpectrogramDisplay.SpectrogramPanel;
import pamScrollSystem.PamScroller;
import pamScrollSystem.jumping.ScrollJumper;

/**
 * Extend ScrollJumper to get data selectors and selected datablocks from 
 * a single spectrogram panel. 
 * @author dg50
 *
 */
public class SpectrogramScrollJumper extends ScrollJumper {

	private SpectrogramDisplay spectrogramDisplay;
	private SpectrogramPanel spectrogramPanel;

	public SpectrogramScrollJumper(SpectrogramDisplay spectrogramDisplay, PamScroller parentScroller, Component mainComponent) {
		super(parentScroller, mainComponent);
		this.spectrogramDisplay = spectrogramDisplay;
	}

	@Override
	public List<PamDataBlock> getUsedDataBlocks() {
		List<PamDataBlock> viewedBlocks = null;
		if (spectrogramPanel != null) {
			viewedBlocks = spectrogramPanel.getViewedDataBlocks();
			return viewedBlocks;
		}
		return super.getUsedDataBlocks();
	}

	@Override
	public DataSelector getDataSelector(PamDataBlock dataBlock) {
		if (spectrogramPanel != null) {
			String selName = spectrogramDisplay.getDataSelectorName(spectrogramPanel.panelId);
			return dataBlock.getDataSelector(selName, false);
		}
		return super.getDataSelector(dataBlock);
	}

	public void setSpectrogramPanel(SpectrogramPanel spectrogramPanel) {
		this.spectrogramPanel = spectrogramPanel;		
	}

}
