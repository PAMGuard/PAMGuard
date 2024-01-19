package Array.layoutFX;

import pamViewFX.fxNodes.PamBorderPane;


import java.util.ArrayList;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.composites.PolyLine3D;

import Array.Hydrophone;
import Array.PamArray;
import Array.Streamer;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Create a 3D visualisation of the array.
 * <p>
  PAMGUARD co-rdinate system is
 * <p>
 * x points right
 * <p>
 * y points north or into the screen
 * <p>
 * z is height and points up
 * <p><p>
 * This is different from the JavAFX 3D system in which
 * <p>
 * x points right
 * <p>
 * y points down
 * <p>
 * z points into the screen
 * <p>
 * Thus the source code for this class is a little bit more complex. By convention the co-ordinate system is only changed for display purposes and remains 
 * in PAMGUARD format throughout the rest of code. 
 * @author Jamie Macaulay
 *
 */
public class Array3DPane extends PamBorderPane {

	public static final Color DEFAULT_HYDRO_COL = Color.RED;

//	private static final Color DEFAULT_SENSOR_COL = Color.LIMEGREEN;

	private double scaleFactor=20; 

	private double axisSize=10*scaleFactor; 

	//keep track of mouse positions
	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;

	/**
	 * This is the group which rotates 
	 */
	Group root3D;

	/**
	 * Group which holds array shapes. 
	 */
	Group arrayGroup; 

	/**
	 * Group which holds axis and other non changing bits. 
	 */
	Group axisGroup; 

	/**
	 * The camera transforms 
	 */
	private Rotate rotateY;
	private Rotate rotateX;
	private Translate translate;

	/**
	 *  The size of the hydrophone for the 3D display. 
	 */
	private double hydrophoneSize = 0.5;
	
	/**
	 * Holds a list of hydrophone spheres
	 */
	private ArrayList<HydrophoneSphere> hydrophonesSpheres = new ArrayList<HydrophoneSphere>();

	public Array3DPane(){

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(20000);
		camera.setNearClip(0.1);
		camera.setDepthTest(DepthTest.ENABLE);
		camera.getTransforms().addAll (
				rotateY=new Rotate(-45, Rotate.Y_AXIS),
				rotateX=new Rotate(-45, Rotate.X_AXIS),
				translate=new Translate(0, 200, -2000));

		//create main 3D group 
		root3D=new Group();
		axisGroup=buildAxes(axisSize); //create axis group
		arrayGroup=new Group();

		root3D.getChildren().add(arrayGroup);
		root3D.getChildren().add(axisGroup);


		AmbientLight light = new AmbientLight();	
		light.setColor(Color.WHITE);
		Group lightGroup = new Group();
		lightGroup.getChildren().add(light);
		root3D.getChildren().add(lightGroup);

		//Use a SubScene to mix 3D and 2D stuff.        
		//note- make sure depth buffer in sub scene is enabled. 
		SubScene subScene = new SubScene(root3D, 500,400, true, SceneAntialiasing.BALANCED);
		subScene.widthProperty().bind(this.widthProperty());
		subScene.heightProperty().bind(this.heightProperty());
		subScene.setDepthTest(DepthTest.ENABLE);
		
		subScene.setOnMouseClicked((MouseEvent me) -> {
	            mousePosX = me.getSceneX();
	            mousePosY = me.getSceneY();
	            PickResult pr = me.getPickResult();
//            	System.out.println("Picked something sphere: " + pr); 

	           	//clear selected radius
            	for (HydrophoneSphere sphere : hydrophonesSpheres) {
            		sphere.setRadius(hydrophoneSize*scaleFactor);
            	}
            	
	            if(pr!=null && pr.getIntersectedNode() != null && pr.getIntersectedNode() instanceof Sphere){
	            	
	            	//make the selected sphere slightly larger
	            	HydrophoneSphere  s = (HydrophoneSphere) pr.getIntersectedNode();
	            	s.setRadius(hydrophoneSize*scaleFactor*1.2);
	            	
	            	hydrophoneSelected(s.getHydrophone());
	            	
//	            	System.out.println("Picked a sphere: " + pr); 
//	                distance=pr.getIntersectedDistance();
//	                s = (Sphere) pr.getIntersectedNode();
//	                isPicking=true;
//	                vecIni = unProjectDirection(mousePosX, mousePosY, scene.getWidth(),scene.getHeight());
	            }
	        });

		//note the fill is actually quite important because if you don't have it mouse rotations etc
		//onyl work if you select a 3D shape
		subScene.setFill(Color.TRANSPARENT);
		subScene.setCamera(camera);

		//handle mouse events for sub scene
		handleMouse(subScene); 



		//create new group to add sub scene to 
		Group group = new Group();
		group.getChildren().add(subScene);

		//add group to window.
		this.setCenter(group);
		this.setDepthTest(DepthTest.ENABLE);

	}

