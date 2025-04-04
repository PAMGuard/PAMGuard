package group3dlocaliser.offline;

import java.util.ArrayList;

import Array.ArrayManager;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import dataMap.OfflineDataMapPoint;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.Group3DProcess;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.toadsimplex.ToadSimplexLocaliser;
import offlineProcessing.OfflineTask;

public class Group3DOfflineTask extends OfflineTask<PamDataUnit>{

	private Group3DLocaliserControl group3DControl;
	private Group3DProcess group3DProcess;
	/**
	 * @param group3dControl
	 */
	public Group3DOfflineTask(Group3DLocaliserControl group3DControl) {
		super(group3DControl.getGroup3dProcess().getGroup3dDataBlock());
		this.group3DControl = group3DControl;
		group3DProcess = group3DControl.getGroup3dProcess();
		addAffectedDataBlock(group3DProcess.getGroup3dDataBlock());
		PamDataBlock parentData = group3DProcess.getParentDataBlock();
		if (parentData != null) {
			this.addRequiredDataBlock(parentData);
		}
		
		//here be large bugs in all data and select data processing if not included
		this.addRequiredDataBlock(ArrayManager.getArrayManager().getHydrophoneDataBlock());
		this.addRequiredDataBlock(ArrayManager.getArrayManager().getStreamerDatabBlock());
		this.addRequiredDataBlock(ArrayManager.getGPSDataBlock());

	}

	@Override
	public String getName() {
		if (group3DControl == null) {
			return null;
		}
		return group3DControl.getUnitName();
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#prepareTask()
	 */
	@Override
	public void prepareTask() {
		super.prepareTask();
		group3DProcess.prepareProcess();
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
		try {
		group3DProcess.newData(null, dataUnit);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("New data unit added: " +dataUnit);
		return false;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		/*
		 * If it's clicks, they may be filtered by super detection, so we'll need to re-attach 
		 * all sub detections across all data blocks - there has to be a better way !
		 */
		ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDetectorDataBlocks();
//		for (PamDataBlock dataBlock:dataBlocks) {
//			dataBlock.reattachSubdetections(null);
//		}
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#hasSettings()
	 */
	@Override
	public boolean hasSettings() {
		return true;
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#callSettings()
	 */
	@Override
	public boolean callSettings() {
		return group3DControl.showSettingsMenu(group3DControl.getGuiFrame());
	}

	/* (non-Javadoc)
	 * @see offlineProcessing.OfflineTask#setParentDataBlock(PamguardMVC.PamDataBlock)
	 */
	@Override
	public void setParentDataBlock(PamDataBlock dataBlock) {
		super.setParentDataBlock(dataBlock);
		if (dataBlock instanceof ClickDataBlock) {
			ClickDataBlock clickDataBlock = (ClickDataBlock) dataBlock;
			OfflineEventDataBlock evDataBlock = clickDataBlock.getClickControl().getClickDetector().getOfflineEventDataBlock();
			addRequiredDataBlock(evDataBlock);
		}
	}

	@Override
	public void completeTask() {
		LocaliserAlgorithm3D locAlg = group3DProcess.getLocaliserAlgorithm3D();
		if (locAlg instanceof ToadSimplexLocaliser) {
			ToadSimplexLocaliser toadSimplex = (ToadSimplexLocaliser) locAlg;
			toadSimplex.printDiagnostics();
		}
	}


}
