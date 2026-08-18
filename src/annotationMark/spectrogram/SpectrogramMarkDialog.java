package annotationMark.spectrogram;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import annotation.handler.AnnotationsSelectionPanel;

public class SpectrogramMarkDialog extends PamDialog {

	private static boolean dialogOk;
	private SpectrogramAnnotationModule spectrogramAnnotationModule;
	private SpectrogramMarkAnnotationHandler annotationHandler;
	private AnnotationsSelectionPanel annotationPanel;

	/**
	 * Controls whether the annotation dialog opens automatically when a new
	 * annotation is marked out. Null for modules which manage this themselves (see
	 * {@link SpectrogramAnnotationModule#hasShowDialogOption()}).
	 */
	private PamCheckBox showDialogOnNewMark;

	private static SpectrogramMarkDialog singleInstance;

	private SpectrogramMarkDialog(Window parentFrame, SpectrogramAnnotationModule spectrogramAnnotationModule) {
		super(parentFrame, spectrogramAnnotationModule.getUnitName() + " settings", false);
		this.spectrogramAnnotationModule = spectrogramAnnotationModule;
		this.annotationHandler = spectrogramAnnotationModule.getAnnotationHandler();
		annotationPanel = annotationHandler.getSelectionPanel();
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Annotations"));
		mainPanel.add(BorderLayout.CENTER, annotationPanel.getDialogComponent());
		if (spectrogramAnnotationModule.hasShowDialogOption()) {
			showDialogOnNewMark = new PamCheckBox("Show annotation dialog for new annotations");
			showDialogOnNewMark.setToolTipText("<html>When checked, the annotation dialog opens automatically every time a new"
					+ "<br>annotation is marked out on a display. When unchecked, new annotations are"
					+ "<br>stored immediately and can be edited later from their right click menu.</html>");
			mainPanel.add(BorderLayout.SOUTH, showDialogOnNewMark);
		}
		this.setDialogComponent(mainPanel);
	}
	
	public static boolean showDialog(Window parentFrame, SpectrogramAnnotationModule spectrogramAnnotationModule) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner() || singleInstance.spectrogramAnnotationModule != spectrogramAnnotationModule) {
			singleInstance = new SpectrogramMarkDialog(parentFrame, spectrogramAnnotationModule);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.dialogOk;
	}

	private void setParams() {
		annotationPanel.setParams();
		if (showDialogOnNewMark != null) {
			showDialogOnNewMark.setSelected(spectrogramAnnotationModule.getSpecMarkParams().isShowDialogOnNewMark());
		}
	}

	@Override
	public boolean getParams() {
		dialogOk = annotationPanel.getParams();
		if (dialogOk && showDialogOnNewMark != null) {
			spectrogramAnnotationModule.getSpecMarkParams().setShowDialogOnNewMark(showDialogOnNewMark.isSelected());
		}
		return dialogOk;
	}

	@Override
	public void cancelButtonPressed() {
		dialogOk = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
