package dataPlotsFX.scroller;

import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * SettingsPane for acosutic scroller. 
 * @author Jamie Macaulay
 *
 */
public class TDScrollerSettingsPane extends DynamicSettingsPane<TDAcousticScrollerParams> {

	private SpectrogramControlPane datagramColours;
	
	
	private TDAcousticScrollerParams currentParams;

	private PamBorderPane mainPane = new PamBorderPane();

	public TDScrollerSettingsPane(Object owner, TDAcousticScroller tdAcousticScroller){
		super(owner);

		datagramColours = new ScrollerColoursPane(); 
		datagramColours.setMaxWidth(500);
		HBox.setHgrow(datagramColours, Priority.ALWAYS);
	

		/**
		 * Add listeners to amplitude limits. Here we use the changing property of the range slider thumbs instead of the amplitude limits.
		 * This is because the recolour can take a whiole and having threads starting and stopping still slows down moving the thumbs making for 
		 * a clunky user experience. Therefore recolour only takes places ones thumbs have stopped moving. 
		 */
		datagramColours.getColourSlider().lowValueChangingProperty().addListener((obserVal, oldVal, newVal)->{
			if (!newVal){
				super.notifySettingsListeners();
			}
		});

		datagramColours.getColourSlider().highValueChangingProperty().addListener((obserVal, oldVal, newVal)->{
			if (!newVal){
				super.notifySettingsListeners();
			}
		});

		//register changes on colour box. 
		datagramColours.getColorBox().valueProperty().addListener((ov,  t,  t1) -> {    
			if (t!=t1){
				super.notifySettingsListeners();
			}
		});

		Label lowValue= new Label(); 
		lowValue.textProperty().bind(datagramColours.getColourSlider().minProperty().asString());

		Label highValue= new Label(); 
		highValue.textProperty().bind(datagramColours.getColourSlider().maxProperty().asString());

		PamButton backButton = new PamButton(); 
//		backButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_LEFT, PamGuiManagerFX.iconSize));
		backButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
		backButton.setOnAction((action)->{
			tdAcousticScroller.showSettingPane();
		});
		backButton.getStyleClass().add("square-button-trans");
		backButton.prefHeightProperty().bind(mainPane.heightProperty());

		backButton.setPadding(new Insets(0,20,0,0));

		Label ampLabel=new Label ("Amplitude " + TDProjectorFX.getUnitName(ParameterUnits.DB)); 
		ampLabel.setPadding(new Insets(0,100,0,0));

		HBox settingsBox=new HBox(); 
		settingsBox.setSpacing(5);
		settingsBox.getChildren().addAll(backButton, new Label ("Amplitude " + TDProjectorFX.getUnitName(ParameterUnits.DB)),
				lowValue, datagramColours, highValue, datagramColours.getColorBox()); 
		settingsBox.setAlignment(Pos.CENTER_LEFT);
		
		
		CheckBox disableFFTScroller = new CheckBox(); 
		

		mainPane.setCenter(settingsBox); 
		mainPane.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
		mainPane.getStyleClass().add("pane-trans");


	}

	@Override
	public TDAcousticScrollerParams getParams(TDAcousticScrollerParams p) {
		currentParams.colourMap=datagramColours.getColourArrayType(); 

		currentParams.amplitudeLimits[0]=datagramColours.getColourSlider().getLowValue(); 
		currentParams.amplitudeLimits[1]=datagramColours.getColourSlider().getHighValue(); 

		currentParams.amplitudeMinMax[0]=datagramColours.getColourSlider().getMin(); 
		currentParams.amplitudeMinMax[1]=datagramColours.getColourSlider().getMax(); 

		return currentParams;
	}

	@Override
	public void setParams(TDAcousticScrollerParams params) {
		this.currentParams=params; 

		datagramColours.getColourSlider().setMin(params.amplitudeMinMax[0]);
		datagramColours.getColourSlider().setMax(params.amplitudeMinMax[1]);

		datagramColours.getColourSlider().setLowValue(params.amplitudeLimits[0]);
		datagramColours.getColourSlider().setHighValue(params.amplitudeLimits[1]);
		
		//because the settings are called by the colourbox, you must set it last when using
		// a dynamic settings pane. Otherwise it calls getParams whihc sets the amplitude limits to the default. 0 to 1. 
		datagramColours.setColourArrayType(params.colourMap);
		
//		System.out.println("AMPLITUDE LIMITS set params: " +datagramColours.getColourSlider().getLowValue()
//				+ " " + datagramColours.getColourSlider().getHighValue());
	}
	
	/**
	 * Increment the amplitude
	 * @param incrment - the increment to set. 
	 */
	public void incrementAmplitude(double incrment) {
		datagramColours.getColourSlider().setLowValue(datagramColours.getColourSlider().getLowValue()+incrment);
		datagramColours.getColourSlider().setHighValue(datagramColours.getColourSlider().getHighValue()+incrment);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Acoustic Scroller Params";
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
	 * Contoirl pane for datagram colours. 
	 * @author Jamie Macaulay
	 *
	 */
	private class ScrollerColoursPane extends SpectrogramControlPane{
		
		private ScrollerColoursPane(){
			super(Orientation.HORIZONTAL, true, 
					false, true, false);
			this.getColourSlider().showTickLabelsProperty().setValue(false);
			this.getColourSlider().showTickMarksProperty().setValue(false);
			this.getChildren().remove(this.getColorBox()); //don't want the colour box in default position
		}
		
		@Override
		public void setColours(){
//			System.out.println("AMPLITUDE LIMITS 1: " +this.getColourSlider().getLowValue()
//					+ " " + this.getColourSlider().getHighValue());
			super.setColours();
			notifySettingsListeners();
//			System.out.println("AMPLITUDE LIMITS 2: " +this.getColourSlider().getLowValue()
//					+ " " + this.getColourSlider().getHighValue());
		}
		
	}



}
