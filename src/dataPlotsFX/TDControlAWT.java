package dataPlotsFX;

import java.awt.Component;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.scroller.TDAcousticScroller;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import pamViewFX.fxStyles.PamStylesManagerFX;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import PamController.PAMStartupEnabler;
import PamController.PamController;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;

/**
 * TDControlFX acts as a wrapper class for a time base display programmed in JavaFX.
 * @author Jamie Macaulay
 */
public class TDControlAWT  extends TDControl implements UserDisplayComponent {

	private TDControlAWT tdControlfx;

	/**
	 * Checks for incoming data. 
	 */
	private DataObserver dataObserver;

	private String uniqueName;

	private TDDisplayProviderFX tdDisplayProviderFX;

	/**
	 * This panel acts as the interface between swing and JavaFX;
	 */
	TDFXPanel fxPanel;

	private UserDisplayControl userDisplayControl;

	public TDControlAWT(TDDisplayProviderFX tdDisplayProviderFX, UserDisplayControl userDisplayControl, String uniqueDisplayName){
		super(uniqueDisplayName); 
		this.tdDisplayProviderFX = tdDisplayProviderFX;
		this.userDisplayControl = userDisplayControl;
		setUniqueName(uniqueDisplayName);
		tdControlfx=this;
		fxPanel = new TDFXPanel();
		create();
	}

	/**
	 * Create the vital components for the display. 
	 */
	private void create(){
		dataObserver = new DataObserver();
	}


	@Override
	public Component getComponent() {

		//		/**
		//		 * 04/09/2018
		//		 * There is a problem if a TDDisplay is present but then shut down by the user then the FX thread automatically 
		//		 * shuts diown if there are no other FX dialogs  users. When a user opens up a new display then PG crashes. So have to add this line. 
		//		 */
		//		FXInitialiser.haveRun=false; 
		//		FXInitialiser.initialise();

		/**
		 * This gets called at startup from the AWT thread but launches an FX thread
		 * to create the actual display within a Swing frame (yep!).
		 * Need to send a message to PamController to hold off enabling the start 
		 * button and other controls until this thread has completed.
		 */
		PAMStartupEnabler.addDisableCount();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Group root=  new  Group();
				Scene scene  =  new  Scene(root, Color.GRAY);
				scene.getStylesheets().clear();
				scene.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getGUICSS());    

				setTDDisplay(new TDDisplayFX(tdControlfx));
				root.getChildren().add(getTDDisplay());
				setSceneBinding(scene, getTDDisplay());
				fxPanel.setScene(scene);
				fxPanel.setMainDisplay(getTDDisplay());
				TDAcousticScroller timeScroller = getTDDisplay().getTimeScroller();
				if (timeScroller != null && isViewer()) {
					timeScroller.coupleScroller(userDisplayControl.getUnitName());
				}

				PAMStartupEnabler.dropDisableCount();
			}
		});		

		return fxPanel;
	}

	/**
	 * This panel acts as an interface between the swing awt thread here and the JavaFX components in the tdgraph. 
	 * @author Jamie Macaulay
	 *
	 */
	public class TDFXPanel extends JFXPanel {

		private static final long serialVersionUID = 1L;

		private TDDisplayFX tdMainDisplay;

		public TDFXPanel() {
			// TODO Auto-generated constructor stub
		}

		public void setMainDisplay(TDDisplayFX tdMainDisplay){
			this.tdMainDisplay=tdMainDisplay;
		}

		public TDDisplayFX getMainDisplay(){
			return tdMainDisplay;
		}

		public double getScrollableRange(){
			double scrollableRange = tdMainDisplay.getTDParams().scrollableTimeRange;
			long visibleRange = tdMainDisplay.getTDParams().visibleTimeRange;
			return Math.max(scrollableRange, visibleRange);
		}

		public void scrollDisplayEnd(final long milliSeconds) {
			if (tdMainDisplay!=null){
				//				System.out.println("TDFXPanel: scrollDisplayEnd(milliSeconds)" );
				//must run on a different thread here 
				Platform.runLater(()->{
					tdMainDisplay.scrollDisplayEnd(milliSeconds);
				});
			}

		}

	}

	/**
	 * Get the main display. 
	 * @return the main display. 
	 */
	public TDDisplayFX getMainDisplay(){
		return fxPanel.getMainDisplay();
	}

	/**
	 * Create the FX components of the graph. 
	 * @param tdControl
	 * @param tdMainDisplay
	 * @return
	 */
	private static void setSceneBinding(Scene scene, final TDDisplayFX tdMainDisplay) {
		//need to make sure that the scene resizes with the JFrame.
		scene.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				tdMainDisplay.setPrefWidth(observableValue.getValue().doubleValue());
			}
		});
		scene.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				tdMainDisplay.setPrefHeight(observableValue.getValue().doubleValue());
			}
		});

	}



	/**
	 * The data observer monitors incoming data from data blocks. 
	 * @author Doug Gillespie and Jamie Macaulay
	 *
	 */
	private class DataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			if (PamRawDataBlock.class == o.getClass()) {
				return 0;
			}
			//			return 0;
			////			//TODO- this should be the range, not the visible range. 
			// should really be the maximum of the two. 
			return (long) (fxPanel.getScrollableRange());
		}


		@Override
		public String getObserverName() {
			return "Time Display FX";
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			//			System.out.println("MASTER CLOCK UPDATE: " + PamCalendar.formatDateTime(milliSeconds));
			fxPanel.scrollDisplayEnd(milliSeconds);
		}

	}

	/**
	 * Get the data observer- monitors incoming real time data an updates graphs. 
	 * @return data observer
	 */
	public DataObserver getDataObserver() {
		return dataObserver;
	}


	@Override
	public void openComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyModelChanged(int changeType) {
		//		System.out.println("TDControlAWT: Notify model changed: " + changeType );
		//need to push onto fx thread. 
		Platform.runLater(()->{
			if (tdMainDisplay!=null){
				tdMainDisplay.notifyModelChanged(changeType);
			}
		});
	}

	/**
	 * In real time mode check if PAMGUARD is paused. 
	 * @return true if paused. 
	 */
	public boolean isPaused(){
		if (PamController.getInstance().getPamStatus()==PamController.PAM_RUNNING) return false; 
		else return true; 
	}

	@Override
	public String getFrameTitle() {
		return "TD Display";
	}

}
