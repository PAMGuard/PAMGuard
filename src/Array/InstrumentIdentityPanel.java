package Array;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamUtils.PamUtils;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;

/**
 * Instrument identity panel, contrians additional fields required by Tethys. 
 * @author dg50
 *
 */
public class InstrumentIdentityPanel {

	private JPanel mainPanel;
	
	private JTextField instrumentId;
	
	private JTextField instrumentType;
	
	public InstrumentIdentityPanel() { 
		mainPanel = new WestAlignedPanel();
		mainPanel.setBorder(new TitledBorder("Instrument information"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Instrument Type ", SwingConstants.RIGHT), c);
		c.gridx++;
		mainPanel.add(instrumentType = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Instrument Id ", SwingConstants.RIGHT), c);
		c.gridx++;
		mainPanel.add(instrumentId = new JTextField(20), c);
		
		instrumentType.setToolTipText("Instrument type, e.g. Towed array, HARP, EAR, Popup, DMON, Rock Hopper, etc.");
		instrumentId.setToolTipText("Instrument identifier, e.g. serial number");
		
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}
	
	public void setParams(PamArray currentArray) {
		if (currentArray == null) {
			currentArray = ArrayManager.getArrayManager().getCurrentArray();
		}
		if (currentArray == null) {
			return;
		}
		instrumentType.setText(currentArray.getInstrumentType());
		instrumentId.setText(currentArray.getInstrumentId());
	}
	
	public void getParams(PamArray currentArray) {
		if (currentArray == null) {
			currentArray = ArrayManager.getArrayManager().getCurrentArray();
		}
		if (currentArray == null) {
			return;
		}
		currentArray.setInstrumentType(PamUtils.trimString(instrumentType.getText()));
		currentArray.setInstrumentId(PamUtils.trimString(instrumentId.getText()));
	}
}
