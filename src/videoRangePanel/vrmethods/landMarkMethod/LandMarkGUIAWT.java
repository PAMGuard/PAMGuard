package videoRangePanel.vrmethods.landMarkMethod;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import videoRangePanel.VRControl;
import videoRangePanel.VRMeasurement;
import videoRangePanel.VRSymbolManager;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.layoutAWT.VRParametersDialog;
import videoRangePanel.layoutAWT.VRSidePanel;
import videoRangePanel.vrmethods.AbstractVRGUIAWT;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.ImageRotRibbonPanel;

/**
 * Contains GUI components for the landmark method. 
 * @author Jamie Macaulay
 *
 */
public class LandMarkGUIAWT extends AbstractVRGUIAWT {
	
	
	
	private PamPanel sidePanel;
	private PamLabel instruction;
	private JButton clearLM;
	private ImageRotRibbonPanel ribbonPanel;
//	private LandMarkMethodUI landMarkUI;
	private JComboBox<LandMarkGroup> landMarkList;
	private JButton lmSettings;
	
	
	private VRLandMarkMethod vrLandMarkmethod;
	
	//Graphics
	final static float dash1[] = {2.0f};
	public final static BasicStroke dashedtmplate =
	        new BasicStroke(2.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        5.0f, dash1, 0.0f);
	
	 public final static BasicStroke solid =
		        new BasicStroke(2f);
	 
	public final static Color landMarkCol=Color.GREEN;
	public final static Color animalCol=Color.CYAN;
	 
