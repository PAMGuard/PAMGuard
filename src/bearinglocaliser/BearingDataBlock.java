package bearinglocaliser;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class BearingDataBlock extends PamDataBlock<PamDataUnit> implements GroupedDataSource {
	
	private BearingLocaliserControl bearingControl;
	private BearingProcess bearingProcess;

	public BearingDataBlock(BearingLocaliserControl bearingControl, BearingProcess bearingProcess, String name) {
		super(PamDataUnit.class, name, bearingProcess, 0);
		this.bearingControl = bearingControl;
		this.bearingProcess = bearingProcess;
		this.setLocalisationContents(LocContents.HAS_BEARING);
	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getChannelMap()
	 */
	@Override
	public int getChannelMap() {
		int cmap = bearingControl.getBearingLocaliserParams().getChannelBitmap();
		PamDataBlock tb = getTriggerDataBlock();
		if (tb != null) {
			cmap &= tb.getChannelMap();
		}
		return cmap;
	}



	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getLocalisationContents()
	 */
	@Override
	public LocalisationInfo getLocalisationContents() {
		PamDataBlock tb = getTriggerDataBlock();
		if (tb != null) {
			addLocalisationContents(tb.getLocalisationContents().getLocContent());
		}
		return super.getLocalisationContents();
	}



	/**
	 * Get the trigger data units data block. 
	 * @return trigger data units data block. 
	 */
	private PamDataBlock getTriggerDataBlock() {
		return bearingProcess.getSourceDataBlock();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit, java.lang.Long)
	 */
	@Override
	public void addPamData(PamDataUnit pamDataUnit, Long uid) {
		/**
		 * Modification of the standard addPamData. In the special case of it 
		 * being a temp data unit, we only want to notify the instant observers. 
		 * Currently the only possible instant observer is a crossed bearing localiser. This
		 * will add loc data to the 'real' data unit, after which, this one is no longer
		 * needed. 
		 * No need to add datablock id, UID or anything !
		 */
//		if (pamDataUnit instanceof TempDataUnit) {
//			notifyInstantObservers(pamDataUnit);
//		}
//		else {
			super.addPamData(pamDataUnit, uid);
//		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addLocalisationContents(int)
	 */
	@Override
	public void addLocalisationContents(int localisationContents) {
		bearingControl.addDownstreamLocalisationContents(localisationContents);
		super.addLocalisationContents(localisationContents);
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		return bearingControl.getBearingLocaliserParams().getRawOrFFTSourceParameters();
	}
}
