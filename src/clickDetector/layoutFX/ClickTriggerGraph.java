package clickDetector.layoutFX;

import java.util.ArrayList;

import PamUtils.PamArrayUtils;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.pamChart.PamLineChart;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSound;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundCategory;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;
import javafx.application.Platform; 

/**
 * Graph which shows a click and associated trigger functions
 * @author Jamie Macaulay
 *
 */
public class ClickTriggerGraph extends PamBorderPane {


	private NumberAxis xAxis;

	private NumberAxis yAxis; 

	/**
	 * The long filter
	 */
	private double longFilter=0.00001;

	/**
	 * The short filter
	 */
	private double shortFilter=0.01;

	/*
	 * Random noise added to the click and surrounding snippet.
	 */
	private double noise=0.1;

	private LineChart<Number, Number> plotChart;

	private int freq2;

	public static final int PREF_GRAPH_WIDTH = 400;

	/**
	 * The example sound factory. 
	 */
	private ExampleSoundFactory exampleSoundFactory;

	private Series<Number, Number> signalLevelSeries;

	private Series<Number, Number> noiseLevelSeries;

	Task<Integer> task;

	private double[] waveform;


	public ClickTriggerGraph(){
		this.exampleSoundFactory = new ExampleSoundFactory(); 
		this.setCenter(createWaveformGraph());
		this.setPrefWidth(400);
		//FIXME - seems to a resize bug in high DPI displays. Seems fixed in 8u60. 
		generateClickWaveform(ExampleSoundType.PORPOISE_CLICK, noise);

	}



	public double[] calcFilter(double[] waveform, double alpha){
		double[] filterVals=new double[waveform.length]; 
		for (int i=0; i<waveform.length; i++){
			if (i==0) filterVals[i]=Math.abs(waveform[i]*alpha);
			else filterVals[i]=alpha*Math.abs(waveform[i])+(1-alpha)*filterVals[i-1];
		}
		return filterVals;

	}


	public Pane createWaveformGraph(){

		StackPane stackPane = new PamStackPane(); 


		xAxis=new NumberAxis();
		xAxis.setMaxWidth(Double.MAX_VALUE);
		xAxis.setLabel("Sample");
		//xAxis.setAutoRanging(false);

		yAxis=new NumberAxis(-1,1,0.2);
		yAxis.setLabel("Amplitude");
		yAxis.setAutoRanging(false);

		plotChart=new PamLineChart<Number, Number>(xAxis, yAxis);
		plotChart.setMaxWidth(Double.MAX_VALUE);

		plotChart.setAnimated(false);
		plotChart.setLegendVisible(false);
		plotChart.setCreateSymbols(false);

		plotChart.getStyleClass().add("thin-chart");
		//plotChart.getXAxis().setSide(Side.BOTTOM);

		ChoiceBox<ExampleSoundType> waveformChoice = new ChoiceBox<ExampleSoundType>(); 
		ArrayList<ExampleSoundType> exampleSounds  = exampleSoundFactory.getExampleSoundTypes(ExampleSoundCategory.ODONTOCETES_CLICKS, ExampleSoundCategory.BAT);

		for (int i=0; i<exampleSounds.size(); i++) {
			waveformChoice.getItems().add(exampleSounds.get(i)); 
		}
		waveformChoice.setOnAction((action)->{
			generateClickWaveform(waveformChoice.getSelectionModel().getSelectedItem(), noise);
			 updateGraphWaveform() ;
			 updateGraphFilter();
		});
		waveformChoice.getSelectionModel().select(ExampleSoundType.PORPOISE_CLICK);
		waveformChoice.setPrefWidth(180);

		StackPane.setAlignment(waveformChoice, Pos.TOP_RIGHT);


		stackPane.getChildren().addAll(plotChart, waveformChoice); 	

		return stackPane;
	}

	public double getLongFilter() {
		return longFilter;
	}

	public void setLongFilter(double longFilter) {
		this.longFilter = longFilter;
	}


