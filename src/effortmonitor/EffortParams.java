package effortmonitor;

import java.io.Serializable;
import java.util.LinkedList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class EffortParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private LinkedList<String> recentObservers = new LinkedList<String>();
	
	private LinkedList<String> recentObjectives = new LinkedList<String>();
	
	public transient boolean isSet;
	
	/**
	 * Only log outer scroll events when paging forwards, not every little movement. 
	 */
	public boolean outserScrollOnly = false;
	
	private static final int MAX_OBSERVERS = 10;
	private static final int MAX_OBJECTIVES = 10;
	
	/**
	 * 
	 * @return The most recently selected observer
	 */
	public String getObserver() {
		if (isSet == false) {
			return null;
		}
		if (recentObservers.size() > 0) {
			return recentObservers.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @return The most recently selected observer
	 */
	public String getObjective() {
		if (isSet == false) {
			return null;
		}
		if (recentObjectives.size() > 0) {
			return recentObjectives.get(0);
		}
		return null;
	}
	
	public void setObserver(String observer) {
		recentObservers.remove(observer);
		recentObservers.add(0, observer);
		while (recentObservers.size() > MAX_OBSERVERS) {
			recentObservers.removeLast();
		}
		isSet = true;
	}

	public void setObjective(String objective) {
		recentObjectives.remove(objective);
		recentObjectives.add(0, objective);
		while (recentObjectives.size() > MAX_OBJECTIVES) {
			recentObjectives.removeLast();
		}
		isSet = true;
	}

	/**
	 * @return the isSet
	 */
	public boolean isSet() {
		return isSet;
	}

	/**
	 * @param isSet the isSet to set
	 */
	public void setSet(boolean isSet) {
		this.isSet = isSet;
	}

	/**
	 * @return the recentObservers
	 */
	public LinkedList<String> getRecentObservers() {
		return recentObservers;
	}

	/**
	 * @return the recentObjectives
	 */
	public LinkedList<String> getRecentObjectives() {
		return recentObjectives;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}


}
