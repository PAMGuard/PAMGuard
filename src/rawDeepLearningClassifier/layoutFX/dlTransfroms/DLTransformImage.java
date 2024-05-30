package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import java.util.ArrayList;
import java.util.Arrays;

import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;
import org.controlsfx.control.decoration.StyleClassDecoration;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jdl4pam.transforms.SimpleTransform;
import org.jamdev.jdl4pam.transforms.WaveTransform;
import org.jamdev.jpamutils.wavFiles.AudioData;
import org.jamdev.jpamutils.wavFiles.FilterParams;

import PamUtils.PamArrayUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.sliders.ColourRangeSlider;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.fxPlotPanes.PlotPane;
import pamViewFX.validator.PamValidator;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSound;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory;
import rawDeepLearningClassifier.layoutFX.exampleSounds.ExampleSoundFactory.ExampleSoundType;

/**
 * 
 * Shows preview spectrogram or waveform for a DL transforms
 * 
 * @author Jamie Macaulay 
 *
 */
public abstract class DLTransformImage extends PamBorderPane{

	/**
	 * Plot pane. 
	 */
	private TransformPlotPane plotPane;  

	/**
	 * Colour array. 
	 */
	private ColourArray colArray = ColourArray.createFireArray(256);

	/**
	 * Choice box to select what example sound to use. 
	 */
	private ChoiceBox<ExampleSoundType> speciesChoiceBox;

	/**
	 * Choice box to select which transform to preview. 
	 */
	private ChoiceBox<DLTransform> transformschoiceBox; 

	/**
	 * Example sound factory. 
	 */
	private ExampleSoundFactory exampleSoundFactory = new ExampleSoundFactory();

	/**
	 * The current two dimensional transform data. 
	 */
	private SpectrogramImage specImage = null; 

	/**
	 * The current 1D transform data. 
	 */
	private double [] data1D = null; 

	/**
	 * The current two dimensional transform data. 
	 */
	private double[][] data2D = null;

	/**
	 * The current example sounds - contains the raw waveform. 
	 */
	private ExampleSound exampleSound;

	/**
	 * The colour range slider. 
	 */
	private ColourRangeSlider colRangeSlider;

	/**
	 * Time slider. 
	 */
	private RangeSlider timeSlider;

	/**
	 * Time bins.
	 */
	private int[] timeBins = new int[2];

	private double[] data1DminMax;

	private float transformsR;

	/**
	 * The time label. 
	 */
	private Label timeLabel;

	/**
	 * The error label. 
	 */
	private Label errorLabel; 


