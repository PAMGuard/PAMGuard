package dataPlots.mouse;

import java.awt.event.MouseEvent;

import dataPlots.data.FoundDataUnit;
import dataPlots.layout.TDGraph;
import PamguardMVC.PamDataUnit;

public interface PlotMouseMotionListener {

	public int mouseDragged(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

	public int mouseMoved(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

}
