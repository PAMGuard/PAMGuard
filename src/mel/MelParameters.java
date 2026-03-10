package mel;

import java.io.Serializable;

public class MelParameters implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	public String dataSource;
	
	public int chanelMap = 0xFF;
	
	public double minFrequency = 1000;
	
	public double maxFrequency = 10000;
	
	public int nMel = 10;

	public double power = 2.0;
	
	/**
	 * Check maxFrequency is <= sample rate. 
	 * @param fs sample rate
	 * @return true if it had to be changed. 
	 */
	public boolean checkSampleRate(double fs) {
		if (maxFrequency == 0 || maxFrequency <= minFrequency || maxFrequency > fs/2.) {
			maxFrequency = fs/2.;
			return true;
		}
		return false;
	}
	

	@Override
	protected MelParameters clone()  {
		try {
			return (MelParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


}
