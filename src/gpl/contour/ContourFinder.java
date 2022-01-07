package gpl.contour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import PamUtils.TxtFileUtils;
import fftManager.FFTDataUnit;
import gpl.DetectedPeak;
import gpl.GPLControlledUnit;
import gpl.GPLParameters;
import gpl.GPLParameters.ConnectType;
import gpl.GPLProcess;
import gpl.whiten.WhitenMatrix;
import weka.core.converters.MatlabSaver;

public class ContourFinder {

	private GPLControlledUnit gplControl;
	
	private GPLProcess gplProcess;

	private double on = 1./9; // make a 3x3 convolution matrix. 
	private double[][] conv = {{on,on,on},{on,on,on},{on,on,on}};
	private int[][] iConv = {{1,1,1},{1,1,1},{1,1,1}};
	
	// search points for connect 4 and connect 8.
	int[][] con4 = {{1,0},{-1,0},{0,1},{0,-1}}; // sides
	int[][] con8 = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}; // sides and diagonals 
	
	public ContourFinder(GPLControlledUnit gplControl, GPLProcess gplProcess) {
		this.gplControl = gplControl;
		this.gplProcess = gplProcess;
	}
	
	/**
	 * Find a contour, or multiple contours, in a detected peak.
	 * Unless there is a fundamental prob with the input data from the peak, this will always return
	 * a list even if it's empty. This is needed in case we're storing null contours. 
	 * @param newPeak Peak, contains all information including lists whitened data and original FFT data. 
	 * @return list of contours (regions rising above threshold)
	 */
	public ArrayList<GPLContour> findContours(DetectedPeak newPeak) {
		if (newPeak == null) {
			return null;
		}
		GPLParameters params = gplControl.getGplParameters();
		double cut = params.contourCut;
		
		// need to make a binary mask of the peak data. 
		ArrayList<double[]> whiteList = newPeak.getwDataList();
		int nTime = whiteList.size();
		if (nTime < 1) {
			return null;
		}
		int nFreq = whiteList.get(0).length;
		/**
		 * Need two arrays for this: binData is a simple binary map of what's over
		 * threshold. 
		 * binData2 is binData convolved by the 3x3 then thresholded again
		 */
		int[][] binData = new int[nTime+2][nFreq+2];
		int[][] binData2 = new int[nTime+2][nFreq+2];
		
		
		/**
		 * TH code makes binary map of > threshold, then convolves with 3x3 matrix
		 * of 1/9ths to smooth it and have points that are within or on the edge of a map.
		 * I think we can do this more quickly in a single step and use 1's instead of 1/9ths. 
		 */
		double[][] whiteData = new double[nTime][];
		for (int i = 0, ib = 1; i < nTime; i++, ib++) {
			double[] wDat = whiteList.get(i);
			whiteData[i] = wDat;
		}

		/**
		 * Also make an array of energy from the original FFT data which 
		 * can be passed around to count from as contours are formed. Also scale 
		 * for FFT length at this point so that energy values for the cont points
		 * and the total for the contour are on the correct scale. 
		 */
		ArrayList<FFTDataUnit> fftList = newPeak.getFFTList();
		double[][] energyData = new double[nTime][];
		double scale = 2./gplProcess.getSourceFFTData().getFftLength();
		for (int i = 0; i < nTime; i++) {
			FFTDataUnit fftUnit = fftList.get(i);
			energyData[i] = fftUnit.getFftData().magsq(gplProcess.binLo, gplProcess.binHi+1);
			for (int j = 0; j < energyData[i].length; j++) {
				energyData[i][j] *= scale;
			}
		}
		
		/*
		 * whiteData is wData from earlier whiteneing. Here it gets whitened yet again. 
		 * Is all this whitening of white things really necessary ? It does make the peaks stand 
		 * out though ! If S and N are sig and noise, our statistic seems to be
		 * (S/N - 1) = (S-N)/N, but this has now been done twice, so hard to connect to 
		 * physical meaning. It 'seems' as though the equivalent in dB should
		 * be 20*log10 of this. 
		 * my binData is TH's msk0, my binData2 is his dm;
		 * Will the central mean always be close to zero ? 
		 */
		double centralMean = WhitenMatrix.quickCentralMean(whiteData);
		/*
		 * central mean seems to always come out very close to 0.4 whatever the input noise level. 
		 * I think this is because the white data (wDat) is abs((S-N)/N), i.e. (S-N)/N would be unit rms, but 
		 * centred around 0. Take the abs and you end up with something with a median around 0.4. Converting this
		 * to dB is pretty meaningless, with wDat having RMS 1 for noise, signal being a lot bigger, 
		 * and centralMean always being about .4 in dB it's probably something like 20*log10(cut+1*.4), which 
		 * for a cut of 6.0 equates to a threshold of about  10dB. Impossible to be really clear though so 
		 * not giving values in the dialog. 
		 */
//		System.out.printf("Contour central mean is %4.2f\n", centralMean);
		for (int i = 0, ib = 1; i < nTime; i++, ib++) {
			double[] wDat = whiteData[i];
			for (int f = 0, fb = 1; f < nFreq-1; f++, fb++) {
				/*
				 * Why the -1 ? It's in Tylers code. Has same effect as increasing threshold by +1. 
				 */
				double qre = wDat[f]/centralMean-1;
				if (qre > cut) {
					binData[ib][fb] = 1;
					for (int x = ib-1; x <= ib+1; x++) {
						for (int y = fb-1; y <= fb+1; y++) {
							binData2[x][y] ++;
						}
					}
				}
			}
		}
		/* 
		 * now we only want to keep the parts of the matrix which are > 2 (which must mean that
		 * they were 1 and had two adjacent friends, or that they were 0 and had three adjacents. 
		 * There may also be values in border, which we'll ignore later.  
		 */
		for (int i = 0; i < nTime+2; i++) {
			for (int f = 0; f < nFreq; f++) {
				binData2[i][f] = binData2[i][f] > 2 ? 1 : 0;
				binData[i][f] += binData2[i][f]; 
			}
		}
		/*
		 * So now binData is the mask of stuff that had multiple adjacent points + the original mask, so 
		 * should be make up of 0, 1 and 2's. 
		 */
		/*
		 * Next step is the connected region search. Make n regions using connect 4. For each sum the total
		 * energy from white data within each. 
		 */
//		double[][] whiteData = (double[][]) whiteList.toArray();
		int nT = binData.length;
		int nF = binData[0].length;
		
//		String str = TxtFileUtils.exportMatlab2DArray(whiteData, 2);
		// whiteData seems to be the same at TH numbers to better than 1%. 
		/*
		 * Note that binData is padded, whiteData isn't. 
		 */
		int[][] con = params.connectType == ConnectType.CONNECT4 ? con4 : con8;
		ArrayList<GPLContour> contours = new ArrayList<>();
		for (int iT = 1; iT < nT-1; iT++) {
			for (int iF = 1; iF < nF-1; iF++) {
				if (binData[iT][iF] > 0) {
					GPLContour newContour = findRegion(binData, whiteData, energyData, iT, iF, con);
					if (newContour.getArea() > params.minContourArea) {
						newContour.sortContourPoints();
						contours.add(newContour);
					}
				}
			}
		}
		
		// sort by energy, biggest first.
		Collections.sort(contours, new ContourEnergySorter());
		
		// then add the null contour so that it's already last in the list. 
		if (gplControl.getGplParameters().keepNullContours) {
			// make a null contour, which is a contour with the energy sums, since
			// we'll still want to output them
			double totNoise = sumArray(whiteData);
			double totEnergy = sumArray(energyData);
			GPLContour nullContour = new NullGPLContour(totNoise, totEnergy, 0, nT-1, gplProcess.binLo, gplProcess.binHi);
			
			contours.add(nullContour);
		}
		
		
		return contours;
	}
	
	private double sumArray(double[][] arrayData) {
		if (arrayData == null) {
			return 0;
		}
		double total = 0.;
		int n = arrayData.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < arrayData[i].length; j++) {
				total += arrayData[i][j];
			}
		}
		return total;
	}

	/**
	 * Sorts contours by total energy, returning biggest first. 
	 * @author dg50
	 *
	 */
	private class ContourEnergySorter implements Comparator<GPLContour> {

		@Override
		public int compare(GPLContour o1, GPLContour o2) {
			return (int) -Math.signum(o1.getTotalExcess() - o2.getTotalExcess());
		}
	}

	/**
	 * Sorts contours by area, returning biggest first. 
	 * @author dg50
	 *
	 */
	private class ContourAreaSorter implements Comparator<GPLContour> {

		@Override
		public int compare(GPLContour o1, GPLContour o2) {
			return -(o1.getArea() - o2.getArea());
		}
	}
	
	/**
	 * Sorts contours by time, returning earliest first
	 * @author dg50
	 *
	 */
	private class ContourTimeSorter implements Comparator<GPLContour> {

		@Override
		public int compare(GPLContour o1, GPLContour o2) {
			int t1 = o1.getContourPoints().get(0).x;
			int t2 = o1.getContourPoints().get(0).x;
			
			return t1-t2;
		}
	}

	/**
	 * Finds a region in binData. The search that is calling this will continue from the
	 * next point, so as points are added to the region, they are set to zero so they don't 
	 * get included in anything else.   
	 * @param binData
	 * @param whiteData 
	 * @param iT
	 * @param iF
	 * @return
	 */
	private GPLContour findRegion(int[][] binData, double[][] whiteData, double[][] energyData, int iT, int iF, int[][] con) {
		/*
		 * Note that binData is padded, whiteData isn't. 
		 */
		GPLContour newContour = new GPLContour(iT-1, iF-1+gplProcess.binLo, whiteData[iT-1][iF-1], energyData[iT-1][iF-1]);
		findAdjacentPoints(newContour, iT, iF, binData, whiteData, energyData, con);
		return newContour;
	}

	/**
	 * Recursive call to find points around the given one. Sets them to zero as they are 
	 * included in the list so that each point is only found once. 
	 * @param newContour
	 * @param currT
	 * @param currF
	 * @param binData
	 * @param whiteData
	 */
	private void findAdjacentPoints(GPLContour newContour, int currT, int currF, int[][] binData, 
			double[][] whiteData, double[][] energyData, int[][] con) {
		/*
		 * Note that binData is padded, whiteData isn't. 
		 * 
		 * Separate loops to get above and to sides, NOT diagonal matches. 
		 * could also easily change to connect 8 instead of connect 4. 
		 */
		binData[currT][currF] = 0; // set current point to 0 so it doesn't get found again
		int nX = binData.length-1;
		int nY = binData[0].length-1;
		for (int i = 0; i < con.length; i++) {
			int nextT = currT+con[i][0];
			int nextF = currF+con[i][1];
			if (nextT < 1 || nextF < 1 || nextT >= nX || nextF >= nY) {
				continue;
			}
			if (binData[nextT][nextF] > 0) {
				newContour.addContourPoint(nextT-1, nextF-1+gplProcess.binLo, whiteData[nextT-1][nextF-1], energyData[nextT-1][nextF-1]);
				findAdjacentPoints(newContour, nextT, nextF, binData, whiteData, energyData, con);
			}
		}
		
	}

	/**
	 * Optionally merge or discard contours depending on merge option.
	 * Note that the contours have already been sorted into size order. If returning many, 
	 * might be worth resorting back into time order. 
	 * @param contours
	 * @param contourMerge
	 * @return
	 */
	public ArrayList<GPLContour> mergeContours(ArrayList<GPLContour> contours, ContourMerge contourMerge) {
		if (contours == null || contours.size() == 1) {
			return contours;
		}
		GPLContour singleContour = null;
		switch (contourMerge) {
		case BIGGEST:
			// keep just the first one. 
			singleContour = contours.get(0);
			break;
		case MERGE:
			singleContour = mergeAll(contours);
			break;
		case SEPARATE:
			return contours;
		default:
			break;
		
		}
		if (singleContour != null) {
			ArrayList<GPLContour> newList = new ArrayList<>(1);
			newList.add(singleContour);
			return newList;
		}
		else {
			return null;
		}
	}

	/**
	 * Merge all the contours into one big one. 
	 * @param contours
	 * @return big merged contour
	 */
	private GPLContour mergeAll(ArrayList<GPLContour> contours) {
		GPLContour bigCont = contours.get(0);
		for (int i = 1; i < contours.size(); i++) {
			ArrayList<GPLContourPoint> points = contours.get(i).getContourPoints();
			for (int p = 0; p < points.size(); p++) {
				bigCont.addContourPoint(points.get(p));
			}
		}
		bigCont.sortContourPoints();
		return bigCont;
	}

}
