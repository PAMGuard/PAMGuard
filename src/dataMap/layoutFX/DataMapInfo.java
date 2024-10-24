package dataMap.layoutFX;

import java.io.Serializable;

/**
 * Holds information whcih can identify a data map. 
 */
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
	

    @Override
    public int hashCode() {
    	//need to make sure this provides a unique hash code or things go wrong...
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((longDataName == null) ? 0 : longDataName.hashCode());
        return result;
    }

	@Override
	public boolean equals(Object dataMapInfo1) {
		if (dataMapInfo1==null) return false;
		DataMapInfo dataMapInfo = (DataMapInfo) dataMapInfo1;
		if (this.longDataName.equals(dataMapInfo.longDataName) && this.name.equals(dataMapInfo.name)) {
			return true;
		}
		else return false;
	}
	
}