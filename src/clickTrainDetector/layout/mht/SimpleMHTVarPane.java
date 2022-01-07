package clickTrainDetector.layout.mht;

import java.math.BigDecimal;
import java.math.MathContext;

import org.controlsfx.control.PopOver;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;

/**
 * Simple Pane to change MHT settings. This contains a check box and slider bar which changes the 
 * error value. 
 * 
 * 
 * @author Jamie Macaulay
 * 
 */
public class SimpleMHTVarPane extends MHTVarSettingsPane<SimpleChi2VarParams>{
	
	/**
	 * The order of magnitude below and over the error scale value the sliders can go. e.g. if 
	 * 1 then the error can be 1/(10^SCALE_ORDER_MAGNITUDE) or x 10^(SCALE_ORDER_MAGNITUDE) over it's
	 *  default value. E.g. 2 is 0.01*default minimum and 100*default maximum. 
	 */
	private static double SCALE_ORDER_MAGNITUDE = 2; 

	/**
	 * Reference to the chi2 variable pane the settings pane is associated with
	 */
	protected SimpleChi2VarParams simpleChi2VarParams;

	/**
	 * The main pane
	 */
	private PamBorderPane mainPane;

	/*
	 * The slider. 
	 */
	protected Slider slider;

	/**
	 * The value label. 
	 */
	private Label valueLabel;

	/**
	 * The advanced settings pane. Holds editable spinners for changing all settings. 
	 */
	protected AdvMHTVarPane advPane;

	/**
	 * The pop ove for advanced menu
	 */
	private PopOver popOver;

	/**
	 * The current input
	 */
	protected SimpleChi2VarParams currentInput;

	/**
	 * Settings button for advanced params
	 */
	private PamButton advSettingsButton;

	/**
	 * The results convert converts between human readable and internal units (e.g degrees and radians )
	 */
	private ResultConverter resultConverter = new ResultConverter(); 

	public SimpleMHTVarPane(SimpleChi2VarParams simpleChi2Var) {
		super(null);
		this.simpleChi2VarParams = simpleChi2Var;
		mainPane= new PamBorderPane(); 
		mainPane.setCenter(createMHTVarPane());
	}
	
	public SimpleMHTVarPane(SimpleChi2VarParams simpleChi2Var, ResultConverter resultsConverter) {
		super(null);
		this.resultConverter = resultsConverter; 
		this.simpleChi2VarParams = simpleChi2Var;
		mainPane= new PamBorderPane(); 
		mainPane.setCenter(createMHTVarPane());
	}
	
	/**
	 * Create the advanced settings pane. 
	 * @return the advanced settings pane. 
	 */
	public AdvMHTVarPane createAdvMHTVarPane(SimpleChi2VarParams simpleChi2VarParams, ResultConverter resultConverter) {
		return new AdvMHTVarPane(simpleChi2VarParams, resultConverter); 
	}

