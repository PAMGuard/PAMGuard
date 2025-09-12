package bearinglocaliser.beamformer;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import beamformer.BeamFormerBaseControl;
import beamformer.BeamFormerBaseProcess;
import beamformer.continuous.BeamOGramDataUnit;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import fftManager.FFTDataBlock;

public class WrappedBeamFormerProcess extends BeamFormerBaseProcess {

	private BearingLocaliserControl bearingLocaliserControl;

	private FFTDataBlock dummyFFTDataBlock;

	private List<BeamOGramDataUnit> collatedBeamOGram = new ArrayList<>();

	public WrappedBeamFormerProcess(BeamFormerBaseControl beamFormerBaseControl, BearingLocaliserControl bearingLocaliserControl, boolean publishBeamOutput) {
		super(beamFormerBaseControl, publishBeamOutput);
		this.bearingLocaliserControl = bearingLocaliserControl;
		dummyFFTDataBlock = new FFTDataBlock("Dummy FFT", bearingLocaliserControl.getBearingProcess(), 0, 512, 256);
		getBeamOGramOutput().addObserver(new BeamDataMonitor());
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
	public boolean isClearAtStart() {
		// definitely always want to clear these data at start, whatever about anything else. 
		return true;
	}


	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseProcess#findFFTDataBlock()
	 */
	@Override
	public FFTDataBlock findFFTDataBlock() {
		return dummyFFTDataBlock;
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseProcess#getFftDataSource()
	 */
	@Override
	public FFTDataBlock getFftDataSource() {
		return dummyFFTDataBlock;
	}

	public void sortFFTBlock() {
		BearingLocaliserParams params = bearingLocaliserControl.getBearingLocaliserParams();
		dummyFFTDataBlock.setChannelMap(params.getChannelBitmap());
		dummyFFTDataBlock.setFftHop(params.getFftHop());
		dummyFFTDataBlock.setFftLength(params.getFftLength());
		dummyFFTDataBlock.setSampleRate(bearingLocaliserControl.getBearingProcess().getSampleRate(), false);
	}

	/* (non-Javadoc)
	 * @see beamformer.BeamFormerBaseProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		super.prepareProcess();
	}

	private class BeamDataMonitor extends PamObserverAdapter {

		/* (non-Javadoc)
		 * @see PamguardMVC.PamObserverAdapter#update(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
		 */
		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			collatedBeamOGram.add((BeamOGramDataUnit) dataUnit);
		}

		@Override
		public String getObserverName() {
			return "Beam form localiser";
		}

	}

	/**
	 * @return the collatedBeamOGram
	 */
	public List<BeamOGramDataUnit> getCollatedBeamOGram() {
		return collatedBeamOGram;
	}

}
