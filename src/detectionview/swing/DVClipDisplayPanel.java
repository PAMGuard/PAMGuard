package detectionview.swing;

import java.awt.BorderLayout;

import PamguardMVC.PamDataBlock;
import clipgenerator.clipDisplay.ClipDisplayPanel;
import clipgenerator.clipDisplay.ClipDisplayParent;
import detectionview.DVControl;
import detectionview.DVObserver;
import detectionview.LoadProgress;
import pamScrollSystem.ScrollPaneAddon;

public class DVClipDisplayPanel extends ClipDisplayPanel implements DVObserver {

	private DVControl dvControl;
	
	private ClipProgressPanel clipProgressPanel;

	public DVClipDisplayPanel(DVControl dvControl) {
		super(dvControl);
		this.dvControl = dvControl;
		clipProgressPanel = new ClipProgressPanel(dvControl);
		getDisplayPanel().add(BorderLayout.SOUTH, clipProgressPanel.getPanel());
		
		dvControl.addObserver(this);
		updateConfig();
	}

	@Override
	public void closeComponent() {
		super.closeComponent();
		dvControl.removeObserver(this);
	}

	@Override
	public void updateData(int updateType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateConfig() {
		ScrollPaneAddon scrollButtons = getDisplayControlPanel().getScrollButtons();
		if (scrollButtons != null) {
			scrollButtons.removeAllDataBlocks();			
		}
		PamDataBlock detBlock = dvControl.getDvProcess().getDetectorDataBlock();
		if (detBlock != null) {
			scrollButtons.addDataBlock(detBlock);
		}
	}

	@Override
	public void loadProgress(LoadProgress loadProgress) {
		if (loadProgress.getLoadState() == LoadProgress.LOAD_DONE) {
			newViewerTimes(0, Long.MAX_VALUE);
		}
	}

	@Override
	public void newViewerTimes(long start, long end) {
		/**
		 * Don't want to call the super version since that will try to load the DVData which we
		 * don't want to do. We want to load the detector data for this period. 
		 */
		super.newViewerTimes(start, end);
	}

	@Override
	protected void newSampleRate(float sampleRate) {
		if (dvControl != null) {
			sampleRate = dvControl.getDvProcess().getSampleRate();
		}
		super.newSampleRate(sampleRate);
	}

	@Override
	public float getSampleRate() {
		if (dvControl != null) {
			return dvControl.getDvProcess().getSampleRate();
		}
		else {
			return super.getSampleRate();
		}
	}



}
