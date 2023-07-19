package tethys.species.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import PamView.dialog.PamGridBagContraints;
import tethys.species.SpeciesMapItem;

public class SpeciesSubPanel {
	
	private JPanel mainPanel;
	
	private JLabel pamguardName;
	private JTextField itisCode, callType, latinName, commonName;
	private JButton searchButton;

	public SpeciesSubPanel(String aSpecies) {
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Name ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(pamguardName = new JLabel(aSpecies), c);
		c.gridx++;
		mainPanel.add(new JLabel(" ITIS code ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(itisCode = new JTextField(3), c);
		c.gridx ++;
		mainPanel.add(searchButton = new JButton("Find"));
		
		int w1 = 2;
		int w2 = 3;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = w1;
		mainPanel.add(new JLabel("Call / sound type ", JLabel.RIGHT), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = w2;
		mainPanel.add(callType = new JTextField(15), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = w1;
		mainPanel.add(new JLabel("Scientific name ", JLabel.RIGHT), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = w2;
		mainPanel.add(latinName = new JTextField(15), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = w1;
		mainPanel.add(new JLabel("Common name ", JLabel.RIGHT), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = w2;
		mainPanel.add(commonName = new JTextField(15), c);
		
		pamguardName.setToolTipText("Internal name within PAMGuard module");
		itisCode.setToolTipText("ITIS species code");
		searchButton.setToolTipText("Search for species code");
		callType.setToolTipText("Descriptive name for call type or measurement");
		latinName.setToolTipText("Scientific name");
		commonName.setToolTipText("Common name");
		
	}

	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(SpeciesMapItem speciesMapItem) {
		if (speciesMapItem == null) {
			itisCode.setText(null);
			callType.setText(null);
			latinName.setText(null);
			commonName.setText(null);
			return;
		}
		pamguardName.setText(speciesMapItem.getPamguardName());
		itisCode.setText(String.format("%d", speciesMapItem.getItisCode()));
		callType.setText(speciesMapItem.getCallType());
		latinName.setText(speciesMapItem.getLatinName());
		commonName.setText(speciesMapItem.getCommonName());
	}

	public SpeciesMapItem getParams() {
		// TODO Auto-generated method stub
		return null;
	}

}
