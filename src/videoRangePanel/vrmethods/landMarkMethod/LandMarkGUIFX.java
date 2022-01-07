package videoRangePanel.vrmethods.landMarkMethod;


import java.awt.Point;
import java.util.ArrayList;

import PamUtils.PamUtils;
import PamView.PamSymbolType;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.PamVBox;
import videoRangePanel.VRControl;
import videoRangePanel.VRMeasurement;
import videoRangePanel.VRUtils;
import videoRangePanel.layoutFX.VRSettingsPane;
import videoRangePanel.vrmethods.AbstractVRGUIFX;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.ImageRibbonPaneFX;

/**
 * Contains GUI components for the landmark method. 
 * @author Jamie Macaulay 
 *
 */
public class LandMarkGUIFX extends AbstractVRGUIFX {

	/**
	 * The side pane for the LandMark 
	 */
	private Pane sidePane;

	/**
	 * Reference to the landmark method.
	 */
	private VRLandMarkMethod vrLandMarkmethod;

	/**
	 * List of current landmark groups 
	 */
	private ComboBox<String> landMarkBox;

	/**
	 * Menu which shows landmarks 
	 */
	private ContextMenu menu;

	/**
	 * Pane which shows image angle values and the last animal heading and bearing. 
	 */
	private ImageRibbonPaneFX imageRibbonPaneFX;

	/**
	 * Allow input- needed for updating landmark combo box without settings the 
	 * selected index to zero. 
	 */
	private boolean allowInput;

	///Mouse shape drawing


	/**
	 * The start point of a drag. 
	 */
	private Point dragStart; 

	/**
	 * The current drag point. 
	 */
	private Point dragEnd; 

	/**
	 * True if last mouse action was dragging. 
	 */
	private boolean dragging = false; 

	/**
	 * A list of polygon image points. These are x y pixels on the image
	 */
	private Point[] polygonImagePoints = null;

	//mouse shape drawing.


	//colours for stuff. 
	public final static Color landMarkCol=Color.LIGHTGREEN;
	public final static Color animalCol=Color.CYAN;

	public final static Color rectOutline=Color.DARKCYAN;
	public final static Color rectFill=Color.color(Color.CYAN.getRed(), Color.CYAN.getGreen(), Color.CYAN.getBlue(), 0.3);

	public static PamSymbolFX landMarkMarker = new PamSymbolFX(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, landMarkCol, landMarkCol);
	public static PamSymbolFX animalMarker = new PamSymbolFX(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, animalCol, animalCol);
	public static PamSymbolFX candAnimalMarker = new PamSymbolFX(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.RED, Color.RED);


	//	public static VRSymbolManager landMarksSymbol = new VRSymbolManager(landMarkMarker, "Landmark");
	//	public static VRSymbolManager animalMark = new VRSymbolManager(animalMarker, "LandMark Animal");

	public LandMarkGUIFX(VRLandMarkMethod landMarkMethod) {
		super(landMarkMethod);
		this.vrLandMarkmethod=landMarkMethod; 
		createSidePane();
		this.imageRibbonPaneFX = new ImageRibbonPaneFX(); 
		imageRibbonPaneFX.setMouseTransparent(true);
	}


	/**
	 * Create the side pane. 
	 */
	public void createSidePane() {

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);

		Label labelHeight = new Label("Camera height"); 
		PamGuiManagerFX.titleFont2style(labelHeight);
		//labelHeight.setFont(PamGuiManagerFX.titleFontSize2);
		Pane heightPane = createCameraHeightPane();
		heightPane.prefWidthProperty().bind(vBox.widthProperty());

		Label labelLocation = new Label("Camera location"); 
		PamGuiManagerFX.titleFont2style(labelLocation);
		//labelLocation.setFont(PamGuiManagerFX.titleFontSize2);

		Pane cameraLocationPane = createCameraLocationPane();
		cameraLocationPane.prefWidthProperty().bind(vBox.widthProperty());

		Label landMarkLocation = new Label("Landmarks"); 
		PamGuiManagerFX.titleFont2style(landMarkLocation);
		//landMarkLocation.setFont(PamGuiManagerFX.titleFontSize2);
		Pane landMarkPane = createLandMarkPane();
		landMarkPane.prefWidthProperty().bind(vBox.widthProperty());

