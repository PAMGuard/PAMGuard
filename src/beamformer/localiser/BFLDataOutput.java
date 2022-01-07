package beamformer.localiser;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import crossedbearinglocaliser.TempDataUnit;

@SuppressWarnings("rawtypes")
public class BFLDataOutput extends PamDataBlock {

	private BeamFormLocProcess bfLocProcess;
	private BeamFormLocaliserControl beamFormerLocaliserControl;

	public BFLDataOutput(BeamFormLocaliserControl beamFormerLocaliserControl, BeamFormLocProcess bfLocProcess, String name) {
		super(PamDataUnit.class, name, bfLocProcess, 0);
		this.beamFormerLocaliserControl = beamFormerLocaliserControl;
		this.bfLocProcess = bfLocProcess;
		this.setLocalisationContents(LocContents.HAS_BEARING);
	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getChannelMap()
	 */
	@Override
	public int getChannelMap() {
		int cmap = bfLocProcess.getBeamFormerLocaliserControl().getBeamFormerParams().getChannelBitmap();
		PamDataBlock tb = getTriggerDataBlock();
		if (tb != null) {
//			cmap &= tb.getChannelMap();
			cmap &= tb.getSequenceMap();
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
		return bfLocProcess.getBeamFormerLocaliserControl().getBfDetectionMonitor().getParentDataBlock();
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
		if (pamDataUnit instanceof TempDataUnit) {
			notifyInstantObservers(pamDataUnit);
		}
		else {
			super.addPamData(pamDataUnit, uid);
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addLocalisationContents(int)
	 */
	@Override
	public void addLocalisationContents(int localisationContents) {
		beamFormerLocaliserControl.addDownstreamLocalisationContents(localisationContents);
		super.addLocalisationContents(localisationContents);
	}

}
