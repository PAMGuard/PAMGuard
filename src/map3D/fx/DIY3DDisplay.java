package map3D.fx;


import Array.ArrayManager;
import Array.PamArray;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.PamSymbolType;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import pamMaths.PamVector;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamSymbolFX;

public class DIY3DDisplay {
	
	private PamBorderPane borderPane;
	private Pane plotPane;
	private Canvas canvas;
	private Scene mainScene;
	double cameraHeight = 1000;
	double cameraScale;
	
	private MapGroup earthGroup = new MapGroup();
	
	private PamSymbolFX fxSymbol = new PamSymbolFX(
			PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.RED, Color.BLUE);
	private PamSymbolFX phoneSymbol = new PamSymbolFX(
			PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLUE, Color.BLUE);
	private Color surfaceColAbove = new Color(0,1,1,.1);
	private Color surfaceColBelow = new Color(0,.5,.5,.2);

	public DIY3DDisplay() {
		plotPane = new Pane();
		plotPane.getChildren().add(canvas = new Canvas(200, 200));
		borderPane = new PamBorderPane(plotPane);
		mainScene = new Scene(borderPane, 400, 100, true, SceneAntialiasing.BALANCED);
		
		cameraScale = 2.*Math.tan(Math.toRadians(30./2.));
		earthGroup.rx.setAngle(30);
		earthGroup.rz.setAngle(30);
		
		InvalidationListener listener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				resizePlot();
			}
		};
		mainScene.widthProperty().addListener(listener);
		mainScene.heightProperty().addListener(listener);
		
		setupMouse();
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				resizePlot();
			}
		});
	}
	
	protected void resizePlot() {
		canvas.setWidth(plotPane.getWidth());
		canvas.setHeight(plotPane.getHeight());
//		System.out.printf("Plot size %3.1f x %3.1f\n", plotPane.getWidth(), plotPane.getHeight());
//		canvas.
		drawObjects();
	}

	private void drawObjects() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
//		System.out.printf("Cam height %3.1f, zAng %3.1f xang %3.1f, yang %3.1f\n",  cameraHeight, 
//				PamUtils.constrainedAngle(earthGroup.rz.getAngle()), earthGroup.rx.getAngle(), earthGroup.ry.getAngle());
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
//		gc.strokeLine(10, 10, plotPane.getWidth()-10, plotPane.getHeight()-10);
//		fxSymbol.draw(gc, translatePoint3D(0, 0, 0.));
		double c = 100.;
		MapPointData p3;
		p3 = translatePoint3D(c, 0, 0);
