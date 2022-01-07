package fftManager;

import java.util.Comparator;

/**
 * Comparator to compare complex numbers first by real part, then by imaginary part. 
 * @author dg50
 *
 */
public class ComplexComparator implements Comparator<Complex> {

	@Override
	public int compare(Complex o1, Complex o2) {
		int ans = Double.compare(o1.real, o2.real);
		if (ans == 0) {
			ans = Double.compare(o1.imag, o2.imag);
		}
		return ans;
	}

}
