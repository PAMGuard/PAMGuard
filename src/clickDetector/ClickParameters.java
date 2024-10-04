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

package clickDetector;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;

import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import Localiser.DelayMeasurementParams;
import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.GroupedSourceParameters;
import PamView.dialog.GroupedSourcePanel;
import PamView.paneloverlay.overlaymark.MarkDataSelectorParams;
import PamguardMVC.PamConstants;
import clickDetector.ClickClassifiers.ClickClassifierManager;
import clickDetector.localisation.ClickLocParams;
import fftFilter.FFTFilterParams;

public class ClickParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 8;

	@Deprecated // now in groupedSourceParameters.
	private String rawDataSource;

	@Deprecated // now in groupedSourceParameters.
	private int channelBitmap = 3;

	public int triggerBitmap = 0xFFFFFFFF;
	
	private GroupedSourceParameters groupedSourceParameters = new GroupedSourceParameters(null, 3, null, GroupedSourcePanel.GROUP_ALL);
	
	/**
	 * Min number of channels that have to be triggered 
	 * for a click to be read. 
	 */
	public int minTriggerChannels = 1;

	@Deprecated // now in groupedSourceParameters.
	private int[] channelGroups;

	@Deprecated // now in groupedSourceParameters.
	private int groupingType = GroupedSourcePanel.GROUP_ALL;

	public double dbThreshold = 10.;

	public double longFilter = 0.00001;

	public double longFilter2 = longFilter / 10;

	public double shortFilter = 0.1;

	public int preSample = 40;

	public int postSample = 40;

	public int minSep = 100;

	public int maxLength = 1024;

	public FilterParams preFilter = new FilterParams();

	public FilterParams triggerFilter = new FilterParams();

	public boolean sampleNoise = true;

	public double noiseSampleInterval = 5;
	
	/**
	 * Store trigger background in binary stores 
	 */
	public boolean storeBackground = true;
	
	/**
	 * Interval for storing background measurements. 
	 */
	public int backgroundIntervalMillis = 5000;
	
	/**
	 * The type of classifier to use - CLASSIFY_SWEEP is the default. 
	 */
	public int clickClassifierType = ClickClassifierManager.CLASSIFY_BETTER;
	
	public boolean runEchoOnline = false;
	
	public boolean discardEchoes = false;
	
	/**
	 * Run classification in real time / online ops. 
	 */
	public boolean classifyOnline;

	public boolean discardUnclassifiedClicks;
	/*
	 * Stuff to do with database storage
	 */
//	public boolean saveAllClicksToDB = false;

	/*
	 * Stuff to do with file storage
	 */
	// changed to false 17/8/08
	public boolean createRCFile = false;

	public boolean rcAutoNewFile = true;

	public float rcFileLength = 1;

	public String storageDirectory = new String();

	public String storageInitials = new String();

	/*
	 * Stuff to do with audible alarm
	 */
    public ArrayList<ClickAlarm> clickAlarmList = new ArrayList<ClickAlarm>();

	/**
	 * Make the trigger function output data available as raw data so it can be viewed. 
	 */
	public boolean publishTriggerFunction = false;

	/*
	 * Waveform display options 
	 */

	/**
	 * Show the envelope waveform
	 */
	public boolean waveShowEnvelope = false;

	/**
	 * Stop auto scaling the x axis - fix it at the max click length. 
	 */
	public boolean waveFixedXScale = false;
	
	/**
	 * view a filtered waveform in the display
	 */
	public boolean viewFilteredWaveform = false;
	
	/**
	 * Parameters for waveform filter. 
	 */
	public FFTFilterParams waveformFilterParams;
	
	/**
	 * Click localiser paramaters. 
	 */
	public ClickLocParams clickLocParams= new ClickLocParams(); 
	
	/**
	 * Single plot of waveforms on top of each other - rather than one per channel
	 */
	public boolean singleWavePlot = false;
	
	private DelayMeasurementParams delayMeasurementParams = new DelayMeasurementParams();
	
	private Hashtable<Integer, DelayMeasurementParams> delayMeasurementTypeParams;
	
	/**
	 * How to colour clicks on radar displays (this will apply to 
	 * all radars - not possible to do them individually at the moment). 
	 */
	public int radarColour = BTDisplayParameters.COLOUR_BY_TRAIN;

	/**
	 * How to colour clicks on spectrogram displays (this will apply to 
	 * all radars - not possible to do them individually at the moment). 
	 */
	public int spectrogramColour = BTDisplayParameters.COLOUR_BY_TRAIN;
	
	private MarkDataSelectorParams overlayMarkDataSelectorParams;
	/*
	 * Parameters for map display.
	 */

	// stuff for map display
	public static final int LINES_SHOW_NONE = 0;
	public static final int LINES_SHOW_SOME = 1;
	public static final int LINES_SHOW_ALL = 2;
	@Deprecated
	public int showShortTrains = LINES_SHOW_SOME;
	@Deprecated
	public double minTimeSeparation = 60;
	@Deprecated
	public double minBearingSeparation = 5;
	@Deprecated
	public double defaultRange = 5000;
	@Deprecated
	public boolean plotIndividualBearings = false;

	/*
	 * parameters for click train identification ...
	 */
	// these have now all been moved to a separate ClickTrainIdParams class. 
