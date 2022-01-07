package amplifier;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class AmpProcess extends PamProcess {

	AmpControl ampControl;
	
	AmplifiedDataBlock outputDataBlock;

	public AmpProcess(AmpControl ampControl) {
		super(ampControl, null);
		this.ampControl = ampControl;
		
		addOutputDataBlock(outputDataBlock = new AmplifiedDataBlock("Amplified Data", this, 3, 0));

		noteNewSettings();
	}

	@Override
	public void pamStart() {


	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		RawDataUnit rawData = (RawDataUnit) arg;
		PamRawDataBlock rawDataBlock = (PamRawDataBlock) o;
		int channel = PamUtils.getSingleChannel(rawData.getChannelBitmap());

		RawDataUnit newData;
		double[] outputData, inputData;
		double gain = ampControl.ampParameters.gain[channel];

		newData = outputDataBlock.getRecycledUnit();
		if (newData != null) {
			newData.setTimeMilliseconds(rawData.getTimeMilliseconds());
			newData.setChannelBitmap(rawData.getChannelBitmap());
			newData.setStartSample(rawData.getStartSample());
			newData.setSampleDuration(rawData.getSampleDuration());
		}
		else {
			newData = new RawDataUnit(rawData.getTimeMilliseconds(), rawData.getChannelBitmap(), 
					rawData.getStartSample(), rawData.getSampleDuration());
		}
		outputData = newData.getRawData();
		inputData = rawData.getRawData();
		if (outputData == null ||outputData.length != rawData.getSampleDuration()) {
			outputData = new double[rawData.getSampleDuration().intValue()];
		}
		
		for (int i = 0; i < rawData.getSampleDuration(); i++) {
			outputData[i] = inputData[i] * gain;
		}
		
//		newData.amplifyMeasuredAmplitudeByLinear(gain);
		newData.setMeasuredAmplitude(rawData.getMeasuredAmplitude() * Math.abs(gain));
		
		newData.setRawData(outputData);
		outputDataBlock.addPamData(newData);
	}

	@Override
	public void noteNewSettings() {
		
		super.noteNewSettings();
		
		outputDataBlock.setDataGain(ampControl.ampParameters.gain);
		
		PamDataBlock sourceData = PamController.getInstance().getRawDataBlock(
				ampControl.ampParameters.rawDataSource);
		if (sourceData != null) {
			setParentDataBlock(sourceData);
			outputDataBlock.setChannelMap(sourceData.getChannelMap());
		}
	}
	
}
