package dataMap.layoutFX;

import java.io.Serializable;

public class DataMapInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String longDataName;

	public String getName() {
		return name;
	}
	
	public String getLongtName() {
		return longDataName;
	}

	public void setLongName(String longDataName) {
		this.longDataName=longDataName; 
	}

	public void setName(String dataName) {
		this.name=dataName;
	}
	
	@Override
	public String toString() {
		return name; 
	}
	
}