package clickDetector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamView.ColourArray.ColourArrayType;

public class ConcatenatedSpectParams  implements Serializable, Cloneable, ManagedParameters { 
	
	public static final long serialVersionUID = 1L;

	public ColourArrayType colourMap = ColourArrayType.HOT;
	
	public void setColourMap(ColourArrayType colourMap) {
		this.colourMap = colourMap;
	}
	
	public ColourArrayType getColourMap() {
		if (colourMap == null) {
			colourMap = ColourArrayType.GREY;
		}
		return colourMap;
	}
	
	public boolean logVal=true;
	
	public double maxLogValS=30.0;
	
	public boolean normaliseAll=false;
	
	@Override
	protected ConcatenatedSpectParams clone()  {
		try {
			if (maxLogValS == 0) {
				maxLogValS = 30.; // will need this the first time to initialise it !
			}
			return (ConcatenatedSpectParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
