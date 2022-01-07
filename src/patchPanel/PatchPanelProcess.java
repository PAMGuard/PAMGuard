package patchPanel;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class PatchPanelProcess extends PamProcess {

	protected PatchPanelControl patchPanelControl;
	
	PatchPanelDataBlock outputDataBlock;
	
	double[][] outputData = new double[PamConstants.MAX_CHANNELS][];
	
	int loadedChannels = 0;
	
	public PatchPanelProcess(PatchPanelControl patchPanelControl) {
		super(patchPanelControl, null);
		this.patchPanelControl = patchPanelControl;
		outputDataBlock = new PatchPanelDataBlock("Patch Panel Data", this, 3, 0);
		addOutputDataBlock(outputDataBlock);
		noteNewSettings();
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
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		outputDataBlock.setSampleRate(sampleRate, false);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {

		RawDataUnit rawData = (RawDataUnit) arg;
		// this is from channel 0, 
		// copy it and send as though from channels 0 and 1.
		PamRawDataBlock rawDataBlock = (PamRawDataBlock) o;
		int inputChannel = PamUtils.getSingleChannel(arg.getChannelBitmap());
		
		if (outputData == null) prepareStorage(rawData.getSampleDuration().intValue());
		
		double gain;
		for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
			if ((gain = patchPanelControl.patchPanelParameters.patches[inputChannel][out]) != 0) {
				addChannelData(out, rawData.getRawData(), gain);
			}
		}
		
		if (inputChannel == PamUtils.getHighestChannel(rawDataBlock.getChannelMap())) {
			// time to output the data
			RawDataUnit newData;
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				
				if ((1<<i & outputDataBlock.getChannelMap()) == 0) continue;
				
				newData = outputDataBlock.getRecycledUnit();
				if (newData != null) {
					newData.setTimeMilliseconds(rawData.getTimeMilliseconds());
					newData.setChannelBitmap(1<<i);
					newData.setStartSample(rawData.getStartSample());
					newData.setSampleDuration(rawData.getSampleDuration());
				}
				else {
					newData = new RawDataUnit(rawData.getTimeMilliseconds(), 1<<i, rawData.getStartSample(), rawData.getSampleDuration());
				}
				newData.setFrequency(rawData.getFrequency());
//				newData.setMeasuredAmplitude(newData.getMeasuredAmplitude());
				newData.setRawData(outputData[i], true);
				outputDataBlock.addPamData(newData);
			}
			clearStorage();
		}
	}
	
	private void addChannelData(int channel, double[] data, double gain) {
		for (int i = 0; i < data.length; i++) {
			outputData[channel][i] += (data[i] * gain);
		}
		loadedChannels |= (1<<channel);
	}
	
	private void clearStorage() {
		outputData = null;// new double[PamConstants.MAX_CHANNELS][];
		loadedChannels = 0;
	}
	
	private void prepareStorage(int dataLen) {
		outputData = new double[PamConstants.MAX_CHANNELS][];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & outputDataBlock.getChannelMap()) != 0) {
				outputData[i] = new double[dataLen];
			}
		}
	}
//	public void newData(PamObservable o, PamDataUnit arg) {
//		RawDataUnit rawData = (RawDataUnit) arg;
//		// this is from channel 0, 
//		// copy it and send as though from channels 0 and 1.
//		PamRawDataBlock rawDataBlock = (PamRawDataBlock) o;
//		if (rawDataBlock.getChannelMap() != 1) {
//			outputDataBlock.addPamData(rawData);
//			return;
//		}
//		RawDataUnit newData;
//		for (int i = 0; i < 2; i++) {
//			newData = outputDataBlock.getRecycledUnit();
//			if (newData != null) {
//				newData.setTimeMilliseconds(rawData.getTimeMilliseconds());
//				newData.setChannelBitmap(1<<i);
//				newData.setStartSample(rawData.getStartSample());
//				newData.setDuration(rawData.getDuration());
//			}
//			else {
//				newData = new RawDataUnit(rawData.getTimeMilliseconds(), 1<<i, rawData.getStartSample(), rawData.getDuration());
//			}
//			newData.setFrequency(rawData.getFrequency());
//			newData.setMeasuredAmplitude(newData.getMeasuredAmplitude());
//			newData.setRawData(rawData.getRawData());
//			outputDataBlock.addPamData(newData);
//		}
//	}

	@Override
	public void noteNewSettings() {
		super.noteNewSettings();
		PamDataBlock sourceData = PamController.getInstance().getRawDataBlock(
				patchPanelControl.patchPanelParameters.dataSource);
		setParentDataBlock(sourceData);
		
		PatchPanelParameters pp = patchPanelControl.patchPanelParameters;
		
		pp.configureSummary(sourceData.getChannelMap());

		outputDataBlock.setChannelMap(pp.getOutputChannels());
		outputDataBlock.getPatchPanelChannelListManager().findParentListManager((PamRawDataBlock) sourceData); 
		clearStorage();
	}

	@Override
	public void destroyProcess() {
		// TODO Auto-generated method stub
		super.destroyProcess();
	}

}
