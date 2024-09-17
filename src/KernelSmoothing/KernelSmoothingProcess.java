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
package KernelSmoothing;

import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.ProcessAnnotation;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * @author Doug Gillespie
 * 
 * Class to smooth out a spectrogram using a Gaussian smoothing kernel Currently
 * fixed to be 3 by 3, but could have others in the future.
 * 
 * 29 October, 2008, Added phase information back into smoothed output so that
 * whistle detector can use the smoothed data to estimate bearings to detected
 * whistles
 */
public class KernelSmoothingProcess extends PamProcess {
	
	private double[][] kernel; // = {{1, 2, 1}, {2, 4, 2}, {1, 2, 1}};
	
	// private double [][] kernel = {{0, 0, 0}, {0, 1, 0}, {0,0,0}};
	private int kernelSize = 3;
	
//	FFTDataSource parentFFTProcess;
	FFTDataBlock sourceData;
	
	FFTDataBlock outputData;
	
	KernelSmoothingControl smoothingControl;
	
	SmoothingChannelProcess[] smoothingChannelProcessList;
	
	public static final String processType = "Noise Reduction";
	public static final String processName = "Gaussian Kernel Smoothing";
	
	public KernelSmoothingProcess(KernelSmoothingControl smoothingControl) {
		
		super(smoothingControl, null);
		
		this.smoothingControl = smoothingControl;
		
		kernel = new double[][] { { 1, 2, 1 }, { 2, 4, 2 }, { 1, 2, 1 } };
		
		double kt = 0;
		
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				kt += kernel[i][j];
			}
		}
		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				kernel[i][j] /= kt;
			}
		}
		
		addOutputDataBlock(outputData = new FFTDataBlock("Data from smoothing kernel", 
				this, 0, 0, 0));
		
		setupProcess();
	}
	
	@Override
	public void noteNewSettings() {
		super.noteNewSettings();
		setupProcess();
	}

	@Override
	public void setParentDataBlock(PamDataBlock newParentDataBlock) {
		sourceData = (FFTDataBlock) newParentDataBlock;
		super.setParentDataBlock(newParentDataBlock);
		if (sourceData != null) {
//			outputData.setChannelMap(smoothingControl.smoothingParameters.channelList);
			outputData.sortOutputMaps(sourceData.getChannelMap(), sourceData.getSequenceMapObject(), smoothingControl.smoothingParameters.channelList);
		}
	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#SetupProcess()
	 */
	@Override
	public void setupProcess() {
		super.setupProcess();
		if (sourceData != null) {
//			int channelMap = getParentDataBlock().getChannelMap() & smoothingControl.smoothingParameters.channelList;
			int chanOrSeqMap = getParentDataBlock().getSequenceMap() & smoothingControl.smoothingParameters.channelList;
//			outputData.setChannelMap(getParentDataBlock().getChannelMap());
//			outputData.setSequenceMap(chanOrSeqMap);
			outputData.sortOutputMaps(getParentDataBlock().getChannelMap(), getParentDataBlock().getSequenceMapObject(), chanOrSeqMap);
			outputData.setFftHop(sourceData.getFftHop());
			outputData.setFftLength(sourceData.getFftLength());
			smoothingChannelProcessList = new SmoothingChannelProcess[PamUtils.getHighestChannel(chanOrSeqMap)+1];
			for (int i = 0; i < PamUtils.getHighestChannel(chanOrSeqMap)+1; i++) {
				smoothingChannelProcessList[i] = new SmoothingChannelProcess();
			}
		}
	}
	
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		/*
		 * Check that we want to precess this channel and have allocated
		 * appropriate stuff for it. 
		 */
		FFTDataUnit newFFTData = (FFTDataUnit) arg;
//		if ((smoothingControl.smoothingParameters.channelList & newFFTData.getChannelBitmap()) == 0) {
		if ((smoothingControl.smoothingParameters.channelList & newFFTData.getSequenceBitmap()) == 0) {
			return;
		}
//		int chan = PamUtils.getSingleChannel(newFFTData.getChannelBitmap());
		int chan = PamUtils.getSingleChannel(newFFTData.getSequenceBitmap());
		smoothingChannelProcessList[chan].newData(o, newFFTData);
	}
	
	
	@Override
	public void pamStart() {
		setupProcess();
	}
	
	@Override
	public void pamStop() {
		
	}
	
	public int getFftHop() {
		if (sourceData != null) return sourceData.getFftHop();
		return 0;
	}
	
	public int getFftLength() {
		if (sourceData != null) 	return sourceData.getFftLength();
		return 0;
	}
	
	@Override
	public ProcessAnnotation getAnnotation(PamDataBlock pamDataBlock, int annotation) {
		// give it the same names as the smoother built into the spectrogram 
		return new ProcessAnnotation(this, this, processType, processName);
	}

	@Override
	public int getNumAnnotations(PamDataBlock pamDataBlock) {
		return 1;
	}

	private class SmoothingChannelProcess {
		
		ComplexArray[] localList;
		
		SmoothingChannelProcess() {
			
			localList = new ComplexArray[kernelSize];
			
		}
		
		protected void newData(PamObservable o, FFTDataUnit arg) {
			// localList.add((Complex) arg.data);
			ComplexArray inputData = arg.getFftData();
			
			localList[kernelSize - 1] = inputData;
			
			if (localList[0] != null) {
				
				/*
				 * KernelSmooth uses a RecycledDatablock for it's output data so get
				 * hold of here and check there is room for the output data. Once
				 * recycled datas start coming through, it will no longer be
				 * necessary to continuously recreate the Complex output data
				 */
				FFTDataUnit newFFTUnit = outputData.getRecycledUnit();
				if (newFFTUnit != null) {
					newFFTUnit.setInfo(arg.getTimeMilliseconds(), arg.getChannelBitmap(), 
							arg.getStartSample(), arg.getSampleDuration(), arg.getFftSlice());
				}
				else {
					newFFTUnit = new FFTDataUnit(arg.getTimeMilliseconds(), arg.getChannelBitmap(), 
							arg.getStartSample(), arg.getSampleDuration(), null, arg.getFftSlice());
//					}
				}
				newFFTUnit.setSequenceBitmap(arg.getSequenceBitmapObject());
//				PamDataUnit outputUnit = getOutputDataBlock(0).getNewUnit(
//				arg.getStartSample(), arg.getDuration(), arg.getChannelBitmap());
				ComplexArray fftData = newFFTUnit.getFftData();
				if (fftData == null || fftData.length() != inputData.length()) {
					fftData = new ComplexArray(inputData.length());
				}
				
				smoothData(inputData.length(), fftData);
				
				newFFTUnit.setFftData(fftData);
				
				outputData.addPamData(newFFTUnit);
			}
			
			/*
			 * finally remove the first item from the list.
			 */
			for (int i = 0; i < kernelSize - 1; i++) {
				localList[i] = localList[i + 1];
			}
		}
		
		private double phaseX, phaseY, mag;
		private void smoothData(int dataLength, ComplexArray fftData) {
			
			// int fftLength = parentProcess.GetFftLength();
			// Complex[] newData = new Complex[fftLength];
			int fOffset = (kernelSize - 1) / 2;
			double realBodge;
			// we'll be adding kernelSise^2 numbers together for every point (yes,
			// this is time consuming)
			
			int dF, dT, tF;
			for (int iF = 0; iF < dataLength; iF++) {
				// newData[iF] = new Complex(0,0);
				realBodge = 0;
				for (dF = 0; dF < kernelSize; dF++) {
					tF = iF + dF - fOffset;
					if (tF < dataLength && tF >= 0) {
						for (dT = 0; dT < kernelSize; dT++) {
							// newData[iF] =
							// newData[iF].add(localList[dT][tF].times(kernel[dT][dF]));
							realBodge += (localList[dT].magsq(tF) * kernel[dT][dF]);
						}
					}
				}
				/*
				 * Now get the phase information out of the central 
				 * point of the smoothing matrix.  
				 */
				mag = localList[1].mag(iF);
				phaseX = localList[1].getReal(iF) / mag;
				phaseY = localList[1].getImag(iF) / mag;
				mag = Math.sqrt(realBodge);
				fftData.set(iF, mag * phaseX, mag * phaseY);
			}
			
		}
		
	}
	
}
