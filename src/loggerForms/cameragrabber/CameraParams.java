package loggerForms.cameragrabber;

import java.awt.Dimension;
import java.io.Serializable;

/**
 * Params for a single camera. 
 */
public class CameraParams implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public String cameraName;
	
	public int cameraIndex;

	public Dimension dimension;
	
	public String imageInitials;

	@Override
	protected CameraParams clone()  {
		try {
			return (CameraParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
