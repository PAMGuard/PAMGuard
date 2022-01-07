package PamView.paneloverlay;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import PamView.PamSymbolSelector;
import PamView.symbol.PamSymbolChooser;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelector;

public class OverlayCheckboxMenuSelect implements ActionListener {

	private OverlayDataInfo overlayDataInfo;
	private DataSelector dataSelector;
	private Window javaFrame;
	private OverlayDataObserver dataObserver;
	private PamDataBlock dataBlock;
	private PamSymbolChooser symbolChooser;

	public OverlayCheckboxMenuSelect(Window javaFrame, OverlayDataObserver dataObserver, 
			PamDataBlock dataBlock, OverlayDataInfo mapDetectionData, DataSelector dataSelector, PamSymbolChooser symbolChooser) {
		this.javaFrame = javaFrame;
		this.dataObserver = dataObserver;
		this.dataBlock = dataBlock;
		this.overlayDataInfo = mapDetectionData;
		this.dataSelector = dataSelector;
		this.symbolChooser = symbolChooser;
	}

	public void actionPerformed(ActionEvent e) {
		OverlayCheckboxMenuItem menuItem = (OverlayCheckboxMenuItem) e.getSource();
		Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		Point menuPos = menuItem.getLocOnScreen();
		boolean iconClicked = false;
		if (menuItem.getIcon() != null && menuPos != null) {
			int w = menuItem.getIcon().getIconWidth();
			if (mousePoint.x-menuPos.x < w + menuItem.getIconTextGap()) {
				iconClicked = true;
			}
		}
		if (iconClicked) {
			menuItem.setSelected(!menuItem.isSelected());
			if (dataSelector == null && symbolChooser == null) {
				return;
			}
			DataSelectDialog dataSelectDialog = new DataSelectDialog(javaFrame, dataBlock, dataSelector, symbolChooser);
			boolean ok = dataSelectDialog.showDialog();
			if (ok) {
				menuItem.setSelected(true);
				dataObserver.selectionChanged(dataBlock, true);
			}
//			if (dataSelector.showSelectDialog(javaFrame)) {
//				menuItem.setSelected(true);
//				dataObserver.selectionChanged(dataBlock, true);
//				//				simpleMapRef.mapDetectionsManager.setShouldPlot(menuItem.getText(), true);
//				//				simpleMapRef.checkViewerData();
//				//				updateObservers();
//				//				createKey();
//			}
		}
		else {
			dataObserver.selectionChanged(dataBlock, menuItem.isSelected());
			//			simpleMapRef.mapDetectionsManager.setShouldPlot(menuItem.getText(), menuItem.isSelected());
			//			simpleMapRef.checkViewerData();
			//			updateObservers();
			//			createKey();
		}
	}
}

