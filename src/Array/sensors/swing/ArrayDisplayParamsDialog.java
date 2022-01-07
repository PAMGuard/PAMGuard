package Array.sensors.swing;

import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import PamView.dialog.PamDialog;

public class ArrayDisplayParamsDialog extends PamDialog {

	protected ArrayDisplayParameters currentParams;
	
	private PitchRollDialogPanel pitchRoll;
	
	private HeadingDialogPanel heading;

	private ArrayDisplayParamsDialog(Window parentFrame, ArrayDisplayParamsProvider paramsProvider) {
		super(parentFrame, "Display options", true);
		currentParams = paramsProvider.getDisplayParameters();
		heading = new HeadingDialogPanel(this);
		pitchRoll = new PitchRollDialogPanel(this);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(heading.getDialogComponent());
		mainPanel.add(pitchRoll.getDialogComponent());
		
		
		setDialogComponent(mainPanel);
	}

	public static boolean showDialog(Window parentFrame, ArrayDisplayParamsProvider paramsProvider) {
		ArrayDisplayParamsDialog singleInstance = new ArrayDisplayParamsDialog(parentFrame, paramsProvider);
		singleInstance.setParams();
		singleInstance.setVisible(true);
		if (singleInstance.currentParams != null) {
			paramsProvider.setDisplayParameters(singleInstance.currentParams);
		}
		return singleInstance.currentParams != null;
	}
	
	
	private void setParams() {
		heading.setParams();
		pitchRoll.setParams();
	}

	@Override
	public boolean getParams() {
		boolean ok = true;
		ok &= heading.getParams();
		ok &= pitchRoll.getParams();
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		currentParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		currentParams = new ArrayDisplayParameters();
		setParams();
	}

}
