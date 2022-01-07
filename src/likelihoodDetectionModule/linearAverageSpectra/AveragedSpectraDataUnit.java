package likelihoodDetectionModule.linearAverageSpectra;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * This is the data type produced by the linearAverageSpectra
 * 
 * @author Dave Flogeras
 *
 * 
 */
public class AveragedSpectraDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {

	double[] spectraData = null;
	
	public AveragedSpectraDataUnit( long timeMilliseconds, int channelBitmap, long startSample, long duration ) {
		super( timeMilliseconds, channelBitmap, startSample, duration );
	}
	
	public void setData( double[] data ) {
		this.spectraData = data;
	}
	
	public double[] getData() {
		return spectraData;
	}
}
