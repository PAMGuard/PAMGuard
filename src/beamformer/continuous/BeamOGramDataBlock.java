package beamformer.continuous;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamConstants;
import PamguardMVC.PamProcess;
import beamformer.BeamFormerBaseProcess;
import dataPlotsFX.data.DataTypeInfo;
import fftManager.FFTDataBlock;

public class BeamOGramDataBlock extends DataBlock2D<BeamOGramDataUnit> {

	private int[] numAngles = new int[PamConstants.MAX_CHANNELS];
	
	private int hopSamples;
	
	private DataTypeInfo scaleInfo = new DataTypeInfo(ParameterType.BEARING, ParameterUnits.DEGREES);
	
	/**
	 * Needed for some amplitude calculations later on. 
	 */
	private int fftLength = 256;
	
	public BeamOGramDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(BeamOGramDataUnit.class, dataName, parentProcess, channelMap);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the numAngles
	 */
	public int getNumAngles(int sequenceNumber) {
		return numAngles[sequenceNumber];
	}

	/**
	 * @param numAngles the numAngles to set
	 */
	public void setNumAngles(int sequenceNumber, int numAngles) {
		this.numAngles[sequenceNumber] = numAngles;
	}

	/**
	 * @param hopSamples the hopSamples to set
	 */
	public void setHopSamples(int hopSamples) {
		this.hopSamples = hopSamples;
	}

	@Override
	public int getHopSamples() {
		return hopSamples;
	}

	@Override
	public int getDataWidth(int sequenceNumber) {
		return numAngles[sequenceNumber];
	}

	@Override
	public double getMinDataValue() {
		return 0;
	}

	@Override
	public double getMaxDataValue() {
		return 180.;
	}

	/**
	 * Need to know the FFT length of the source data so that the magnitude values in the 
	 * data units can be correctly scaled. 
	 * @return the fftLength
	 */
	public int getFftLength() {
		return fftLength;
	}

	/**
	 * Need to set the FFT length of the source data so that the magnitude values in the 
	 * data units can be correctly scaled. 
	 * @param fftLength the fftLength to set
	 */
	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	@Override
	public int getChannelsForSequenceMap(int sequenceMap) {
		return ((BeamFormerBaseProcess) this.getParentProcess()).getChannelsForSequenceMap(sequenceMap);
	}
	

	@Override
	public boolean shouldNotify() {
		return true;
	}

	@Override
	public DataTypeInfo getScaleInfo() {
		return scaleInfo;
	}


//	private int gBinCount = 0;
//	private double[][] gData;
//	/* (non-Javadoc)
//	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit, java.lang.Long)
//	 */
//	@Override
//	public void addPamData(BeamOGramDataUnit pamDataUnit, Long uid) {
//	/**
//	 * Bodges version of addPamData so that we can generate output to compare with GDS
//	 */
//		super.addPamData(pamDataUnit, uid);
//		boolean geraldbodge = true;
//		if (geraldbodge) {
//			int gBins = 5;
//			/*
//			 *  need to output the first 5 frames of the data
//			 */
//			long sn = pamDataUnit.getStartSample();
//			if (pamDataUnit.getLastSample()<= 98304 ) {
//				double[] angData = pamDataUnit.getAngle1Data(false);
//				if (sn <= 0) {
//					gBinCount = 0;
////					gData = new double[gBins][angData.length];
//				}
//				else {
//					gBinCount++;
//				}
//				for (int i = 0; i < angData.length; i++) {
////					gData[gBinCount][i] = angData[i];
//					System.out.printf("%3.6f", angData[i]);
//					if (i < angData.length-1) {
//						System.out.printf(",");
//					}
//					else {
//						System.out.printf("\n");
//					}
//				}
////				System.out.printf("BeamoUnit startsample %d chans %d with %d angles\n", 
////						sn, pamDataUnit.getChannelBitmap(), angData.length);
//				
//			}
//		}
//	}


}
