package dataPlotsFX.spectrogramPlotFX;

import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import javafx.application.Platform;

public class Spectrogram2DPlotData extends Scrolling2DPlotDataFX {

	private Scrolling2DPlotInfo specPlotInfo;

	public Spectrogram2DPlotData(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
		super(specPlotInfo, iChannel);
		this.specPlotInfo = specPlotInfo;
	}
	
	@Override
	public void rebuildFinished(){
		specPlotInfo.setnRebuiltPanels(specPlotInfo.getnRebuiltPanels()+1);
		//only repaint once all channels have finished rebuilding. 
		if (specPlotInfo.getnRebuiltPanels()>=specPlotInfo.getNActivePanels()){
			Platform.runLater(()->{
				specPlotInfo.getTDGraph().repaint(0);
			}); 
		}
	}

}
