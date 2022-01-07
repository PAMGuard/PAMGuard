package PamView.paneloverlay.overlaymark;
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
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationsSelectionPanel;
import weka.core.SingleIndex;

public class GeneralMarkDialog  extends PamDialog {

	private static GeneralMarkDialog mapGrouperDialog;
	private MarkProvidersPanel markProvidersPanel;
	private OverlaySwingPanel overlayPanel;
	private boolean okPressed;
	private OverlayMarkObserver markObserver;
	private AnnotationChoiceHandler annotationChoiceHandler;
	private AnnotationsSelectionPanel annotationSelectionPanel;
	private OverlayDataManager overlayDataManager;

	private GeneralMarkDialog(Window parentFrame, OverlayMarkObserver markObserver, 
			OverlayDataManager overlayDataManager, AnnotationChoiceHandler annotationChoiceHandler) {
		super(parentFrame, markObserver.getObserverName() + " options", false);
		this.markObserver = markObserver;
		this.overlayDataManager = overlayDataManager;
		this.annotationChoiceHandler = annotationChoiceHandler;
		JPanel mainPanel = new JPanel();
		JTabbedPane tabbedPane = new JTabbedPane();
		mainPanel.add(tabbedPane);

		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		markProvidersPanel = new MarkProvidersPanel(markObserver, markObserver.getRequiredParameterTypes());
		WestAlignedPanel wap;
		dataPanel.add(wap = new WestAlignedPanel(markProvidersPanel.getDialogComponent()));
		wap.setBorder(new TitledBorder("Observe Marks on ..."));

		if (overlayDataManager != null) {
			overlayPanel = overlayDataManager.getSwingPanel(parentFrame);
			dataPanel.add(wap = new WestAlignedPanel(overlayPanel.getDialogComponent()));
			wap.setBorder(new TitledBorder("Data to select"));
			tabbedPane.add("Data", dataPanel);
		}

		if (annotationChoiceHandler != null) {
			JPanel anPanel = new JPanel();
			anPanel.setLayout(new BoxLayout(anPanel, BoxLayout.Y_AXIS));
			annotationSelectionPanel = annotationChoiceHandler.getSelectionPanel();
			anPanel.add(wap = new WestAlignedPanel(annotationSelectionPanel.getDialogComponent()));
			wap.setBorder(new TitledBorder("Select Annotations"));
			tabbedPane.add("Annotation", anPanel);
		}

		setDialogComponent(mainPanel);
	}

	public static boolean showDialog(Window parentFrame, OverlayMarkObserver markObserver, OverlayDataManager overlayDataManager, AnnotationChoiceHandler annotationChoiceHandler) {
		if (needNew(parentFrame, markObserver, overlayDataManager, annotationChoiceHandler)) {
			mapGrouperDialog = new GeneralMarkDialog(parentFrame, markObserver, overlayDataManager, annotationChoiceHandler);
		}
		mapGrouperDialog.setParams();
		mapGrouperDialog.setVisible(true);
		return mapGrouperDialog.okPressed;
	}

	private static boolean needNew(Window parentFrame, OverlayMarkObserver markObserver, OverlayDataManager overlayDataManager, 
			AnnotationChoiceHandler annotationChoiceHandler) {
		if (mapGrouperDialog == null) {
			return true;
		}
		if (parentFrame != mapGrouperDialog.getOwner()) return true;
		if (mapGrouperDialog.markObserver != markObserver || mapGrouperDialog.overlayDataManager != overlayDataManager || 
				mapGrouperDialog.annotationChoiceHandler != annotationChoiceHandler) {
			return true;
		}
		return false;
	}


	private void setParams() {
		if (markProvidersPanel != null) {
			markProvidersPanel.setParams();
		}
		if (overlayPanel != null) {
			overlayPanel.setParams();
		}
		if (annotationSelectionPanel != null) {
			annotationSelectionPanel.setParams();
		}
		this.pack();
	}

	@Override
	public boolean getParams() {
		if (markProvidersPanel != null) {
			if (markProvidersPanel.getParams() == false) {
				int ans = WarnOnce.showWarning(getOwner(), "Marker Selection", "No marking displays have been selected", WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return false;
				}
			}
		}
		if (overlayPanel != null) {
			if (overlayPanel.getParams() == false) {
				int ans = WarnOnce.showWarning(getOwner(), "Data Selection", "No data types have been selected", WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return false;
				}
			}
		}
		if (annotationSelectionPanel != null) {
			annotationSelectionPanel.getParams();
		}
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

