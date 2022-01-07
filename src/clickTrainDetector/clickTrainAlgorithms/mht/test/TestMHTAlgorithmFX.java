package clickTrainDetector.clickTrainAlgorithms.mht.test;

import java.util.ListIterator;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import clickTrainDetector.clickTrainAlgorithms.mht.test.ExampleClickTrains.SimClickImport;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Runs a simple GUI and tests the MHT click train algorithm on a bunch
 * of simulated clicks. 
 * @author Jamie Macaulay 
 *
 */
public class TestMHTAlgorithmFX extends Application {

	/**
	 * The simulated click class. 
	 */
	private ExampleClickTrains simSimpleClicks;

	/**
	 * The MHT test algorithm 
	 */
	private MHTTestAlgorithm mhtTestlgorithm;

	/**
	 * The test chart
	 */
	private MHTTestChart mhTestChart;

	/**
	 * Task for the running the click trian detector. 
	 */
	private Task<Object> mhtWorker;

	/**
	 * The progress label
	 */
	private Label progressLabel;

	/**
	 * Animate check box. 
	 */
	private CheckBox animateCheckBox; 

	@Override 
	public void start(Stage stage) {

		simSimpleClicks = new ExampleClickTrains(); 
		simSimpleClicks.importSimClicks(SimClickImport.SIMCLICKS_1);

		mhtTestlgorithm = new MHTTestAlgorithm(); 

		//create the chart
		mhTestChart = new MHTTestChart(); 
		mhTestChart.setChartData(simSimpleClicks.getSimClicks()); 

		//create progress bar
		ProgressBar progressBar= new ProgressBar();
		progressBar.setProgress(0);
		progressBar.setPrefWidth(Double.MAX_VALUE);
		progressLabel = new Label("");
		
		VBox progresBox = new VBox();
		progresBox.setSpacing(5);
		progresBox.setPadding(new Insets(5,5,5,5));
		progresBox.setAlignment(Pos.CENTER);
		progresBox.getChildren().addAll(progressLabel,progressBar);
		
		
		//create spinner to set how many clicks to run on 
		PamSpinner<Integer> spinner =
				new PamSpinner<Integer>(3,100000,10,1); 
		spinner.setEditable(true);

		
		//create combo box to select test data. 
		ComboBox<SimClickImport> simDatasetBox = new ComboBox<SimClickImport>();
		simDatasetBox.getItems().addAll(FXCollections.observableArrayList(ExampleClickTrains.SimClickImport.values()));
		simDatasetBox.valueProperty().addListener((ov,  t,  t1)->{                
			simSimpleClicks.importSimClicks(t1);
			mhTestChart.setChartData(simSimpleClicks.getSimClicks()); 
			spinner.getValueFactory().setValue(simSimpleClicks.getSimClicks().getUnitsCount());
	    });
		simDatasetBox.getSelectionModel().select(0);
		
		
		animateCheckBox = new CheckBox("Animate");
		

		//create button to run simulation
		Button goButton = new Button("Run MHT"); 
		goButton.setPrefWidth(100);

		goButton.setOnAction((action)->{

			if (mhtWorker==null || !mhtWorker.isRunning()) {
				mhtWorker = createMHTWorker(spinner.getValue());

				progressBar.progressProperty().unbind();
				progressBar.progressProperty().bind(mhtWorker.progressProperty());
				
				progressBar.progressProperty().addListener((a,oldVal,newVal)->{
					progressLabel.setText(String.format("%.1f", newVal.doubleValue()*100.));
				});

				mhtWorker.setOnSucceeded((a)->{
					goButton.setText("Run MHT");
					
				});

				mhtWorker.setOnCancelled((a)->{
					goButton.setText("Run MHT");
				});

				goButton.setText("Stop");

				new Thread(mhtWorker).start();
			}
			else if (mhtWorker!=null) {
				mhtWorker.cancel();
			}
		});

		//button to set the maximum number of clicks. 
		Button allClicksButton = new Button("All Clicks");
		allClicksButton.setOnAction((action)->{
			spinner.getValueFactory().setValue(simSimpleClicks.getSimClicks().getUnitsCount());
		});

		HBox controls = new HBox(); 
		controls.setPadding(new Insets(5,5,5,5));
		controls.setSpacing(5);
		controls.getChildren().addAll(
				goButton, new Label("No. clicks"), spinner, allClicksButton, 
				new Label("Click Data") ,simDatasetBox, animateCheckBox); 
		controls.setAlignment(Pos.CENTER_LEFT);

		//create holder panes and scene. 
		BorderPane holder = new BorderPane(mhTestChart);
		holder.setTop(controls); 
		holder.setBottom(progresBox);

		Scene scene  = new Scene(new Group());
		holder.prefWidthProperty().bind(scene.widthProperty());
		holder.prefHeightProperty().bind(scene.heightProperty());
		((Group)scene.getRoot()).getChildren().add(holder);

		//		scene.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());
		//		holder.setStyle("-fx-background-color: -fx-darkbackground");

		stage.setTitle("MHT Test algorithm");
		stage.setScene(scene);
		stage.setWidth(1000);
		stage.setHeight(500);
		stage.show();
	}


	/**
	 * Create the task that runs the click train detector
	 * @param number - the number of clicks to run through. 
	 * @return a boolean object (at the moment nothing)
	 */
	public Task<Object> createMHTWorker(int number) {
		return new Task<Object>() {
			
			@Override
			protected Object call() throws Exception {
				runTrainDetector(number);
				return true;
			}

			/**
			 * Run the MHT algorithm on simulated data. 
			 * @param number - the number of clicks to run on. 
			 */
			private void runTrainDetector(int number) {
				//clear the kernel
				mhtTestlgorithm.clearKernel(); 
				mhtTestlgorithm.printSettings();

				//set up and run the algorithm 
				SimpleClickDataBlock simpleClickDataBlock = simSimpleClicks.getSimClicks(); 
				ListIterator<SimpleClick> simpleClickIterator = simpleClickDataBlock.getListIterator(0);
				SimpleClick simpleClick; 
				
				int n=0;
				while (simpleClickIterator.hasNext() && n<number) {
					if (this.isCancelled()) break;
					simpleClick=simpleClickIterator.next();
					//System.out.println(n + " Add click: " + simpleClick);
					mhtTestlgorithm.addSimpleClick(simpleClick);
					n=n+1; 
					updateProgress(n, number);
					if (animateCheckBox.isSelected()) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Platform.runLater(()->{
							mhTestChart.updateMHTGraphics(mhtTestlgorithm.getMHTKernel());
						});
					}
				}
				
				mhtTestlgorithm.mhtKernel.confirmRemainingTracks();
				Platform.runLater(()->{
					mhTestChart.updateMHTGraphics(mhtTestlgorithm.getMHTKernel());
				});
			}
		};
	}


	public static void main(String[] args) {
		launch(args); 
	}

}
