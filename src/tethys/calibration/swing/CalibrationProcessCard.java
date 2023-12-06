package tethys.calibration.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.wizard.PamWizard;
import nilus.AlgorithmType;
import nilus.AlgorithmType.Parameters;
import nilus.AlgorithmType.SupportSoftware;
import nilus.Calibration;
import nilus.Calibration.QualityAssurance;
import nilus.QualityValueBasic;
import tethys.calibration.CalibrationHandler;

public class CalibrationProcessCard extends CalibrationsCard {
	
	private JPanel processPanel;
	
	private JComboBox<String> calMethod;
	
	private JTextArea software;
	
	private JTextField version;
	
	private JComboBox<String> qaQuality;
	
	private JTextField qaComment;

	public CalibrationProcessCard(PamWizard pamWizard) {
		super(pamWizard, "Calibration Process");
		this.setLayout(new BorderLayout());
		processPanel = new JPanel(new GridBagLayout());
		processPanel.setBorder(new TitledBorder("Calibration Process"));
		this.add(BorderLayout.NORTH, processPanel);
		GridBagConstraints c = new PamGridBagContraints();
		
		calMethod = new JComboBox<String>();
		String[] meths = CalibrationHandler.calibrationMethods;
		for (int i = 0; i < meths.length; i++) {
			calMethod.addItem(meths[i]);
		}
		
		qaQuality = new JComboBox<>();
		String[] vals = CalibrationHandler.qaTypes;
		for (int i = 0; i < vals.length; i++) {
			qaQuality.addItem(vals[i]);
		}
		
		software = new JTextArea(5, 25);
		software.setLineWrap(true);
		software.setWrapStyleWord(true);
		software.setToolTipText("Details of calibration method and software used");
		
		version = new JTextField(20);
		version.setToolTipText("Serial number of calibration device");
		
		qaComment = new JTextField(20);
		qaComment.setToolTipText("Comment on calibration quality");

		processPanel.add(new JLabel("Method ", JLabel.RIGHT), c);
		c.gridx++;
		processPanel.add(calMethod, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		processPanel.add(new JLabel("Serial number ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		processPanel.add(version, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		processPanel.add(new JLabel("Quality ", JLabel.RIGHT), c);
		c.gridx++;
		processPanel.add(qaQuality, c);
		c.gridx = 0;
		c.gridy++;
		processPanel.add(new JLabel("QA Comment ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		processPanel.add(qaComment, c);

		this.add(BorderLayout.CENTER, makeScrollablePanel(software, "Calibration method"));
		
	}

	private JScrollPane makeScrollablePanel(JTextArea textArea, String title) {
		// TODO Auto-generated method stub
//		mainPanel.add(new Label(title, JLabel.LEFT));
//		textArea.setMinimumSize(new Dimension(200, 200));
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(new TitledBorder(title));
		scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().height/2, 0));
		return scrollPane;
	}
	

	@Override
	public boolean getParams(Calibration calibration) {
		if (calibration == null) {
			return false;
		}
		AlgorithmType process = calibration.getProcess();
		if (process == null) {
			process = new AlgorithmType();
			calibration.setProcess(process);
		}
		process.setMethod((String) calMethod.getSelectedItem());
		process.setVersion(version.getText());
		process.setSoftware(software.getText());
		if (software.getText() == null) {
			getPamWizard().showWarning("You must specify the calibratin method used");
		}
		
		QualityAssurance qa = calibration.getQualityAssurance();
		if (qa == null) {
			qa = new QualityAssurance();
			calibration.setQualityAssurance(qa);
		}
		qa.setComment(qaComment.getText());
		qa.setQuality(QualityValueBasic.fromValue((String) qaQuality.getSelectedItem()));
		
		// need to add a few fixed things for this to work...
//		List<SupportSoftware> supportSoftware = process.getSupportSoftware();
		Parameters params = process.getParameters();
		if (params == null) {
			params = new Parameters();
			process.setParameters(params);
		}
		
		return true;
	}

	@Override
	public void setParams(Calibration calibration) {
		if (calibration == null) {
			return;
		}
		AlgorithmType process = calibration.getProcess();
		if (process != null) {
			calMethod.setSelectedItem(process.getMethod());
			version.setText(process.getVersion());
			software.setText(process.getSoftware());
		}
		QualityAssurance qa = calibration.getQualityAssurance();
		if (qa != null) {
			QualityValueBasic qb = qa.getQuality();
			if (qb != null) {
				qaQuality.setSelectedItem(qb.value());
			}
			qaComment.setText(qa.getComment());
		}
	}
}
