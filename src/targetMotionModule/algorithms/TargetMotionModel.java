package targetMotionModule.algorithms;

import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionResult;
import PamView.PamSymbol;

public interface TargetMotionModel {

	String getName();
	
	String getToolTipText();
	
	boolean hasParameters();
	
	boolean parametersDialog();
	
	TargetMotionResult[] runModel(TargetMotionInformation targetMotionInformation);
	
	public PamSymbol getPlotSymbol(int iResult);
	
//	public TransformGroup getPlotSymbol3D(Vector3f vector, double[] sizeVector, double minSize);
	
	
}
