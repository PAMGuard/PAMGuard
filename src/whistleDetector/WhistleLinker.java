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
package whistleDetector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

/**
 * @author Doug Gillespie
 *         <p>
 *         Links up peaks into chains which may become whistles
 * 
 */
public class WhistleLinker extends PamProcess {

	WhistleDetector whistleDetector;

	PeakDetector peakDetector;

	WhistleControl whistleControl;

	// PamDataBlock linkData;
//	ArrayList<WhistleShape> whistleShapes;
	
	LinkedList<WhistleShape> whistleShapes;

	PamDataBlock<PeakDataUnit> peakDataBlock;

	long firstReqSample = 0;

	/**
	 * a bitmap of the channels (or sequences) in this group
	 */
	private int groupChannels;

	/**
	 * the lowest channel/sequence in the groupChannels object
	 */
	private int detectionChannel;

	ArrayList<PeakDataUnit> localSliceList;

	public WhistleLinker(WhistleControl whistleControl,
			WhistleDetector whistleDetector, PeakDetector peakDetector, int groupChannels) {

		super(whistleControl, peakDetector.getOutputDataBlock(0));

		this.peakDetector = peakDetector;
		this.whistleDetector = whistleDetector;
		this.whistleControl = whistleControl;
		setGroupChannels(groupChannels);

		// AddOutputDataBlock(linkData = new
		// PamDataBlock(PamDataBlock.DataTypes.DETECTOR, "Whistle Links",
		// null));
		// whistleDetector.AddOutputDataBlock(linkData);

		/*
		 * Store embryonic whistles in an ArrayList - saves lots of type casting
		 * with PamDataBlock
		 */
		whistleShapes = new LinkedList<WhistleShape>();

		// register with the peakDetectors output data.
		peakDataBlock = peakDetector.getPeakDataBlock();
		peakDataBlock.addObserver(this);

		// make a local array list, which will hold just the slices we're
		// interested in
		localSliceList = new ArrayList<PeakDataUnit>();
	}

	public long getFirstShapeStart() {
		if (whistleShapes != null && whistleShapes.size() > 0) {
			return whistleShapes.getFirst().GetPeak(0).timeMillis;
		}
		return 0;
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		firstReqSample = 0;
	}

	@Override
	public void newData(PamObservable obs, PamDataUnit newUnit) {
		// Should get called each time there is a new DataUnit from an FFT
		// Process that has created whistle peaks
		PeakDataUnit peakDatUnit = (PeakDataUnit) newUnit;
//		if ((peakDatUnit.getChannelBitmap() & 1 << detectionChannel) == 0)
		if ((peakDatUnit.getSequenceBitmap() & 1 << detectionChannel) == 0)
			return;
//		searchForLinks(obs, peakDatUnit, PamUtils.getSingleChannel(peakDatUnit.getChannelBitmap()));
		searchForLinks(obs, peakDatUnit, PamUtils.getSingleChannel(peakDatUnit.getSequenceBitmap()));
	}

