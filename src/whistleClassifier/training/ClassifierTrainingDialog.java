package whistleClassifier.training;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import pamMaths.PamMatrix;

import classifier.Classifier;
import classifier.ClassifierParams;
import classifier.ClassifierTypes;

import whistleClassifier.FragmentClassifierParams;
import whistleClassifier.WhistleClassificationParameters;
import whistleClassifier.WhistleClassifierControl;

import Jama.Matrix;
import PamUtils.FolderChangeListener;
import PamUtils.PamCalendar;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ClassifierTrainingDialog extends PamDialog implements TrainingObserver {

	private WhistleClassificationParameters whistleClassificationParameters;

	private FragmentClassifierParams fragmentClassifierParams;

	private static ClassifierTrainingDialog singleInstance;

	private WhistleClassifierControl whistleClassifierControl; 

	private SelectFolder selectFolder;

	private JCheckBox useFolderNames, dumpTextFile;
	
	private DataSelectPanel dataSelectPanel;

	private DataViewPanel dataViewPanel;

	private ProgressPanel progressPanel;

	private RunTrainingPanel runTrainingPanel;

	private ClassifierTrainer classifierTrainer;

	private Window frame;

	public ClassifierTrainingDialog(Window parentFrame, String title, WhistleClassifierControl wc) {
		super(parentFrame, title, false);
		this.frame = parentFrame;
		this.whistleClassifierControl = wc;

		JPanel p = new JPanel(new BorderLayout());

		JPanel w = new JPanel(new BorderLayout());

		GridBagConstraints c = new PamGridBagContraints();
		JPanel f = new JPanel(new BorderLayout());
		f.setBorder(new TitledBorder("Training data soure"));
		selectFolder = new SelectFolder("Select Folder", 40, true);
		selectFolder.addFolderChangeListener(new FolderChanged());
		f.add(BorderLayout.CENTER, selectFolder.getFolderPanel());
		f.add(BorderLayout.SOUTH, dataSelectPanel = new DataSelectPanel());
		w.add(BorderLayout.NORTH, f);


		dataViewPanel = new DataViewPanel();
		w.add(BorderLayout.CENTER, dataViewPanel);
		p.add(BorderLayout.WEST, w);

		p.add(BorderLayout.CENTER, runTrainingPanel = new RunTrainingPanel());

		p.add(BorderLayout.SOUTH, progressPanel = new ProgressPanel());

		setDialogComponent(p);
		
		setResizable(true);

		setHelpPoint("detectors.whistleClassifierHelp.docs.whistleClassifier_Training");
	}

	public static WhistleClassificationParameters showDialog(Frame parentFrame, WhistleClassifierControl wc) {
		if (singleInstance == null || parentFrame != singleInstance.frame) {
			singleInstance = new ClassifierTrainingDialog(parentFrame, "Classifier training", wc);
		}
		singleInstance.whistleClassificationParameters = wc.getWhistleClassificationParameters().clone();

		singleInstance.setParams();
		singleInstance.setVisible(true);

		return singleInstance.whistleClassificationParameters;
	}

	private boolean needsSave = false;
	private int trainingStatus = ClassifierTrainingProgress.IDLE;
	private boolean isRunning = false;
	@Override
	public void setStatus(ClassifierTrainingProgress trainingProgress) {
		if (trainingProgress.message != null) {
			trainingProgressMessage(trainingProgress.message);
			return;
		}
		
		trainingStatus = trainingProgress.status;
		switch (trainingStatus) {
		case ClassifierTrainingProgress.START_ALL:
			isRunning = true;
			break;
		case ClassifierTrainingProgress.START_ONE:
			isRunning = true;
			runTrainingPanel.addMessage(String.format("Starting Bootstrap %d", 
					trainingProgress.completedBootstraps+1));
			break;
		case ClassifierTrainingProgress.COMPLETE_ALL:
			isRunning = false;
			needsSave = true;
			break;
		case ClassifierTrainingProgress.COMPLETE_ONE:
			isRunning = true;
			progressPanel.setProgress(trainingProgress.completedBootstraps);
			break;
		case ClassifierTrainingProgress.ABORT:
			isRunning = false;
			break;
		case ClassifierTrainingProgress.IDLE:
			isRunning = false;
			break;
		}
		enableControls();
//		synchronized(this) {
			if (batchTrainingDialog != null) {
				batchTrainingDialog.setStatus(trainingProgress);
			}
//		}
	}

	private void trainingProgressMessage(String message) {
		runTrainingPanel.addMessage(message);
	}

	private TrainingDataCollection trainingDataCollection;
	private volatile boolean creating = false;

	private BatchTrainingDialog batchTrainingDialog;
	/**
	 * This has to run in a different thread to get the progress bar to work correctly
	 */
	private void runCreateTrainingThread() {
		Thread createTraining = new Thread(new CreateTraining());
		createTraining.start();
	}

	/**
	 * Thread class to read in data from lots of files in the user
	 * selected directory. 
	 * @author Doug
	 *
	 */
	private class CreateTraining implements Runnable {
		@Override
		public void run() {
			createTrainingStore();
		}
	}
	/**
	 * Creates the training store. 
	 * <p>Reads all the contours from the files in the selected folder and sub folders. 
	 * <p> Ideally, this 
	 * should only ever be run from inside the CreateTraining thread. 
	 */
	private void createTrainingStore() {
		creating = true;
		enableControls();
		trainingDataCollection = new TrainingDataCollection(whistleClassifierControl);
		trainingDataCollection.loadTrainingData(whistleClassificationParameters, 
				selectFolder.isIncludeSubFolders(), useFolderNames.isSelected(), progressPanel);
		trainingDataCollection.dumpStoreContent();
		if (dataViewPanel != null) {
			dataViewPanel.refill();
		}
		creating = false;

		runTrainingPanel.setParams();

		enableControls();
	}

	/**
	 * Have removed synchronisation from this method since for some reason it was hanging the whole 
	 * thing when called from createTrainingStore. Presumably this object must have synched with something else that 
	 * was waiting for createTrainingStore to complete which causes lock up. 
	 */
	void enableControls() {
		synchronized(enableSynch) {
			selectFolder.setEnabled(creating == false && isRunning == false);
			useFolderNames.setEnabled(creating == false && isRunning == false);
			dataSelectPanel.enableControls();
			runTrainingPanel.enableControls();
			dataViewPanel.enableControls();
			getCancelButton().setEnabled(creating == false && isRunning == false);
			getOkButton().setEnabled(creating == false && isRunning == false);
		}
	}
	Object enableSynch = new Object();


	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	private void setParams() {
		fragmentClassifierParams = whistleClassificationParameters.fragmentClassifierParams;
		if (fragmentClassifierParams == null) {
			fragmentClassifierParams = new FragmentClassifierParams();
			whistleClassificationParameters.fragmentClassifierParams = fragmentClassifierParams;
		}
		selectFolder.setFolderName(whistleClassificationParameters.trainingDataFolder);
		useFolderNames.setSelected(whistleClassificationParameters.trainingDataFolderAsSpecies);
		dataSelectPanel.setParams();
		runTrainingPanel.setParams();

		runCreateTrainingThread();
	}

	public boolean getFixedParams() {

		whistleClassificationParameters.trainingDataFolder = selectFolder.getFolderName(false);
		whistleClassificationParameters.trainingDataFolderAsSpecies = useFolderNames.isSelected();
		if (runTrainingPanel.getParams() == false) {
			return false;
		}
		return true;

	}

	@Override
	public boolean getParams() {
		if (getFixedParams() == false) {
			return false;
		}
		if (dataSelectPanel.getParams() == false) {
			return false;
		}
		Classifier classifier = whistleClassifierControl.getFragmentClassifier();
		if (classifier == null) {
			return false;
		}
		ClassifierParams classifierParams = classifier.getClassifierParams();
		if (classifierParams == null) {
			return false;
		}
		fragmentClassifierParams.setClassifierParams(classifierParams);
		if (classifierTrainer == null) {
			return false;
		}
		if (classifierTrainer.getMeanConfusion() == null) {
			return false;
		}
		fragmentClassifierParams.setConfusionMatrix(classifierTrainer.getMeanConfusion());
		fragmentClassifierParams.setStdConfusion(classifierTrainer.getSTDConfusion());
		fragmentClassifierParams.setSpeciesList(trainingDataCollection.getSpeciesList());

		return true;
	}

	public boolean startBatchBootstrap(int fragmentLength, int sectionLength,
			double minProbability) {
		// force these parameters into the dialog, then start as normal. 
		runTrainingPanel.setBootstrapParams(fragmentLength, sectionLength, minProbability);
		if (runTrainingPanel.getParams() == false) {
			return false;
		}
		if (dataSelectPanel.getParams() == false) {
			return false;
		}
		startTraining();
		return true;
	}

	private void startTraining() {
		if (runTrainingPanel.getParams() == false) {
			return;
		}
		if (dataSelectPanel.getParams() == false) {
			return;
		}
		progressPanel.setProgressLimits(0, fragmentClassifierParams.getNBootstrap());
		progressPanel.setProgress(0);
		progressPanel.setText("Running training bootstrap");
		
		whistleClassifierControl.setFragmentClassifier(ClassifierTypes.createClassifier(fragmentClassifierParams.getClassifierType()));
		whistleClassifierControl.getWhistleFragmenter().setFragmentLength(fragmentClassifierParams.getFragmentLength());
		//		whistleClassifierControl.getWhistle
		classifierTrainer = new ClassifierTrainer(whistleClassifierControl, trainingDataCollection,
				this);
		classifierTrainer.startBootstrap(fragmentClassifierParams, dumpTextFile.isSelected());
	}

	private void stopTraining() {
		if (classifierTrainer == null) {
			return;
		}
		classifierTrainer.stop();
		runTrainingPanel.addMessage("************************************");
		runTrainingPanel.addMessage("**  STOP AFTER CURRENT BOOTSTRAP ***");
		runTrainingPanel.addMessage("************************************");
	}
	
	private void setClassifierOptions() {
		Classifier classifier = whistleClassifierControl.getFragmentClassifier();
		if (classifier == null || classifier.hasParamsDialog() == false) {
			return;
		}
		classifier.showParamsDialog(this);
	}
	
	private void batchTraining() {
		if (runTrainingPanel.getParams() == false) {
			return;
		}
		if (dataSelectPanel.getParams() == false) {
			return;
		}
		batchTrainingDialog = new BatchTrainingDialog(this, this, fragmentClassifierParams);
		batchTrainingDialog.setVisible(true);
		
		batchTrainingDialog = null;
	}

	/**
	 * An export function which saves the classifier settings
	 * separately from the main PAMGUARD settings file data.  
	 */
	private void saveSettings() {
		if (getParams() == false) {
			showWarning("No classifier params to Export");
			return;
		}
		boolean saved = whistleClassifierControl.saveClassifierParams(frame, fragmentClassifierParams);
		if (saved) {
			needsSave = false;
			enableControls();
		}
	}


	/**
	 * Copy the mean and std confusion matrices to the clipboard. 
	 */
	private void copyConfusion() {
		Matrix meanConfusion = classifierTrainer.getMeanConfusion();
		if (meanConfusion == null) {
			return;
		}
		JTextField tempField = new JTextField();
		String str = "Mean Confusion Matrix\r\n";
		str += PamMatrix.matrixToString(meanConfusion, "%5.4f", "\t", 
				trainingDataCollection.getSpeciesList(), trainingDataCollection.getSpeciesList(), false);
		Matrix stdConfusion = classifierTrainer.getSTDConfusion();
		if (stdConfusion != null) {
			str += "STD Confusion Matrix\r\n";
			str += PamMatrix.matrixToString(stdConfusion, "%5.5f", "\t", 
					trainingDataCollection.getSpeciesList(), trainingDataCollection.getSpeciesList(), false);
		}
		tempField.setText(str);
		tempField.selectAll();
		tempField.copy();

	}

	/**
	 * @return the classifierTrainer
	 */
	public ClassifierTrainer getClassifierTrainer() {
		return classifierTrainer;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	/**
	 * Sub selection of training data based on min contour length and 
	 * frequency range. 
	 */
	class DataSelectPanel extends JPanel {

		private JTextField[] freqRange = new JTextField[2];
		private JButton defaultButton;
		private JTextField minContourLength;

		public DataSelectPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			useFolderNames = new JCheckBox("Use folder names as species names" +
			" (this will override the species names in the files)");
			useFolderNames.addActionListener(new UseFolderNamesChange());
			c.gridwidth = 6;
			addComponent(this, useFolderNames, c);

			c.gridwidth = 1;
			c.gridx=0;
			c.gridy++;
			addComponent(this, new JLabel("Limit frequency search range ", SwingConstants.RIGHT), c);
			c.gridx++;
			addComponent(this, freqRange[0] = new JTextField(5), c);
			c.gridx++;
			addComponent(this, new JLabel(" to "), c);
			c.gridx++;
			addComponent(this, freqRange[1] = new JTextField(5), c);
			c.gridx++;
			addComponent(this, new JLabel(" Hz  "), c);
			c.gridx++;
			addComponent(this, defaultButton = new JButton("default"), c);
			defaultButton.addActionListener(new DefaultFreqs());
			
			c.gridx = 0;
			c.gridy++;
			addComponent(this, new JLabel(" Require minimum contour length of "), c);
			c.gridx++;
			addComponent(this, minContourLength = new JTextField(5), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(this, new JLabel(" bins"), c);
			

		}
		class DefaultFreqs implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				defaultFreqs();
			}
		}

		private void defaultFreqs() {
			freqRange[0].setText("0");
			if (trainingDataCollection != null && trainingDataCollection.getNumTrainingGroups() > 0) {
				float sr = trainingDataCollection.getTrainingDataGroup(0).getDataSet(0).getSampleRate();
				freqRange[1].setText(String.format("%d", (int) sr/2));
			}
			else {
				freqRange[1].setText("0000");
			}
		}

		void setParams() {
			double[] f = fragmentClassifierParams.getFrequencyRange();
			if (f != null) {
				for (int i = 0; i < 2; i++) {
					freqRange[i].setText(String.format("%d", (int) f[i]));
				}
			}
			else {
				defaultFreqs();
			}
			
			minContourLength.setText(String.format("%d", 
					fragmentClassifierParams.minimumContourLength));
		}

		boolean getParams() {
			try {
				double[] f = new double[2];
				for (int i = 0; i < 2; i++) {
					f[i] = Double.valueOf(freqRange[i].getText());
				}
				fragmentClassifierParams.setFrequencyRange(f);
				
				fragmentClassifierParams.minimumContourLength = 
					Integer.valueOf(minContourLength.getText());
			}
			catch (NumberFormatException e) {
				return false;
			}
			return true;
		}

		void enableControls() {			
			boolean b = creating == false && isRunning == false;
			defaultButton.setEnabled(b);
			freqRange[0].setEnabled(b);
			freqRange[1].setEnabled(b);
		}
	}

	/**
	 * Show all the data, listed by species in a treeview. 
	 */
	class DataViewPanel extends JPanel {

		private JScrollPane scrollPane;

		private JTree dataTree;

		private TreeDataModel treeDataModel;

		private DefaultMutableTreeNode topNode;

		private DataViewPanel() {

			topNode =
				new DefaultMutableTreeNode("Training Data");
			//			 new TreeDataModel()

			dataTree = new JTree(treeDataModel = new TreeDataModel());
			scrollPane = new JScrollPane(dataTree);
			this.setLayout(new BorderLayout());
			setBorder(new TitledBorder("Training data"));
			add(BorderLayout.CENTER, scrollPane);
			//			setPreferredSize(new Dimension(200,200));

			fillData();
		}

		protected void refill() {
			dataTree.setModel(treeDataModel = new TreeDataModel());
		}

		void enableControls() {

		}

		class TreeDataModel implements TreeModel {

			//			JCheckBox[] boxes = new JCheckBox[3];

			public TreeDataModel() {
				//				for (int i = 0; i < 3; i++) {
				//					boxes[i] = new JCheckBox("Box " + i);
				//				}
			}

			@Override
			public void addTreeModelListener(TreeModelListener l) {

			}

			@Override
			public Object getChild(Object parent, int index) {
				if (parent == null) {
					return topNode;
				}
				else if (parent == topNode){
					if (trainingDataCollection == null) {
						return 0;
					}
					else {
						return trainingDataCollection.getTrainingDataGroup(index);
					}
				}
				else if (parent.getClass() == TrainingDataGroup.class) {
					return ((TrainingDataGroup) parent).getDataSet(index);
				}
				return null;
			}

			@Override
			public int getChildCount(Object parent) {
				if (trainingDataCollection == null) {
					return 0;
				}
				if (parent == topNode) {
					return trainingDataCollection.getNumTrainingGroups();
				}
				else if (parent.getClass() == TrainingDataGroup.class) {
					return ((TrainingDataGroup) parent).getNumDataSets();
				}
				return 0;
			}

			@Override
			public int getIndexOfChild(Object parent, Object child) {
				return 0;
			}

			@Override
			public Object getRoot() {
				return topNode;
			}

			@Override
			public boolean isLeaf(Object node) {
				return (node.getClass() == TrainingDataSet.class);
			}

			@Override
			public void removeTreeModelListener(TreeModelListener l) {
				// TODO Auto-generated method stub

			}

			@Override
			public void valueForPathChanged(TreePath path, Object newValue) {
				// TODO Auto-generated method stub

			}

		}

		protected void fillData() {
			topNode.add(new JTree.DynamicUtilTreeNode(new JCheckBox("test box"), null));
		}
	}

	//	class DataTreeModel extends 


	class FolderChanged implements FolderChangeListener {
		@Override
		public void folderChanged(String newFolder, boolean subFolders) {
			if (getFixedParams()) {
				runCreateTrainingThread();
			}
		}
	}

	class UseFolderNamesChange implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getFixedParams()) {
				runCreateTrainingThread();
			}
		}
	}

	class RunTrainingPanel extends JPanel {

		private JButton start, stop, save;
		private JTextArea textArea;
		private JScrollPane scrollPane;
		private JTextField fragmentLength, sectionLength, nBoots;
		private JTextField minProbability;
		private JComboBox classifierTypes;
		private JButton batchTraining;
		private JButton classifierOptions;

		RunTrainingPanel() {
			setBorder(new TitledBorder("Run training"));
			setLayout(new BorderLayout());
			JPanel topPanel = new JPanel(new BorderLayout());
			JPanel toptopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			toptopPanel.add(start = new JButton("Start Training"));
			toptopPanel.add(stop = new JButton("Stop"));
			toptopPanel.add(save = new JButton("Export..."));
			toptopPanel.add(dumpTextFile = new JCheckBox("Create text output"));
			dumpTextFile.setToolTipText("Dump bootstrap output to a text file");
			//			toptopPanel.add(copy = new JButton("Copy Confusion"));
			topPanel.add(BorderLayout.NORTH, toptopPanel);
			JPanel optPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();


			c.gridx = 0;
			addComponent(optPanel, new JLabel("Classifier type "), c);
			c.gridx++;
			c.gridwidth = 5;
			addComponent(optPanel, classifierTypes = new JComboBox(), c);
			classifierTypes.addActionListener(new ClassifierTypeListener());
			c.gridx += c.gridwidth;
			c.gridwidth = 1;
			addComponent(optPanel, classifierOptions = new JButton("Options"), c);
			classifierOptions.addActionListener(new ClassifierOptions());
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(optPanel, new JLabel("Fragment length (FFT's) "), c);
			c.gridx++;
			addComponent(optPanel, fragmentLength = new JTextField(5), c);
			
			c.gridx++;
			c.gridheight = 3;
			c.gridwidth = 4;
			c.fill = GridBagConstraints.BOTH;
			addComponent(optPanel, batchTraining = new JButton("<html>Batch Testing of<p>Multiple Values</html>"), c);
			batchTraining.addActionListener(new BatchTraining());
			c.gridheight = 1;
			c.gridwidth = 1;
			//			c.gridx++;
			//			addComponent(optPanel, new JLabel(" FFT bins,  "), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(optPanel, new JLabel("Section length (fragments) "), c);
			c.gridx++;
			addComponent(optPanel, sectionLength = new JTextField(5), c);
			//			c.gridx++;
			//			addComponent(optPanel, new JLabel(" Fragments"), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(optPanel, new JLabel("Minimum probability threshold "), c);
			c.gridx++;
			addComponent(optPanel, minProbability = new JTextField(5), c);
			minProbability.setToolTipText("<html>Minimum probability for classification "+
					"Outputs with a probability less than this will be"+
					"classified as unknowns");
			c.gridx = 0;
			c.gridy++;
			//			c.gridwidth = 2;
			addComponent(optPanel, new JLabel("Number of test bootstraps "), c);
			c.gridx++;
			c.gridwidth = 1;
			addComponent(optPanel, nBoots = new JTextField(5), c);

			topPanel.add(BorderLayout.WEST, optPanel);

			this.add(BorderLayout.NORTH, topPanel);
			start.addActionListener(new StartTraining());
			stop.addActionListener(new StopTraining());
			save.addActionListener(new SaveTraining());
			//			copy.addActionListener(new CopyConfusion());

			for (int i = 0; i < ClassifierTypes.getNumClassifiers(); i++) {
				classifierTypes.addItem(ClassifierTypes.getClassifierName(i));
			}

			textArea = new JTextArea();
			textArea.addMouseListener(new MousePopupListener());
			//			textArea.setw
			textArea.setEditable(false);
			scrollPane = new JScrollPane(textArea);
			Dimension d = scrollPane.getPreferredSize();
			d.width = 450;
			scrollPane.setPreferredSize(d);
			this.add(BorderLayout.CENTER, scrollPane);
		}

		class MousePopupListener extends MouseAdapter {
			@Override
			public void mousePressed(MouseEvent e) {
				triggerMenu(e);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				triggerMenu(e);
			}
		}

		private void triggerMenu(MouseEvent e) {
			if (e.isPopupTrigger() == false) {
				return;
			}
			if (classifierTrainer == null) {
				return;
			}
			if (classifierTrainer.getMeanConfusion() == null) {
				return;
			}
			JPopupMenu menu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Copy all");
			menuItem.addActionListener(new CopyAll());
			menu.add(menuItem);

			menuItem = new JMenuItem("Copy Confusion Matrix");
			menuItem.addActionListener(new CopyConfusion());
			menu.add(menuItem);

			menu.show(e.getComponent(), e.getX(), e.getY());

		}

		class StartTraining implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearMessages();
				startTraining();
			}
		}
		class StopTraining implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopTraining();
			}
		}
		class BatchTraining implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				batchTraining();
			}
		}
		class ClassifierTypeListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
