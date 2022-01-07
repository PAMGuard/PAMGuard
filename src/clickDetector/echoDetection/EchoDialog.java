package clickDetector.echoDetection;

import java.awt.Window;

import PamView.dialog.PamDialog;

public class EchoDialog extends PamDialog {
	
	private EchoDetectionSystem echoDetectionSystem;
	
	private EchoDialogPanel echoDialogPanel;
	
	private static EchoDialog echoDialog;

	private EchoDialog(Window parentFrame, EchoDetectionSystem echoDetectionSystem) {
		super(parentFrame, "Echo Detection", false);
		this.echoDetectionSystem = echoDetectionSystem;
		echoDialogPanel = echoDetectionSystem.getEchoDialogPanel();
		setDialogComponent(echoDialogPanel.getDialogComponent());
	}
	
	public static boolean showDialog(Window parentFrame, EchoDetectionSystem echoDetectionSystem) {
		if (echoDialog == null || echoDialog.getOwner() != parentFrame || 
				echoDialog.echoDetectionSystem != echoDetectionSystem) {
			echoDialog = new EchoDialog(parentFrame, echoDetectionSystem);
		}
		echoDialog.echoDialogPanel.setParams();
		echoDialog.setVisible(true);
		return echoDialog.getParams();
	}

	@Override
	public void cancelButtonPressed() {
		return;
	}

	@Override
	public boolean getParams() {
		return echoDialogPanel.getParams();
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	
}
