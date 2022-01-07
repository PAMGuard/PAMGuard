package cpod;

import PamController.PamControlledUnit;
import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamProcess;
import cpod.dataSelector.CPODDataSelectorCreator;

/**
 * Data block which holds CPOD and FPOD detections. 
 * <p>
 * Note that the data block implements GroupedDataSource so that it is compatible with 
 * downstream modules, such as the click train detector. 
 * 
 * @author Doug Gillepsie
 *
 */
public class CPODClickDataBlock extends AcousticDataBlock<CPODClick> {

	protected int clikcType;

	public static final float CPOD_SR = 255*2*1000;
	
	public CPODClickDataBlock(String dataName,
			PamProcess parentProcess, int clikcType) {
		super(CPODClick.class, dataName, parentProcess, 1);
		this.clikcType = clikcType;
		
		this.setDataSelectCreator(new CPODDataSelectorCreator(parentProcess.getPamControlledUnit(), this));
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataName()
	 */
	@Override
	public String getDataName() {
		PamControlledUnit cpodControl = getParentProcess().getPamControlledUnit();
		if (cpodControl == null) {
			return super.getDataName();
		}
		return cpodControl.getUnitName() + "_" + getDataTypeString();
	}

	private String getDataTypeString() {
		switch (clikcType) {
		case CPODMap.FILE_CP1:
			return "CP1";
		case CPODMap.FILE_CP3:
			return "CP3";
		}
		return null;
	}
	
	
	@Override
	public float getSampleRate() {
		return CPOD_SR; 
	}

}
