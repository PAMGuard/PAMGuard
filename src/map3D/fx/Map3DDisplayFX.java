package map3D.fx;

import java.util.ArrayList;


import Array.ArrayManager;
import Array.Hydrophone;
import Array.PamArray;
import PamUtils.PamCalendar;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Scale;
import pamMaths.PamVector;
import pamViewFX.fxNodes.PamBorderPane;

public class Map3DDisplayFX {

	private Scene mainScene;

	private MapGroup earthGroup;
	
	private MapGroup cameraGroup;
	
	private MapGroup movingGroup;

	private PerspectiveCamera camera;

	private int midXAngle = 270;
	
	private StackPane stackPane;
	
	private SubScene subScene;
	
	Canvas canvas;

	public Map3DDisplayFX() {		
		earthGroup = new MapGroup();
		subScene = new SubScene(earthGroup, 100, 100);
		mainScene = new Scene(stackPane = new StackPane(subScene));
		stackPane.getChildren().add(canvas = new Canvas(100, 100));
//		mainScene = new Scene(earthGroup = new MapGroup(), 100, 100, true, SceneAntialiasing.BALANCED);
		mainScene.setFill(Color.GRAY);
		camera = new PerspectiveCamera(false);
		
		camera.setTranslateZ(-1000);
		cameraGroup = new MapGroup();
		cameraGroup.getChildren().add(camera);
		earthGroup.getChildren().add(cameraGroup);
		subScene.setCamera(camera);
		cameraGroup.rx.setAngle(midXAngle);
		
		
		movingGroup = new MapGroup();
		earthGroup.getChildren().add(movingGroup);
//		earthGroup.getChildren().add(cameraGroup);
//		camera.setTranslateX(10000);
		//		camera.setTranslateX(-400); // makes no difference !
		//		camera.setTranslateY(0);

		addFixedObjects();
		
		setupMouse();

		InvalidationListener listener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				centrePlot();
			}

		};
		mainScene.widthProperty().addListener(listener);
		mainScene.heightProperty().addListener(listener);
		

//        canvas = new Canvas(1, 1);
//        stackPane.getChildren().add(canvas);
//        canvas.heightProperty().bind(stackPane.heightProperty());
//        canvas.widthProperty().bind(stackPane.widthProperty());
//        canvas.setOpacity(.1);
//        canvas.getGraphicsContext2D()
	}

	public Scene getMainScene() {
		return mainScene;
	}
	
	private void centrePlot() {
		subScene.setWidth(mainScene.getWidth());
		subScene.setHeight(mainScene.getWidth());
		earthGroup.t.setX(mainScene.getWidth()/2);
		earthGroup.t.setY(mainScene.getHeight()/2);		
        paintMovingObjects();
	}

	private void addFixedObjects() {
		earthGroup.getChildren().clear();
		earthGroup.getChildren().add(movingGroup);
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.RED);
//		redMaterial.setSpecularColor(Color.RED);
		Sphere sphere = new Sphere(40.0);
		sphere.setOpacity(.01);
//		sphere.setTranslateX(100);
//		sphere.setTranslateZ(100);
		sphere.setMaterial(redMaterial);
		
		
		Cylinder cylinder = new Cylinder(20, 100);
		PhongMaterial sphereMaterial = new PhongMaterial();
		Color spCol = new Color(1, 0, 0, 1);
		sphereMaterial.setDiffuseColor(spCol);
//		sphereMaterial.setSpecularColor(spCol);
		cylinder.setMaterial(sphereMaterial);
		cylinder.setOpacity(.001);

		/*
		 * Try to work out a size for the sea surface. 
		 */
		double camDist = camera.getTranslateZ();
		double fov = camera.getFieldOfView();
		double focalLength = mainScene.getHeight()/2./Math.tan(Math.toRadians(fov/2));
		double blockSize = Math.abs(camDist);
		double pixsPerMetre = Math.sqrt(mainScene.getHeight() / (Math.abs(camDist) / Math.tan(Math.toRadians(fov/2))*2));
