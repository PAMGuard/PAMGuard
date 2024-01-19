package rawDeepLearningClassifier;

import java.awt.Frame;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import PamView.PamSidePanel;
import PamView.WrapperControlledGUISwing;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import ai.djl.engine.Engine;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import detectionPlotFX.data.DDPlotRegister;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;
import rawDeepLearningClassifier.dataPlotFX.DLDetectionPlotProvider;
import rawDeepLearningClassifier.dataPlotFX.DLPredictionProvider;
import rawDeepLearningClassifier.ddPlotFX.RawDLDDPlotProvider;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLClassNameManager;
import rawDeepLearningClassifier.dlClassification.DLClassiferModel;
import rawDeepLearningClassifier.dlClassification.DLClassifierChooser;
import rawDeepLearningClassifier.dlClassification.DLClassifyProcess;
import rawDeepLearningClassifier.dlClassification.animalSpot.SoundSpotClassifier;
import rawDeepLearningClassifier.dlClassification.genericModel.GenericDLClassifier;
import rawDeepLearningClassifier.dlClassification.ketos.KetosClassifier;
import rawDeepLearningClassifier.layoutFX.DLModelSelectPane;
import rawDeepLearningClassifier.layoutFX.DLSidePanelSwing;
import rawDeepLearningClassifier.layoutFX.DLSymbolManager;
import rawDeepLearningClassifier.layoutFX.PredictionSymbolManager;
import rawDeepLearningClassifier.layoutFX.RawDLSettingsPane;
import rawDeepLearningClassifier.logging.DLAnnotationType;
import rawDeepLearningClassifier.logging.DLDataUnitDatagram;
import rawDeepLearningClassifier.logging.DLDetectionBinarySource;
import rawDeepLearningClassifier.logging.DLDetectionDatagram;
import rawDeepLearningClassifier.logging.DLResultBinarySource;
import rawDeepLearningClassifier.offline.DLOfflineProcess;
import rawDeepLearningClassifier.segmenter.SegmenterProcess;

/**
 * Module which uses an external deep learning classifier to identify any data
 * unit containing raw data.
 * <p>
 * For example, the module can be used to apply a deep learning model to analyse
 * raw sound data, clips or click detections.
 * <p>
 * The module is essentially a PAMGuard wrapper for the jdl4pam library which
 * does most of the working in loading models and setting up spectrogram
 * transforms so models receive the correct input data. The majority of the code
 * for the module involves interfacing with PAMGuard's data management system
 * (e.g. binary files), visualisation tools such as spectrogram graphics and the
 * TDDisplayFX and creating a settings GUI so users can load models easily and
 * visualise how they work.
 * <p>
 * Currently the jdl4pam library supports three types of deep learning model,
 * Generic, AnimalSpot and Ketos.
 * <p>
 * <li>Generic</li>
 * Generic models allows users to load almost any type of model and manually
 * assign the types of data transform and input shape. This means that the user
 * has to get the settings exactly right or the model will not work. It is the
 * most complex way to run a deep learning model and generally not recommenced.
 * However, users can export a settings file which makes it easier to set up for
 * another user.
 * <p><p>
 * <li>AnimalSpot </li>
 * AnimalSpot is a framework for training acoustic deep learning
 * models using Pytorch. Users can load a .py model which contains embedded
 * metadata so that PMAGuard knows the exact transforms required for the model
 * input. This makes deploying models in PAMGuard very easy - users require little
 * or no experience to get this working.
 * <p>
 * <li>Ketos</li>
 * Ketos is a framework for training acoustic deep learning models
 * using TensorFlow. Users can load a .ktpb model which contains embedded
 * metadata so that PMAGuard knows the exact transforms required for the model
 * input. Like AnimalSpot, this makes deploying Ketos models in PAMGuard very
 * straightforward.
 * 
 * @author Jamie Macaulay
 *
 */
public class DLControl extends PamControlledUnit implements PamSettings {
	
	/**
	 * PLUGIN_BUILD boolean is set to true so that the class loader isn't changed.  When
	 * ready to compile into a fatjar set to true. When using eclipse set to false. 
	 */
	public static final boolean PLUGIN_BUILD = true; //default is false. 

	/**
	 * Flag for processing start
	 */
	public static final int PROCESSING_START = 0;

	/**
	 * Flag to indicate a setup is required
	 */
	public static final int NEW_PARAMS = 1;

