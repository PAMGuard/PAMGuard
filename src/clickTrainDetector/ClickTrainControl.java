package clickTrainDetector;

import java.awt.Frame;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.status.ModuleStatus;
import PamView.PamSidePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierManager;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoManager;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.classificationRatio.RatioClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.layout.CTFXGUI;
import clickTrainDetector.layout.CTSwingGUI;
import clickTrainDetector.layout.warnings.CTWarningManager;
import clickTrainDetector.offline.ClickTrainOfflineProcess;
import detectionPlotFX.clickTrainDDPlot.ClickTrainDDPlotProvider;
import detectionPlotFX.data.DDPlotRegister;

/**
 * 
 * A click train detector. The detector has a detection stage and a classification stage. 
 * <p>
 * The detector is an algorithm which extract any plausible click train from the data.
 * <p>
 * The classification stage decides whether a detected click train should be saved or removed form
 * the click train data block. 
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class ClickTrainControl extends PamControlledUnit implements PamSettings { 

	/**
	 * Flag for processing start
	 */
	public static final int PROCESSING_START = 0;

	/**
	 * Flag to indicate a setup is required
	 */
	public static final int NEW_PARAMS = 1;

	/*
	 * Called whenever processing has ended. This allows algorithms to save currently 
	 * held click trains etc once processing has completed. 
	 */
	public static final int PROCESSING_END = 2;

	/**
	 * Flag indicating a new parent data block has been selected. This allows settings panes to 
	 * execute any appropriate GUI changes.
	 */
	public static final int NEW_PARENT_DATABLOCK= 3;

	/**
	 * The PAMGuard clock has been updated. 
	 */
	public static final int CLOCK_UPDATE = 4;

	/**
	 * Reference to the click train process. 
	 */
	private ClickTrainProcess clickTrainProcess;

	/**
	 * Click train settings. 
	 */
	private ClickTrainParams clickTrainParams;

	/**
	 * List of click train algorithms 
	 */
	private ArrayList<ClickTrainAlgorithm> clickTrainAlgorithms; 
	
	/**
	 * The click train offline task used to process click train in viewer mode. 
	 */
	private ClickTrainOfflineProcess clickTrainOffline;

	/**
	 * The click train classification process. 
	 */
	private CTClassificationProcess ctClassificationProcess;

	/**
	 * Click train localisation process. 
	 */
	private CTLocalisationProcess ctLocalisationProcess;

	/**
	 * The CT classifier manager. 
	 */
	private CTClassifierManager classifierManager;
	
	/**
	 * A data selector which is used to initially filter data incoming to the click train detector. 
	 */
	private DataSelector dataSelector;


	/***GUI***/

	/**
	 * The swing GUI for the click train detector. 
	 */
	private CTSwingGUI ctGUISwing;
	
	/**
	 * The JavaFX based GUI for the click train detector. 
	 */
	private CTFXGUI ctCGUIFX;

	/**
	 * True to allow saving of trains. ONLY FOR DEBUG PURPOSES- true to save data units, false otherwise. 
	 */
	protected boolean saveTrains = true;

	/**
	 *The warning manager handles warning messages between the UI or process and the controlled unit
	 */
	private CTWarningManager warningManager;
	
	/**
	 * True for all processes to be notified. The click train is a bit of a special case because during offline processing 
	 * larger super data units are created which are passed to classification and localisation processes. In real time click 
	 * trains should always passed to downstream processes but during viewer they should only be passed to downstream processes
	 * if offline processing is running.  
	 */
	private boolean notifyProcess = true;

	/**
	 * Click info manager- handles information from the algorithms. 
	 */
	private CTAlgorithmInfoManager clAlgorithmInfoManager;
	
	/**
	 * Help point which can be referenced form dialogs, etc. 
	 */
	public static final String helpPoint = "detectors.ClickTrainDetector.docs.ClickTrainDetector";
 
	/**
	 * Constructor for the ClickTrainControl. 
	 * @param unitName - the unit name. 
	 */
	public ClickTrainControl(String unitName) {
		super("Click Train Detector", unitName);

		this.clickTrainParams=new ClickTrainParams(); 
		
		if (this.isViewer) notifyProcess=false; //not notification in viewer mode. 

		addPamProcess(clickTrainProcess=new ClickTrainProcess(this));
		addPamProcess(ctClassificationProcess=new CTClassificationProcess(this));	
		addPamProcess(ctLocalisationProcess=new CTLocalisationProcess(this));	


		//click train algorithms
		clickTrainAlgorithms= new ArrayList<ClickTrainAlgorithm>(); 

		/*****Click Train Algorithms******/
		clickTrainAlgorithms.add(new MHTClickTrainAlgorithm(this)); 
		//clickTrainAlgorithms.add(new RatioClickTrainAlgorithm(this));
		//		clickTrainAlgorithms.add(new TestAlgorithm(this));
		
		classifierManager = new CTClassifierManager(this);
		
		//algorithm info manager
		clAlgorithmInfoManager = new CTAlgorithmInfoManager(this); 

		if (this.isViewer) {
			clickTrainOffline = new ClickTrainOfflineProcess(this);
		}
		
		warningManager = new CTWarningManager(this); 

		PamSettingManager.getInstance().registerSettings(this);
		
//		System.out.println("----INITIAL CLICK TRIAN PARAMS----"); 
//		System.out.println(this.clickTrainParams.toString()); 
//		System.out.println("----------------------------------"); 

		setupClickTrainDetector();
		
		//register the DD display
		DDPlotRegister.getInstance().registerDataInfo(new ClickTrainDDPlotProvider(this));
	}


	/**
	 * Get the click train data block. The click train  data block holds all confirmed click
	 * trains included those which are not classified to species. 
	 * @return the clickTrainDataBlock
	 */
	public ClickTrainDataBlock getClickTrainDataBlock() {
		return clickTrainProcess.getClickTrainDataBlock();
	}
	
	/**
	 * Get the classified click train data block. This holds click trains which have been classified to a species. 
	 * @return the classified click train data block. 
	 */
	public ClickTrainDataBlock getClssfdClickTrainDataBlock() {
		return this.ctClassificationProcess.getClssfdClickTrainDataBlock(); 
	}



	public void setClickTrainParams(ClickTrainParams clickTrainParams) {
		this.clickTrainParams = clickTrainParams;
	}

	@Override
	public void setupControlledUnit() {
		setupClickTrainDetector(); 
		super.setupControlledUnit();
	}


	/**
	 * Set up the click train detector 
	 */
	private void setupClickTrainDetector() {
		if (clickTrainProcess.getParentDataBlock()!=null) {
			getClickTrainDataBlock().setNaturalLifetime((int) 
					(clickTrainProcess.getParentDataBlock().getRequiredHistory()/1000.));
			
			//create the data selector
			createDataSelector(clickTrainProcess.getParentDataBlock());
		}
		
		for (int i=0; i<this.clickTrainAlgorithms.size(); i++) {
			clickTrainAlgorithms.get(i).update(ClickTrainControl.NEW_PARAMS, null); 
		}
		
		//set up the classifier parameters
		this.classifierManager.setupClassifiers(); 
		
		this.ctLocalisationProcess.setTMlocParams(this.clickTrainParams); 
		
		PamControlledUnitGUI gui = this.getGUI(PamGUIManager.getGUIType());
		if (gui != null) {
			gui.notifyGUIChange(ClickTrainControl.NEW_PARAMS);
		}
	}

	/**
	 * The current data selector. 
	 * @return the current data selector. 
	 */
	public DataSelector getDataSelector() {
		return dataSelector;
	}


	@Override
	public Serializable getSettingsReference() {
//		System.out.println("----SAVING CLICK TRIAN PARAMS----"); 
//		System.out.println(this.clickTrainParams.toString()); 
//		System.out.println("----------------------------------"); 
		return clickTrainParams; 
	}

	@Override
	public long getSettingsVersion() {
		return ClickTrainParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			this.clickTrainParams = (ClickTrainParams) pamControlledUnitSettings.getSettings();
//			System.out.println("----LOADING CLICK TRIAN PARAMS----"); 
//			System.out.println(this.clickTrainParams.toString()); 
//			System.out.println("----------------------------------"); 
			this.setupControlledUnit();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return true; 
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			clickTrainProcess.setupProcess();
		}
	}

	/**
	 * Get click train parameters. 
	 * @return the click train parameters. 
	 */
	public ClickTrainParams getClickTrainParams() {
		return clickTrainParams;
	}

	/**
	 * Get click train process. 
	 * @return the click train process. 
	 */
	public ClickTrainProcess getClickTrainProcess() {
		return clickTrainProcess;
	}

	/**
	 * Get the current click train algorithm.  
	 * @return the click train algorithm. 
	 */
	public ClickTrainAlgorithm getCurrentCTAlgorithm() {
		//System.out.println("Get the current algorithm "+clickTrainParams.ctDetectorType);
		if (clickTrainParams.ctDetectorType<clickTrainAlgorithms.size() && clickTrainParams.ctDetectorType>=0)
			return clickTrainAlgorithms.get(clickTrainParams.ctDetectorType);
		else 
			return clickTrainAlgorithms.get(0);
	}
	

	/**
	 * Send update flag to module. Sends the flag to all click train 
	 * algorithms. 
	 * @param flag - the update flag. 
	 */
	public void update(int flag) {
		for (int i=0; i<this.clickTrainAlgorithms.size(); i++) {
			clickTrainAlgorithms.get(i).update(flag, null);
		}
	}
	
	/**
	 * Get the classification process. This classifies the click train. 
	 * @return the ctClassificationProcess
	 */
	public CTClassificationProcess getCTClassificationProcess() {
		return ctClassificationProcess;
	}


	/**
	 * Get all available click train algorithms. 
	 * @return list of available click train algorithms. 
	 */
	public ArrayList<ClickTrainAlgorithm> getClickTrainAlgorithms() {
		return this.clickTrainAlgorithms;
	}



	/**
	 * Update the parameters. 
	 * @param newParams - the parameters to update. 
	 */
	public void updateParams(ClickTrainParams newParams) {
		if (newParams!=null) {
			this.clickTrainParams=newParams.clone();
			this.clickTrainProcess.setupProcess();
			this.getClickTrainDataBlock().setChannelMap(clickTrainParams.getChannelMap());
		}
		setupClickTrainDetector();
	}

	/**
	 * Convenience method to get the current parent data block of the 
	 * process. 
	 * @return - the parent data block. 
	 */
	public PamDataBlock<?> getParentDataBlock() {
		return this.clickTrainProcess.getParentDataBlock();
	}

	/**
	 * Get the parent data block if it is a click train data block
	 * @return the click data block. 
	 */
	public PamDataBlock getClickDataBlock() {
		return this.clickTrainProcess.getParentDataBlock();
	}

	/**
	 * Called whenever CLICK train settings panes should getParams() e.g. when a dialog is
	 * closed. The algorithms handle their own settings. 
	 */
	public boolean getAlgorithmParams() {
		boolean ok = true; 
		for (int i=0; i<this.clickTrainAlgorithms.size(); i++) {
			if (clickTrainAlgorithms.get(i).getClickTrainGraphics()!=null) {
				if (!clickTrainAlgorithms.get(i).getClickTrainGraphics().getParams()) {
					ok=false;
				} 
			}
		}
		return ok; 
	}
	
	
	/**
	 * Get the current classifiers. 
	 * @return the current classifiers. 
	 */
	public ArrayList<CTClassifier> getCurrentClassifiers(){
		return classifierManager.getCurrentClassifiers(); 
	}

	
	/***************Swing GUI******************/
	
	@Override
	public PamSidePanel getSidePanel() {
//		System.out.println("SIDE PANEL:L " + getSwingGUI().getSidePanel()); 
		return getSwingGUI().getSidePanel(); 
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return getSwingGUI().createDetectionMenu(parentFrame); 
	}
	
	public CTSwingGUI getSwingGUI(){
		return (CTSwingGUI) getGUI(PamGUIManager.SWING); 
	}
	
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag==PamGUIManager.FX) {
			if (ctCGUIFX == null) {
				ctCGUIFX= new CTFXGUI(this);
			}
			return ctCGUIFX;
		}
		if (flag==PamGUIManager.SWING) {
			if (ctGUISwing == null) {
				ctGUISwing= new CTSwingGUI(this);
			}
			return ctGUISwing;
		}
		return null;
	}

	/************************************/


	/**
	 * Get the offline process for the click train detector i.e. using it in viewer mode. 
	 * @return the offline process. 
	 */
	public ClickTrainOfflineProcess getClickTrainsOffline() {
		return this.clickTrainOffline;
	}


	/**
	 * Get the data selector. 
	 * @param source - the source data block 
	 * @return the data selector.
	 */
	public void createDataSelector(PamDataBlock<?> source) {
		if (dataSelector==null || dataSelector.getPamDataBlock()!=source) {
			//create the data selector
			//System.out.println("Data selector: " + dataSelector); 
			if (source!=null) {
				dataSelector=source.getDataSelectCreator().getDataSelector(this.getUnitName() +"_clicks", false, null);
				//System.out.println("Data selector: " + dataSelector); 
			}
			else {
				dataSelector=null; 
			}
		}
	}


	/**
	 * The classifier manager. 
	 * @return the classifier manager. 
	 */
	public CTClassifierManager getClassifierManager() {
		return this.classifierManager;
	}


	/**
	 * Get the classification process
	 * @return the classification process. 
	 */
	public CTClassificationProcess getClickClassifierProccess() {
		return ctClassificationProcess;
	}

	/**
	 * Get the click train localisation process. This localises clicks from the classified click train 
	 * data block. 
	 * @return the click loclaisation process. 
	 */
	public CTLocalisationProcess getCTLocalisationProccess() {
		return this.ctLocalisationProcess;
	}

	/**
	 * Get the warning manager.  This handles any warning messages passed from the 
	 * UI or other processes. 
	 * @return the warning manager.
	 */
	public CTWarningManager getWarningManager() {
		return warningManager;
	}

	/**
	 * True to notify downstream processes if a new click train is detected. 
	 * @return true to notify processes. 
	 */
	public boolean isNotifyProcesses() {
		return this.notifyProcess;
	}	
	
	
	/**
	 * Set whether to notify downstream processes if a new click train is detected. 
	 * @return true to notify processes. 
	 */
	public void setNotifyProcesses(boolean notify) {
		 this.notifyProcess = notify;
	}	

	@Override
	public ModuleStatus getModuleStatus() {
		return super.getModuleStatus();
	}

	/**
	 * Get the algorithm info manager. This handles logging information on different algorithms into the database
	 * 
	 * @return
	 */
	public CTAlgorithmInfoManager getCTAlgorithmInfoManager() {
		return clAlgorithmInfoManager;
	}


	


}
