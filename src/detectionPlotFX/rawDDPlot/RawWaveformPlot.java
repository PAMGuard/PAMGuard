package detectionPlotFX.rawDDPlot;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.WaveformPlot;

/**
 * Plot for any RawDataHolder to show a waveform.  
 * @author Jamie Macaulay
 *
 */
public class RawWaveformPlot extends WaveformPlot<PamDataUnit>{

	public RawWaveformPlot(DetectionPlotDisplay detectionPlotDisplay) {
		super(detectionPlotDisplay);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double[][] getWaveform(PamDataUnit pamDetection) {
		
		RawDataHolder rawDetection = (RawDataHolder) pamDetection; 
		if (pamDetection==null) return null; 
		if (super.getWaveformPlotParams().showFilteredWaveform) {
//			System.out.println("Get filterred waveform. " + super.getWaveformPlotParams().waveformFilterParams.highPassFreq + 
//					" " + super.getWaveformPlotParams().waveformFilterParams.lowPassFreq);
			//seems crazy but have to clone here. The getFilteredWaceData function comapres the waveformfilterparams to
			//see if it was the same as the last filter params. As the reference is the same it always is?...so have 
			//to clone. 
			return rawDetection.getDataTransforms().getFilteredWaveData(super.getWaveformPlotParams().waveformFilterParams.clone());
		}
		else {
			return rawDetection.getWaveData(); 
		}	
	}

	@Override
	public String getName() {
		return "Click Waveform";
	}
	
	@Override
	public double[][] getEnvelope(PamDataUnit pamDetection) {
		
		if (pamDetection == null) {
			return null;
		}
		
		RawDataHolder rawDetection = (RawDataHolder) pamDetection; 

		
		int nchan=PamUtils.getNumChannels(pamDetection.getChannelBitmap());
		double[][] hilbertTransformAll=new double[nchan][];
		for (int i=0; i<PamUtils.getNumChannels(pamDetection.getChannelBitmap()); i++){
			double[] hilbertTransform;
			if (super.getWaveformPlotParams().showFilteredWaveform) {
				hilbertTransform= rawDetection.getDataTransforms().getFilteredAnalyticWaveform(super.getWaveformPlotParams().waveformFilterParams, i);
			}
			else {
				hilbertTransform=rawDetection.getDataTransforms().getAnalyticWaveform(i);
			}
			hilbertTransformAll[i]=hilbertTransform;
		}
		return hilbertTransformAll;
	}

}
