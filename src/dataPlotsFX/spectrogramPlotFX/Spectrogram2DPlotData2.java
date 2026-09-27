package dataPlotsFX.spectrogramPlotFX;

import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX2;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import javafx.application.Platform;

/**
 * Tiled equivalent of {@link Spectrogram2DPlotData}, used with the
 * {@link Scrolling2DPlotDataFX2} renderer. Carries the same repaint coordination
 * so that the display only repaints once all channels have finished re-colouring.
 *
 * @author Jamie Macaulay
 */
public class Spectrogram2DPlotData2 extends Scrolling2DPlotDataFX2 {

	private Scrolling2DPlotInfo specPlotInfo;

	public Spectrogram2DPlotData2(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
		super(specPlotInfo, iChannel);
		this.specPlotInfo = specPlotInfo;
	}

	@Override
	public void rebuildFinished() {
		specPlotInfo.setnRebuiltPanels(specPlotInfo.getnRebuiltPanels() + 1);
		// only repaint once all channels have finished rebuilding.
		if (specPlotInfo.getnRebuiltPanels() >= specPlotInfo.getNActivePanels()) {
			Platform.runLater(() -> {
				specPlotInfo.getTDGraph().repaint(0);
			});
		}
	}

}
