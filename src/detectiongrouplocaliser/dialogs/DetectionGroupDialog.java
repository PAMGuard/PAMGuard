package detectiongrouplocaliser.dialogs;

import java.awt.Window;

import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.panel.PamNorthPanel;
import PamView.paneloverlay.OverlaySwingPanel;
import PamView.paneloverlay.overlaymark.MarkProvidersPanel;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationHandler;
import annotation.handler.AnnotationsSelectionPanel;
import dataPlotsFX.overlaymark.OverlayMarkerManager;
import detectiongrouplocaliser.DetectionGroupControl;
import detectiongrouplocaliser.DetectionGroupProcess;

public class DetectionGroupDialog extends PamDialog {
	
	private boolean okPressed;
	
	private static DetectionGroupDialog singleInstance;
	
	private DetectionGroupControl detectionGroupControl;

	private OverlaySwingPanel dataSelector;

	private AnnotationChoiceHandler annotationHandler;

	private AnnotationsSelectionPanel annotationPanel;

	private MarkProvidersPanel markProvidersPanel;

	private DetectionGroupDialog(Window parentFrame, DetectionGroupControl detectionGroupControl) {
		super(parentFrame, detectionGroupControl.getUnitName() + " settings", false);
		this.detectionGroupControl = detectionGroupControl;
		DetectionGroupProcess process = detectionGroupControl.getDetectionGroupProcess();
		annotationHandler = process.getAnnotationHandler();
		JTabbedPane tabbedPane = new JTabbedPane();

		markProvidersPanel = new MarkProvidersPanel(detectionGroupControl.getDetectionGroupProcess().getMarkObserver(), null);
		tabbedPane.addTab("Markers", markProvidersPanel.getDialogComponent());
		markProvidersPanel.getDialogComponent().setBorder(new TitledBorder("Select Displays"));
		
		dataSelector = process.getDataSelector().getSwingPanel(parentFrame);
		tabbedPane.addTab("Data selection", new PamNorthPanel(dataSelector.getDialogComponent()));
		dataSelector.getDialogComponent().setBorder(new TitledBorder("Select Data"));
		
		annotationPanel = annotationHandler.getSelectionPanel();
		tabbedPane.addTab("Annotations", new PamNorthPanel(annotationPanel.getDialogComponent()));
		annotationPanel.getDialogComponent().setBorder(new TitledBorder("Select Annotations"));
		
		
		setHelpPoint("localisation.detectiongroup.docs.dglocaliser");
		setDialogComponent(tabbedPane);
		// TODO Auto-generated constructor stub
	}

	public static boolean showDialog(Window parentFrame, DetectionGroupControl detectionGroupControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.detectionGroupControl != detectionGroupControl) {
			singleInstance = new DetectionGroupDialog(parentFrame, detectionGroupControl);
		}
		singleInstance.setParams();
		singleInstance.pack();
		singleInstance.setVisible(true);
		return singleInstance.okPressed;
	}
	
	private void setParams() {
		markProvidersPanel.setParams();
		dataSelector.setParams();
		annotationPanel.setParams();
	}

	@Override
	public boolean getParams() {
		if (markProvidersPanel.getParams() == false) {
			return false;
		}
		dataSelector.getParams();
		if (annotationPanel.getParams() == false) {
			return showWarning("Invalid annotation choise selection");
		};
		okPressed = true;
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		okPressed = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
