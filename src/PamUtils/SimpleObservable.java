package PamUtils;

import java.util.ArrayList;

/**
 * Simple observerbale class which can pass on a single object to observables. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public class SimpleObservable<T extends Object> {
	
	private ArrayList<SimpleObserver<T>> observers = new ArrayList<>();
	
	/**
	 * Add an observer
	 * @param observer
	 */
	public void addObserver(SimpleObserver<T> observer) {
		removeObserver(observer);
		observers.add(observer);
	}

	/**
	 * Remove an observer
	 * @param observer
	 * @return true if observer existed. 
	 */
	public boolean removeObserver(SimpleObserver<T> observer) {
		return observers.remove(observer);
	}
	
	/**
	 * Update observers. 
	 * @param data data to pass to observers. 
	 */
	public void update(T data) {
		for (SimpleObserver<T> obs:observers) {
			obs.update(data);
		}
	}
	
	public void updateSettings() {
		for (SimpleObserver<T> obs:observers) {
			obs.updateSettings();
		}
	}
}
