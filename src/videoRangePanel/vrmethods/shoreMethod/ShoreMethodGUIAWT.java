package videoRangePanel.vrmethods.shoreMethod;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JButton;

import Map.MapContour;
import Map.MapFileManager;
import PamUtils.LatLong;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRHeightData;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.vrmethods.AbstractVRGUIAWT;
import videoRangePanel.vrmethods.ImageAnglePanel;

/**
 * GUI Components for the shore method
 * @author Jamie Macaulay 
 *
 */
public class ShoreMethodGUIAWT extends AbstractVRGUIAWT {
	
	private VRShoreMethod vrShoreMethod;
	
	//PamPanel components
	private PamLabel lab;
	private JButton clearShore;
	PamPanel sidePanel;

	private PamLabel instruction;
//	private JButton mapSettings;
//	private JTextField gps;
//	private JButton gpsLocSettings;
	private ImageAnglePanel imageAnglePanel;
	
	/**
	 * The last mouse position
	 */
	private Point currentMouse;
	
	///Symbols.
	private Color landColour = Color.BLACK;
	private PamSymbol landPointSymbol = new PamSymbol(PamSymbolType.SYMBOL_POINT, 1, 1, true, Color.RED, Color.RED);
	

	public ShoreMethodGUIAWT(VRShoreMethod vrShoreMethod) {
		super(vrShoreMethod);
		this.vrShoreMethod=vrShoreMethod; 
		this.imageAnglePanel=new ImageAnglePanel(vrControl,vrShoreMethod);
		this.sidePanel=createSidePanel();
	}

	private PamPanel createSidePanel(){
		
		PamPanel panel=new PamPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		c.gridx=0;
		c.gridy=0;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, lab=new PamLabel("Shore Measurement "), c);
		lab.setFont(PamColors.getInstance().getBoldFont());
		
