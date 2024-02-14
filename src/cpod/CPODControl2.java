package cpod;

import java.awt.Frame;
import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitGUI;
import PamController.PamControlledUnitSettings;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.WrapperControlledGUISwing;
import cpod.dataPlotFX.CPODDPlotProvider;
import cpod.dataPlotFX.CPODPlotProviderFX;
import cpod.fx.CPODGUIFX;
import cpod.fx.CPODSettingsPane;
import cpod.logging.CPODClickTrainLogging;
import cpod.logging.CPODSubDetLogging;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import detectionPlotFX.data.DDPlotRegister;
import fileOfflineData.OfflineFileParams;
import javafx.concurrent.Task;
import pamScrollSystem.AbstractScrollManager;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

/**
 * CPOD control. Loads and manages CPOD and FPOD data into 
 *PAMGaurd. 
 * <p>
 * Note that this module (CPODControl) originally used a folder of CP1 files as it's file store but
 * this version now converts CP1/CP3 etc. into binary files instead in order to integrate into PAMGuard's 
 * data management system better.
 * 
 * @author Jamie Macaulay
 *
 */
public class CPODControl2 extends PamControlledUnit implements PamSettings {

	/**
	 * The  CPOD detection datablock
	 */
	private CPODClickDataBlock cp1DataBlock;

	/**
	 * The CPOD paramters. 
	 */
	protected CPODParams cpodParams = new CPODParams(new OfflineFileParams()); 

	/**
	 * The CPOD process. 
	 */
	private CPODProcess cpodProcess;


	/**
	 * Holds references to the datagram provider
	 */
	protected CPODDataGramProvider[]  cpodDataGramProvider = new CPODDataGramProvider[2];


	/***GUI Bits***/

	/**
	 * Handles stuff for the FX GUI. 
	 */
	private CPODGUIFX rawGUIFX;

	/**
	 * The settings dialog
	 */
	private PamDialogFX2AWT<CPODParams> settingsDialog;

	/**
	 * Handles swing bits. 
	 */
	private WrapperControlledGUISwing rawDLGUISwing;

	/*
	 * The JavaFX settings pane for the cpod
	 */
	private CPODSettingsPane settingsPane;

	/**
	 * CPOD importer. 
	 */
	private CPODImporter cpodImporter;

	private CPODClickTrainDataBlock clickTrainDataBlock;

	private CPODClickTrainLogging clickTrainDetLogging;


	public CPODControl2(String unitName) {
		super("CPOD", unitName);

		addPamProcess(cpodProcess = new CPODProcess(this));		

		cpodProcess.addOutputDataBlock(cp1DataBlock = new CPODClickDataBlock("CPOD Detections", 
				cpodProcess, CPODMap.FILE_CP1));
		cp1DataBlock.setDatagramProvider(cpodDataGramProvider[0] = new CPODDataGramProvider(this));
		cp1DataBlock.setPamSymbolManager(new CPODSymbolManager(this, 	cp1DataBlock));
		cp1DataBlock.setBinaryDataSource(new CPODBinaryStore(this, cp1DataBlock));
		//must set overlay draw so that hover text can be extracted from general projector and thus plotted on TDGraphFX . This is a HACK and should be sorted. 
		cp1DataBlock.setOverlayDraw(new PamDetectionOverlayGraphics(cp1DataBlock, new PamSymbol())); 

		

		//		// add the CP3 data block
		//		cpodProcess.addOutputDataBlock(cp3DataBlock = new CPODClickDataBlock("CP3 Data", 
		//				cpodProcess, CPODMap.FILE_CP3));
		//
		//		cp3DataBlock.setPamSymbolManager(new CPODSymbolManager(this, 	cp3DataBlock));
		//		cp3DataBlock.setDatagramProvider(cpodDataGramProvider[1] = new CPODDataGramProvider(this));
		//		cp3DataBlock.setBinaryDataSource(new CPODBinaryStore(this, cp1DataBlock));
		//		//must set overlay draw so that hover text can be extracted from general projector and thus plotted on TDGraphFX . This is a HACK and should be sorted. 
		//		cp3DataBlock.setOverlayDraw(new PamDetectionOverlayGraphics(cp3DataBlock, new PamSymbol())); 


		clickTrainDataBlock=  new CPODClickTrainDataBlock(this, cpodProcess, "CPOD Click Trains", 0); 
		clickTrainDataBlock.SetLogging(clickTrainDetLogging = new CPODClickTrainLogging(this, clickTrainDataBlock));
		clickTrainDetLogging.setSubLogging(new CPODSubDetLogging(clickTrainDetLogging, clickTrainDataBlock));
		//create symbols for clicks trains
		clickTrainDataBlock.setPamSymbolManager(new CPODTrainSymbolManager(clickTrainDataBlock));
		
		//makes sure the click trains are loaded
		int maxndays = 5; //maximum days to load. 
		AbstractScrollManager.getScrollManager().addToSpecialDatablock(clickTrainDataBlock, maxndays*24*60*60*1000L , maxndays*24*60*60*1000L);

		cpodProcess.addOutputDataBlock(clickTrainDataBlock);

		PamSettingManager.getInstance().registerSettings(this);
		cpodProcess.setSampleRate(CPODClickDataBlock.CPOD_SR, false);

		cpodImporter = new CPODImporter(this); 

		//FX display data providers
		CPODPlotProviderFX cpodPlotProviderFX = new CPODPlotProviderFX(this, cp1DataBlock);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(cpodPlotProviderFX);

		//		cpodPlotProviderFX = new CPODPlotProviderFX(this, cp3DataBlock);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(cpodPlotProviderFX);
		// register the DD display
		DDPlotRegister.getInstance().registerDataInfo(new CPODDPlotProvider(this, cp1DataBlock));
		//		DDPlotRegister.getInstance().registerDataInfo(new CPODDPlotProvider(this, cp3DataBlock));


		//		//swing time display data providers. 
		//		CPODPlotProvider cpodPlotProvider = new CPODPlotProvider(this, cp1DataBlock);
		//		TDDataProviderRegister.getInstance().registerDataInfo(cpodPlotProvider);
		//		cpodPlotProvider = new CPODPlotProvider(this, cp3DataBlock);
		//		TDDataProviderRegister.getInstance().registerDataInfo(cpodPlotProvider);
		//		
		//		//FX display data providers
		//		CPODPlotProviderFX cpodPlotProviderFX = new CPODPlotProviderFX(this, cp1DataBlock);
		//		TDDataProviderRegisterFX.getInstance().registerDataInfo(cpodPlotProviderFX);
		//		
		//		cpodPlotProviderFX = new CPODPlotProviderFX(this, cp3DataBlock);
		//		TDDataProviderRegisterFX.getInstance().registerDataInfo(cpodPlotProviderFX);


	}


