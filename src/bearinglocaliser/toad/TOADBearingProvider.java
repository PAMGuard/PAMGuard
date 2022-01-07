package bearinglocaliser.toad;

import java.awt.Window;

import Array.ArrayManager;
import Array.PamArray;
import PamController.PamController;
import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamguardMVC.AcousticDataBlock;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.BearingProcess;
import bearinglocaliser.algorithms.BearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;
import bearinglocaliser.algorithms.StaticAlgorithmProperties;

public class TOADBearingProvider extends BearingAlgorithmProvider {

	private BearingLocaliserControl bearingLocaliserControl;
	
	private TOADSettingsPane settingsPane;

	private StaticAlgorithmProperties staticProperties = new StaticAlgorithmProperties("TDOA Localiser", "TOAD");

	public TOADBearingProvider(BearingLocaliserControl bearingLocaliserControl) {
		this.bearingLocaliserControl = bearingLocaliserControl;
	}

	@Override
	public SettingsPane<?> getSettingsPane(Window awtWindow, BearingLocaliserParams blParams, BearingAlgorithmParams algoParams) {
		
		// check the array shape.  Only planar and volumetric arrays have parameters  
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		PamArray currentArray = arrayManager.getCurrentArray();
		AcousticDataBlock acousticDataSource = (AcousticDataBlock) PamController.getInstance().getDataBlockByLongName(blParams.getDataSource());
		int phones = acousticDataSource.getChannelListManager().channelIndexesToPhones(algoParams.getChannelMap());
		int arrayShape = arrayManager.getArrayShape(currentArray, phones);
		int nPhones = PamUtils.getNumChannels(phones);
		
		if (hasSettings(arrayShape, nPhones) == false) {
			System.out.println("No available settings pane for array shape " + ArrayManager.getArrayTypeString(arrayShape));
			return null;
		}

		// create the parameters pane and populate it
		TOADBearingParams tbp = (TOADBearingParams) algoParams;
		if (settingsPane==null){
			settingsPane=new TOADSettingsPane(awtWindow);
		}
		settingsPane.setDataSource(acousticDataSource);
		settingsPane.setParams(tbp);
		
		return settingsPane;
	}
	
	private boolean hasSettings(int arrayShape, int nPhones) {
		switch (arrayShape) {
		case ArrayManager.ARRAY_TYPE_LINE:
			return nPhones > 2;
		case ArrayManager.ARRAY_TYPE_PLANE:
		case ArrayManager.ARRAY_TYPE_VOLUME:
			return true;
		}
		return false;
	}
	
	@Override
	public BearingAlgorithm createAlgorithm(BearingProcess bearingProcess, BearingAlgorithmParams algorithmParams, int groupindex) {
		return new TOADBearingAlgorithm(this, bearingProcess, algorithmParams, groupindex);
	}

	@Override
	public StaticAlgorithmProperties getStaticProperties() {
		return staticProperties;
	}

	@Override
	public BearingAlgorithmParams createNewParams(int groupNumber, int groupChanMap) {
		return new TOADBearingParams(groupNumber, groupChanMap);
	}
	
}
