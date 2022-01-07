package annotationMark.spectrogram;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import annotation.handler.AnnotationsSelectionPanel;

public class SpectrogramMarkDialog extends PamDialog {

	private static boolean dialogOk;
	private SpectrogramAnnotationModule spectrogramAnnotationModule;
	private SpectrogramMarkAnnotationHandler annotationHandler;
	private AnnotationsSelectionPanel annotationPanel;
	
	private static SpectrogramMarkDialog singleInstance;

	private SpectrogramMarkDialog(Window parentFrame, SpectrogramAnnotationModule spectrogramAnnotationModule) {
		super(parentFrame, spectrogramAnnotationModule.getUnitName() + " settings", false);
		this.spectrogramAnnotationModule = spectrogramAnnotationModule;
		this.annotationHandler = spectrogramAnnotationModule.getAnnotationHandler();
		annotationPanel = annotationHandler.getSelectionPanel();
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Annotations"));
		mainPanel.add(BorderLayout.CENTER, annotationPanel.getDialogComponent());
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
	}

	@Override
	public boolean getParams() {
		dialogOk = annotationPanel.getParams();
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
