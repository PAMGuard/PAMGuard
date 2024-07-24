package rawDeepLearningClassifier.dataSelector;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import rawDeepLearningClassifier.dlClassification.DLClassName;

/**
 *  Settings pane for filtering deep learning results by class prediciton. 
 */
public class DLPredictonPane extends DynamicSettingsPane<DLPredictionFilterParams>{

	private DLPredictionFilter predicitonFilter;

	private PamBorderPane mainPane;

	private PamVBox contentPane;

	private ClassDataSelector[] classPanes;

	private ToggleButton toggelButton;

	public DLPredictonPane(DLPredictionFilter predicitonFilter) {
		super(null);
		this.predicitonFilter=predicitonFilter;
		createPane();
	}


	private void createPane() {
		mainPane = new PamBorderPane();

		PamBorderPane topPane = new PamBorderPane(); 
		
		Label label = new Label("Show classes above min. prediction");
		PamBorderPane.setAlignment(label, Pos.BOTTOM_LEFT);
//		label.setTextAlignment(TextAlignment.LEFT);
		label.setAlignment(Pos.BOTTOM_LEFT);
		
		topPane.setLeft(label);
		
		toggelButton = new ToggleButton(); 
		toggelButton.setTooltip(new Tooltip("Lock sliders together so one slider changes all values"));
		
		toggelButton.setGraphic(PamGlyphDude.createPamIcon("mdi2l-lock-open", PamGuiManagerFX.iconSize));
		toggelButton.selectedProperty().addListener((onsVal, oldVal,newVal)->{
			if (newVal) {
				toggelButton.setGraphic(PamGlyphDude.createPamIcon("mdi2l-lock", PamGuiManagerFX.iconSize));
			}
			else {
				toggelButton.setGraphic(PamGlyphDude.createPamIcon("mdi2l-lock-open", PamGuiManagerFX.iconSize));
			}
		});
		toggelButton.setSelected(false);
		
		topPane.setRight(toggelButton);
//		topPane.setAlignment(Pos.CENTER_RIGHT);

		contentPane = new 	PamVBox(); 
		contentPane.setSpacing(5);
		
		mainPane.setTop(topPane);
		mainPane.setCenter(contentPane);
		PamBorderPane.setMargin(topPane, new Insets(0,0,5.,0));
	}

	class ClassDataSelector extends PamHBox {

		Slider slider;

		CheckBox enable;
		
		Label valueLabel;

		private boolean enableListener = true;

		ClassDataSelector(String classType, int index) {

			enable = new CheckBox(classType);
			enable.setPrefWidth(80);
			enable.setTooltip(new Tooltip(classType));
			enable.setOnAction((a)->{
				//disable if the class is unticked. 
				slider.setDisable(!enable.isSelected());
				valueLabel.setDisable(!enable.isSelected());
				notifySettingsListeners();
			});
			
			slider = new Slider();
			slider.setShowTickMarks(true);
			slider.setMinorTickCount(10);
			slider.setShowTickLabels(false);
			slider.setMajorTickUnit(0.5);

			slider.valueProperty().addListener((obsval, oldval, newval)->{
				valueLabel.setText(String.format("%.2f", newval)); 
				
				//if the lock button has been sleected then change all the sliders
				//so that they are the same value as this slide (unless the class is disabled)
				if (toggelButton.isSelected() && enableListener) {
					for (int i=0; i<classPanes.length; i++) {
						if (classPanes[i].enable.isSelected() && i!=index) {
							
							classPanes[i].enableListener = false; //prevent needless calls to notify settings
							classPanes[i].slider.setValue(newval.doubleValue());
							classPanes[i].enableListener = true;
						}
					};
				}
				notifySettingsListeners();
			});
			
			
			slider.setMin(0.);
			slider.setMax(1.);
			
			PamHBox.setHgrow(slider, Priority.ALWAYS); 
			
			valueLabel = new Label();
			valueLabel.setMinWidth(30);
			
			valueLabel.setText(String.format("%.2f", slider.getValue())); 

			this.getChildren().addAll(enable, slider, valueLabel);
		}
	}


	@Override
	public DLPredictionFilterParams getParams(DLPredictionFilterParams currParams) {
		
		if (classPanes==null) return currParams;
		for (int i=0; i<classPanes.length ; i++) {
			currParams.classSelect[i] = classPanes[i].enable.isSelected();
			currParams.minClassPredicton[i] = classPanes[i].slider.getValue();
		}
		
		return currParams;
	}

	@Override
	public void setParams(DLPredictionFilterParams input) {
		//set the parameters. Note that class numbers should have been checked already. 
		setClassPane(input); 

	}

	private void setClassPane(DLPredictionFilterParams input) {
		DLClassName[] classNames = predicitonFilter.getDLControl().getDLModel().getClassNames(); 
		contentPane.getChildren().clear();
		
		classPanes = new ClassDataSelector[input.classSelect.length];
		ClassDataSelector classPane;
		for (int i=0; i<input.classSelect.length ; i++) {
			classPane = new ClassDataSelector(classNames[i].className, i);
			classPanes[i] = classPane;
			contentPane.getChildren().add(classPane);
			classPane.enable.setSelected(input.classSelect[i]);
			
			classPane.slider.setValue(input.minClassPredicton[i]);
			if (i==input.classSelect.length-1) {
				classPane.slider.setShowTickLabels(true);
			}
		}
	}



	//	/**
	//	 * Check the class numbers are correct
	//	 * @param input
	//	 */
	//	private void checkSpeciesClassNumbers(DLPredictionFilterParams input) {
	//		this.predicitonFilter.getSpeciesClassList(); 
	//		
	//	}


	@Override
	public String getName() {
		return "Deep learning prediciton filter";
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
