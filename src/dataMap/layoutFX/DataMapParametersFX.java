package dataMap.layoutFX;

import java.io.Serializable;
import java.util.HashMap;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataMap.OfflineDataMap;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;

public class DataMapParametersFX implements Cloneable, Serializable, ManagedParameters {

	protected static final long serialVersionUID = 1L;

	/**
	 * The data maps which are collapsed. 
	 */
	public HashMap<DataMapInfo, Boolean> dataMapsCollapsed = new HashMap<DataMapInfo, Boolean>();

	
	/**
	 * The data maps which are shown. 
	 */
	public HashMap<DataMapInfo, Boolean> dataMapsShown = new HashMap<DataMapInfo, Boolean>();

	
	/**
	 * A has map of colours for each data gram. 
	 */
	public HashMap<DataMapInfo, PlotParams2D> datagramColours = new HashMap<DataMapInfo, PlotParams2D>();
	
	/**
	 * The log scale. 
	 */
	public int vScaleChoice = OfflineDataMap.SCALE_PERHOUR;
	
	/**
	 * Use a log scale. 
	 */
	public boolean vLogScale = true;
	
	/**
	 * The selected dfatagranm for colour changes. 
	 */
	public int selectedColourDatagram = 0; 
	

	@Override
	protected DataMapParametersFX clone() {
		try {
			return (DataMapParametersFX) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
