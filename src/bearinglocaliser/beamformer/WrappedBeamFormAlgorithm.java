package bearinglocaliser.beamformer;

import java.util.ArrayList;
import java.util.List;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.PeakPosition;
import Localiser.algorithms.PeakPosition2D;
import Localiser.algorithms.PeakSearch;
import PamDetection.LocContents;
import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import PamguardMVC.debug.Debug;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamGroupProcess;
import beamformer.continuous.BeamOGramDataUnit;
import beamformer.loc.BeamFormerLocalisation;
import bearinglocaliser.BearingAlgorithmGroup;
import bearinglocaliser.BearingLocalisation;
import bearinglocaliser.BearingProcess;
import bearinglocaliser.algorithms.BaseFFTBearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.beamformer.display.Beam2DPlot;
import bearinglocaliser.display.Bearing2DPlot;
import bearinglocaliser.display.BearingDataDisplay;
import fftManager.FFTDataUnit;
import fftManager.fftorganiser.FFTDataList;
import pamMaths.PamVector;

public class WrappedBeamFormAlgorithm extends BaseFFTBearingAlgorithm {

	private BeamGroupProcess beamGroupProcess;

	private WrappedBeamFormerProcess wrappedBeamFormerProcess;

	private PeakSearch peakSearch;

	private BeamAlgorithmParams beamAlgorithmParams;

	private int nDimensions;
	
	private int locContents;

	private int arrayShape;

	private int groupHydrophones;

	private WrappedBeamFormAlgorithmProvider wrappedBeamFormAlgorithmProvider;
	
	private String shortAlgoName;

	private Beam2DPlot bearing2DPlot;

	private PamVector[] arrayAxes;
	
	public WrappedBeamFormAlgorithm(WrappedBeamFormAlgorithmProvider wrappedBeamFormAlgorithmProvider, WrappedBeamFormerProcess wrappedBeamFormerProcess, 
			BeamGroupProcess beamGroupProcess, BearingProcess bearingProcess, BearingAlgorithmParams algorithmParams,
			int groupIndex) {
		super(bearingProcess, algorithmParams, groupIndex);
		this.wrappedBeamFormAlgorithmProvider = wrappedBeamFormAlgorithmProvider;
		this.wrappedBeamFormerProcess = wrappedBeamFormerProcess;
		this.beamGroupProcess = beamGroupProcess;
		shortAlgoName = wrappedBeamFormAlgorithmProvider.getStaticProperties().getShortName();
		peakSearch = new PeakSearch(true);
		beamAlgorithmParams = ((WrappedBeamFormParams) algorithmParams).getBeamAlgorithmParams();
		beamAlgorithmParams.setNumBeamogram(1);

		int[] slants = beamAlgorithmParams.getBeamOGramSlants();
		nDimensions = 1;
		if (slants != null && slants.length >= 2) {
			if (slants[1] > slants[0]) {
				nDimensions = 2;
			}
		}
		if (nDimensions == 1) {
			locContents = LocContents.HAS_BEARING | LocContents.HAS_AMBIGUITY;
		}
		else {
			locContents = LocContents.HAS_BEARING;
		}
		try {
			ArrayManager arrayManager = ArrayManager.getArrayManager();
			PamArray currentArray = arrayManager.getCurrentArray();
			groupHydrophones = beamAlgorithmParams.getChannelMap();
			groupHydrophones = getFftSourceData().getChannelListManager().channelIndexesToPhones(groupHydrophones);
			arrayShape = arrayManager.getArrayShape(currentArray, groupHydrophones);
			arrayAxes = arrayManager.getArrayVectors(currentArray, groupHydrophones);
		}
		catch (Exception e) {
			System.out.println("Problem configuring beam former: " + e.getMessage());
		}
	}



