package clickDetector.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import PamView.dialog.PamDialog;
import clickDetector.ClickParameters;

public class OverlayOptionsDialog extends PamDialog {
	
	private static OverlayOptionsDialog singleInstance;
	
	private ClickParameters clickParameters;
	
	private ClickColoursPanel radarColours, specColours;

	private OverlayOptionsDialog(Window parentFrame) {
		super(parentFrame, "Graphic overlay options", false);
		radarColours = new ClickColoursPanel("Radar Displays");
		specColours = new ClickColoursPanel("Spectrogram Displays");
		
		JPanel p = new JPanel(new BorderLayout());
		JTabbedPane t = new JTabbedPane();
		t.add("Radar", radarColours.getPanel());
		t.add("Spectrogram", specColours.getPanel());
		p.add(BorderLayout.CENTER, t);
//		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//		p.add(radarColours.getPanel());
//		p.add(specColours.getPanel());
		
		setDialogComponent(p);
		
	}
	
	public static ClickParameters showDialog(Window window, ClickParameters clickParameters) {
		if (singleInstance == null || singleInstance.getOwner() != window) {
			singleInstance = new OverlayOptionsDialog(window);
		}
		singleInstance.clickParameters = clickParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clickParameters;
	}

	private void setParams() {
		radarColours.setColour(clickParameters.radarColour);
		specColours.setColour(clickParameters.spectrogramColour);
	}

	@Override
	public void cancelButtonPressed() {
		clickParameters = null;
	}

	@Override
	public boolean getParams() {
		clickParameters.radarColour = radarColours.getColour();
		clickParameters.spectrogramColour = specColours.getColour();
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
