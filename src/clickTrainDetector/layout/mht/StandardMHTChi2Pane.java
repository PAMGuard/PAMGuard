package clickTrainDetector.layout.mht;


import java.util.ArrayList;

import org.controlsfx.control.PopOver;

import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.MHTChi2Var;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * 
 * The settings pane for the chi2 value. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardMHTChi2Pane extends SettingsPane<StandardMHTChi2Params> {
	
	/**
	 * The main pane
	 */
	private PamBorderPane mainPane;
	
	/**
	 * Chi^2 holder. 
	 */
	private PamGridPane chi2Holder;

	/**
	 * Max ICI control. 
	 */
	private PamSpinner<Double> maxICISpinner;

	/**
	 * Check box to use correlation. 
	 */
	private PamToggleSwitch useCorrelation;

	/**
	 * The check boxes
	 */
	private PamToggleSwitch[] checkBoxes;

	/**
	 * List of settings panes for the chi^2 variables. 
	 */
	private MHTVarSettingsPane[] chi2SettingsPanes;

	/**
	 * Check box to set whether electrical noise filter is used
	 */
	private CheckBox electricalNoiseFilter;

	/**
	 * The advanced electrical noise button, shows pop over.
	 */
	private PamButton advElecNoiseButton;
	
	/**
	 * Advacned settings button.
	 */
	private PamButton advSettingsButton;

	/**
	 * Pop over which holds the advanced electrical noise pane.
	 */
	private PopOver popOver;
	
	/**
	 * Pop over which holds the advanced settings pane.
	 */
	private PopOver popOver2;


	/**
	 * Advanced settings pane for the electrical noise filter. 
	 */
	private AdvElectricalNoisePane advElecNoisePane;

	/**
	 * Reference to the current parameters. 
	 */
	private StandardMHTChi2Params currParams;

	/**
	 * Reference to the MHTClickTrain Algortithm
	 */
	private MHTClickTrainAlgorithm mhtClickTrainAlgorithm;

	/**
	 * A set of MHTVars for reference. Note that these are NEVER used other than 
	 * to access functions. Chi2Vars are instantiated for each possible train. These are
	 * just used to grab a =settings pane and checj whether certain  data types are allowed. 
	 * A bit of a hack but works well. 
	 */
	private ArrayList<MHTChi2Var<PamDataUnit>> chi2Vars;

	/**
	 * The advanced settings pane. 
	 */
	private StandardMHTChi2AdvPane advSettingsPane;




	public StandardMHTChi2Pane(MHTClickTrainAlgorithm mhtClickTrainAlgorithm) {
		super(null);
		this.mhtClickTrainAlgorithm=mhtClickTrainAlgorithm; 
		mainPane = new PamBorderPane();
		mainPane.setCenter(createPane());
	}
	
	
	/**
	 * Create the pane
	 */
	private Pane createPane() {
		
		//ICI general settings
		Label iciLabel = new Label("ICI Settings");
		iciLabel.setFont(Font.font(null,FontWeight.BOLD, 11));
		
		maxICISpinner = new PamSpinner<Double>(0,Double.MAX_VALUE,0.1,0.01);
		maxICISpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL); 
		maxICISpinner.setEditable(true);
		maxICISpinner.setPrefWidth(80);

		PamHBox iciSpinnerBox = new PamHBox();
		iciSpinnerBox.setAlignment(Pos.CENTER_LEFT);
		iciSpinnerBox.setSpacing(5);
		iciSpinnerBox.getChildren().addAll(new Label("Max. ICI"), maxICISpinner, new Label("s")); 

		//correlation check box. 
		useCorrelation = new PamToggleSwitch("Use cross correlation");
		useCorrelation.setTooltip(new Tooltip("The inter detection interval is calcualted via cross correaltion\n"
											+ "which is more accurate but signifcantly more processor intensive"));

		//Chi2 variables.variables 
		Label chi2Label = new Label("X\u00b2 Variables");
		chi2Label.setFont(Font.font(null,FontWeight.BOLD, 11));
		
		chi2Holder = new PamGridPane();
		chi2Holder.setVgap(5);
		chi2Holder.setHgap(5);
		chi2Holder.setAlignment(Pos.TOP_LEFT);
		chi2Holder.setAlignment(Pos.CENTER_LEFT);

		chi2Holder.prefWidthProperty().bind(mainPane.widthProperty());
		
		//label for the electrical noise filter.
		Label electricalNoiseLabel = new Label("X\u00b2 Electrical Noise Filter");
		electricalNoiseLabel.setFont(Font.font(null,FontWeight.BOLD, 11));

		//the main holder. 
		PamVBox pamVBox = new PamVBox();
		pamVBox.setSpacing(5); 
	
		pamVBox.getChildren().addAll(iciLabel, iciSpinnerBox, useCorrelation,
				chi2Label, chi2Holder, createAdvSettingsPane(), electricalNoiseLabel, 
				createElectricalNoisePane());	
		
		//populate the chi2 pane. 
		populateChi2Pane();

		return pamVBox; 
	}
	
	
	/**
	 * Create the advanced settings pane.
	 * @return the advanced settings pane. 
	 */
	private Pane createAdvSettingsPane() {

		advSettingsPane = new StandardMHTChi2AdvPane();
		advSettingsPane.setPadding(new Insets(5,5,5,5));
		
		advSettingsButton = new PamButton(); 
//		advSettingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		advSettingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		advSettingsButton.setOnAction((action)->{
			showAdvSettingsPane();
		});

		//label for the electrical noise filter.
		Label advSettingsLabel = new Label("Advanced X\u00b2 Settings");
		advSettingsLabel.setFont(Font.font(null,FontWeight.BOLD, 11));

		PamBorderPane borderPane = new PamBorderPane(); 
		borderPane.setLeft(advSettingsLabel); 
		borderPane.setRight(advSettingsButton); 
		BorderPane.setAlignment(advSettingsLabel, Pos.CENTER_LEFT);
		
		return borderPane;
	}

	
	/**
	 * Create the electrical noise filter pane.
	 * @return the electrical noise filter pane. 
	 */
	private Pane createElectricalNoisePane() {
		
		PamHBox pamHBox = new PamHBox(); 
		pamHBox.setAlignment(Pos.CENTER_RIGHT);
		pamHBox.setSpacing(5);
		pamHBox.setMaxWidth(Double.MAX_VALUE);
		
		//pamHBox.minWidthProperty().bind(mainPane.widthProperty()); //keep same size as pane. 
		electricalNoiseFilter = new CheckBox("Run electric noise fitler");
		electricalNoiseFilter.setOnAction((action)->{
			advElecNoiseButton.setDisable(!electricalNoiseFilter.isSelected());
		});
		
		advElecNoiseButton = new PamButton(); 
//		advElecNoiseButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		advElecNoiseButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		advElecNoiseButton.setOnAction((action)->{
			showAdvElectricalPane();
		});
		HBox.setHgrow(advElecNoiseButton, Priority.ALWAYS);

		
		advElecNoisePane = new AdvElectricalNoisePane(); 
		((Region) advElecNoisePane.getContentNode()).setPadding(new Insets(5,5,5,5)); 
		
		String tooltipText = 
					"Run the electric noise filter. This looks for click trains which have an\n"
				+ 	"almost constant MHT X\u00b2 variable, e.g. bearing. Nature does not do\n"
				+ 	"straight lines so these are classified as noise."; 
				
		electricalNoiseFilter.setTooltip(new Tooltip(tooltipText) );
		advElecNoiseButton.setTooltip(new Tooltip("Advanced electrical noise filter settings.\n " + tooltipText) );
		
		pamHBox.getChildren().addAll(electricalNoiseFilter, advElecNoiseButton); 
		
		return pamHBox; 
	}
	
	/**
	 * Creates pane allowing the user to change fine scale things such as error limits. 
	 * @return the pop over pane. 
	 */
	public void showAdvSettingsPane() {

		if (popOver2==null) {
			popOver2 = new PopOver(); 
			popOver2.setContentNode(advSettingsPane);
		}

		popOver2.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				//System.out.println("Pop over shown: " + currentInput.minError);
				//update any info into params before showing adv dialog. 
				//currParams=getParams(currParams);
				advSettingsPane.setParams(currParams);
			}
			else {
				currParams = advSettingsPane.getParams(currParams);
			}
		});

		//show pop up menu. 
		popOver2.show(advSettingsButton);
	} 
	
	
	/**
	 * Creates pane allowing the user to change fine scale things such as error limits. 
	 * @return the pop over pane. 
	 */
	public void showAdvElectricalPane() {

		if (popOver==null) {
			popOver = new PopOver(); 
			popOver.setContentNode(advElecNoisePane.getContentNode());
		}

		popOver.showingProperty().addListener((obs, old, newval)->{
			if (newval) {
				//System.out.println("Pop over shown: " + currentInput.minError);
				//update any info into params before showing adv dialog. 
				//currParams=getParams(currParams);
				advElecNoisePane.setParams(currParams.electricalNoiseParams);
			}
			else {
				currParams.electricalNoiseParams = advElecNoisePane.getParams(currParams.electricalNoiseParams);

			}
		});

		//show pop up menu. 
		popOver.show(advElecNoiseButton);
	} 
	
	/***
	 * Populate the chi2 pane with specific settings pane for different chi^2 variables. 
	 */
	private void populateChi2Pane() {
			
		chi2Holder.getChildren().clear();
		
		//HACK: must instantiate chi2vars to get the settings panes...bit of a pain actually. 
		chi2Vars= StandardMHTChi2.createChi2Vars(); 
		
		checkBoxes = new PamToggleSwitch[chi2Vars.size()]; 
		chi2SettingsPanes = new MHTVarSettingsPane[chi2Vars.size()]; 

		Node chi2pane; 
		for (int i=0; i<checkBoxes.length ; i++) {
			chi2Holder.add(checkBoxes[i]=new PamToggleSwitch(""), 0, i);
			chi2Holder.add(new Label(chi2Vars.get(i).getName()), 1, i);
			
			//add to an array of settings panes. 
			chi2SettingsPanes[i]=chi2Vars.get(i).getSettingsPane(); 
			//if there is actually a settings pane then add. 
			if (chi2Vars.get(i).getSettingsPane()!=null) {
				chi2Holder.add(chi2pane=chi2Vars.get(i).
						getSettingsPane().getContentNode(),2,i);
				
				PamGridPane.setHgrow(chi2pane, Priority.ALWAYS);
			}
		}
	}
	

	@Override
	public StandardMHTChi2Params getParams(StandardMHTChi2Params currParams) {
		try {
			StandardMHTChi2Params currParams2 = currParams.clone(); 

			currParams2.maxICI=maxICISpinner.getValue();
			currParams2.useCorrelation=useCorrelation.selectedProperty().get(); 
			
			for (int i=0; i<currParams2.chi2Settings.length; i++) {
				//get chi^2 var settings object. 
				currParams2.enable[i]=this.checkBoxes[i].isSelected(); 
				currParams2.chi2Settings[i]=chi2SettingsPanes[i].getParams(currParams.chi2Settings[i]); 
			}
			
			//electrical noise filter. 
			currParams2.electricalNoiseParams= this.advElecNoisePane.getParams(currParams.electricalNoiseParams);
			currParams2.useElectricNoiseFilter = this.electricalNoiseFilter.isSelected();
			
			//get advanced settings. 
			currParams2=advSettingsPane.getParams(currParams2); 

//			//make sure to call into all chi2 vars and get settings object. 
			currParams2.printSettings();
			
			return currParams2;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setParams(StandardMHTChi2Params currParams) {
		this.currParams = currParams; 
		//System.out.println("StandardMHTChi2Params: Set Params: " + currParams.hashCode());
		//set basic params. 
		maxICISpinner.getValueFactory().setValue(currParams.maxICI);
		useCorrelation.setSelected(currParams.useCorrelation);
		
		//get chi^2 var settings object. 
		for (int i=0; i<currParams.chi2Settings.length; i++) {
			checkBoxes[i].setSelected(currParams.enable[i]);
			//get chi^2 var settings object. 
			if (chi2SettingsPanes[i]!=null) {
				chi2SettingsPanes[i].setParams(currParams.chi2Settings[i]); 
				chi2SettingsPanes[i].setMhtAlgorithm(this.mhtClickTrainAlgorithm);
			}
		}
		
		//check data blocks too. 
		enableChi2Panes(this.mhtClickTrainAlgorithm.getClickTrainControl().getParentDataBlock()); 
		
		//set advanced settings. 
		advSettingsPane.setParams(currParams); 

		//electrical noise filter. 
		electricalNoiseFilter.setSelected(currParams.useElectricNoiseFilter);
		advElecNoisePane.setParams(currParams.electricalNoiseParams);
		advElecNoiseButton.setDisable(!electricalNoiseFilter.isSelected());
		
	}

	@Override
	public String getName() {
		return "Click Train X^2 Pane";
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
	 * Enable or disbale variables based ont eh data types in the datablock 
	 * @param parentDataBlock
	 */
	private void enableChi2Panes(PamDataBlock parentDataBlock) {
		boolean canUse = false; 
		for (int i=0; i<this.chi2SettingsPanes.length; i++) {
			canUse = isChi2VarAllowed(i, parentDataBlock);  
			if (!canUse )checkBoxes[i].setSelected(canUse); //only deselcts box if it cannot be used. 
			checkBoxes[i].setDisable(!canUse);
			chi2SettingsPanes[i].getContentNode().setDisable(!canUse);
		}
	}
	
	
	/**
	 * Checks whethe a variable can be used or not. Some chi2 vars can only be used if data units 
	 * are ofr a certain type. 
	 * @param mhtVarSettingsPane
	 * @return
	 */
	private boolean isChi2VarAllowed(int index, PamDataBlock parentDataBlock) {
		return chi2Vars.get(index).isDataBlockCompatible(parentDataBlock); 
	}


	@Override
	public void notifyChange(int flag, Object data) {
		switch (flag) {
		case ClickTrainControl.NEW_PARENT_DATABLOCK:
			//pass along to the MHTChi2 pane. Nothing to change here. 
			for (int i=0; i<this.chi2SettingsPanes.length; i++) {
				this.chi2SettingsPanes[i].notifyChange(ClickTrainControl.NEW_PARENT_DATABLOCK, data);
				//enable or disable depending on the data block
				enableChi2Panes((PamDataBlock) data); 
			}
			break; 
		}
	}
	
}
