package mapgrouplocaliser;

import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamView.GeneralProjector.ParameterType;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.WestAlignedPanel;
import PamView.paneloverlay.OverlayDataManager;
import PamView.paneloverlay.OverlaySwingPanel;
import PamView.paneloverlay.overlaymark.MarkProvidersPanel;
import annotation.handler.AnnotationsSelectionPanel;

public class MapGrouperDialog extends PamDialog {

	private static MapGrouperDialog mapGrouperDialog;
	private MarkProvidersPanel markProvidersPanel;
	private OverlaySwingPanel overlayPanel;
	private boolean okPressed;
	private MapGroupOverlayManager overlayDataManager;
	private MapGroupLocaliserControl mapGroupLocaliserControl;
	private AnnotationsSelectionPanel annotationSelectionPanel;
	
	private MapGrouperDialog(Window parentFrame, MapGroupLocaliserControl mapGroupLocaliserControl, String title) {
		super(parentFrame, title, false);
		this.mapGroupLocaliserControl = mapGroupLocaliserControl;
		this.overlayDataManager = mapGroupLocaliserControl.getMapGroupOverlayManager();
		JPanel mainPanel = new JPanel();
		JTabbedPane tabbedPane = new JTabbedPane();
		mainPanel.add(tabbedPane);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		markProvidersPanel = new MarkProvidersPanel(mapGroupLocaliserControl.getMarkGroupProcess(), overlayDataManager.getParameterTypes());
		WestAlignedPanel wap;
		dataPanel.add(wap = new WestAlignedPanel(markProvidersPanel.getDialogComponent()));
		wap.setBorder(new TitledBorder("Observe Marks on ..."));
		overlayPanel = overlayDataManager.getSwingPanel(parentFrame);
		dataPanel.add(wap = new WestAlignedPanel(overlayPanel.getDialogComponent()));
		wap.setBorder(new TitledBorder("Data to select"));
		tabbedPane.add("Data", dataPanel);
		
		JPanel anPanel = new JPanel();
		anPanel.setLayout(new BoxLayout(anPanel, BoxLayout.Y_AXIS));
		annotationSelectionPanel = mapGroupLocaliserControl.getAnnotationHandler().getSelectionPanel();
		anPanel.add(wap = new WestAlignedPanel(annotationSelectionPanel.getDialogComponent()));
		wap.setBorder(new TitledBorder("Select Annotations"));
		tabbedPane.add("Annotation", anPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static boolean showDialog(Window parentFrame, MapGroupLocaliserControl mapGroupLocaliserControl, String title) {
		if (mapGrouperDialog == null || mapGrouperDialog.mapGroupLocaliserControl != mapGroupLocaliserControl) {
			mapGrouperDialog = new MapGrouperDialog(parentFrame, mapGroupLocaliserControl, title);
		}
		mapGrouperDialog.setParams();
		mapGrouperDialog.setVisible(true);
		return mapGrouperDialog.okPressed;
	}

	private void setParams() {
		markProvidersPanel.setParams();
		overlayPanel.setParams();
		annotationSelectionPanel.setParams();
		this.pack();
	}
	
	@Override
	public boolean getParams() {
		if (markProvidersPanel.getParams() == false) {
			int ans = WarnOnce.showWarning(getOwner(), "Marker Selection", "No marking displays have been selected", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		if (overlayPanel.getParams() == false) {
			int ans = WarnOnce.showWarning(getOwner(), "Data Selection", "No data types have been selected", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		annotationSelectionPanel.getParams();
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
