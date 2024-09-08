/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Filters;

import PamguardMVC.PamRawDataBlock;
import fftManager.Complex;

/**
 * @author Doug Gillespie
 *         <p>
 *         Digital filtering with Infinite Impulse Response filter functions A
 *         new instance of this class must be created for each channel
 * 
 */
public class IirfFilter implements Filter {

	private int channel;

	private FilterParams filterParams;

	private PamRawDataBlock rawDataBlock;

	private IIRFilterMethod filterMethod;

	private Complex[] poles, zeros;

	private int nFilterUnits;

	private double sampleRate = 1;

	private FilterUnit[] filterUnits;

	private double filterGainConstant = 1;

	/**
	 * @param channel
	 *            Channel number
	 * @param filterParams
	 *            Parameters for filter operation
	 */
	public IirfFilter(int channel, double sampleRate, FilterParams filterParams) {

		this.channel = channel;
		this.sampleRate = sampleRate;
		setParams(filterParams);

	}

	public void setParams(int channel, FilterParams filterParams, float sampleRate) {

		this.channel = channel;
		this.sampleRate = sampleRate;
		setParams(filterParams);

	}

	/**
	 * Sets parameters then calls stuff to work out all the filter coefficients, etc. 
	 * @param filterParams
	 */
	public void setParams(FilterParams filterParams) {

		this.filterParams = filterParams.clone();

		prepareFilter();

	}


	@Override
	public void prepareFilter() {
		// super.PrepareProcess();

		switch (filterParams.filterType) {
		case BUTTERWORTH:
			filterMethod = new ButterworthMethod(sampleRate, filterParams);
			break;
		case CHEBYCHEV:
			filterMethod = new ChebyshevMethod(sampleRate, filterParams);
			break;
		default:
			filterMethod = null;
		}

		if (filterMethod != null && sampleRate != 0) {
			poles = filterMethod.getPoles(filterParams);
			zeros = filterMethod.getZeros(filterParams);
		} else {
			poles = zeros = null;
		}

		setupRTOperation();
	}

	public void sayFilter(){
		System.out.println(String.format("Filter type %s, %s, order %d, Pole Zero Pairs %d", 
				filterParams.filterType, filterParams.filterBand, filterParams.filterOrder, nFilterUnits));
		for (int i = 0; i < nFilterUnits; i++) {
			System.out.print(String.format("PZ Pair %d ", i));
			filterUnits[i].sayPair();
		}
	}
	
	synchronized public void resetFilter() {
		if (filterUnits == null) {
			return;
		}
		for (int i = 0; i < filterUnits.length; i++) {
			filterUnits[i].reset();
		}
	}

	/**
	 * Setup buffers and filter pairs for real time operation
	 */
	synchronized private void setupRTOperation() {

		if (filterMethod == null || filterParams == null)
			return;
		int pzCount = filterMethod.poleZeroCount();
//		filterMetho/d.
		int nPairs = filterParams.filterOrder / 2;
		int nSingles = filterParams.filterOrder % 2;
		if (filterParams.filterBand == FilterBand.BANDPASS || filterParams.filterBand == FilterBand.BANDSTOP) {
			nPairs *= 2;
			nSingles *= 2;
		}
		nFilterUnits = nPairs + nSingles;
		
//		nFilterUnits = pzCount / 2;
//		int oddOne = -1;
//		if (pzCount % 2 == 1) {
//			oddOne = nFilterUnits;
//			nFilterUnits++;
//		}

		if (nFilterUnits <= 0) return;
		filterUnits = new FilterUnit[nFilterUnits];

		if (poles == null || zeros == null) {
			return;
		}
		int oddBand = getOddBand();
		int i;
		for (i = 0; i < nPairs; i++) {
			/*
			 * The poles and zeros will be symetrical about the real
			 * axis and can be processed in conjugate pairs - do the first half
			 * of the pairs and potentially the odd one in the middle
			 */
			filterUnits[i] = new FilterPair(poles[i], zeros[i], i==oddBand);
		}
		// now check for the odd one
		for (int odd = 0; odd < nSingles; odd++, i++) {
//			new FilterNotPair(null, null);
			filterUnits[i] = new FilterNotPair(poles[i], zeros[i]);
		}

		filterGainConstant = this.filterMethod.getFilterGainConstant();

	}

	private int getOddBand() {
		if (filterParams == null) {
			return-1;
		}
		if (filterParams.filterBand == FilterBand.HIGHPASS || filterParams.filterBand == FilterBand.LOWPASS) {
			return -1;
		}
		int ord = filterParams.filterOrder;
		if (ord%2 == 0) { // no problem if it's even numbered. 
			return -1;
		}
		return ord-1;
	}

	/* (non-Javadoc)
	 * @see Filters.Filter#runFilter(double[])
	 */
	@Override
	synchronized public void runFilter(double[] inputData) {
		int f;
		for (int i = 0; i < inputData.length; i++) {
			for (f = 0; f < nFilterUnits; f++) {
				inputData[i] = filterUnits[f].RunFilter(inputData[i]);
			}
			inputData[i] /= filterGainConstant;
		}
	}

	/* (non-Javadoc)
	 * @see Filters.Filter#runFilter(double[], double[])
	 */
	@Override
	synchronized public void runFilter(double[] inputData, double[] outputData) {
		int f;
		if (outputData == null || outputData.length != inputData.length) {
			outputData = new double[inputData.length];
		}
		for (int i = 0; i < inputData.length; i++) {
			outputData[i] = inputData[i];
			for (f = 0; f < nFilterUnits; f++) {
				outputData[i] = filterUnits[f].RunFilter(outputData[i]);
			}
			outputData[i] /= filterGainConstant;
		}
	}

