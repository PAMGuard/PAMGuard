package targetMotionOld.algorithms;

import java.awt.Color;

import targetMotionOld.TargetMotionModel;

//import com.sun.j3d.utils.geometry.Box;
//import com.sun.j3d.utils.geometry.Sphere;

import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataUnit;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
abstract public class AbstractTargetMotionModel<T extends PamDataUnit> implements TargetMotionModel<T> {

	abstract Color getSymbolColour();
	
	private PamSymbol pamSymbol;

	@Override
	public PamSymbol getPlotSymbol(int iResult) {
		if (pamSymbol == null) {
			pamSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 9, 9, true, getSymbolColour(), getSymbolColour());
		}
		return pamSymbol;
	}

//	/**
//	 * 
//	 */
//	public TransformGroup getPlotSymbol3D(Vector3f locResult, LocaliserError localiserError, double minSize, EventRotator eventRotator) {
//
//		//create the transform group to hold the shape. 
//		TransformGroup tg = new TransformGroup();
//		tg.setCapability( BranchGroup.ALLOW_DETACH );
//
//		//create transfor to translate and rotate shape. 
//		Transform3D transform = new Transform3D();
//
//
//		//create the appearance of the whatever shape is going to be used to represent the loclaisation
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
//		if (localiserError instanceof SimpleError){
//			
//			SimpleError simpleError=(SimpleError) localiserError;
//			
//			double depthError; 
//			if (simpleError.getDepthError()==null) depthError=2; 
//			else depthError =simpleError.getDepthError(); 
//			
//			Box box=new Box(simpleError.getPerpError().floatValue(), simpleError.getParallelError().floatValue(), (float) depthError,  ap); 
//		
//			//System.out.println("Loc result "+ locResult.getX()+ " "+locResult.getY()+" "+ locResult.getZ());
//			
//			transform.setTranslation(locResult);
//			
//			
//			//heading, roll, pitch
//			PamQuaternion quaternion=new PamQuaternion(simpleError.getPerpAngle()+eventRotator.getReferenceAngle()+Math.PI/2,0,0); 
//			
//			//PamQuaternion quaternion=new PamQuaternion(Math.toRadians(80)+eventRotator.getReferenceAngle()+Math.PI/2,0, Math.toRadians(20)); 
//
//			transform.setRotation(new Quat4d(quaternion.getX(),quaternion.getY(),quaternion.getZ(),quaternion.getW()));
//			
//			//transform.rotZ(simpleError.getPerpAngle()+eventRotator.getReferenceAngle()+Math.PI/2);
//			
//			tg.addChild(box);
//			tg.setTransform(transform);
//			
//			return tg; 
//
//		}
//		else if (localiserError instanceof EllipticalError){
//
//			EllipticalError ellipsticalError=(EllipticalError) localiserError;
//
//			double[] sizeVector = Arrays.copyOf(ellipsticalError.getEllipseDim(), 3);
//
////			System.out.println("GetPlotSymbol3D Size: "+sizeVector[0] +" "+sizeVector[1] + " "+sizeVector[2]);
////			System.out.println("GetPlotSymbol3D Angles: "+ Math.toDegrees(ellipsticalError.getAngles()[0]) +" "+
////					Math.toDegrees(ellipsticalError.getAngles()[1]) + " "+Math.toDegrees(ellipsticalError.getAngles()[2]));
//
//			for (int i = 0; i < 2; i++) {
//				// only loop to 2 so that points can have 0 dimension in z. 
//				sizeVector[i] = Math.max(sizeVector[i], minSize);
//				if (Double.isInfinite(sizeVector[i]) || Double.isNaN(sizeVector[i])){
//					sizeVector[i]= 100f; 
//				}
//			}
//			sizeVector[2] = Math.max(sizeVector[2], 2);
//
//
//			Sphere sphere = new Sphere(1f);
//			sphere.setAppearance(ap); 
//
//			transform.setScale(new Vector3d(sizeVector));
//			transform.setTranslation(locResult);
//
//			//heading, roll, pitch
//			PamQuaternion quaternion=new PamQuaternion(ellipsticalError.getAngles()[0]+eventRotator.getReferenceAngle()+Math.PI/2,ellipsticalError.getAngles()[2], -ellipsticalError.getAngles()[1]);
//			
//			//PamQuaternion quaternion=new PamQuaternion(Math.toRadians(80)+eventRotator.getReferenceAngle()+Math.PI/2,0, Math.toRadians(20)); 
//
//			transform.setRotation(new Quat4d(quaternion.getX(),quaternion.getY(),quaternion.getZ(),quaternion.getW()));
//			tg.addChild(sphere);
//			tg.setTransform(transform);
//
//			return tg;
//		}
//		return null; 
//	}
}
