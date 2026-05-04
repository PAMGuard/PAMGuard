package loggerForms.cameragrabber;

import java.io.Serializable;
import java.util.HashMap;

public class GrabberParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public int nCameras = 1;
	
	private HashMap<Integer, CameraParams> cameraSets = new HashMap<>();
	
	public String outputFolder;
	
	public boolean foldersByDate;
	
	public boolean timeStampImages;
	
	public boolean autoGrab;
	
	public int autoGrabSeconds = 5;
	
	public boolean autoGrabRandomise = false;

	@Override
	protected GrabberParams clone() {
		try {
			return (GrabberParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Set params for given index. 
	 * @param index
	 * @param cameraParams
	 */
	public void setCameraParams(int index, CameraParams cameraParams) {
		cameraSets.put(index, cameraParams);
	}
	
	/**
	 * Get params for given index. Create new object if params don't already exist. 
	 * @param index
	 * @return
	 */
	public CameraParams getCameraParams(int index) {
		CameraParams p = cameraSets.get(index);
		if (p == null) {
			p = new CameraParams();
			cameraSets.put(index, p);
		}
		return p;
	}
}