	/* (non-Javadoc)
	 * @see Filters.Filter#runFilter(double)
	 */
	@Override
	synchronized public double runFilter(double aData) {
		double newData = aData;
		for (int f = 0; f < nFilterUnits; f++) {
			newData = filterUnits[f].RunFilter(newData);
		}
		return newData / filterGainConstant;
	}

	abstract class FilterUnit {

		abstract double RunFilter(double dataIn);

		abstract public void reset();

		abstract public void sayPair();

	}

	class FilterPair extends FilterUnit {

		private double[] HistoryIn, HistoryIna;

		private double[] HistoryOut, HistoryOuta;

		private double yp1, yp2, xp1, xp2; // parameters for difference equations

		private int HistoryShiftSize, DataBlockSize;
		
		private Complex pole, zero;

		FilterPair(Complex P1, Complex Z1, boolean oddBand) {
			// constructor for single pole and single zero
			this.pole = P1;
			this.zero = Z1;
			yp1 = P1.real * 2.;
			yp2 = -P1.magsq();
			xp1 = -Z1.real * 2.;
			xp2 = Z1.magsq();
			if (oddBand) {
				// see longer comment about zero pairing in FastIIRFilter. 
				xp1 = 0;
				xp2 = -xp2;
			}
			HistoryShiftSize = 2;
			// DataBlockSize = sizeof(FILTERDATASIZE) * nChannels;
			HistoryIn = new double[3];
			// HistoryIna = HistoryIn + nChannels;
			HistoryOut = new double[3];
			// HistoryOuta = HistoryOut + nChannels;
		}

		@Override
		public void reset() {
			for (int i = 0; i < 3; i++) {
				HistoryIn[i] = 0;
				HistoryOut[i] = 0;
			}			
		}

		@Override
		public void sayPair() {
			System.out.println(String.format("Elements yp1 %3.1f, yp2 %3.1f, xp1 %3.1f, xp2 %3.1f", 
					yp1, yp2, xp1, xp2));
		}

		@Override
		double RunFilter(double dataIn) {
			// MoveMemory(HistoryIna, HistoryIn, HistoryShiftSize);
			// MoveMemory(HistoryOuta, HistoryOut, HistoryShiftSize);
			// MoveMemory is VERY slow - so use the = instead - it's loads
			// faster
			// but do it backwards since the locations overlap !!!!!

			HistoryIn[2] = HistoryIn[1];
			HistoryIn[1] = HistoryIn[0];
			HistoryOut[2] = HistoryOut[1];
			HistoryOut[1] = HistoryOut[0];
//			for (int i = 2 - 1; i >= 0; i--) {
//				HistoryIn[i + 1] = HistoryIn[i];
//				HistoryOut[i + 1] = HistoryOut[i];
//			}
			HistoryIn[0] = dataIn;
			HistoryOut[0] = HistoryOut[1] * yp1 + HistoryOut[2] * yp2 + HistoryIn[0] + 
					HistoryIn[1] * xp1 + HistoryIn[2] * xp2;
			return HistoryOut[0];
		}

	}
	
	class FilterNotPair extends FilterUnit {

		private double[] HistoryIn, HistoryIna;

		private double[] HistoryOut, HistoryOuta;

		private double yp1, xp1; // parameters for difference equations

		private int HistoryShiftSize, DataBlockSize;

		FilterNotPair(Complex P1, Complex Z1) {
			yp1 = P1.real;
			xp1 = -Z1.real;
			HistoryShiftSize = 1;
			// DataBlockSize = sizeof(FILTERDATASIZE) * nChannels;
			HistoryIn = new double[2];
			// HistoryIna = HistoryIn + nChannels;
			HistoryOut = new double[2];
			// HistoryOuta = HistoryOut + nChannels;
		}

		@Override
		public void reset() {
			for (int i = 0; i < 2; i++) {
				HistoryIn[i] = 0;
				HistoryOut[i] = 0;
			}			
		}

		@Override
		public void sayPair() {
			System.out.println(String.format("Elements 1, yp1 %3.1f, xp1 %3.1f", 
					yp1, xp1));
		}

		@Override
		double RunFilter(double dataIn) {
			// MoveMemory(HistoryIna, HistoryIn, HistoryShiftSize);
			// MoveMemory(HistoryOuta, HistoryOut, HistoryShiftSize);
			// MoveMemory is VERY slow - so use the = instead - it's loads
			// faster
			// but do it backwards since the locations overlap !!!!!
			HistoryIn[1] = HistoryIn[0];
			HistoryOut[1] = HistoryOut[0];
//			for (int i = 0; i >= 0; i--) {
//				HistoryIn[i + 1] = HistoryIn[i];
//				HistoryOut[i + 1] = HistoryOut[i];
//			}
			HistoryIn[0] = dataIn;
			HistoryOut[0] = HistoryOut[1] * yp1 + HistoryIn[0] + HistoryIn[1] * xp1;
			return HistoryOut[0];
		}

	}
	//
	//	public void pPamStart() {
	//		// nothing to do here for this class - it all happens in update
	//
	//	}
	//
	//	public void pamStop() {
	//		// nothing to do here for this class - it all happens in update
	//	}

	public FilterMethod getFilterMethod() {
		return filterMethod;
	}

	public void setFilterMethod(IIRFilterMethod filterMethod) {
		this.filterMethod = filterMethod;
	}

	@Override
	public int getFilterDelay() {
		return nFilterUnits / 2;
	}
}
