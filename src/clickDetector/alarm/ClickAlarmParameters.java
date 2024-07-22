package clickDetector.alarm;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamguardMVC.dataSelector.DataSelectParams;


public class ClickAlarmParameters extends DataSelectParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	private boolean[] useSpeciesList;
	private double[] speciesWeightings;
	public boolean useEchoes = true;
	public boolean scoreByAmplitude; // alarm options, probably not used any more.  
	public double minimumAmplitude;
	public boolean onlineAutoEvents = true, onlineManualEvents = true;
	public int minICIMillis;
	private boolean clicksOREvents = false;
	/*
	 * Which events to use ...
	 */
	public boolean unassignedEvents = true; // not in an event
//	private boolean[] eventTypeList; // event types for specific spp. 
	private java.util.Hashtable<String, Boolean> eventTypes = new Hashtable<>();

	/**
	 * @return the showSpeciesList
	 */
	public boolean getUseSpecies(int speciesIndex) {
		if (useSpeciesList != null && useSpeciesList.length > speciesIndex) {
			return useSpeciesList[speciesIndex];
		}
		makeUseSpeciesList(speciesIndex);
		return true;
	}
	
	/**
	 * @param useSpeciesList the showSpeciesList to set
	 */
	public void setSpeciesWeighting(int speciesIndex, double speciesWeight) {
		makeUseSpeciesList(speciesIndex);
		speciesWeightings[speciesIndex] = speciesWeight;
	}
	public double getSpeciesWeight(int speciesIndex) {
		if (speciesWeightings != null && speciesWeightings.length > speciesIndex) {
			return speciesWeightings[speciesIndex];
		}
		makeUseSpeciesList(speciesIndex);
		return 1;
	}
	
	/**
	 * @param useSpeciesList the showSpeciesList to set
	 */
	public void setUseSpecies(int speciesIndex, boolean showSpecies) {
		makeUseSpeciesList(speciesIndex);
		useSpeciesList[speciesIndex] = showSpecies;
	}

	private void makeUseSpeciesList(int maxIndex) {
		if (useSpeciesList == null) {
			useSpeciesList = new boolean[0];
		}
		int oldLength = useSpeciesList.length;
		if (oldLength <= maxIndex) {
			useSpeciesList = Arrays.copyOf(useSpeciesList, maxIndex + 1);
			for (int i = oldLength; i <= maxIndex; i++) {
				useSpeciesList[i] = true;
			}
		}

		if (speciesWeightings == null) {
			speciesWeightings = new double[0];
		}
		oldLength = speciesWeightings.length;
		if (oldLength <= maxIndex) {
			speciesWeightings = Arrays.copyOf(speciesWeightings, maxIndex + 1);
			for (int i = oldLength; i <= maxIndex; i++) {
				speciesWeightings[i] = 1;
			}
		}
	}
	
	public void setUseEventType(String eventCode, boolean useType) {
//		if (eventTypeList == null) {
//			eventTypeList = new boolean[eventTypeIndex+1];
//		}
//		else if (eventTypeList.length < eventTypeIndex+1) {
//			eventTypeList = Arrays.copyOf(eventTypeList, eventTypeIndex+1);
//		}
//		eventTypeList[eventTypeIndex] = useType;
		if (eventCode == null) {
			return;
		}
		eventTypes.put(eventCode,  useType);
	}
	
	public boolean isUseEventType(String eventCode) {
//		if (eventTypeList == null || eventTypeList.length <= eventTypeIndex) {
//			return false;
//		}
//		return eventTypeList[eventTypeIndex];
		if (eventCode == null) return unassignedEvents;
		if (eventTypes == null) return false;
		Boolean use = eventTypes.get(eventCode);
		if (use == null) return false;
		return use;
	}

	@Override
	public ClickAlarmParameters clone() {
		try {
			ClickAlarmParameters newParams = (ClickAlarmParameters) super.clone();
			if (newParams.eventTypes == null) {
				newParams.eventTypes = new Hashtable<>();
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("eventTypes");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return eventTypes;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("speciesWeightings");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return speciesWeightings;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("useSpeciesList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return useSpeciesList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	/**
	 * @return the clicksOREvents
	 */
	public boolean isClicksOREvents() {
		return clicksOREvents;
	}

	/**
	 * @param clicksOREvents the clicksOREvents to set
	 */
	public void setClicksOREvents(boolean clicksOREvents) {
		this.clicksOREvents = clicksOREvents;
	}
	/**
	 * @return the clicksANDEvents
	 */
	public boolean isClicksANDEvents() {
		return !clicksOREvents;
	}


	public void setClicksANDEvents(boolean clicksANDEvents) {
		this.clicksOREvents = !clicksANDEvents;
	}


}
