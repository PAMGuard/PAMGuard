package clickDetector.layoutFX;

import Localiser.DelayMeasurementParams;
import PamController.SettingsPane;
import PamView.PamSymbol;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import clickDetector.ClickClassifiers.ClickIdentifier;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.PamTilePane;
import pamViewFX.fxNodes.PamVBox;


/**
 * Pane to chnage click delay settings. 
 * @author Jamie Macaulay
 *
 */
public class ClickDelayPane extends SettingsPane<ClickParameters> {

	/**
	 * Reference click control. 
	 */
	private ClickControl clickControl;

	/**
	 * List of measurment parametrs.
	 */
	private DelayMeasurementParams[] delayParams;

	/**
	 * Type list 
	 */
	private int[] typesList;

	/**
	 * Type settings. 
	 */
	private Label[] typeSettings;

	/**
	 * List of name of click types which can have time delay calculation changed
	 */
	private Label[] typeNames;

	/**
	 * List of button to chnage settings
	 */
	private Button[] typeChange;

	/**
	 * List of check boxes. 
	 */
	private CheckBox[] useDefault;

	/**
	 * Tile pane
	 */
	private PamTilePane holderPane;

	/**
	 * Copy of the current click parameters.
	 */
	private ClickParameters clickParameters;
	
	private PamBorderPane mainPane; 

	public ClickDelayPane(ClickControl clickControl){
		super(null);
		this.clickControl = clickControl;
		mainPane= new PamBorderPane(); 
		mainPane.setCenter(createPane());
	}

	/**
	 * Create the pane. 
	 */
	private Pane createPane(){
		holderPane = new PamTilePane();
		holderPane.setHgap(5);
		holderPane.setVgap(5);
		return holderPane;
	}

	/**
	 * Create controls to change the delay measurement settings for current click types. 
	 * @param clickParameters - the click parameters
	 */
	private void createPanelControls(ClickParameters clickParameters) {
		ClickIdentifier clickIdentifier = clickControl.getClickIdentifier();
		int nTypes = 1; // always a default. 
		int[] codeList = null;
		if (clickIdentifier != null) {
			codeList = clickIdentifier.getCodeList();
			if (codeList != null) {
				nTypes += codeList.length;
			}
		}
		// allocate space for everything we need. 
		typesList = new int[nTypes];
		typeNames = new Label[nTypes];
		useDefault = new CheckBox[nTypes];
		typeSettings = new Label[nTypes];
		typeChange = new Button[nTypes];
		delayParams = new DelayMeasurementParams[nTypes];
		delayParams[0] = clickParameters.getDelayMeasurementParams(0, true);
		for (int i = 1; i < nTypes; i++) {
			typesList[i] = codeList[i-1];
			delayParams[i] = clickParameters.getDelayMeasurementParams(typesList[i], false);
		}
		
		holderPane.getChildren().clear();

		PamVBox subPanel;
		// column headings. 

		String typeName;
		PamSymbol[] typeSymbols = null;
		if (clickIdentifier != null) {
			typeSymbols = clickIdentifier.getSymbols();
		}
		for (int i = 0; i < nTypes; i++) {
			
			// create title
			if (i == 0) {
				typeName = "Unclassified clicks / default";
			}
			else {
				typeName = clickIdentifier.getSpeciesName(typesList[i]);
			}
			
			Label typeNameTitle = new Label(typeName);
			
			//create check box and settings button

			useDefault[i] = new CheckBox("Use Default");

			final int typeIndex=i; 
			useDefault[i].setOnAction((action)->{
				defaultButtonPress(typeIndex);
			});
						
			PamHBox hbox=new PamHBox(); 
			hbox.setSpacing(5);
			if (i > 0) {
				hbox.getChildren().add(useDefault[i]);
				PamSymbolFX icon = new PamSymbolFX(typeSymbols[i-1]);
				hbox.getChildren().add(icon.getNode(PamGuiManagerFX.iconSize));
			}
			
//			typeChange[i] = new Button("",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS,Color.WHITE, PamGuiManagerFX.iconSize));
			typeChange[i] = new Button("",PamGlyphDude.createPamIcon("mdi2c-cog",Color.WHITE, PamGuiManagerFX.iconSize));
			typeChange[i].setOnAction((action)->{
				settingsButtonPress(typeIndex);
			});
			hbox.getChildren().add(typeChange[i]);
					
			//create info string;
			typeSettings[i] = new Label(" "); 
					
			//now create the Pane
			subPanel = new PamVBox();
			subPanel.setPadding(new Insets(5,5,5,5));
			subPanel.setSpacing(5);
			subPanel.getChildren().addAll(typeNameTitle, hbox, typeSettings[i]);
			
			holderPane.getChildren().add(subPanel);
		}
	}


	
	public ClickParameters getParams() {
		return getParams(clickParameters);
	}
	
	/**
	 * Copies the local list back into the click settings, removing 
	 * old entries as necessary. 
	 * @param clickParameters
	 * @return true unless there are no default params. 
	 */
	public ClickParameters getParams(ClickParameters clickParameters) {
		if (clickParameters==null) return null; 
		
		if (delayParams==null || delayParams[0] == null) {
			return clickParameters;
		}
		clickParameters.setDelayMeasurementParams(0, delayParams[0]);
		for (int i = 1; i < typesList.length; i++) {
			if (useDefault[i].isSelected()) {
				clickParameters.setDelayMeasurementParams(typesList[i], null);
			}
			else {
				clickParameters.setDelayMeasurementParams(typesList[i], delayParams[i]);
			}
		}
		return clickParameters;
	}

	@Override
	public void setParams(ClickParameters clickParameters) {
//		this.clickParameters = clickParameters.clone();
//		System.out.println("ClickDelayPane: Hello: "+clickParameters.clickClassifierType);
		createPanelControls(clickParameters);
		for (int i = 0; i < typesList.length; i++) {
			useDefault[i].setSelected(delayParams[i] == null);
		}
		saySettings(0); // will do the lot !
		enableControls();
	}

	@Override
	public String getName() {
		return "Click Delay Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
	}

	void defaultButtonPress(int typeIndex) {

		saySettings(typeIndex);
		enableControls();
	}

	private DelayMeasurementParams getParamsForIndex(int index, boolean forceDefault) {
		DelayMeasurementParams dmp = delayParams[index];
		if (useDefault[index].isSelected()) {
			dmp = delayParams[0];
		}
		if (forceDefault && dmp == null) {
			dmp = delayParams[0];
		}
		return dmp;
	}

	private void saySettings(int index) {
		DelayMeasurementParams dmp = getParamsForIndex(index, true);
		if (dmp == null) {
			typeSettings[index].setText("Error - enknown delay settings");
		}
		else {
			typeSettings[index].setText(dmp.toString());
		}
		if (index == 0) {
			for (int i = 1; i < typesList.length; i++) {
				saySettings(i);
			}
		}
	}

	public void enableControls() {
		for (int i = 0; i < typesList.length; i++) {
			typeChange[i].setDisable((i != 0 && useDefault[i].isSelected()));
		}
	}
	
	void settingsButtonPress(int typeIndex) {
//		DelayMeasurementParams dmp = getParamsForIndex(typeIndex, true);
//		dmp = DelayOptionsDialog.showDialog(dmp);
//		if (dmp != null) {
//			delayParams[typeIndex] = dmp;
//			saySettings(typeIndex);
//			enableControls();
//		}
	}

}