	/*
	 * Called whenever processing has ended. This allows algorithms to save
	 * currently held click trains etc once processing has completed.
	 */
	public static final int PROCESSING_END = 2;

	/**
	 * List of different deep learning models that are available.
	 */
	private ArrayList<DLClassiferModel> dlModels = new ArrayList<DLClassiferModel>();
	

	/**
	 * The settings pane.
	 */
	private RawDLSettingsPane settingsPane;

	/**
	 * The settings dialog
	 */
	private PamDialogFX2AWT<RawDLParams> settingsDialog;

	/**
	 * Generic parameters.
	 */
	private RawDLParams rawDLParmas = new RawDLParams();

	/**
	 * The deep elarning process.
	 */
	private SegmenterProcess segmenterProcess;

	/**
	 * The deep learning classification process
	 */
	private DLClassifyProcess dlClassifyProcess;

	/**
	 * The DL side panel - holds algorithm info.
	 */
	private DLSidePanelSwing dlSidePanel;

	/**
	 * DLControl GUI using JavaFX
	 */
	private DLControlGUI rawGUIFX;

	/**
	 * The GUI for swing.
	 */
	private WrapperControlledGUISwing rawDLGUISwing;

	/**
	 * Binary store for the model results.
	 */
	private DLResultBinarySource dlBinaryDataSource;

	/**
	 * The binary data source for detection data
	 */
	private DLDetectionBinarySource dlDetectionBinarySource;

	/**
	 * The DL offline process.
	 */
	private DLOfflineProcess dlOfflineProcess;

	private DLClassNameManager dlClassNameManager;

	/**
	 * The current data selector. 
	 */
	private DataSelector dataSelector;
	
	/**
	 * Figure out which model type has been imported. 
	 */
	private DLClassifierChooser dlClassifierChooser;

	private DLDownloadManager modelDownloadManager;



	/**
	 * Constructor for the DL Control.
	 * 
	 * @param unitName - the unit name.
	 */
	public DLControl(String unitName) {
		super("Deep Learning Classifier", unitName);

		PamRawDataBlock rawDataBlock = PamController.getInstance()
				.getRawDataBlock(rawDLParmas.groupedSourceParams.getDataSource());
		
		
		/**
		 * In the latest release of djl (0.11.0) there is a bug with the dll's of tensorflow and 
		 * pytorch. If tensorflow is loaded before pytorch there is a conglict in dll's and 
		 * pytorch models will not load. This is a workaround for now and the bug has been logged and 
		 * will bne fixed in subsequent djl releases. 
		 */
		Engine.getEngine("PyTorch"); 

		// segment the raw sound data
		addPamProcess(segmenterProcess = new SegmenterProcess(this, rawDataBlock));

		// classify the raw data segments.
		addPamProcess(dlClassifyProcess = new DLClassifyProcess(this, segmenterProcess.getSegmenterDataBlock()));

		//manages the names assigned to different output classes. 
		dlClassNameManager = new DLClassNameManager(this);
		
		//manages downloading models
		modelDownloadManager = new DLDownloadManager(); 

		// add storage options etc.
		dlBinaryDataSource = new DLResultBinarySource(dlClassifyProcess);
		dlClassifyProcess.getDLPredictionDataBlock().setBinaryDataSource(dlBinaryDataSource);
		dlClassifyProcess.getDLPredictionDataBlock().setDatagramProvider(new DLDataUnitDatagram(this));

		dlDetectionBinarySource = new DLDetectionBinarySource(this, dlClassifyProcess.getDLDetectionDatablock());
		dlClassifyProcess.getDLDetectionDatablock().setBinaryDataSource(dlDetectionBinarySource);
		dlClassifyProcess.getDLDetectionDatablock().setDatagramProvider(new DLDetectionDatagram(this));

		dlClassifyProcess.getDLDetectionDatablock()
				.setPamSymbolManager(new DLSymbolManager(this, dlClassifyProcess.getDLDetectionDatablock()));
		dlClassifyProcess.getDLPredictionDataBlock()
				.setPamSymbolManager(new PredictionSymbolManager(this, dlClassifyProcess.getDLDetectionDatablock()));

		/***** Add new deep learning models here ****/

		dlModels.add(new SoundSpotClassifier(this));
		dlModels.add(new KetosClassifier(this));
		//it is important the Generic Model is last because we need to check 
		//for PG metadata in all other models before resorting to manually 
		//setting up a model. 
		dlModels.add(new GenericDLClassifier(this));

		// dlModels.add(new DummyClassifier());
		// dlModels.add(new OrcaSpotClassifier(this)); //removed soon.

		if (this.isViewer) {
			dlOfflineProcess = new DLOfflineProcess(this);
		}
		;

		// register click detector for the javafx display.
		TDDataProviderRegisterFX.getInstance()
				.registerDataInfo(new DLDetectionPlotProvider(this, dlClassifyProcess.getDLDetectionDatablock()));
		TDDataProviderRegisterFX.getInstance()
				.registerDataInfo(new DLPredictionProvider(this, dlClassifyProcess.getDLDetectionDatablock()));

		// register the DD display
		DDPlotRegister.getInstance()
				.registerDataInfo(new RawDLDDPlotProvider(this, dlClassifyProcess.getDLDetectionDatablock()));
		// load the previous settings
		PamSettingManager.getInstance().registerSettings(this);

		// because this was added after some settings classes have already been
		// serialized
		if (rawDLParmas.classNameMap == null)
			rawDLParmas.classNameMap = new ArrayList<DLClassName>();
		
		//create the classiifer chooser. 
		dlClassifierChooser = new DLClassifierChooser(this); 
		
		// ensure everything is updated.
		updateParams(rawDLParmas);
	}