	/**
	 * Called whenever a hydrophone is selected. 
	 * @param hydrophone - the selected hydrophone. 
	 */
	public void hydrophoneSelected(Hydrophone hydrophone) {
		// TODO Auto-generated method stub
	}

	/**
	 * Create a 3D axis with default colours set. 
	 * @param- size of the axis
	 */
	public Group buildAxes(double axisSize) {
		return buildAxes( axisSize,Color.RED, Color.RED,
				Color.GREEN, Color.GREEN,
				Color.BLUE, Color.BLUE,
				Color.WHITE); 
	}


	/**
	 * Create a 3D axis. 
	 * @param- size of the axis
	 */
	public static Group buildAxes(double axisSize, Color xAxisDiffuse, Color xAxisSpectacular,
			Color yAxisDiffuse, Color yAxisSpectacular,
			Color zAxisDiffuse, Color zAxisSpectacular,
			Color textColour) {

		Group axisGroup=new Group(); 

		double length = 2d*axisSize;
		double width = axisSize/100d;
		double radius = 2d*axisSize/100d;


		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(xAxisDiffuse);
		redMaterial.setSpecularColor(xAxisSpectacular);
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(yAxisDiffuse);
		greenMaterial.setSpecularColor( yAxisSpectacular);
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(zAxisDiffuse);
		blueMaterial.setSpecularColor(zAxisSpectacular);

		Text xText=new Text("x"); 
		xText.setStyle("-fx-font: 20px Tahoma;");
		xText.setFill(textColour);
		xText.setStroke(textColour);

		Text yText=new Text("z"); 
		yText.setStyle("-fx-font: 20px Tahoma; ");
		yText.setFill(textColour);
		yText.setStroke(textColour);

		Text zText=new Text("y"); 
		zText.setStyle("-fx-font: 20px Tahoma; ");
		zText.setFill(textColour);
		zText.setStroke(textColour);

		xText.setTranslateX(axisSize+5);
		xText.setTranslateZ(1); //dunno why but shifting a little in z is required to see colour

		yText.setTranslateY(-(axisSize+5));
		yText.setTranslateZ(1); //dunno why but shifting a little in z is required to see colour

		zText.setTranslateZ(axisSize+5);

		Sphere xSphere = new Sphere(radius);
		Sphere ySphere = new Sphere(radius);
		Sphere zSphere = new Sphere(radius);
		xSphere.setMaterial(redMaterial);
		ySphere.setMaterial(greenMaterial);
		zSphere.setMaterial(blueMaterial);

		xSphere.setTranslateX(axisSize);
		ySphere.setTranslateY(-axisSize);
		zSphere.setTranslateZ(axisSize);

		Box xAxis = new Box(length, width, width);
		Box yAxis = new Box(width, length, width);
		Box zAxis = new Box(width, width, length);
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		axisGroup.getChildren().addAll(xText, yText, zText);
		axisGroup.getChildren().addAll(xSphere, ySphere, zSphere);


		return axisGroup;
	}


