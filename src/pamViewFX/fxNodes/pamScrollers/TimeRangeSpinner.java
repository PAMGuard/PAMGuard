package pamViewFX.fxNodes.pamScrollers;


import pamViewFX.fxNodes.PamListSpinnerFactory;
import pamViewFX.fxNodes.PamSpinner;
import PamUtils.PamCalendar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Spinner which cycles through various time ranges. Used a lot in time scroll bars. 
 * @author Jamie Macaulay
 *
 */
public class TimeRangeSpinner extends PamSpinner<Long> {
	
	/**
	 * One hour in milliseconds.
	 */
	private static long hour=3600000; 
	
	/**
	 * Value factory for a set lift of values
	 */
	private PamListSpinnerFactory<Long> listSpinnerFactory;
	
	/**
	 * Default time ranges in milliseconds. 
	 */
	private long defaultTimeRanges[] = {1, 2, 5, 50, 100, 500, 1000, 5000, 10000, 20000, 30000, 60000, 120000,
			300000, 600000, 900000, 1200000, 1800000, 2700000, hour, 2*hour, 6*hour, 12*hour, 24*hour, 36*hour, 48*hour, 24*7*hour, 24*30*hour};

	public TimeRangeSpinner() {
		this.setEditable(true);
		this.listSpinnerFactory= new PamListSpinnerFactory<Long>(createStepTimesList()); 
		listSpinnerFactory.setConverter(new TimeConverter());
		this.setValueFactory(listSpinnerFactory);
	}

	/**
	 * Create an observable list of times.
	 * @return list of time ranges.
	 */
	private ObservableList<Long> createStepTimesList(){
		ObservableList<Long> stepSizeList=FXCollections.observableArrayList();
		for (int i=0; i<defaultTimeRanges.length; i++){
			stepSizeList.add(defaultTimeRanges[i]);
		}
		return stepSizeList;
	}
	
	/**
	 * Time converter to allow users to enter numbers into the spinner. 
	 * @author Jamie Macaulay
	 */
	public class TimeConverter extends StringConverter<Long> {
		
		Long lastTime=defaultTimeRanges[0]; 
		
	    @Override
	    public String toString(Long time) {
	    	if (time==null) return timeToString(lastTime);
	    	lastTime=time; 
	        return timeToString(time);
	    }

		@Override
		public Long fromString(String value) {
			Long duration=PamCalendar.readTimeDuration(value); 
			//if null then there is an issue. User probably entered a bad value. 
			if (duration==null) {
				//if value is null then reset values. 
				listSpinnerFactory.setValue(lastTime);
			}
			return duration;
		}
	}
	
	/**
	 * Convert time in milliseconds to a string representation of time
	 * @return string representation of time. 
	 */
	private String timeToString(long timeMillis){
		return (" " + PamCalendar.formatDuration(timeMillis));
	}
	
	/**
	 * Get the current time displayed in the spinner. 
	 * @return the current time in the spinner. 
	 */
	public long getTime(){
		Long duration=listSpinnerFactory.getValue();
		return duration;
	}
	
	/**
	 * Set the time of the spinner.
	 * @param millis - the time to set in milliseconds. 
	 */
	public void setTime(long millis){
		listSpinnerFactory.setValue(millis);
	}
			
}

