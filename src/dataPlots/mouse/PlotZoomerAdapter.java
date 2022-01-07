package dataPlots.mouse;

import java.awt.Point;
import java.util.ArrayList;

import PamView.zoomer.Zoomer;
import PamguardMVC.PamDataUnit;
import dataPlots.data.FoundDataUnit;
import dataPlots.data.TDDataInfo;
import dataPlots.layout.TDGraph;

/**
 * This is a class designed to act as an adapter between the zoomer class and the TDGraph class. Basicaly it adds an instance of the desired zoomer to each panel in the class and 
 * deals with changing number of panels etc. Maybe expanded to introduce a new sub class of zoomer or for other extra functionality. 
 * @author Jamie Macaulay
 *
 */
public class PlotZoomerAdapter {
	
	private TDGraph tdGraph;
	private ArrayList<Zoomer> tdGraphZoomer;
	private ArrayList<AbstractTDZoomable> tdGraphZoomable;

	public  PlotZoomerAdapter(TDGraph tdGraph){
		this.tdGraph=tdGraph; 
		addZoomables();
	}
	
	/**
	 * Test whether or not a pamdetection is within a marked area on the screen   
	 * @param dataUnit- data unit to test whether 
	 * @return true if no mark or click is within the mark. 
	 */
	public boolean dataUnitInMarkArea(TDDataInfo dataInfo, PamDataUnit dataUnit, int iPanel) {
		Point dataUnitPoint = tdGraph.getGraphPlotPanel(iPanel).getDataUnitPoint(dataInfo,dataUnit);
		if (dataUnitPoint==null) return false; 
		//return true if there is no mark which we don't want
		boolean inMark=getZoomer(iPanel).isInMark(tdGraph.getGraphPlotPanel(iPanel), dataUnitPoint);
		//so check there's actually a shape there. 
		boolean shapeThere=(getZoomer(iPanel).getTopMostShape()!=null);
		return (inMark && shapeThere);
	}
	
	/**
	 * Create a new zoomable. Sub class this to use a different zoomable. 
	 * @param- panel for this zoomable 
	 * @return
	 */
	public AbstractTDZoomable createNewZoomable(int iPanel){
		return new TDGraphZoomer(tdGraph, iPanel, this); 
	}
	
	/**
	 * Get the zoomer for a specific panel 
	 * @param iPanel the panel number
	 * @return zoomer for the panel 
	 */
	public Zoomer getZoomer(int iPanel){
		if (tdGraphZoomer==null) return null; 
		return tdGraphZoomer.get(iPanel);
	}
	
	/**
	 * Get the zoomable for a panel 
	 * @param iPanel the panel number
	 * @return zoomable for the panel 
	 */
	public AbstractTDZoomable getZoomable(int iPanel){
		if (tdGraphZoomable==null) return null; 
		return tdGraphZoomable.get(iPanel);
	} 
	
	/**
	 * Clear any existing zoomables and add new zoomables to each plot panel. A zoomable is a legacy name for a class which alows
	 * users to mark out multiple detections on a display. 
	 */
	public void addZoomables(){
		tdGraphZoomer=new ArrayList<Zoomer>();
		tdGraphZoomable=new ArrayList<AbstractTDZoomable>();
		AbstractTDZoomable tempZoomer;
		//add a zoomer to each plot panel. 
		for (int i = 0; i < tdGraph.getNumPlotPanels(); i++) {
			//add zoomables
			tdGraphZoomer.add(new Zoomer(tempZoomer=createNewZoomable(i),tdGraph.getGraphPlotPanel(i))); 
			tdGraphZoomable.add(tempZoomer);
		}
	}

	/**
	 * Called whenever a panels zoom shape is completed. 
	 * @param iPanel- the panel within the tdGraph. 
	 */
	public void zoomShapeComplete(int iPanel) {
		//we need to find all data units within the zoomer shape. 
		ArrayList<FoundDataUnit> foundUnits=tdGraph.getGraphPlotPanel(iPanel).findUnitsWithinMark(this);
		tdGraph.getSelectedDataUnits().addAll(foundUnits);
		//remove duplicates- this could happen in the unlikely event we have more than one plotadapater in the tdGraph
		//or have marked already selected data units. 
		tdGraph.removeListDuplicates(tdGraph.getSelectedDataUnits());
		//now we need to repaint the graph to show highlighted units etc. 
		tdGraph.getGraphPlotPanel(iPanel).repaintHighlights();
	
	}
	
	/**
	 * Called whenever there is an attempt to clear a zoom shape. We must clear zoom shape on all panels. 
	 */
	public void zoomShapeCleared(int iPanel){
		boolean shapeThere=(getZoomer(iPanel).getTopMostShape()!=null);
		if (shapeThere) tdGraph.clearSelectedDataUnits();
	}
	
	/**
	 * Called whenever a new zoom shape is started. Needs to clear other zoom shapes if other panels are present in this graph. 
	 */
	public void newZoomShapeStarted(int iPanel){
		for (int i=0; i<tdGraphZoomer.size(); i++){
			if (i!=iPanel) tdGraphZoomer.get(i).clearLatestShape();
		}
	}
	
	

	
}
