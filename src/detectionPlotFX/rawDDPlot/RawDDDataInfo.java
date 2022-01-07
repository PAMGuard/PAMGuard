package detectionPlotFX.rawDDPlot;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;

/**
 * Detection Display dataInfo for any rawdata holder. This deals with drawing  waveforms, frequency, wigner plots etc. 
 * @author Jamie Macaulay
 *
 */
public class RawDDDataInfo extends DDDataInfo<PamDataUnit> {
	

	public RawDDDataInfo(PamDataBlock datablock,
			DetectionPlotDisplay displayPlot) {
		super(displayPlot,  datablock);
		
		//add the various click plots
		super.addDetectionPlot(new RawWaveformPlot(displayPlot));
		super.addDetectionPlot(new RawSpectrumPlot(displayPlot));
		super.addDetectionPlot(new RawWignerPlot(displayPlot));
		super.addDetectionPlot(new RawHolderFFTPlot(displayPlot, displayPlot.getDetectionPlotProjector()));

		super.setCurrentDetectionPlot(0);
		
	}





}
