package clickDetector.offlineFuncs;


import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.OverlayDataManager;
import PamView.paneloverlay.overlaymark.BasicMarkDataSelector;
import PamView.paneloverlay.overlaymark.GeneralMarkDialog;
import PamView.paneloverlay.overlaymark.MarkDataSelector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMarkDataInfo;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import Spectrogram.SpectrogramDisplay;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import detectiongrouplocaliser.DetectionGroupSummary;
import fftManager.FFTDataBlock;
import javafx.scene.input.MouseEvent;
import warnings.PamWarning;
import warnings.WarningSystem;

public class ClickMarkHandler implements OverlayMarkObserver {

	private ClickControl clickControl;
	private MarkDataSelector markDataSelector;
	private static PamWarning MarkWarning = new PamWarning("Click Detector", "", 2);
	
	/**
	 * @param clickControl
	 */
	public ClickMarkHandler(ClickControl clickControl) {
		super();
		this.clickControl = clickControl;
		markDataSelector = new ClickMarkSelector();
	}

	@Override
	public boolean markUpdate(int markStatus, MouseEvent mouseEvent, OverlayMarker overlayMarker,
			OverlayMark overlayMark) {
		if (markStatus != MARK_END) {
			return false;
		}
		
		// quick check of the source - if this is a spectrogram display and the underlying source data block contains sequence numbers, ignore the mark
		if (overlayMark.getMarkSource() instanceof SpectrogramDisplay) {
			FFTDataBlock source = ((SpectrogramDisplay) overlayMark.getMarkSource()).getSourceFFTDataBlock();
			if (source.getSequenceMapObject()!=null) {
				String err = "Error: this Spectrogram uses Beamformer data as it's source, and Beamformer output does not contain "
						+ "the link back to a single channel of raw audio data that the Click Detector requires.  You will not be able to select detections "
						+ "until the source is changed";
				MarkWarning.setWarningMessage(err);
				WarningSystem.getWarningSystem().addWarning(MarkWarning);
				return true; // return true to indicate that the mark has been consumed
			}
		}
		WarningSystem.getWarningSystem().removeWarning(MarkWarning);

		List<PamDataUnit> selData = overlayMarker.getSelectedMarkedDataUnits(overlayMark, markDataSelector);
		if (selData == null || selData.size() == 0) {
			return false;
		}
//		System.out.printf("Overlay mark received on click detector with %d data units\n", selData.size());
//		//TEMP
//		for (int i=0; i<selData.size(); i++){
//			System.out.println(PamCalendar.formatDateTime(selData.get(i).getTimeMilliseconds())); 
//		}
		
		ClicksOffline clicksOffline = clickControl.getClicksOffline();
		JPopupMenu popupMenu = new JPopupMenu();
		if (clicksOffline != null) {
			clicksOffline.addBTMenuItems(popupMenu, overlayMark, selData, false, null);
		}
		Point mousePoint = OverlayMark.getSwingComponentMousePos(clickControl.getGuiFrame(), mouseEvent);
		popupMenu.show(clickControl.getGuiFrame(), mousePoint.x, mousePoint.y);
		return true;
	}

	@Override
	public JPopupMenu getPopupMenuItems(DetectionGroupSummary markSummaryData) {
		ClicksOffline clicksOffline = clickControl.getClicksOffline();
		if (clicksOffline == null) {
			return null;
		}
		if (markSummaryData == null || markSummaryData.getNumDataUnits() == 0) {
			return null;
		}
		JPopupMenu popupMenu = new JPopupMenu(clickControl.getUnitName());
		
		List<PamDataUnit> dataList = markSummaryData.getDataList();
		PamDataUnit singleUnit = null;
		if (dataList != null && dataList.size() == 1) {
			singleUnit = dataList.get(0);
		}
		
		clicksOffline.addBTMenuItems(popupMenu, markSummaryData.getOverlayMark(), markSummaryData.getDataList(), false, singleUnit);
		
		return popupMenu;
	}

	@Override
	public ParameterType[] getRequiredParameterTypes() {
		return null;
	}

	@Override
	public String getObserverName() {
		return clickControl.getUnitName();
	}
	
	/**
	 * Get a single menu item to include in the greater click detector menu. 
	 * @param parentFrame
	 * @return menu item. 
	 */
	public JMenuItem getMarkOptionsMenuItem(Frame parentFrame) {
		JMenuItem jm = new JMenuItem("Mark observer options");
		jm.setToolTipText("Control which data from marked displays are added to click detector events");
		jm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMarkOptions(parentFrame);
			}
		});
		return jm;
	}

	protected void showMarkOptions(Frame parentFrame) {
		OverlayDataManager overlayDataManager = (OverlayDataManager) markDataSelector;
		GeneralMarkDialog.showDialog(parentFrame, this, overlayDataManager, null);
		
	}
	
	private String getClickDataSelectorName() {
		return clickControl.getUnitName() + " Overlay Marks";
	}


	private class ClickMarkSelector extends BasicMarkDataSelector {

		public ClickMarkSelector() {
			super(getRequiredParameterTypes(), null); // accept data from any type of mark. 
		}

		@Override
		public void selectionChanged(PamDataBlock dataBlock, boolean selected) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getDataSelectorName() {
			return getClickDataSelectorName();
		}

		@Override
		public OverlayMarkDataInfo getOverlayInfo(PamDataBlock dataBlock) {
			return clickControl.getClickParameters().getOverlayMarkDataSelectorParams().getOverlayInfo(dataBlock);
		}
		
	}

	@Override
	public MarkDataSelector getMarkDataSelector(OverlayMarker overlayMarker) {
		return markDataSelector;
	}

	@Override
	public String getMarkName() {
		// TODO Auto-generated method stub
		return null;
	}
}
