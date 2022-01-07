package clickTrainDetector.layout.mht;

import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.ResultConverter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Pane with a few more advanced settings for the MHT variables. 
 * 
 * @author Jamie Macaulay
 *
 */
public class AdvMHTVarPane extends DynamicSettingsPane<SimpleChi2VarParams> {
	
	/**
	 * Default divisor of error for min error. 
	 */
	private static final Double ERROR_DIVISOR = 10000.0;

	/**
	 * The main pane. 
	 */
	private PamBorderPane mainPane;
	
	/**
	 * The error value 
	 */
	private PamSpinner<Double> errorSpinner;
	
	/**
	 * The minimum value the error can be after being multiplied by the ICI
	 */
	private PamSpinner<Double> minErrorSpinner;

	/**
	 * The error unit label - shows units e.g. degrees, dB etc. 
	 */
	private Label errorUnitLabel;

	/**
	 * The min error unit label - shows units e.g. degrees, dB etc. 
	 */
	private Label minErrorUnitLabel;

	/**
	 * Reference to the chi2 variable. 
	 */
	private SimpleChi2VarParams simpleChi2Var;

		/**
	 * The results convert converts between human readable and internal units (e.g degrees and radians )
	 */
	private ResultConverter resultConverter = new ResultConverter();



	public AdvMHTVarPane(SimpleChi2VarParams simpleChi2Var2) {
		super(null);
		this.simpleChi2Var=simpleChi2Var2; 
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(createAdvPane());
		mainPane.setPadding(new Insets(5,5,5,5));
	}
	
	public AdvMHTVarPane(SimpleChi2VarParams simpleChi2Var2, ResultConverter resultConverter) {
		super(null);
		this.simpleChi2Var=simpleChi2Var2; 
		this.resultConverter=resultConverter; 
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(createAdvPane());
		mainPane.setPadding(new Insets(5,5,5,5));
	}


	/**
	 * Create the pane. 
	 * @return the settings pane
	 * 
	 */
	protected Pane createAdvPane() {
		
		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		int gridY=0; 
		
		Label erroCoeffLbl = new Label("Varience coeff"); 
		gridPane.add(erroCoeffLbl, 0, gridY); 
		errorSpinner = new PamSpinner<Double>(0,Double.MAX_VALUE,3,1); 
		errorSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		errorSpinner.setPrefWidth(90);
		errorSpinner.valueProperty().addListener((obs,oldVal,newVal)->{
			notifySettingsListeners();
		});
		gridPane.add(errorSpinner, 1, gridY); 
		gridPane.add(errorUnitLabel = new Label(""), 2, gridY); 
		errorSpinner.setEditable(true);
		
		errorSpinner.getValueFactory().setConverter(PamSpinner.createStringConverter(7)); 

		
		//create tooltip
		Tooltip errorCoeffTip = new Tooltip("The varience coefficient divides the difference in value between \n"
											+"subsequent detections. It therefore essentially applies a weight\n"
											+ "to the measurement for the click trian detector. Higher values \n "
											+ "mean less weight and so the measurment is less important in \n "
											+ "determining whether a set of detection is a train."); 
		erroCoeffLbl.setTooltip(errorCoeffTip);
		errorSpinner.setTooltip(errorCoeffTip);

		gridY++;
		
		Label minErrorLabel = new Label("Min. Error"); 
		gridPane.add(minErrorLabel, 0, gridY); 
		minErrorSpinner = new PamSpinner<Double>(0,Double.MAX_VALUE,0.0005,0.0005); 
		minErrorSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minErrorSpinner.setPrefWidth(90);
		minErrorSpinner.setEditable(true);
		minErrorSpinner.valueProperty().addListener((obs,oldVal,newVal)->{
			notifySettingsListeners();
		});
		//important as spinners don;t seem to handle small numbers very well. 
		minErrorSpinner.getValueFactory().setConverter(PamSpinner.createStringConverter(7)); 
		
		gridPane.add(minErrorSpinner, 1, gridY); 
		gridPane.add(minErrorUnitLabel = new Label(""), 2, gridY); 
		
		//default button to set min error
//		PamButton defaultMinError = new PamButton("",PamGlyphDude.createPamGlyph(MaterialIcon.REFRESH, PamGuiManagerFX.iconSize-2));
		PamButton defaultMinError = new PamButton("",PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-2));
		defaultMinError.prefHeightProperty().bind(minErrorSpinner.heightProperty());
		defaultMinError.setTooltip(new Tooltip("Set default minimum value for input varience"));
		gridPane.add(defaultMinError, 3, gridY); 
		defaultMinError.setOnAction((action)->{
			//set default as a fraction of varience
			minErrorSpinner.getValueFactory().setValue(errorSpinner.getValue()/ERROR_DIVISOR);
		});

		
		Tooltip minErrorTooltip = new Tooltip("The varience coefficient is multiplied by the current inter detection \n "
											+ "interval (IDI). If the interval is very small, then the error can become \n "
											+ "tiny. It is then squared creating an even smaller number and thus applying \n"
											+ "a large penalty to a potential click train set. Therefore, a minimum error \n "
											+ "is required to ensure small IDI values in a click train do not unfairly\n "
											+ "penalise it."); 
		minErrorLabel.setTooltip(minErrorTooltip);
		minErrorSpinner.setTooltip(minErrorTooltip);

