package deepWhistle;

import java.util.List;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataUnit;

public class DummyFFTMask implements PamFFTMask {

	private static final double FFT_MASK_VAL = 0.1;


	@Override
	public boolean initMask() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<FFTDataUnit> applyMask(List<FFTDataUnit> batch) {

		//System.out.println("MaskedFFTProcess: processing batch of size START "+batch.get(0).getFftData().getReal(0));

		//perform the processing
		ComplexArray out = batch.get(0).getFftData();
		if (out == null) {
			System.err.println("MaskedFFTProcess: no FFT data in first unit of batch");
			return null;
		}

		double[][] mask = getMask(out.length(), batch.size());

		//now apply the mask to each unit
		for (int i = 0; i < batch.size(); i++) {
			out = batch.get(i).getFftData();
			if (out == null) {
				System.err.println("MaskedFFTProcess: no FFT data in unit "+i+" of batch");
				continue;
			}
			for (int j = 0; j < out.length(); j++) {

				//to apply a mask must multiply both real and imaginary parts by the mask value
				double re = out.getReal(j) * mask[i][j];
				out.setReal(j, re);
				double im = out.getImag(j) * mask[i][j];
				out.setImag(j, im);
			}

		}

		//System.out.println("MaskedFFTProcess: processing batch of size DONE "+batch.get(0).getFftData().getReal(0));


		return batch;
	}
	

	/**
	 * Dummy mask - all values as  number
	 * @param n number of frequency bins
	 * @param m number of time slices
	 * @return
	 */
	public double[][] getMask(int n, int m) {
		double[][] mask = new double[m][n];
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				mask[i][j] = FFT_MASK_VAL;
			}
		}
		return mask;
	}
}
