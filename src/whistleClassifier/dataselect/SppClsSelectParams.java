package whistleClassifier.dataselect;

import java.io.Serializable;

/**
 * Data selector params for a single species. 
 * @author dg50
 *
 */
public class SppClsSelectParams implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	public String name;
	public boolean selected;
	public double minScore;
	
	public SppClsSelectParams(String name, boolean selected, double minScore) {
		super();
		this.name = name;
		this.selected = selected;
		this.minScore = minScore;
	}

	@Override
	protected SppClsSelectParams clone() {
		try {
			return (SppClsSelectParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
