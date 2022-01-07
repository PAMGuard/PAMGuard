package userDisplay;

import java.awt.Rectangle;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

abstract public class UserFrameParameters implements Cloneable, Serializable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8644304909594229770L;
	
	public Rectangle boundingRectangle = new Rectangle();
	
//	@Override
//	protected UserFrameParameters clone()  {
//		try {
//			return (UserFrameParameters) super.clone();
//		}
//		catch (CloneNotSupportedException Ex) {
//			Ex.printStackTrace()
//		}
//		return null;
//	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
