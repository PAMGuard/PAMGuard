package whistlesAndMoans;

import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.DataAutomation;
import PamguardMVC.DataAutomationInfo;
import PamguardMVC.FFTDataHolderBlock;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.toad.TOADCalculator;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import whistlesAndMoans.alarm.WMAlarmCounterProvider;
import whistlesAndMoans.dataSelector.WMDDataSelectCreator;
import whistlesAndMoans.species.WhistleMoanTethysProvider;
import whistlesAndMoans.species.WhistleSpeciesManager;
import whistlesAndMoans.toad.WSLToadCalculator;

public class ConnectedRegionDataBlock extends AbstractWhistleDataBlock<ConnectedRegionDataUnit> implements AlarmDataSource, GroupedDataSource, FFTDataHolderBlock  {

	private WhistleToneConnectProcess parentProcess;
	private WhistleMoanControl whistleMoanControl;
	private WMAlarmCounterProvider wmAlarmCounterProvider;
	private WMDDataSelectCreator dataSelectCreator;
	private WSLToadCalculator wslToadCalculator;
	private WhistleSpeciesManager whistleSpeciesManager;
	private WhistleMoanTethysProvider whistleTethysProvider;
		
	public ConnectedRegionDataBlock(String dataName,
			WhistleMoanControl whistleMoanControl, WhistleToneConnectProcess parentProcess, int channelMap) {
		super(ConnectedRegionDataUnit.class, dataName, parentProcess, channelMap);
		this.parentProcess = parentProcess;
		this.whistleMoanControl = whistleMoanControl;
		// TODO Auto-generated constructor stub
	}

	@Override
	public WhistleToneConnectProcess getParentProcess() {
		return parentProcess;
	}

	@Override
	public AlarmCounterProvider getAlarmCounterProvider() {
		if (wmAlarmCounterProvider == null) {
			wmAlarmCounterProvider = new WMAlarmCounterProvider(whistleMoanControl);
		}
		return wmAlarmCounterProvider;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
	 */
	@Override
	public DataSelectorCreator getDataSelectCreator() {
		if (dataSelectCreator == null) {
			dataSelectCreator = new WMDDataSelectCreator(whistleMoanControl, this);
		}
		return dataSelectCreator;
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		return whistleMoanControl.getWhistleToneParameters();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getTOADCalculator()
	 */
	@Override
	public TOADCalculator getTOADCalculator() {
		if (wslToadCalculator == null) {
			wslToadCalculator = new WSLToadCalculator(whistleMoanControl, this);
		}
		return wslToadCalculator;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDurationRange()
	 */
	@Override
	public double[] getDurationRange() {
		return parentProcess.getDurationRange();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.DataBlockForFFTDataHolder#getFFTparams()
	 */
	@Override
	public int[] getFFTparams() {
		int[] fftParams = new int[2];
		fftParams[0] = getFftLength();
		fftParams[1] = getFftHop();
		return fftParams;
	}

	@Override
	public DataBlockSpeciesManager<ConnectedRegionDataUnit> getDatablockSpeciesManager() {
		if (whistleSpeciesManager == null) {
			whistleSpeciesManager = new WhistleSpeciesManager(this);
		}
		return whistleSpeciesManager;
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (whistleTethysProvider == null) {
			whistleTethysProvider = new WhistleMoanTethysProvider(tethysControl, this);
		}
		return whistleTethysProvider;
	}

	@Override
	public DataAutomationInfo getDataAutomationInfo() {
		return new DataAutomationInfo(DataAutomation.AUTOMATIC);
	}

	

	
	
}
