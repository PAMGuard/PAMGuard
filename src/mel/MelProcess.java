package mel;

import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class MelProcess extends PamProcess {

	private MelControl melControl;
	
	private MelConverter melConverter;

	private boolean prepOk;
	
	private MelDataBlock melDataBlock;

	private FFTDataBlock inputFFTData;

	public MelProcess(MelControl melControl) {
		super(melControl, null);
		this.melControl = melControl;
		melDataBlock = new MelDataBlock(melControl, this, 0, 128, 256);
		addOutputDataBlock(melDataBlock);
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
	public void newData(PamObservable o, PamDataUnit arg) {
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
		if (melConverter == null) {
			return;
		}
		if ((fftDataUnit.getChannelBitmap() & melDataBlock.getChannelMap()) == 0) {
			return;
		}
		double[] mels = melConverter.melFromComplex(fftDataUnit.getFftData());
		ComplexArray ca = ComplexArray.realToComplex(mels);
		MelDataUnit mdu = new MelDataUnit(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), ca,
				fftDataUnit.getFftSlice());
		melDataBlock.addPamData(mdu);
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
		melControl.getMelParameters().checkSampleRate(sampleRate);
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		MelParameters melParams = melControl.getMelParameters();
		inputFFTData = (FFTDataBlock) melControl.getPamConfiguration().getDataBlockByLongName(melParams.dataSource);
		setParentDataBlock(inputFFTData);
		if (inputFFTData == null) {
			prepOk = false;
		}
		melDataBlock.setChannelMap(melParams.chanelMap & inputFFTData.getChannelMap());
		melDataBlock.setFftHop(inputFFTData.getFftHop());
		melDataBlock.setFftLength(inputFFTData.getFftLength());
		setSampleRate(inputFFTData.getSampleRate(), false);
		melConverter = new MelConverter(getSampleRate(), melParams.minFrequency, melParams.maxFrequency, inputFFTData.getFftLength(), melParams.nMel, melParams.power);
		prepOk = true;
	}

	@Override
	public boolean prepareProcessOK() {
		prepareProcess();;
		return prepOk;
	}

	/**
	 * @return the inputFFTData
	 */
	public FFTDataBlock getInputFFTData() {
		return inputFFTData;
	}

}
