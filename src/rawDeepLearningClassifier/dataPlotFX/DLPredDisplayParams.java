package rawDeepLearningClassifier.dataPlotFX;

import java.io.Serializable;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import javafx.scene.paint.Color;

/**
 * The parameters for the deep learning prediction plot. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLPredDisplayParams implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The line infos. 
	 */
	public LineInfo[] lineInfos; 

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DLPredDisplayParams clone() {
		try {
			return (DLPredDisplayParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
