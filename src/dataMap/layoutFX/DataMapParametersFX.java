package dataMap.layoutFX;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import dataMap.OfflineDataMap;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;

public class DataMapParametersFX implements Cloneable, Serializable, ManagedParameters {

	protected static final long serialVersionUID = 2L;

	/**
	 * The data maps which are collapsed. 
	 */
	public HashMap<DataMapInfo, Boolean> dataMapsCollapsed = new HashMap<DataMapInfo, Boolean>();

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
	
	/**
	 * Called before saving serialised settings
	 */
	public void saveSerialised() {
		//a bit messy but have to make sure we save the plat params 
		Iterator<DataMapInfo> keys =  this.datagramColours.keySet().iterator();
		DataMapInfo aKey;
		while (keys.hasNext()) {
			aKey = keys.next();
			PlotParams2D plotparams = datagramColours.get(aKey);
			plotparams.getAmplitudeLimitsSerial()[0] = plotparams.getAmplitudeLimits()[0].get();
			plotparams.getAmplitudeLimitsSerial()[1] = plotparams.getAmplitudeLimits()[1].get();
//			System.out.println("ArrayColours " + aKey.getName() + ":" + plotparams.getAmplitudeLimits()[0].get() + "  " +  plotparams.getAmplitudeLimits()[1].get());
		}
	}
	
	public void loadSerialised() {
		//a bit messy but have to make sure we save the plat params 
		Iterator<DataMapInfo> keys =  this.datagramColours.keySet().iterator();
		DataMapInfo aKey;
		while (keys.hasNext()) {
			aKey = keys.next();
			PlotParams2D plotparams = datagramColours.get(aKey);
			plotparams.getAmplitudeLimits()[0].set(plotparams.getAmplitudeLimitsSerial()[0]);
			plotparams.getAmplitudeLimits()[1].set(plotparams.getAmplitudeLimitsSerial()[1]);

			System.out.println("ArrayColours " + aKey.getName() + ":" + plotparams.getAmplitudeLimits()[0].get() + "  " +  plotparams.getAmplitudeLimits()[1].get());
		}
	}

}
