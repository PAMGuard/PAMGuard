package beamformer.algorithms.nullalgo;

import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseProcess;
import beamformer.algorithms.BeamFormerAlgorithm;
import beamformer.algorithms.BeamInformation;
import beamformer.continuous.BeamFormerDataBlock;
import beamformer.continuous.BeamFormerDataUnit;
import beamformer.continuous.BeamFormerProcess;
import fftManager.FFTDataUnit;

/**
 * Not really a beam former. Just passes through FFT data so that we 
 * can debug some of the displays, etc.
 * @author dg50
 *
 */
public class NullBeamFormer implements BeamFormerAlgorithm {

	
	private int channelMap;

	private int firstSeqNum;
	
	private BeamFormerDataBlock beamformerOutput;
	
	public NullBeamFormer(NullBeamProvider nullBeamProvider, BeamFormerBaseProcess beamFormerProcess, BeamAlgorithmParams parameters, int firstSeqNum) {
		super();
		this.channelMap = parameters.getChannelMap();
		this.firstSeqNum = firstSeqNum;
		beamformerOutput = beamFormerProcess.getBeamFormerOutput();
	}

	@Override
	public void process(FFTDataUnit[] fftDataUnits) {
		/*
		 * Doesn't do anything apart from copy the FFT data. No beam forming whatsoever !
		 */
		for (int i = 0; i < fftDataUnits.length; i++) {
			FFTDataUnit fftUnit = fftDataUnits[i];
			int seqMap = 0;
			PamUtils.SetBit(seqMap, firstSeqNum+i, true);
			BeamFormerDataUnit newUnit = new BeamFormerDataUnit(fftUnit.getTimeMilliseconds(), fftUnit.getChannelBitmap(), seqMap,
					fftUnit.getStartSample(), fftUnit.getSampleDuration(), fftUnit.getFftData().clone(), fftUnit.getFftSlice());
			beamformerOutput.addPamData(newUnit);
		}
		
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumBeams() {
		return PamUtils.getNumChannels(channelMap);
	}

	@Override
	public BeamInformation getBeamInformation(int iBeam) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#thereIsABeamogram()
	 */
	@Override
	public boolean thereIsABeamogram() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see beamformer.algorithms.BeamFormerAlgorithm#getNumBeamogramAngles()
	 */
	@Override
	public int getNumBeamogramAngles() {
		return 0;
	}

	@Override
	public void setKeepFrequencyInformation(boolean keep) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFrequencyBinRange(int binFrom, int binTo) {
		// TODO Auto-generated method stub
		
	}


}
