package soundPlayback.fx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import soundPlayback.preprocess.PlaybackFilter;
import soundPlayback.preprocess.PreProcessFXPane;

public class FilterSidePane  implements PreProcessFXPane {
	
	
	private Label label;
	
	
	private FilterSlider filterSlider;


	private PlaybackFilter playBackFilter;


	private PamBorderPane mainPane;


	private PamButton defaultGainButton;
	
	
	public FilterSidePane(PlaybackFilter playBackFilter) {
		
		this.playBackFilter  = playBackFilter; 
		
		filterSlider = new FilterSlider();

		filterSlider.getSlider().setTooltip(new Tooltip("High pass filter the data before playback"));
		filterSlider.addChangeListener((oldval, newVal, obsVal)->{
			filterChanged();

		});
		
		
		defaultGainButton = new PamButton("off");
		defaultGainButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-refresh", PamGuiManagerFX.iconSize-3));
		defaultGainButton.setPrefWidth(70);
		defaultGainButton.setOnAction((action)->{
			filterSlider.setDataValue(filterSlider.getMinValue());
		});
		
		label = new Label("Filter"); 
		//label.setGraphic(PamGlyphDude.createPamIcon("mdi2f-filter", PamGuiManagerFX.iconSize));
		
		PamHBox hBox = new PamHBox();
		hBox.setAlignment(Pos.CENTER_LEFT);
		hBox.setSpacing(5);
		hBox.getChildren().addAll(PamGlyphDude.createPamIcon("mdi2f-filter", PamGuiManagerFX.iconSize), filterSlider);
		filterSlider.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(filterSlider, Priority.ALWAYS);
	
		this.mainPane = new PamBorderPane();
		this.mainPane.setCenter(hBox);
		
	}
	
	protected void filterChanged() {
		playBackFilter.setValue(filterSlider.getDataValue());
		sayFilter();
	}
	
	@Override
	public void update() {
		filterSlider.setDataValue(playBackFilter.getValue());
		sayFilter();
	}
	
	private void sayFilter() {
		//playGainSlider.setTextLabel(playbackGain.getTextValue());
		label.setText(playBackFilter.getTextValue());
		defaultGainButton.setDisable(false);
		if (playBackFilter.getValue()==filterSlider.getMinValue()) {
			defaultGainButton.setDisable(true);
		}
	}



	@Override
	public Pane getPane() {
		return mainPane;
	}


	@Override
	public Label getLabel() {
		return label;
	}

	@Override
	public Node getDefaultButton() {
		return defaultGainButton;
	}


}