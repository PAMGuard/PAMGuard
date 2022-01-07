package map3D.fx;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import map3D.Map3DDisplayComponent;
import pamViewFX.threeD.Xform;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class Test3DDisplayFX {

	private Map3DDisplayComponent map3dDisplayComponent;

	private Scene mainScene;

	private CameraControl cameraControl;

	private Xform xFormWorld = new Xform();

	private Group root = new Group();

	final Xform axisGroup = new Xform();

	private static final double AXIS_LENGTH = 250.0;
	
	private double metresPerPixel = 1.0;

	public Test3DDisplayFX(Map3DDisplayComponent map3dDisplayComponent) {
		this.map3dDisplayComponent = map3dDisplayComponent;
		mainScene = new Scene(root, 300, 300, true);
		mainScene.setFill(Color.GREY);
		root.getChildren().add(xFormWorld);
//		root.setVisible(false);
		cameraControl = new CameraControl(this);
		mainScene.setCamera(cameraControl.getCamera());
		cameraControl.buildCamera();
		buildAxes();
		makeObjects();
		handleKeyboard(mainScene, xFormWorld);
		handleMouse(mainScene, xFormWorld);

		axisGroup.setTranslate(0, 0, 0);
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				centrePlot();
			}
		});

		InvalidationListener listener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				centrePlot();
			}
		};
		mainScene.widthProperty().addListener(listener);
		mainScene.heightProperty().addListener(listener);
	}

	protected void centrePlot() {
//		System.out.printf("Scene Dimensions %3.1f x %3.1f\n", mainScene.getWidth(), mainScene.getHeight());
//		cameraControl.getCameraXform().setTranslate(mainScene.getWidth()/2, mainScene.getHeight()/2);
	}

	public Scene getMainScene() {
		return mainScene;
	}

	/**
	 * @return the map3dDisplayComponent
	 */
	public Map3DDisplayComponent getMap3dDisplayComponent() {
		return map3dDisplayComponent;
	}

	/**
	 * @return the xFormWorld
	 */
	public Xform getxFormWorld() {
		return xFormWorld;
	}

	/**
	 * @return the root
	 */
	public Group getRoot() {
		return root;
	}

	private void makeObjects() {
	       final PhongMaterial redMaterial = new PhongMaterial();
	       redMaterial.setDiffuseColor(Color.BLUE);
	       redMaterial.setSpecularColor(Color.RED);
	       Sphere oxygenSphere = new Sphere(40.0);
	       oxygenSphere.setTranslateX(200);
	       oxygenSphere.setMaterial(redMaterial);
		axisGroup.getChildren().add(oxygenSphere);
		int blockSize = 100000;
		Box surface = new Box(blockSize, blockSize, blockSize);
		surface.setTranslateZ(-blockSize/2);
		PhongMaterial surfaceMaterial = new PhongMaterial();
		Color surfaceCol = new Color(0.5, 1, 1, .02);
		surfaceMaterial.setDiffuseColor(surfaceCol);
		surface.setMaterial(surfaceMaterial);
		axisGroup.getChildren().add(surface);
	}

	private void buildAxes() {
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);

		final Box xAxis = new Box(AXIS_LENGTH, 10, 10);
		final Box yAxis = new Box(1, AXIS_LENGTH, 1);
		final Box zAxis = new Box(1, 1, AXIS_LENGTH);

		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		axisGroup.setVisible(true);
		xFormWorld.getChildren().addAll(axisGroup);
	}

	private static final double CONTROL_MULTIPLIER = 0.1;    
	private static final double SHIFT_MULTIPLIER = 10.0;    
	private static final double MOUSE_SPEED = 1.1;    
	private static final double ROTATION_SPEED = 1.0;    
	private static final double TRACK_SPEED = 0.3;

	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;
	double modifierFactor = 10.;
	private void handleMouse(Scene scene, final Node root) {

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX); 
				mouseDeltaY = (mousePosY - mouseOldY);
//				System.out.printf("Mouse X=%3.1f,Y=%3.1f\n", mousePosX, mousePosY);

				double modifier = 1.0;

				if (me.isControlDown()) {
					modifier = CONTROL_MULTIPLIER;
				} 
				if (me.isShiftDown()) {
					modifier = SHIFT_MULTIPLIER;
				}     
				if (me.isPrimaryButtonDown()) {
					Xform cameraXform = cameraControl.getCameraXform();
					double ax, ay;
					cameraXform.ry.setAngle(ay = cameraXform.ry.getAngle() -
							mouseDeltaX*modifierFactor*modifier*ROTATION_SPEED);  // 
							cameraXform.rx.setAngle(ax = cameraXform.rx.getAngle() +
									mouseDeltaY*modifierFactor*modifier*ROTATION_SPEED);  // -
//					System.out.printf("Camer angles: %3.1f and %3.1f\n", ax, ay);
				}
				else if (me.isSecondaryButtonDown()) {
					PerspectiveCamera camera = cameraControl.getCamera();
					double z = camera.getTranslateZ();
					double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
					camera.setTranslateZ(newZ);
				}
				else if (me.isMiddleButtonDown()) {
					Xform cameraXform2 = cameraControl.getCameraXform2();
					cameraXform2.t.setX(cameraXform2.t.getX() + 
							mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);  // -
							cameraXform2.t.setY(cameraXform2.t.getY() + 
									mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);  // -
				}
			}
		}); // setOnMouseDragged
		scene.setOnScroll((ScrollEvent event) -> {
			double zoomFactor = .95;
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
            	zoomFactor = 1./zoomFactor;
            }
            axisGroup.setScaleX(axisGroup.getScaleX()*zoomFactor);
            axisGroup.setScaleY(axisGroup.getScaleY()*zoomFactor);
            axisGroup.setScaleZ(axisGroup.getScaleZ()*zoomFactor);
			
		});
	} //handleMouse
	private void handleKeyboard(Scene scene, final Node root) {

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
               switch (event.getCode()) {
                   case Z:
   					Xform cameraXform2 = cameraControl.getCameraXform2();
					Xform cameraXform = cameraControl.getCameraXform();
                       cameraXform2.t.setX(0.0);
                       cameraXform2.t.setY(0.0);
                       cameraXform.ry.setAngle(CameraControl.CAMERA_INITIAL_Y_ANGLE);
                       cameraXform.rx.setAngle(CameraControl.CAMERA_INITIAL_X_ANGLE);
                       break;
                   case X:
                        axisGroup.setVisible(!axisGroup.isVisible());
                        break;
                    case V:
//                       moleculeGroup.setVisible(!moleculeGroup.isVisible());
                       break;
               } // switch
            } // handle()
        });  // setOnKeyPressed
    }  //  handleKeyboard()
}
