package annotation;

import java.awt.Window;

import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import annotation.handler.AnnotationOptions;

public class AnnotationSettingsDialog extends PamDialog {
	
	private boolean okPressed = false;
	private AnnotationSettingsPanel dialogPanel;
	private DataAnnotationType annotationType;

	
	private AnnotationSettingsDialog(Window parentFrame, DataAnnotationType annotationType) {
		super(parentFrame, annotationType.getAnnotationName() + " config", false);
		this.annotationType = annotationType;
		dialogPanel = annotationType.getSettingsPanel();
		JPanel borderPanel = new JPanel();
		borderPanel.add(dialogPanel.getSwingPanel());
		setDialogComponent(borderPanel);
	}

	public static boolean showDialog(Window parentFrame, DataAnnotationType annotationType) {
		AnnotationSettingsDialog sd = new AnnotationSettingsDialog(parentFrame, annotationType);
		sd.setParams();
		sd.setVisible(true);
		return sd.okPressed;
	}

	private void setParams() {
		dialogPanel.setSettings(annotationType.getAnnotationOptions());
		pack();
	}

	@Override
	public boolean getParams() {
		AnnotationOptions newOptions = dialogPanel.getSettings();
		if (newOptions == null) {
			return false;
		}
		annotationType.setAnnotationOptions(newOptions);
		return okPressed = true;
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