		c.gridy++;
		c.gridx=0;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, createCalibrationList(), c);

		c.gridy++;
		PamDialog.addComponent(panel, createMapFilePanel(), c);
	
		c.gridy++;
		PamDialog.addComponent(panel, createLocationListPanel(), c);

		c.gridy++;
		c.gridx=0;
		PamDialog.addComponent(panel, instruction = new PamLabel("...."), c);
		instruction.setFont(PamColors.getInstance().getBoldFont());
		
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, clearShore = new JButton("Clear Shore"), c);
		clearShore.addActionListener(new ClearShore());
		c.gridy++;
		c.gridy++;
		
		newCalibration();
		
		setInstruction(vrShoreMethod.getCurrentStatus());
		
		return panel;
	}
	
	
	

	@Override
	public void mouseAction(MouseEvent e, boolean motion) {
		Point mouseClick=vrControl.getVRPanel().screenToImage(e.getPoint());
		currentMouse=mouseClick; 
		switch (vrShoreMethod.getCurrentStatus()) {
			case VRShoreMethod.MEASURE_SHORE:
				//System.out.println("shore Point clicked: "+currentStatus);
				vrShoreMethod.setShorePoint(new Point(mouseClick));
				if (vrShoreMethod.horizonPointsFromShore(vrShoreMethod.getHorizonTilt())) {
					vrShoreMethod.setVrSubStatus(VRShoreMethod.MEASURE_ANIMAL);
				}
				else vrShoreMethod.setShorePoint(null); 
				break;
			
			case VRShoreMethod.MEASURE_ANIMAL:
				//System.out.println("Measure Animal from  shore: "+currentStatus);
				vrShoreMethod.newAnimalMeasurement_Shore(mouseClick);
			break;
		}
		vrControl.getVRPanel().repaint();
	}
	
	/**
	 * Sets the text telling the user what to do. 
	 * @param status- the current status
	 */
	public void setInstruction(int status){

		switch (status) {
		case VRShoreMethod.MEASURE_SHORE:
			instruction.setText("Click on shore");
			break;
		case VRShoreMethod.MEASURE_ANIMAL:
			instruction.setText("Click animal");
			break;
		}
		//check if there are problems with required params
		if (vrShoreMethod.getGPSinfo(vrControl.getImageTime())==null) instruction.setText("No GPS info");
		if (vrControl.getMapFileManager().getAvailableContours()==null || vrControl.getMapFileManager().getContourCount() <=1) instruction.setText("No Map Points");
		if (imageAnglePanel.getAngle()==null) instruction.setText("No bearing info");


		//check there are no general instructions-e.g. no image
		setInstruction(instruction);
		
	}
	
	@Override
	public void update(int updateType){
		super.update(updateType);
	}
	
	private class ClearShore implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			vrShoreMethod.clearOverlay(); 
		}
	}
	
	
	/**
	 * Draw the land outline
	 * <p>
	 * Requires knowledge of the horizon or some point on the land and 
	 * also the angle for this to work. 
	 * @param g graphics handle
	 */
	private void drawLand(Graphics g) {
		/**
		 * Will have to get the positions of the land at each x pixel. For this
		 * we will need to convert a distance to a y coordinate. 
		 * The most general way of doing this will be to have a horizon pixel for
		 * any point, and then work out the angle below the horizon for a given angle. 
		 * (can't plot anything above the horizon of course). 
		 * If we're working to the shore, then work out where the horizon should be before 
		 * plotting. 
		 */
		Double landAngle = vrShoreMethod.getImageHeading();
		if (landAngle == null) {
			return;
		}
		// check that there are some reference points before drawing
		if (vrShoreMethod.getHorizonPixel(vrControl.getVRPanel().getImageWidth()/2) == null) {
			return; 
		}
		LatLong origin = vrShoreMethod.getGPSinfo();
		MapFileManager mapManager = vrShoreMethod.getShoreManager().getMapFileManager();
		if (mapManager == null) {
			return;
		}
		VRHeightData heightData = vrControl.getVRParams().getCurrentheightData();
		if (heightData == null) {
			return;
		}
		
		VRCalibrationData calData = vrControl.getVRParams().getCurrentCalibrationData();
		if (calData == null) {
			return;
		}
		g.setColor(Color.BLACK);
		Vector<LatLong> contour;
		MapContour mapContour;
		for (int i = 0; i < mapManager.getContourCount(); i++) {
			mapContour = mapManager.getMapContour(i);
			contour = mapContour.getLatLongs();
			for (int l = 0; l < contour.size()-1; l++) {
				drawMapSegment(g, origin, heightData.height, calData.degreesPerUnit, landAngle, contour.get(l), contour.get(l+1));
			}
		}
	}
	
	
	protected void drawMarksandLine(Graphics g, Point p1, Point p2, PamSymbol symbol, boolean drawLine, Point currentMouse) {
		super.drawMarksandLine(g, p1, p2, symbol, drawLine, currentMouse);
		Point sp1 = vrShoreMethod.getShorePoint();
		if (sp1 != null) {
			symbol.draw(g, vrControl.getVRPanel().imageToScreen(sp1));
		}
	}
	
	
	private void addMeasurementMarks(Graphics g) {
		drawMarksandLine(g, vrShoreMethod.getHorizonPoint1(), vrShoreMethod.getHorizonPoint2(), 
				VRPanel.horizonSymbol.getPamSymbol(), vrControl.getVRParams().drawTempHorizon, getCurrentMouse());
	}
	
	
	private void drawMapSegment(Graphics g, LatLong origin, double height, double degreesPerUnit,
	double imageAngle, LatLong ll1, LatLong ll2) {
		Point p1, p2;
		p1 = vrShoreMethod.getObjectPoint(origin, height, degreesPerUnit, imageAngle, ll1,vrControl.getRangeMethods().getCurrentMethod());
		p2 = vrShoreMethod.getObjectPoint(origin, height, degreesPerUnit, imageAngle, ll2,vrControl.getRangeMethods().getCurrentMethod());
		if (p1 == null || p2 == null) {
			return;
		}
		if (p1.x < 0 && p2.x < 0) {
			return;
		}
		if (p1.x > vrControl.getVRPanel().getImageWidth() && p2.x > vrControl.getVRPanel().getImageWidth()) {
			return;
		}
		p1 = vrControl.getVRPanel().imageToScreen(p1);
		p2 = vrControl.getVRPanel().imageToScreen(p2);
		g.setColor(landColour);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		if (vrControl.getVRParams().showShorePoints) {
			landPointSymbol.draw(g, p1);
			landPointSymbol.draw(g, p2);
		}
	}
	
	
	@Override
	public void paintMarks(Graphics g) {
		if (vrControl.getVRParams().getShowShore()) {
			drawLand(g);
		}
		addMeasurementMarks(g);
		addAnimals(g);
	}
	
	/**
	 * Get the current mouse. 
	 * @return
	 */
	protected Point getCurrentMouse() {
		return this.currentMouse;
	}
	

	/**
	 * Get 
	 * @return
	 */
	public PamPanel getSidePanel() {
		return this.sidePanel;
	}
	
	public Double getImageTilt() {
		return imageAnglePanel.getTilt();
	}

	public Double getImageHeading() {
		return imageAnglePanel.getImageHeading(); 
	}

	public PamPanel getImageAnglePanbel() {
		return this.imageAnglePanel;
	}

	@Override
	public PamPanel getRibbonPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamPanel getSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

}