//				if (arg0.)
				if (whistleClassifierControl == null) {
					return;
				}
				whistleClassifierControl.setFragmentClassifier(ClassifierTypes.createClassifier(classifierTypes.getSelectedIndex()));
				enableControls();
			}
		}
		class ClassifierOptions implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setClassifierOptions();
			}
		}
		class SaveTraining implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveSettings();
			}
		}
		class CopyAll implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				copyAll();
			}
		}

		class CopyConfusion implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				copyConfusion();
			}
		}

		private void copyAll() {
			int selStart = textArea.getSelectionStart();
			int selEnd = textArea.getSelectionEnd();
			textArea.selectAll();
			textArea.copy();
			textArea.setSelectionEnd(selEnd);
			textArea.setSelectionStart(selStart);
		}

		private void setParams() {
			classifierTypes.setSelectedIndex(fragmentClassifierParams.getClassifierType());
			fragmentLength.setText(String.format("%d", fragmentClassifierParams.getFragmentLength()));
			sectionLength.setText(String.format("%d", fragmentClassifierParams.getSectionLength()));
			minProbability.setText(String.format("%3.3f", fragmentClassifierParams.getMinimumProbability()));
			nBoots.setText(String.format("%d", fragmentClassifierParams.getNBootstrap()));
			dumpTextFile.setSelected(fragmentClassifierParams.dumpTextFile);
		}
		public void setBootstrapParams(int fragmentLength, int sectionLength,
				double minProbability) {
			this.fragmentLength.setText(String.format("%d", fragmentLength));
			this.sectionLength.setText(String.format("%d", sectionLength));
			this.minProbability.setText(String.format("%3.3f", minProbability));
		}

		private boolean getParams() {
			if (fragmentClassifierParams == null) {
				return false;
			}
			double pVal;
			fragmentClassifierParams.setClassifierType(classifierTypes.getSelectedIndex());
			fragmentClassifierParams.dumpTextFile = dumpTextFile.isSelected();
			try {
				fragmentClassifierParams.setFragmentLength(Integer.valueOf(fragmentLength.getText()));
				fragmentClassifierParams.setSectionLength(Integer.valueOf(sectionLength.getText()));
				fragmentClassifierParams.setNBootstrap(Integer.valueOf(nBoots.getText()));
				fragmentClassifierParams.setMinimumProbability(pVal = Double.valueOf(minProbability.getText()));

			}
			catch (NumberFormatException e) {
				return false;
			}
			if (fragmentClassifierParams.getFragmentLength() < 3) {
				return showWarning("The Fragment Length must be at least 3 FFT bins");
			}
			if (fragmentClassifierParams.getSectionLength() < 1) {
				return showWarning("The section length cannot be zero");
			}
			if (pVal < 0.0 || pVal > 1.0) {
				return showWarning("Probability threshold value must be between 0 and 1.0");
			}

			return true;
		}

		void enableControls() {
//			int classifierType = classifierTypes.getSelectedIndex();
			Classifier classifier = whistleClassifierControl.getFragmentClassifier();
			if (classifier != null && classifier.hasParamsDialog()) {
				classifierOptions.setEnabled(true);
			}
			else {
				classifierOptions.setEnabled(false);
			}
			start.setEnabled(creating == false && isRunning == false);
			stop.setEnabled(creating == false && isRunning);
			save.setEnabled(creating == false && needsSave);
			dumpTextFile.setEnabled(creating == false && isRunning == false);
			//			copy.setEnabled(creating == false && needsSave);
			boolean b = creating == false && isRunning == false;
			classifierTypes.setEnabled(b);
			fragmentLength.setEnabled(b);
			sectionLength.setEnabled(b);
			minProbability.setEnabled(b);
			batchTraining.setEnabled(b);
			nBoots.setEnabled(b);
		}

		public void addMessage(String message) {
			textArea.append(message + "\n");
			textArea.repaint();
			scrollToEnd();
		}

		public void clearMessages() {
			textArea.setText("");
			textArea.repaint();
		}

		private void scrollToEnd() {
			JScrollBar v;
			if ((v = scrollPane.getVerticalScrollBar()).isShowing()) {
//				v.setValue(v.getMaximum());
			}
		}
	}

	class ProgressPanel extends JPanel implements ProgressInformation{

		protected JLabel progressText, etaText;;

		protected JProgressBar progressBar;

		public ProgressPanel() {

			setBorder(new TitledBorder("Progress"));
			setLayout(new BorderLayout());
			JPanel topBit = new JPanel(new BorderLayout());
			topBit.add(BorderLayout.WEST, progressText = new JLabel(" "));
			topBit.add(BorderLayout.EAST, etaText = new JLabel(""));
			add(BorderLayout.NORTH, topBit);
			add(BorderLayout.CENTER, progressBar = new JProgressBar(SwingConstants.HORIZONTAL));

		}

		private long startTime;
		@Override
		public void setProgressLimits(int minLim, int maxLim) {
			progressBar.setMinimum(minLim);
			progressBar.setMaximum(maxLim);
			startTime = System.currentTimeMillis();
		}

		@Override
		public void setProgress(int progress) {
			progressBar.setValue(progress);
			progressBar.repaint();
			setETA(progress);
		}
		public void setETA(int pos) {
			int max = progressBar.getMaximum();
			int min = progressBar.getMinimum();
			if (pos == min) {
				sayProgress("");
				return;
			}

			long now = System.currentTimeMillis();
			long ttg = (now - startTime) * (max-pos) / (pos-min);
			sayProgress(PamCalendar.formatTime(ttg) + " remaining");
		}

		public void sayProgress(String str) {
			etaText.setText(str);
		}

		@Override
		public void setText(String txt) {
			progressText.setText(txt);
		}
	}
	public TrainingDataCollection getTrainingDataCollection() {
		return trainingDataCollection;
	}

}
