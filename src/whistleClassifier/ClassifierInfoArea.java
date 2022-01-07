package whistleClassifier;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import classifier.ClassifierTypes;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * dialog panel to show basic information from a fragemtn classifier and to load
 * up new classifier files.
 * 
 * @author Doug Gillespie
 * 
 */
public class ClassifierInfoArea extends JPanel {

	// private JTextArea textArea;
	// private JScrollPane scrollPane;
	private JTextField fragmentLength, sectionLength, nBoots;

	private JTextField[] freqRange = new JTextField[2];

	private JTextField trainingFile;
	
	private JComboBox classifierTypes;

	private JButton choseFile;

	private WhistleClassifierControl whistleClassifierControl;
	
	private WhistleClassificationDialog whistleClassificationDialog;

	private Frame frame;

	public ClassifierInfoArea(
			WhistleClassifierControl whistleClassifierControl, 
			WhistleClassificationDialog whistleClassificationDialog, Frame parentFrame) {

		this.whistleClassifierControl = whistleClassifierControl;
		this.whistleClassificationDialog = whistleClassificationDialog;
		this.frame = parentFrame;

		setLayout(new BorderLayout());

		JPanel filePanel = new JPanel(new BorderLayout());
		filePanel.add(BorderLayout.CENTER, trainingFile = new JTextField(
				"Classifier training file"));
		filePanel.add(BorderLayout.EAST, choseFile = new JButton("Select ..."));
		choseFile.addActionListener(new ChoseFile());
		this.add(BorderLayout.NORTH, filePanel);

		JPanel optPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		c.gridx = 0;
		PamDialog.addComponent(optPanel, new JLabel("Classifier type "), c);
		c.gridx++;
		c.gridwidth = 5;
		PamDialog.addComponent(optPanel, classifierTypes = new JComboBox(), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		PamDialog.addComponent(optPanel,
				new JLabel("Fragment length (FFT's) "), c);
		c.gridx++;
		PamDialog.addComponent(optPanel, fragmentLength = new JTextField(5), c);
		// c.gridx++;
		// addComponent(optPanel, new JLabel(" FFT bins,  "), c);
		c.gridx = 0;
		c.gridy++;
		PamDialog.addComponent(optPanel, new JLabel(
				"Section length (fragments) "), c);
		c.gridx++;
		PamDialog.addComponent(optPanel, sectionLength = new JTextField(5), c);
		// c.gridx++;
		// addComponent(optPanel, new JLabel(" Fragments"), c);
		c.gridx = 0;
		c.gridy++;
		// c.gridwidth = 2;
		PamDialog.addComponent(optPanel, new JLabel(
				"Number of test bootstraps "), c);
		c.gridx++;
		c.gridwidth = 1;
		PamDialog.addComponent(optPanel, nBoots = new JTextField(5), c);
		c.gridx = 0;
		c.gridy++;
		PamDialog.addComponent(optPanel, new JLabel("Frequency search range "),
				c);
		c.gridx++;
		PamDialog.addComponent(optPanel, freqRange[0] = new JTextField(5), c);
		c.gridx++;
		PamDialog.addComponent(optPanel, new JLabel(" to "), c);
		c.gridx++;
		PamDialog.addComponent(optPanel, freqRange[1] = new JTextField(5), c);
		c.gridx++;
		PamDialog.addComponent(optPanel, new JLabel(" Hz  "), c);
		c.gridx++;

		for (int i = 0; i < ClassifierTypes.getNumClassifiers(); i++) {
			classifierTypes.addItem(ClassifierTypes.getClassifierName(i));
		}

		this.add(BorderLayout.CENTER, optPanel);
	}

	private void clearAll() {
		trainingFile.setText(null);
		fragmentLength.setText(null);
		sectionLength.setText(null);
		nBoots.setText(null);
		freqRange[0].setText(null);
		freqRange[1].setText(null);
	}

	public void setParams(FragmentClassifierParams fragmentClassifierParams) {
		if (fragmentClassifierParams == null) {
			clearAll();
			return;
		}
		
		trainingFile.setText(fragmentClassifierParams.fileName);
		
		classifierTypes.setSelectedIndex(fragmentClassifierParams.classifierType);
		fragmentLength.setText(String.format("%d", fragmentClassifierParams
				.getFragmentLength()));
		sectionLength.setText(String.format("%d", fragmentClassifierParams
				.getSectionLength()));
		nBoots.setText(String.format("%d", fragmentClassifierParams
				.getNBootstrap()));
		double[] f = fragmentClassifierParams.getFrequencyRange();
		if (f != null) {
			for (int i = 0; i < 2; i++) {
				freqRange[i].setText(String.format("%d", (int) f[i]));
			}
		} else {
			for (int i = 0; i < 2; i++) {
				freqRange[i].setText("");
			}
		}
	}

	public void enableAll(boolean b) {

		classifierTypes.setEnabled(b);
		fragmentLength.setEnabled(b);
		sectionLength.setEnabled(b);
		nBoots.setEnabled(b);
		freqRange[0].setEnabled(b);
		freqRange[1].setEnabled(b);
		trainingFile.setEnabled(b);

	}

	/**
	 * select a new training file
	 * 
	 * @return true file new file loaded.
	 */
	public boolean choseTrainingFile() {
		FragmentClassifierParams fp = whistleClassifierControl.loadFragmentClassifierParams(frame, 
				whistleClassificationDialog.getWhistleClassificationParameters());
		setParams(fp);
		whistleClassificationDialog.loadedNewParams(fp);
		return (fp != null);
	}

	class ChoseFile implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			choseTrainingFile();
		}
	}
}
