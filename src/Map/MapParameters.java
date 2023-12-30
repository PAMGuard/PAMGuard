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
package Map;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;

import Array.Hydrophone;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class MapParameters implements Serializable, Cloneable, ManagedParameters {

	static final long serialVersionUID = 1;

	/**
	 * How long to display the ships track for
	 */
	int trackShowTime = 3600; 

	/**
	 * How long to keep GPS and other data for, even if it's not being used.
	 */
	int dataKeepTime = 3600;  

	/**
	 * How long a period to show detector data for.
	 * This is generally just used as a default now, with most datablocks 
	 * having their own time set that they show foe. 
	 */
	int dataShowTime = 600;  
	
	/**
	 * In the viewer, show all loaded detections, rather than just the ons in the 
	 * showtimes up to the curent cursor position time. 
	 */
	boolean viewerShowAll = false;
	
	/**
	 * A list of datablocks which are suitable for drawing on the map
	 */
//	ArrayList<String> plotableBlockIdList;
	/**
	 * A list of datablocks which are selected for drawing on the map
	 */
//	ArrayList<String> plottedBlockIds;
	/**
	 * A flag to enable/disable drawing of the hydrophone array on the map
	 */
	boolean showHydrophones = true;
	
	/**
	 * hide ship. Have to use the negative in order to not mess old configurations. 
	 */
	boolean hideShip = false;
	
	boolean keepShipOnMap = true;
	
	boolean keepShipCentred = false;
	
	boolean headingUp = false;
	
	File mapFile;
	
	boolean[] mapContours;
	
	boolean showKey = true;
	
	boolean showPanZoom = true;
	
	boolean showGpsData = true;

	public boolean colourHydrophonesByChannel;
	
	public int symbolSize = Hydrophone.DefaultSymbolSize;
	
	private static final int defaultMapRange = 10000;
	/**
	 * Value to store persistently between runs. 
	 */
	public int mapRangeMetres = defaultMapRange;
	
	public static final int RANGE_RINGS_NONE = 0;
	public static final int RANGE_RINGS_M = 1;
	public static final int RANGE_RINGS_KM = 2;
	public static final int RANGE_RINGS_NMI = 3;
	public int showRangeRings = RANGE_RINGS_NONE;
	public double rangeRingDistance = 1000;
	
	/*
	 * Use inverse logic so that current configurations don't change. 
	 */
	public boolean hideGrid; 
	
	/**
	 * Allow 3D rotation using the shift key and mouse movement. 
	 */
	public boolean allow3D;

	/**
	 * hide the sea surface
	 */
	public boolean hideSurface;

	@Override
	protected MapParameters clone() {
		try {
			MapParameters newParams = (MapParameters) super.clone();
			if (newParams.symbolSize == 0) {
				newParams.symbolSize = Hydrophone.DefaultSymbolSize;
			}
			if (newParams.mapRangeMetres == 0) {
				newParams.mapRangeMetres = defaultMapRange;
			}
			return newParams;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public int getTrackShowTime() {
		return trackShowTime;
	}

	public void setTrackShowTime(int trackShowTime) {
		this.trackShowTime = trackShowTime;
	}

	public int getDataKeepTime() {
		return dataKeepTime;
	}

	public void setDataKeepTime(int dataKeepTime) {
		this.dataKeepTime = dataKeepTime;
	}

	public int getDataShowTime() {
		return dataShowTime;
	}

	public void setDataShowTime(int dataShowTime) {
		this.dataShowTime = dataShowTime;
	}

	public boolean isViewerShowAll() {
		return viewerShowAll;
	}

	public void setViewerShowAll(boolean viewerShowAll) {
		this.viewerShowAll = viewerShowAll;
	}

	public boolean isShowHydrophones() {
		return showHydrophones;
	}

	public void setShowHydrophones(boolean showHydrophones) {
		this.showHydrophones = showHydrophones;
	}

	public boolean isKeepShipOnMap() {
		return keepShipOnMap;
	}

	public void setKeepShipOnMap(boolean keepShipOnMap) {
		this.keepShipOnMap = keepShipOnMap;
	}

	public boolean isKeepShipCentred() {
		return keepShipCentred;
	}

	public void setKeepShipCentred(boolean keepShipCentred) {
		this.keepShipCentred = keepShipCentred;
	}

	public boolean isHeadingUp() {
		return headingUp;
	}

	public void setHeadingUp(boolean headingUp) {
		this.headingUp = headingUp;
	}

	public File getMapFile() {
		return mapFile;
	}

	public void setMapFile(File mapFile) {
		this.mapFile = mapFile;
	}

	public boolean[] getMapContours() {
		return mapContours;
	}

	public void setMapContours(boolean[] mapContours) {
		this.mapContours = mapContours;
	}

	public boolean isShowKey() {
		return showKey;
	}

	public void setShowKey(boolean showKey) {
		this.showKey = showKey;
	}

	public boolean isShowPanZoom() {
		return showPanZoom;
	}

	public void setShowPanZoom(boolean showPanZoom) {
		this.showPanZoom = showPanZoom;
	}

	public boolean isShowGpsData() {
		return showGpsData;
	}

	public void setShowGpsData(boolean showGpsData) {
		this.showGpsData = showGpsData;
	}

	public boolean isColourHydrophonesByChannel() {
		return colourHydrophonesByChannel;
	}

	public void setColourHydrophonesByChannel(boolean colourHydrophonesByChannel) {
		this.colourHydrophonesByChannel = colourHydrophonesByChannel;
	}

	public int getSymbolSize() {
		return symbolSize;
	}

	public void setSymbolSize(int symbolSize) {
		this.symbolSize = symbolSize;
	}

	public int getMapRangeMetres() {
		return mapRangeMetres;
	}

	public void setMapRangeMetres(int mapRangeMetres) {
		this.mapRangeMetres = mapRangeMetres;
	}

	public int getShowRangeRings() {
		return showRangeRings;
	}

	public void setShowRangeRings(int showRangeRings) {
		this.showRangeRings = showRangeRings;
	}

	public double getRangeRingDistance() {
		return rangeRingDistance;
	}

	public void setRangeRingDistance(double rangeRingDistance) {
		this.rangeRingDistance = rangeRingDistance;
	}

	public boolean isHideGrid() {
		return hideGrid;
	}

	public void setHideGrid(boolean hideGrid) {
		this.hideGrid = hideGrid;
	}

	public boolean isAllow3D() {
		return allow3D;
	}

	public void setAllow3D(boolean allow3d) {
		allow3D = allow3d;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}


}
