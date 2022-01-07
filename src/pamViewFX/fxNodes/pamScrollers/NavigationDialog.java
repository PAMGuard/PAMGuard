package pamViewFX.fxNodes.pamScrollers;

import PamController.PamController;
import PamController.SettingsPane;
import PamUtils.PamCalendar;
import pamScrollSystem.PamScrollerData;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.picker.DateTimeSpinnerPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class NavigationDialog extends SettingsPane<PamScrollerData> {
	
	/**
	 * Reference to the scroller for this dialog. 
	 */
	private AbstractPamScrollerFX scroller;
	
	/**
	 * Pane which contains date time spinner and labels 
	 */
	private PamHBox dateTimePane;
	
	/**
	 * Pane used to set the current start time. Sits inside dateTimePane
	 */
	private DateTimeSpinnerPane dateTimeSpinner;
	
	/**
	 * Pane which allows users to set data amount of data to load and step size. 
	 */
	private PamHBox dataLoadPane;

	/**
	 * Standard load time in millis. 
	 */
	private long[] standardLoadTimes = {1000, 2000, 5000, 10000, 30000, 60000, 120000,
			300*1000, 600*1000, 30*60*1000, 3600*1000, 3600*2*1000, 3600*6*1000,
			3600*12*1000, 3600*24*1000, 2*3600*24*1000, 7*3600*24*1000, 14L*3600L*24L*1000L, 30L*3600L*24L*1000L,
			60L*3600L*24L*1000L, 365L*3600L*24L*1000L};
	
	/**
	 * Standard step times. 
	 */
	private int[] standardStepTimes = {10, 25, 50, 75, 100};

	/**
	 * Choice box to set load duration. 
	 */
	private ChoiceBox<String> durationChoiceBox;

	/**
	 * Choice box to set step size. 
	 */
	private ChoiceBox<String> stepChoiceBox;

	/**
	 * Pam scroller data. 
	 */
	private PamScrollerData input;
	
	private  PamBorderPane outerPane = new PamBorderPane();

	public NavigationDialog(AbstractPamScrollerFX scroller){
		super(null);
		
		this.scroller=scroller;
		
		//pane to set start time of loaded date
		dateTimePane=new PamHBox();
		dateTimePane.setPadding(new Insets(10,10,10,10));
		dateTimePane.setSpacing(10);
		dateTimePane.setAlignment(Pos.CENTER_LEFT); 
		dateTimePane.getChildren().addAll(new Label("Start Time"), dateTimeSpinner=new DateTimeSpinnerPane());
		
		//choice box to set duration
		durationChoiceBox=new ChoiceBox<String>(createDurationList());

		//choice box to set step size
		stepChoiceBox=new ChoiceBox<String>(createStepTimesList());

		dataLoadPane=new PamHBox();
		dataLoadPane.setPadding(new Insets(10,10,10,10));
		dataLoadPane.setSpacing(10);
		dataLoadPane.setAlignment(Pos.CENTER_LEFT);
		dataLoadPane.getChildren().addAll(new Label("Load Time"), durationChoiceBox, new Label("Step Size"), stepChoiceBox);

		//create main pane  for dialog
		PamVBox mainPane=new PamVBox();
		mainPane.getChildren().addAll(dateTimePane, dataLoadPane);
		
		
		//FIXME for some reason when styling with settings CSS the dialog has strange resizing issues. This is due
		//to padding being set in the CSS- have fixed now but kept commented code to show workaround if ever needed in future. 
		outerPane.getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamSettingsCSS());
//		this.getDialogPane().getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamCSS());
//		this.getDialogPane().getStyleClass().add("dialog-pane-dark");
//		this.getDialogPane().lookupButton(ButtonType.OK).getStyleClass().add("dialog-button-dark");
//		this.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("dialog-button-dark");

		outerPane.setCenter(mainPane);
		
	}
	
//	public static PamScrollerData showDialog(Window owner, boolean lightweight, StageStyle style,  AbstractPamScrollerFX scroller) {
//		 
//		double width;
//		double height;
//		
//		if (singleInstance == null) {
//			singleInstance=new NavigationDialog(owner, lightweight, style, scroller);
//			//OK this is an ugly solution to get the size of the dialog. We could set a preferred size and use that
//			//BUT as we move to high DPI displays we want to do that as little as possible because it messes up DPI scaling. 
//			singleInstance.show();
//			singleInstance.close();
//		}
//
//		//work out size 
//		width=singleInstance.getDialogPane().getWidth();
//		height=singleInstance.getDialogPane().getHeight();
//		//following code opens the dialog next to the scroll bar. 
//		//must set location of stage after it is shown stage.getHeight/Width()=NanN before shown.
//		Point2D p2d=scroller.getSettingsButton().localToScreen(scroller.getSettingsButton().getLayoutX()-width, scroller.getSettingsButton().getLayoutY()-height);		
//	
//		return (PamScrollerData) singleInstance.showDialog(scroller.getScrollerData(), p2d);
//	}
	
	/**
	 * Create list of load times. 
	 * @return list of load times duration. 
	 */
	private ObservableList<String> createDurationList(){
		ObservableList<String> loadTimeList=FXCollections.observableArrayList();
		for (int i=0; i<standardLoadTimes.length; i++){
			loadTimeList.add(durationToString(standardLoadTimes[i]));
		}
		return loadTimeList;
	}
	
	/**
	 * Create list of step times.
	 * @return list of step sizes.
	 */
	private ObservableList<String> createStepTimesList(){
		ObservableList<String> stepSizeList=FXCollections.observableArrayList();
		for (int i=0; i<standardStepTimes.length; i++){
			stepSizeList.add(stepToString(standardStepTimes[i]));
		}
		return stepSizeList;
	}
	
	/**
	 * Convert step size to string.
	 * @param steSize. Step size 0->1 were 1 is 100%
	 */
	private String stepToString(double stepSize){
		return (" " + (Double.toString(stepSize))+"%");
	}
	
	/**
	 * Convert duration to string.
	 * @param duration in  millis.
	 */
	private String durationToString(long duration){
		return (" " + PamCalendar.formatDuration(duration));
	}


	public AbstractPamScrollerFX getScroller() {
		return scroller;
	}

	public void setScroller(AbstractPamScrollerFX scroller) {
		this.scroller = scroller;
	}

	@Override
	public void setParams(PamScrollerData input) {
		this.input=input;
		//set the duration choice box
		durationChoiceBox.getSelectionModel().select(durationToString(input.getLength()));
		//set the step size choice box. 
		stepChoiceBox.getSelectionModel().select(stepToString(input.getPageStep()));
		//set the date time spinner to the correct time. 
		dateTimeSpinner.setDateTime(input.getMinimumMillis());
	}

	@Override
	public PamScrollerData getParams(PamScrollerData p) {
		//get parameters. 
		try {
			input.setPageStep(standardStepTimes[stepChoiceBox.getSelectionModel().getSelectedIndex()]);
			input.setMinimumMillis(dateTimeSpinner.getDateTime());
			input.setMaximumMillis(input.getMinimumMillis()+standardLoadTimes[durationChoiceBox.getSelectionModel().getSelectedIndex()]);
		}
		catch (Exception e){
			e.printStackTrace();
		}
//		System.out.println("Navigation Dialog returned: Time: "+ PamCalendar.formatDateTime2(input.getMinimumMillis())+" Duration: "+durationChoiceBox.getValue()+" Step Size: "+stepChoiceBox.getValue());
		return input;
	}

	@Override
	public String getName() {
		return "Time Navigation";
	}

	@Override
	public Node getContentNode() {
		return outerPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