	@Override
	public BearingLocalisation processFFTData(PamDataUnit pamDataUnit, BearingAlgorithmGroup beamGroup,
			FFTDataList fftDataList) {
		ArrayList<FFTDataUnit> fftDataUnits = fftDataList.getFftDataUnits();
		if (fftDataUnits == null || fftDataUnits.size() == 0) {
			return null;
		}
		beamGroupProcess.resetFFTStore();
		wrappedBeamFormerProcess.getCollatedBeamOGram().clear();
		beamGroupProcess.getBeamFormerBaseProcess().getBeamOGramOutput().clearAll();

		boolean keepF = nDimensions == 1 && bearing2DPlot != null;
		beamGroupProcess.getBeamFormerAlgorithm().setKeepFrequencyInformation(keepF);
		
		/**
		 * Set the frequency range for analysis. this is pretty crude, but will work for any 
		 * data unit that has a single freqeuncy range. Sweeping sounds such as whistles will 
		 * probably override this on a bin by bin basis. 
		 */
		double[] fRange = pamDataUnit.getFrequency();
		if (fRange != null && fRange.length >= 2) {
			int[] binRange = frequencyToBin(fRange);
//			System.out.println("Set freq range to " + FrequencyFormat.formatFrequencyRange(fRange, true));
			beamGroupProcess.getBeamFormerAlgorithm().setFrequencyBinRange(binRange[0], binRange[1]);
		}
		/*
		 * Now see if the data unit has more detailed contour information that
		 * we might use ...
		 */
		double[] lowF = null, highF = null;
		TFContourData contourData = null;
		int nContourCont = 0;
		if (pamDataUnit instanceof TFContourProvider) {
			TFContourProvider cp = (TFContourProvider) pamDataUnit;
			contourData = cp.getTFContourData();
			if (contourData != null) {
				nContourCont = contourData.getContourTimes().length;
				lowF = contourData.getLowFrequency();
				highF = contourData.getHighFrequecy();
				// check if it's simple to match the contour info with the FFT data:
				//				int nFFT = fftDataList.size() / PamUtils.getNumChannels(beamGroup.channelMap);
				//				System.out.printf("Contour with %d slices for %d FFT datas dt1 = %d\n", 
				//						contourData.getContourTimes().length, nFFT, fftDataList.get(0).getTimeMilliseconds()-contourData.getContourTimes()[0]);
			}

		}

		int iGroupCount = 0;
		int firstChannel = fftDataUnits.get(0).getChannelBitmap();
		for (FFTDataUnit fftDataUnit:fftDataUnits) {
			if ((fftDataUnit.getChannelBitmap() & beamGroup.channelMap) == 0) {
				continue;
			}
			fftDataUnit.setParentDataBlock(getFftSourceData());
			if (iGroupCount < nContourCont && fftDataUnit.getChannelBitmap() == firstChannel) {
				int binLo = frequencyToBin(lowF[iGroupCount]);
				int binHi = frequencyToBin(highF[iGroupCount])+1;
				binHi = Math.min(binHi, fftDataUnit.getFftData().length());
				beamGroupProcess.getBeamFormerAlgorithm().setFrequencyBinRange(binLo, binHi);
				iGroupCount++;
			}
			beamGroupProcess.process(fftDataUnit);
		}

		int n = wrappedBeamFormerProcess.getCollatedBeamOGram().size();
		if (n == 0) {
			return null;
		}

		double[] angles;
		switch (nDimensions) {
		case 1:
			angles = interpret1DBeamOGram(wrappedBeamFormerProcess.getCollatedBeamOGram());
			break;
		case 2:
			angles = interpret2DBeamOGram(wrappedBeamFormerProcess.getCollatedBeamOGram());
			break;
		default:
			angles = null;
		}
		if (angles == null) {
			return null;
		}
		/*
		 * The graphics output...
		 */
		if (bearing2DPlot != null) {
			bearing2DPlot.plotBeamData(pamDataUnit, wrappedBeamFormerProcess.getCollatedBeamOGram(), angles);
		}
//		System.out.printf("%d beamogramsreceived for channels %d data channels %d, best angle %3.1f\n", 
//				n, beamGroup.channelMap, pamDataUnit.getChannelBitmap(), Math.toDegrees(angles[0]));
		//		see if it's 1 or 2 dimension
		if (pamDataUnit.getDurationInMilliseconds() > 300) {
			Debug.out.printf("BF Primary angle for UID %d = %3.1f\n", pamDataUnit.getUID(), Math.toDegrees(angles[0]));
		}

		double[] arrayAngles = PamVector.getMinimalHeadingPitchRoll(arrayAxes);
		BearingLocalisation bl = new BearingLocalisation(pamDataUnit, shortAlgoName, 
				locContents, groupHydrophones, angles, null, arrayAngles);
		bl.setSubArrayType(arrayShape);
//		PamVector[] arrayAxis = beamGroupProcess.getArrayMainAxes();
//		bl.setArrayAxis(arrayAxis);
		pamDataUnit.setLocalisation(bl);
		return bl;
	}

	private double[] interpret2DBeamOGram(List<BeamOGramDataUnit> collatedBeamOGram) {
		double[][] angleData = BeamOGramDataUnit.averageAngleAngleData(collatedBeamOGram);
		peakSearch.setWrapDim0(true);
		peakSearch.setWrapStep0(2);
		PeakPosition2D peakPosition = peakSearch.interpolatedPeakSearch(angleData);
		int[] angRange = beamAlgorithmParams.getBeamOGramAngles();
		int[] slantRange = beamAlgorithmParams.getBeamOGramSlants();
		double ang0 = peakPosition.getBin0() * angRange[2] + angRange[0];
		double ang1 = peakPosition.getBin1() * slantRange[2] + slantRange[0];
		double[] ang = {Math.toRadians(ang0), Math.toRadians(ang1)};
		

		return ang;
	}

	private double[] interpret1DBeamOGram(List<BeamOGramDataUnit> collatedBeamOGram) {
		double[] angle1Data = BeamOGramDataUnit.getAverageAngle1Data(collatedBeamOGram);
		// now collapse that 
		peakSearch.setWrapDim0(false);
		PeakPosition peakPosition = peakSearch.interpolatedPeakSearch(angle1Data);
		int[] angRange = beamAlgorithmParams.getBeamOGramAngles();
		double[] ang = {Math.toRadians(peakPosition.getBin() * angRange[2] + angRange[0])};
		
//		/*
//		 * The graphics output...
//		 */
//		if (bearing2DPlot != null) {
//			double[][] faData = BeamOGramDataUnit.averageFrequencyAngle1Data(collatedBeamOGram);
//			double[] aR = new double[2];
//			double[] fR = new double[2];
//			for (int i = 0; i < 2; i++) {
//				aR[i] = angRange[i];
//			}
//			fR[1] = 1000;
//			bearing2DPlot.setData(faData, aR, fR);
//		}
		
		return ang;
	}



	@Override
	public BearingDataDisplay createDataDisplay() {
//		if (bearing2DPlot == null) {
		String plotName = "Beamforming on Channels " + PamUtils.getChannelList(beamAlgorithmParams.getChannelMap());
			bearing2DPlot = new Beam2DPlot(this, plotName, nDimensions, beamAlgorithmParams);
//		}
		return bearing2DPlot;
	}


}
