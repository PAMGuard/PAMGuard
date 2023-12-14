package clickDetector.offlineFuncs;

import PamguardMVC.PamRawDataBlock;
import offlineProcessing.OfflineTask;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickParameters;
import clickDetector.ClickTabPanelControl;
import clickDetector.dialogs.ClickDelayDialog;
import dataMap.OfflineDataMapPoint;

public class ClickDelayTask extends OfflineTask<ClickDetection> {

	private ClickControl clickControl;
	
	private ClickDetector clickDetector;

	private ClicksOffline clicksOffline;

//	private DelayMeasurementParams delayMeasurementParams;
//
//	private Correlations correlations = new Correlations();
//	private DelayGroup delayGroup = new DelayGroup();
//
//	private int correlationLength;
//	private CPUMonitor cpuMonitor;

	private PamRawDataBlock rawDataSource;

	public ClickDelayTask(ClickControl clickControl) {
		super(clickControl.getClickDataBlock());
		this.clickControl = clickControl;
		clicksOffline = clickControl.getClicksOffline();
		clickDetector = clickControl.getClickDetector();
//		setParentDataBlock(clickControl.getClickDataBlock());
		addAffectedDataBlock(clickControl.getClickDataBlock());
//		cpuMonitor = new CPUMonitor();
	}

	@Override
	public String getName() {
		return "Recalculate click delays";
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean processDataUnit(ClickDetection click) {
//		cpuMonitor.start();
		double[] delays = clickDetector.measureDelays(click);
//		cpuMonitor.stop();
		if (delays != null) {
			click.setDelaysInSamples(delays);
		}
		click.freeClickMemory();
		return (delays != null) ;
	}

//	@Override
//	public boolean processDataUnit(ClickDetection click) {
//		// need to look at options to see whether or not to filter the data
//		// and whether or not to use the envelope.
//
//		correlations = clickDetector.getCorrelations(click);
//		double[][] correlationSignals;
//		if (delayMeasurementParams.envelopeBearings) {
//			if (delayMeasurementParams.filterBearings) {
//				correlationSignals = click.getFilteredAnalyticWaveform(delayMeasurementParams.delayFilterParams);
//			}
//			else {
//				correlationSignals = click.getFilteredAnalyticWaveform(null);
//			}
//		}
//		else {
//			correlationSignals = click.getWaveData(delayMeasurementParams.filterBearings, 
//					delayMeasurementParams.delayFilterParams);
//		}
//		int nChannels = correlationSignals.length;
//		double delaySamples;
//		int iD = 0;
//		DelayMeasurementParams delayParams = clickControl.getClickDetector().getDelayMeasurementParams(click);
//		for (int i = 0; i < nChannels; i++) {
//			for (int j = i+1; j < nChannels; j++) {
//				delaySamples = correlations.getDelay(correlationSignals[i], correlationSignals[j], delayParams, correlationLength);
//				click.setDelay(iD++, delaySamples);
//			}
//		}
//		click.freeClickMemory();
//		return false;
//	}

	@Override
	public void prepareTask() {
//		delayMeasurementParams = clickControl.getClickParameters().getDelayMeasurementParams(0);
//		if (delayMeasurementParams == null) {
//			// use default set - will be no filtering and no envelope
//			delayMeasurementParams = new DelayMeasurementParams();
//		}
		rawDataSource = (PamRawDataBlock) clickDetector.getRawSourceDataBlock(clickDetector.getSampleRate());
//		cpuMonitor.reset();
//		correlationLength = PamUtils.getMinFftLength(clickControl.getClickParameters().maxLength);
	}

	@Override
	public void loadedDataComplete() {
		ClickTabPanelControl ctpc = (ClickTabPanelControl) clickControl.getTabPanel();
		if (ctpc != null) {
			ctpc.offlineDataChanged();
		}
	}
	
	@Override
	public boolean callSettings() {
//		DelayMeasurementParams newParams = DelayOptionsDialog.showDialog(clickControl.getPamView().getGuiFrame(), 
//				clickControl.getClickParameters().getDelayMeasurementParams(0, true));
//		if (newParams != null) {
//			clickControl.getClickParameters().setDelayMeasurementParams(0, newParams.clone());
//			return true;
//		}
		ClickParameters newParams = ClickDelayDialog.showDialog(clickControl.getGuiFrame(), clickControl);
		if (newParams != null) {
			clickControl.setClickParameters(newParams);
			return true;
		}
		return false;
	}


	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public void completeTask() {
		super.completeTask();
//		System.out.println(cpuMonitor.getSummary("Delay measurement times: "));
	}

}
