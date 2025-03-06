package whistleClassifier.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Jama.Matrix;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import pamMaths.MatrixDialog;
import whistleClassifier.FragmentClassifierParams;
import whistleClassifier.WhistleClassificationParameters;
import whistleClassifier.WhistleClassifierControl;
import whistlesAndMoans.AbstractWhistleDataUnit;

public class WhistleClassificationDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private WhistleClassifierControl whistleClassifierControl;
	
	private Frame frame;

	private static WhistleClassificationDialog singleInstance;
	
	private WhistleClassificationDialog THIS;

	private WhistleClassificationParameters whistleClassificationParameters;

	private SourcePanel sourcePanel;

	private TrainingPanel trainingPanel;

	private RunningPanel runningPanel;

	private JRadioButton trainButton, runButton;


	private WhistleClassificationDialog(Frame parentFrame, WhistleClassifierControl whistleClassifierControl) {
		super(parentFrame, whistleClassifierControl.getUnitName() + " Settings", false);
		this.whistleClassifierControl = whistleClassifierControl;
		this.frame = parentFrame;
		THIS = this;

		sourcePanel = new SourcePanel(this, "Data source", AbstractWhistleDataUnit.class, false, true);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(sourcePanel.getPanel());

		JPanel op = new JPanel();
		op.setLayout(new GridBagLayout());
		op.setBorder(new TitledBorder("Operation mode"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(op, runButton = new JRadioButton("Run Classifier"), c);
		c.gridy++;
		addComponent(op, trainButton = new JRadioButton("Collect training data"), c);
		ButtonGroup bg = new ButtonGroup();
		bg.add(runButton);
		bg.add(trainButton);
		runButton.addActionListener(new RunSelection());
		trainButton.addActionListener(new RunSelection());
		trainButton.setToolTipText("Gather whistle contours to build up a classifier training database");
		runButton.setToolTipText("Classifiy whistles using exiting classification settings");
		p.add(op);

		p.add(trainingPanel = new TrainingPanel());

		p.add(runningPanel = new RunningPanel());

		setDialogComponent(p);

		setHelpPoint("detectors.whistleClassifierHelp.docs.whistleClassifier_Overview");

	}

	public static WhistleClassificationParameters showDialog(Frame parentFrame, 
			WhistleClassifierControl whistleClassifierControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.whistleClassifierControl != whistleClassifierControl) {
			singleInstance = new WhistleClassificationDialog(parentFrame, whistleClassifierControl);
		}
		singleInstance.whistleClassificationParameters = whistleClassifierControl.getWhistleClassificationParameters().clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);

		return singleInstance.whistleClassificationParameters;
	}

	@Override
	public void cancelButtonPressed() {
		whistleClassificationParameters = null;
	}

	class RunSelection implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableAndShow();
		}

	}

	private void enableAndShow() {
		trainingPanel.setVisible(trainButton.isSelected());
		runningPanel.setVisible(runButton.isSelected());
		runningPanel.enableControls();
		pack();
	}


	private void setParams() {
		sourcePanel.setSourceList();
		sourcePanel.setSource(whistleClassificationParameters.dataSource);

		trainButton.setSelected(whistleClassificationParameters.operationMode == 
			WhistleClassificationParameters.COLLECT_TRAINING_DATA);
		runButton.setSelected(whistleClassificationParameters.operationMode == 
			WhistleClassificationParameters.ANALYSE_DATA);

		trainingPanel.setParams();
		runningPanel.setParams();

		enableAndShow();
	}

	@Override
	public boolean getParams() {
		PamDataBlock db = sourcePanel.getSource();
		if (db == null) {
			return false;
		}
		whistleClassificationParameters.dataSource = db.getDataName();
		if (trainButton.isSelected()) {
			whistleClassificationParameters.operationMode = WhistleClassificationParameters.COLLECT_TRAINING_DATA;
			if (!trainingPanel.getParams()) {
				return false;
			}
		}
		else {
			whistleClassificationParameters.operationMode = WhistleClassificationParameters.ANALYSE_DATA;
			if (!runningPanel.getParams()){
				return false;
			}
		}


		return true;
	}

	@Override
	public void restoreDefaultSettings() {

	}

	class TrainingPanel extends JPanel {

		private JTextField species;
		private JCheckBox useFolderNames;
		private SelectFolder selectFolder;

		public TrainingPanel() {
			super();
			setLayout(new GridBagLayout());
			setBorder(new TitledBorder("Training Data Gathering"));
			GridBagConstraints c = new PamGridBagContraints();
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.EAST;
			addComponent(this, new JLabel("Species"), c);
			c.gridx++;
			addComponent(this, species = new JTextField(30), c);
			c.gridx=0;
			c.gridy++;
			c.anchor = GridBagConstraints.WEST;
			c.gridwidth = 2;
			addComponent(this, useFolderNames = new JCheckBox("Use folder names for species (audio files should be appropriately organised)"), c);
			useFolderNames.addActionListener(new FolderNamesAction());
			c.gridx = 0;
			c.gridy++;
			addComponent(this, new JLabel("Storage folder for training data files"), c);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 2;
			selectFolder = new SelectFolder(50);
			addComponent(this, selectFolder.getFolderPanel(), c);
		}

		protected void setParams() {
			species.setText(whistleClassificationParameters.trainingSpecies);
			selectFolder.setFolderName(whistleClassificationParameters.trainingDataFolder);
			useFolderNames.setSelected(whistleClassificationParameters.wavFolderNameAsSpecies);
			enableControls();
		}


		protected boolean getParams() {
			whistleClassificationParameters.trainingSpecies = species.getText();
			whistleClassificationParameters.trainingDataFolder = selectFolder.getFolderName(true);
//			new FileParts(whistleClassificationParameters.trainingDataFolder);
			if (whistleClassificationParameters.trainingDataFolder == null) {
				return false;
			}
			whistleClassificationParameters.wavFolderNameAsSpecies = useFolderNames.isSelected();
			if (!whistleClassificationParameters.wavFolderNameAsSpecies) {
				if (whistleClassificationParameters.trainingSpecies == null ||
						whistleClassificationParameters.trainingDataFolder == null) {
					return false;
				}
			}
			if (!checkStorageFolder(whistleClassificationParameters.trainingDataFolder)) {
				return false;
			}
			return true;
		}
		
		private void enableControls() {
			species.setEnabled(!useFolderNames.isSelected());
		}
		
		class FolderNamesAction implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		}

		private boolean checkStorageFolder(String dirName) {

			File file = new File(dirName);
			String dlgTitle = "Whistle Classifier Training Data Storage";
			if (file.exists() && file.isDirectory()) {
				return true;
			} else if (!file.exists()) {
				int ans = JOptionPane.showOptionDialog(null, "Folder " + dirName
						+ " does not exist. \nWould you like to create it ?",
						dlgTitle,
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, null);
				if (ans == JOptionPane.NO_OPTION) {
					return false;
				}
				if (ans == JOptionPane.YES_OPTION) {
					try {
						if (!file.mkdir()) {
							return checkStorageFolder(dirName);
						}
					} catch (SecurityException ex) {
						ex.printStackTrace();
					}
				}
			} else {
				JOptionPane
				.showMessageDialog(null, dirName
						+ " is a file, not a folder \nCreate a new folder for click storage",
						dlgTitle, JOptionPane.WARNING_MESSAGE);
				return false;
			}

			return true;
		}
	}
	
	protected void loadedNewParams(FragmentClassifierParams fp) {
		getParams();
		whistleClassificationParameters.fragmentClassifierParams = fp;
		setParams();
	}

	class RunningPanel extends JPanel {
		
		private ClassifierInfoArea classifierInfoArea;
		
		private SpeciesPanel speciesPanel;
		
		private RunOptsPanel runOptsPanel;

		public RunningPanel() {
			super();
			setLayout(new BorderLayout());
			add(BorderLayout.NORTH, runOptsPanel = new RunOptsPanel());
			add(BorderLayout.CENTER, classifierInfoArea = new ClassifierInfoArea(whistleClassifierControl, 
					THIS, frame));
			add(BorderLayout.SOUTH, speciesPanel = new SpeciesPanel());
			classifierInfoArea.setBorder(new TitledBorder("Classifier Training Parameters"));
			classifierInfoArea.enableAll(false);
		}
		protected void setParams() {
			classifierInfoArea.setParams(whistleClassificationParameters.fragmentClassifierParams);
			speciesPanel.setParams();
			runOptsPanel.setParams();
		}

		protected boolean getParams() {
			if (!runOptsPanel.getParams()) {
				return false;
			}
			return true;
		}
		
		void enableControls() {
			speciesPanel.enableControls();
		}
	}
	
	/**
	 * Optiosn on when to run classification
	 * @author Doug Gillespie
	 *
	 */
	class RunOptsPanel extends JPanel {
		
		private JTextField lowWhistleClearTime;
		
		private JTextField lowWhistleNumber;
		
		private JCheckBox alwaysClassify;
		
		private RunOptsPanel () {
			super();
			setBorder(new TitledBorder("When to classify"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(this, new JLabel("Clear distributions if fewer than "), c);
			c.gridx++;
			addComponent(this, lowWhistleNumber = new JTextField(5), c);
			c.gridx++;
			addComponent(this, new JLabel("  "), c);
			c.gridx = 0;
			c.gridy ++;
			addComponent(this, new JLabel("whistles are detected in "), c);
			c.gridx++;
			addComponent(this, lowWhistleClearTime = new JTextField(5), c);
			c.gridx++;
			addComponent(this, new JLabel(" seconds "), c);
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 3;
			addComponent(this, alwaysClassify = new JCheckBox("Always run classification before clearing"), c);
			
		}
		
		private void setParams() {
			lowWhistleClearTime.setText(String.format("%d", whistleClassificationParameters.lowWhistleClearTime));
			lowWhistleNumber.setText(String.format("%d", whistleClassificationParameters.lowWhistleNumber));
			alwaysClassify.setSelected(whistleClassificationParameters.alwaysClassify);
		}
		
		private boolean getParams() {
			try {
				whistleClassificationParameters.lowWhistleClearTime = Integer.valueOf(lowWhistleClearTime.getText());
				whistleClassificationParameters.lowWhistleNumber = Integer.valueOf(lowWhistleNumber.getText());
			}
			catch (NumberFormatException e) {
				return false;
			}
			whistleClassificationParameters.alwaysClassify = alwaysClassify.isSelected();
			return true;
		}
	}
	
	/**
	 * List of species. May be able to select at a later date
	 * @author Doug Gillespie
	 *
	 */
	class SpeciesPanel extends JPanel {
		
		private JCheckBox[] speciesBox;
		
		private JButton showConfusion;
		
		ShowConfusion sc = new ShowConfusion();
		
		protected SpeciesPanel () {
			setLayout(new GridBagLayout());
			setBorder(new TitledBorder("Species"));
		}
		
		public void setParams() {
			this.removeAll();
			if (whistleClassificationParameters.fragmentClassifierParams == null) {
				return;
			}
			String[] species = whistleClassificationParameters.fragmentClassifierParams.getSpeciesList();
			if (species == null || species.length == 0) {
				return;
			}
			GridBagConstraints c = new PamGridBagContraints();
			int maxCol = 3;
			speciesBox = new JCheckBox[species.length];
			for (int i = 0; i < species.length; i++) {
				addComponent(this, speciesBox[i] = new JCheckBox(species[i]), c);
				if (++c.gridx == maxCol) {
					c.gridx = 0;
					c.gridy++;
				}
				speciesBox[i].setSelected(true);
				speciesBox[i].setEnabled(false);
			}
			c.gridx = 0;
			c.gridwidth = 3;
			c.gridy++;
			addComponent(this, showConfusion = new JButton("Show Confusion Matrix"), c);
			showConfusion.addActionListener(sc);
			pack();
		}
		
		void enableControls() {
			boolean e = false;
			if (whistleClassificationParameters == null) {
				return;
			}
			
			if (whistleClassificationParameters.fragmentClassifierParams != null && 
					whistleClassificationParameters.fragmentClassifierParams.confusionMatrix != null) {
				e = true;
			}
			if (showConfusion != null) {
				showConfusion.setEnabled(e);
			}
		}
	}
	
	public WhistleClassificationParameters getWhistleClassificationParameters() {
		return whistleClassificationParameters;
	}

	class ShowConfusion implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (whistleClassificationParameters.fragmentClassifierParams == null) {
				return;
			}
			String[] species = whistleClassificationParameters.fragmentClassifierParams.getSpeciesList();
			Matrix m = whistleClassificationParameters.fragmentClassifierParams.confusionMatrix;
			int nRow = m.getRowDimension();
			int nCol = m.getColumnDimension();
			String[] colNames = species;
			if (nCol > nRow) {
				colNames = Arrays.copyOf(species, nCol);
				colNames[nRow] = "??";
			}
			MatrixDialog.showDialog(frame, "Confusion Matrix (%)", 
					m.times(100), species, colNames);
		}
	}

}