	public DLTransformImage() {
		plotPane = new TransformPlotPane(); 
		plotPane.setAxisVisible(true, false, false, true); 
		plotPane.setPrefHeight(150);

		timeLabel = new Label(""); 

		//		//create a time slider. 
		timeSlider = new RangeSlider(); 
		timeSlider.setShowTickLabels(true);
		timeSlider.setShowTickMarks(true);
		timeSlider.highValueProperty().addListener((obsval, oldval, newval)->{
			calcTimeBins(transformsR);
			plotPane.repaint();
		});
		timeSlider.lowValueProperty().addListener((obsval, oldval, newval)->{
			calcTimeBins(transformsR);
			plotPane.repaint();
		}); 

		//create choice box for time slider.
		transformschoiceBox	 = new ChoiceBox<DLTransform>(); 
		transformschoiceBox.valueProperty().addListener((obsval, oldval, newval)->{
			updateTransformImage();
			//			calcTimeBins(transformsR);
			//			plotPane.repaint();
		});
		transformschoiceBox.setConverter(new DLTransformConverter());
		transformschoiceBox.setPrefWidth(170); 

		plotPane.getPlotCanvas().widthProperty().addListener((obsval, oldval, newval)->{
			plotPane.repaint();
		});

		plotPane.getPlotCanvas().heightProperty().addListener((obsval, oldval, newval)->{
			plotPane.repaint();
		});

		//create a colour slider for the spectrogram
		colRangeSlider = new ColourRangeSlider(Orientation.VERTICAL); 
		colRangeSlider.setColourArrayType(ColourArrayType.FIRE);
		colRangeSlider.setShowTickLabels(true);
		colRangeSlider.setShowTickMarks(true);

		colRangeSlider.highValueProperty().addListener((obsval, oldval, newval)->{
			specImage = new SpectrogramImage(data2D, colArray, new double[] {colRangeSlider.getLowValue(), colRangeSlider.getHighValue()} , false); 
			plotPane.repaint();

		});
		colRangeSlider.lowValueProperty().addListener((obsval, oldval, newval)->{
			specImage = new SpectrogramImage(data2D, colArray, new double[] {colRangeSlider.getLowValue(), colRangeSlider.getHighValue()}, false); 
			plotPane.repaint();

		});

		plotPane.getAxis(Side.BOTTOM).setLabel("Time (ms)");


		speciesChoiceBox = new ChoiceBox<ExampleSoundType>(); 
		speciesChoiceBox.getItems().addAll( FXCollections.observableArrayList(ExampleSoundType.values())); 
		speciesChoiceBox.valueProperty().addListener((obsval, oldval, newval)->{
			updateExampleSound(newval); 
		});
		speciesChoiceBox.getSelectionModel().select(0);
		speciesChoiceBox.setPrefWidth(170); 

		PamHBox hBox = new PamHBox(); 
		hBox.setAlignment(Pos.CENTER_RIGHT);
		hBox.setSpacing(5);
		hBox.getChildren().addAll(new Label("Example sound"), speciesChoiceBox, transformschoiceBox); 
		BorderPane.setMargin(hBox, new Insets(5,0,0,0));

		this.setTop(hBox);
		BorderPane.setMargin(hBox, new Insets(0,5,10,5));
		this.setCenter(plotPane);
		BorderPane.setMargin(colRangeSlider, new Insets(40,5,0,5));
		plotPane.setRight(colRangeSlider);


		//BIT OF A HACK but works to align the range slider wiht the axis. 
		timeSlider.translateXProperty().bind(plotPane.getAxisPane(Side.LEFT).widthProperty());
		plotPane.setBottom(timeSlider);

		timeSlider.maxWidthProperty().bind(plotPane.getPlotCanvas().widthProperty().add(5));

		//create an error label 
		errorLabel = new Label();
		Node decoration = PamGlyphDude.createPamIcon("mdi2c-close-circle-outline", Color.RED, 10);
		errorLabel.setGraphic(decoration);
//		Decorator.addDecoration(errorLabel, new GraphicDecoration(decoration, Pos.CENTER_RIGHT));

		BorderPane.setAlignment(timeLabel, Pos.CENTER);
		this.setBottom(timeLabel);

	}

	
	/**
	 * Get the species choice box. 
	 * @return - the species choice box. 
	 */
	public ChoiceBox<ExampleSoundType> getSpeciesChoiceBox() {
		return speciesChoiceBox;
	}


	/**
	 * Calculate the sample time bins. 
	 */
	private void calcTimeBins(float sampleRate) {

		timeBins[0] = (int) (sampleRate*(timeSlider.getLowValue()/1000.0));
		timeBins[1] = (int) (sampleRate*(timeSlider.getHighValue()/1000.0));

		this.plotPane.getAxis(Side.TOP).setMinVal(timeSlider.getLowValue());
		this.plotPane.getAxis(Side.TOP).setMaxVal(timeSlider.getHighValue());

		long[] shape = calcShape(); 

		double nSamples = this.exampleSound.getSampleRate()*((timeSlider.getHighValue()-timeSlider.getLowValue())/1000.0);


		if (shape!=null && shape.length==2) {
			int timeShape =  (int) (shape[0]*(nSamples/(double) exampleSound.getWave().length));
			timeLabel.setText(String.format("Segment size %.0f (samples) Transform shape: [%d %d]",  nSamples, timeShape, shape[1])); 
		}
		else if (shape!=null && shape.length==1) {
			int timeShape =  (int) (shape[0]*(nSamples/(double) exampleSound.getWave().length));
			timeLabel.setText(String.format("Segment size %.0f (samples) Transform shape: [%d]",  nSamples, timeShape)); 
		}
		else {
			timeLabel.setText(String.format("Segment size %.0f (samples) Transform shape: NaN",  nSamples)); 
		}
	}

	
	/**
	 * Calculate the output shape. 
	 */
	private long[] calcShape() {

		//bit tricky - do not want to recalculate the transforms every time the time slider moves. 
		if  (this.exampleSound==null)  return null; 

		ArrayList<DLTransform> transforms = getDLTransforms(); 

		if  (transforms==null)  return null; 

		long[] shape; 
		if (transforms.get(transforms.size()-1) instanceof FreqTransform) {
			FreqTransform freqTransform = ((FreqTransform) transforms.get(transforms.size()-1)); 

			if (freqTransform.getSpecTransfrom()==null) return null; 

			double[][] data2D = freqTransform.getSpecTransfrom().getTransformedData();

			shape = new long[] {data2D.length, data2D[0].length};
		}
		else {
			WaveTransform waveTransform = ((WaveTransform) transforms.get(transforms.size()-1)); 

			//			System.out.println("Get wave data: " +  waveTransform.getDLTransformType() + "  " +  waveTransform.getWaveData()); 

			if (waveTransform.getWaveData()==null) {
				return null;
			}

			double[] data = waveTransform.getWaveData().getScaledSampleAmplitudes();

			shape = new long[] {data.length};
		}

		return shape;
	}


