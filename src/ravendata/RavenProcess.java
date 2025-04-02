package ravendata;

import java.awt.Color;
import java.util.ArrayList;

import Acquisition.AcquisitionControl;
import PamController.InputStoreInfo;
import PamController.PamController;
import PamView.PamSymbolType;
import PamView.dialog.warn.WarnOnce;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamProcess;
import dataMap.OfflineDataMap;
import dataPlots.data.TDDataProviderRegister;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import generalDatabase.DBControlUnit;
import ravendata.fx.RavenPlotProviderFX;
import ravendata.swing.RavenGraphics;
import ravendata.swing.RavenPlotProvider;

public class RavenProcess extends PamProcess {

	private RavenControl ravenControl;
	
	private RavenDataBlock ravenDataBlock;
	
	private RavenLogging ravenLogging;

	private static SymbolData standardSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 20, 20, false, Color.white, Color.red);

	public RavenProcess(RavenControl pamControlledUnit) {
		super(pamControlledUnit, null);
		this.ravenControl = pamControlledUnit;
		ravenDataBlock = new RavenDataBlock(this, 0);
		addOutputDataBlock(ravenDataBlock);
		ravenLogging = new RavenLogging(pamControlledUnit, ravenDataBlock);
		ravenDataBlock.SetLogging(ravenLogging);
		ravenDataBlock.setOverlayDraw(new RavenGraphics(ravenDataBlock));
		ravenDataBlock.setPamSymbolManager(new StandardSymbolManager(ravenDataBlock, standardSymbol));
		
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new RavenPlotProviderFX(ravenDataBlock));
//		TDDataProviderRegister.getInstance().registerDataInfo(new RavenPlotProvider(ravenDataBlock));
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub	
	}

	public RavenLogging getRavenLogging() {
		return ravenLogging;
	}
	
	protected void createPAMGuardData(RavenFileReader fileReader, ArrayList<RavenDataRow> ravenData) {
		/**
		 * Need to find the acquisition module and then get detailed times of every file, not just
		 * the datamap, which is currently just a normal databsae map of the db entries, so it not 
		 * necessarily precise on the starts / ends of files for the viewer. 
		 */
		AcquisitionControl daqControl = (AcquisitionControl) PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (daqControl == null) {
			WarnOnce.showWarning("No acquisition module", "Can only import ROCCA data if there is a Sound Acquisition Module", 
					WarnOnce.WARNING_MESSAGE);
			return;
		}
		// need to get the detailed data map. 
		InputStoreInfo daqInfo = daqControl.getStoreInfo(true);
		if (daqInfo == null || daqInfo.getAllFileEnds() == null || daqInfo.getAllFileEnds().length == 0) {
			WarnOnce.showWarning("No sound file info module", "Can only import ROCCA data if sound files arre present to extract absolute times", 
					WarnOnce.WARNING_MESSAGE);
			return;
		}
		// need these to look for gaps to convert ROCCA time to abs time. 
		long[] fileStarts = daqInfo.getFileStartTimes();
		long[] fileEnds = daqInfo.getAllFileEnds();
		// make an array of absolute times to match to ROCCA data.
		// these are the end times of each file (start of first file is known to be 0). 
		long[] absTime = new long[fileStarts.length+1]; // one longer to capture end of last file. 
		for (int i = 0; i < fileStarts.length; i++) {
			absTime[i+1] = absTime[i] + fileEnds[i]-fileStarts[i];
		}
	
		// delete all existing data from database. 
		ravenDataBlock.clearAll();
		ravenLogging.deleteData(0, System.currentTimeMillis()*2);
		
		/**
		 * Had to add an offset for some messed up Raven data. May or may not have to include
		 * this as an option in future releases. 
		 * Offset of 2843100 needed for mn23_055a tag data.
		 */
		long offsetMillis = (long) (ravenControl.getRavenParameters().timeOffsetSeconds * 1000.);
		
		RavenDataRow prevRow = null;
		for (RavenDataRow ravenRow : ravenData) {
			if (!ravenRow.equals(prevRow)) {
				/**
				 *  A lot of Raven data appear twice, with different view values. 
				 *  No need to import both. so only doing this if they are different. 
				 */
				double ravenStart = ravenRow.getBeginT()*1000 + offsetMillis;
				int fileInd = getTimeIndex(ravenStart, absTime);
				if (fileInd == absTime.length) {
					String msg = String.format("Data at time %6.4f is beyond the end of available sound file data", ravenRow.getBeginT());
					WarnOnce.showWarning("Error importing RAVEN data", msg, WarnOnce.WARNING_MESSAGE);
					break;
				}
				long fileStart = fileStarts[fileInd];
				long absStart = fileStart + (long) (ravenStart)-absTime[fileInd];
				long duration = (long) ((ravenRow.getEndT()-ravenRow.getBeginT())*1000.);
				int chanMap = 1<<(ravenRow.getChannel()-1);
				RavenDataUnit rdu = new RavenDataUnit(absStart, chanMap, duration, ravenRow.getF1(), ravenRow.getF2());
				rdu.setExtraData(ravenRow.getExtraData());
				getRavenDataBlock().addPamData(rdu);
				ravenLogging.logData(DBControlUnit.findConnection(), rdu);
			}
			prevRow = ravenRow;
		}
		
		OfflineDataMap dataMap = ravenDataBlock.getPrimaryDataMap();
//		dataMap.c
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl != null) {
			dbControl.createOfflineDataMap(ravenControl.getGuiFrame(), ravenControl);
		}

		 dataMap = ravenDataBlock.getPrimaryDataMap();
	}
	
	/**
	 * Find which file time bin the raven data are in 
	 * @param ravenTime
	 * @param absTimes
	 * @return
	 */
	private int getTimeIndex(double ravenTime, long[] absTimes) {
		int i = 0;
		while (i < absTimes.length-1 && ravenTime > absTimes[i+1]) {
			i++;
		}
		return i;
	}

	/**
	 * @return the ravenDataBlock
	 */
	public RavenDataBlock getRavenDataBlock() {
		return ravenDataBlock;
	}

}