	/**
	 * Get the available deep learning models
	 * 
	 * @return the available deep learning models.
	 */
	public ArrayList<DLClassiferModel> getDLModels() {
		return dlModels;
	}
	
	
	/**
	 * Get a model by it's name. 
	 * @param the name the model. 
	 * @return the corresponding model object or null if no model with the name exists.  
	 */
	public DLClassiferModel getDLModel(String string) {
		for (int i=0; i< this.dlModels.size(); i++) {
			if (dlModels.get(i).getName().equals(string)) {
				return dlModels.get(i); 
			}
		}
		return null;
	}


	/**
	 * Get the current deep learning model.
	 * 
	 * @return the current deep learning model.
	 */
	public DLClassiferModel getDLModel() {
		if (this.rawDLParmas.modelSelection<0 || this.rawDLParmas.modelSelection>=dlModels.size()) {
			return null;
		}
		else {
			return dlModels.get(this.rawDLParmas.modelSelection);
		}
	}

	/**
	 * Called whenever there are new params.
	 * 
	 * @param newParams - new deep learning params to implement.
	 */
	private void updateParams(RawDLParams newParams) {
		this.rawDLParmas = newParams;

		this.segmenterProcess.setupSegmenter();
		this.dlClassifyProcess.setupProcess();

		// this is a bit of a hack. Annotations are added to data units but the
		// datablock knows nothing about them
		// unless the annotation type is set in the datablock. This is required for
		// things like symbol choosers that
		// may need to know a data block contains a certian type of annotation.

		this.getParentDataBlock().addDataAnnotationType(dlClassifyProcess.getDLAnnotionType());

		if (dlSidePanel != null) {
			dlSidePanel.setupPanel();
		}
	}

	/**
	 * Get basic parameters.
	 * 
	 * @return parameters class.
	 */
	public RawDLParams getDLParams() {
		return rawDLParmas;
	}

	@Override
	public Serializable getSettingsReference() {
		return this.rawDLParmas;
	}

	@Override
	public long getSettingsVersion() {
		return RawDLParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		RawDLParams newParameters = (RawDLParams) pamControlledUnitSettings.getSettings();
		;
		rawDLParmas = newParameters.clone();
		return true;
	}

	/**
	 * Get the segmenter process. This breaks raw data into chunks, combines into
	 * groups and sends to DL classifiers.
	 * 
	 * @return the segmenter process.
	 */
	public SegmenterProcess getSegmenter() {
		return this.segmenterProcess;
	}

	/**** GUI STUFF ***/

	/**
	 * Get the settings pane.
	 * 
	 * @return the settings pane.
	 */
	public RawDLSettingsPane getSettingsPane() {

		if (this.settingsPane == null) {
			settingsPane = new RawDLSettingsPane(this);
		}
		
		System.out.println("Get DL raw settings pane...");
		return settingsPane;
	}

	/**** ----Baked in Swing stuff---- *****/

