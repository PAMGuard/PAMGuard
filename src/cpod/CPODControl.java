package cpod;

import java.awt.Frame;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import cpod.dataPlotFX.CPODPlotProviderFX;
import cpod.tdPlots.CPODPlotProvider;
import dataGram.DatagramManager;
import dataPlots.data.TDDataProviderRegister;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import fileOfflineData.OfflineFileControl;
import fileOfflineData.OfflineFileDataMap;
import fileOfflineData.OfflineFileMapPoint;
import fileOfflineData.OfflineFileProcess;
import pamScrollSystem.ViewLoadObserver;

/**
 * Module for loading CPOD data and converting into PAMGuard binary fiel format. 
 * @author Doug Gillespie
 * @author Jamie Macaulay
 *
 */	
@Deprecated
public class CPODControl extends OfflineFileControl implements PamSettings {

	private CPODClickDataBlock cp1DataBlock, cp3DataBlock;
	protected CPODDataGramProvider[] cpodDatagramProvider;
	protected CPODParams cpodParams;
	private CPODMap cpodMap;
	OfflineFileDataMap cp3Map;

	public CPODControl(String unitName) {
		super("CPOD", unitName);
		

		// add the CP3 data block
		getOfflineFileProcess().addOutputDataBlock(cp3DataBlock = new CPODClickDataBlock("CP3 Data", 
				getOfflineFileProcess(), CPODMap.FILE_CP3));
		cp3DataBlock.setPamSymbolManager(new CPODSymbolManager(this, 	cp3DataBlock));

		cp3DataBlock.setDatagramProvider(cpodDatagramProvider[1] = new CPODDataGramProvider(this));
		cp3Map = new OfflineFileDataMap(this, cp3DataBlock);
		
		
		//must set overlay draw so that hover text can be extracted from general projector and thus plotted on TDGraphFX . This is a HACK and should be sorted. 
		cp3DataBlock.setOverlayDraw(new PamDetectionOverlayGraphics(cp3DataBlock, new PamSymbol())); 
		cp3DataBlock.addOfflineDataMap(cp3Map);

		setFileParams(cpodParams = new CPODParams(getFileParams()));
		PamSettingManager.getInstance().registerSettings(this);
		getOfflineFileProcess().setSampleRate(200000L, false);
		setDatagramManager(new DatagramManager(this));

		
		//swing time display data providers. 
		CPODPlotProvider cpodPlotProvider = new CPODPlotProvider(this, cp1DataBlock);
		TDDataProviderRegister.getInstance().registerDataInfo(cpodPlotProvider);
		cpodPlotProvider = new CPODPlotProvider(this, cp3DataBlock);
		TDDataProviderRegister.getInstance().registerDataInfo(cpodPlotProvider);
		
		//FX display data providers
		CPODPlotProviderFX cpodPlotProviderFX = new CPODPlotProviderFX(this, cp1DataBlock);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(cpodPlotProviderFX);
		
		cpodPlotProviderFX = new CPODPlotProviderFX(this, cp3DataBlock);
		TDDataProviderRegisterFX.getInstance().registerDataInfo(cpodPlotProviderFX);


	}

	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		return false;
	}

	/**
	 * Convert POD time to JAVA millis - POD time is 
	 * integer minutes past the same epoc as Windows uses
	 * i.e. 0th January 1900.
	 * @param podTime
	 * @return milliseconds. 
	 */
	public static long podTimeToMillis(long podTime) {
		return CPODUtils.podTimeToMillis(podTime);
	}

	public long stretchClicktime(long rawTime) {
		if (cpodMap == null) {
			return rawTime;
		}
		long fileStart = cpodMap.getFileStart();
		double stretch = cpodParams.timeStretch/1.e6 * (rawTime-fileStart);
		return (long) (rawTime + cpodParams.startOffset*1000 + stretch);
	}

	@Override
	protected void processMapFile(File aFile, int fileIndex) {
		System.out.println("Process cpod map file " + aFile.getAbsolutePath());

		// map the cps data. 
		cpodMap = new CPODMap(this, aFile, CPODMap.FILE_CP1);

		// change the file end to CP3
		File cp3File = getCP3File(aFile);
		if (cp3File.exists()) {
			new CPODMap(this, cp3File, CPODMap.FILE_CP3);
		}

	}

	protected static File getCP3File(File cp1File) {
		String newName = cp1File.getAbsolutePath();
		newName = newName.substring(0, newName.length()-4) + ".CP3";
		File cp3File = new File(newName);
		return cp3File;
	}

	@Override
	protected String getOfflineFileType() {
		return "cp1";
	}

	@Override
	protected PamDataBlock createOfflineDataBlock(
			OfflineFileProcess offlineFileProcess) {
		cpodDatagramProvider = new CPODDataGramProvider[2];
		cp1DataBlock = new CPODClickDataBlock("CP1 Data", offlineFileProcess, CPODMap.FILE_CP1);
		cp1DataBlock.setDatagramProvider(cpodDatagramProvider[0] = new CPODDataGramProvider(this));
		cp1DataBlock.setPamSymbolManager(new CPODSymbolManager(this, 	cp1DataBlock));
		
		//must set overlay draw so that hover text can be extracted from general projector and thus plotted on TDGraphFX . This is a HACK and should be sorted. 
		cp1DataBlock.setOverlayDraw(new PamDetectionOverlayGraphics(cp1DataBlock, new PamSymbol())); 
		
		return cp1DataBlock;
	}



	/**
	 * @return the cp1DataBlock
	 */
	public CPODClickDataBlock getCp1DataBlock() {
		return cp1DataBlock;
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
		setFileParams(cpodParams);
		return true;
	}

	/* (non-Javadoc)
	 * @see fileOfflineData.OfflineFileControl#detectionMenuAction(java.awt.Frame)
	 */
	@Override
	public void detectionMenuAction(Frame parentFrame) {
		CPODParams newParams = CPODDialog.showDialog(parentFrame, this, cpodParams);
		
		boolean newDatagram = false; 
		if (newParams != null) {
			
			if (cpodParams.offlineFolder==null || !cpodParams.offlineFolder.equals(newParams.offlineFolder) 
					|| cpodParams.subFolders!= newParams.subFolders) {
				//redo the datagram; 
				newDatagram = true; 
			}
			cpodParams = newParams.clone();	
			setFileParams(cpodParams);
		}
		
		if (newDatagram) PamController.getInstance().updateDataMap();

	}


	public OfflineFileDataMap getOfflineFileDataMap(int cpFileType) {
		switch (cpFileType) {
		case CPODMap.FILE_CP1:
			return super.getOfflineFileDataMap();
		case CPODMap.FILE_CP3:
			return cp3Map;
		}
		return null;
	}


	
	@Override
	public boolean loadData(PamDataBlock dataBlock,
			ArrayList<OfflineFileMapPoint> usedMapPoints, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		// load data from given map points. Note that many map points may 
		// refer to the same file, so probably only need to ever use the first one
		// but will need to scan the rest to see if they refer to a different File. 
		CPODLoader cpodLoader = new CPODLoader(this);
		int n = cpodLoader.loadData(dataBlock, usedMapPoints, offlineDataLoadInfo, loadObserver);
		return n >= 0;
	}

	

}
