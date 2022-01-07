package dataPlots.mouse;

import java.awt.event.MouseEvent;

import dataPlots.data.FoundDataUnit;
import dataPlots.layout.TDGraph;

public interface PlotMouseListener {

	public int mouseClicked(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

	public int mouseEntered(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

	public int mouseExited(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

	public int mousePressed(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

	public int mouseReleased(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent, FoundDataUnit foundDataUnit);

}
