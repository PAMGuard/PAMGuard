package gpl;

import PamUtils.PamUtils;
import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamView.dialog.GroupedSourcePanel;
import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamDataBlock;
import gpl.tethys.GPLSpeciesManager;
import gpl.tethys.GPLTethysProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

public class GPLDetectionBlock extends AcousticDataBlock<GPLDetection> implements GroupedDataSource {

	private GPLProcess gplProcess;
	private GroupedSourceParameters groupedSourceParameters;
	private GPLControlledUnit gplControl;
	
	private GPLSpeciesManager gplSpeciesManager;
	
	private GPLTethysProvider gplTethysProvider;

	public GPLDetectionBlock(GPLProcess gplProcess) {
		super(GPLDetection.class, gplProcess.getGplControlledUnit().getUnitName() + " Detections", gplProcess, 0);
		this.gplProcess = gplProcess;
		gplControl = gplProcess.getGplControlledUnit();
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		PamDataBlock parentData = gplProcess.getParentDataBlock();
		GPLParameters params = gplControl.getGplParameters();
		if (parentData == null) {
			return null;
		}
		int chans = params.sequenceMap;
		int[] chanList = PamUtils.getChannelArray(chans);
		groupedSourceParameters = new GroupedSourceParameters(params.fftSourceName, chans, chanList, GroupedSourcePanel.GROUP_USER);
		return groupedSourceParameters;
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (gplTethysProvider == null) {
			gplTethysProvider = new GPLTethysProvider(tethysControl, gplProcess.getGplControlledUnit(), this);
		}
		return gplTethysProvider;
	}

	@Override
	public DataBlockSpeciesManager<GPLDetection> getDatablockSpeciesManager() {
		if (gplSpeciesManager == null) {
			gplSpeciesManager = new GPLSpeciesManager(this);
		}
		return gplSpeciesManager;
	}

}
