package tethys.calibration.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jdesktop.swingx.JXDatePicker;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.wizard.PamWizard;
import nilus.Calibration;
import nilus.ContactInfo;
import nilus.MetadataInfo;
import nilus.ResponsibleParty;
import tethys.TethysTimeFuncs;
import tethys.calibration.CalibrationHandler;

public class CalibrationsContactCard extends CalibrationsCard {

	private JXDatePicker datePicker;
	
	private JTextField individual, organisation, position, email;
	
	private JComboBox<String> updateInterval;
	
	public CalibrationsContactCard(PamWizard pamWizard) {
		super(pamWizard, "Contact Details");
		// TODO Auto-generated constructor stub
//		setBorder(new TitledBorder("Contact"));
		setLayout(new BorderLayout());
		
		updateInterval = new JComboBox<>();
		String[] vals = CalibrationHandler.updateOptions;
		for (int i = 0; i < vals.length; i++) {
			updateInterval.addItem(vals[i]);
		}
		
		JPanel datePanel = new JPanel(new GridBagLayout());
		datePanel.setBorder(new TitledBorder("Calibration date"));
		add(BorderLayout.NORTH, datePanel);
		GridBagConstraints c = new PamGridBagContraints();
		datePanel.add(new JLabel("Calibration date: ", JLabel.RIGHT), c);
		datePicker = new JXDatePicker();
		c.gridx++;
		datePanel.add(datePicker, c);
		c.gridx = 0;
		c.gridy++;
		datePanel.add(new JLabel("Update Frequency", JLabel.RIGHT), c);
		c.gridx++;
		datePanel.add(updateInterval, c);
		
		
		JPanel contactPanel = new JPanel(new GridBagLayout());
		contactPanel.setBorder(new TitledBorder("Contact"));
		this.add(BorderLayout.CENTER, contactPanel);
		c = new PamGridBagContraints();
		contactPanel.add(new JLabel("Individual Name "), c);
		c.gridx++;
		contactPanel.add(individual = new JTextField(15), c);
		c.gridx = 0;
		c.gridy++;
		contactPanel.add(new JLabel("Organisation "), c);
		c.gridx++;
		contactPanel.add(organisation = new JTextField(15), c);
		c.gridx = 0;
		c.gridy++;
		contactPanel.add(new JLabel("Position "), c);
		c.gridx++;
		contactPanel.add(position = new JTextField(15), c);
		c.gridx = 0;
		c.gridy++;
		contactPanel.add(new JLabel("Email "), c);
		c.gridx++;
		contactPanel.add(email = new JTextField(15), c);
		c.gridx = 0;
		c.gridy++;
		
	}

	@Override
	public boolean getParams(Calibration cardParams) {
		if (cardParams == null) {
			return false;
		}
		MetadataInfo metaInf = cardParams.getMetadataInfo();
		if (metaInf == null) {
			metaInf = new MetadataInfo();
			cardParams.setMetadataInfo(metaInf);
		}
		ResponsibleParty contact = metaInf.getContact();
		if (contact == null) {
			contact = new ResponsibleParty();
			metaInf.setContact(contact);
		}
		ContactInfo contactInfo = contact.getContactInfo();
		if (contactInfo == null) {
			contactInfo = new ContactInfo();
			contact.setContactInfo(contactInfo);
		}
		
		// so far as I'm aware, the meta info contains the time we create this record
		// and the other timestamp is the data the calibration was donw. 
		metaInf.setDate(TethysTimeFuncs.xmlGregCalFromMillis(System.currentTimeMillis()));
		metaInf.setUpdateFrequency((String) updateInterval.getSelectedItem());
		
		contact.setIndividualName(individual.getText());
		contact.setOrganizationName(organisation.getText());
		contact.setPositionName(position.getText());
		contactInfo.setContactInstructions(email.getText());
		
		// and set this both as the RepsonsiblePArty and in the metadata. 
		cardParams.setResponsibleParty(contact);
		
		Date date = datePicker.getDate();
		if (date == null) {
			return getPamWizard().showWarning("You must specify the data of the calibration");
		}
		long millis = date.getTime();
		cardParams.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(millis));
		
		return true;
	}
	
	private ResponsibleParty findResponsibleParty(Calibration cal) {
		if (cal == null) {
			return null;
		}
		MetadataInfo metaInfo = cal.getMetadataInfo();
		if (metaInfo != null) {
			ResponsibleParty resp = metaInfo.getContact();
			if (resp != null && resp.getIndividualName() != null) {
				return resp;
			}
		}
		return cal.getResponsibleParty();
		
	}

	@Override
	public void setParams(Calibration cardParams) {
		// fill in as much as possible from the existing Calibration
		ResponsibleParty resp = findResponsibleParty(cardParams);
		if (resp != null) {
			individual.setText(resp.getIndividualName());
			organisation.setText(resp.getOrganizationName());
			position.setText(resp.getPositionName());
			ContactInfo cInf = resp.getContactInfo();
			if (cInf != null) {
				email.setText(cInf.getContactInstructions());
			}
		}
		
		MetadataInfo metaInf = cardParams.getMetadataInfo();
		if (metaInf != null) {
			String uf = metaInf.getUpdateFrequency();
			if (uf != null) {
				updateInterval.setSelectedItem(uf);
			}
		}
		
		XMLGregorianCalendar ts = cardParams.getTimeStamp();
		if (ts != null) {
			datePicker.setDate(new Date(TethysTimeFuncs.millisFromGregorianXML(ts)));
		}
		

	}

}
