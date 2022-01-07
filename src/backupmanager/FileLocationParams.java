package backupmanager;

import java.io.Serializable;

/**
 * This is generally used as a temporary class for calling Copy and move actions since
 * by necessity the source and destination locations are held in different places. This 
 * brings them together for a call to the dialog, then they can be split again when the 
 * dialog has closed. 
 * @author dg50
 *
 */
public class FileLocationParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	private FileLocation sourceLocation;
	
	private FileLocation destLocation;

	public FileLocationParams(FileLocation sourceLocation, FileLocation destLocation) {
		super();
		this.sourceLocation = sourceLocation;
		this.destLocation = destLocation;
	}

	/**
	 * @return the sourceLocation
	 */
	public FileLocation getSourceLocation() {
		return sourceLocation;
	}

	/**
	 * @param sourceLocation the sourceLocation to set
	 */
	public void setSourceLocation(FileLocation sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	/**
	 * @return the destLocation
	 */
	public FileLocation getDestLocation() {
		return destLocation;
	}

	/**
	 * @param destLocation the destLocation to set
	 */
	public void setDestLocation(FileLocation destLocation) {
		this.destLocation = destLocation;
	}

	@Override
	protected FileLocationParams clone() {
		try {
			return (FileLocationParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
