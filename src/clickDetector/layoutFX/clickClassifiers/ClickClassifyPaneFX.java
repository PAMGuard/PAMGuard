package clickDetector.layoutFX.clickClassifiers;

import org.controlsfx.glyphfont.Glyph;

import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import clickDetector.ClickClassifiers.ClickClassifierManager;
import clickDetector.ClickClassifiers.ClickIdentifier;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * Pane which allows users to select click classifiers. Rather than opening endless dialogs click settings are 
 * handled by sliding panes. 
 * <p>
 * Note that this is a copy...ish of the swing frameowrk - wouldn't od things this way usually. 
 * @author Jamie Macaulay
 *
 */
public class ClickClassifyPaneFX extends PamStackPane {

	/**
	 * Box which selects which type of classifier to use. 
	 */
	private ComboBox<String> classifierComboBox;

	/**
	 * Reference to the click detector controlled unit. 
	 */
	private ClickControl clickControl;

	
	private PamToggleSwitch runOnlineCheckBox;

	private CheckBox discardClicksCheckBox;

	private PamBorderPane classifierHolder;

	/**
	 * The currently sleected click identifer. 
	 */
	private ClickIdentifier currentClickIdentifier;

	/**
	 * Holds the comboBox and custom click classification panes.
	 */
	public PamBorderPane holderPane;

	/**
	 * Hiding pane. 
	 */
	private HidingPane hidingPane;

	/**
	 * The pane to add content to whihc can hide. 
	 */
	private PamBorderPane hidingHolderPane = new PamBorderPane(); 

	private PamButton hidingPaneCloseButton; 


	public ClickClassifyPaneFX(ClickControl clickControl){
		this.clickControl=clickControl;
		holderPane=new PamBorderPane(); 
		holderPane.setTop(createClassifyPane());
		this.getChildren().add(holderPane);
		createHidingPane(); 
	}

	/**
	 * Create a hiding pane which can show classifier settings etc. 
	 */
	private void createHidingPane(){
		hidingPane=new HidingPane(Side.RIGHT, hidingHolderPane, this, false);
		//hidingPane.showHidePane(false);		

		hidingPaneCloseButton=hidingPane.getHideButton();
		hidingPaneCloseButton.setPrefWidth(40);
		hidingPaneCloseButton.getStyleClass().add("close-button-right-trans");
		hidingPaneCloseButton.setGraphic(Glyph.create("FontAwesome|CHEVRON_RIGHT").size(22).color(Color.DARKGRAY.darker()));

		setHidingPaneLocation();
	}

	/**
	 * Added purely so can be override and hiding pane set in different location if required
	 */
	public void setHidingPaneLocation(){
		holderPane.setRight(hidingPane);
		hidingPane.showHidePane(false);
	}

	private Node createClassifyPane(){

		//create the main pane. 
		PamVBox holderPane=new PamVBox();
		holderPane.setPadding(new Insets(5,5,5,5));
		holderPane.setSpacing(5);

		//create label
		Label title=new Label("Click Classifiers");
		PamGuiManagerFX.titleFont2style(title);

		//create the comboBox with classifier name
		classifierComboBox=new ComboBox<String>();
		for (int i=0; i<clickControl.getClassifierManager().getNumClassifiers(); i++){
			classifierComboBox.getItems().add(clickControl.getClassifierManager().getClassifierName(i));
			classifierComboBox.setOnAction((action)->{
				//get the current click identifier.
				//System.out.println("ClickClassifyPaneFX:setOnAction: " +classifierComboBox.getSelectionModel().getSelectedIndex());
				currentClickIdentifier=clickControl.getClassifierManager().
						getClassifier(classifierComboBox.getSelectionModel().getSelectedIndex());				
				//change the centre pane to classifier specific pane
				setCenterPane();
			});
		}

		runOnlineCheckBox=new PamToggleSwitch("Run Classifier");
		//discardClicksCheckBox=new CheckBox("Discdard Classified Clicks");

		PamHBox pamHBox=new PamHBox();
		pamHBox.setAlignment(Pos.CENTER);
		pamHBox.setSpacing(5.);
		pamHBox.getChildren().addAll(classifierComboBox);
		classifierComboBox.setMaxWidth(500);
		HBox.setHgrow(classifierComboBox, Priority.ALWAYS);
		pamHBox.setAlignment(Pos.CENTER_LEFT);

		//other label
		Label classifierTitle=new Label("Click Classifier Type");
		PamGuiManagerFX.titleFont2style(classifierTitle);

		holderPane.getChildren().addAll(title, pamHBox, classifierTitle, classifierHolder=new PamBorderPane());
		return holderPane;

	}

	/**
	 * Set the custom pane for whichever click classifier has been selected. 
	 */
	private void setCenterPane(){
		if (currentClickIdentifier!=null){
			holderPane.setCenter(currentClickIdentifier.getClassifierPane().getNode());
		}
		else holderPane.setCenter(null);
	}

	public void setParams(ClickParameters clickParameters){
		//set parameters - listener will do everything else.
		currentClickIdentifier=clickControl.getClassifierManager().
				getClassifier(clickParameters.clickClassifierType);
		if (clickParameters.clickClassifierType == ClickClassifierManager.CLASSIFY_BASIC) {
			
		}
		
		//System.out.println("ClickClassifyPaneFX:setParams(): " +classifierComboBox.getSelectionModel().getSelectedIndex());
		classifierComboBox.getSelectionModel().select(clickParameters.clickClassifierType);
		
		//System.out.println("ClickClassifyPaneFX: setParams: get selected classifier is: " + 	clickParameters.clickClassifierType);

		//set classifier parameters
		if (currentClickIdentifier!=null && currentClickIdentifier.getClassifierPane()!=null) {
			this.currentClickIdentifier.getClassifierPane().setParams();
		}
		setCenterPane();
	}

	public ClickParameters getParams(ClickParameters clickParameters){
		//set all parameters in classifier. 
		//System.out.println("ClickClassifyPaneFX:getParams(): " +classifierComboBox.getSelectionModel().getSelectedIndex());
		
		clickParameters.clickClassifierType=classifierComboBox.getSelectionModel().getSelectedIndex();
		
		//System.out.println("ClickClassifyPaneFX: getParams: get selected classifier is: " + 	clickParameters.clickClassifierType);
		if (currentClickIdentifier != null && currentClickIdentifier.getClassifierPane() != null) {
			 currentClickIdentifier.getClassifierPane().getParams();
		}
		return clickParameters;
	}
}
