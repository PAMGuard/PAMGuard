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
package decimator;

import java.util.ArrayList;
import java.util.Arrays;

import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterType;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import clickDetector.ClickDetection;
import clipgenerator.ClipDataUnit;

/**
 * Decimates data - i.e. reduces it's frequency by 
 * first of all digitally filtering, then downsampling
 * @author Doug Gillespie
 *<p>
 * Deprecated April 2019. Replaced with DecimatorProcessW which uses the DecimatorWorker class
 * which is much better at upsampling. 
 */
@Deprecated
public class DecimatorProcess extends PamProcess {

	// float sourceSampleRate;
	// int nChan;

	private DecimatorControl decimatorControl;

	private Filter[] filters;

//	private long sampleCounts[];

	private double[][] filteredData;
	private double[][] outputData;
	private int[] outputIndex;
	private long[] outputSampleNumber;

	//PamRawDataBlock inputData;

	private PamRawDataBlock outputDataBlock;

	private float sourceSampleRate = 1;

	private double[] x = new double[PamConstants.MAX_CHANNELS];
	private double[] b1 = new double[PamConstants.MAX_CHANNELS];
	private double[] b2 = new double[PamConstants.MAX_CHANNELS];

	/**
	 * step size between samples. 
	 */
	private float step;

	/**
	 * Number of samples in each output block. 
	 */
	private int outputBlockSize;

	private long[] outputDataStart = new long[PamConstants.MAX_CHANNELS];

	private DecimatorProcess(DecimatorControl controlUnit){//), PamRawDataBlock sourceData) {

		super(controlUnit, null);

		//		inputData = sourceData;
		decimatorControl = controlUnit;
		decimatorControl.decimatorParams.newSampleRate = 2000f;

		setSampleRate(decimatorControl.decimatorParams.newSampleRate, false);

		// DG changed default filter setting from .1 (!!!) to sampleRate / 2.2
		decimatorControl.decimatorParams.filterParams.lowPassFreq = (int) (getSampleRate() / 2.2);
		decimatorControl.decimatorParams.filterParams.filterType = FilterType.CHEBYCHEV;
		decimatorControl.decimatorParams.filterParams.filterBand = FilterBand.LOWPASS;
		decimatorControl.decimatorParams.filterParams.filterOrder = 4;
		decimatorControl.decimatorParams.filterParams.passBandRipple = 2;

		addOutputDataBlock(outputDataBlock = new PamRawDataBlock(controlUnit.getUnitName() + " Data", this,
				0, decimatorControl.decimatorParams.newSampleRate));

		//		sourceData.addObserver(this);

	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#SetupProcess()
	 */
	@Override
	public void setupProcess() {
		super.setupProcess();
		newSettings();
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#SetSampleRate(float, boolean)
	 */
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		sourceSampleRate = sampleRate;
//		super.setSampleRate(decimatorControl.decimatorParams.newSampleRate, false);	
		//		this.sourceSampleRate = sampleRate;
		if (decimatorControl != null) {
			super.setSampleRate(decimatorControl.decimatorParams.newSampleRate, notify);
		}
	}


	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// Don't do anything. Update decimator output when data are added. 
		super.masterClockUpdate(milliSeconds, (long) (sampleNumber/step));
	}

	public void newSettings() {
		// if the data source has changed ...
		PamRawDataBlock rawDataBlock = PamController.getInstance().
		getRawDataBlock(decimatorControl.decimatorParams.rawDataSource);
		if (rawDataBlock != getParentDataBlock()) {
			setParentDataBlock(rawDataBlock);
		}
		if (getParentDataBlock() != null) {
			decimatorControl.decimatorParams.channelMap &= getParentDataBlock().getChannelMap();
			outputDataBlock.setChannelMap(decimatorControl.decimatorParams.channelMap);
			this.setSampleRate(sourceSampleRate, true);
//			outputDataBlock.setSampleRate(decimatorControl.decimatorParams.newSampleRate, true);
			sourceSampleRate = rawDataBlock.getSampleRate();
		}
		setupFilters();
	}

	/**
	 * @return the outputDataBlock
	 */
	public PamRawDataBlock getOutputDataBlock() {
		return outputDataBlock;
	}

	@Override
	public void pamStart() {
		setupFilters();
//		sampleCounts = new long[PamConstants.MAX_CHANNELS];
	}
	
	void setupFilters(){
		// now create the filters.
		if (getParentDataBlock() == null) return;
		filters = new Filter[PamUtils.getHighestChannel(getParentDataBlock()
				.getChannelMap()) + 1];
		filteredData = new double[PamUtils.getHighestChannel(getParentDataBlock()
				.getChannelMap()) + 1][];
		FilterMethod filterMethod = FilterMethod.createFilterMethod(sourceSampleRate, 
				decimatorControl.decimatorParams.filterParams);
		if (filterMethod != null) {
			for (int i = 0; i < PamUtils.getHighestChannel(getParentDataBlock()
					.getChannelMap()) + 1; i++) {
				filters[i] = filterMethod.createFilter(i);
				filters[i].prepareFilter();
			}
		}

	}

	@Override
	public void pamStop() {
	}