//	public boolean runClickTrainId = false;
//	public double[] iciRange = {0.1, 2.0};
//	public double maxIciChange = 1.2;
//	public double okAngleError = 2.0;
//	public double initialPerpendicularDistance = 100;
//	public int minTrainClicks = 6;
//	public double iciUpdateRatio = 0.5; //1 == full update, 0 = no update

	public ClickParameters() {

		channelGroups = new int[PamConstants.MAX_CHANNELS];

		preFilter.filterBand = FilterBand.HIGHPASS;
		preFilter.filterOrder = 4;
		preFilter.filterType = FilterType.BUTTERWORTH;
		preFilter.highPassFreq = 500;

		triggerFilter.filterBand = FilterBand.HIGHPASS;
		triggerFilter.filterOrder = 2;
		triggerFilter.filterType = FilterType.BUTTERWORTH;
		triggerFilter.highPassFreq = 2000;

		storageDirectory = new String("C:\\clicks");
		storageInitials = new String("RBC");

        /* add a ClickAlarm to the ClickAlarmList.  Note that there must always
         * be at least 1 ClickAlarm in the list, and the first ClickAlarm is
         * the default alarm
         */
        clickAlarmList.add(new ClickAlarm());
	}

	@Override
	public ClickParameters clone() {
		try {
			// set some defaults for new parameters (which will set to zero)
			if (groupedSourceParameters == null) {
				groupedSourceParameters = new GroupedSourceParameters(rawDataSource, channelBitmap, channelGroups, groupingType);
			}
			ClickParameters n = (ClickParameters) super.clone();
			if (n.noiseSampleInterval == 0) {
				n.noiseSampleInterval = 5;
				n.sampleNoise = true;
			}
			if (n.delayMeasurementParams == null) {
				n.delayMeasurementParams = new DelayMeasurementParams();
			}
			if (n.clickLocParams == null) {
				n.clickLocParams = new ClickLocParams();
			}
			else {
				n.clickLocParams = clickLocParams.clone();
			}
			if (n.minTriggerChannels <= 0) {
				n.minTriggerChannels = 1;
			}
			return n;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the delay measurement params for a specific click type. 
	 * @param clickType click type
	 * @return delay measurement parameters. 
	 */
	public DelayMeasurementParams getDelayMeasurementParams(int clickType, boolean forceDefault) {
		DelayMeasurementParams dmp = null;
		if (delayMeasurementTypeParams != null && clickType > 0) {
			dmp = delayMeasurementTypeParams.get(clickType);
		}
		if (dmp == null) {
			return getDefaultDelayMesurementParams();
		}
		else {
			return dmp;
		}
	}
	
	/**
	 * Set the delay measurement parameters for a specific click type. 
	 * @param clickType click type
	 * @param delayMeasurementParams measurement parameters
	 */
	public void setDelayMeasurementParams(int clickType, DelayMeasurementParams delayMeasurementParams) {
		if (clickType == 0) {
			this.delayMeasurementParams = delayMeasurementParams;
		}
		else {
			if (delayMeasurementTypeParams == null) {
				delayMeasurementTypeParams = new Hashtable<>();
			}
			if (delayMeasurementParams != null) {
				delayMeasurementTypeParams.put(clickType, delayMeasurementParams);
			}
			else {
				delayMeasurementTypeParams.remove(clickType);
			}
		}
	}

	/**
	 * @return The default delay measurement parameters. 
	 */
	private DelayMeasurementParams getDefaultDelayMesurementParams() {
		if (delayMeasurementParams == null) {
			delayMeasurementParams = new DelayMeasurementParams();
		}
		return delayMeasurementParams;
	}


	/**
	 * 
	 * @return the click localisation parameters. 
	 */
	public ClickLocParams getLocalisationParams() {
		if (clickLocParams == null) {
			clickLocParams = new ClickLocParams();
		}
		return clickLocParams; 
	}

	/**
	 * @return the overlayMarkDataSelectorParams
	 */
	public MarkDataSelectorParams getOverlayMarkDataSelectorParams() {
		if (overlayMarkDataSelectorParams == null) {
			overlayMarkDataSelectorParams = new MarkDataSelectorParams();
		}
		return overlayMarkDataSelectorParams;
	}

	/**
	 * @return the groupedSourceParameters
	 */
	public GroupedSourceParameters getGroupedSourceParameters() {
		if (groupedSourceParameters == null) {
			groupedSourceParameters = new GroupedSourceParameters(rawDataSource, channelBitmap, channelGroups, groupingType);
		}
		return groupedSourceParameters;
	}

	/**
	 * @param groupedSourceParameters the groupedSourceParameters to set
	 */
	public void setGroupedSourceParameters(GroupedSourceParameters groupedSourceParameters) {
		this.groupedSourceParameters = groupedSourceParameters;
	}

	/**
	 * @return the rawDataSource
	 */
	public String getRawDataSource() {
		return groupedSourceParameters.getDataSource();
	}

	/**
	 * @param rawDataSource the rawDataSource to set
	 */
	public void setRawDataSource(String rawDataSource) {
		groupedSourceParameters.setDataSource(rawDataSource);
	}
	
	/**
	 * @return the channelBitmap
	 */
	public int getChannelBitmap(int i) {
		return groupedSourceParameters.getGroupChannels(i); 
	}

	/**
	 * @return the channelBitmap
	 */
	public int getChannelBitmap() {
		return groupedSourceParameters.getChanOrSeqBitmap();
	}

	/**
	 * @param channelBitmap the channelBitmap to set
	 */
	public void setChannelBitmap(int channelBitmap) {
		groupedSourceParameters.setChanOrSeqBitmap(channelBitmap);
	}

	/**
	 * @return the channelGroups
	 */
	public int[] getChannelGroups() {
		return groupedSourceParameters.getChannelGroups();
	}

	/**
	 * @param channelGroups the channelGroups to set
	 */
	public void setChannelGroups(int[] channelGroups) {
		groupedSourceParameters.setChannelGroups(channelGroups);
	}

	/**
	 * @return the groupingType
	 */
	public int getGroupingType() {
		return groupedSourceParameters.getGroupingType();
	}

	/**
	 * @param groupingType the groupingType to set
	 */
	public void setGroupingType(int groupingType) {
		groupedSourceParameters.setGroupingType(groupingType);
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			PamParameterData param = ps.findParameterData("dbThreshold");
			param.setShortName("Detection Threshold");
			param.setToolTip("Detector detection threshold in dB");
		}
		catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("delayMeasurementParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return delayMeasurementParams;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("delayMeasurementTypeParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return delayMeasurementTypeParams;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	




}
