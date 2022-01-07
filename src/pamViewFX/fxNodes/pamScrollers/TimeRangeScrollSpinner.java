package pamViewFX.fxNodes.pamScrollers;

import java.util.ArrayList;

import PamguardMVC.debug.Debug;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.PamScrollObserver;

/**
 * A time range spinner which allows integration with a PAMGUARD time scroll bar. 
 * @author Jamie Macaulay
 *
 */
public class TimeRangeScrollSpinner extends TimeRangeSpinner implements PamScrollObserver {
	
	/**
	 * List of listeners for this range spinner. 
	 */
	private ArrayList<VisibleRangeObserver> rangeSpinnerListeners;
		
	public TimeRangeScrollSpinner(){
		rangeSpinnerListeners = new ArrayList<VisibleRangeObserver>();
		//add listener
		this.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> {
			rangeChanged(oldValue, newValue);
		}); 
	}
	
	private void rangeChanged(long oldValue, long newValue){
		//Debug.out.println("TimeRangeScrollSpinner value changed from: "+ oldValue+ " to "+newValue +" no. listeners "+rangeSpinnerListeners.size()+ " "); 
		for (int i=0; i<rangeSpinnerListeners.size(); i++){
			rangeSpinnerListeners.get(i).visibleRangeChanged(oldValue, newValue);
		}
	}
	
	/**
	 * Add a range spinner list
	 * @param rangeSpinnerListener
	 */
	public void addRangeSpinnerListener(VisibleRangeObserver rangeSpinnerListener) {
		rangeSpinnerListeners.add(rangeSpinnerListener);
	}
	/**
	 * Remove a range spinner listener. 
	 * @param rangeSpinnerListener
	 */
	public void removeRangeSpinnerListener(VisibleRangeObserver rangeSpinnerListener) {
		rangeSpinnerListeners.remove(rangeSpinnerListener);
	}


	@Override
	public void scrollRangeChanged(AbstractPamScroller pamScroller) {
		//if the scroll range is changed then set the spinner value. 
		Debug.out.println("TimeRangeScrollerScrollSpinner: scrollRangeChanged: " +  pamScroller); 
		this.getValueFactory().setValue(pamScroller.getRangeMillis());
	}
	

	@Override
	public void scrollValueChanged(AbstractPamScroller abstractPamScroller) {
		//TODO Auto-generated method stub
	}


}
