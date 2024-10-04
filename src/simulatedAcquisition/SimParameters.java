package simulatedAcquisition;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import Acquisition.DaqSystemXMLManager;
import Acquisition.SoundCardParameters;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Parameters for all simulated objects
 * @author Doug Gillespie
 *
 */
public class SimParameters extends SoundCardParameters implements Cloneable, Serializable, ManagedParameters {


	public static final long serialVersionUID = 1L;
	
//	public float sampleRate = 48000;
	
	public double backgroundNoise = 50;
	
	private ArrayList<SimObject> simObjects;
	
	public String propagationModel;
	
	
	/**
	 * @param systemType
	 */
	public SimParameters(String systemType) {
		super(systemType);
	}

	public int getNumObjects() {
		if (simObjects == null) {
			return 0;
		}
		return simObjects.size();
	}
	
	public SimObject getObject(int i) {
		return simObjects.get(i);
	}
	
	public void addSimObject(SimObject simObject) {
		if (simObjects == null) {
			simObjects = new ArrayList<SimObject>();
		}
		simObjects.add(simObject);
	}
	
	public boolean removeObject(SimObject simObject) {
		return simObjects.remove(simObject);
	}
	
	public SimObject removeObject(int simObject) {
		return simObjects.remove(simObject);
	}
	public void replaceSimObject(SimObject oldObject, SimObject newObject) {
		int pos = simObjects.indexOf(oldObject);
		simObjects.remove(pos);
		pos = Math.max(0, pos);
		simObjects.add(pos, newObject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SimParameters clone()  {
			SimParameters newParams = (SimParameters) super.clone();
			if (simObjects != null) {
				newParams.simObjects = (ArrayList<SimObject>) simObjects.clone();
			}
			return newParams;
		
	}

	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected a simulated source, just return null
		if (!DaqSystemXMLManager.isSelected(SimProcess.sysType)) {
			return null;
		}

		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("simObjects");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return simObjects;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
