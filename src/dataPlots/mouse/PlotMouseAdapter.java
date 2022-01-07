package dataPlots.mouse;

import java.awt.event.MouseEvent;

import dataPlots.data.FoundDataUnit;
import dataPlots.layout.TDGraph;
import PamguardMVC.PamDataUnit;

public class PlotMouseAdapter implements PlotMouseMotionListener,
		PlotMouseListener {

	@Override
	public int mouseClicked(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		return 0;
	}

	@Override
	public int mouseEntered(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		return 0;
	}

	@Override
	public int mouseExited(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		return 0;
	}

	@Override
	public int mousePressed(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		return 0;
	}

	@Override
	public int mouseReleased(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int mouseDragged(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		return 0;
	}

	@Override
	public int mouseMoved(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		return 0;
	}

}
