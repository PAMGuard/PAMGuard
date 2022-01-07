package dataPlots.mouse;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import dataPlots.data.FoundDataUnit;
import dataPlots.layout.TDGraph;

/**
 * Class for dealing with sleecting individual data units with a Mouse. 
 * @author spn1
 *
 */
public class MouseSelectionListener extends PlotMouseAdapter {
	
	/**
	 * The minimum pixel distance for a mouse to be for unit to be selected
	 */
	public double minPixDist=15; 
	
	
	public MouseSelectionListener(){
		
	}

	@Override
	public int mouseClicked(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent,
			FoundDataUnit foundDataUnit) {
//		System.out.println("Mouse Clicked: "+foundDataUnit);
//		return selectDataUnit(tdGraph, iPanel, mouseEvent, foundDataUnit);
		return 0; 
	}

	@Override
	public int mouseEntered(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent,
			FoundDataUnit foundDataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int mouseExited(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent,
			FoundDataUnit foundDataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int mousePressed(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent,
			FoundDataUnit foundDataUnit) {
		if (mouseEvent.getButton()==MouseEvent.BUTTON1){
			checkZoomerShapes(tdGraph, iPanel, mouseEvent, foundDataUnit); 
			return selectDataUnit(tdGraph, iPanel, mouseEvent, foundDataUnit);
		}
		else return 0; 
	}

	@Override
	public int mouseReleased(TDGraph tdGraph, int iPanel,
			MouseEvent mouseEvent, FoundDataUnit foundDataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Select the data if clicked. 
	 * @param tdGraph
	 * @param iPanel
	 * @param mouseEvent
	 * @param foundDataUnit
	 * @return
	 */
	private int selectDataUnit(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent,
			FoundDataUnit foundDataUnit){
		if (foundDataUnit==null || foundDataUnit.distance>minPixDist) {
			//clear the selected clicks if none are pressed. 
			tdGraph.setSelectedDataUnits(new ArrayList<FoundDataUnit>());
			tdGraph.getGraphPlotPanel(iPanel).repaintHighlights();
			return 0; 
		}
		//check if the control key is pressed
		if ((mouseEvent.getModifiers() & ActionEvent.CTRL_MASK) !=ActionEvent.CTRL_MASK) {
			//if not pressed clear the selected data units;
			tdGraph.setSelectedDataUnits(new ArrayList<FoundDataUnit>());
		}
		//if a data units is found set as selected data unit.
		tdGraph.addSelectedDataUnit(foundDataUnit);
		tdGraph.getGraphPlotPanel(iPanel).repaintHighlights();
		return 1;
	}
	
	/**
	 * Check if any zoomer shapes should be altered or cleared. 
	 */
	private void checkZoomerShapes(TDGraph tdGraph, int iPanel, MouseEvent mouseEvent,
			FoundDataUnit foundDataUnit){
		if (mouseEvent.getButton()==MouseEvent.BUTTON1){
			for (int i=0; i<tdGraph.getNumPlotZoomers(); i++){
				if (tdGraph.getPlotZoomer(i).getZoomable(iPanel).isComplete()) tdGraph.getPlotZoomer(i).getZoomer(iPanel).clearLatestShape();
			}
		}
	}
	
}
