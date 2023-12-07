package tethys.swing.export;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import nilus.ContactInfo.Address;
import nilus.ResponsibleParty;

/**
 * Simple Swing panel for responsibleparty fields. 
 * @author dg50
 *
 */
public class ResponsiblePartyPanel {

	private JTextField name, organisation, position, email;
	
	private JPanel mainPanel;

	public ResponsiblePartyPanel() {
		this(null);
	}
	/**
	 * 
	 */
	public ResponsiblePartyPanel(String borderTitle) {
		super();
		mainPanel = new JPanel(new GridBagLayout());
		if (borderTitle != null)
		mainPanel.setBorder(new TitledBorder(borderTitle));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Name ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(name = new JTextField(40), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Organisation ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(organisation = new JTextField(30), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Position ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(position = new JTextField(30), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Email ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(email = new JTextField(30), c);
		c.gridx = 0;
		c.gridy++;
		
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void setParams(ResponsibleParty responsibleParty) {
		if (responsibleParty == null) {
			return;
		}
		name.setText(responsibleParty.getIndividualName());
		organisation.setText(responsibleParty.getOrganizationName());
		position.setText(responsibleParty.getPositionName());

		Address addr = responsibleParty.getContactInfo().getAddress();
		if (addr != null) {
			email.setText(addr.getElectronicMailAddress());
		}
		
	}
	
	
	public boolean getParams(ResponsibleParty responsibleParty) {
		responsibleParty.setIndividualName(name.getText());
		responsibleParty.setOrganizationName(organisation.getText());
		responsibleParty.setPositionName(position.getText());

		Address addr = responsibleParty.getContactInfo().getAddress();
		if (addr == null) {
			addr = new Address();
			responsibleParty.getContactInfo().setAddress(addr);
		}
		addr.setElectronicMailAddress(email.getText());
		return true;
	}
}
