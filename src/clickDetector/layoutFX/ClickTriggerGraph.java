package clickDetector.layoutFX;

import clickDetector.ClickParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.pamChart.PamLineChart;
import simulatedAcquisition.sounds.ClickSound;
import simulatedAcquisition.sounds.ClickSound.WINDOWTYPE;
import simulatedAcquisition.sounds.SimSignal;

/**
 * Graph which shows a click and associated trigger functions
 * @author Jamie Macaulay
 *
 */
public class ClickTriggerGraph extends PamBorderPane {
	
	public final static int PORPOISE_CLICK=0; 
	
	public final static int SPERM_WHALE=1; 


	private int currentClick=PORPOISE_CLICK;
	
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

	private double[] waveform;
	
	public ClickTriggerGraph(){
		this.setCenter(createWaveformGraph());
		this.setPrefWidth(400);
		//FIXME - seems to a resize bug in high DPI displays. Seems fixed in 8u60. 
		this.waveform=generateClickWaveform(currentClick, noise);		

	}
	
	/**
	 * Update graph. Delete current data series and create new one. 
	 */
	public void updateWaveformGraph(ClickParameters clickParameters){
		
//		System.out.println(" Graph width: xAxis: "+xAxis.widthProperty().get());
//		System.out.println(" Graph width: this: "+this.widthProperty().get());
//		System.out.println(" Graph width: plotChart: "+plotChart.widthProperty().get());
//		System.out.println(" xAxisLayout:: "+xAxis.layoutXProperty().get());
//		System.out.println(" plotLayout:: "+plotChart.layoutXProperty().get());
	
		Series<Number, Number> waveformSeries = new Series<Number, Number>();
		Series<Number, Number> signalLevelSeries = new Series<Number, Number>();
		Series<Number, Number> noiseLevelSeries = new Series<Number, Number>();

		
		double[] signalLevel=calcFilter(waveform, shortFilter);
		double[] noiseLevel=calcFilter(waveform, longFilter);

		
		for (int i=0; i<waveform.length; i++){
			waveformSeries.getData().add(new Data<Number, Number>(i, waveform[i])); 
			signalLevelSeries.getData().add(new Data<Number, Number>(i, signalLevel[i])); 
			noiseLevelSeries.getData().add(new Data<Number, Number>(i, noiseLevel[i])); 

		}
		
		plotChart.getData().removeAll(plotChart.getData());
		plotChart.getData().addAll(waveformSeries,signalLevelSeries,noiseLevelSeries);
	}
	
	
	public double[] calcFilter(double[] waveform, double alpha){
		double[] filterVals=new double[waveform.length]; 
		for (int i=0; i<waveform.length; i++){
			if (i==0) filterVals[i]=Math.abs(waveform[i]*alpha);
			else filterVals[i]=alpha*Math.abs(waveform[i])+(1-alpha)*filterVals[i-1];
		}
		return filterVals;
		
	}
	
	/**
	 * Generate a click waveform with some added noise.
	 * @param type - the type of click e.g. ClickTriggerGraph.PORPOISE_CLICK.
	 * @param noise. 0  to 1. 1 means max noise amplitude will be same as maximum click amplitude. 
	 * @return click and noise waveform. 
	 */
	private double[] generateClickWaveform(int type, double noise){
		SimSignal clickSound;
		int sR;
		double length;
		double freq;
		switch (type){
		//TODO - add more types of clicks. 
		case PORPOISE_CLICK:
			clickSound=new ClickSound("Porpoise", freq=140000, freq2=140000, length=0.00015, WINDOWTYPE.HANN);
			sR=500000;
			break;
		case SPERM_WHALE:
			clickSound=(new ClickSound("Beaked Whale", 30000, 60000, length = 0.3e-3, WINDOWTYPE.HANN));
			sR=192000;
			break;
		default:
			clickSound=new ClickSound("Porpoise", freq=140000, freq2=140000, length=0.00015, WINDOWTYPE.HANN);
			sR=500000;
			break;
		}

		//now need to work out how many noise samples to add. Use the length of the click 
		int nNoiseSamples=(int) (2*length*sR);
		double[] waveform=new double[3*nNoiseSamples];
		int n=0;
		for (int i=0; i<3*nNoiseSamples; i++){
			double noiseSample=noise*(Math.random()-0.5);
			double[] signal=clickSound.getSignal(0,sR,0);
			if (i>nNoiseSamples && n <signal.length){
				waveform[i]=signal[n]+noiseSample;
				n=n+1;
			}
			else waveform[i]=noiseSample;
		}
		
		return waveform;
	}
	
	public LineChart<Number, Number> createWaveformGraph(){
		
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
		return plotChart;
	}
	
	public double getLongFilter() {
		return longFilter;
	}

	public void setLongFilter(double longFilter) {
		this.longFilter = longFilter;
	}

	public double getShortFilter() {
		return shortFilter;
	}

	public void setShortFilter(double shortFilter) {
		this.shortFilter = shortFilter;
	}


	

}
