package angleVetoes;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * PArameters controlling angle vetoes for a particular detector.
 *  
 * @author Douglas Gillespie
 *
 */
public class AngleVetoParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 0;
	
	private ArrayList<AngleVeto> vetoList;
	
	/**
	 * Get a count of the number of angle vetoes in the list
	 * @return angle veto count. 
	 */
	public int getVetoCount() {
		if (vetoList == null) {
			return 0;
		}
		else {
			return vetoList.size();
		}
	}
	
	/**
	 * Get an angle veto from the list
	 * @param index index of veto to retreive. 
	 * @return Angle veto reference
	 */
	public AngleVeto getVeto(int index) {
		return vetoList.get(index);
	}
	
	/**
	 * Add a veto to the list.
	 * @param angleVeto new angle veto
	 */
	public void addVeto(AngleVeto angleVeto) {
		if (vetoList == null) {
			vetoList = new ArrayList<AngleVeto>();
		}
		vetoList.add(angleVeto);
	}
	
	/**
	 * Remove a veto from the list
	 * @param index index of veto to remove
	 */
	public void removeVeto(int index) {
		if (vetoList == null) {
			return;
		}
		vetoList.remove(index);		
	}
	
	/**
	 * Remove a veto from the list.
	 * @param angleVeto reference to veto to remove
	 * @return true ifveto was found and removed from list
	 */
	public boolean removeVeto(AngleVeto angleVeto) {
		if (vetoList == null) {
			return false;
		}
		return vetoList.remove(angleVeto);		
	}
	
	/**
	 * Replace a veto in the list at given index.
	 * @param index index of veto to replace
	 * @param newOne new veto
	 */
	public void replaceVeto(int index, AngleVeto newOne) {
		vetoList.remove(index);
		vetoList.add(index, newOne);
	}
	
	@Override
	public AngleVetoParameters clone() {

		try {
			return (AngleVetoParameters) super.clone();
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
			Field field = this.getClass().getDeclaredField("vetoList");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return vetoList;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
