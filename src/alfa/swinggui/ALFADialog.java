package alfa.swinggui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.dataSelector.DataSelector;
import alfa.ALFAControl;
import alfa.ALFAParameters;
import loggerForms.FormDescription;

public class ALFADialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;

	private static ALFADialog singleInstance;

	private ALFAControl alfaControl;
	
	private ALFAParameters alfaParameters;
	
	private JTextField effIntNoWhales;
	
//	private JTextField effIntWhales;
	private JTextField histosPerInterval, binsPerHisto;
	
	private JCheckBox reloadOldReports;
	
	private JCheckBox launchScreenMirror;
	
	private PamDialogPanel loggerPanel;

	private PamDialogPanel loggerDialogPanel;
	
	private JRadioButton loggerComma, loggerJson, loggerXML;

	private JRadioButton jRadioButtonClks;

	private JRadioButton jRadioButtonTrns;
	
	private JCheckBox followOffline;
	
	private static final int TSCALE = 60;

	private ALFADialog(Window parentFrame, ALFAControl alfaControl) {
		super(parentFrame, alfaControl.getUnitName() + " options", true);
		this.alfaControl = alfaControl;
		JTabbedPane tabPane = new JTabbedPane();
		JPanel reportPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		reportPanel.setBorder(new TitledBorder("Effort Intervals"));
		
		c.gridwidth = 3;
		reportPanel.add(new JLabel("DataType ", JLabel.LEFT), c);
		c.gridy++;
        jRadioButtonClks = new JRadioButton("No. Clicks in Trains"); 
		reportPanel.add(jRadioButtonClks, c);
        c.gridy++;
        jRadioButtonTrns= new JRadioButton("Click Trains"); 
		reportPanel.add(jRadioButtonTrns, c);
        c.gridy++;
        
        ButtonGroup group = new ButtonGroup();
        group.add(jRadioButtonClks);
        group.add(jRadioButtonTrns);
		
		c.gridwidth = 3;
		reportPanel.add(new JLabel("Standard Reporting Intervals ", JLabel.LEFT), c);
		c.gridwidth = 1;
		c.gridy++;
		reportPanel.add(new JLabel("Reporting interval ", JLabel.RIGHT), c);
		c.gridx++;
		reportPanel.add(effIntNoWhales = new JTextField(5), c);
		c.gridx++;
		reportPanel.add(new JLabel( "(minutes)"), c);
		c.gridx = 0;
		c.gridy++;
		reportPanel.add(new JLabel("Angle Histograms ", JLabel.RIGHT), c);
		c.gridx++;
		reportPanel.add(histosPerInterval = new JTextField(5), c);
		c.gridx++;
		reportPanel.add(new JLabel( "(per report)"), c);
		c.gridx = 0;
		c.gridy++;
		reportPanel.add(new JLabel("Bins per Histogram ", JLabel.RIGHT), c);
		c.gridx++;
		reportPanel.add(binsPerHisto = new JTextField(5), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		reportPanel.add(reloadOldReports = new JCheckBox("Reload old reports at start up"), c);
		tabPane.add("Messaging", reportPanel);
		
		DataSelector loggerformsSelector = alfaControl.getLoggerMonitor().getFormsSelector();
		JPanel logStPanel = new JPanel(new GridBagLayout());
		GridBagConstraints lc = new PamGridBagContraints();
		loggerComma = new JRadioButton("Comma separated");
		loggerJson = new JRadioButton("JSON String");
		loggerXML = new JRadioButton("XML");
		ButtonGroup bg = new ButtonGroup();
		bg.add(loggerComma);
		bg.add(loggerJson);
		bg.add(loggerXML);
		logStPanel.setBorder(new TitledBorder("Message Style"));
		logStPanel.add(loggerComma, c);
		c.gridy++;
		logStPanel.add(loggerJson,c);
//		c.gridy++;
//		logStPanel.add(loggerXML,c);
		if (loggerformsSelector != null) {
			loggerDialogPanel = loggerformsSelector.getDialogPanel();
			JPanel lp = new JPanel();
			lp.setLayout(new BoxLayout(lp, BoxLayout.Y_AXIS));
			lp.add(logStPanel);
			lp.add(loggerDialogPanel.getDialogComponent());
			tabPane.addTab("Logger Forms", lp);
		}
		
		JPanel dispPanel = new JPanel(new GridBagLayout());
		dispPanel.setBorder(new TitledBorder("Display Options"));
		c = new PamGridBagContraints();
		dispPanel.add(launchScreenMirror = new JCheckBox("Launch Screen Mirror"), c);
		tabPane.add("Display", dispPanel);
		
		followOffline = new JCheckBox("Follow data offline");
		followOffline.setToolTipText("Automaticlly check server for new data arrival, update data map and display");
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			JPanel offlinePanel = new JPanel(new GridBagLayout());
			offlinePanel.setBorder(new TitledBorder("Offline"));
			c = new PamGridBagContraints();
			offlinePanel.add(followOffline, c);
			tabPane.add("Offline", offlinePanel);
		}
		
		effIntNoWhales.setToolTipText("Interval for sending ship track data ");
//		effIntWhales.setToolTipText("Interval for sending data when whale have been detected");
		histosPerInterval.setToolTipText("Number of angle histograms per reporting interval");
		binsPerHisto.setToolTipText("Number of angle bins per histogram");
		reloadOldReports.setToolTipText("Reload old reports when the system restarts");
		
		setDialogComponent(tabPane);
	}
	
	public static ALFAParameters showDialog(Window parentFrame, ALFAControl alfaControl) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.alfaControl != alfaControl) {
			singleInstance = new ALFADialog(parentFrame, alfaControl);
//		}
		singleInstance.setParams(alfaControl.getAlfaParameters().clone());
		singleInstance.setVisible(true);
		return singleInstance.alfaParameters;
	}

	private void setParams(ALFAParameters alfaParameters) {
		this.alfaParameters = alfaParameters;
		
		effIntNoWhales.setText(String.format("%d", alfaParameters.effortMsgIntervalNoWhales/TSCALE));
		histosPerInterval.setText(String.format("%d", alfaParameters.histosPerReportInterval));
		binsPerHisto.setText(String.format("%d", alfaParameters.getBinsPerhistogram()));
		reloadOldReports.setSelected(alfaParameters.reloadOldReports);
		
		loggerComma.setSelected(alfaParameters.loggerFormFormat == FormDescription.LOGGER_FORMS_COMMA);
		loggerJson.setSelected(alfaParameters.loggerFormFormat == FormDescription.LOGGER_FORMS_JSON);
		loggerXML.setSelected(alfaParameters.loggerFormFormat == FormDescription.LOGGER_FORMS_XML);
		
		if (loggerDialogPanel != null) {
			loggerDialogPanel.setParams();
		}
		
		jRadioButtonClks.setSelected(!alfaParameters.useClkTrains);
		
		launchScreenMirror.setSelected(alfaParameters.autoScreenMirror);
		followOffline.setSelected(alfaParameters.followOffline);
		
		this.pack();
	}

	@Override
	public boolean getParams() {
		try {
			int inval = Integer.valueOf(effIntNoWhales.getText());
			alfaParameters.effortMsgIntervalNoWhales = inval*TSCALE;
			inval = Integer.valueOf(histosPerInterval.getText());
			alfaParameters.histosPerReportInterval = inval;
			int bins = Integer.valueOf(binsPerHisto.getText());
			alfaParameters.setBinsPerhistogram(bins);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid interval data, must be an Integer (whole) number"); 
		}
		if (loggerDialogPanel != null) {
			if (loggerDialogPanel.getParams() == false) {
				return showWarning("Error in logger forms selection");
			}
		}
		alfaParameters.reloadOldReports = reloadOldReports.isSelected();
		alfaParameters.autoScreenMirror = launchScreenMirror.isSelected();
		
		if (loggerJson.isSelected()) {
			alfaParameters.loggerFormFormat = FormDescription.LOGGER_FORMS_JSON;
		}
		else if (loggerComma.isSelected()) {
			alfaParameters.loggerFormFormat = FormDescription.LOGGER_FORMS_COMMA;
		}
		else if (loggerXML.isSelected()) {
			alfaParameters.loggerFormFormat = FormDescription.LOGGER_FORMS_XML;
		}
		alfaParameters.followOffline = followOffline.isSelected();	
		
		
		if (this.jRadioButtonClks.isSelected()) alfaParameters.useClkTrains = false; 
		else alfaParameters.useClkTrains = true;
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		alfaParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new ALFAParameters());
	}


}
