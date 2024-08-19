package export.wavExport;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;

/**
 * 
 */
public class WavOptionsPanel extends PamPanel {
	
	private static final long serialVersionUID = 1L;
	
	
	private JRadioButton zeroPad;


	private JRadioButton noZeroPad;


	private JRadioButton indvidualWav; 

	
	public WavOptionsPanel() {
		super(); 
		
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0; 
		
		this.setLayout((new GridBagLayout()));

		addComponent(this, zeroPad = new JRadioButton("Concat"), c);
		
		c.gridx++;
		
		addComponent(this, noZeroPad = new JRadioButton("Zero pad"), c);

		c.gridx++;
		addComponent(this, indvidualWav = new JRadioButton("Individual"), c);
		
		c.gridx++;
		addComponent(this, new JLabel(" wav"), c);
		
		 // Initialization of object of "ButtonGroup" class. 
		ButtonGroup buttonGroup = new ButtonGroup(); 
		buttonGroup.add(zeroPad);
		buttonGroup.add(noZeroPad);
		buttonGroup.add(indvidualWav);
		
		noZeroPad.setToolTipText(
				  "<html>"
				+ "Concatonate detections within wav files. If selected, then the wav files are concatenated"
		        + "<br>"
				+ "and a seperate text file encodes the detection times - this saves a lot of storage space!"
				+ "</html>");
		
		zeroPad.setToolTipText(
				  "<html>"
				+ "Zero pad wav files. If selected, then the wav files are zero padding between detections "
		        + "<br>"
				+ "so they appear at the right time - this can be very storage space intensive. "
				+ "</html>");
		
		indvidualWav.setToolTipText(
				  "<html>"
				+ "Save each detection as an individual time stamped wav file"
				+ "</html>");
		
	}
	
	public void setParams(WavExportOptions wavExportOptions) {
		switch (wavExportOptions.wavSaveChoice) {
		
		case WavExportOptions.SAVEWAV_CONCAT:
			noZeroPad.setSelected(true);
			break;
		case WavExportOptions.SAVEWAV_ZERO_PAD:
			zeroPad.setSelected(true);
			break;
		case WavExportOptions.SAVEWAV_INDIVIDUAL:
			indvidualWav.setSelected(true);
			break;
		
		}
	
	}
	
	public WavExportOptions getParams(WavExportOptions wavExportOptions) {
		
		if (zeroPad.isSelected()) wavExportOptions.wavSaveChoice = WavExportOptions.SAVEWAV_ZERO_PAD;
		if (noZeroPad.isSelected()) wavExportOptions.wavSaveChoice = WavExportOptions.SAVEWAV_ZERO_PAD;
		if (indvidualWav.isSelected()) wavExportOptions.wavSaveChoice = WavExportOptions.SAVEWAV_ZERO_PAD;

		return wavExportOptions; 
	}

}
