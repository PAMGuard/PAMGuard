package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.rawDataPlotFX.RawSoundPlotDataFX;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * Graphics for raw data for the acoustic scroll bar. 
 * @author Jamie Macaulay
 *
 */
public class RawScrollBarGraphics implements AcousticScrollerGraphics {

	/**
	 * Functions to draw sound data.
	 */
	RawSoundPlotDataFX rawSoundPlotDataFX = new RawSoundPlotDataFX();

	/**
	 * Reference to the acoustic scroller this data is being displayed in. 
	 */
	private AcousticScrollerFX acousticScroller;

	/**
	 * Reference to the acoustic data block. 
	 */
	private PamDataBlock<RawDataUnit> rawDataBlock;

	/*
	 * 
	 */
	private PamAxisFX amplitudeAxis;

	private RawDataUnit lastRawDataUnit;
	
	private float currentSampleRate=0;

	public RawScrollBarGraphics(AcousticScrollerFX acousticScroller, PamDataBlock<RawDataUnit> rawDataBlock){
		this.acousticScroller=acousticScroller; 
		this.rawDataBlock=rawDataBlock; 
		if (rawDataBlock!=null) currentSampleRate=rawDataBlock.getSampleRate();
		createAmplitudeAxis();
	}

	private void createAmplitudeAxis(){
		amplitudeAxis = new PamAxisFX(0, 1, 0, 1, 0, 10, PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE, "%4d");
		amplitudeAxis.y1Property().setValue(0);
		amplitudeAxis.y2Property().bind(acousticScroller.getScrollBarPane().heightProperty());
		amplitudeAxis.x1Property().bind(acousticScroller.getScrollBarPane().widthProperty());
		amplitudeAxis.x2Property().bind(acousticScroller.getScrollBarPane().widthProperty());

		amplitudeAxis.minValProperty().setValue(-1);
		amplitudeAxis.maxValProperty().setValue(1);
	}

	@Override
	public PamDataBlock getDataBlock() {
		return rawDataBlock;
	}

	PamDataUnit lastData;
	@Override
	public synchronized void addNewData(PamDataUnit rawDataUnit) {
		try{
			if (rawDataUnit.getParentDataBlock()==rawDataBlock && rawDataUnit!=lastData){
				lastRawDataUnit=((RawDataUnit) rawDataUnit);
				rawSoundPlotDataFX.setSampleRate(rawDataUnit.getParentDataBlock().getSampleRate());
				rawSoundPlotDataFX.newRawData(lastRawDataUnit, acousticScroller.getBinsPerPixel(rawDataUnit.getParentDataBlock().getSampleRate()));
				lastData=rawDataUnit;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}



	Canvas canvas;
	Rectangle windowRect;
	@Override
	public void repaint(){

		//get the canvas. 
		canvas=acousticScroller.getScrollBarPane().getDrawCanvas();


		windowRect=new Rectangle(0,0, 	canvas.getWidth(), 		canvas.getHeight());

		//		canvas.getGraphicsContext2D().fillRect(1000*(Math.random()), 50*(Math.random()), 10*(Math.random()-0.5), 10*(Math.random()));
		//		canvas.getGraphicsContext2D().setFill(Color.BLUEVIOLET);

		//clear the rectangle 
		acousticScroller.getScrollBarPane().getDrawCanvas().getGraphicsContext2D().clearRect(0, 0, windowRect.getWidth(), windowRect.getHeight());


//				System.out.println(acousticScroller);
//				System.out.println("AcousticScrollerFX: x1: "+acousticScroller.getTimeAxis().x1Property().get() +" x2: "+  acousticScroller.getTimeAxis().x2Property().get() + 
//						" min val: "+acousticScroller.getTimeAxis().minValProperty().get()+ " max val: "+acousticScroller.getTimeAxis().maxValProperty().get()+ "binsperpixel: "+rawSoundPlotDataFX.getBinsPerPixel()
//					+ " samplerate: "+rawSoundPlotDataFX.getSampleRate());
		//		
		//		System.out.println("AcousticScroller millis: "+ " value: "+ PamCalendar.formatTime(acousticScroller.getValueMillis())
		//		+ "  min "+PamCalendar.formatTime(acousticScroller.getMinimumMillis())+ " visible "+acousticScroller.getVisibleAmount()/1000. + 
		//		" max: " +PamCalendar.formatTime(acousticScroller.getMaximumMillis()) + "binsperpixel: "+rawSoundPlotDataFX.getBinsPerPixel());
		//		
		//		System.out.println(""); 

		rawSoundPlotDataFX.drawRawSoundData(acousticScroller.getScrollBarPane().getDrawCanvas().getGraphicsContext2D(),  
				windowRect, acousticScroller.getOrientation() ,  acousticScroller.getTimeAxis(),  amplitudeAxis,
				acousticScroller.getMinimumMillis(), -1);
	}

	@Override
	public String getName() {
		return "Waveform";
	}
	
	@Override
	public void clearStore() {
		this.rawSoundPlotDataFX.clearRawData();
	}
	
	/**
	 * True if the data block requires offline loading of data. 
	 */
	public boolean orderOfflineData(){
		return true;
	}

	@Override
	public void notifyUpdate(int flag) {

	}

	@Override
	public void setColors(Plot2DColours specColors) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Plot2DColours getColors() {
		// TODO Auto-generated method stub
		return null;
	}

}
