package cepstrum;

import PamController.PamController;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FFTPluginPanelProvider;
import fftManager.FastFFT;

public class CepstrumProcess extends PamProcess {

	private CepstrumDataBlock cepstrumDataBlock;
	private CepstrumControl cepstrumControl;
	private FastFFT fft;
	
	public CepstrumProcess(CepstrumControl cepstrumControl) {
		super(cepstrumControl, null);
		
		this.cepstrumControl = cepstrumControl;
		cepstrumDataBlock = new CepstrumDataBlock("Cepstrum Data", this, 0, 512, 256);
		addOutputDataBlock(cepstrumDataBlock);
		
		new FFTPluginPanelProvider(cepstrumDataBlock);
		
		fft = new FastFFT();
	}


	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		CepstrumParams cepstrumParams = cepstrumControl.getCepstrumParams();
		PamDataBlock sourceDataBlock = PamController.getInstance().getDataBlockByLongName(cepstrumParams.sourceDataBlock);
		if (sourceDataBlock == null) {
			return;
		}
		FFTDataBlock fftDataBlock = (FFTDataBlock) sourceDataBlock;
		setParentDataBlock(sourceDataBlock);
		cepstrumDataBlock.setFftLength(fftDataBlock.getFftLength());
		cepstrumDataBlock.setFftHop(fftDataBlock.getFftHop());
		cepstrumDataBlock.setChannelMap(cepstrumParams.channelMap);
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit dataUnit) {
		
		// make sure this is a channel we are interested in
		if ((dataUnit.getChannelBitmap() & cepstrumControl.getCepstrumParams().channelMap) == 0) return;

		FFTDataUnit fftDataUnit = (FFTDataUnit) dataUnit;
		ComplexArray fftData = fftDataUnit.getFftData();
		int fftLen = fftData.length();
		ComplexArray logmagdata = new ComplexArray(fftLen*2);
		double v;
		for (int i = 0, j = fftLen*2-1; i < fftLen; i++, j--){
			v = Math.log(fftData.magsq(i))/2.;
			logmagdata.setReal(i, v);
			logmagdata.setReal(i, v);
		}
		fft.ifft(logmagdata, fftLen*2);
		/*
		 * now put back on a linear scale or magnitude information will get mesed up. 
		 */
		ComplexArray outputData = new ComplexArray(fftLen);
		for (int i = 0; i < outputData.length(); i++) {
			v = (logmagdata.getReal(i));
			outputData.set(i, v, 0);
		}
		
		CepstrumDataUnit newDataUnit = new CepstrumDataUnit(fftDataUnit.getTimeMilliseconds(), 
				fftDataUnit.getChannelBitmap(), fftDataUnit.getStartSample(), fftLen, outputData, 
				fftDataUnit.getFftSlice());
		cepstrumDataBlock.addPamData(newDataUnit);
		
	}

}
