package map3D.layout;

import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import pamViewFX.threeD.Xform;

/**
 * Simple pane with everything setup for 3D viewing. 
 * @author Jamie Macaulay	
 *
 */
public class Pane3D extends BorderPane {


	/**
	 * The scene camera. 
	 */

	final PerspectiveCamera camera = new PerspectiveCamera(true);
	
    final double cameraDistance = 1500;


	final Xform cameraXform = new Xform();
	final Xform cameraXform2 = new Xform();
	final Xform cameraXform3 = new Xform();

	/**
	 * Automatic rotation
	 */
	private Timeline timeline;
	boolean timelinePlaying = false;


	/**
	 * Keep track of mouse positions. 
	 */
	private double mousePosX;
	private double mousePosY;
	private double mouseOldX;
	private double mouseOldY;
	private double mouseDeltaY;
	private double mouseDeltaX;

	double ONE_FRAME = 1.0 / 24.0;
	double DELTA_MULTIPLIER = 200.0;
	double CONTROL_MULTIPLIER = 0.1;
	double SHIFT_MULTIPLIER = 0.1;
	double ALT_MULTIPLIER = 0.5;

	/** 
	 * The main group which holds other groups. 
	 */
	private Group sceneRoot;
	
	private Xform simWorld;
	
	private Node headLight;
	
	private Node pointLight1;

	/**
	 * Group which holds all shapes which are constantly added and removed. 
	 */
	private Xform dynamicGroup;
	
	
	private PointLight pointLight2; 


	public Pane3D(){
		create3DMap();
	}

	private void buildCamera(Group sceneRoot) {
		sceneRoot.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(camera);
		//cameraXform3.setRotateZ(180.0);

		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setTranslateZ(-cameraDistance);
		cameraXform.ry.setAngle(0);
		cameraXform.rx.setAngle(0);
		cameraXform.rz.setAngle(0);
	}

	/**
	 * Create the basics of the 3D map. 
	 */
	public void  create3DMap(){
		
		//create the groups we want. 
		sceneRoot = new Group();
		simWorld=new Xform();
		dynamicGroup=new Xform();

		//handle mouse behaviour
		buildCamera(sceneRoot);
		//			handleMouse( subScene);
		initLights(simWorld);

		simWorld.getChildren().add(dynamicGroup);

		sceneRoot.getChildren().add(simWorld);
		
		SubScene subScene = new SubScene(sceneRoot, 500,500, true, SceneAntialiasing.BALANCED);
		subScene.setFill(Color.BLACK);
		subScene.widthProperty().bind(this.widthProperty());
		subScene.heightProperty().bind(this.heightProperty());
		subScene.setDepthTest(DepthTest.ENABLE);

		handleKeyboard(subScene, simWorld);
		handleMouse(subScene, simWorld);
		subScene.setCamera(camera);

		//render text properly
		this.setCache(true);
		this.setCacheHint(CacheHint.SCALE_AND_ROTATE);
		
		this.setCenter(subScene);
	}



	private void initLights(Group sceneRoot){
		
		headLight = new PointLight();        
		headLight.translateXProperty().bindBidirectional(camera.translateXProperty());
		headLight.translateYProperty().bindBidirectional(camera.translateYProperty());
		headLight.translateZProperty().bindBidirectional(camera.translateZProperty());	        
		headLight.setRotationAxis(Rotate.Y_AXIS);

		pointLight1 = new PointLight();
		pointLight1.setTranslateX(-1000);
		pointLight1.setTranslateY(-1000);
		pointLight1.setTranslateZ(-1000);
		
		pointLight2 = new PointLight();
		pointLight2.setTranslateX(-1000);
		pointLight2.setTranslateY(1000);
		pointLight2.setTranslateZ(1000);

		//	        ambientLight = new AmbientLight();
		//	        ambientLight.setTranslateY(-1000);
		sceneRoot.getChildren().addAll(pointLight1, pointLight2, headLight);
	}

	/**
	 * Get the root group of the pane. Generally dd only permenent shapes here. 
	 * @return the main root group
	 */
	public Group getRootGroup(){
		return sceneRoot;
	}


	/**
	 * Get group to add and remove shapes from 
	 * @return the map provider group.  
	 */
	public Group getDynamicGroup() {
		return dynamicGroup;
	}

