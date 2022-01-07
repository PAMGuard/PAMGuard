package videoRangePanel;

import java.io.Serializable;

public class VRHeightData implements Serializable, Cloneable {

	static public final long serialVersionUID = 0;
	
	public String name;
	
	public double height;
	
	public VRHeightData() {
		
	}

	public VRHeightData(String name, double height) {
		this.name=name;
		this.height=height; 
	}

	@Override
	public VRHeightData clone() {
		try {
			return (VRHeightData) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void update(VRHeightData newData) {
		name = new String(newData.name);
		height = newData.height;
	}

	@Override
	public String toString() {
		return String.format("%s %.2fm", name, height);
	}
	
}
