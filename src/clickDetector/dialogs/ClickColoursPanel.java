package clickDetector.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import clickDetector.BTDisplayParameters;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Simple panel to offer the three basic colour options for clicks - 
 * colour by species, train or both. 
 * @author Doug Gillespie
 *
 */
public class ClickColoursPanel {

	private JPanel panel;
	
	private JRadioButton[] rButtons = new JRadioButton[3];
	
	public ClickColoursPanel(String borderTit) {
		panel = new JPanel(new GridBagLayout());
		if (borderTit != null) {
			panel.setBorder(new TitledBorder(borderTit));
		}
		GridBagConstraints c = new PamGridBagContraints();
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < 3; i++) {
			rButtons[i] = new JRadioButton("Colour by " + BTDisplayParameters.colourNames[i]);
			PamDialog.addComponent(panel, rButtons[i], c);
			bg.add(rButtons[i]);
			c.gridy++;
		}
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	public void setColour(int colour) {
		for (int i = 0; i < 3; i++) {
			rButtons[i].setSelected(colour == i);
		}
	}
	
	public int getColour() {
		for (int i = 0; i < 3; i++) {
			if (rButtons[i].isSelected()) {
				return i;
			}
		}
		return 0;
	}
}
