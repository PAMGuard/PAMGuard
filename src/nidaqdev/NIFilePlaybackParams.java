package nidaqdev;

import java.io.Serializable;

public class NIFilePlaybackParams implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;

	public double outputRange = 2.0;

	@Override
	protected NIFilePlaybackParams clone() {
		try {
			return (NIFilePlaybackParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
