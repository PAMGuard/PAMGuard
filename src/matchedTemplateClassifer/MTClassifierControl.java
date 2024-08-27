package matchedTemplateClassifer;

import java.awt.Frame;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import PamView.WrapperControlledGUISwing;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickClassifiers.ClickTypeProvider;
import matchedTemplateClassifer.layoutFX.MTSettingsPane;
import matchedTemplateClassifer.offline.MTOfflineProcess;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

/**
 * Classifier which classifies clicks.
 * 
 * @author Jamie Macaulay
 */
public class MTClassifierControl extends PamControlledUnit implements PamSettings, ClickTypeProvider {
	
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
	 * The parameters for the matched template classifier. 
	 */
	private MatchedTemplateParams matchedTemplateParams;

	/**
	 * The MTProcess
	 */
	private MTProcess mtProcess;

	/**
	 * The settings pane
	 */
	private MTSettingsPane settingsPane;

//	/**
//	 * The settings dialog for the click classifier.
//	 */
//	private SettingsDialog<MatchedTemplateParams> newDialog;

	/**
	 * The offline process. 
	 */
	private MTOfflineProcess mtOfflineProcess;
	
	/**
	 * The JavaFX graphics
	 */
	private MTControlGUI matchedClickGUIFX;
	
	/**
	 * The swing graphics - in this case the graphics are JavaFX so this calls a swing wrapper
	 * to hold the JavaFX graphics. 
	 */
	private PamControlledUnitGUI matchedClickGUIGUISwing;


	public MTClassifierControl(String unitName) {
		super("Matched Template Classifier", unitName);

		addPamProcess(mtProcess=new MTProcess(this));

		matchedTemplateParams= new MatchedTemplateParams(); 
		
		if (this.isViewer) {
			mtOfflineProcess = new MTOfflineProcess(this);
		}

		PamSettingManager.getInstance().registerSettings(this);
		
		updateParams(matchedTemplateParams); 
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			mtProcess.setupProcess();
		}
	}


	/**
	 * Currently the click detector has an integrated classifier and click detection type flags
	 * are integrated into this. Therefore to properly display flags in the click detectior display and 
	 * generic time base displays a classifer needs to be set up in the click detector. This is a bit of a HACK
	 * for now. Currently we set the flag of this classifier to 101. 
	 */
	public void setupClickClassifier() {

		mtProcess.getBespokeClassifierManager().updateSettings(matchedTemplateParams);

	}

	private PamDialogFX2AWT<MatchedTemplateParams> settingsDialog;

	/**
	 * Show settings dialog. 
	 * @param parentFrame - the frame. 
	 */
	public void showSettingsDialog(Frame parentFrame) {
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			SettingsPane<MatchedTemplateParams> setPane = (SettingsPane<MatchedTemplateParams>) getSettingsPane();
			setPane.setParams(this.matchedTemplateParams);
			settingsDialog = new PamDialogFX2AWT<MatchedTemplateParams>(parentFrame, setPane, false);
			settingsDialog.setResizable(true);
		}
		MatchedTemplateParams newParams = settingsDialog.showDialog(matchedTemplateParams);
		updateParams( newParams); 
	}

	@Override
	public void setupControlledUnit() {
		super.setupControlledUnit();
	}

	//	JFXPanel p;
	//	/*
	//	 * Open the settings dialog. 
	//	 */
	//	public void openSettingsPane() {
	//		//this has to be called in order to initialis the FX toolkit. Otherwise will crash if no other 
	//		//FX has been called. 
	//		Platform.runLater(new Runnable() {
	//			
	//			@Override
	//			public void run() {
	//				SettingsDialog<?> newDialog=new SettingsDialog<>(getSettingsPane());
	//				newDialog.setResizable(true);
	//				newDialog.setOnShown((value)->{
	//					 getSettingsPane().paneInitialized();
	//				});
	//				getSettingsPane().setParams(matchedTemplateParams);
	//				
	//				
	//				//show the dialog 
	//				newDialog.showAndWait().ifPresent(response -> {
	//					if (response!=null) updateParams(); 
	//				});
	//				
	//				//notify stuff that process settings may have changed. 
	//				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	//			}
	//		});


	//		System.out.println("Open Settings Dialog 1 ");
	//		if (pamDialogFX2AWT==null){
	//			pamDialogFX2AWT= new DialogAWT("Matched Template Classifier");
	//		}
	//		Platform.runLater(()->{
	//			getSettingsPane().setParams(matchedTemplateParams);
	//		});
	//		pamDialogFX2AWT.setVisible(true);

	//		if (p==null) p =  new JFXPanel(); // initializes JavaFX environment
	//		System.out.println("Open Settings Dialog 2 ");
	//
	//				Platform.runLater(()->{
	//					try {
	//					System.out.println("Open Settings Dialog 2");
	//					if (newDialog==null){
	//						newDialog=new SettingsDialog<MatchedTemplateParams>(getSettingsPane());
	//						newDialog.setResizable(true);
	//						newDialog.setOnShown((value)->{
	//							getSettingsPane().paneInitialized();
	//						});
	//					}
	//					
	//					//set the params
	//					getSettingsPane().setParams(matchedTemplateParams);
	//		
	//					}
	//					catch (Exception e) {
	//						e.printStackTrace();
	//					}
	//					//show the dialog 
	//					newDialog.showAndWait().ifPresent(response -> {
	//						if (response!=null) updateParams(); 
	
	//					});
	//				});
	//	}


	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu("Matched Template Classifier"); 
		
		JMenuItem menuItem = new JMenuItem("Matched Template Classifier..."); 
		menuItem.addActionListener((action)->{
			showSettingsDialog(parentFrame); 
		});
		menu.add(menuItem);
		
		if (this.isViewer) {
			menuItem = new JMenuItem("Reclassify clicks..."); 
			menuItem.addActionListener((action)->{
				this.mtOfflineProcess.showOfflineDialog(parentFrame);
			});
			menu.add(menuItem);
		}
		
		return menu; 
	}

	/**
	 * Update the parameters 
	 */
	private void updateParams(MatchedTemplateParams newParams) {
		//MatchedTemplateParams newParams=getSettingsPane().getParams(matchedTemplateParams); 
		//System.out.println("Hello: " +  newParams.classifiers.get(0).thresholdToAccept);
		if (newParams!=null) {
			this.matchedTemplateParams=newParams;
			
			//reset all saved fft's etc in classifiers. Prevents silly interpolation errors.
			for (int i=0; i<matchedTemplateParams.classifiers.size(); i++) {
				matchedTemplateParams.classifiers.get(i).reset();
			}
		}
		
		this.setupClickClassifier();
		//setup the controlled unit. 
		//System.out.println("Matched click detector data source: "+newParams.dataSourceName);
				
		setupControlledUnit(); 
	}


	/**
	 * Get the settings pane. 
	 * @return the settings pane. 
	 */
	public MTSettingsPane getSettingsPane(){

		if (this.settingsPane==null){
			settingsPane= new MTSettingsPane(this); 
		}
		return settingsPane; 
	}

	/**
	 * Get params for the controlled unit. 
	 * @return the params. 
	 */
	public MatchedTemplateParams getMTParams() {
		return matchedTemplateParams;
	}


	@Override
	public long getSettingsVersion() {
		return MatchedTemplateParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			this.matchedTemplateParams = (MatchedTemplateParams) pamControlledUnitSettings.getSettings();

			//add classifiers
			if (matchedTemplateParams.classifiers==null) matchedTemplateParams.classifiers = new ArrayList<MTClassifier>(); 
			if (matchedTemplateParams.classifiers.size()==0) matchedTemplateParams.classifiers.add(new MTClassifier()); 

			this.setupControlledUnit();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return true; 
		}
	}


	@Override
	public String getUnitType() {
		return "Match Template Classifer";
	}

	@Override
	public Serializable getSettingsReference() {
		return matchedTemplateParams; 
	}

	/**
	 * Convenience function to get parent data block
	 * @return the parent data block. 
	 */
	@SuppressWarnings("rawtypes")
	public PamDataBlock getParentDataBlock() {
		return mtProcess.getParentDataBlock();
	}
	