	public static PamSymbol landMarkMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, landMarkCol, landMarkCol);
	public static PamSymbol animalMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, animalCol, animalCol);
	public static PamSymbol candAnimalMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.red, Color.red);
	public static VRSymbolManager landMarksSymbol = new VRSymbolManager(landMarkMarker, "Landmark");
	public static VRSymbolManager animalMark = new VRSymbolManager(animalMarker, "LandMark Animal");

	
	/**
	 */
	private VRControl vrControl;

	public LandMarkGUIAWT(VRLandMarkMethod vrLandMarkmethod) {
		super(vrLandMarkmethod); 
		this.vrLandMarkmethod=vrLandMarkmethod; 
		this.vrControl=vrLandMarkmethod.getVRControl(); 
		sidePanel=createSidePanel(); 
		this.ribbonPanel=new ImageRotRibbonPanel(vrControl);
	}
	
	
	public PamPanel createSidePanel(){
		PamLabel lab;
		PamPanel panel=new PamPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy=0;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, lab=new PamLabel("Landmark Measurement "), c);
		lab.setFont(PamColors.getInstance().getBoldFont());
		
		c.gridx=0;
		c.gridy++;
		PamDialog.addComponent(panel, createLandMarkPanel(), c);

		c.gridy++;
		PamDialog.addComponent(panel, createLocationListPanel(), c);
		
		c.gridy++;
		PamDialog.addComponent(panel, instruction = new PamLabel("..."), c);
		instruction.setFont(PamColors.getInstance().getBoldFont());
		
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, clearLM = new JButton("Clear LandMarks"), c);
		clearLM.addActionListener(new ClearLandMarks());
		c.gridy++;
		c.gridy++;
	
		
		setInstruction(this.vrLandMarkmethod.getCurrentStatus());
		
		return panel;
	}
	
	
	/**
	 * Create panel whihc shows the current landmark groups. 
	 * @return pane with controls to change landmark group.  
	 */
	private PamPanel createLandMarkPanel(){
		PamPanel panel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridwidth = 3;
		PamDialog.addComponent(panel, landMarkList=createLandMarkList(), c);
		setLandMarks();
		landMarkList.addActionListener(new SelectLandMarkGroup());
		c.weightx = 0;
		c.gridx =3;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, lmSettings=new JButton(VRSidePanel.settings), c);
		lmSettings.setPreferredSize(VRPanel.settingsButtonSize);
		lmSettings.addActionListener(new SelectLandMarkSettings());
		return panel;
	}
	
	

	private JComboBox<LandMarkGroup> createLandMarkList() {
		return new JComboBox<LandMarkGroup>();
	}

	
	/**
	 * Clear values on the ribbon panel. 
	 */
	private void clearRibbonPanel(){
		ribbonPanel.clearImageLabels();
		ribbonPanel.clearAnimalLabels();
	}
	
	
	/**
	 * Create a landmark list for the landmark combo box on side panel;  
	 */
	public void setLandMarks(){
		
		ArrayList<LandMarkGroup> landMarkGroups=vrControl.getVRParams().getLandMarkDatas();
		landMarkList.removeAllItems();
		if (landMarkGroups==null) return;
		if (landMarkGroups.size()==0) return;

		for (int i=0; i<landMarkGroups.size(); i++){
			landMarkList.addItem(landMarkGroups.get(i));
		}
		
		landMarkList.setSelectedIndex(vrControl.getVRParams().getSelectedLandMarkGroup());
		
	}
	
	public void setInstruction(int currentStatus) {
		
		switch (currentStatus) {
		case VRLandMarkMethod.SET_LANDMARK:
			instruction.setText("Add Landmark");
			break;
		case VRLandMarkMethod.SET_LANDMARK_READY:
			instruction.setText("Add Landmark/Click Animal");
			break;
		case VRLandMarkMethod.MEASURE_ANIMAL:
			instruction.setText("Click animal");
			break;
		}
		
		setInstruction(instruction);
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
	
	
	
	
	public void setImageLabels(VRMeasurement imageMeasurement){
		if (imageMeasurement==null) ribbonPanel.clearImageLabels();
		else {
			ribbonPanel.setImageBearing(imageMeasurement.imageBearing,imageMeasurement.imageBearingErr);
			ribbonPanel.setImagePitch(imageMeasurement.imagePitch,imageMeasurement.imagePitchErr);
			ribbonPanel.setImageTilt(imageMeasurement.imageTilt,imageMeasurement.imageTiltErr);
		}
	}
	
	public void setAnimalLabels(VRMeasurement imageMeasurement){
		if (imageMeasurement.locBearing==null)  ribbonPanel.clearImageLabels();
		else{
			ribbonPanel.setAnimalBearing(Math.toRadians(imageMeasurement.locBearing), Math.toRadians(imageMeasurement.locBearingError));
			ribbonPanel.setAnimalDistance(imageMeasurement.locDistance, imageMeasurement.locDistanceError);
		}
	}
	
	private class SelectLandMarkGroup implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			vrControl.getVRParams().setCurrentLandMarkGroupIndex(landMarkList.getSelectedIndex());
			clearOverlay();
		}
	}
	
	private class SelectLandMarkSettings implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			vrControl.settingsButtonAWT(null,VRParametersDialog.LAND_MARK);
		}
	}
	
	private class ClearLandMarks implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearOverlay();
		}
	}
	
	class SetAnimal implements ActionListener{

		Point point;
		
		protected SetAnimal(Point point){
			this.point=point;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			vrLandMarkmethod.measureAnimal(this.point);
			setInstruction(vrLandMarkmethod.getCurrentStatus());
		}
	}
	
	
	/******Pop Up Menu********/
	
	/**
	 * Create a dynamic pop up menu to allow users to select landmarks and animals. 
	 * @param point- current mouse point. 
	 * @return the pop up menu
	 */
	private JPopupMenu showlmPopUp(Point point){
		
		JPopupMenu popMenu=new JPopupMenu();
		JMenuItem menuItem;
		LandMarkGroup landMarks=vrControl.getVRParams().getLandMarkDatas().get(vrControl.getVRParams().getSelectedLandMarkGroup());
		int n=0;
		if (vrLandMarkmethod.getCurrentStatus()!=AbstractVRMethod.MEASURE_ANIMAL){
			for (int i=0; i<landMarks.size(); i++){
				if (!vrLandMarkmethod.isInList(landMarks.get(i))){
					menuItem=new JMenuItem(landMarks.get(i).getName());
					menuItem.addActionListener(new SetLandMark(landMarks.get(i),point));
					popMenu.add(menuItem);
					n++;
				}
			}
			//if ready to calculate then give the user the option of calculating a position
			if (vrLandMarkmethod.isCalcReady()){
				if (n!=0) popMenu.addSeparator();
				menuItem=new JMenuItem("Calc Orientation");
				menuItem.addActionListener(new Calculate());
				popMenu.add(menuItem);
			}
			
			popMenu.addSeparator();
		}
		
		if (vrLandMarkmethod.isCalcReady()){
			menuItem=new JMenuItem("Measure Animal");
			menuItem.addActionListener(new SetAnimal(point));
			popMenu.add(menuItem);
		}
		
		menuItem=new JMenuItem("Clear");
		menuItem.addActionListener(new ClearLandMarks());
		popMenu.add(menuItem);
		
		return popMenu;
	}
	
	class SetLandMark implements ActionListener{
		
		LandMark landMark;
		Point point;
		
		protected SetLandMark(LandMark landMark, Point point){
			this.landMark=landMark;
			this.point=point;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			vrLandMarkmethod.setLandMark(landMark, point); 
			setInstruction(vrLandMarkmethod.getCurrentStatus());
			
		}
	}
	
	private class Calculate implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			vrLandMarkmethod.setCurrentStatus(AbstractVRMethod.MEASURE_ANIMAL);
			vrLandMarkmethod.calcVals();
			vrControl.getVRPanel().repaint();
			VRMeasurement imageMeasurement=new VRMeasurement();
			vrLandMarkmethod.setImagePosVals(imageMeasurement);
			setImageLabels(imageMeasurement);
			setInstruction(vrLandMarkmethod.getCurrentStatus());
		}
		
	}
	
	JPopupMenu menu;

	public void mouseAction(MouseEvent e, boolean motion) {
		boolean vis=isMenuVisible();
		
		if (e.isPopupTrigger()){
		    menu=showlmPopUp(e.getPoint());
			menu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);
		}
		//if there are enough landmark points and popup menu is not visible then a double click will measure animal location. 
		else if (e.getClickCount()>1 && e.getButton()==MouseEvent.BUTTON1 && !vis){
			if (vrLandMarkmethod.isCalcReady()) vrLandMarkmethod.measureAnimal(e.getPoint());
		}
		vrControl.getVRPanel().repaint();
	}
	

	private boolean isMenuVisible(){
		if (menu==null) return false; 
		return menu.isVisible();
	}

	/**
	 * Get the side panel.
	 * @return the side panel 
	 */
	public PamPanel getSidePane() {
		return this.sidePanel;
	}

	/**
	 * Get the ribbon panel. 
	 * @return the ribbon panel.
	 */
	public PamPanel getRibbonPanel() {
		return this.ribbonPanel;
	}
	
	/**
	 * Update the GUI. 
	 * @param updateType - the update flag. 
	 */
	public void update(int updateType) {
		super.update(updateType);
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			setLandMarks();
			break;
		case VRControl.HEADING_UPDATE:

			break;
		case VRControl.TILT_UPDATE:

			break;
		case VRControl.IMAGE_CHANGE:
			setInstruction(updateType);
			break;
		case VRLandMarkMethod.NEW_MESURMENT:
			setAnimalLabels(vrControl.getMeasuredAnimals().get(vrControl.getMeasuredAnimals().size()-1));
			break;
		}
	}


	@Override
	public PamPanel getSidePanel() {
		return this.sidePanel;
	}


	@Override
	public PamPanel getSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	/*************Overlay Drawing ***************/
	

	@Override
	public void paintMarks(Graphics g) {
		addMeasurementMarks(g);
		addAnimals(g);
	}

	@Override
	protected void addAnimals(Graphics g) {
		drawCandAnimals(g, candAnimalMarker);
		drawAnimals(g, animalMarker);
	}   
	
	private void drawAnimals(Graphics g, PamSymbol pamSymbol) {

		//check for previous animals
		if (vrControl.getMeasuredAnimals()==null) return;
				
		if (vrLandMarkmethod.getLandMarkPoints()==null) return;
		if (vrLandMarkmethod.getLandMarkPoints().size()==0) return;
		
		for (int i=0; i<vrControl.getMeasuredAnimals().size(); i++){
			drawAnimal( g, vrControl.getMeasuredAnimals().get(i), pamSymbol); 
		}
	}
	
	/**
	 * Draw the candidate measurement.
	 * @param g - graphics handle
	 * @param pamSymbol
	 */
	private void drawCandAnimals(Graphics g, PamSymbol pamSymbol) {

		if (vrLandMarkmethod.getCandidateMeasurement()==null) return;
				
		if (vrLandMarkmethod.getLandMarkPoints()==null) return;
		if (vrLandMarkmethod.getLandMarkPoints().size()==0) return;
		
		drawAnimal( g, vrLandMarkmethod.getCandidateMeasurement(), pamSymbol); 
	}
	
	
	/**
	 * Draw all animal measurements for the image. 
	 */
	protected void drawAnimal(Graphics g, VRMeasurement vr, PamSymbol symbol) {
				
		Graphics2D g2=(Graphics2D) g;

		Point sp1;
		Point pA;
		Point anP=vrControl.getVRPanel().imageToScreen(vr.animalPoint);
		for (int i=0; i<vrLandMarkmethod.getLandMarkPoints().size(); i++){
			sp1=vrControl.getVRPanel().imageToScreen(vrLandMarkmethod.getLandMarkPoints().get(i));
			g2.setColor(symbol.getLineColor());
			g2.setStroke(solid);
			g2.drawLine(sp1.x, sp1.y, anP.x, anP.y);
			//draw the bearing and pitch lines
			pA=VRLandMarkMethod.calcPerpPoint(vrLandMarkmethod.getTilt(),VRLandMarkMethod.calcBearingDiffAnimal(vrLandMarkmethod.getTilt(), sp1, anP),anP);
			g2.setStroke(dashedtmplate);
			g.drawLine(sp1.x, sp1.y, pA.x, pA.y);
			g.drawLine(anP.x, anP.y, pA.x, pA.y);
		}
		//draw the animal circle
		symbol.draw(g, anP);

	}


	private void addMeasurementMarks(Graphics g) {
		drawLandMarks(g, landMarksSymbol.getPamSymbol());
	}

	/**
	 * Draw the landmark measurment points. 
	 * @param g - the landmark measurment points. 
	 * @param symbol 
	 */
	protected void drawLandMarks(Graphics g, PamSymbol symbol){
		
		Graphics2D g2=(Graphics2D) g;
		
		if (vrLandMarkmethod.getLandMarkPoints()==null) return;
		if (vrLandMarkmethod.getLandMarkPoints().size()==0) return;
		
		Point sp1 = null;
		Point sp2 = null;
		
		if (vrLandMarkmethod.getLandMarkPoints() != null) {
			for (int i=0; i<vrLandMarkmethod.getLandMarkPoints().size(); i++){
				Point landMarkPoint = new Point(); 
				landMarkPoint.setLocation(vrLandMarkmethod.getLandMarkPoints().get(i));
				
				symbol.draw(g2, sp1 = vrControl.getVRPanel().imageToScreen(landMarkPoint));
			}
		}
		
		if (vrLandMarkmethod.getCurrentStatus()==AbstractVRMethod.MEASURE_ANIMAL){
			if (vrLandMarkmethod.getTiltVals()==null) return;
			Point perpPoint;
			ArrayList<Integer> indexM1 = PamUtils.indexM1(vrLandMarkmethod.getLandMarkPoints().size());
			ArrayList<Integer> indexM2 = PamUtils.indexM2(vrLandMarkmethod.getLandMarkPoints().size());
			for (int i=0; i<indexM1.size(); i++){
				g.setColor(symbol.getLineColor());
				sp1=vrControl.getVRPanel().imageToScreen(vrLandMarkmethod.getLandMarkPoints().get(indexM1.get(i)));
				sp2=vrControl.getVRPanel().imageToScreen(vrLandMarkmethod.getLandMarkPoints().get(indexM2.get(i)));
				//calc the tilt
//				System.out.println("LandMark: "+indexM1.get(i)+" : "+indexM2.get(i)+" tilt: "+Math.toDegrees(tiltVals.get(i)));
				perpPoint=vrLandMarkmethod.calcPerpPoint(vrLandMarkmethod.getTiltVals().get(i), vrLandMarkmethod.getLandMarkPoints().get(indexM1.get(i)), 
						vrLandMarkmethod.getLandMarkPoints().get(indexM2.get(i)),  vrLandMarkmethod.getSetLandMarks().get(indexM1.get(i)),  
						vrLandMarkmethod.getSetLandMarks().get(indexM2.get(i)), vrLandMarkmethod.getSetLandMarks().get(0).getLatLongOrigin());
				perpPoint=vrControl.getVRPanel().imageToScreen(perpPoint);
				g2.setColor(landMarkCol);
				g2.setStroke(solid);
				g2.drawLine(sp1.x, sp1.y, sp2.x, sp2.y);
				g2.setStroke(dashedtmplate);
				g.drawLine(sp1.x, sp1.y, perpPoint.x, perpPoint.y);
				g.drawLine(sp2.x, sp2.y, perpPoint.x, perpPoint.y);
			}
		}
		
	}
	
	
	
	

}
