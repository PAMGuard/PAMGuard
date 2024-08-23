package ravendata;

import java.io.Serializable;

/**
 * Information on a Raven table non standard column. 
 * @author dg50
 *
 */
public class RavenColumnInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public int ravenTableIndex;
	
	public String name;
	
	public int maxStrLength;
	
	public Integer sqlType;

	public RavenColumnInfo(int ravenTableIndex, String name) {
		super();
		this.ravenTableIndex = ravenTableIndex;
		this.name = name;
	}

	public RavenColumnInfo(String name, int maxStrLength, Integer sqlType) {
		super();
		this.name = name;
		this.maxStrLength = maxStrLength;
		this.sqlType = sqlType;
	}
	
}