//		camera.getf
//		System.out.printf("Sea surface dimension = %3.1f, fov %3.1f, camera at %3.1f,%3.1f,%3.1f, clips %3.1f %3.1f, ppm %3.2f\n", 
//				blockSize, fov, camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ(), camera.getNearClip(), camera.getFarClip(), pixsPerMetre);
		Box surface = new Box(blockSize, blockSize, 1);
		surface.setTranslateZ(-1/2);
		PhongMaterial surfaceMaterial = new PhongMaterial();
		Color surfaceCol = new Color(0.5, 1, 1, .01);
		surfaceMaterial.setDiffuseColor(surfaceCol);
		surfaceMaterial.setSpecularColor(surfaceCol);
		surface.setMaterial(surfaceMaterial);
		
//		mapGroup.t.setX(200);
//		mapGroup.t.setY(200);
		

		PhongMaterial phoneMaterial = new PhongMaterial();
		Color phoneCol = Color.BLUE;
		phoneMaterial.setDiffuseColor(phoneCol);
		phoneMaterial.setSpecularColor(phoneCol);
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int nPhones = currentArray.getHydrophoneCount();
		long now = PamCalendar.getTimeInMillis();
		for (int i = 0; i < nPhones; i++) {
			PamVector phonVec = currentArray.getAbsHydrophoneVector(i, now);
			double phoneSz = Math.max(.0, 5./pixsPerMetre); 
			Sphere pS = new Sphere(phoneSz);
			pS.setMaterial(phoneMaterial);
			pS.setTranslateX(phonVec.getElement(0));
			pS.setTranslateY(phonVec.getElement(1)*10);
			pS.setTranslateZ(phonVec.getElement(2)-100);
			earthGroup.getChildren().add(pS);
		}

		earthGroup.getChildren().addAll(surface, cylinder);
	}
	
	private void paintMovingObjects() {
//		canvas.setTranslateX(-canvas.getWidth()/2*0);
//		canvas.setTranslateY(-canvas.getHeight()/2*0);
//		GraphicsContext gc = canvas.getGraphicsContext2D();
//		gc.setFill(new Color(0,1,1,.1));
//		gc.setStroke(Color.RED);
//		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//		gc.setFill(Color.RED);
//		gc.strokeLine(0, 0, 200, 200);
		movingGroup.getChildren().clear();
		Line line = new Line(0, 0, 100, 100);
		movingGroup.getChildren().add(line);
	}

	double mouseOldX;
	double mouseOldY;
	private void setupMouse() {
		// TODO Auto-generated method stub
		mainScene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});
		mainScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				double mouseX = me.getSceneX();
				double mouseY = me.getSceneY();
				double diffX = mouseX-mouseOldX;
				double diffY = mouseY-mouseOldY;
				if (me.isSecondaryButtonDown() || me.isControlDown()) {
					cameraGroup.t.setX(cameraGroup.t.getX() + diffX);
					cameraGroup.t.setY(cameraGroup.t.getY() - diffY);
				}
				else if (me.isPrimaryButtonDown()) {
					cameraGroup.rz.setAngle(cameraGroup.rz.getAngle()+diffX);
					double newX = cameraGroup.rx.getAngle()+diffY;
					newX = Math.min(midXAngle+45, Math.max(midXAngle-90, newX));
					cameraGroup.rx.setAngle(newX);
				}
				mouseOldX = mouseX;
				mouseOldY = mouseY;
			}
		});
		mainScene.setOnScroll((ScrollEvent event) -> {
			double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
            	zoomFactor = 1./zoomFactor;
            }
//            Scale s = earthGroup.s;
//            s.
//            earthGroup.setScaleX(earthGroup.getScaleX()*zoomFactor);
//            earthGroup.setScaleY(earthGroup.getScaleY()*zoomFactor);
//            earthGroup.setScaleZ(earthGroup.getScaleZ()*zoomFactor);
            camera.setTranslateZ(camera.getTranslateZ()*zoomFactor);
            addFixedObjects();
            paintMovingObjects();
			
		});
	}
}
