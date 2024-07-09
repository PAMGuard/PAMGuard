package Acquisition.sud;

import java.io.Serializable;


/**
 * Parameters for sud file extraction. 
 */
public class PamSudParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Zero padding fills gaps in sud files with zeros - these gaps are usually due
	 * to errors in the recording hardware.Without zero pad then time drift within a
	 * file can be difficult to predict, however zero padding means the sample
	 * numbers in other files e.g. csv sensor files will not align.
	 */
	public boolean zeroPad = true; 

	@Override
	public PamSudParams clone() {
		try {
			PamSudParams ap = (PamSudParams) super.clone();
			
			return ap;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}
}
