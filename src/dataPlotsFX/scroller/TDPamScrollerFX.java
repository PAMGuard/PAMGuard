package dataPlotsFX.scroller;

import dataPlotsFX.layout.TDDisplayFX;
import javafx.geometry.Orientation;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import pamScrollSystem.PamScrollerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.pamScrollers.VisibleRangeObserver;
import pamViewFX.fxNodes.pamScrollers.TimeRangeScrollSpinner;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.AcousticScrollerFX;

/**
 * Subclass for the td display. Provides a specific type of navigation dialog for this scroller. 
 * @author Jamie Macaulay
 *
 */
public class TDPamScrollerFX extends PamScrollerFX {
	
	/**
	 * The tdDisplay pane which holds this time scroller.
	 */
	private TDDisplayFX tdDisplay;
	
	/**
	 * Spinner for changing the window size. i.e. how many seconds the windows shows. 
	 */
	private TimeRangeScrollSpinner spinner;

	/**
	 * Holds the scroll bar and spinner. 
	 */
	private PamBorderPane scrollHolder;

	/**
	 * Construct a PAMGUARD scroll bar which contains 
	 * a main scroll bar bit and buttons for moving forward
	 * in large secScollbar name (used in scroll bar management)
	 * @param orientation AbstractPamScroller.VERTICAL or AbstractPamScroller.HORIZONTAL
	 * @param stepSizeMillis step size in milliseconds for scroller. 
	 * @param defaultLoadTime default amount of data to load.
	 * @param hasMenu true if menu options should be shown in navigation area. 
	 * @parma owner The pamscroller window owner. Used to keep dialogs inside the TDDisplay window. 
	 */
	public TDPamScrollerFX(String name, Orientation orientation,
			int stepSizeMillis, long defaultLoadTime, boolean hasMenu, TDDisplayFX tdDisplay) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		this.tdDisplay=tdDisplay;
		this.addObserver(spinner); //spinner changes if the visible scroll range changes. 
	}
	
	@Override 
	public Pane getNode(){
		return scrollHolder;
	}
	
	/**
	 * Open the time navigation dialog. Add some modifications to create dialog which sits inside the tdDisplay.  
	 */
//	@Override
//	public void openTimeNavigationDialog(){
//		//create a stage which is both undecorated and sits inside tdDisplay. 
//        PamScrollerData newData=NavigationDialog.showDialog(tdDisplay.getScene().getWindow(), true, StageStyle.UNDECORATED, this);
//        //TODO serious thread issues here...need to sort out. 
//        try {
//		if (newData != null) {
//			scrollerData = newData;
//			//Code to change Swing data.
//			rangesChangedF(getValueMillis());
//		}
//        }
//        catch (Exception e){
//        	e.printStackTrace();
//        }
//	}
	
	/**
	 * Create a spinner which allows users to change the number of seconds shown in the window. Note this is different from 'duration'
	 * which is changed in the navigation dialog. Duration refers to the number of seconds of loaded data, rather than the number of seconds 
	 * shown on the window. 
	 */
	private TimeRangeScrollSpinner createTimeSpinner(){
		TimeRangeScrollSpinner spinner=new TimeRangeScrollSpinner(); 
		//set size of text field. 
		return spinner;
	}
	
	@Override
	public void layoutScrollBarPane(Orientation orientation){
		//need to create spinner as CSS has some layout issues
		super.layoutScrollBarPane(orientation);
		
		if (scrollHolder==null) scrollHolder=new PamBorderPane(); 
		scrollHolder.setCenter(super.getNode());
		
		if (spinner==null ) spinner=createTimeSpinner();
		spinner.prefHeightProperty().unbind();

		if (orientation==Orientation.HORIZONTAL){
			scrollHolder.setRight(spinner);
			spinner.prefHeightProperty().bind(scrollHolder.heightProperty());
		}
		else{
			scrollHolder.setBottom(spinner);
		}
		setSpinnerOrientation(orientation); 
	}
	
	/**
	 * Set the orientation of the time range spinner 
	 * @param orientation - orientation to set the spinner to. 
	 */
	private void setSpinnerOrientation(Orientation orientation){
		if (orientation==Orientation.HORIZONTAL) {
			spinner.getStyleClass().remove(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
			spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			spinner.setPrefWidth(150);
		}
		else{
//			System.out.println("Vertical spinner"); 
			spinner.getStyleClass().remove(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL); 
			spinner.setPrefHeight(100);

		}
	}
	
	/**
	 * Set orientation for the scroller. 
	 * @param orientation- orientation of the time scroller. 
	 */
	public void setOrientation(Orientation orientation){
		setSpinnerOrientation(orientation); 
		super.setOrientation(orientation);
		//FIXME- fudge here that doesn't even work too well to get the scroll bar to resize properly. Issue somewhere with changing spinner to vertical type. Could
		//create another spinner but this brings whole bunch of other issues, such as adding listeners again etc. 
		if (orientation==Orientation.VERTICAL) getControlPane().setPrefWidth(75); 
	} 
	
	/**
	 * Get the spinner which changes time ranges. 
	 * @return time range spinner. 
	 */
	public TimeRangeScrollSpinner getTimeRangeSpinner() {
		return spinner;
	}

	/**
	 * Add an observer for visible range changes in the scroll bar. 
	 * @param timeRangeListener
	 */
	public void addRangeObserver(VisibleRangeObserver timeRangeListener) {
		spinner.addRangeSpinnerListener(timeRangeListener);
	}
	

	

}
