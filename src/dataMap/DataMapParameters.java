package dataMap;

import java.io.Serializable;
import java.util.HashMap;

import PamView.ColourArray.ColourArrayType;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;


public class DataMapParameters implements Cloneable, Serializable, ManagedParameters {

	protected static final long serialVersionUID = 1L;
	
	public int vScaleChoice = OfflineDataMap.SCALE_PERHOUR;
	
	public boolean vLogScale = true;
	
	/*
	 * Scale factor for horizontal axis. 
	 */
	public int hScaleChoice = 4;
	
	public static final double[] hScaleChoices = {0.1, 0.5, 1, 2, 5, 10, 20, 60, 180, 600};
	
	/**
	 * Colour map to use for each 3D datagram, keyed on the data block's long data name.
	 * May be null if the settings were serialised by a version of PAMGuard from before
	 * datagram colours became selectable, so always go through 
	 * {@link #getDatagramColourMap(String)} and {@link #setDatagramColourMap(String, ColourArrayType)}.
	 */
	private HashMap<String, ColourArrayType> datagramColourMaps = new HashMap<>();

	/**
	 * Get the stored colour map for a datagram. 
	 * @param dataName long data name of the data block
	 * @return the stored colour map, or null if there isn't one. 
	 */
	public ColourArrayType getDatagramColourMap(String dataName) {
		if (datagramColourMaps == null) {
			return null;
		}
		return datagramColourMaps.get(dataName);
	}
	
	/**
	 * Store the colour map for a datagram. 
	 * @param dataName long data name of the data block
	 * @param colourArrayType colour map to remember
	 */
	public void setDatagramColourMap(String dataName, ColourArrayType colourArrayType) {
		if (datagramColourMaps == null) {
			datagramColourMaps = new HashMap<>();
		}
		datagramColourMaps.put(dataName, colourArrayType);
	}
	
	/**
	 * Which summary to show for raw sound files - see the MODE_ constants in
	 * dataMap.filemaps.SoundFileDatagramProvider.
	 */
	public int soundFileDatagramMode = 0;
	
	public double getPixeslPerHour() {
		return hScaleChoices[hScaleChoice];
	}

	/**
	 * Get the colour maps for 3D datagrams. Can be null after deserialising
	 * settings saved before this field existed, so create it if needed.
	 * @return map of colour map types keyed on data block long data name
	 */
	public HashMap<String, ColourArrayType> getDatagramColourMaps() {
		if (datagramColourMaps == null) {
			datagramColourMaps = new HashMap<>();
		}
		return datagramColourMaps;
	}

	@Override
	protected DataMapParameters clone() {
		try {
			DataMapParameters newParams = (DataMapParameters) super.clone();
			newParams.datagramColourMaps = new HashMap<>(getDatagramColourMaps());
			return newParams;
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