	public long stretchClicktime(long rawTime) {		
		if (cp1DataBlock.getDatagrammedMap() == null) {
			return rawTime;
		}
		long fileStart = cp1DataBlock.getDatagrammedMap().getFirstDataTime(); 
		double stretch = cpodParams.timeStretch/1.e6 * (rawTime-fileStart);
		return (long) (rawTime + cpodParams.startOffset*1000 + stretch);
	}



	@Override
	public Serializable getSettingsReference() {
		return cpodParams;
	}

	@Override
	public long getSettingsVersion() {
		return CPODParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		this.cpodParams = ((CPODParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	public CPODClickDataBlock getCP1DataBlock() {
		return cp1DataBlock;
	}

	//	public CPODClickDataBlock getCP3DataBlock() {
	//		return cp3DataBlock;
	//	}

	/**
	 * Get the CPOD click train data block. 
	 * @return the CPOD click train data block. 
	 */
	public CPODClickTrainDataBlock getClickTrainDataBlock() {
		return this.clickTrainDataBlock;
	}

	/**
	 * Set the CPOD parameters. 
	 * @param newParams - new parameters. 
	 */
	public void setCPODParams(CPODParams newParams) {
		this.cpodParams=newParams;
	}

	/**
	 * Gets the CPOD parameters. 
	 * @return the CPOD parameters
	 */
	public CPODParams getCPODParam() {
		return cpodParams;	
	}

	/****	GUI  ****/

	/**
	 * Get the settings pane.
	 * 
	 * @return the settings pane.
	 */
	public CPODSettingsPane getSettingsPane() {

		if (this.settingsPane == null) {
			settingsPane = new CPODSettingsPane(this);
		}
		return settingsPane;
	}

	/**
	 * Show settings dialog.
	 * 
	 * @param parentFrame - the frame.
	 */
	public void showSettingsDialog(Frame parentFrame) {
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			SettingsPane<CPODParams> setPane = (SettingsPane<CPODParams>) getSettingsPane();
			setPane.setParams(this.cpodParams);
			settingsDialog = new PamDialogFX2AWT<CPODParams>(parentFrame, setPane, false);
			settingsDialog.setResizable(true);
		}
		CPODParams newParams = settingsDialog.showDialog(cpodParams);

		// if cancel button is pressed then new params will be null.
		if (newParams != null) {
			updateParams(newParams);
		}
	}

	/**
	 * Called whenever there are new parameters.. 
	 * @param newParams - new CPOD parameters. 
	 */
	private void updateParams(CPODParams newParams) {
		// TODO Auto-generated method stub

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
				rawGUIFX = new CPODGUIFX(this);
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

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {

		JMenuItem menu = new JMenuItem();
		// no need for nested menus if there is only one option.
		menu.setText("CPOD Importer...");
		menu.addActionListener((action) -> {
			showSettingsDialog(parentFrame);
		});

		return menu;
	}

	/**
	 * Get the CPOD importer. 
	 * @return the CPOD importer.
	 */
	public CPODImporter getCpodImporter() {
		return cpodImporter;
	}

	/**
	 * Import POD data. This will either be a list of CPOD or FPOD files. 
	 * @param files - the files to import. 
	 * @return a list of import Tasks. 
	 */
	public List<Task<Integer>> importPODData(List<File> files) {
		return cpodImporter.importCPODData(files);
	}



}
