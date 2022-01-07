package IshmaelDetector.layoutFX;

import IshmaelDetector.IshDetParams;
import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

/**
 * Pane for peak picking parameters
 * 
 * @author Jamie Macaulay
 *
 */
public class PeakPickingPane extends SettingsPane<IshDetParams> {
	
	/**
	 * The main pane. 
	 */
	private Pane mainPane;

	/**
	 * Spinner for the peak threshold value. 
	 */
	private PamSpinner<Double> threshold;

	/**
	 * 	Spinner for the minimum time over threshold before a detection is recorded
	 */
	private PamSpinner<Double> minTime;

	/**
	 * 	Spinner for the minimum time
	 */
	private PamSpinner<Double> maxTime;

	/**
	 * 	Spinner for the minimum inter detection interval (IDI) between concurrent detections. 
	 */
	private PamSpinner<Double> minIDI; 

	public PeakPickingPane() {
		super(null);
		// TODO Auto-generated constructor stub
		mainPane= createPeakPickingPane();
	}
	
	/**
	 * The peak picking pane. 
	 */
	private Pane createPeakPickingPane() {
		
		PamVBox mainPane = new PamVBox(); 
		mainPane.setSpacing(5);
		
		Label titleLabel = new Label("Peak Detection"); 
		PamGuiManagerFX.titleFont2style(titleLabel);
		//titleLabel.setFont(PamGuiManagerFX.titleFontSize2);

		
		PamGridPane gridHolder = new PamGridPane(); 
		gridHolder.setHgap(5);
		gridHolder.setVgap(5);
		
		int row=0; 
		
		gridHolder.add(new Label("Threshold"), 0, 0);
		gridHolder.add(threshold = new PamSpinner<Double>(0, Double.MAX_VALUE, 0.05, 0.01), 1, 0);
		threshold.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		threshold.getValueFactory().setConverter(PamSpinner.createStringConverter(8));
		threshold.setEditable(true);

		row++;
		gridHolder.add(new Label("Min time over threshold"), 0, row);
		gridHolder.add(minTime = new PamSpinner<Double>(0, Double.MAX_VALUE, 0.4, 0.1), 1, row);
		minTime.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minTime.getValueFactory().setConverter(PamSpinner.createStringConverter(4));
		gridHolder.add(new Label("s"), 2, row);
		minTime.setEditable(true);
		minTime.valueProperty().addListener((obsVal, oldVal, newVal)->{
			//System.out.println("New value: " + maxTime.getValue() + "   " + newVal.doubleValue());
//			if (maxTime.getValue()!=0 && maxTime.getValue()<newVal.doubleValue()) {
			if (maxTime.getValue()<newVal.doubleValue()) {
				maxTime.getValueFactory().setValue(newVal.doubleValue());
				//System.out.println("MinTime: New value: " + maxTime.getValue() + "   " + newVal.doubleValue());
			}
		});

		row++;
		gridHolder.add(new Label("Max time over threshold"), 0, row);
		gridHolder.add(maxTime = new PamSpinner<Double>(0, Double.MAX_VALUE, 0, 0.1), 1, row);
		maxTime.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxTime.getValueFactory().setConverter(PamSpinner.createStringConverter(4));
		gridHolder.add(new Label("s"), 2, row);
		maxTime.setEditable(true);
		maxTime.valueProperty().addListener((obsVal, oldVal, newVal)->{
			if (newVal.doubleValue()<minTime.getValue()) {
				if (newVal.doubleValue()>oldVal.doubleValue()) maxTime.getValueFactory().setValue(minTime.getValue()+0.01);
				//set back to zero. 
				//if (newVal.doubleValue()<oldVal.doubleValue()) maxTime.getValueFactory().setValue(0.0);
			}
		});

		row++;
		gridHolder.add(new Label("Min IDI"), 0, row);
		gridHolder.add(minIDI = new PamSpinner<Double>(0, Double.MAX_VALUE, 0.4, 0.1), 1, row);
		minIDI.getValueFactory().setConverter(PamSpinner.createStringConverter(4));
		minIDI.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		gridHolder.add(new Label("s"), 2, row);
		minIDI.setEditable(true);

		mainPane.getChildren().addAll(titleLabel, gridHolder); 
		
		return mainPane; 
	}


	@Override
	public IshDetParams getParams(IshDetParams currParams) {
		currParams.thresh=threshold.getValue();
		currParams.minTime=minTime.getValue();
		currParams.maxTime=maxTime.getValue();
		currParams.refractoryTime=minIDI.getValue();

		return currParams;
	}

	@Override
	public void setParams(IshDetParams input) {
		//System.out.println("IshDetParams: " + input.minTime +  "  " + input.maxTime); 
		threshold.getValueFactory().setValue(input.thresh);
		minTime.getValueFactory().setValue(input.minTime);
		maxTime.getValueFactory().setValue(input.maxTime);
		minIDI.getValueFactory().setValue(input.refractoryTime); 	
	}

	@Override
	public String getName() {
		return "Ish Peak Picking";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
