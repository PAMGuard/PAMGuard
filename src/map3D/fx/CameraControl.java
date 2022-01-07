package map3D.fx;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import pamViewFX.threeD.Xform;

/**
 * Class for controlling everything to do with the FX Camera observing the scene. 
 * @author Doug Gillespie
 *
 */
public class CameraControl {

	/**
	 * @return the cameraXform
	 */
	public Xform getCameraXform() {
		return cameraXform;
	}

	/**
	 * @return the cameraXform2
	 */
	public Xform getCameraXform2() {
		return cameraXform2;
	}

	/**
	 * @return the cameraXform3
	 */
	public Xform getCameraXform3() {
		return cameraXform3;
	}

	private PerspectiveCamera camera = new PerspectiveCamera();
	private Test3DDisplayFX map3dDisplayFX;

    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    static final double CAMERA_INITIAL_DISTANCE = -10000;
    static final double CAMERA_INITIAL_X_ANGLE = 120.0;
    static final double CAMERA_INITIAL_Y_ANGLE = 10.0;
    static final double CAMERA_NEAR_CLIP = 0.1;
    static final double CAMERA_FAR_CLIP = 10000.0;

	public CameraControl(Test3DDisplayFX map3dDisplayFX) {
		this.map3dDisplayFX = map3dDisplayFX;
		
	}
	
	public void buildCamera() {
		Group root = map3dDisplayFX.getRoot();        
		root.getChildren().add(cameraXform);  
		cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);
 
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
	}

	/**
	 * @return the camera
	 */
	public PerspectiveCamera getCamera() {
		return camera;
	}

	/**
	 * @return the map3dDisplayFX
	 */
	public Test3DDisplayFX getMap3dDisplayFX() {
		return map3dDisplayFX;
	}

	 
}