	/**
	 * Called whenever there are new settings. 
	 */
	public void newSettings() {
		transformschoiceBox.getItems().clear();
		if (getDLTransforms()!=null && getDLTransforms().size()>0) {
			transformschoiceBox.getItems().addAll(FXCollections.observableArrayList(getDLTransforms()));
			transformschoiceBox.getSelectionModel().select(transformschoiceBox.getItems().size()-1);
			//listener should handle drawing stuff. 
		}
	}


	/**
	 * Get the current DL transforms. 
	 * @param dlTransform - the DLTransform list. 
	 */
	public abstract ArrayList<DLTransform> getDLTransforms(); 


	/**
	 * Get the sample rate for the example wave. 
	 * @return the sR for the example wave. 
	 */
	public float getExampleSR() {
		if (this.exampleSound==null) return -1.0f; 
		return this.exampleSound.getSampleRate();
	}

	
	/**
	 * Update the example sound. 
	 * @param exampleSoundType - the example sound type. 
	 */
	private void updateExampleSound(ExampleSoundType exampleSoundType) {
		this.exampleSound = exampleSoundFactory.getExampleSound(exampleSoundType); 
		updateExampleSound(exampleSound); 	
	}

	
	/**
	 * Update the example sound. 
	 * @param exampleSound - the new example sound. 
	 */
	public void updateExampleSound(ExampleSound exampleSound) {
		//update the time slider to show the correct time. 
		double maxMillis = 1000.0*exampleSound.getWave().length/exampleSound.getSampleRate(); 
		this.timeSlider.setMin(0.0);
		this.timeSlider.setMax(maxMillis);
		this.timeSlider.setLowValue(0.0);
		this.timeSlider.setHighValue(maxMillis);
		this.timeSlider.setMajorTickUnit(maxMillis/5);

		this.plotPane.getAxis(Side.TOP).setLabel("Time (ms)");
		this.plotPane.getAxis(Side.TOP).setMinVal(0);
		this.plotPane.getAxis(Side.TOP).setMaxVal(maxMillis);

		if (getDLTransforms()==null) return; 

		updateTransformImage();
	}

	
	/**
	 * Update the transform image to the latest selected transform and data params. 
	 */
	public boolean updateTransformImage() {

		setErrorMessage(null); 


		if  (this.exampleSound==null) {
			System.err.println("DLTRanfromImage.updateTransformImage: the example sound is null"); 
			return false; 
		}

		specImage=null;
		data1D=null; 

		AudioData soundData = new AudioData(exampleSound.getWave(), exampleSound.getSampleRate()); 

		((WaveTransform)  getDLTransforms().get(0)).setWaveData(soundData); 

		DLTransform currentTransform =  getDLTransforms().get(0); 

		int i = 0;
		String errCheck;
		//update all the transforms with the transformed data
		for (i=0; i<transformschoiceBox.getSelectionModel().getSelectedIndex()+1; i++) {
			errCheck = checkTransform(getDLTransforms().get(i), currentTransform);
			if (errCheck!=null) {
				setErrorMessage("Error in " + getDLTransforms().get(i).getDLTransformType() + " - " + errCheck); 
				return false;
			}
			//				System.out.println("TRANSFROM IMAGE 1: " + currentTransform.getDLTransformType() + " index: " + i + " " + ((WaveTransform) currentTransform).getWaveData().getSampleAmplitudes().length);
			currentTransform = getDLTransforms().get(i).transformData(currentTransform);
			//				System.out.println("TRANSFROM IMAGE 2: " + currentTransform.getDLTransformType() + " index: " + i + " " + ((WaveTransform) currentTransform).getWaveData().getSampleAmplitudes().length);
		}

		if (currentTransform instanceof FreqTransform) {
			if (((FreqTransform) currentTransform).getSpecTransfrom()!=null) {

				data2D = ((FreqTransform) currentTransform).getSpecTransfrom().getTransformedData();

				double[] minMax = PamArrayUtils.minmax(data2D); 

				colRangeSlider.setDisable(false);

				if (currentTransform.getDLTransformType()==DLTransformType.SPECCLAMP) {
					colRangeSlider.setMin(0.0);
					colRangeSlider.setMax(1.0);
				}
				else { 
					colRangeSlider.setMin(minMax[0]);
					colRangeSlider.setMax(minMax[1]);
				}

				colRangeSlider.setLowValue(minMax[0]);
				colRangeSlider.setHighValue(minMax[1]);
				colRangeSlider.setMajorTickUnit((minMax[1]-minMax[0])/3.0);

				double[] freqLims= ((FreqTransform) currentTransform).getFreqlims(); 

				if (freqLims[1]<1000.0) {
					plotPane.getAxis(Side.LEFT).setLabel("Frequency (Hz)");
					plotPane.getAxis(Side.LEFT).setMinVal(freqLims[0]);
					plotPane.getAxis(Side.LEFT).setMaxVal(freqLims[1]);
				}
				else {
					plotPane.getAxis(Side.LEFT).setLabel("Frequency (kHz)");
					plotPane.getAxis(Side.LEFT).setMinVal(freqLims[0]/1000.0);
					plotPane.getAxis(Side.LEFT).setMaxVal(freqLims[1]/1000.0);
				}

				specImage = new SpectrogramImage(data2D, colArray, PamArrayUtils.minmax(data2D), false); 
				transformsR = ((FreqTransform) currentTransform).getSpecTransfrom().getSpectrgram().getSampleRate(); 
			}
			else {
				//error 
				setErrorMessage("Error in " + currentTransform.getDLTransformType()); 
				return false;
			}
		}

		if (currentTransform instanceof WaveTransform) {
			if (((WaveTransform) currentTransform).getWaveData()!=null && ((WaveTransform) currentTransform).getWaveData().getSampleAmplitudes().length>0) {
				plotPane.getAxis(Side.LEFT).setLabel("Amplitude");

				colRangeSlider.setDisable(true);

				data1D = ((WaveTransform) currentTransform).getWaveData().getScaledSampleAmplitudes(); 
				data1DminMax= PamArrayUtils.minmax(data1D); 

				plotPane.getAxis(Side.LEFT).setMinVal(data1DminMax[0]);
				plotPane.getAxis(Side.LEFT).setMaxVal(data1DminMax[1]);

				transformsR =  ((WaveTransform) currentTransform).getWaveData().getSampleRate(); 
				//System.out.println("Spec wave data: " + ((WaveTransform) currentTransform).getWaveData().getScaledSampleAmpliudes()[10]); 
			}
			else {
				setErrorMessage("Error in " + currentTransform.getDLTransformType()); 
				return false;
			}

		}

		calcTimeBins(transformsR);
		plotPane.repaint();
		return true;
	}

	
	/**
	 * Set an error message in the plit
	 * @param string
	 */
	public void setErrorMessage(String string) {
		if (string==null) {
			this.setCenter(plotPane);
			return;
		}
		plotPane.getPlotCanvas().getGraphicsContext2D().clearRect(0, 0, plotPane.getPlotCanvas().getWidth(),
				plotPane.getPlotCanvas().getHeight());


		errorLabel.setText(string);


		this.setCenter(errorLabel);

		//			getPlotCanvas().getGraphicsContext2D().setStroke(Color.RED);
		//		getPlotCanvas().getGraphicsContext2D().setFill(Color.RED);
		//		getPlotCanvas().getGraphicsContext2D().strokeText(string, 10, getPlotCanvas().getHeight()/2);
	}


