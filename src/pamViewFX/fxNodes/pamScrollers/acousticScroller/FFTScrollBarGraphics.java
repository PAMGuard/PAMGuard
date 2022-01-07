package pamViewFX.fxNodes.pamScrollers.acousticScroller;
import PamUtils.PamCoordinate;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public class FFTScrollBarGraphics implements AcousticScrollerGraphics {
	
	/**
	 * The spectrogram plot. 
	 */
	public SpecDatagramPlot spectrogramPlot;
	
	/**
	 * Reference to the FFT data block. 
	 */
	private FFTDataBlock fftDataBlock; 
	
	/**
	 * The spectrogram channel to plot. 
	 */
	private int channel=0;

	/**
	 * The acoustic scroller. 
	 */
	private AcousticScrollerFX acousticScroller;

	private Canvas canvas;
	
//	public GeneralSpectrogramColours spectrogramColours;

//	private PamAxisFX freqAxis; 
	
	/**
	 * 
	 */
	AcousticScrollerProjector projector;

	/**
	 * Colours for this datagram 
	 */
	private StandardPlot2DColours datagramColours;

	private PamAxisFX freqAxis; 
	
	
		
	public FFTScrollBarGraphics(AcousticScrollerFX acousticScroller, FFTDataBlock fftDataBlock){
		this.acousticScroller=acousticScroller; 
		this.fftDataBlock=fftDataBlock; 
//		spectrogramColours=new GeneralSpectrogramColours();
				
		projector=new AcousticScrollerProjector(); 
		
		this.datagramColours= new StandardPlot2DColours(); 
		
		spectrogramPlot=new SpecDatagramPlot(projector, fftDataBlock, datagramColours, 0, this.acousticScroller.isViewer); 
//		channel=PamUtils.getLowestChannel(fftDataBlock.getChannelMap());
		channel=PamUtils.getLowestChannel(fftDataBlock.getSequenceMap());

		//create axis and bind frequencies. 
		createAmplitudeAxis();
				 
	}
	
	public class AcousticScrollerProjector extends TDProjectorFX {

		public AcousticScrollerProjector() {
			super();
			Rectangle windowRect=new Rectangle(); 
			windowRect.widthProperty().bind(acousticScroller.getScrollBarPane().getDrawCanvas().widthProperty());
			windowRect.heightProperty().bind(acousticScroller.getScrollBarPane().getDrawCanvas().heightProperty());
			this.setWindowRect(windowRect); 
		}	

		@Override
		public PamAxisFX getYAxis(){
			return freqAxis; 
		}

		@Override
		public PamAxisFX getTimeAxis(){
			return acousticScroller.getTimeAxis(); 
		}
		
		@Override
		public double getVisibleTime(){
			return acousticScroller.getRangeMillis();
		}
		
		public double getGraphTimePixels(){
			return acousticScroller.getScrollBarPane().getWidth();
		}

	}
	
	private void createAmplitudeAxis(){
		freqAxis = new PamAxisFX(0, 1, 0, 1, 0, 10, PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE, "%4d");
		freqAxis.y1Property().setValue(0);
		freqAxis.y2Property().bind(acousticScroller.getScrollBarPane().heightProperty().divide(2));
		freqAxis.x1Property().bind(acousticScroller.getScrollBarPane().widthProperty());
		freqAxis.x2Property().bind(acousticScroller.getScrollBarPane().widthProperty());
	}
	
	/**
	 * Update the frequency axis based on fft datablock sample rate. 
	 */
	private void updateFreqLimits(){
		freqAxis.minValProperty().setValue(0);
		freqAxis.maxValProperty().setValue(fftDataBlock.getSampleRate()/2);
//		DoubleProperty[] axisVals={freqAxis.minValProperty() , freqAxis.maxValProperty()};
//		spectrogramPlot.setFreqLimits(axisVals);
	}


	@Override
	public PamDataBlock getDataBlock() {
		return fftDataBlock;
	}

	PamDataUnit lastData;

	private Rectangle windowRect;
	@Override
	public void addNewData(PamDataUnit rawData) { 
		try{
			if (rawData.getParentDataBlock()==fftDataBlock
//					&& PamUtils.hasChannel(rawData.getChannelBitmap(), channel) 
					&& PamUtils.hasChannel(rawData.getSequenceBitmap(), channel) 
					&& lastData!=rawData){							
				spectrogramPlot.new2DData((FFTDataUnit) rawData);
				lastData=rawData; 
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void repaint() {
		
		//get the canvas. 
		canvas=acousticScroller.getScrollBarPane().getDrawCanvas();

		//calculate the size of the scrollbar
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(),canvas.getHeight());
		
		windowRect=new Rectangle(0,0, 	canvas.getWidth(), 		canvas.getHeight());

//		System.out.println("Projector: top " + projector.getYPix(24000) +" bottom "+ projector.getYPix(0)+ "  height: " + projector.getHeight() 
//			+ " lims "+	freqAxis.minValProperty().getValue() + "  " + freqAxis.maxValProperty().getValue()); 
		
		//plot the spectrogram. 
		spectrogramPlot.drawSpectrogram(canvas.getGraphicsContext2D(), windowRect, acousticScroller.getOrientation(),
				acousticScroller.getTimeAxis(), acousticScroller.getMinimumMillis(), false);
	}

	@Override
	public String getName() {
		return "Spectrogram";
	}

	@Override
	public void clearStore() {
//		System.out.printf("In clearStore(): ");
		spectrogramPlot.resetForLoad();
		updateFreqLimits(); 
	}

	@Override
	public boolean orderOfflineData() {
		return true;
	}

	@Override
	public void notifyUpdate(int flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColors(Plot2DColours specColors) {
		spectrogramPlot.setSpecColors(specColors);
		spectrogramPlot.reBuildImage();
		spectrogramPlot.rebuildFinished();
	}
	
	@Override
	public Plot2DColours getColors() {
		return spectrogramPlot.getSpecColors(); 
	}
	
	private class SpecDatagramPlot extends Scrolling2DPlotDataFX {

		public SpecDatagramPlot(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
			super(specPlotInfo, iChannel);
		}
		
		public SpecDatagramPlot(AcousticScrollerProjector projector, FFTDataBlock fftDataBlock,
				StandardPlot2DColours dataGramColors, int i, boolean isViewer) {
			super(projector, fftDataBlock, dataGramColors, i, isViewer); 
			// TODO Auto-generated constructor stub
		}

		public void rebuildFinished(){
			acousticScroller.repaint(0);
		}
		
	}

}
