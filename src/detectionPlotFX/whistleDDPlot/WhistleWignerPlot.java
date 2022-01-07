package detectionPlotFX.whistleDDPlot;

import PamguardMVC.PamRawDataBlock;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawDataOrder;
import detectionPlotFX.plots.WignerPlot;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import whistlesAndMoans.ConnectedRegionDataUnit;

public class WhistleWignerPlot extends WignerPlot<ConnectedRegionDataUnit> {

	/**
	 * Observer of loaded data. 
	 */
	private RawDataOrder rawDataOrder;
	
	/**
	 * 
	 * Constructor for a whistle wigner plot. 
	 * @param displayPlot
	 */
	public WhistleWignerPlot(DetectionPlotDisplay displayPlot) {
		super(displayPlot);
		rawDataOrder= new WignerRawDataOrder(); 
		super.getWignerParameters().limitLength=false; //always plot the entire whistle 
	}


	/**
	 * Get the waveforms from a PamDetection
	 * @param pamDetection the detection
	 * @return the waveform for each channel 
	 */
	@Override
	public double[] getWaveform(ConnectedRegionDataUnit pamDetection, int chan){
	
		//have to load waveform on a new thread. 
		PamRawDataBlock rawDataBlock=(PamRawDataBlock) pamDetection.getParentDataBlock().getRawSourceDataBlock();
		long timeMillis= pamDetection.getTimeMilliseconds(); 
		long dataStart = timeMillis;
		//Note: do not try to add a float and a long. Weird things happen
		long dataEnd = pamDetection.getEndTimeInMilliseconds();

//		System.out.println("dataStart: " + dataStart + " dataEnd: " + dataEnd + 
//				" pamDetection.getDurationInMilliseconds() " + pamDetection.getDurationInMilliseconds() ); 
		//reset the observer for a load. 
		rawDataOrder.startRawDataLoad(pamDetection, 0, chan);
	
		//thread has started
//		System.out.println("WhistleWignerPlot: Raw data has started loading: " + (dataEnd-dataStart) + " millis");
		
		return null; 

	}

	
	@Override
	public Pane getSettingsPane() {
		super.getSettingsPane(); //make sure the pane is created. HACK...gah. 
		this.getWignerSettingsPane().setDynamicColourChanging(false);
		return super.getSettingsPane(); 
	}


//	/**
//	 * Check whether the wigner plot needs recalculated with a new settings. 
//	 * @param wignerParameters
//	 */
//	public void checWignerRecalc(WignerPlotParams wignerParameters) {
//		super.checWignerRecalc(wignerParameters);
//		//additionally changing channel will require raw data to be loaded up again. 
//		if (wignerParameters.chan != this.getWignerParameters().chan) this.needRawData=true;
//
//		wignerImage=null; 
//	}

	

	@Override
	public String getName() {
		return "Whistle Wigner";
	}
	
	class WignerRawDataOrder extends RawDataOrder {

		@Override
		public void dataLoadingFinished(double[] rawData) {
//			System.out.println("RAW DATA FINISHED LOADING: " + rawData.length);
			//now need to repaint
			setWignerWaveform(rawData); 
			setNeedsRecalc(true); //now need to calc wigner data  
			//must move onto fx thread
			Platform.runLater(()->{
				reDrawLastUnit();
			});
			
		}
		
	}

}
