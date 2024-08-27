package spectrogramNoiseReduction;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class SpectrogramNoiseSettings implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;

	public ArrayList<Serializable> methodSettings = new ArrayList<Serializable>();
	
	public String dataSource;
	
	public int channelList = 0xF;

	private boolean[] runMethod; 

	public void clearSettings() {
		methodSettings.clear();
	}

	public void addSettings(Serializable set) {
		methodSettings.add(set);
	}

	public Serializable getSettings(int iSet) {
		if (methodSettings.size() > iSet) {
			return methodSettings.get(iSet);
		}
		return null;
	}
	
	/**
	 * Added so that the run methods could be properly logged in the XML output
	 * 
	 * @return
	 */
	public boolean[] isRunMethod() {
		return runMethod;
	}

	public boolean isRunMethod(int iMethod) {
		if (runMethod == null || runMethod.length <= iMethod) {
			return false;
		}
		return runMethod[iMethod];
	}

	public void setRunMethod(int iMethod, boolean run) {
		boolean[] newMethodList = runMethod;
		if (newMethodList == null || runMethod.length <= iMethod) {
			newMethodList = new boolean[iMethod+1];
			if (runMethod != null) {
				for (int i = 0; i < runMethod.length; i++) {
					newMethodList[i] = runMethod[i];
				}
			}
		}
		newMethodList[iMethod] = run;
		runMethod = newMethodList;
	}

	@Override
	public SpectrogramNoiseSettings clone() {
		// TODO Auto-generated method stub
		try {
			return (SpectrogramNoiseSettings) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("runMethod");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return runMethod;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