		vBox.getChildren().addAll(labelHeight,  heightPane, labelLocation, cameraLocationPane,
				landMarkLocation, landMarkPane); 

		this.sidePane=vBox; 
	}


	/**
	 * Clear the overlay. 
	 */
	public void clearOverlay() {
		this.vrLandMarkmethod.clearLandMarks();
		setInstruction(vrLandMarkmethod.getCurrentStatus());
		//clear ribbon panel
		clearRibbonPanel();
		//repaint the vr panel
		vrControl.getVRPanel().repaint();
	}

	/**
	 * Set the image angle labels.
	 * @param imageMeasurement the image measurement. 
	 */
	protected void setImageLabels(VRMeasurement imageMeasurement){
		if (imageMeasurement==null) imageRibbonPaneFX.clearImageLabels();
		else {
			imageRibbonPaneFX.setImageBearing(imageMeasurement.imageBearing,imageMeasurement.imageBearingErr);
			imageRibbonPaneFX.setImagePitch(imageMeasurement.imagePitch,imageMeasurement.imagePitchErr);
			imageRibbonPaneFX.setImageTilt(imageMeasurement.imageTilt,imageMeasurement.imageTiltErr);
		}
	}

	/**
	 * Set the animal location labels
	 * @param imageMeasurement the image measurement. 
	 */
	protected void setAnimalLabels(VRMeasurement imageMeasurement){
		if (imageMeasurement.locBearing==null)  imageRibbonPaneFX.clearImageLabels();
		else{
			imageRibbonPaneFX.setAnimalBearing(imageMeasurement.locBearing, Math.toRadians(imageMeasurement.locBearingError));
			imageRibbonPaneFX.setAnimalDistance(imageMeasurement.locDistance, imageMeasurement.locDistanceError);
		}
	}

	/**
	 * Set the instruction what to do
	 * @param currentStatus - the current instruction
	 */
	private void setInstruction(int currentStatus) {
		// TODO Auto-generated method stub

	}


	/***
	 * Clear the ribbon pane. 
	 */
	private void clearRibbonPanel() {
		imageRibbonPaneFX.clearAnimalLabels();
		imageRibbonPaneFX.clearImageLabels();
	}


	/**
	 * Create a pane which allows users to select a landmark group. 
	 * @return - the landmark group pane
	 */
	private Pane createLandMarkPane() {
		PamVBox landMarkHolder = new PamVBox(); 
		landMarkHolder.setSpacing(5);

		landMarkBox = new ComboBox<String>(); 
		landMarkBox.setMaxWidth(Double.MAX_VALUE);
		landMarkBox.setOnAction((action)->{
			//set landmarks properly
			if (allowInput) {
				vrControl.getVRParams().setCurrentLandMarkGroupIndex(landMarkBox.getSelectionModel().getSelectedIndex());
				vrControl.update(VRControl.LANDMARKGROUP_CHANGE);
				clearOverlay();
			}

		});

		PamButton settingsButton = new PamButton(); 
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
		settingsButton.setStyle("-fx-border-color: -fx_border_col;"); 
		settingsButton.setOnAction((action)->{
			//vrControl.settingsButtonAWT(null,VRParametersDialog.LAND_MARK);
			vrControl.settingsButtonFX(VRSettingsPane.LANDMARKTAB);
		});

		PamHBox hbox = new PamHBox(); 
		PamHBox.setHgrow(landMarkBox, Priority.ALWAYS);
		hbox.setSpacing(5);

		hbox.getChildren().addAll(landMarkBox, settingsButton);

		PamButton pamButton = new PamButton("Clear Landmarks");
		pamButton.setOnAction((action)->{
			clearOverlay(); 
		});
		pamButton.setStyle("-fx-border-color: -fx_border_col;"); 
		pamButton.prefWidthProperty().bind(landMarkHolder.widthProperty());
//		pamButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.CLEAR, PamGuiManagerFX.iconSize));
		pamButton.setGraphic(PamGlyphDude.createPamIcon("mdi2w-window-close", PamGuiManagerFX.iconSize));


		landMarkHolder.getChildren().addAll(hbox, pamButton); 

		return landMarkHolder;
	}


	/**
	 * Create a landmark list for the landmark combo box on side panel
	 */
	public void setLandMarks(){
		//System.out.println("Set LandMarks " + vrControl.getVRParams().getSelectedLandMarkGroup());
		this.allowInput=false;
		ArrayList<LandMarkGroup> landMarkGroups=vrControl.getVRParams().getLandMarkDatas();
		landMarkBox.getItems().clear(); 
		if (landMarkGroups==null) return;
		if (landMarkGroups.size()==0) return;

		for (int i=0; i<landMarkGroups.size(); i++){
			landMarkBox.getItems().add(landMarkGroups.get(i).getName());
		}

		//System.out.println("Set LandMarks End" + vrControl.getVRParams().getSelectedLandMarkGroup());

		//System.out.println("Get selected landmark group");
		landMarkBox.getSelectionModel().select(vrControl.getVRParams().getSelectedLandMarkGroup());
		this.allowInput=true;

	}


	/**
	 * The side pane with controls specific for landmarks. 
	 * @return - the side pane for landmarks
	 */
	@Override
	public Pane getSidePaneFX() {
		return sidePane; 
	}

	/**
	 * Get the ribbon pane.
	 * @return the ribbon pane. 
	 */
	@Override
	public Pane getRibbonPaneFX() {
		return this.imageRibbonPaneFX;
	}


	/**
	 * Update the GUI. 
	 * @param updateType - the update flag. 
	 */
	public void update(int updateType) {
		Platform.runLater(()->{;
		super.update(updateType);
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			setLandMarks();
			//			System.out.println("Select landmark BOX: " + landMarkBox.getSelectionModel().getSelectedIndex() + 
			//					" Selected in params" +vrControl.getVRParams().getSelectedLandMarkGroup());
			break;
		case VRControl.IMAGE_CHANGE:
			setInstruction(updateType);
			this.imageRibbonPaneFX.clearAnimalLabels();
			this.imageRibbonPaneFX.clearImageLabels();
			if (vrControl.getMeasuredAnimals()!=null) vrControl.getMeasuredAnimals().clear(); 
			//finally want to reset current status so more landmarks can be added.
			if (vrLandMarkmethod.getSetLandMarks()!=null &&vrLandMarkmethod.getSetLandMarks().size()>=2) {
				this.vrLandMarkmethod.setCurrentStatus(VRLandMarkMethod.SET_LANDMARK_READY);
			}
			this.vrLandMarkmethod.setCurrentStatus(VRLandMarkMethod.SET_LANDMARK);

			this.vrControl.getVRPanel().repaint(); //need to repaint to get rid of measured animals.

			break;
		case VRLandMarkMethod.NEW_MESURMENT:
			setAnimalLabels(vrControl.getMeasuredAnimals().get(vrControl.getMeasuredAnimals().size()-1));
			break;
		}
		}); 
	}


	@Override
	public void paint(GraphicsContext g) {
		drawLandMarks(g, landMarkMarker); 
		drawAnimals(g,  animalMarker);
		drawAreaVertex(g); 
	}

	/******Pop Up Menu********/

	/**
	 * Create a dynamic pop up menu to allow users to select landmarks and animals. 
	 * @param point- the location on the image (not the screen)
	 * @return the pop up menu
	 */
	private ContextMenu showlmPopUp(Point2D landMarkPoint){

		Point pointawt = new Point();
		pointawt.setLocation(landMarkPoint.getX(), landMarkPoint.getY());

		final ContextMenu contextMenu = new ContextMenu();
		MenuItem menuItem;

		boolean landmarksOK=true; 
		if (vrControl.getVRParams().getLandMarkDatas()==null || vrControl.getVRParams().getLandMarkDatas().size()==0) {
			menuItem=new MenuItem("Add a landmark group and at least two landmarks");
			contextMenu.getItems().add(menuItem);
			landmarksOK=false;
		}

		LandMarkGroup landMarks=vrControl.getVRParams().getLandMarkDatas().get(vrControl.getVRParams().getSelectedLandMarkGroup());

		if (landMarks.size()<2) {
			menuItem=new MenuItem("Add a landmark group and at least two landmarks");
			contextMenu.getItems().add(menuItem);
			landmarksOK=false;
		}

		if (landmarksOK) {
			int n=0;

			if (vrLandMarkmethod.getCurrentStatus()!=AbstractVRMethod.MEASURE_ANIMAL){

				for (int i=0; i<landMarks.size(); i++){
					if (!vrLandMarkmethod.isInList(landMarks.get(i))){
						menuItem=new MenuItem(landMarks.get(i).getName());
						final int index=i;

						menuItem.setOnAction((action)->{
							vrLandMarkmethod.setLandMark(landMarks.get(index), pointawt); 
							setInstruction(vrLandMarkmethod.getCurrentStatus());
						});

						contextMenu.getItems().add(menuItem);
						n++;
					}
				}

				//add option to remove set landmarks. 
				Menu removeLandMarks= new Menu("Remove");
				for (int i=0; i<landMarks.size(); i++){
					if (vrLandMarkmethod.isInList(landMarks.get(i))){
						menuItem=new MenuItem(landMarks.get(i).getName());
						final int index=i;

						menuItem.setOnAction((action)->{
							vrLandMarkmethod.removeLandMark(landMarks.get(index)); 
						});
						removeLandMarks.getItems().add(menuItem);
						n++;
					}
				}

				if (removeLandMarks.getItems().size()>0) {
					contextMenu.getItems().add(new SeparatorMenuItem());
					contextMenu.getItems().add(removeLandMarks); 
				}


				//if ready to calculate then give the user the option of calculating a position
				if (vrLandMarkmethod.isCalcReady()){
					if (n!=0) contextMenu.getItems().add(new SeparatorMenuItem()); 
					menuItem=new MenuItem("Calc Orientation");
					menuItem.setOnAction((action)->{
						calculate(); 
						vrControl.getVRPanel().repaint();
					});
					contextMenu.getItems().add(menuItem);
				}

				contextMenu.getItems().add(new SeparatorMenuItem()); 
			}

			if (vrLandMarkmethod.isCalcReady()){
				menuItem=new MenuItem("Measure Animal");
				menuItem.setOnAction((action)->{
					calculate(); 
					vrControl.getVRPanel().repaint();
					vrLandMarkmethod.measureAnimal(pointawt);
					setInstruction(vrLandMarkmethod.getCurrentStatus());
				});
				contextMenu.getItems().add(menuItem);
			}

			menuItem=new MenuItem("Clear");
			menuItem.setOnAction((action)->{
				clearOverlay();
			});
			contextMenu.getItems().add(menuItem);
		}

		//if a rectangle or polygon is drawn then save that. 
		if (this.polygonImagePoints!=null){
			contextMenu.getItems().add(new SeparatorMenuItem()); 
			menuItem=new MenuItem("Measure Polygon");
			menuItem.setOnAction((action)->{
				vrLandMarkmethod.measurePolygon(this.polygonImagePoints); 
			});
			contextMenu.getItems().add(menuItem);
		}

		return contextMenu;
	}

	@Override
	public void mouseAction(MouseEvent e, boolean motion, Canvas drawCanvas) {
		boolean vis=isMenuVisible();

		if (e.isPopupTrigger() && !e.isControlDown()){
			//System.out.println("Pop up menu: !!!");
			//System.out.println("We have anew mouse action and it's a pop up menu!"); 
			//			System.out.println("Set the popup point. " + imagePoint.getX() + " " + imagePoint.getY());

			menu=showlmPopUp(drawCanvas.localToScreen(new Point2D(e.getX(), e.getY())));
			menu.show(drawCanvas,e.getScreenX(), e.getScreenY());
			menu.setOnHidden((action)->{
				vrControl.getVRPanel().repaint();
			});
		}

		//if there are enough landmark points and popup menu is not visible then a double click will measure animal location. 
		else if (e.getClickCount()>1 && e.getButton()==MouseButton.PRIMARY && !vis && !e.isControlDown()){
			//System.out.println("Primary double click: !!!");
			if (vrLandMarkmethod.isCalcReady()) {
				vrLandMarkmethod.measureAnimal(point(drawCanvas.localToScreen(new Point2D(e.getX(), e.getY()))));
			}
			//double click deletes drag or any polygon drawing. 
			this.dragStart=null; 
		}		

		//mouse action to draw a polygon. 
		else mousePolygonAction( e,  motion,  drawCanvas);

		//		else if (e.getButton()==MouseButton.PRIMARY && e.getEventType()==MouseEvent.MOUSE_RELEASED && e.) {
		//			System.out.println("Primary no control,: !!!");
		//			dragStart=null;
		//		}

		vrControl.getVRPanel().repaint();
	}

	/**
	 * Handles mouse actions to draw polygon on the image.
	 * TODO- in future should implement a double click polygon. 
	 * @param e - the mouse event
	 * @param motion - true fo the mouse is in motion
	 * @param drawCanvas - the draw canvas
	 */
	private void mousePolygonAction(MouseEvent e, boolean motion, Canvas drawCanvas) {

		if (e.getButton()==MouseButton.NONE) {
			//System.out.println("None: !!!");
			dragging=false;
		}

		else if (e.isPrimaryButtonDown() && e.isDragDetect() && e.isControlDown()) {
			//System.out.println("Drag: !!!");
			if (dragStart==null || dragging ==false) this.dragStart=imagePoint(e,  drawCanvas);
			this.dragEnd=imagePoint(e,  drawCanvas);
			dragging=true;
			updatePolygon(0); 
			//			System.out.println("Drag point 1: Start: x1: " + dragStart.getX() + " y1: " + dragStart.getY() + " x2: "
			//					+ dragEnd.getX() + " y2: " + dragEnd.getY() + " x2: ");
		}

		else if (e.isPrimaryButtonDown() && dragging==false ) {
			//System.out.println("Drag down: !!!");
			this.dragStart=null;
			updatePolygon(0);  
		}

		else if (e.getButton()==MouseButton.SECONDARY && e.isControlDown() ) {
			//System.out.println("Secondary with  control,: !!!");
			this.dragStart=null; 
			updatePolygon(0);  
		}
	}

	/**
	 * Updates the polygon. 
	 * @param flag - type of polygon to create. 
	 */
	private void updatePolygon(int flag) {
		//TODO- can have different types of polygon here. 
		if (dragStart==null) {
			this.polygonImagePoints=null;
			return; 
		}
		if (this.polygonImagePoints==null) {
			polygonImagePoints=new Point[4];
		}
		polygonImagePoints[0]=this.dragStart; 
		polygonImagePoints[1]=new Point((int) dragEnd.getX(), (int) dragStart.getY()); 
		polygonImagePoints[2]=this.dragEnd; 
		polygonImagePoints[3]=new Point((int) dragStart.getX(), (int) dragEnd.getY());  
	}


	/**
	 * Convert a mouse point to a point on the image. 
	 * @param e - mouse event
	 * @param drawCanvas - the draw canvas node.
	 * @return a corresponding point on the image. 
	 */
	private Point imagePoint(MouseEvent e, Canvas drawCanvas) {
		return vrControl.getVRPanel().screenToImage(point(drawCanvas.localToScreen(new Point2D(e.getX(), e.getY())))); 
	}


	/**
	 * Called whenever calculate control is pressed. 
	 */
	private void calculate() {
		vrLandMarkmethod.setCurrentStatus(AbstractVRMethod.MEASURE_ANIMAL);
		vrLandMarkmethod.calcVals();
		vrControl.getVRPanel().repaint();
		VRMeasurement imageMeasurement=new VRMeasurement();
		vrLandMarkmethod.setImagePosVals(imageMeasurement);
		setImageLabels(imageMeasurement);
		setInstruction(vrLandMarkmethod.getCurrentStatus());
	}


	/**
	 * Check whether a menu is visible. 
	 * @return true if the pop up menu is showing. 
	 */
	private boolean isMenuVisible() {
		if (menu==null) return false; 
		return this.menu.isShowing();
	}


	/******Drawing Functions*******/	

	/**
	 * Draw an area when control button is down. 
	 * @param g - the graphics handle. 
	 */
	private void drawAreaVertex(GraphicsContext g2) {
		//System.out.println("Draw rect: " + dragStart +  " " +dragEnd);
		if (dragStart!=null && dragEnd!=null) {
			//make sure to copy points as altered when point converstion happens. 
			Point2D rectStart = imageToLocal(g2.getCanvas(), new Point(this.dragStart));
			Point2D rectEnd = imageToLocal(g2.getCanvas(), new Point(this.dragEnd));
			
			double x0, x1, y0, y1; 
			
			x0=Math.min(rectStart.getX(), rectEnd.getX());
			x1=Math.max(rectStart.getX(), rectEnd.getX());
			y0=Math.min(rectStart.getY(), rectEnd.getY());
			y1=Math.max(rectStart.getY(), rectEnd.getY());

			g2.setStroke(rectOutline);
			g2.setFill(rectFill);
			g2.strokeRect(x0, y0, 
					x1-x0, y1-y0);
			g2.fillRect(x0, y0, 
					x1-x0, y1-y0);
		}
	}


	/**
	 * Draw the position of the animal on the image. 
	 * @param g - the graphics context
	 * @param pamSymbol - the pam symbol for the animal. 
	 */
	private void drawAnimals(GraphicsContext g, PamSymbolFX pamSymbol) {
		//check for previous animals
		if (vrControl.getMeasuredAnimals()==null) return;

		//		if (vrLandMarkmethod.getLandMarkPoints()==null) return;
		//		if (vrLandMarkmethod.getLandMarkPoints().size()==0) return;

		for (int i=0; i<vrControl.getMeasuredAnimals().size(); i++){
			drawAnimal( g, vrControl.getMeasuredAnimals().get(i), pamSymbol); 
		}
	}


	/**
	 * Draw all animal measurements for the image. 
	 */
	protected void drawAnimal(GraphicsContext g2, VRMeasurement vr, PamSymbolFX symbol) {
		Point sp1;
		Point pA;
		Point anP=null;

		if (vr.animalPoint!=null && vrLandMarkmethod.getLandMarkPoints()!=null) {
			anP=point(imageToLocal(g2.getCanvas(), new Point(vr.animalPoint)));
			//draw lines from landmarks to animal 
			for (int i=0; i<vrLandMarkmethod.getLandMarkPoints().size(); i++){

				pA=new Point(vrLandMarkmethod.getLandMarkPoints().get(i));

				sp1=point(imageToLocal(g2.getCanvas(), pA));

				g2.setStroke(symbol.getLineColor());
				g2.setLineDashes(null);
				g2.strokeLine(sp1.getX(), sp1.getY(), anP.getX(), anP.getY());
				//draw the bearing and pitch lines
				pA=VRLandMarkMethod.calcPerpPoint(vrLandMarkmethod.getTilt(),
						VRLandMarkMethod.calcBearingDiffAnimal(vrLandMarkmethod.getTilt(), sp1, anP),anP);
				g2.setLineDashes(5);
				g2.strokeLine(sp1.getX(), sp1.getY(), pA.getX(), pA.getY());
				g2.strokeLine(anP.getX(), anP.getY(), pA.getX(), pA.getY());
			}

			symbol.draw(g2, point2D(anP));
		}

		//draw a polygon
		if (vr.polygonArea!=null) {
			//System.out.println("Draw a polygon!!!: " + vr +"   " +vr.polygonArea.length);
			Point[] loacalPoint= new Point[vr.polygonArea.length+1];
			for (int i=0; i<loacalPoint.length-1; i++) {
				loacalPoint[i]=point(imageToLocal(g2.getCanvas(), new Point(vr.polygonArea[i])));
			}
			loacalPoint[loacalPoint.length-1]=point(imageToLocal(g2.getCanvas(), new Point(vr.polygonArea[0])));

			double[] xPoints=VRUtils.points2Array(loacalPoint, true); 
			double[] yPoints=VRUtils.points2Array(loacalPoint, false); 

			g2.strokePolyline(xPoints, yPoints, yPoints.length);
			g2.setFill(rectFill);
			g2.fillPolygon(xPoints, yPoints, yPoints.length);

			//draw some cool lines from center of polygon to corners. 
			if (anP!=null) {
				for (int i=0; i<loacalPoint.length-1; i++) {
					g2.strokeLine(anP.getX(), anP.getY(), loacalPoint[i].getX(),  loacalPoint[i].getY());
				}

			}
		}

		//draw the animal circle
		g2.setLineDashes(null);
	}



	/**
	 * Draw the landmark measurement points. 
	 * @param g - the landmark measurement points. 
	 * @param symbol 
	 */
	protected void drawLandMarks(GraphicsContext g2, PamSymbolFX symbol){

		if (vrLandMarkmethod.getLandMarkPoints()==null) return;
		if (vrLandMarkmethod.getLandMarkPoints().size()==0) return;

		g2.setLineWidth(3);

		Point2D sp1 = null;
		Point2D sp2 = null;

		Point landMarkPoint  = new Point();
		if (vrLandMarkmethod.getLandMarkPoints() != null) {
			for (int i=0; i<vrLandMarkmethod.getLandMarkPoints().size(); i++){
				//				System.out.println("LandMark point on image" + vrLandMarkmethod.getLandMarkPoints().get(i).getX() + " " + vrLandMarkmethod.getLandMarkPoints().get(i).getY());

				/**
				 * 07/09/2017. Note sure why but there seems to be an issue with the point references being changed. So 
				 * have to make a new point here or the one in the list changes. Must be missing something up the fucntion
				 * chain. This is a working fix. 
				 */
				landMarkPoint.setLocation(vrLandMarkmethod.getLandMarkPoints().get(i));

				symbol.draw(g2, imageToLocal(g2.getCanvas(), landMarkPoint));
			}
		}

		Point landMarkIndexM1  = new Point();
		Point landMarkIndexM2 = new Point();
		if (vrLandMarkmethod.getCurrentStatus()==AbstractVRMethod.MEASURE_ANIMAL){
			if (vrLandMarkmethod.getTiltVals()==null) return;
			Point perpPoint;
			ArrayList<Integer> indexM1 = PamUtils.indexM1(vrLandMarkmethod.getLandMarkPoints().size());
			ArrayList<Integer> indexM2 = PamUtils.indexM2(vrLandMarkmethod.getLandMarkPoints().size());
			for (int i=0; i<indexM1.size(); i++){
				g2.setStroke(symbol.getLineColor());

				landMarkIndexM1.setLocation( vrLandMarkmethod.getLandMarkPoints().get(indexM1.get(i)));
				sp1=imageToLocal(g2.getCanvas(), landMarkIndexM1);
				landMarkIndexM2.setLocation(vrLandMarkmethod.getLandMarkPoints().get(indexM2.get(i)));
				sp2=imageToLocal(g2.getCanvas(), landMarkIndexM2);
				//				//calc the tilt
				//				System.out.println("LandMark m1: "+landMarkIndexM1.getX()+ " : "+ landMarkIndexM1.getY()); 
				//				System.out.println("LandMark m2: "+landMarkIndexM2.getX()+ " : "+ landMarkIndexM2.getY()); 

				perpPoint=vrLandMarkmethod.calcPerpPoint(vrLandMarkmethod.getTiltVals().get(i), vrLandMarkmethod.getLandMarkPoints().get(indexM1.get(i)), 
						vrLandMarkmethod.getLandMarkPoints().get(indexM2.get(i)),  vrLandMarkmethod.getSetLandMarks().get(indexM1.get(i)),  
						vrLandMarkmethod.getSetLandMarks().get(indexM2.get(i)), vrLandMarkmethod.getSetLandMarks().get(0).getLatLongOrigin());


				perpPoint= point(imageToLocal(g2.getCanvas(), perpPoint));
				//				perpPoint=vrControl.getVRPanel().imageToScreen(perpPoint);
				g2.setStroke(landMarkCol);
				g2.setLineDashes(5);
				g2.strokeLine(sp1.getX(), sp1.getY(), sp2.getX(), sp2.getY());
				g2.setLineDashes(null);
				g2.strokeLine(sp1.getX(), sp1.getY(), perpPoint.getX(), perpPoint.getY());
				g2.strokeLine(sp2.getX(), sp2.getY(), perpPoint.getX(), perpPoint.getY());
			}
		}

	}

	/**
	 * Get polygon points on the image. These points are in image pixels. If there is no drawn polygon
	 * will return null.
	 * @return list of points representing corners of polygon drawn on image. 
	 */
	public Point[] getMeasuredPolygon() {
		return this.polygonImagePoints;
	}

}
