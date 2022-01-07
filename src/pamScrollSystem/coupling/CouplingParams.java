package pamScrollSystem.coupling;

import java.io.Serializable;

/**
 * Options for improved display coupling and inter-display zooming. 
 * @author Doug
 *
 */
public class CouplingParams implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	public static final int COUPLING_NONE = 0;
	public static final int COUPLING_START = 1;
	public static final int COUPLING_MIDDLE = 2;
	public static final int COUPLING_RANGE = 3;
	
	public static final String[] names = {"No coupling", "Couple scroller starts", 
			"Couple scroller middles", "Couple scroller range"};
	public static final String[] tips = {"Scrollers will not be coupled",
			"Start times of scrollers will follow each other",
			"Middle / centre times of scrollers will follow each other",
			"Scrollers will maintain overlap, but more more independently"};
	
	public int couplingType = COUPLING_RANGE;

	@Override
	protected CouplingParams clone() {
		try {
			return (CouplingParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
