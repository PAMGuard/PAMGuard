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
package Spectrogram;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import Array.ArrayManager;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.ColourArray.ColourArrayType;
import PamView.paneloverlay.OverlayDataInfo;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import userDisplay.UserFrameParameters;

/**
 * These are the parameter settings for a single
 *         spectrogram window, note tha a single spectrogram tab may have
 *         multiple windows
 * @author Doug Gillespie <p> 
 */
public class SpectrogramParameters extends UserFrameParameters implements Serializable, Cloneable, ManagedParameters {
	/*
	 * spectrograms get a lot of their parameters from the output of one of the
	 * FFT type data blocks. Like all data blocks, these acn be identified by
	 * name and having dataType == FFT; The persistent parameters should hold
	 * the name and the FFT length to make it reasonably easy to relocate an FFT
	 * block when PamGuard starts.
	 */
	static final long serialVersionUID = 5;

	public String sourceName;

	public String windowName;

	@Deprecated
	private int fftBlockIndex;
	
	public int nPanels = ArrayManager.getArrayManager().getCurrentArray().getHydrophoneCount();
	
	public boolean hideSidePanels = false;
	
	/**
	 * To make a whistle detector type display with different overlays on 
	 * each panel, channelList needs to be a list rather than a bit map so 
	 * that each panel can, if it wants, display the same data.
	 *  <p>Note that channelList may actually refer to sequence numbers and
	 *  not channels, depending on the FFT data source
	 */
	public int channelList[] = new int[PamConstants.MAX_CHANNELS];
	

	/**
	 * Frequency limits for display
	 */
	public double frequencyLimits[] = new double[2];

	/**
	 * Limits of amplitude range
	 */
	public double amplitudeLimits[] = { 50, 120 };
	
	public Integer horizontalSplitLocation;
	
	/**
	 * flag to say don't show raw data in viewer mode. 
	 */
	public boolean hideViewerSpectrogram;
	
	private Hashtable<String, OverlayDataInfo> overlayDataInfoChoices;
	
	/**
	 * Get a bitmap of plot numbers which use a particular channel.
	 * 0 means it's not used at all. 1, by panel 0, 3 by panels 0 and 1, etc. 
	 * @param iChannel
	 * @return
	 */
	public int channelUsers(int iChannel) {
		int users = 0;
		if (channelList == null) {
			return 0;
		}
		for (int i = 0; i < channelList.length; i++) {
			if (channelList[i] == iChannel) {
				users |= (1<<i);
			}
		}
		return users;
	}
	
	/**
	 * Type of colour scheme for spectrogram. 
	 */
	private ColourArrayType colourMap = ColourArrayType.GREY;

	public ColourArrayType getColourMap() {
		if (colourMap == null) {
			colourMap = ColourArrayType.GREY;
		}
		return colourMap;
	}

	public void setColourMap(ColourArrayType colourMap) {
		this.colourMap = colourMap;
	}


	/**
	 * Wraps display if this is true (defalt)
	 */
	public boolean wrapDisplay = true;

	/**
	 * Fixed time scale, if this is false, then it's based on the number of
	 * pixels and a set number of slices per pixel
	 */
	public boolean timeScaleFixed = false;

	/**
	 * Display length in seconds.
	 */
	public double displayLength = 20;

	/**
	 * Used if timeScaleFixed is false to set the scale
	 */
	public int pixelsPerSlics = 1;

	public boolean showScale = true;

	/**
	 * Also show the waveform display under the spectrogram
	 */
	public boolean showWaveform = false;
	
	public boolean autoScaleWaveform = false;

	private boolean[][] showDetector; // separate arrays for each panel
	
	public boolean[] showPluginDisplay;
	
	public boolean[] useSpectrogramMarkObserver;

	public SpectrogramParameters() {
		boundingRectangle.x = (int) (Math.random() * 300);
		boundingRectangle.y = (int) (Math.random() * 200);
		boundingRectangle.width = 500;
		boundingRectangle.height = 300;
		channelList = new int[PamConstants.MAX_CHANNELS];
	}
	
	

//	public boolean getShowDetector(int panelId, int itemId) {
//		checkShowDetectorSize(panelId+1, itemId+1);
//		if (itemId < 0) return false;
//		return showDetector[panelId][itemId];
//	}
//
//
//
//	public void setShowDetector(int panelId, int itemId, boolean showDetector) {
//		checkShowDetectorSize(panelId+1, itemId+1);
//		this.showDetector[panelId][itemId] = showDetector;
//	}

	private void checkShowDetectorSize(int nPanels, int nItems) {
		if (showDetector == null || 
				showDetector.length < nPanels || 
				showDetector[nPanels-1].length < nItems) {
			boolean[][] oldShow = showDetector;
			showDetector = new boolean[nPanels][nItems];
			for (int i = 0; i < nPanels; i++) {
				showDetector[i] = new boolean[nItems];
			}
			if (oldShow != null) {
				for (int iP = 0; iP < Math.min(nPanels, oldShow.length); iP++) {
					for (int iI = 0; iI < Math.min(nItems, oldShow[0].length); iI++) {
						showDetector[iP][iI] = oldShow[iP][iI];
					}
				}
			}
		}
	}


	@Override
	public SpectrogramParameters clone() {
		try{
		  return (SpectrogramParameters) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Get overlay data information for a given panel and datablock. 
	 * @param dataBlock Data block
	 * @param panelId panel index / id
	 * @return Overlay Data information. 
	 */
	public OverlayDataInfo getOverlayDataInfo(PamDataBlock dataBlock, int panelId) {
		String name = dataBlock.getLongDataName()+panelId;
		if (overlayDataInfoChoices == null) {
			overlayDataInfoChoices = new Hashtable<>();
		}
		OverlayDataInfo odi = overlayDataInfoChoices.get(name);
		if (odi == null) {
			odi = new OverlayDataInfo(name);
			overlayDataInfoChoices.put(name, odi);
		}
		return odi;
	}

	/**
	 * Shouldn't be used, just there for legacy configurations. 
	 * Data block should be identified only by name. 
	 * @return the fftBlockIndex
	 */
	public int getFftBlockIndex() {
		return fftBlockIndex;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("overlayDataInfoChoices");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return overlayDataInfoChoices;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("showDetector");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return showDetector;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
