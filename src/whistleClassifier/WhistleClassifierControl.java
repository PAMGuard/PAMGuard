package whistleClassifier;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.PamTabPanel;
import classifier.Classifier;
import classifier.ClassifierParams;
import classifier.ClassifierTypes;
import classifier.LinearClassifier;
import whistleClassifier.offline.ClassifierOffline;
import whistleClassifier.swing.WhistleClassificationDialog;
import whistleClassifier.swing.WhistleClassifierTabPanelControl;
import whistleClassifier.training.ClassifierTrainingDialog;

/**
 * Whistle Classifier control class. 
 * <p>
 * Two main run modes  / Three main tasks. 
 * <p>
 * While running PAMGUARD, the classifier can either accumulate contours in 
 * training data files, or it can run a species classifier. 
 * <p>
 * While PAMGUARD is idle, the user can run the training dialog which 
 * will take a set of training contours for various species and use
 * them to tune a classifier. 
 * <p>
 * The output from the classifier training is held in the normal 
 * module settings (in the psg files), but there is a facility for 
 * the specific training data to be exported to other files so that 
 * they can be loaded separately into different PAMGUARD configurations.   
 * @author Doug
 *
 */
public class WhistleClassifierControl extends PamControlledUnit implements PamSettings {

	protected WhistleClassifierProcess whistleClassifierProcess;
	
//	protected WhistleClassifierParameters whistleClassifierParameters = new WhistleClassifierParameters();
	
	private WhistleClassifierTabPanelControl whistleClassifierTabPanel;
	
	private WhistleClassificationParameters whistleClassificationParameters = new WhistleClassificationParameters();
	
	private WhistleFragmenter whistleFragmenter = new OverlappingFragmenter();
	
	private FragmentParameteriser fragmentParameteriser = new QuadraticParameteriser();
	
	private Classifier fragmentClassifier = new LinearClassifier();
	
	private ClassifierOffline classifierOffline;
		
	/**
	 * File end for training data (contour) files
	 */
	static public final String trainingFileEnd = ".wctd";

	/**
	 * File end for whistle classifier data (i.e. Eigen matrixes and that stuff)
	 */
	static public final String classifierFileEnd = "wcsd";
	
	private boolean isViewer;
	
	public WhistleClassifierControl( String unitName) {
		
		super("Whistle Classification", unitName);

		addPamProcess(whistleClassifierProcess = new WhistleClassifierProcess(this));
		
		whistleClassifierTabPanel = new WhistleClassifierTabPanelControl(this);
		
//		setWhistleClassificationParameters(new WhistleClassificationParameters());
		
		PamSettingManager.getInstance().registerSettings(this);
		
		whistleClassifierProcess.setupLogging();
		
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
//		if (isViewer) { // so it appears in batch!
			classifierOffline = new ClassifierOffline(this);
//		}
	}


	/**
	 * @return the whistleClassifierProcess
	 */
	public WhistleClassifierProcess getWhistleClassifierProcess() {
		return whistleClassifierProcess;
	}


	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		
		if (isViewer) {
			classifierOffline.addOfflineMenuItems(menu, parentFrame);
			menu.addSeparator();
		}
		
		JMenuItem menuItem = new JMenuItem("Settings ...");
		menuItem.addActionListener(new DetectionSettings(parentFrame));
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Discriminant function training ...");
		menuItem.addActionListener(new MenuTraining(parentFrame));
		menu.add(menuItem);
		