//		double symbolSize = getSymbolSize(100, p3.getZ(), 2, 2000);
//		fxSymbol.setFillColor(Color.RED);
//		fxSymbol.draw(gc, p3.toPoint2D(), symbolSize, symbolSize);
//		fxSymbol.setFillColor(Color.BLUE);
//		p3 = translatePoint3D(0,0,0);
//		symbolSize = getSymbolSize(100, p3.getZ(), 2, 2000);
//		fxSymbol.draw(gc, p3.toPoint2D(), symbolSize, symbolSize);
//		fxSymbol.draw(gc, p = translatePoint3D(c, c, c).toPoint2D());
//		gc.strokeText("(1,1,1)", p.getX(), p.getY());
//		fxSymbol.draw(gc, p = translatePoint3D(c, c, -c).toPoint2D());
//		fxSymbol.draw(gc, p = translatePoint3D(c, -c, c).toPoint2D());
//		fxSymbol.draw(gc, p = translatePoint3D(-c, c, c).toPoint2D());
//		fxSymbol.draw(gc, p = translatePoint3D(c, -c, -c).toPoint2D());
//		fxSymbol.draw(gc, p = translatePoint3D(-c, -c, c).toPoint2D());
//		fxSymbol.draw(gc, p = translatePoint3D(-c, c, -c).toPoint2D());
//		fxSymbol.draw(gc, p = translatePoint3D(-c, -c, -c).toPoint2D());
//		gc.strokeText("(-1,-1,-1)", p.getX(), p.getY());
//		double axisLength = getSymbolSize(canvas.getHeight()/2, cameraHeight, 20, 10000);
		double axisLength = cameraHeight * cameraScale/2;
		Point2D orig = translatePoint3D(0,0,0).toPoint2D();
		Point2D ax = translatePoint3D(axisLength, 0, 0).toPoint2D();
		gc.strokeLine(orig.getX(), orig.getY(), ax.getX(), ax.getY());
		gc.strokeText("X", ax.getX(), ax.getY());
		ax = translatePoint3D(0, axisLength, 0).toPoint2D();
		gc.strokeLine(orig.getX(), orig.getY(), ax.getX(), ax.getY());
		gc.strokeText("Y", ax.getX(), ax.getY());
		ax = translatePoint3D(0, 0, axisLength).toPoint2D();
		gc.strokeLine(orig.getX(), orig.getY(), ax.getX(), ax.getY());
		gc.strokeText("Z", ax.getX(), ax.getY());
		
		// try to draw the sea surface. 
		double surfacesize = cameraHeight/2;
		double[] sx = new double[4];
		double[] sy = new double[4];
		Point2D p = translatePoint3D(-surfacesize, -surfacesize, 0).toPoint2D();
		sx[0] = p.getX();
		sy[0] = p.getY();
		p = translatePoint3D(-surfacesize, surfacesize, 0).toPoint2D();
		sx[1] = p.getX();
		sy[1] = p.getY();
		p = translatePoint3D(+surfacesize, surfacesize, 0).toPoint2D();
		sx[2] = p.getX();
		sy[2] = p.getY();
		p = translatePoint3D(surfacesize, -surfacesize, 0).toPoint2D();
		sx[3] = p.getX();
		sy[3] = p.getY();
		if (isAbove()) {
			gc.setFill(surfaceColAbove);
		}
		else {
			gc.setFill(surfaceColBelow);
		}
		gc.fillPolygon(sx, sy, 4);
		
		drawArray();
	}

	private void drawArray() {
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int nPhones = currentArray.getHydrophoneCount();
		long now = PamCalendar.getTimeInMillis();
		GraphicsContext gc = canvas.getGraphicsContext2D();
		for (int i = 0; i < nPhones; i++) {
			PamVector phonVec = currentArray.getAbsHydrophoneVector(i, now);
			MapPointData p = translatePoint3D(phonVec.getElement(0), phonVec.getElement(1), phonVec.getElement(2));
			double phoneSz = getSymbolSize(.02, p.getZ(), 4, 100); 
			phoneSymbol.draw(gc, p.toPoint2D(), phoneSz, phoneSz);
		}
		
	}

	/**
	 * Calculate the size for displaying a symbol on the display. 
	 * Normally this will scale with distance from the camera, but 
	 * will not get any smaller than a set minimum or larger than a set maximum. 
	 * @param objectSize nominal object size in metres
	 * @param distance distance from camera. 
	 * @param minPixels minimum number of pixels for the object
	 * @param maxPixels maximum number of pixels for the object. 
	 * @return size to display. 
	 */
	private double getSymbolSize(double objectSize, double distance, int minPixels, int maxPixels) {
		double size = objectSize / distance / cameraScale * plotPane.getHeight();
		return Math.max(minPixels, Math.min(maxPixels, size));
	}

	private boolean isAbove() {
		return Math.cos(Math.toRadians(earthGroup.rx.getAngle())) > 0;
	}
	private MapPointData translatePoint3D(double x, double y, double z) {
		return translatePoint3D(new Point3D(x, y, -z));
	}
	private MapPointData translatePoint3D(Point3D point3D) {
		ObservableList<Transform> transforms = earthGroup.getTransforms();
		for (int i = 0; i < transforms.size(); i++) {
			point3D = transforms.get(i).transform(point3D);
		}
		double x0 = plotPane.getWidth()/2.;
		double y0 = plotPane.getHeight()/2.;
		double cameraDist = cameraHeight + point3D.getZ();
		double x = Math.atan2(point3D.getX(), cameraDist) * plotPane.getHeight() / cameraScale;
		double y = Math.atan2(point3D.getY(), cameraDist) * plotPane.getHeight() / cameraScale;
//		return new Point2D(point3D.getX()+x0, point3D.getY()+y0);
		return new MapPointData(x + x0, y0-y, cameraDist);
	}

	public Scene getMainScene() {
		return mainScene;
	}
	

	double mouseOldX;
	double mouseOldY;
	private void setupMouse() {
		// TODO Auto-generated method stub
		mainScene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
		        drawObjects();
			}
		});
		mainScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent me) {
				double mouseX = me.getSceneX();
				double mouseY = me.getSceneY();
				double diffX = mouseX-mouseOldX;
				double diffY = mouseY-mouseOldY;
				if (me.isSecondaryButtonDown() || me.isControlDown()) {
					earthGroup.t.setX(earthGroup.t.getX() + diffX);
					earthGroup.t.setY(earthGroup.t.getY() - diffY);
				}
				else if (me.isPrimaryButtonDown()) {
					earthGroup.rz.setAngle(earthGroup.rz.getAngle()+diffX);
					double newX = earthGroup.rx.getAngle()+diffY;
					newX = Math.min(135, Math.max(0, newX));
					earthGroup.rx.setAngle(newX);
				}
				mouseOldX = mouseX;
				mouseOldY = mouseY;
		        drawObjects();
			}
		});
		
		mainScene.setOnScroll((ScrollEvent event) -> {
			double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
            	zoomFactor = 1./zoomFactor;
            }
            cameraHeight *= zoomFactor;
//            Scale s = earthGroup.s;
//            s.
//            earthGroup.setScaleX(earthGroup.getScaleX()*zoomFactor);
//            earthGroup.setScaleY(earthGroup.getScaleY()*zoomFactor);
//            earthGroup.setScaleZ(earthGroup.getScaleZ()*zoomFactor);
//            camera.setTranslateZ(camera.getTranslateZ()*zoomFactor);
//            addFixedObjects();
//            paintMovingObjects();
            drawObjects();
			
		});
	}
}
