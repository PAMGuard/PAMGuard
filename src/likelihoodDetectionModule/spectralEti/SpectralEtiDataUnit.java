package likelihoodDetectionModule.spectralEti;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

/**
 * The data type for the output of the spectralEti module.
 * 
 * @author Dave Flogeras
 *
 */
public class SpectralEtiDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit {

	double[] spectraData = null;
	
	public SpectralEtiDataUnit( long timeMilliseconds, int channelBitmap, long startSample, long duration ) {
		super( timeMilliseconds, channelBitmap, startSample, duration );
	}
	
	public void setData( double[] data ) {
		this.spectraData = data;
	}
	
	public double[] getData() {
		return spectraData;
	}
}