	/**
	 * Check for error in a transform and return the transform if true; 
	 * @param dlTransform - the new transform
	 * @param currentTransform - the data t be transformed
	 * @return a string error message. 
	 */
	private String checkTransform(DLTransform dlTransform, DLTransform currentTransform) {

		String error = null;
		SimpleTransform simpleTransform = ((SimpleTransform) dlTransform);
		float sampleRate;
		switch (dlTransform.getDLTransformType()) {
		case DECIMATE: 
		case DECIMATE_SCIPY:			
			//decimation sampleRate
			sampleRate = simpleTransform.getParams()[0].floatValue();
			double seconds = ((WaveTransform) currentTransform).getWaveData().getLengthInSeconds();
			
			if (seconds < 3/sampleRate) {
				//only three samples left
				error = "The decimation Nyquist is too low for the sample sound. Use a different example species";
			}

			break;
		case ENHANCE:
			break;
		case FILTER:
			FilterParams params = WaveTransform.transform2FilterParams(simpleTransform.getParams());
			sampleRate = ((WaveTransform) currentTransform).getWaveData().getSampleRate();

			if (params.filterType==AudioData.BANDPASS || params.filterType== AudioData.LOWPASS) {
				//filter cutoff must be within sample rate. 
				if (params.highCut>=sampleRate) {
					error = "The upper filter frequency cannnot be greater than or equal to Nyquist";
				}
			}
			if (params.filterType==AudioData.BANDPASS || params.filterType== AudioData.HIGHPASS) {

				if (params.lowCut>=sampleRate) {
					error = "The lower filter frequency cannnot be greater than or equal to Nyquist";
				}
			}
			break;
		case FILTER_ISOLATED_SPOTS:
			break;
		case GAUSSIAN_FILTER:
			break;
		case MEDIANFILTER:
			break;
		case NORMALISE_WAV:
			break;
		case PREEMPHSIS:
			break;
		case REDUCETONALNOISE_MEAN:
			break;
		case REDUCETONALNOISE_MEDIAN:
			break;
		case SPEC2DB:
			break;
		case SPECCLAMP:
			break;
		case SPECCROPINTERP:
			break;
		case SPECNORMALISE:
			break;
		case SPECNORMALISEROWSUM:
			break;
		case SPECNORMALISESTD:
			break;
		case SPECNORMALISE_MINIMAX:
			break;
		case SPECTROGRAM:
			break;
		case SPECTROGRAMKETOS:
			break;
		case TRIM:
			break;
		default:
			break;

		}
		return error;
	}


