package fftManager;

import javax.swing.AbstractSpinnerModel;

import PamUtils.PamUtils;

public class FFTLengthModel extends AbstractSpinnerModel {

	int newVal;
	
	FFTLengthModeled host;

	public FFTLengthModel(FFTLengthModeled host) {
		super();
		this.host = host;
	}

	@Override
	public Object getNextValue() {
		int v = getNearestLogLength(host.getFFTLength());
		if (v > 0) {
			newVal = (v * 2);
			host.setFFTLength(newVal);
			return newVal;
		}
		return null;
	}

	@Override
	public Object getPreviousValue() {
		int v = getNearestLogLength(host.getFFTLength());
		if (v > 0) {
			newVal = Math.max(v / 2, 4);
			host.setFFTLength(newVal);
			return newVal;
		}
		return null;
	}


	@Override
	public void setValue(Object arg0) {

		newVal = (Integer) arg0;
		
	}

	@Override
	public Object getValue() {
		return newVal;
	}
	
	protected int getNearestLogLength(int l) {
		if (l < 0) {
			return l;
		}
		l = PamUtils.log2(l);
		return 1<<l;
	}
	
}