	/**
	 * Create the MHT variable pane with controls to change the error. 
	 * @return a basic MHT chi2 variable pane
	 */
	private Pane createMHTVarPane() {

		PamHBox pamHBox = new PamHBox(); 
		pamHBox.setSpacing(5);

		this.advPane = createAdvMHTVarPane(simpleChi2VarParams, resultConverter); 
		advPane.addSettingsListener(()->{
			//update the params now. 
			//System.out.println("Settings Listener targetted: " + currentInput.minError); 
			if (currentInput!=null) {
			currentInput = advPane.getParams(currentInput);
			this.setParams(currentInput);
			}
		});

		//label to 
		//Label label= new Label(simpleChi2Var.getName()); 

		//create the slider and label to show value. 
		slider= new Slider(0, 100, 10); 
//		slider.setShowTickLabels(false);
//		slider.setShowTickMarks(false);
		valueLabel= new Label("");
		valueLabel.setTooltip(new Tooltip("Shows the current denominator error value.\n"
				+	"The error coefficient divides the difference in value between\n"
				+	"subsequent detections. It therefore essentially applies a weight\n"
				+ 	"to the measurement for the click trian detector. Higher values\n "
				+ 	"mean less weight and so the measurment is less important in\n "
				+ 	"determining whether a set of detection is a train.\n"
				+   "The error value can also be set in the advanced settings pane")); 
		//change the value label whenever the param changes.
		slider.valueProperty().addListener((obsVal, oldVal, newVal)->{
			//System.out.println("Hello I am null: "  + simpleChi2VarParams.name + "  " 
			//	+  simpleChi2VarParams.getResultConverter() + "   ::::   " +  simpleChi2VarParams); 
			setValueLabel(resultConverter.
					convert2Control(slider2Error(newVal.doubleValue(), simpleChi2VarParams.errorScaleValue)));
		});
		
		
		valueLabel.setMinWidth(30);
		setValueLabel(slider.getValue());
		slider.setTooltip(new Tooltip("The slider changes the denominator error value.\n "
				+	"The error coefficient divides the difference in value between\n"
				+	"subsequent detections. It therefore essentially applies a weight\n"
				+ 	"to the measurement for the click trian detector. Higher values\n "
				+ 	"mean less weight and so the measurment is less important in\n "
				+ 	"determining whether a set of detection is a train.\n"
				+ 	"The error value can also be set in the advanced settings pane"));

		advSettingsButton = new PamButton(); 
//		advSettingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		advSettingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		advSettingsButton.setOnAction((action)->{
			showAdvPane();
		});
		advSettingsButton.setTooltip(new Tooltip("Opens advanced settings for the MHT click train detector variable."));

		PamHBox.setHgrow(slider, Priority.ALWAYS);
		pamHBox.setAlignment(Pos.CENTER);
		
		pamHBox.getChildren().addAll(slider, valueLabel, advSettingsButton); 
		
		//make sure sliders stretch with resizing pane
		pamHBox.prefWidthProperty().bind(mainPane.widthProperty());

		return pamHBox;
	}

	/**
	 * Creates pane allowing the user to change fine scale things such as error limits. 
	 * @return the pop over pane. 
	 */
	public void showAdvPane() {

		if (popOver==null) {
			popOver = new PopOver(); 
			popOver.setContentNode(advPane.getContentNode());
		}

		popOver.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				//update any info into params before showing adv dialog. 
				currentInput=getParams(currentInput);
				advPane.setParams(currentInput);
				
			}
			else {
				//System.out.println("Pop over closed: ");
				currentInput = advPane.getParams(currentInput);
			}
		});

		popOver.show(advSettingsButton);
	} 

	/**
	 * Set the value label. 
	 * @param newVal - the new value
	 */
	private void setValueLabel(double newVal) {
		//when slider moves show value to 3 significant figures. 
		BigDecimal bd = new BigDecimal(newVal);
		bd = bd.round(new MathContext(3));
		double rounded = bd.doubleValue();
		valueLabel.setText(String.valueOf(rounded) + simpleChi2VarParams.getUnits());
	}


	@Override
	public SimpleChi2VarParams getParams(SimpleChi2VarParams currParams) {

		//get params from adv settings pane.
		currParams = advPane.getParams(currParams);

		//the error limits
//		double[] errLimits = new double[] {simpleChi2Var.getResultConverter().convert2Value(slider.getMin()), 
//				simpleChi2Var.getResultConverter().convert2Value(slider.getMax())};
		//currParams.errLimits = errLimits; 

		//the error
		currParams.error= slider2Error(slider.getValue(), simpleChi2VarParams.errorScaleValue);

		//return the modified parameter 
		return currParams;
	}

	@Override
	public void setParams(SimpleChi2VarParams simpleChi2VarParams) {
			
		this.simpleChi2VarParams = simpleChi2VarParams; 

		currentInput = simpleChi2VarParams.clone(); 
		
		
//		this.slider.setMin(simpleChi2Var.getResultConverter().convert2Control(input.errLimits[0]));
//		this.slider.setMax(simpleChi2Var.getResultConverter().convert2Control(input.errLimits[1]));
//		
//		slider.setMajorTickUnit(simpleChi2Var.getResultConverter().convert2Control((input.errLimits[1]-input.errLimits[0])/10));
//		slider.setMinorTickCount(5);

//		this.slider.setShowTickLabels(false);
//		this.slider.setShowTickMarks(false);

		this.slider.setValue(error2Slider(simpleChi2VarParams.error, simpleChi2VarParams.errorScaleValue));
		
		this.advPane.setParams(currentInput);
	}
	
	/**
	 * Convert a slider value to an error value. The slider is on a log scale in order to allow
	 * the user to change by an order of magnitude defined by SCALE_ORDER_MAGNITUDE. 
	 * @param sliderValue - the slider value
	 * @param errorScaleValue the error. 
	 */
	public static double slider2Error(double sliderValue, double errorScaleValue){
		//So the error scale value should be 50% of the slider. We can go 10 times above and times below the value. 
		return errorScaleValue*Math.exp(0.0461*SCALE_ORDER_MAGNITUDE * sliderValue)/(Math.pow(10, SCALE_ORDER_MAGNITUDE)); 
	}
	
	/**
	 * Convert an error value to a slider value. The slider is on a log scale in order to allow
	 * the user to change by an order of magnitude defined by SCALE_LIMITS. 
	 * @param sliderValue - the slider results
	 * @param errorScaleValue - 
	 */
	public static double error2Slider(double errorValue, double errorScaleValue){
		return Math.log((Math.pow(10, SCALE_ORDER_MAGNITUDE)*errorValue)/errorScaleValue)/(0.0461*SCALE_ORDER_MAGNITUDE);
	}

	@Override
	public String getName() {
		return "MHT Chi^2 Var Pane";
	}

	@Override
	public Node getContentNode() {
		return this.mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void notifyChange(int flag, Object data) {
		switch (flag) {
		case ClickTrainControl.NEW_PARENT_DATABLOCK:
			//pass along to the MHTChi2 pane. Nothing to change here. 
			this.advPane.notifyChange(ClickTrainControl.NEW_PARENT_DATABLOCK, data);
//			
//			PamDataBlock data = (PamDataBlock) data; 
//			data.getLocalisationContents().
			break; 
		}
	}
	
//	private void slider2ErrorTest() {
//		
//	}
	

}