	/**
	 * A pane for showing a transform. 
	 * 
	 */
	public class TransformPlotPane extends PlotPane {


		public void repaint() {

			//run the current transforms on the example sound 
			getPlotCanvas().getGraphicsContext2D().clearRect(0, 0, 	getPlotCanvas().getWidth(), getPlotCanvas().getHeight());

			if (specImage!=null) {
				//work out how to paint with time slider settings. 

				double x1 = (timeSlider.getLowValue()/(timeSlider.getMax()-timeSlider.getMin()))*specImage.getRawSpecImage().getWidth();
				double x2 = (timeSlider.getHighValue()/(timeSlider.getMax()-timeSlider.getMin()))*specImage.getRawSpecImage().getWidth();

				getPlotCanvas().getGraphicsContext2D().drawImage(specImage.getRawSpecImage(), x1, 0, x2-x1, specImage.getRawSpecImage().getHeight(),  0, 0, 
						getPlotCanvas().getWidth(), getPlotCanvas().getHeight());

			}
			else if (data1D!=null) {
				//plot 1D data
				double x1, x2, y1, y2; 
				double[] dataTrim = Arrays.copyOfRange(data1D, timeBins[0], timeBins[1]); 
				double dataRange = (data1DminMax[1]- data1DminMax[0]); 
				for (int i=0; i<dataTrim.length-1; i++) {

					x1 = (i/(double) dataTrim.length)*getPlotCanvas().getWidth(); 
					x2 = ((i+1)/(double) dataTrim.length)*getPlotCanvas().getWidth(); 

					//System.out.println(" Canvas Width: x2: " + x2 + " cwidth: " +  getPlotCanvas().getWidth()+ "Trim: " + dataTrim[i]); 

					y1=  ((dataTrim[i]- data1DminMax[0])/(dataRange))*getPlotCanvas().getHeight(); 
					y2=  ((dataTrim[i+1]- data1DminMax[0])/(dataRange))*getPlotCanvas().getHeight(); 

					getPlotCanvas().getGraphicsContext2D().setStroke(Color.BLACK);
					getPlotCanvas().getGraphicsContext2D().setFill(Color.BLACK);
					getPlotCanvas().getGraphicsContext2D().strokeLine(x1, y1, x2, y2);
				}
			}
		}
	}


	/**
	 * String converter to show sensible names for transforms
	 * @author Jamie Macaulay
	 *
	 */
	public class DLTransformConverter extends StringConverter<DLTransform> {

		public DLTransform fromString(String string) {
			// convert from a string to a myClass instance
			return null;
		}

		public String toString(DLTransform myClassinstance) {
			if (myClassinstance==null) return "null"; 
			// convert a myClass instance to the text displayed in the choice box
			return myClassinstance.getDLTransformType().toString(); 
		}
	}

}
