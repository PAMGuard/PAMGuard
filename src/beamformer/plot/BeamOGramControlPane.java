package beamformer.plot;

import dataPlotsFX.FXIconLoder;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.scrollingPlot2D.Plot2DControPane;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import dataPlotsFX.spectrogramPlotFX.SpectrogramParamsFX;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

public class BeamOGramControlPane extends Plot2DControPane implements TDSettingsPane {

	private BeamOGramPlotInfo beamOGramPlotInfo;

	private Canvas icon; // 20x20 icon. load from an image. 

	public BeamOGramControlPane(TDGraphFX tdGraph, BeamOGramPlotInfo beamOGramPlotInfo) {
		super(beamOGramPlotInfo, tdGraph, Orientation.VERTICAL, true, false, true, true);
		this.beamOGramPlotInfo = beamOGramPlotInfo;
		icon = FXIconLoder.createIcon("Resources/BeamformIcon20.png", 20, 20);
		
		BeamOGramPlotParams boPlotParams = (BeamOGramPlotParams) beamOGramPlotInfo.getPlot2DParameters();

//		setAmplitudeProperties(boPlotParams.getAmplitudeLimits(), boPlotParams.getMaxAmplitudeLimits());
	}

	@Override
	public Node getHidingIcon() {
		return icon;
	}

	@Override
	public String getShowingName() {
		return beamOGramPlotInfo.getShortName();
	}

	@Override
	public Node getShowingIcon() {
		return icon;
	}

	@Override
	public Pane getPane() {
		return this;
	}

}