		gridY++;
		
		return gridPane; 
	}

	@Override
	public SimpleChi2VarParams getParams(SimpleChi2VarParams currParams) {	
		
		//check if the limits
		SimpleChi2VarParams newParams = currParams.clone();
		
		//the new errors
		newParams.error=resultConverter.convert2Value(errorSpinner.getValue());
		newParams.minError=resultConverter.convert2Value(minErrorSpinner.getValue());
		
//		//now check the min. and max. value for the slider in case setting errors has gone under or over the limits
//		if (newParams.error>newParams.errLimits[1] || newParams.error<=newParams.errLimits[0]) {
//			//what to do if the error is zero? 
//			//System.out.println("The error value is: "  + newParams.error);
//			if (newParams.error==0) {
//				newParams.errLimits[0]=0; 
//				newParams.errLimits[1]=Math.max(1., newParams.errLimits[1]/2); 
//			}
//			else {
//				newParams.errLimits[0]=Math.max(0, newParams.error -  newParams.error*2); 
//				newParams.errLimits[1]=newParams.error + newParams.error*2; 
//			}
//		}
		return newParams;
	}

	@Override
	public void setParams(SimpleChi2VarParams input) {
		
		errorSpinner.getValueFactory().setValue(resultConverter.convert2Control(input.error));
		
		double step=(resultConverter.convert2Control(input.errLimits[1])-
				resultConverter.convert2Control(input.errLimits[0]))/(100); 
		
		((DoubleSpinnerValueFactory) errorSpinner.getValueFactory()).setAmountToStepBy(step);
		
		//System.out.println("Min. Error: for " + simpleChi2Var.getName() + "  " + " input.minError "+ input.minError + " control: " + simpleChi2Var.getResultConverter().convert2Control(input.minError));
		
		minErrorSpinner.getValueFactory().setValue(resultConverter.convert2Control(input.minError));
		
		step=step/10.;
		((DoubleSpinnerValueFactory) minErrorSpinner.getValueFactory()).setAmountToStepBy(step);
		
		errorUnitLabel.setText(input.getUnits());
		minErrorUnitLabel.setText(input.getUnits());
	}

	@Override
	public String getName() {
		return "Adv MHT Variable Pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Get the result converter. This is used to convert between stored units and
	 * displayed units in controls. e.g. degrees and radians.
	 * 
	 * @return the result converter
	 */
	public ResultConverter getResultConverter() {
		return resultConverter;
	}

}
