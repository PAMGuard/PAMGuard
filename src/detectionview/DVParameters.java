package detectionview;

import java.io.Serializable;

public class DVParameters implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	public String detectorName;
	
	public boolean autoFindRaw = true;
	
	public String rawDataName;
	
	public double preSeconds = 1;
	
	public double postSeconds = 1;

	@Override
	public DVParameters clone() {
		try {
			return (DVParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}



}
