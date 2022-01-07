package difar.demux;

/**
 * Structure to hold data returned from the native C call
 * @author Doug Gillespie
 *
 */
public class DifarResult {
	/*
	 * 
(JNIEnv *env, jobject obj, jdoubleArray jRawData, jdoubleArray jom,
		jdoubleArray jew, jdoubleArray jns, jdoubleArray jnco_freq,
		jdoubleArray jphase_diff, jbooleanArray jlock_75,
		jbooleanArray jlock_15,	jbooleanArray jnco_out) {
	 */

	double[] om;
	double[] ew;
	double[] ns;
	double[] nco_freq;
	double[] phase_diff;
	boolean[] lock_75;
	boolean[] lock_15;
	boolean[] nco_out;
	
	public DifarResult(int length) {
		om = new double[length];
		ew = new double[length];
		ns = new double[length];
		nco_freq = new double[length];
		phase_diff = new double[length];
		lock_15 = new boolean[length];
		lock_75 = new boolean[length];
		nco_out = new boolean[length];
	}

	/**
	 * @return the ew
	 */
	public double[] getEw() {
		return ew;
	}

	/**
	 * @return the ns
	 */
	public double[] getNs() {
		return ns;
	}

	/**
	 * @return the nco_freq
	 */
	public double[] getNco_freq() {
		return nco_freq;
	}

	/**
	 * @return the phase_diff
	 */
	public double[] getPhase_diff() {
		return phase_diff;
	}

	/**
	 * @return the lock_75
	 */
	public boolean[] getLock_75() {
		return lock_75;
	}

	/**
	 * @return the lock_15
	 */
	public boolean[] getLock_15() {
		return lock_15;
	}

	/**
	 * @return the nco_out
	 */
	public boolean[] getNco_out() {
		return nco_out;
	}

	/**
	 * @return the om
	 */
	public double[] getOm() {
		return om;
	}

	public double[][] getDataArrays() {
		double[][] dataArrays = new double[3][];
		dataArrays[0] = om;
		dataArrays[1] = ew;
		dataArrays[2] = ns;
		
		return dataArrays;
	}

	
}
