package clickTrainDetector.layout.mht;

import PamController.SettingsPane;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.mht.DefaultMHTParams;
import clickTrainDetector.clickTrainAlgorithms.mht.DefaultMHTParams.DefaultMHTSpecies;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTParams;
import clickTrainDetector.layout.ClickTrainAlgorithmPaneFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;

/**
 * The main settings pane for changing settings of the MHT algorithm. 
 * <p>
 * The pane contains both the standard parameters for chi^2 calculations
 * and a set of sliders which allow the user to change if and how much
 * different variables change click train detection, e.g. bearing, amplitude, 
 * correlation. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("unchecked")
public class MHTSettingsPane extends SettingsPane<MHTParams> {

	/**
	 * Main holder pane.
	 */
	private PamBorderPane mainPane;

	/**
	 * The number of clicks to prune-back.
	 */
	private PamSpinner<Integer> pruneBackSpinner; 

	/**
	 * The number of coasts at any one time. 
	 */
	private PamSpinner<Integer> nCoastsSpinner; 

	/**
	 * The maximum number of allowed trains at any one time.
	 */
	private PamSpinner<Integer> nHoldSpinner; 
	
	/**
	 * The number of detection before prune back begins. 
	 */
	private PamSpinner<Integer> pruneStartSpinner; 

	/**
	 * Reference to the MHT Click train algorithm. 
	 */
	private MHTClickTrainAlgorithm mhtClickTrainAlgorithm;

	/**
	 * The MHT Chi2 pane
	 */
	@SuppressWarnings("rawtypes")
	private SettingsPane mhtChi2Pane;



	/**
	 * Create the MHT Settings Pane
	 * @param mhtClickTrainAlgorithm
	 * @param clickTrainPane
	 */
	MHTSettingsPane(MHTClickTrainAlgorithm mhtClickTrainAlgorithm) {
		super(null);
		this.mhtClickTrainAlgorithm=mhtClickTrainAlgorithm; 
		mainPane= new PamBorderPane();
		mainPane.setCenter(createPane()); 
		
		mainPane.setBottom(createDefaultSpeciesPane());
		
//		//TEMP
//		PamButton button = new PamButton("TestMe"); 
//		button.setOnAction((action)->{
//			mhtClickTrainAlgorithm.testAlgorithm();
//		});
//		mainPane.setBottom(button);
	}

	/**
	 * Create the chi^2 settings pane. If a new chi^2 algorithm is used then this function will 
	 * need to change to set new settings pane. 
	 * @return the chi2 settings pane. 
	 */
	public SettingsPane<? extends MHTChi2Params> createMHTChi2Pane() {
		return this.mhtClickTrainAlgorithm.getChi2ProviderManager().createMHTChi2Pane(); 
	}


	/**
	 * Create the main pane for settings
	 * @return the main settings pane. 
	 */
	private Pane createPane() {
		PamVBox mainPane = new PamVBox(); 
		Label label = new Label("MHT Settings");
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(label);

		mainPane.getChildren().add(createGeneralSettingsPane());
		mainPane.getChildren().add(createMHTVarPane());

		return mainPane; 
	}

	/**
	 * Create pane with controls for changing general settings of the MHT Kernel.  
	 * @return MHT Kernel settings pane. 
	 */
	private Pane createGeneralSettingsPane() {

		PamVBox pamVBox = new PamVBox(); 
		pamVBox.setSpacing(5);

		Label label = new Label("MHT Kernal Settings"); 
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(label);

		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		int gridY=0; 
		
		gridPane.add(new Label("Prune-back"), 0, gridY); 
		pruneBackSpinner = new PamSpinner<Integer>(0,10,3,1); 
		pruneBackSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		pruneBackSpinner.setPrefWidth(70);
		pruneBackSpinner.setEditable(true);
		pruneBackSpinner.setTooltip(new Tooltip("The number of detections to keep possibilities for. The click train detector prunes\n"
												+ "bad tracks for every input detection."));
		gridPane.add(pruneBackSpinner, 1, gridY); 
		gridY++;
		
		
		gridPane.add(new Label("Prune-start"), 0, gridY); 
		pruneStartSpinner = new PamSpinner<Integer>(0,30,3,1); 
		pruneStartSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		pruneStartSpinner.setPrefWidth(70);
		pruneStartSpinner.setEditable(true);
		pruneBackSpinner.setTooltip(new Tooltip("The minimum number of detections before pruning starts."));
		gridPane.add(pruneStartSpinner, 1, gridY); 
		gridY=0;

		gridPane.add(new Label("  Max no. coasts"), 2, gridY); 
		nCoastsSpinner = new PamSpinner<Integer>(0,Integer.MAX_VALUE,3,1); 
		nCoastsSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		nCoastsSpinner.setPrefWidth(70);
		nCoastsSpinner.setEditable(true);
		nCoastsSpinner.setTooltip(new Tooltip("The maximum number of missing detections before a track is closed"));

		gridPane.add(nCoastsSpinner, 3, gridY); 
		gridY++; 

		gridPane.add(new Label("  Max no. trains"), 2, gridY); 
		nHoldSpinner = new PamSpinner<Integer>(0,1000,3,1); 
		nHoldSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		nHoldSpinner.setPrefWidth(70);
		nHoldSpinner.setEditable(true);
		nHoldSpinner.setTooltip(new Tooltip("The maximum number of unique click trains that can be tracked at the same time."));
		gridPane.add(nHoldSpinner, 3, gridY); 

		pamVBox.getChildren().addAll(label, gridPane); 

		pamVBox.setPadding(new Insets(10,0,0,0));

		return pamVBox; 
	}


	/**
	 * Create pane with controls to change individual chi^2 variables. 
	 * @return the chi^2 variable pane. 
	 */
	private Pane createMHTVarPane() {

		PamVBox pamVBox = new PamVBox(); 
		pamVBox.setSpacing(5);

		Label label = new Label("X\u00b2 Calculation Settings"); 
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(label);
		pamVBox.getChildren().add(label); 

		//create chi^2 pane
		this.mhtChi2Pane = createMHTChi2Pane();
		pamVBox.getChildren().add(this.mhtChi2Pane.getContentNode()); 

		pamVBox.setPadding(new Insets(10,0,0,0));

		return pamVBox;
	}
	
	/**
	 * Create pane to allow selection of default species. 
	 * @return pane for selecting default species
	 */
	private Pane createDefaultSpeciesPane() {
		
		MenuButton speciesChoiceBox = new MenuButton("Select Species"); 
		
		MenuItem menuItem;
		for (DefaultMHTSpecies speciesTypes: DefaultMHTSpecies.values()) {
			menuItem = new MenuItem(DefaultMHTParams.getDefaultSpeciesName(speciesTypes)); 
			menuItem.setOnAction((action)->{
				setParams(DefaultMHTParams.getDefaultMHTParams(speciesTypes, mhtClickTrainAlgorithm.getClickTrainControl().getParentDataBlock())); 
			});
			speciesChoiceBox.getItems().add(menuItem); 
		}
		
		PamHBox pamHBox = new PamHBox(); 
		pamHBox.setSpacing(5);
		pamHBox.setAlignment(Pos.CENTER_RIGHT);
		
		pamHBox.getChildren().addAll(new Label("Optimise for Species"), speciesChoiceBox); 
		
		pamHBox.setPadding(new Insets(5,0,0,0));
		
		return pamHBox; 
		
	}


	@Override
	public MHTParams getParams(MHTParams currParams) {
		try {
			MHTParams currParams2 = currParams.clone(); 

			//MHT kernel settings. 
			currParams2.mhtKernal.nPruneback=pruneBackSpinner.getValue(); 
			currParams2.mhtKernal.maxCoast=nCoastsSpinner.getValue(); 
			currParams2.mhtKernal.nHold=nHoldSpinner.getValue(); 
			currParams2.mhtKernal.nPruneBackStart=this.pruneStartSpinner.getValue(); 

			//Chi2 settings. 
			//can;t get this work with bounded generic parameter- why?
			currParams2.chi2Params = (MHTChi2Params) mhtChi2Pane.getParams(currParams.chi2Params); 
			
			return currParams2;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}

	}


	@Override
	public void setParams(MHTParams currParams) {

		//MHT Kernel
		pruneBackSpinner.getValueFactory().setValue(currParams.mhtKernal.nPruneback);
		nCoastsSpinner.getValueFactory().setValue(currParams.mhtKernal.maxCoast);
		nHoldSpinner.getValueFactory().setValue(currParams.mhtKernal.nHold);
		pruneStartSpinner.getValueFactory().setValue(currParams.mhtKernal.nPruneBackStart); 

		//MHT set params 
		mhtChi2Pane.setParams(currParams.chi2Params);

	}

	@Override
	public String getName() {
		return "MHT CLick Train Detector";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
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
			this.mhtChi2Pane.notifyChange(ClickTrainControl.NEW_PARENT_DATABLOCK, data);
			//notify model changed.
			
			break; 
		}
	}



}
