package pamViewFX.fxNodes.picker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import PamUtils.PamCalendar;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * Pane which allows users to select a data and time using various spinner. 
 * @author spn1
 *
 */
public class DateTimeSpinnerPane extends PamHBox {
	
	/**
	 * Create all the spinners. Isn't the way we represent time really weird?
	 */
	Spinner<Integer> yearSpinner; // 1970-3000;
	Spinner<Integer> monthSpinner; //1-12
	Spinner<Integer> daySpinner;  //depends on month 30 or 31
	Spinner<Integer> hourSpinner;  //1-24
	Spinner<Integer> minuteSpinner; //1-60
	Spinner<Integer> secondSpinner; //1-60
	Spinner<Integer> millisSpinner; //1-999
	
	/**
	 * The width of spinners; 
	 */
	private double spinnerWidth=20; 
	
	/**
	 * The height of spinners; 
	 */
	private double spinnerHeight=60; 

	
	public DateTimeSpinnerPane(){
		
		//create all the spinners 
		yearSpinner=new DateTimeSpinner(1970,3000,1);
		yearSpinner.setPrefSize(45, spinnerHeight); //year need to be a bit wider. 
		yearSpinner.valueProperty().addListener((observable, oldVal, newVal)->{
			((IntegerSpinnerValueFactory) daySpinner.getValueFactory()).setMax(getNumberDays(yearSpinner.getValue(), monthSpinner.getValue()));
		});
		monthSpinner=new DateTimeSpinner(1,12,1);
		monthSpinner.valueProperty().addListener((observable, oldVal, newVal)->{
			((IntegerSpinnerValueFactory) daySpinner.getValueFactory()).setMax(getNumberDays(yearSpinner.getValue(), monthSpinner.getValue()));
		});
		daySpinner=new DateTimeSpinner(1,31,1); //special case as changes with month either 30 or 31. 
		hourSpinner=new DateTimeSpinner(1,24,1);
		minuteSpinner=new DateTimeSpinner(1,59,1);
		secondSpinner=new DateTimeSpinner(1,59,1);
		millisSpinner=new DateTimeSpinner(0,999,1);
		millisSpinner.setPrefSize(35, spinnerHeight); //year need to be a bit wider. 
		millisSpinner.getValueFactory().setConverter(new MillisDateConverter()); //need three leading zeros


		//add all to hBox
		this.setSpacing(2);
		//center nodes within hBox. 
		this.setAlignment(Pos.CENTER_LEFT); 
		this.getChildren().addAll(yearSpinner,new Label("-"), monthSpinner , new Label("-"), daySpinner, new Label("   "), hourSpinner,
				new Label(":"), minuteSpinner ,new Label(":"), secondSpinner,new Label(":"), millisSpinner); 
		
	}
	
	/**
	 * Get the number of days in a month. 
	 * @param year - the year
	 * @param month - the day
	 * @return the number of days in the month. 
	 */
	private int getNumberDays(int year, int month){
		 LocalDate date = LocalDate.of(year, month, 01);
		 return date.getMonth().length(true);
	}
	
	/**
	 * Quick class for custom spinners used to show date/time. 
	 * @author Jamie Macaulay
	 */
	class DateTimeSpinner extends PamSpinner<Integer>{
		
		DateTimeSpinner(int start, int end, int increment){
			super(start,end,increment);
			//set a different converter so dates show leading zeros
			this.getValueFactory().setConverter(new DateConverter());
			this.getValueFactory().setWrapAround(true);
			this.setPrefSize(spinnerWidth, spinnerHeight);
			this.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
			this.setEditable(true);
			//FIXME quick fix to make things display properly with leading zeros
			this.getValueFactory();
		}
	}
	
	/**
	 * Slightly different converter which adds leading zeros to two digit numbers to make it easier to read dates. 
	 * @author Jamie Macaulay
	 *
	 */
	class DateConverter extends StringConverter<Integer>{

		@Override
		public Integer fromString(String arg0) {
			return Integer.valueOf(arg0);
		}

		@Override
		public String toString(Integer arg0) {
			//if two digits or less then add leading zero. 
			if (arg0<100) return String.format("%02d", arg0);
			return arg0.toString();
		}
	}
	
	/**
	 * Slightly different converter which adds leading zeros to three digit numbers to make it easier to read dates. 
	 * @author Jamie Macaulay
	 *
	 */
	class MillisDateConverter extends StringConverter<Integer>{

		@Override
		public Integer fromString(String arg0) {
			return Integer.valueOf(arg0);
		}

		@Override
		public String toString(Integer arg0) {
			//if two digits or less then add leading zero. 
			return String.format("%03d", arg0);
		}
		
	}
	
	/**
	 * Get the set datetime in millis.
	 * @return date time in PAMGUARD millis datenum.  
	 */
	public long getDateTime(){
		return PamCalendar.msFromDate(yearSpinner.getValue(), monthSpinner.getValue(), daySpinner.getValue(), hourSpinner.getValue(), minuteSpinner.getValue(), secondSpinner.getValue(), millisSpinner.getValue());
	}
	
	/**
	 * Set the data and time for the spinner. 
	 * @param timeMillis
	 */
	public void setDateTime(long timeMillis){
		//TODO- this time API needs to replace PamCalander
		Instant instant = Instant.ofEpochMilli(timeMillis);
		LocalDateTime res = LocalDateTime.ofInstant(instant, ZoneOffset.UTC); //make sure UTC. 
		
		//set times. 
		yearSpinner.getValueFactory().setValue(res.getYear());
		monthSpinner.getValueFactory().setValue(res.getMonthValue());
		daySpinner.getValueFactory().setValue(res.getDayOfMonth());
		hourSpinner.getValueFactory().setValue(res.getHour());
		minuteSpinner.getValueFactory().setValue(res.getMinute());
		secondSpinner.getValueFactory().setValue(res.getSecond());
		millisSpinner.getValueFactory().setValue(res.getNano()/1000/1000);
		
	}
		
	
	
	
	
}
