package Filters;

import java.util.ArrayList;
import java.util.Arrays;

import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class FilterProcess extends PamProcess {

	FilterControl filterControl;
	
	PamRawDataBlock outputData;
	
	Filter[] iirfFilters;
	
	private String oldName = "Filtered Data";
	
	public FilterProcess(FilterControl filterControl) {
		
		super(filterControl, null);
		
		this.filterControl = filterControl;

		addOutputDataBlock(outputData = new PamRawDataBlock(getDataName(), this, 0, getSampleRate()));
	}
	
	/**
	 * Earlier version was always using "Filtered Data" as the data name, so if there were
	 * multiple filters, each would get the same name. Do something so that if there is one, 
	 * then it will still be the same, but if > 1, then something unique. 
	 * @return a unique filter data name.
	 */
	private String getDataName() {
		/**
		 * Look to see if other Filter data blocks already exist. 
		 */
		PamDataBlock other = PamController.getInstance().getDataBlock(RawDataUnit.class, oldName);
		if (other == null) {
			return oldName;
		}
		return filterControl.getUnitName() + " Data"; 
	}

	@Override
	public void prepareProcess() {
		setupProcess();
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupProcess() {
		super.setupProcess();
		PamRawDataBlock rawDataBlock = PamController.getInstance().
			getRawDataBlock(filterControl.filterParams.rawDataSource);
		setParentDataBlock(rawDataBlock);
		if (rawDataBlock == null) {
			return;
		}
		boolean initComplete = PamController.getInstance().isInitializationComplete();
		if (initComplete) {
			int rawChannels = rawDataBlock.getChannelMap();
			filterControl.filterParams.channelBitmap &= rawDataBlock.getChannelMap();
			outputData.setChannelMap(filterControl.filterParams.channelBitmap);
		}
		int maxChan = PamUtils.getHighestChannel(filterControl.filterParams.channelBitmap);
		iirfFilters = new Filter[maxChan+1];
		FilterMethod filterMethod = FilterMethod.createFilterMethod(getSampleRate(), filterControl.filterParams.filterParams);
		if (filterMethod == null) {
			return;
		}
		for (int i = 0; i <= maxChan; i++) {
			if ((1<<i & filterControl.filterParams.channelBitmap) > 0) {
				iirfFilters[i] = filterMethod.createFilter(i);
			}
		}
	}
	
	

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		RawDataUnit inputDataUnit = (RawDataUnit) arg;
		if ((inputDataUnit.getChannelBitmap() & filterControl.filterParams.channelBitmap) == 0) return;
		RawDataUnit outputDataUnit = outputData.getRecycledUnit();
		if (outputDataUnit == null) {
			outputDataUnit = new RawDataUnit(inputDataUnit.getTimeMilliseconds(),inputDataUnit.getChannelBitmap(),
					inputDataUnit.getStartSample(), inputDataUnit.getSampleDuration());
		}
		else {
			outputDataUnit.setInfo(inputDataUnit.getTimeMilliseconds(),inputDataUnit.getChannelBitmap(),
					inputDataUnit.getStartSample(), inputDataUnit.getSampleDuration());
		}
		if (outputDataUnit.getRawData() == null || outputDataUnit.getRawData().length != inputDataUnit.getRawData().length) {
			outputDataUnit.setRawData(new double[inputDataUnit.getRawData().length]);
		}
		int chan = PamUtils.getSingleChannel(inputDataUnit.getChannelBitmap());
		iirfFilters[chan].runFilter(inputDataUnit.getRawData(), outputDataUnit.getRawData());
		outputDataUnit.setRawData(outputDataUnit.getRawData(), true);
		AcquisitionProcess acquisitionProcess = (AcquisitionProcess) getSourceProcess();
		outputDataUnit.setCalculatedAmlitudeDB(acquisitionProcess.rawAmplitude2dB(outputDataUnit.getMeasuredAmplitude(),
				PamUtils.getSingleChannel(outputDataUnit.getChannelBitmap()), false));
		outputData.addPamData(outputDataUnit);
	}
	
	@Override
	public  ArrayList getCompatibleDataUnits(){
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(RawDataUnit.class));
	}

}
