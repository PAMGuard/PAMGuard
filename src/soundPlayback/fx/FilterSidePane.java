package soundPlayback.fx;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import soundPlayback.preprocess.PlaybackFilter;
import soundPlayback.preprocess.PreProcessFXPane;

public class FilterSidePane  implements PreProcessFXPane {
	
	
	private Label label;
	
	
	private FilterSlider filterSlider;


	private PlaybackFilter playBackFilter;


	private PamBorderPane mainPane;
	
	
	public FilterSidePane(PlaybackFilter playBackFilter) {
		
		this.playBackFilter  = playBackFilter; 
		
		filterSlider = new FilterSlider();

		filterSlider.getSlider().setTooltip(new Tooltip("High pass filter the data before playback"));
		filterSlider.addChangeListener((oldval, newVal, obsVal)->{
			filterChanged();

		});

		this.mainPane = new PamBorderPane();
		this.mainPane.setCenter(filterSlider);
		
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
	}



	@Override
	public Pane getPane() {
		return mainPane;
	}


	@Override
	public Label getLabel() {
		label = new Label("Filter"); 
		label.setGraphic(PamGlyphDude.createPamIcon("mdi2f-filter", PamGuiManagerFX.iconSize));
		return label;
	}


}