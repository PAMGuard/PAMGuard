package angleMeasurement;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.PamColors;
import PamView.PamSidePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamBorderPanel;

public class AngleSidePanel implements PamSidePanel {

	
	private PamBorderPanel outerPanel;
	private AngleControl angleControl;
	
	private JLabel currentAngle, heldAngle;
	private JButton holdButton;
	private TitledBorder titledBorder;
	
	
	public AngleSidePanel(AngleControl angleControl) {
		super();
		this.angleControl = angleControl;
		JPanel panel = new PamBorderPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		PamDialog.addComponent(panel, currentAngle = new JLabel(" Measurement not available "), c);
		currentAngle.setFont(PamColors.getInstance().getBoldFont());
		currentAngle.setHorizontalAlignment(SwingConstants.CENTER);
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, holdButton = new JButton("Hold"), c);
		holdButton.addActionListener(new HoldButton());
		holdButton.setEnabled(false);
		c.gridx++;
		PamDialog.addComponent(panel, heldAngle = new JLabel("     "), c);
		
		outerPanel = new PamBorderPanel(new BorderLayout());
		outerPanel.setBorder(titledBorder = new TitledBorder(angleControl.getUnitName()));
		outerPanel.add(BorderLayout.CENTER, panel);
	}
	
	private boolean first = true;
	public void newAngle(AngleDataUnit angleDataUnit) {
		currentAngle.setText(String.format("Corrected angle = %.1f\u00B0", angleDataUnit.getTrueHeading()));
		if (first) {
			holdButton.setEnabled(true);
			first = false;
		}
	}

	@Override
	public JComponent getPanel() {
		return outerPanel;
	}

	@Override
	public void rename(String newName) {
		titledBorder.setTitle(newName);
	}
	
	class HoldButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			angleControl.holdButton();
		}
	}

	protected void showHeldAngle(AngleDataUnit angleDataUnit) {
		if (angleDataUnit != null){
			heldAngle.setText(String.format("Held: %.1f\u00B0", angleDataUnit.getTrueHeading()));
		}
		else {
			heldAngle.setText(" ");
		}
	}
}
