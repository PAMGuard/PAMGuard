package ravendata;

import java.io.Serializable;
import java.util.ArrayList;

public class RavenParameters implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	public String importFile;
	
	public double timeOffsetSeconds = 0;
	
	private ArrayList<RavenColumnInfo> extraColumns;

	public ArrayList<RavenColumnInfo> getExtraColumns() {
		if (extraColumns == null) {
			extraColumns = new ArrayList<>();
		}
		return extraColumns;
	}

	public void setExtraColumns(ArrayList<RavenColumnInfo> extraColumns) {
		this.extraColumns = extraColumns;
	}
	
	
}
