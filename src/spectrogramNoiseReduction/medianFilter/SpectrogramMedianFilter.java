package spectrogramNoiseReduction.medianFilter;

import java.io.Serializable;

import org.w3c.dom.Element;

import PamUtils.complex.ComplexArray;
import spectrogramNoiseReduction.SpecNoiseDialogComponent;
import spectrogramNoiseReduction.SpecNoiseMethod;
import spectrogramNoiseReduction.layoutFX.SpecNoiseNodeFX;
import whistlesAndMoans.MedianFilter;

import fftManager.Complex;
import fftManager.FFTDataUnit;

public class SpectrogramMedianFilter extends SpecNoiseMethod {

	MedianFilterParams medianFilterParams = new MedianFilterParams();
	
	MedianFilterDialogBits medianFilterDialogBits;
	
	MedianFilter medianFilter;
	
	/**
	 * The FX dialog bits
	 */
	private MedianFilterNodeFX medianFilterNodeFX;

	
	public SpectrogramMedianFilter() {
		super();
		medianFilter = new MedianFilter();
		medianFilterDialogBits = new MedianFilterDialogBits(this);
	}

	@Override
	public SpecNoiseDialogComponent getDialogComponent() {
		return medianFilterDialogBits;
	}

	@Override
	public String getName() {
		return "Median Filter";
	}
	
	@Override
	public String getDescription() {
		return "<html>Within each spectrogram slice, the <p>"+
		             "median value about each point is <p>"+
		             "taken and subtracted from that point</html>";
	}

	@Override
	public int getDelay() {
		return 0;
	}
	
	@Override
	public Serializable getParams() {
		return medianFilterParams;
	}

	@Override
	public boolean initialise(int channelMap) {
		// don't need to do anything here. 
		return medianFilterParams.filterLength > 0;
	}

	private double[] medData; // temp array for real median filter input
	private double[] filterOut; // temp array for real median filter input

	@Override
	public boolean runNoiseReduction(FFTDataUnit fftDataUnit) {
		if (medianFilterParams.filterLength <= 0) {
			return false;
		}
		// run the median filter
		ComplexArray fftData = fftDataUnit.getFftData();
		
		medData = checkAlloc(medData, fftData.length());
		filterOut = checkAlloc(filterOut, fftData.length());
		
		for (int m = 0; m < fftData.length(); m++) {
			medData[m] = fftData.mag(m);
		}
		medianFilter.medianFilter(medData, filterOut, medianFilterParams.filterLength);
		for (int m = 0; m < fftData.length(); m++) {
			if (filterOut[m] > 0) {
				fftData.internalTimes(m, 1./filterOut[m]);
			}
		}
		return true;
	}
	
	
	private double[] checkAlloc(double[] array, int len) {
		if (array == null || array.length != len) {
			array = new double[len];		}
		
		return array;
	}

	@Override
	public boolean setParams(Serializable noiseParams) {
		try {
			medianFilterParams = (MedianFilterParams) noiseParams;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public MedianFilterParams getMedianFilterParams() {
		return medianFilterParams;
	}

	@Override
	public SpecNoiseNodeFX getNode() {
		if (medianFilterNodeFX==null) {
			medianFilterNodeFX= new MedianFilterNodeFX(this); 
		}
		return medianFilterNodeFX;
	}
}
