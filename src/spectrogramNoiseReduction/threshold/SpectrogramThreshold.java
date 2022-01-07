package spectrogramNoiseReduction.threshold;

import java.io.Serializable;

import org.w3c.dom.Element;

import PamUtils.complex.ComplexArray;
import spectrogramNoiseReduction.SpecNoiseDialogComponent;
import spectrogramNoiseReduction.SpecNoiseMethod;
import spectrogramNoiseReduction.layoutFX.SpecNoiseNodeFX;
import fftManager.Complex;
import fftManager.FFTDataUnit;

public class SpectrogramThreshold extends SpecNoiseMethod{


	public static final int OUTPUT_BINARY = 0;
	public static final int OUTPUT_INPUT = 1;
	public static final int OUTPUT_RAW = 2;
	
	protected ThresholdParams thresholdParams = new ThresholdParams();
	
	private double powerThreshold;
	
	private ThresholdDialogComponent thresholdDialogComponent;
	
	/**
	 * FX bits of the dialog. 
	 */
	private ThresholdNodeFX thresholdNodeFX;
	
	public SpectrogramThreshold() {
		thresholdDialogComponent = new ThresholdDialogComponent(this);
	}
	
	@Override
	public SpecNoiseDialogComponent getDialogComponent() {
		return thresholdDialogComponent;
	}

	@Override
	public String getName() {
		return "Thresholding";
	}

	@Override
	public String getDescription() {
		return "<html>A threshold is applied and all data<p>" +
				"falling below that threshold set to 0</html>";
	}

	@Override
	public int getDelay() {
		return 0;
	}
	
	@Override
	public Serializable getParams() {
		return thresholdParams;
	}

	@Override
	public boolean initialise(int channelMap) {
		powerThreshold = Math.pow(10.,thresholdParams.thresholdDB/10.);
		return true;
	}

	@Override
	public boolean runNoiseReduction(FFTDataUnit fftDataUnit) {
		ComplexArray fftData = fftDataUnit.getFftData();
		for (int i = 0; i < fftData.length(); i++) {
			if (fftData.magsq(i) < powerThreshold) {
				fftData.set(i,0,0);
			}
			else if (thresholdParams.finalOutput != OUTPUT_INPUT) {
				fftData.set(i,1,0);
			}
		}
		return true;
	}
	
	/**
	 * go through an array of other data, and 
	 * copy data that's in earlyData into thresholdData
	 * if the threhsoldData is > 0;
	 * @param fftData data to pick from (generally raw input fft data to noise process)
	 * @param binaryChoice output from runNoiseReduction()
	 */
	public void pickEarlierData(ComplexArray fftData, ComplexArray complexArray) {
		for (int i = 0; i < fftData.length(); i++) {
			if (complexArray.getReal(i) > 0) {
				complexArray.set(i, fftData.getReal(i), fftData.getImag(i));
			}
		}
	}

	@Override
	public boolean setParams(Serializable noiseParams) {
		try {
			thresholdParams = (ThresholdParams) noiseParams;
		}
		catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ThresholdParams getThresholdParams() {
		return thresholdParams;
	}

	@Override
	public SpecNoiseNodeFX getNode() {
		if (thresholdNodeFX==null) {
			thresholdNodeFX= new ThresholdNodeFX(this);
		}
		return thresholdNodeFX;
	}
}