//	/**
//	 * Convenience function to get case parent datablock to clickcontrol. 
//	 * @return the parent datablock. 
//	 */
//	@Deprecated
//	public ClickDataBlock getClickDataBlock() {
//		return (ClickDataBlock) mtProcess.getParentDataBlock(); 
//	}

	/**
	 * Get the MT process. 
	 * @return the MT process. 
	 */
	public MTProcess getMTProcess() {
		return this.mtProcess;
	}

	/**
	 * Called whenever offline processing is occurring 
	 * @param processingFlag
	 */
	public void update(int processingFlag) {
		switch (processingFlag) {
		case MTClassifierControl.PROCESSING_END:
			//force the click detector to repaint. 
			break;
		}

	}
	


	/**
	 * Returns the symbol data for a classification type. If the type does not match the current type then
	 * null is returned. 
	 * @param clickType - the click type. 
	 * @return symbol data for the click type.
	 */
	public SymbolData getSymbolData(byte clickType) {
		if (clickType==this.getMTParams().type) {
			return getMTParams().pamSymbol; 
		}
		return null;
	}

	@Override
	public String[] getSpeciesList() {
		return new String[]{this.getUnitName()};		
	}
	

	@Override
	public int[] getCodeList() {
		return new int[] {this.matchedTemplateParams.type};
	}

	@Override
	public int codeToListIndex(int code) {
		return 0;
	}

	@Override
	public SymbolData[] getSymbolsData() {
		return new SymbolData[] {this.matchedTemplateParams.pamSymbol};
	}

	/**
	 * The offline MT process. 
	 * @return the offline mt process. 
	 */
	public MTOfflineProcess getOfflineMTProcess() {
		return mtOfflineProcess;
	}

	public void setMTParams(MatchedTemplateParams newParams) {
		this.matchedTemplateParams = newParams;
		
	}

	/**
	 * Get the GUI for the PAMControlled unit. This has multiple GUI options which
	 * are instantiated depending on the view type.
	 * 
	 * @param flag. The GUI type flag defined in PAMGuiManager.
	 * @return the GUI for the PamControlledUnit unit.
	 */
	@Override
	public PamControlledUnitGUI getGUI(int flag) {
		if (flag == PamGUIManager.FX) {
			if (matchedClickGUIFX == null) {
				matchedClickGUIFX = new MTControlGUI(this);
			}
			return matchedClickGUIFX;
		}
		if (flag == PamGUIManager.SWING) {
			if (matchedClickGUIGUISwing == null) {
				matchedClickGUIGUISwing = new WrapperControlledGUISwing(this);
			}
			return matchedClickGUIGUISwing;
		}
		return null;
	}




}