	private void handleMouse(SubScene subScene, final Node root) {

		subScene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});
		
		subScene.setOnScroll(new EventHandler<ScrollEvent>() {
					double modifier = 1.0;
					double modifierFactor =1;
					@Override public void handle(ScrollEvent event) {
//		            	System.out.println("Scroll Event: "+event.getDeltaX() + " "+event.getDeltaY()); 
						double z = camera.getTranslateZ();
						double newZ = z + event.getDeltaY() * modifierFactor * modifier*0.001*camera.getTranslateZ();
						camera.setTranslateZ(newZ); 
		            }
		        });


		subScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
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
					cameraXform.rz.setAngle(cameraXform.rz.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0);  // +
					cameraXform.rx.setAngle(cameraXform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0);  // -
				} else if (me.isSecondaryButtonDown()) {
					cameraXform2.t.setX(cameraXform2.t.getX() - mouseDeltaX * modifierFactor * modifier * 3);  // -
					cameraXform2.t.setY(cameraXform2.t.getY() - mouseDeltaY * modifierFactor * modifier * 3);  // -
				} else if (me.isMiddleButtonDown()) {
					cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 3);  // -
					cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 3);  // -
				}
			}
		});
	}

	private void handleKeyboard(SubScene subScene, final Node root) {
		final boolean moveCamera = true;
		subScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				Duration currentTime;
				switch (event.getCode()) {
				case Z:
					if (event.isShiftDown()) {
						cameraXform.ry.setAngle(0.0);
						cameraXform.rx.setAngle(0.0);
						camera.setTranslateZ(-300.0);
					}
					cameraXform2.t.setX(0.0);
					cameraXform2.t.setY(0.0);
					break;
				case X:
//					if (event.isControlDown()) {
//						if (axisGroup.isVisible()) {
//							axisGroup.setVisible(false);
//						} else {
//							axisGroup.setVisible(true);
//						}
//					}
					break;
				case S:
					//	                        if (event.isControlDown()) {
						//	                            if (moleculeGroup.isVisible()) {
					//	                                moleculeGroup.setVisible(false);
					//	                            } else {
					//	                                moleculeGroup.setVisible(true);
					//	                            }
					//	                        }
					break;
				case SPACE:
					if (timelinePlaying) {
						timeline.pause();
						timelinePlaying = false;
					} else {
						timeline.play();
						timelinePlaying = true;
					}
					break;
				case UP:
					if (event.isControlDown() && event.isShiftDown()) {
						cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown() && event.isShiftDown()) {
						cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
					} else if (event.isControlDown()) {
						cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown()) { 
						cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
					} else if (event.isShiftDown()) {
						double z = camera.getTranslateZ();
						double newZ = z + 5.0 * SHIFT_MULTIPLIER;
						camera.setTranslateZ(newZ);
					}
					break;
				case DOWN:
					if (event.isControlDown() && event.isShiftDown()) {
						cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown() && event.isShiftDown()) {
						cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
					} else if (event.isControlDown()) {
						cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown()) {
						cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
					} else if (event.isShiftDown()) {
						double z = camera.getTranslateZ();
						double newZ = z - 5.0 * SHIFT_MULTIPLIER;
						camera.setTranslateZ(newZ);
					}
					break;
				case RIGHT:
					if (event.isControlDown() && event.isShiftDown()) {
						cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown() && event.isShiftDown()) {
						cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
					} else if (event.isControlDown()) {
						cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown()) {
						cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
					}
					break;
				case LEFT:
					if (event.isControlDown() && event.isShiftDown()) {
						cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown() && event.isShiftDown()) {
						cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
					} else if (event.isControlDown()) {
						cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
					} else if (event.isAltDown()) {
						cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
					}
					break;
				}
			}
		});
	}


	//		 private void handleMouse(SubScene scene) {
	//    	
	//        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
	//			@Override public void handle(MouseEvent me) {
	//                mousePosX = me.getSceneX();
	//                mousePosY = me.getSceneY();
	//                mouseOldX = me.getSceneX();
	//                mouseOldY = me.getSceneY();
	//            }
	//        });
	//        
	//        scene.setOnScroll(new EventHandler<ScrollEvent>() {
	//
	//			@Override public void handle(ScrollEvent event) {
	//            	System.out.println("Scroll Event: "+event.getDeltaX() + " "+event.getDeltaY()); 
	//         	translate.setZ(translate.getZ()+  event.getDeltaY() *0.001*translate.getZ());   // + 
	//            }
	//        });
	//        
	//        
	//        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
	//
	//			@Override
	//            public void handle(MouseEvent me) {
	//                mouseOldX = mousePosX;
	//                mouseOldY = mousePosY;
	//                mousePosX = me.getSceneX();
	//                mousePosY = me.getSceneY();
	//                mouseDeltaX = (mousePosX - mouseOldX);
	//                mouseDeltaY = (mousePosY - mouseOldY);
	//
	//                double modifier = 1.0;
	//                double modifierFactor = 0.1;
	//
	//                if (me.isControlDown()) {
	//                    modifier = 50;
	//                }
	//                if (me.isShiftDown()) {
	//                    modifier = 0.1;
	//                }
	//                if (me.isPrimaryButtonDown()) {
	//                	rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0);  // +
	//                	rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0);  // -
	//                }
	//                if (me.isSecondaryButtonDown()) {
	//                	translate.setX(translate.getX() -mouseDeltaX * modifierFactor * modifier * 5);
	//                	translate.setY(translate.getY() - mouseDeltaY * modifierFactor * modifier * 5);   // +
	//                }
	//              
	//               
	//            }
	//        });
	//  }





}
