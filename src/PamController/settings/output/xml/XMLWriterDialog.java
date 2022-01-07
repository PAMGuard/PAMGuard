package PamController.settings.output.xml;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;

/**
 * control options for when to save XML data and also 
 * have buttons for preview and output. 
 * @author dg50
 *
 */
public class XMLWriterDialog extends PamDialog {

	private static XMLWriterDialog xmlWriterDialog = null;
	
	private JRadioButton allModules, oneModule, oneModulePlusChain;
	private JComboBox<String> modules;
	private JCheckBox nonModuleData;
	private JCheckBox includeFinal;
	private JCheckBox writeNames, writeTips;
	
	private JButton writeNow, preview, defaultFile, browseFile;
	private JTextField fileName;
	
	private JCheckBox writeOnStart, writeInHeaders;

	private long currentTime;
	
	private PamguardXMLWriter xmlWriter;
	
	private XMLWriterDialog(Window parentFrame) {
		super(parentFrame, "XML Configuration Output", false);
		xmlWriter = PamguardXMLWriter.getXMLWriter();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		ActionListener enableAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		};
		
		JPanel modulePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		modulePanel.setBorder(new TitledBorder("Module Selection"));
		modulePanel.add(allModules = new JRadioButton("All Modules"), c);
		c.gridy++;
		modulePanel.add(oneModule = new JRadioButton("One Module"), c);
		c.gridy++;
		c.gridwidth = 1;
		modulePanel.add(oneModulePlusChain = new JRadioButton("Up to selected module"), c);
		c.gridy++;
		c.gridwidth = 1;
		modulePanel.add(modules = new JComboBox<>(), c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		modulePanel.add(nonModuleData = new JCheckBox("Output non module data"), c);
		c.gridy++;
		modulePanel.add(includeFinal = new JCheckBox("Include class constants"), c);
		c.gridy++;
		modulePanel.add(writeNames = new JCheckBox("Write parameter names"), c);
		c.gridy++;
		modulePanel.add(writeTips = new JCheckBox("Write parameter tips"), c);
		mainPanel.add(modulePanel);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(allModules);
		buttonGroup.add(oneModule);
		buttonGroup.add(oneModulePlusChain);
		allModules.addActionListener(enableAction);
		oneModule.addActionListener(enableAction);
		oneModulePlusChain.addActionListener(enableAction);
		
		JPanel outputPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		outputPanel.setBorder(new TitledBorder("Output Options"));
		outputPanel.add(new JLabel("File name", JLabel.CENTER), c);
		c.gridx+=2;
		outputPanel.add(defaultFile = new JButton("Default"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		outputPanel.add(fileName = new JTextField(), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		outputPanel.add(browseFile = new JButton("Browse"), c);
		c.gridx++;
		outputPanel.add(writeNow = new JButton("Write Now"), c);
		c.gridx++;
		outputPanel.add(preview = new JButton("Preview"), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		outputPanel.add(writeOnStart = new JCheckBox("Write settings file whenever PAMGuard starts"), c);
		mainPanel.add(outputPanel);		
		
		oneModule.setToolTipText("Write settings for a single module");
		allModules.setToolTipText("Write settings for all modules");
		oneModulePlusChain.setToolTipText("Write settings for a module and all modules providing input to that module");
		modules.setToolTipText("Select PAMGuard module");
		nonModuleData.setToolTipText("Write data not associated with PAMGuard modules (mostly display information)");
		includeFinal.setToolTipText("Write class constants");
		writeNames.setToolTipText("Write user friendly names (parameter values are primarily labelled with their JAVA variable names)");
		writeTips.setToolTipText("Write longer descriptions of parameter values");
		writeOnStart.setToolTipText("Write an XML configuration file every time PAMGuard starts");
		
		defaultFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				makeDefaultFileName();
			}
		});
		preview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPreview();
			}
		});
		browseFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseFileName();
			}
		});
		writeNow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				writeFile();
			}
		});
		
		this.setDialogComponent(mainPanel);
		this.setResizable(true);
		setHelpPoint("overview.PamMasterHelp.docs.xmloutput");
	}
	
	protected void writeFile() {
		File f = new File(fileName.getText());
		writeFile(f);
	}
	
	private boolean writeFile(File file) {
		getParams();
		Document doc = xmlWriter.writeSelection(currentTime);
		try {
			xmlWriter.writeToFile(doc, file);
		} catch (IOException e) {
			WarnOnce.showWarning(getOwner(), "Unable to write XML document", e.getMessage(), WarnOnce.WARNING_MESSAGE);
			return false;
		}
		return true;
	}


	protected void browseFileName() {
		PamFileFilter fileFilter = new PamFileFilter("XML file", "xml");
		PamFileChooser pfc = new PamFileChooser();
		pfc.setFileFilter(fileFilter);
		File currFile = new File(fileName.getText());
		pfc.setCurrentDirectory(currFile.getParentFile());
		int ans = pfc.showSaveDialog(this);
		if (ans == JFileChooser.CANCEL_OPTION) {
			return;
		}
		if (ans == JFileChooser.APPROVE_OPTION) {
			File f = pfc.getSelectedFile();
			if (writeFile(f)) {
				setVisible(false);
			}
		}
	}

	protected void showPreview() {
		getParams();
		Document doc = xmlWriter.writeSelection(currentTime);
		if (doc != null) {
			new PAMGuardXMLPreview(getOwner(), "XML Preview", xmlWriter.getAsString(doc));
		}
	}

	protected void makeDefaultFileName() {
		String fileName = xmlWriter.makeFileName(currentTime);
		this.fileName.setText(fileName);
	}

	public static boolean showDialog(Window parentFrame) {
//		if (xmlWriterDialog == null || xmlWriterDialog.getOwner() != parentFrame) {
			xmlWriterDialog = new XMLWriterDialog(parentFrame);
//		}
		xmlWriterDialog.setParams();
		xmlWriterDialog.setVisible(true);
		
		
		return true;
	}

	private void setParams() {
		PamController pamController = PamController.getInstance();
		XMLWriterSettings params = xmlWriter.getWriterSettings();
		int nCU = pamController.getNumControlledUnits();
		modules.removeAllItems();
		int iSel = -1;
		for (int i = 0; i < nCU; i++) {
			PamControlledUnit pcu = pamController.getControlledUnit(i);
			modules.addItem(pcu.getUnitName());
			if (pcu.getUnitType().equals(params.selectedModuleType) && pcu.getUnitName().equals(params.selectedModuleName)) {
				iSel = i;
			}
		}
		allModules.setSelected(params.moduleSelection == XMLWriterSettings.MODULES_ALL);
		oneModule.setSelected(params.moduleSelection == XMLWriterSettings.MODULES_ONE);
		oneModulePlusChain.setSelected(params.moduleSelection == XMLWriterSettings.MODULES_CHAIN);
		if (iSel >= 0) {
			modules.setSelectedIndex(iSel);
		}
		nonModuleData.setSelected(params.nonModuleData);
		includeFinal.setSelected(params.includeConstants);
		writeNames.setSelected(params.writeShortNames);
		writeTips.setSelected(params.writeToolTips);
		
		makeDefaultFileName();
		writeOnStart.setSelected(params.writeAtStart);
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		XMLWriterSettings params = xmlWriter.getWriterSettings().clone();
		if (allModules.isSelected()) {
			params.moduleSelection = XMLWriterSettings.MODULES_ALL;
		}
		else if (oneModule.isSelected()) {
			params.moduleSelection = XMLWriterSettings.MODULES_ONE;
		}
		else {
			params.moduleSelection = XMLWriterSettings.MODULES_CHAIN;
		}
		int iSel = modules.getSelectedIndex();
		if (iSel >= 0) {
			PamController pamController = PamController.getInstance();
			PamControlledUnit pcu = pamController.getControlledUnit(iSel);
			params.selectedModuleType = pcu.getUnitType();
			params.selectedModuleName = pcu.getUnitName();
		}
		params.nonModuleData = nonModuleData.isSelected();
		params.includeConstants = includeFinal.isSelected();
		params.writeShortNames = writeNames.isSelected();
		params.writeToolTips = writeTips.isSelected();
		
		params.writeAtStart = writeOnStart.isSelected();
		
		xmlWriter.setWriterSettings(params);
		return true;
	}

	private void enableControls() {
		modules.setEnabled(allModules.isSelected() == false);
		nonModuleData.setEnabled(allModules.isSelected() == true);
//		writeOnStart.setEnabled(false);
	}
	
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		currentTime = PamCalendar.getTimeInMillis();
		super.setVisible(visible);
	}

}
