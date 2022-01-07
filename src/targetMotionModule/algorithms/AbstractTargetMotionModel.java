package targetMotionModule.algorithms;

import java.awt.Color;

import PamView.PamSymbol;
import PamView.PamSymbolType;

abstract public class AbstractTargetMotionModel implements TargetMotionModel {

	abstract Color getSymbolColour();
	
	private PamSymbol pamSymbol;

	@Override
	public PamSymbol getPlotSymbol(int iResult) {
		if (pamSymbol == null) {
			pamSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 9, 9, true, getSymbolColour(), getSymbolColour());
		}
		return pamSymbol;
	}


//	public TransformGroup getPlotSymbol3D(Vector3f vector, double[] sizeVector, double minSize) {
//	
//
//		sizeVector = Arrays.copyOf(sizeVector, 3);
//		for (int i = 0; i < 2; i++) {
//			// only loop to 2 so that points can have 0 dimension in z. 
//			sizeVector[i] = Math.max(sizeVector[i], minSize);
//		}
//		sizeVector[2] = Math.max(sizeVector[2], 2);
//		
//		TransformGroup tg = new TransformGroup();
//		tg.setCapability( BranchGroup.ALLOW_DETACH );
//
//		Sphere sphere = new Sphere(5f);
//		//		sphere.setAppearance();
//		Appearance ap = new Appearance();
//		
//		Material mat = new Material();
//
//		Color3f col = new Color3f(getSymbolColour());	
//		mat.setAmbientColor(new Color3f(0.0f,1.0f,1.0f));
//		mat.setDiffuseColor(col);
//		mat.setSpecularColor(col);
//		ap.setMaterial(mat);
//		
//		ColoringAttributes ca = new ColoringAttributes(col, ColoringAttributes.NICEST);	
//		ap.setColoringAttributes(ca);	
//		TransparencyAttributes trans=new TransparencyAttributes(TransparencyAttributes.NICEST,0.2f);
//		ap.setTransparencyAttributes(trans);
//
//		sphere.setAppearance(ap); 
//	
//		Transform3D transform = new Transform3D();
//
//		transform.setScale(new Vector3d(sizeVector));
//		transform.setTranslation(vector);	
//		tg.addChild(sphere);
//		tg.setTransform(transform);
//	
//
//		return tg;
//	}
//	
}