		return menu;
	}
	
	class DetectionSettings implements ActionListener {

		private Frame parentFrame;
		
		public DetectionSettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsDialog(parentFrame);	
		}
		
	}
	class MenuTraining implements ActionListener {

		private Frame parentFrame;
		
		public MenuTraining(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			trainingDialog(parentFrame);	
		}
		
	}
	
	public void settingsDialog(Frame parentFrame) {
		WhistleClassificationParameters newSettings = WhistleClassificationDialog.showDialog(parentFrame, 
				this);
		if (newSettings != null) {
			setWhistleClassificationParameters(newSettings.clone());
			setupProcess();
		}
	}

	private void trainingDialog(Frame parentFrame) {
		WhistleClassificationParameters newSettings = ClassifierTrainingDialog.showDialog(parentFrame, this);
		if (newSettings != null) {
			setWhistleClassificationParameters(newSettings.clone());
			setupProcess();
			
		}
	}


	private void setupProcess() {
		whistleClassifierProcess.findSourceData();
		
		if (classifierOffline != null) {
			/*
			 * WTF ! This is setting the source datablock for the classifer task to the
			 * original source (raw data) - should be the whistles input to the classifier ! 
			 */
//			classifierOffline.getReclassifyWhistles().getReclassifyTask().setParentDataBlock(whistleClassifierProcess.getSourceDataBlock());
			classifierOffline.getReclassifyWhistles().getReclassifyTask().setParentDataBlock(whistleClassifierProcess.getParentDataBlock());
		}
		
		FragmentClassifierParams fp = whistleClassificationParameters.fragmentClassifierParams;
		if (fp == null) {
			return;
		}
		whistleFragmenter.setFragmentLength(fp.fragmentLength);
		
		setupClassifier();
		
		whistleClassifierProcess.checkLoggingTables();
		
		// also need to tell the Graphics windows about changes (to build key at least)
		whistleClassifierTabPanel.prepareDisplay();
//		getWhistleFragmenter().setFragmentLength(getWhistleClassificationParameters().getFragmentLength());
	}
	
	/**
	 * SEts up a classifier with classifier specific settings
	 * Future versions, which will support > 1 type of classifier
	 * will have to look at the class in the parameters and 
	 * create the correct type of classifier. For now though, 
	 * assume that a linear classifier has already been created 
	 * as default. 
	 * @return true if parameters were there and can be set up.
	 */
	private boolean setupClassifier() {

		/*
		 * When this gets called from the settings manager, the parameters
		 * may or may not have null classifier parameters, so act accordingly !
		 */
		if (whistleClassificationParameters.fragmentClassifierParams == null) {
			return false;
		}	
		ClassifierParams params = whistleClassificationParameters.fragmentClassifierParams.classifierParams;
		if (params == null) {
			return false;
		}
		fragmentClassifier = ClassifierTypes.createClassifier(whistleClassificationParameters.fragmentClassifierParams.classifierType);
		fragmentClassifier.setMinimumProbability(whistleClassificationParameters.fragmentClassifierParams.getMinimumProbability());
		return fragmentClassifier.setClassifierParams(params);

	}
	

	@Override
	public void pamHasStopped() {
		whistleClassifierProcess.pamHasStopped();
	}


	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			setupProcess();
		}
	}

	@Override
	public PamTabPanel getTabPanel() {
		return whistleClassifierTabPanel;
	}

	/**
	 * called from the Classification Process whenever the statistical model has run
	 * @param ok
	 */
	public void updateClassification (boolean ok) {
		
	}

	@Override
	public Serializable getSettingsReference() {
		return getWhistleClassificationParameters();
	}

	@Override
	public long getSettingsVersion() {
		return WhistleClassificationParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		setWhistleClassificationParameters(((WhistleClassificationParameters) 
				pamControlledUnitSettings.getSettings()).clone());
		setupProcess();
		return true;
	}

	/**
	 * Save a set of classifier params. Since this is primarily an
	 * export function, it will always show the file save dialog
	 * @param params parameters to save
	 * @return true if successful. 
	 */
	public boolean saveClassifierParams(Window frame, FragmentClassifierParams params) {
		String defFileName = PamCalendar.createFileName(System.currentTimeMillis(), 
				"WhistleClassify_", classifierFileEnd);
		File file = new File(defFileName);
		JFileChooser jFileChooser = new PamFileChooser(file);
		jFileChooser.setApproveButtonText("Select");
		jFileChooser.setFileFilter(new PamFileFilter("Whistle Classification Settings", classifierFileEnd));
		int state = jFileChooser.showSaveDialog(frame);
		if (state != JFileChooser.APPROVE_OPTION) return false;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return false;
		newFile = PamFileFilter.checkFileEnd(newFile, classifierFileEnd, true);
		
		// include the file name in the file we're about to save. 
		params.fileName = newFile.getAbsolutePath();
		
		ObjectOutputStream ooStream;
		try {
			ooStream = new ObjectOutputStream(new FileOutputStream(newFile));
			ooStream.writeObject(params);
			ooStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(newFile.getAbsolutePath());
		
		return true;
	}
	
	public FragmentClassifierParams loadFragmentClassifierParams(Frame frame, WhistleClassificationParameters whistleClassificationParameters) {
		/*
		 * If existing params exist, then start with same file name
		 */
		File file = null;
		if (whistleClassificationParameters.fragmentClassifierParams != null &&
				whistleClassificationParameters.fragmentClassifierParams.fileName != null) {
			file = new File(whistleClassificationParameters.fragmentClassifierParams.fileName);
		}
		JFileChooser jFileChooser = new PamFileChooser(file);
		jFileChooser.setApproveButtonText("Select");
		jFileChooser.setFileFilter(new PamFileFilter("Whistle Classification Settings", classifierFileEnd));
		jFileChooser.setAcceptAllFileFilterUsed(false);
		int state = jFileChooser.showOpenDialog(frame);
		if (state != JFileChooser.APPROVE_OPTION) return null;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return null;
		

		ObjectInputStream oiStream;
		FragmentClassifierParams params = null;
		try {
			oiStream = new ObjectInputStream(new FileInputStream(newFile));
			params = (FragmentClassifierParams) oiStream.readObject();
//			whistleClassificationParameters.fragmentClassifierParams = params;
			oiStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return params;
	}

	/**
	 * @param whistleFragmenter the whistleFragmenter to set
	 */
	public void setWhistleFragmenter(WhistleFragmenter whistleFragmenter) {
		this.whistleFragmenter = whistleFragmenter;
	}


	/**
	 * @return the whistleFragmenter
	 */
	public WhistleFragmenter getWhistleFragmenter() {
		return whistleFragmenter;
	}


	/**
	 * @param whistleClassificationParameters the whistleClassificationParameters to set
	 */
	public void setWhistleClassificationParameters(
			WhistleClassificationParameters whistleClassificationParameters) {
		this.whistleClassificationParameters = whistleClassificationParameters;
	}


	/**
	 * @return the whistleClassificationParameters
	 */
	public WhistleClassificationParameters getWhistleClassificationParameters() {
		return whistleClassificationParameters;
	}


	public FragmentParameteriser getFragmentParameteriser() {
		return fragmentParameteriser;
	}


	public void setFragmentParameteriser(FragmentParameteriser fragmentParameteriser) {
		this.fragmentParameteriser = fragmentParameteriser;
	}


	/**
	 * @param fragmentClassifier the fragmentClassifier to set
	 */
	public void setFragmentClassifier(Classifier fragmentClassifier) {
		if (this.fragmentClassifier.getClass() == fragmentClassifier.getClass()) {
			return; // no need to change anything !
		}
		this.fragmentClassifier = fragmentClassifier;
		if (whistleClassificationParameters != null && 
			whistleClassificationParameters.fragmentClassifierParams != null &&
			whistleClassificationParameters.
			fragmentClassifierParams.classifierParams != null) {
			this.fragmentClassifier.setClassifierParams(whistleClassificationParameters.
					fragmentClassifierParams.classifierParams);			
		}
	}


	/**
	 * @return the fragmentClassifier
	 */
	public Classifier getFragmentClassifier() {
		return fragmentClassifier;
	}
	
}
