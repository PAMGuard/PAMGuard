package tethys.calibration.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jdesktop.swingx.JXDatePicker;

import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamView.wizard.PamWizard;
import nilus.Calibration;
import nilus.ContactInfo;
import nilus.MetadataInfo;
import nilus.ResponsibleParty;
import tethys.TethysTimeFuncs;
import tethys.calibration.CalibrationHandler;
import tethys.niluswraps.NilusChecker;
import tethys.swing.export.ResponsiblePartyPanel;

public class CalibrationsContactCard extends CalibrationsCard {

	private JXDatePicker datePicker;
	
	private ResponsiblePartyPanel calibrator, dataManager;
	
	private JComboBox<String> updateInterval;

	private MetadataInfo metaData;
	
	private JButton copyDown, copyUp;
	
	private JComboBox<String> hydrophoneSelection;
	
	public CalibrationsContactCard(PamWizard pamWizard) {
		super(pamWizard, "Contact Details");
		// TODO Auto-generated constructor stub
//		setBorder(new TitledBorder("Contact"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		updateInterval = new JComboBox<>();
		String[] vals = CalibrationHandler.updateOptions;
		for (int i = 0; i < vals.length; i++) {
			updateInterval.addItem(vals[i]);
		}
		
		JPanel datePanel = new JPanel(new GridBagLayout());
		JPanel lp = new WestAlignedPanel(datePanel);
		lp.setBorder(new TitledBorder("Date and hydrophones"));
		GridBagConstraints c = new PamGridBagContraints();
		datePanel.add(new JLabel("Calibration date: ", JLabel.RIGHT), c);
		datePicker = new JXDatePicker();
		c.gridx++;
		datePanel.add(datePicker, c);
//		c.gridx = 0;
		c.gridx++;
		datePanel.add(new JLabel("  Update Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		datePanel.add(updateInterval, c);
		c.gridx = 0;
		c.gridy++;
		datePanel.add(new JLabel("  Hydrophones ", JLabel.RIGHT), c);
		c.gridwidth = 5;
		c.gridx++;
		hydrophoneSelection = new JComboBox<>();
		datePanel.add(hydrophoneSelection, c);
		hydrophoneSelection.setToolTipText("Select which hydrophone calibrations to export");
		
		
		calibrator = new ResponsiblePartyPanel("Technical Person");
		dataManager = new ResponsiblePartyPanel("Data Manager");
		
		JPanel copyPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		copyPanel.add(copyDown = new JButton("Copy down"),c);
		c.gridx++;
		copyPanel.add(copyUp = new JButton("Copy up"), c);

		add(lp);
		add(calibrator.getMainPanel());
		add(copyPanel);
		add(dataManager.getMainPanel());
		
		copyDown.setToolTipText("Copy technical person to data manager");
		copyUp.setToolTipText("Copy data manager to technical person");
		copyDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyRPDown();
			}
		});
		copyUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyRPUp();
			}

		});
	}

	protected void copyRPDown() {
		copyRPData(calibrator, dataManager);
	}
	private void copyRPUp() {
		copyRPData(dataManager, calibrator);
	}

	private void copyRPData(ResponsiblePartyPanel rFrom, ResponsiblePartyPanel rTo) {
		ResponsibleParty rp = checkRPChildren(null);
		rFrom.getParams(rp);
		rTo.setParams(rp);
	}

	@Override
	public boolean getParams(Calibration cardParams) {
		ResponsibleParty rp = checkRPChildren(cardParams.getResponsibleParty());
		cardParams.setResponsibleParty(rp);
		calibrator.getParams(rp);
		
		metaData = cardParams.getMetadataInfo();
		if (metaData == null) {
			metaData = new MetadataInfo();
			cardParams.setMetadataInfo(metaData);
		}
		metaData.setContact(checkRPChildren(metaData.getContact()));
		
		dataManager.getParams(metaData.getContact());
		
		ResponsibleParty metaContact = metaData.getContact();
		NilusChecker.removeEmptyFields(metaData);
		if (metaData.getContact() == null) {
			return PamDialog.showWarning(getPamWizard(), "Missing data", "The Data Manager fields must be completed");
		}		
		
		metaData.setUpdateFrequency((String) updateInterval.getSelectedItem());
		metaData.setDate(TethysTimeFuncs.xmlGregCalFromMillis(System.currentTimeMillis()));
		
		Date date = datePicker.getDate();
		if (date == null) {
			return getPamWizard().showWarning("You must specify the data of the calibration");
		}
		long millis = date.getTime();
		cardParams.setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(millis));
		
		
		checkEmptyFields(rp);
		checkEmptyFields(metaData);
		
		return true;
	}
	
	private ResponsibleParty checkRPChildren(ResponsibleParty rp) {
		if (rp == null) {
			rp = new ResponsibleParty();
		}
		if (rp.getContactInfo() == null) {
			rp.setContactInfo(new ContactInfo());
		}
		if (rp.getContactInfo().getAddress() == null) {
//			rp.getContactInfo().setAddress(new Address());
		}
		return rp;
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
		ResponsibleParty resp = cardParams.getResponsibleParty();
		if (resp != null) {
			calibrator.setParams(resp);
		}
		
		MetadataInfo metaInf = cardParams.getMetadataInfo();
		if (metaInf != null) {
			resp = metaInf.getContact();
			if (resp != null) {
				dataManager.setParams(resp);
			}
			String uf = metaInf.getUpdateFrequency();
			if (uf != null) {
				updateInterval.setSelectedItem(uf);
			}
		}
		
		XMLGregorianCalendar ts = cardParams.getTimeStamp();
		if (ts != null) {
			datePicker.setDate(new Date(TethysTimeFuncs.millisFromGregorianXML(ts)));
		}
		
		hydrophoneSelection.removeAllItems();
		hydrophoneSelection.addItem("All hydrophones");
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		ArrayList<Hydrophone> phones = array.getHydrophoneArray();
		int i = 0;
		for (Hydrophone phone : phones) {
			String txt = String.format("Hydrophone %d, %s, %3.1f dBre1\u00B5Pa", i, phone.getType(), phone.getSensitivity());
			hydrophoneSelection.addItem(txt);
			i++;
		}

	}

}