	private void searchForLinks(PamObservable obs, PeakDataUnit newDataUnit, int channel) {
		/*
		 * Unfortunately, the C version of this was written in Ansi C and didn't
		 * use any oop whatsoever since it was derived from the right whale
		 * detector code. The only thing to do is start again !
		 */
		// if (true) return;
		// keep a local list of peak data arrays.
		localSliceList.add(newDataUnit);
		ArrayList<WhistlePeak> peakData;

		double df, d2f, deltaF, deltaF2, deltaA, delta;
		int bestPeak, bestPeakTP;
		double bestPeakDelta, bestSlope;
		double hzPerBin = getSampleRate() / whistleDetector.fftLength;
		double binsPerSecond = getSampleRate() / whistleDetector.fftHop;
		double binsToHzPerSecond = hzPerBin * getSampleRate() / whistleDetector.fftHop;

		WhistleParameters whistleParameters = whistleControl.whistleParameters;
		WhistleShape whistleShape;
		ListIterator<WhistleShape> shapesIterator;
		/*
		 * maxGap the maximum number of empty slices you can have at any point
		 * in a whistle - so if it's zero, whistles must be totally continuous.
		 * 
		 * Information in any existing whistles is stored in the output data
		 * block, so we don't need to keep back any more information from more
		 * than (maxGap + 1) slices. i.e. if maxGap is zero, we just need to
		 * hold on to the current data and all previous data can be binned. if
		 * maxGap is 1, then we need to hold onto this slice and the previous
		 * one, since whistles may only go as far as two slices back - got that ?
		 */
		if (localSliceList.size() < whistleParameters.maxGap + 1) {
			return; // not enough data yet to do anything.
		} else if (localSliceList.size() > whistleParameters.maxGap + 1) {
			localSliceList.remove(0);
		}
		firstReqSample = newDataUnit.getStartSample() - whistleDetector.fftHop
		* (whistleParameters.maxGap + 5);
		
//		if (newDataUnit.getSlicenumber() == 2258) {
//			System.out.println("Stop for debug");
//		}
		/*
		 * First go through old data and see if any embryonic whistles have got
		 * too big a gap and need to be either discarded or promoted to full
		 * whistledom
		 * gap is the LAST gap in the whistle and should not be used here - look at the
		 * time between the last peak and the current data. Will probably need a gloabl 
		 * slice number in the data unit for this to work since there may be no peaks at 
		 * all. 
		 */
		int currentSlice = newDataUnit.getSlicenumber();

//		for (int i = 0; i < whistleShapes.size(); i++) {
		shapesIterator = whistleShapes.listIterator();
		while (shapesIterator.hasNext()) {
//			if (whistleShapes.get(i).gap > whistleParameters.maxGap) {
			whistleShape = shapesIterator.next();
			if (currentSlice - whistleShape.getLastPeak().getSliceNo() > whistleParameters.maxGap + 1) {
				/*
				 * send it to the main whistle detector class and remove it from
				 * this list
				 */
				whistleDetector.newWhistleEmbryo(whistleShape, groupChannels,
						detectionChannel);
				// remove it
				shapesIterator.remove();
//				whistleShapes.remove(i);
				// drop i down one - the list has changed a bit and we don't
				// want to miss the next one.
//				i--;
			}
		}
		// now go through and see if it's possible to extend any of these
		// whistles.
		// but first, sort them so that the longest one is looked at first.
		// Collections.sort(whistleShapes);
//		WhistleShape whistleShape;
		WhistlePeak whistlePeak;
		bestPeakTP = 0;
		bestPeak = -1;
		bestPeakDelta = 9999;
//		bestSlope = 0;


//		for (int i = 0; i < whistleShapes.size(); i++) {
		shapesIterator = whistleShapes.listIterator();
		while (shapesIterator.hasNext()) {
			whistleShape = shapesIterator.next();
			// start searching from the end of the existing gap for that whistle
//			whistleShape = whistleShapes.get(i);
//			if (whistleShape.GetPeak(0).absoluteSliceNumber == 9068 && whistleShape.getSliceCount() > 2) {
//				System.out.println(String.format("Pause at bad whistle, time now = %d", newDataUnit.getTimeMilliseconds()));
//			}
			bestPeakTP = 0;
			bestPeak = -1;
			bestPeakDelta = 9999;
			for (int iTP = whistleShape.gap; iTP <= whistleParameters.maxGap; iTP++) {
				/*
				 * Loop over slices and peaks in those slices to find the peak
				 * (if any) which best matches this whistle shape
				 */
				// pTP = ppTPData[maxSoundGap-iTP];
				peakData = localSliceList.get(iTP).getWhistlePeaks();
				bestPeak = -1;
				bestPeakDelta = 9999.;
				for (int iP = 0; iP < peakData.size(); iP++) {
//					if (peakData.get(0).absoluteSliceNumber == 9011) {
//					System.out.println("Pause at bad whistle");
//					}
//					if (whistleShape.GetPeak(0).getSliceNo() == 8925) {
////						System.out.println("slice number = " + String.format("%d", peakData.get(iP).absoluteSliceNumber));
//					}
					if (peakData.get(iP).whistleShape != null) {
						continue;
					}
					if (peakData.get(iP).MaxFreq - peakData.get(iP).MinFreq < whistleParameters.minPeakWidth
							|| peakData.get(iP).MaxFreq
							- peakData.get(iP).MinFreq > whistleParameters.maxPeakWidth)
						continue;
					// sounds are checked in decending order of length, so
					// if a peak has been used , it can't be used again.
					/*dfa = df = ((double) peakData.get(iP).PeakFreq / (double) whistleShape.lastPeakFrequency);// /(double)(iTP-pSound->Gap+1);
					if (dfa < 1.)
						dfa = 1. / dfa;*/
					df = ((double) peakData.get(iP).PeakFreq - 
							(double) whistleShape.getLastPeak().PeakFreq);
					df /= (whistleShape.gap+1);
					df *= binsToHzPerSecond;
//					if (dfa < 1.)
//					dfa = 1. / dfa;
					deltaF = Math.abs(df / whistleParameters.maxDF);
					if (deltaF > 1) continue;
					if (whistleParameters.weightD2F > 0 && whistleShape.getSliceCount() > 1) {
//						d2f = df / whistleShape.gradient; // (pSound->pPeakFreq[lastSlice]-pSound->pPeakFreq[lastSlice-1]);
						d2f = df - whistleShape.gradient * binsToHzPerSecond;
						d2f /= (whistleShape.gap+1);
						d2f *= binsPerSecond;
						// (pSound->pPeakFreq[lastSlice]-pSound->pPeakFreq[lastSlice-1]);
//						if (d2f < 1.)
//						d2f = 1. / d2f;
						deltaF2 = Math.abs(d2f / whistleParameters.maxD2F);
						if (deltaF2 > 1) continue;
					}
					else deltaF2 = 0;

					if (whistleParameters.weightDA > 0) {
						deltaA = Math.abs(10 * Math.log10(peakData.get(iP).MaxAmp/whistleShape.getLastPeak().MaxAmp)) * binsPerSecond;
						deltaA /= whistleParameters.maxDA;
						if (deltaA > 1) continue;
					}
					else deltaA = 0;

					delta = deltaF * whistleParameters.weightDF + 
					deltaF2 * whistleParameters.weightD2F +
					deltaA * whistleParameters.weightDA;

					if (delta < bestPeakDelta) {
						bestPeak = iP;
						bestPeakTP = iTP;
						bestPeakDelta = delta;
						bestSlope = df;
					}
				}
				if (bestPeak >= 0 && bestPeakDelta <= 1.) break;
			} // end of loop over slices and peaks

			if (bestPeak >= 0 && bestPeakDelta <= 1.) {
				/*
				 * Add the best peak to the current whistle
				 */
				peakData = localSliceList.get(bestPeakTP).getWhistlePeaks();
				whistlePeak = peakData.get(bestPeak);

				if (whistlePeak.whistleShape != null) {
					whistleShape.nSoundClash++;
					whistlePeak.whistleShape.nSoundClash++;
				}

				whistleShape.addPeak(whistlePeak);
				whistleShape.gap = bestPeakTP;
			} else {
				whistleShape.gap++;
			}
			
			// pTP = ppTPData[maxSoundGap];
		} // end of loop over whistles which might get extended
		
		peakData = localSliceList.get(0).getWhistlePeaks();
		for (int i = 0; i < peakData.size(); i++) {
			// any peaks without a sound pointer become the start of a new sound
			// tacked onto the end of the Continued Sounds
			if (peakData.get(i).whistleShape == null) {
				if (peakData.get(i).MaxFreq - peakData.get(i).MinFreq < whistleParameters.minPeakWidth
						|| peakData.get(i).MaxFreq
						- peakData.get(i).MinFreq > whistleParameters.maxPeakWidth) {
					continue;
				}
				whistleShape = new WhistleShape(channel);
				whistleShape.addPeak(peakData.get(i));
				whistleShapes.add(whistleShape);
			}
		}
	}


	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		/*
		 * Need to be able to go far enough back to calculate bearings
		 * at the end of the whistle.
		 */
		return whistleDetector.fftHop * (whistleControl.whistleParameters.maxGap + 1);
	}

	@Override
	public void pamStart() {
		whistleShapes.clear();
	}

	@Override
	public void pamStop() {

	}

	public int getGroupChannels() {
		return groupChannels;
	}

	public void setGroupChannels(int groupChannels) {
		this.groupChannels = groupChannels;
		detectionChannel = PamUtils.getLowestChannel(groupChannels);
	}
}