	/**
	 * Update the waveform of the species. 
	 */
	public synchronized void updateGraphWaveform() {
		plotChart.getData().clear();


		Series<Number, Number> waveformSeries = new Series<Number, Number>();
		for (int i=0; i<waveform.length; i++){
			waveformSeries.getData().add(new Data<Number, Number>(i, waveform[i])); 
		}

		plotChart.getData().add(waveformSeries);
	}
	/**
	 * Update graph. Delete current data series and create new one. 
	 */
	public synchronized void updateGraphFilter(){

		if (waveform.length<500) {
			if (task!=null) {
				task.cancel(true); 
			}

			//no need to do on separate thread. 
			plotChart.getData().remove(signalLevelSeries);
			plotChart.getData().remove(noiseLevelSeries);

			plotChart.getData().add(signalLevelSeries = calcSeries(waveform, shortFilter));
			plotChart.getData().add(noiseLevelSeries = calcSeries(waveform, longFilter));
		}
		else {
			if (task!=null) {
				if (task.isRunning()) {
					return;
				}
			}

			/***
			 * FIXME
			 * This is a little ridiculous but there are two bugs in JavaFX
			 * 1) The line chart is super slow
			 * 2) If you get a freeze in the FX application thread then your trigger the spinner button multiple times. 
			 * 
			 * Doing this on a thread gets rid of the issue but is not a particular nice solution. 
			 */

			task = new Task<Integer>() {
				@Override protected Integer call() throws Exception {

					Series<Number, Number> signalLevelSeries1 = calcSeries(waveform, shortFilter); 
					Series<Number, Number> noiseLevelSeries2 = calcSeries(waveform, longFilter); 

					Platform.runLater(()->{	
						//						if (shortFilter!=shortFilter1 || forceChange) {
						plotChart.getData().remove(signalLevelSeries);
						plotChart.getData().add(signalLevelSeries1);
						signalLevelSeries = signalLevelSeries1; 
						//						}

						//						if (shortFilter!=shortFilter1 || forceChange) {
						plotChart.getData().remove(noiseLevelSeries);
						plotChart.getData().add(noiseLevelSeries2);
						noiseLevelSeries = noiseLevelSeries2; 
						//						}

					});

					return 0;
				}
			};

			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
		}
	}

	private Series<Number, Number> calcSeries(double[] waveform, double alpha){
		Series<Number, Number> signalLevelSeries1 = new Series<Number, Number>();
		double[] signalLevel=calcFilter(waveform, alpha);

		for (int i=0; i<waveform.length; i++){
			signalLevelSeries1.getData().add(new Data<Number, Number>(i, signalLevel[i])); 
		}

		return signalLevelSeries1; 

	}

	/**
	 * Generate a click waveform with some added noise.
	 * @param type - the type of click e.g. ClickTriggerGraph.PORPOISE_CLICK.
	 * @param noise. 0  to 1. 1 means max noise amplitude will be same as maximum click amplitude. 
	 * @return click and noise waveform. 
	 */
	private void generateClickWaveform(ExampleSoundType selectedItem, double noise) {

		ExampleSound sound = this.exampleSoundFactory.getExampleSound(selectedItem); 

		double sR = sound.getSampleRate();

		//System.out.println("Waveform: " + sound.getWave().length + "  " + selectedItem);

		double[] clickWave =  PamArrayUtils.divide( sound.getWave(), PamArrayUtils.max( sound.getWave()));
		//now need to work out how many noise samples to add. Use the length of the click 
		int nNoiseSamples=Math.min((int) (clickWave.length), sound.getWave().length >2000? 0 : 100);
		double[] waveform=new double[2*nNoiseSamples + clickWave.length];
		int n=0;
		double[] signal=clickWave;
		for (int i=0; i<waveform.length; i++){
			double noiseSample=noise*(Math.random()-0.5);
			if (i>nNoiseSamples && n<clickWave.length){
				waveform[i]=signal[n]+noiseSample;
				n=n+1;
			}
			else waveform[i]=noiseSample;
		}

		this.waveform=waveform;
		//System.out.println("Waveform: " + waveform.length);
	}


	public double getShortFilter() {
		return shortFilter;
	}

	public void setShortFilter(double shortFilter) {
		this.shortFilter = shortFilter;
	}




}
