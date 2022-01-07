package landMarks;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamUtils.LatLong;
import PamUtils.LatLongDialogStrip;
import PamView.PamSymbolSelector;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class LandmarkDialog extends PamDialog {

	private static LandmarkDialog singleInstance;
	
	private LandmarkControl landmarkControl;
	
	private LandmarkData landmarkData;
	
	LatLongDialogStrip latStrip, longStrip;
	
	JTextField name, height;
	
	PamSymbolSelector pamSymbolSelector;
	
	
	private LandmarkDialog(Window parentFrame,LandmarkControl landmarkControl) {
		super(parentFrame, "Landmark Detail", false);
		this.landmarkControl = landmarkControl;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		addComponent(panel, name = new JTextField(20), c);
		c.gridx  = 0;
		c.gridy++;
		c.gridwidth = 1;
		addComponent(panel, new JLabel("Symbol "), c);
		c.gridx++;
		addComponent(panel, pamSymbolSelector = new PamSymbolSelector(parentFrame), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		addComponent(panel, latStrip = new LatLongDialogStrip(true), c);
		c.gridy++;
		addComponent(panel, longStrip = new LatLongDialogStrip(false), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		addComponent(panel, new JLabel("Height (m) "), c);
		c.gridx++;
		addComponent(panel, height = new JTextField(6), c);
		
		setDialogComponent(panel);
	}
	
	public static LandmarkData showDialog(Window parentFrame, LandmarkControl landmarkControl, LandmarkData landmarkData) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				landmarkControl != singleInstance.landmarkControl) {
			singleInstance = new LandmarkDialog(parentFrame, landmarkControl);
		}
		singleInstance.landmarkData = landmarkData;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.landmarkData;
	}

	@Override
	public void cancelButtonPressed() {
		landmarkData = null;
	}

	private void setParams() {
		if (landmarkData == null) {
			pamSymbolSelector.setCurrentSymbol(landmarkControl.getDefaultSymbol());
			latStrip.sayValue(0);
			longStrip.sayValue(0);
			name.setText("");
			height.setText("0");
		}
		else {
			pamSymbolSelector.setCurrentSymbol(landmarkData.symbol);
			LatLong latLong = landmarkData.latLong;
			if (latLong != null) {
				latStrip.sayValue(latLong.getLatitude());
				longStrip.sayValue(latLong.getLongitude());
			}
			else {
				latStrip.sayValue(0);
				longStrip.sayValue(0);				
			}
			name.setText(landmarkData.name);
			height.setText(String.format("%.1f", landmarkData.height));
		}
		
	}
	
	@Override
	public boolean getParams() {
		
		if (landmarkData == null) {
			landmarkData = new LandmarkData();
		}
		landmarkData.symbol = pamSymbolSelector.getCurrentSymbol();
		if (landmarkData.symbol == null) {
			return false;
		}
		landmarkData.name = name.getText();
		if (landmarkData.latLong == null) {
			landmarkData.latLong = new LatLong();
		}
		landmarkData.latLong.setLatitude(latStrip.getValue());
		landmarkData.latLong.setLongitude(longStrip.getValue());
		try {
			landmarkData.height = Double.valueOf(height.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
