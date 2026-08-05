package binaryFileStorage.checker;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.BinaryOfflineDataMapPoint;
import binaryFileStorage.BinaryStore;
import dataMap.OfflineDataMap;

/**
 * Functions for updating all binary files to the latest PAMGuard binary file version. 
 * @author dg50
 *
 */
public class BinaryUpdater implements PamSettings {

	private BinaryStore binaryStore;
	private volatile boolean keepRunning;
	
	private BinaryUpdateParams binaryUpdateParams = new BinaryUpdateParams();
	private UpdateWorker updateWorker;

	public BinaryUpdater(BinaryStore binaryStore) {
		this.binaryStore = binaryStore;
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Show the dialog that will control all the updating. 
	 * @return
	 */
	public boolean showDialog() {
		return BinaryUpdateDialog.showDialog(binaryStore.getGuiFrame(), this);
	}
	
	/**
	 * Get all datablocks in the model that have binary data. 
	 * @return
	 */
	public List<PamDataBlock> getBinaryDataBlocks() {
		ArrayList<PamDataBlock> allB = PamController.getInstance().getDataBlocks();
		ArrayList<PamDataBlock> binB = new ArrayList<>();
		for (PamDataBlock aB : allB) {
			OfflineDataMap bMap = aB.getOfflineDataMap(binaryStore);
			if (bMap == null || bMap.getNumMapPoints() == 0) {
				continue;
			}
			binB.add(aB);
		}
		return binB;
	}
	
	/**
	 * Get a list of selected datablocks. 
	 * @param params
	 * @return
	 */
	public ArrayList<PamDataBlock> getSelectedDataBlocks(BinaryUpdateParams params) {
		ArrayList<PamDataBlock> a = new ArrayList<>();
		List<PamDataBlock> allBlocks = getBinaryDataBlocks();
		for (PamDataBlock aBlock : allBlocks) {
			BinaryUpdateSet as = params.getUpdateSet(aBlock);
			if (as.update) {
				a.add(aBlock);
			}
		}
		return a;
	}

	/**
	 * Get current binary storage folder. 
	 * @return
	 */
	public String getCurrentFolder() {
		return binaryStore.getBinaryStoreSettings().getStoreLocation();	
	}

	/**
	 * Stop the background process 
	 */
	public void stopUpdate() {
		keepRunning = false;
	}
	
	public boolean isRunning() {
		if (updateWorker == null) {
			return false;
		}
		return updateWorker.isDone() == false;
	}
	
	public void runUpdate(UpdateWorkObserver updateObserver) {
		keepRunning = true;
		updateWorker = new UpdateWorker(binaryUpdateParams, updateObserver);
		updateWorker.execute();
	}

	/**
	 * @return the binaryUpdateParams
	 */
	public BinaryUpdateParams getBinaryUpdateParams() {
		return binaryUpdateParams;
	}
	
	/**
	 * @param binaryUpdateParams the binaryUpdateParams to set
	 */
	public void setBinaryUpdateParams(BinaryUpdateParams binaryUpdateParams) {
		this.binaryUpdateParams = binaryUpdateParams;
	}

	/**
	 * Updater swing worker. 
	 * @author dg50
	 *
	 */
	private class UpdateWorker extends SwingWorker<Integer, UpdateWorkProgress> {

		private UpdateWorkObserver updateObserver;
		private BinaryUpdateParams params;

		public UpdateWorker(BinaryUpdateParams params, UpdateWorkObserver updateObserver) {
			this.params = params;
			this.updateObserver = updateObserver;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			ArrayList<PamDataBlock> blocks = getSelectedDataBlocks(params);
			int n = blocks.size();
			for (int i = 0; i < n; i++) {
				updateBlock(blocks.get(i), n, i);
				if (keepRunning == false) {
					break;
				}
			}
			return null;
		}

		private boolean updateBlock(PamDataBlock pamDataBlock, int n, int i) {
			publish(new UpdateWorkProgress(pamDataBlock, null, n, i, -1, -1));
			BinaryDataSource dataSource = pamDataBlock.getBinaryDataSource();
			// we really need the datamap to get the binary files for this block. I think. 
			BinaryOfflineDataMap binaryDataMap = (BinaryOfflineDataMap) pamDataBlock.getOfflineDataMap(binaryStore);
			List<BinaryOfflineDataMapPoint> mapPoints = binaryDataMap.getMapPoints();
			if (mapPoints == null) {
				return false;
			}
			int nFiles = mapPoints.size();
			int iFile = 0;
			for (BinaryOfflineDataMapPoint mapPoint : mapPoints) {
				File srcFile = mapPoint.getBinaryFile(binaryStore);
				File dstFile = mapPoint.getBinaryFile(params.getNewFolderName());
				publish(new UpdateWorkProgress(pamDataBlock, srcFile.getName(), n, i+1, nFiles, iFile));
				binaryStore.updateBinaryFile(pamDataBlock, mapPoint, srcFile, dstFile, true);
				// now do the noise file. Change the names and get on with it. 
				// note that the noise file may not exist!
				File srcNoise = makeNoiseFile(srcFile);
				File dstNoise = makeNoiseFile(dstFile);
				if (srcNoise.exists()) {
					binaryStore.updateBinaryFile(pamDataBlock, mapPoint, srcNoise, dstNoise, false);
				}
				
				
				iFile++;
				if (keepRunning == false) {
					break;
				}
			}
			

			publish(new UpdateWorkProgress(pamDataBlock, null, n, i+1, nFiles, iFile));
			return true;
		}

		private File makeNoiseFile(File src) {
			String path = src.getAbsolutePath();
			path = path.replace(".pgdf", ".pgnf");
			return new File(path);
		}


		@Override
		protected void process(List<UpdateWorkProgress> chunks) {
			if (updateObserver != null) {
				for (UpdateWorkProgress chunk : chunks) {
					updateObserver.update(chunk);
				}
			}
		}

		@Override
		protected void done() {
			super.done();
			if (updateObserver != null) {
				updateObserver.done();
			}
		}

	}

	@Override
	public String getUnitName() {
		return binaryStore.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Binary file updater";
	}

	@Override
	public Serializable getSettingsReference() {
		return binaryUpdateParams;
	}

	@Override
	public long getSettingsVersion() {
		return BinaryUpdateParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.binaryUpdateParams = (BinaryUpdateParams) pamControlledUnitSettings.getSettings();
		return true;
	}

}
