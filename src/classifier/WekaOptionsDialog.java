package classifier;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import weka.core.Option;
import PamView.PamGui;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class WekaOptionsDialog extends PamDialog {

	AbstractWekaClassifier abstractWekaClassifier;

	weka.classifiers.AbstractClassifier wekaClassifier;

	private static WekaOptionsDialog singleInstance;
	
	private static final String wekaHome = "http://www.cs.waikato.ac.nz/ml/weka/";

	private int[] argumentCount = null;

	JTextField[][] paramFields = null;
	JCheckBox[] checkBoxes = null;

	private WekaOptionsDialog(Window parentFrame, AbstractWekaClassifier abstractWekaClassifier) {
		super(parentFrame, abstractWekaClassifier.getClassifierName() + " Options", false);
		wekaClassifier = abstractWekaClassifier.getWekaClassifier(); 
		countWekaArguments();
		paramFields = new JTextField[argumentCount.length][];
		checkBoxes = new JCheckBox[argumentCount.length];
		JPanel p = new JPanel();
		String tit = String.format("%s rev. %s", abstractWekaClassifier.getClassifierName(), wekaClassifier.getRevision());
		p.setBorder(new TitledBorder(tit));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		Enumeration paramsList = wekaClassifier.listOptions();
		String[] options = wekaClassifier.getOptions();
		weka.core.Option wekaOption;
		JLabel label;
		JTextField txtField;
		int nParams = 0;
		int nArgs;
		String lab;
//		c.gridwidth = 2;
//		int oldanchor = c.anchor;
//		c.anchor = GridBagConstraints.CENTER;
////		String wName = String.format("Weka %s rev. %s", wekaClassifier.toString(), wekaClassifier.getRevision());
//		addComponent(p, new JLabel(wekaClassifier.toString()), c);
//		c.gridwidth = 1;
//		c.gridy++;
//		wekaClassifier.
		
		while (paramsList.hasMoreElements()) {
			c.gridx = 0;
			wekaOption = (Option) paramsList.nextElement();
			nArgs = wekaOption.numArguments();
			lab = String.format("%s (-%s))", wekaOption.description(), 
					wekaOption.name());
			c.gridwidth = 1;
			addComponent(p, label = new JLabel(lab, SwingConstants.RIGHT), c);
			if (nArgs > 0) {
				paramFields[nParams] = new JTextField[nArgs];
				label.setToolTipText(wekaOption.synopsis());
				for (int i = 0; i < nArgs; i++) {
					c.gridx++;
					addComponent(p, txtField = new JTextField(5), c);
					paramFields[nParams][i] = txtField;
				}
			}
			else {
				c.gridx++;
				c.gridwidth = 2;
				addComponent(p, checkBoxes[nParams] = new JCheckBox(), c);
			}
			nParams++;

			c.gridy++;
		}
		JPanel q = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		q.setBorder(new TitledBorder("This classifier uses Weka open source machine learning"));
		ImageIcon image = new ImageIcon(ClassLoader
				.getSystemResource("Resources/Weka (software) logo.png"));
		JButton aButton;
		JLabel aLabel;
		if (image != null) {
			addComponent(q, aLabel = new JLabel(image), c);
			aLabel.setToolTipText(wekaClassifier.toString());
			c.gridx++;
		}
		c.anchor = GridBagConstraints.SOUTH;
		addComponent(q, aButton = new JButton("Weka home page ..."), c);
		aButton.addActionListener(new WekaHelpPoint(wekaHome));
		
//		q.add(BorderLayout.WEST, new )
		JPanel outer = new JPanel(new BorderLayout());
		outer.add(BorderLayout.CENTER, p);
		outer.add(BorderLayout.SOUTH, q);

		setDialogComponent(outer);
	}

	private void countWekaArguments() {
		int nParams = 0;
		weka.core.Option wekaOption;
		Enumeration paramsList = wekaClassifier.listOptions();
		while (paramsList.hasMoreElements()) {
			wekaOption = (Option) paramsList.nextElement();
			if (argumentCount == null) {
				argumentCount = new int[1];
			}
			else {
				argumentCount = Arrays.copyOf(argumentCount, nParams+1);
			}
			argumentCount[nParams] = wekaOption.numArguments();
			nParams++;
		}
	}

	static public boolean showDialog(Window parentFrame, AbstractWekaClassifier abstractWekaClassifier) {
		if (true || singleInstance == null || 
				singleInstance.getOwner() != parentFrame || 
				singleInstance.abstractWekaClassifier != abstractWekaClassifier) {
			singleInstance = new WekaOptionsDialog(parentFrame, abstractWekaClassifier);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return true;
	}

	@Override
	public boolean getParams() {
		Enumeration paramsList = wekaClassifier.listOptions();
		Option option;
		String[] optionString = new String[0];
		int stringLength = 0;
		int i = -1;
		int nParams;
		String firstText;
		while (paramsList.hasMoreElements()) {
			i++;
			option = (Option) paramsList.nextElement();
			nParams = option.numArguments();
			if (nParams == 0) {
				if (checkBoxes[i].isSelected()) {
					optionString = buildOptionsString(optionString, "-"+option.name());
				}
			}
			else {
				firstText = paramFields[i][0].getText();
				if (firstText != null && firstText.length() > 0) {
					optionString = buildOptionsString(optionString, "-"+option.name());
					for (int j = 0; j < nParams; j++) {
						optionString = buildOptionsString(optionString, paramFields[i][j].getText());
					}
				}
			}
		}
		
		try {
			wekaClassifier.setOptions(optionString);
		} catch (Exception e) {
			return showWarning("Invalid option");
		}
		
		return true;
	}
	
	String[] buildOptionsString(String[] currentString, String newOption) {
		String[] newStrings = Arrays.copyOf(currentString, currentString.length+1);
		newStrings[currentString.length] = newOption;
		return newStrings;
	}

	private void setParams() {
		Enumeration paramsList = wekaClassifier.listOptions();
		String[] options = wekaClassifier.getOptions();
//		for (int j = 0; j < options.length; j++) {
//			System.out.println("Weka option " + j + " " + options[j]);
//		}
		
		Option option;
		int nParams;
		for (int o = 0; o < checkBoxes.length; o++) {
			if (checkBoxes[o] != null) {
				checkBoxes[o].setSelected(false);
			}
		}
		int optIndex;
		int i = -1;
		while (paramsList.hasMoreElements()) {
			i++;
			option = (Option) paramsList.nextElement();
			optIndex = findOptionsIndex(options, "-"+option.name());
			if (optIndex < 0) {
				continue;
			}
			nParams = option.numArguments();
//			option.
			if (nParams == 0) {
				checkBoxes[i].setSelected(true);
			}
			else {
				for (int j = 0; j < nParams; j++) {
					paramFields[i][j].setText(options[optIndex+1+j]);
				}
			}
		}
	}
	
	/**
	 * find an option based on it's name. 
	 * @param name
	 * @return
	 */
	Option findOption(String name) {
		if (name.startsWith("-")) {
			name = name.substring(1);
		}
		Option option;
		Enumeration paramsList = wekaClassifier.listOptions();
		while (paramsList.hasMoreElements()) {
			option = (Option) paramsList.nextElement();
			if (option.name().equals(name)) {
				return option;
			}
		}
		return null;
	}
	
	/**
	 * Find the index of an option in an option string
	 * @param options
	 * @return index of option or -1 if there isn't one.
	 */
	int findOptionsIndex(String[] options, String name) {
		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(name)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class WekaHelpPoint implements ActionListener {

		String urlString;
		
		public WekaHelpPoint(String urlString) {
			super();
			this.urlString = urlString;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			PamGui.openURL(urlString);
		}
		
	}
}
