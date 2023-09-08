package clickDetector.localisation;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class ClickLocParams implements Serializable, Cloneable, DetectionGroupOptions, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	private int internalVersion = 1;

	
	/**
	 * The selected localiser. 
	 */
	private boolean[] isSelected;
	
	/**
	 * The maximum range in meters
	 */
	public double maxRange=20000;
	
	
	/**
	 * The minimum depth (max height because that's the system we use in PAMGuard) in meters
	 */
	public double maxHeight=5;
	
	/**
	 * The maximum depth (min height because that's the system we use in PAMGuard) in meters
	 */
	public double minHeight=-5000;
	
	/**
	 * The maximum time inb millis before a warning is shown
	 */
	public long maxTime=200;
	
	/**
	 * Put a limit on the maximum number of points to localise
	 */
	public boolean limitLocPoints;
	
	/**
	 * Maximum number of points to localise
	 */
	public int maxLocPoints = 30;
	
	/**
	 * Click localiser params 
	 */
	public ClickLocParams(){		

	}
	
	public boolean getIsSelected(int iAlgo) {
		checkSelLength(iAlgo+1);
		return isSelected[iAlgo];
	}
	
	public void setIsSelected(int iAlgo, boolean selected) {
		checkSelLength(iAlgo+1);
		isSelected[iAlgo] = selected;
	}
	
	private void checkSelLength(int nAlgo) {
		if (isSelected == null) {
			isSelected = new boolean[nAlgo];
			isSelected[0] = true;
		}
		if (isSelected.length < nAlgo) {
			isSelected = Arrays.copyOf(isSelected, nAlgo);
		}		
	}

	@Override
	public ClickLocParams clone() {
		try {
			ClickLocParams newPar = (ClickLocParams) super.clone();
			if (newPar.internalVersion < 1) {
				newPar.internalVersion = 1;
				newPar.limitLocPoints = false;
				newPar.maxLocPoints = 30;
			}
			return newPar;
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	@Override
	public int getMaxLocalisationPoints() {
		if (limitLocPoints == false) {
			return Integer.MAX_VALUE;
		}
		else {
			return maxLocPoints;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("internalVersion");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return internalVersion;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("isSelected");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return isSelected;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
		}
		return ps;
	}



}