	private void handleMouse(SubScene scene) {

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});

		scene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override public void handle(ScrollEvent event) {
				//	            	System.out.println("Scroll Event: "+event.getDeltaX() + " "+event.getDeltaY()); 
				translate.setZ(translate.getZ()+  event.getDeltaY() *0.001*translate.getZ());   // + 
			}
		});


		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 1.0;
				double modifierFactor = 0.1;

				if (me.isControlDown()) {
					modifier = 0.1;
				}
				if (me.isShiftDown()) {
					modifier = 10.0;
				}
				if (me.isPrimaryButtonDown()) {
					rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0);  // +
					rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0);  // -
				}
				if (me.isSecondaryButtonDown()) {
					translate.setX(translate.getX() -mouseDeltaX * modifierFactor * modifier * 5);
					translate.setY(translate.getY() - mouseDeltaY * modifierFactor * modifier * 5);   // +
				}


			}
		});
	}

	/**
	 * Draw the hydrophone array. 
	 * @param array - the hydrophone array to draw. 
	 */
	public void drawArray(PamArray array) {

//		System.out.println("DRAW ARRAY: " + array); 

		//clear the array
		arrayGroup.getChildren().removeAll(arrayGroup.getChildren()); 

		ArrayList<Hydrophone> hydrophones = array.getHydrophoneArray();

		//draw hydrophones
		HydrophoneSphere sphere;
		Streamer streamer;
		hydrophonesSpheres.clear();
		for (int i=0; i<hydrophones.size(); i++){
			
			//get the streamer for the hydrophone
			streamer = array.getStreamer(hydrophones.get(i).getStreamerId());

			double x  = hydrophones.get(i).getX() + hydrophones.get(i).getStreamerId();
			double y  = hydrophones.get(i).getY() + hydrophones.get(i).getStreamerId();
			double z  = hydrophones.get(i).getZ() + hydrophones.get(i).getStreamerId();

			sphere=new HydrophoneSphere(hydrophoneSize*scaleFactor);
			sphere.setTranslateX(x*scaleFactor);
			sphere.setTranslateY(z*scaleFactor);
			sphere.setTranslateZ(y*scaleFactor);

			Color col = Color.RED;

			final PhongMaterial aMaterial = new PhongMaterial();
			aMaterial.setDiffuseColor(col);
			aMaterial.setSpecularColor(col.brighter());
			sphere.setMaterial(aMaterial);
			sphere.setHydrophone(hydrophones.get(i));

//			System.out.println("Add hydrophone: " + x + " " + y + " " +z); 

			hydrophonesSpheres.add(sphere);
			arrayGroup.getChildren().add(sphere);
			
			ArrayList<Point3D> streamerPoints=new ArrayList<Point3D>(); 
			//now plot a line from the streamer to the hydrophone
			Point3D newPoint;
			
			newPoint=new Point3D(x*scaleFactor, z*scaleFactor, y*scaleFactor);
			streamerPoints.add(newPoint);
			
			newPoint =new Point3D(streamer.getCoordinate(0)*scaleFactor, streamer.getCoordinate(2)*scaleFactor,  streamer.getCoordinate(1)*scaleFactor);
			streamerPoints.add(newPoint);
			
			System.out.println("Streamer points: " + streamerPoints.size()); 

			PolyLine3D polyLine3D=new PolyLine3D(streamerPoints, 4f, Color.DODGERBLUE); 
			arrayGroup.getChildren().add(polyLine3D);
		}
	}


	private class HydrophoneSphere extends Sphere {
		
		Hydrophone hydrophone; 
		
		public Hydrophone getHydrophone() {
			return hydrophone;
		}
		


		public void setHydrophone(Hydrophone hydrophone) {
			this.hydrophone = hydrophone;
		}

		public HydrophoneSphere() {
			super();
		}
		
		public HydrophoneSphere(double radius) {
			super(radius);
		}
		
	}
	
	//	/**
	//	 * Draw the entire array 
	//	 * @param pos - hydrophone and streamer positions in the same co-ordinate frame as the reference frame. 
	//	 */
	//	public void drawArrays(ArrayList<ArrayList<ArrayPos>> pos){
	//
	//		arrayGroup.getChildren().removeAll(arrayGroup.getChildren()); 
	//
	//		if (pos==null){
	//			System.err.println("Array3DPane: Hydrophone positions are null");
	//			return; 
	//		}
	//
	//		for (int i=0; i< pos.size(); i++){
	//			for (int j=0; j<pos.get(i).size(); j++){
	//				drawArray(pos.get(i).get(j)); 
	//			}
	//		}
	//
	//		//System.out.println("Draw 3D hydrophone array");
	//	}

	//	/**
	//	 * Draw an array.
	//	 * @param arrayPos - hydrophone and streamer positions in the same co-ordinate frame as the reference frame. 
	//	 */
	//	private void drawArray(ArrayPos arrayPos){
	//
	//		final PhongMaterial redMaterial = new PhongMaterial();
	//		redMaterial.setDiffuseColor(DEFAULT_HYDRO_COL);
	//		redMaterial.setSpecularColor(DEFAULT_HYDRO_COL.brighter());
	//
	//		final PhongMaterial greenMaterial = new PhongMaterial();
	//		greenMaterial.setDiffuseColor(DEFAULT_SENSOR_COL);
	//		greenMaterial.setSpecularColor(DEFAULT_SENSOR_COL.brighter());
	//
	//		//draw hydrophones
	//		Sphere sphere;
	//		for (int i=0; i<arrayPos.getTransformHydrophonePos().size(); i++){
	//			sphere=new Sphere(settings.hydrophoneSize*scaleFactor);
	//			sphere.setTranslateX(arrayPos.getTransformHydrophonePos().get(i)[0]*scaleFactor);
	//			sphere.setTranslateY(-arrayPos.getTransformHydrophonePos().get(i)[2]*scaleFactor);
	//			sphere.setTranslateZ(arrayPos.getTransformHydrophonePos().get(i)[1]*scaleFactor);
	//
	//			Color hydroCol = settings.hydrophoneColours[arrayPos.getHArray().getHydrophones().get(i).channel.get()]; 
	//
	//			if (hydroCol == null) {
	//				sphere.setMaterial(redMaterial);
	//			}
	//			else {
	//				final PhongMaterial aMaterial = new PhongMaterial();
	//				aMaterial.setDiffuseColor(hydroCol);
	//				aMaterial.setSpecularColor(hydroCol.brighter());
	//				sphere.setMaterial(aMaterial);
	//
	//			}
	//			arrayGroup.getChildren().add(sphere);
	//
	//		}
	//
	//
	//
	//		//draw streamer
	//		PolyLine3D polyLine3D;
	//		ArrayList<Point3D> streamerPoints;
	//
	//		for (int i=0; i<arrayPos.getTransformStreamerPositions().size(); i++){
	//			if (arrayPos.getTransformStreamerPositions().get(i)==null) return; 
	//			streamerPoints=new ArrayList<Point3D>(); 
	//			for (int j=0; j<arrayPos.getTransformStreamerPositions().get(i).size(); j++){
	//
	//				//TODO- use cylinder for line
	//				//					 Cylinder cylinder=createConnection(arrayPos.getTransformStreamerPositions().get(i).get(j).multiply(scaleFactor),  arrayPos.getTransformStreamerPositions().get(i).get(j+1).multiply(scaleFactor),0.2*scaleFactor); 
	//				//					 arrayGroup.getChildren().add(cylinder);
	//
	//				//need to convert to fxyz 3D point - stupid but no work around. 
	//				Point3D newPoint=new Point3D((float) (arrayPos.getTransformStreamerPositions().get(i).get(j).getX()*scaleFactor),
	//						(float) (-arrayPos.getTransformStreamerPositions().get(i).get(j).getZ()*scaleFactor), (float) (arrayPos.getTransformStreamerPositions().get(i).get(j).getY()*scaleFactor));
	//				streamerPoints.add(newPoint);
	//			}
	//			polyLine3D=new PolyLine3D(streamerPoints, 4, Color.BLUE); 
	//			arrayGroup.getChildren().add(polyLine3D);
	//		}
	//
	//	}




}
