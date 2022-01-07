package dataPlots.mouse;

import java.awt.event.MouseEvent;

import dataPlots.layout.TDGraph;
import PamView.zoomer.ZoomShape;
import PamView.zoomer.Zoomable;

/**
 * A basic zoomer class for the tdgraph.
 * @author Jamie Macaulay
 */
public class TDGraphZoomer extends AbstractTDZoomable {
	

	private TDGraph tdGraph;
	private int iPanel; 
	private boolean complete=true;
	private PlotZoomerAdapter zomerAdapater;

	public TDGraphZoomer(TDGraph tdGraph, int iPanel, PlotZoomerAdapter zoomerAdapter){
		this.zomerAdapater=zoomerAdapter; 
		this.tdGraph=tdGraph;
		this.iPanel=iPanel;
	}

	@Override
	public boolean canStartZoomArea(MouseEvent mouseEvent) {
		zomerAdapater.newZoomShapeStarted(iPanel);
		return true;
	}

	@Override
	public void zoomShapeChanging(ZoomShape zoomShape) {
		complete=false;
		repaintPanels();
	}
	
	/**
	 * Repaint all panels in the tdgraph. 
	 */
	private void repaintPanels(){
		for (int i=0; i<tdGraph.getNumPlotPanels(); i++){
			tdGraph.getGraphPlotPanel(i).repaintHighlights();
		}
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public void zoomPolygonComplete(ZoomShape zoomShape) {
		zomerAdapater.zoomShapeComplete(iPanel);
		complete=true; 
		repaintPanels();
	}

	@Override
	public double getXStart() {
//		System.out.println("RDGraphZoomer: "+tdGraph.getTdControl().getTimeScroller().getValueMillis() + " "+ tdGraph.getTdControl().getTdAxes().getTimeAxis().getMinVal()); 
		return tdGraph.getTdControl().getTimeScroller().getValueMillis();
	}

	@Override
	public double getXScale() {
		return tdGraph.getTdControl().getGraphTimePixels()/getXRange();
	}

	@Override
	public double getXRange() {
		return tdGraph.getTdControl().getTimeScroller().getVisibleAmount();
	}

	@Override
	public double getYStart() {
		return tdGraph.getGraphAxis().getMinVal();
	}

	@Override
	public double getYScale() {
		return tdGraph.getGraphPlotPanel(iPanel).getHeight()/getYRange(); 

	}

	@Override
	public double getYRange() {
		return tdGraph.getGraphAxis().getMaxVal()-getYStart();
	}

	@Override
	public int getCoordinateType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void zoomToShape(ZoomShape zoomShape) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean canClearZoomShape(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON1){
			//shape cleared then we clear all the selected data units...
			zomerAdapater.zoomShapeCleared(iPanel);
			return true;
		}
		return false;
	}
	
}