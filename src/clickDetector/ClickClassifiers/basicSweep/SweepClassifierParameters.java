package clickDetector.ClickClassifiers.basicSweep;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Vector;

import PamModel.SMRUEnable;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class SweepClassifierParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private Vector<SweepClassifierSet> classifierSets = new Vector<SweepClassifierSet>();
	
	/**
	 * Test a click with all classifiers and add an add an annotation to the click 
	 * with info on all classifiers it passed. 
	 */
	public boolean checkAllClassifiers = false;  
	
	
	@Override
	public SweepClassifierParameters clone() {
		try {
			SweepClassifierParameters newSet = (SweepClassifierParameters) super.clone();
			newSet.classifierSets = new Vector<SweepClassifierSet>();
			for (SweepClassifierSet aSet:this.classifierSets) {
				newSet.classifierSets.addElement(aSet.clone());
			}
			return newSet;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getNumSets() {
		return classifierSets.size();
	}
	
	public SweepClassifierSet getSet(int i) {
		return classifierSets.get(i);
	}
	
	public void addSet(SweepClassifierSet set) {
		classifierSets.add(set);
	}
	
	public void addSet(int ind, SweepClassifierSet set) {
		classifierSets.add(ind, set);
	}
	
	public int getSetRow(SweepClassifierSet set) {
		return classifierSets.indexOf(set);
	}
	
	public void remove(SweepClassifierSet set) {
		classifierSets.remove(set);
	}
	
	public void remove(int ind) {
		classifierSets.remove(ind);
	}
	
	public void removeAll() {
		classifierSets.clear();
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("classifierSets");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return classifierSets;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
