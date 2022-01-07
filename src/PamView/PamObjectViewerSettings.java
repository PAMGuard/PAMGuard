package PamView;

import java.awt.Rectangle;
import java.io.Serializable;

public class PamObjectViewerSettings implements Serializable, Cloneable {
	
	static final long serialVersionUID = 1;
	
	Rectangle frameRectangle;
	
	static public final int VIEWBYCONTROLLER = 0;
	static public final int VIEWBYPROCESS = 1;
	
	public int viewStyle = VIEWBYCONTROLLER;
	
	public boolean showProcesslessModules = true;
	
	public boolean showProcesslessObservers = false;

	@Override
	protected PamObjectViewerSettings clone() {
		try{
			return (PamObjectViewerSettings) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
