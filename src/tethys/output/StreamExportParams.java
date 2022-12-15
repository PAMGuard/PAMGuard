package tethys.output;

import java.io.Serializable;

/**
 * Parameters controlling export of a single stream. 
 * Starts just with a boolean 'selected', but may grow. 
 * These all contain data names rather than references to a Datablock so that 
 * they can be serialised. 
 * @author dg50
 *
 */
public class StreamExportParams implements Serializable {

	public static final long serialVersionUID = 1L;

	public StreamExportParams(String longDataName, boolean selected) {
		super();
		this.longDataName = longDataName;
		this.selected = selected;
	}

	public String longDataName;
	
	public boolean selected;

}
