package Spectrogram;

import java.awt.event.MouseEvent;
import PamView.zoomer.ZoomShape;
import PamView.zoomer.Zoomable;
import Spectrogram.SpectrogramDisplay.SpectrogramPanel;

/**
 * The Spectrogram Zoomer acts as the class which handles all zoomer functionality. The zoomer is assocaited with a particular spectrogramPanel. 
 * @author Jamie Macaulay
 */
public class SpectrogramZoomer implements Zoomable {
	

	private SpectrogramPanel spectroPanel;
	private SpectrogramDisplay spectrogramDisplay;
	private boolean complete;

	public SpectrogramZoomer(SpectrogramDisplay spectrogramDisplay, SpectrogramPanel spectroPanel){
		this.spectroPanel=spectroPanel;
		this.spectrogramDisplay=spectrogramDisplay;
	}

	@Override
	public boolean canStartZoomArea(MouseEvent mouseEvent) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void zoomShapeChanging(ZoomShape zoomShape) {
		complete=false; 
//		System.out.println("Spectro Zoom Shape Changing");
		spectroPanel.repaint();
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	@Override
	public void zoomPolygonComplete(ZoomShape zoomShape) {
		complete=true; 
		spectroPanel.repaint();
		spectrogramDisplay.repaintAll();
	}

	@Override
	public double getXStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getXScale() {
		return 10;
	}

	@Override
	public double getXRange() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public double getYStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getYScale() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public double getYRange() {
		// TODO Auto-generated method stub
		return 100;
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
		// TODO Auto-generated method stub
		return false;
	}

}
