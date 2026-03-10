package noiseMonitor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ListIterator;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import noiseBandMonitor.BandType;

public class NoiseSettings implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	
	public String dataSource;
	
	/**
	 * May be a channelMap or a sequenceMap, depending on source selected
	 */
	public int channelBitmap = 1;
	
	/**
	 * Interval between measurements in seconds
	 */
	public int measurementIntervalSeconds = 60;
	
	/**
	 * Number of measures in measurementIntervalSeconds
	 * These will be placed at random within the interval
	 */
	public int nMeasures = 100;
	
	/**
	 * Tells PAMGUARD to use every single FFT data coming in. 
	 * <p> Overrides nMeasures.
	 */
	public boolean useAll = true;
	
	private ArrayList<NoiseMeasurementBand> measurementBands;
	
	private double lowestFrequency, highestFrequency;

	public NoiseSettings() {
		super();
		measurementBands = new ArrayList<NoiseMeasurementBand>();
	}

	public ListIterator<NoiseMeasurementBand> getBandIterator() {
		if (measurementBands == null) {
			return null;
		}
		return measurementBands.listIterator();
	}
	
	public int getNumMeasurementBands() {
	  if (measurementBands == null) {
		  return 0;
	  }
	  return measurementBands.size();
	}
	
	public NoiseMeasurementBand getMeasurementBand(int i) {
		return measurementBands.get(i);
	}
	
	/**
	 * Add a noise measurement band to the end of the list. 
	 * @param noiseMeasurementBand noise band data
	 */
	public void addNoiseMeasurementBand(NoiseMeasurementBand noiseMeasurementBand) {
		measurementBands.add(noiseMeasurementBand);
		sortLowHigh();
	}
	
	/**
	 * Add a noise measurement band at a specific place in the list. 
	 * @param listPos position in list
	 * @param noiseMeasurementBand noise band data
	 */
	public void addNoiseMeasurementBand(int listPos, NoiseMeasurementBand noiseMeasurementBand) {
		measurementBands.add(listPos, noiseMeasurementBand);
		sortLowHigh();
	}
	
	/**
	 * Remove a noise measurement band from the list
	 * @param iBand band index
	 */
	public void removeMeasurementBand(int iBand) {
		if (iBand < 0 || iBand >= measurementBands.size()) {
			return;
		}
		measurementBands.remove(iBand);
		sortLowHigh();
	}
	
	private void sortLowHigh() {
		lowestFrequency = 1;
		highestFrequency = 10;
		NoiseMeasurementBand aBand;
		for (int i = 0; i < measurementBands.size(); i++) {
			aBand = measurementBands.get(i);
			if (i == 0) {
				lowestFrequency = aBand.f1;
				highestFrequency = aBand.f2;
			}
			else {
				lowestFrequency = Math.min(lowestFrequency, aBand.f1);
				highestFrequency = Math.max(highestFrequency, aBand.f2);
			}
		}
	}

	/**
	 * Get the lowest frequency used by any band. 
	 */
	public double getLowestFrequency() {
		return lowestFrequency;
	}
	
	/**
	 * Get the highest frequency used by any band
	 * @return
	 */
	public double getHighestFrequency() {
		return highestFrequency;
	}
	/**
	 * Get the lowest frequency used by any band. 
	 * rounded down to nearest factor of 10
	 */
	public double getLowestFrequency10() {
		return Math.pow(10., Math.floor(Math.log10(Math.max(1,lowestFrequency))));
	}
	
	/**
	 * Get the highest frequency used by any band
	 * rounded up to nearest factor of 10
	 * @return
	 */
	public double getHighestFrequency10() {
		return Math.pow(10., Math.ceil(Math.log10(Math.max(1,highestFrequency))));
	}

	@Override
	public NoiseSettings clone() {
		try {
			/*
			 * Do full hard clone of array list of measurements too !
			 */
			NoiseSettings ns = (NoiseSettings) super.clone();
			ns.measurementBands = (ArrayList<NoiseMeasurementBand>) this.measurementBands.clone();
			return ns;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("measurementBands");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return measurementBands;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