	@Override
	public void newData(PamObservable obs, PamDataUnit newData) {
		RawDataUnit rawDataUnit = (RawDataUnit) newData;
		PamRawDataBlock rawDataBlock = (PamRawDataBlock) obs;
		if ((decimatorControl.decimatorParams.channelMap & rawDataUnit.getChannelBitmap())== 0) {
			return;
		}
		int chan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		if (chan >= filteredData.length) {
			return;
		}
		double[] inData = rawDataUnit.getRawData();
		if (filteredData[chan] == null || filteredData.length != inData.length) {
			filteredData[chan] = new double[inData.length];
		}
		filters[chan].runFilter(inData, filteredData[chan]);
		double endBit;

		if (outputBlockSize == 0) {
			/*
			 * Need to round up the decimator value so that the output block length (in abs time)
			 * is never less than the input block, otherwise we will lose the interleaving 
			 * of output data on the rare occasions that the synch between the input and 
			 * output causes a single input to generate exactly two output. By rounding up
			 * this ensures that the opposite happens and just occasionally we get no outputs 
			 * for one of the inputs. 
			 */
			outputBlockSize = (int) Math.ceil(inData.length * getSampleRate() / rawDataBlock.getSampleRate());
		}
		if (outputData[chan] == null) {
			outputData[chan] = new double[outputBlockSize];
		}
		int nData = inData.length;
		while (true) {
			int cb1 = (int) Math.max(0,Math.ceil(b1[chan]));
			int cb2 = (int) Math.ceil(b2[chan]);
			for (int i = cb1; i < cb2; i++) {
				if (i >= nData) {
					b1[chan] -= nData;
					b2[chan] -= nData;
					return; 
				}
				x[chan] += filteredData[chan][i];
			}
			endBit = filteredData[chan][cb2-1]*(cb2-b2[chan]);
			x[chan]-=endBit;
			/*
			 * Now have the correct value, so add to output data stream
			 */
			if (outputIndex[chan] == 0) {
				// get the start time of the next output data unit based on the time of the current 
				// unit + any bin offset within that unit. 
				outputDataStart[chan] = rawDataUnit.getTimeMilliseconds() + (long) (cb1 * 1000 / rawDataBlock.getSampleRate());
			}
			outputData[chan][outputIndex[chan]++] = x[chan]/step;
			b1[chan] = b2[chan];
			b2[chan] = b1[chan] + step;
			/**
			 * and get x ready for the start of the next bin
			 */
			x[chan] = endBit;
			/*
			 * If the block is full, create the data unit and output it. 
			 */
			if (outputIndex[chan] == outputBlockSize) {
				// output the data. 
				
//				RawDataUnit newDataUnit = outputDataBlock.getRecycledUnit();
//				if (newDataUnit != null) {
//					newDataUnit.setInfo(outputDataStart[chan], rawDataUnit.getChannelBitmap(), 
//							outputSampleNumber[chan], outputBlockSize);
//				}
//				else {
				RawDataUnit newDataUnit = new RawDataUnit(outputDataStart[chan], rawDataUnit.getChannelBitmap(), 
							outputSampleNumber[chan], outputBlockSize);
//				}
//				System.out.println("Create decimated data at " + PamCalendar.formatTime2(outputDataStart[chan], 3));
//				newDataUnit.setRawData(Arrays.copyOf(outputData[chan],outputBlockSize), true);
				newDataUnit.setRawData(outputData[chan], true);
				outputDataBlock.addPamData(newDataUnit);
				outputData[chan] = new double[outputBlockSize];
				outputIndex[chan] = 0;
				outputSampleNumber[chan] += outputBlockSize;
			}
		}
	}
	
	@Override
	public void prepareProcess() {
		// do nothing, especially NOT set the sample rate !!!!
		//super.prepareProcess();
		//startMilliseconds = PamCalendar.getTimeInMillis();
		// just zero all the indexes. 
		if (getParentDataBlock() == null) {
			return;
		}
		step = getParentDataBlock().getSampleRate() / getSampleRate();
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			x[i] = b1[i] = 0;
			b2[i] = step;
		}
		outputBlockSize = 0;
		outputData = new double[PamConstants.MAX_CHANNELS][];
		outputIndex = new int[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			outputIndex[i] = 0;//Math.round(step)-1;
		}
		outputSampleNumber = new long[PamConstants.MAX_CHANNELS];
//		System.out.println(getPamControlledUnit().getUnitName() + " prepared.");
		
	}
//
//	@Override
//	public int getOfflineData(PamDataBlock dataBlock, PamObserver endUser,
//			long startMillis, long endMillis, RequestCancellationObject cancellationObject) {
//		setupProcess();
//		prepareProcess();
//		pamStart();
//		return super.getOfflineData(dataBlock, endUser, startMillis, endMillis, cancellationObject);
//	}

	@Override
	public int getOfflineData(OfflineDataLoadInfo offlineLoadDataInfo) {
		setupProcess();
		prepareProcess();
		pamStart();
		if (decimatorControl.getOfflineFileServer() == null) {
			return PamDataBlock.REQUEST_NO_DATA;
		}
		/*
		 * if offline files are not requested, continue to ask up the chain - there may
		 * be data in an upstream process which can still be decimated as normal. 
		 */
		if (decimatorControl.getOfflineFileServer().getOfflineFileParameters().enable == false) {
			return super.getOfflineData(offlineLoadDataInfo);
		}
		if (decimatorControl.getOfflineFileServer().loadData(getOutputDataBlock(), offlineLoadDataInfo, null)) {
			return PamDataBlock.REQUEST_DATA_LOADED;
		}
		else {
			return PamDataBlock.REQUEST_NO_DATA;
		}
	}
	
	
	@Override
	public ArrayList getCompatibleDataUnits(){
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(RawDataUnit.class));
	}
}