	// Swing components should not be in the control class but that is way PG is at
	// the moment.

	/**
	 * Show settings dialog.
	 * 
	 * @param parentFrame - the frame.
	 */
	public void showSettingsDialog(Frame parentFrame) {
		System.out.println("Get DL raw settings pane BAD...");

		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			SettingsPane<RawDLParams> setPane = (SettingsPane<RawDLParams>) getSettingsPane();
			setPane.setParams(this.rawDLParmas);
			settingsDialog = new PamDialogFX2AWT<RawDLParams>(parentFrame, setPane, false);
			settingsDialog.setResizable(true);
		}
		RawDLParams newParams = settingsDialog.showDialog(rawDLParmas);

		// if cancel button is pressed then new params will be null.
		if (newParams != null) {
			updateParams(newParams);
		}
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (dlSidePanel == null) {
			dlSidePanel = new DLSidePanelSwing(this);
		}
		return dlSidePanel;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menu;
		if (this.isViewer) {
			menu = new JMenu("Raw Deep Learning Classifier");

			JMenuItem menuItem = new JMenuItem("Settings...");
			menuItem.addActionListener((action) -> {
				showSettingsDialog(parentFrame);
			});
			menu.add(menuItem);

			menuItem = new JMenuItem("Reclassify detections...");
			menuItem.addActionListener((action) -> {
				this.dlOfflineProcess.showOfflineDialog(parentFrame);
			});
			menu.add(menuItem);
		}

		else {
			menu = new JMenuItem();
			// no need for nested menus if there is only one option.
			menu.setText("Raw Deep Learning Classifier...");
			menu.addActionListener((action) -> {
				showSettingsDialog(parentFrame);
			});
		}

		return menu;
	}

	/**
	 * Get the deep learning classification process. This handles running the
	 * current deep learning model.
	 * 
	 * @return the deep learning classification process.
	 */
	public DLClassifyProcess getDLClassifyProcess() {
		return this.dlClassifyProcess;
	}

	/**
	 * Get the GUI for the PAMControlled unit. This has multiple GUI options which
	 * are instantiated depending on the view type.
	 * 
	 * @param flag. The GUI type flag defined in PAMGuiManager.
	 * @return the GUI for the PamControlledUnit unit.
	 */
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag == PamGUIManager.FX) {
			if (rawGUIFX == null) {
				rawGUIFX = new DLControlGUI(this);
			}
			return rawGUIFX;
		}
		if (flag == PamGUIManager.SWING) {
			if (rawDLGUISwing == null) {
				rawDLGUISwing = new WrapperControlledGUISwing(this);
			}
			return rawDLGUISwing;
		}
		return null;
	}

	public void setParams(RawDLParams newParams) {
		this.rawDLParmas = newParams;
	}

	/**
	 * Get the parent data block.
	 * 
	 * @return the parent data block.
	 */
	@SuppressWarnings("rawtypes")
	public PamDataBlock getParentDataBlock() {
		return segmenterProcess.getParentDataBlock();
	}

	/**
	 * Get the number of classes for the current classifier.
	 * 
	 * @return the number of classes.
	 */
	public int getNumClasses() {
		return getDLModel().getNumClasses();

	}

	/**
	 * Called whenever offline processing is occurring
	 * 
	 * @param processingFlag
	 */
	public void update(int processingFlag) {
		switch (processingFlag) {
		case DLControl.PROCESSING_END:
			// force the click detector to repaint.
			break;
		}

	}

	public DLClassNameManager getClassNameManager() {
		return this.dlClassNameManager;
	}

	/**
	 * Convenience function to get the DLAnnotationType from the DLClassification
	 * process.
	 * 
	 * @return the DLAnnotationType
	 */
	public DLAnnotationType getAnnotationType() {
		return this.dlClassifyProcess.getDLAnnotionType();
	}

		/**
	 * The current data selector. 
	 * @return the current data selector. 
	 */
	public DataSelector getDataSelector() {
		return dataSelector;
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
	 * Get the classifier chooser. 
	 * @return the classifier chooser.Take it
	 */
	public DLClassifierChooser getDlClassifierChooser() {
		return dlClassifierChooser;
	}

	/**
	 * Get the download manager for downloading models offline. 
	 * @return the download manager. 
	 */
	public DLDownloadManager getDownloadManager() {
		return modelDownloadManager;
	}



}
