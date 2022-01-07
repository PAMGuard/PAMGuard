package videoRangePanel.vrmethods;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import PamUtils.LatLong;
import PamView.PamSymbol;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import videoRangePanel.LocationManager;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;
import videoRangePanel.VRMeasurement;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.layoutAWT.VRParametersDialog;
import videoRangePanel.layoutAWT.VRSidePanel;


public abstract class AbstractVRGUIAWT implements VROverlayAWT {
	
	private AbstractVRMethod abstractVRMethod;
	
	//generic components
	private PamLabel calibrationData;
	
	private JComboBox<VRCalibrationData> calibrations;
	
	protected JTextField mapText;
	protected JButton imuSettings;
	private JTextField gps;
	private JButton gpsLocSettings;
	protected VRControl vrControl; 

	public AbstractVRGUIAWT(AbstractVRMethod abstractVRMethod) {
		this.abstractVRMethod=abstractVRMethod; 
		this.vrControl=abstractVRMethod.vrControl; 
	}
	
	/**
	 * Set an instruction. This is handfles by a the higher level GUI in the side pane. 
	 * @param instruction - the instruction to set. 
	 */
	public void setInstruction(JLabel instruction){
		
		if (vrControl.getCurrentImage()==null){ 
			instruction.setText("No Image...");
			return;
		}
		if (vrControl.getVRParams().getCurrentheightData()==null){
			instruction.setText("Add height data...");
			return;
		}
		if (vrControl.getVRParams().getCurrentCalibrationData()==null){
			instruction.setText("Add a calibration value...");
			return;
		}
	}
	

	/**
	 * Creates a panel containing the calibration combo box and labels. 
	 * @return
	 */
	public PamPanel createCalibrationList(){
		
		PamPanel panel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx=0;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, new PamLabel("Calibration"), c);
		c.gridx++;
		PamDialog.addComponent(panel, calibrationData = new PamLabel(""), c);
		c.gridy++;
		c.gridy++;
		c.gridx=0;
		c.gridwidth = 2;
		PamDialog.addComponent(panel, calibrations = new JComboBox<VRCalibrationData>(), c);
		calibrations.addActionListener(new SelectCalibration());
		
		return panel; 
		
	}
	
	/**
	 * Creates a panel allowing the user to select a map and view map file. 
	 * @return
	 */
	public PamPanel createMapFilePanel(){
		
		PamPanel panel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 4;
		PamDialog.addComponent(panel, new PamLabel("Map File"), c);
		c.weightx=1;
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 3;
		PamDialog.addComponent(panel, mapText=new JTextField(10), c);
		mapText.setEditable(false);
		setMapText(); 
		c.weightx=0;
		c.gridx =3;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, imuSettings=new JButton(VRSidePanel.settings), c);
		imuSettings.setPreferredSize(VRPanel.settingsButtonSize);
		imuSettings.addActionListener(new SelectMap());
		
		return panel;
		
	}
	
	private class SelectMap implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			selectMap();
		}
	}
	
	private void selectMap(){
		vrControl.settingsButtonAWT(null,VRParametersDialog.SHORE_TAB);
	}
	
	protected void setMapText(){
		if (mapText==null) return;
		if (vrControl.getVRParams().shoreFile!=null) mapText.setText(vrControl.getVRParams().shoreFile.getName());
		else mapText.setText("no map file");
	}
	
	/**
	 * Create a panel to show GPS location and allow user to change GPS location method. 
	 */
	public PamPanel createLocationListPanel(){
		PamPanel panel=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, new PamLabel("GPS Location"), c);
		c.gridy++;
		c.gridx=0;
		c.gridwidth = 3;
		PamDialog.addComponent(panel, gps=new JTextField(10), c);
		gps.setEditable(false);
		gps.setText("no GPS data");
		c.weightx=0;
		c.gridx =3;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, gpsLocSettings=new JButton(VRSidePanel.settings), c);
		gpsLocSettings.setPreferredSize(VRPanel.settingsButtonSize);
		gpsLocSettings.addActionListener(new SelcetGPSLoc());
		return panel;
	}
	
	private class SelcetGPSLoc implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			vrControl.settingsButtonAWT(null,VRParametersDialog.CAMERA_POS);
		}
		
	}
	
	/**
	 * Add calibration data to the calibration combo box;
	 */
	protected void newCalibration(){
		//TODO
		if (calibrations==null) return; 
		
		ArrayList<VRCalibrationData> calData = vrControl.getVRParams().getCalibrationDatas();
		VRCalibrationData currentSelection = vrControl.getVRParams().getCurrentCalibrationData();
		int currIndex = 0;
		calibrations.removeAllItems();

//		System.out.println("calData: "+ vrControl.getVRParams().getCalibrationDatas());
		if (calData != null) {
			for (int i = 0; i < calData.size(); i++) {
				calibrations.addItem(calData.get(i));
				if (calData.get(i) == currentSelection) {
					currIndex = i;
				}
			}
			if (currIndex >= 0) {
				calibrations.setSelectedIndex(currIndex);
			}
		}
		vrControl.getVRParams().setCurrentCalibration(currentSelection);
	}

	
	void newCalibration(boolean rebuildList) {
		VRCalibrationData vcd = vrControl.getVRParams().getCurrentCalibrationData();
		if (vcd == null) return;
		if (rebuildList) newCalibration();
		calibrations.setSelectedItem(vcd);
		calibrationData.setText(String.format("%.5f\u00B0/px", vcd.degreesPerUnit));
	}
	
	
	class SelectCalibration implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			selectCalibration((VRCalibrationData) calibrations.getSelectedItem());
		}
	}
	
	public void selectCalibration(VRCalibrationData vrCalibrationData) {
		vrControl.getVRParams().setCurrentCalibration(vrCalibrationData);
		newCalibration(false);
	}
	
	
	public void enableControls(){
		//TODO
	}
	
	
	/** Set GPS text. 
	 * @param gpsInfo
	 * @param gps
	 */
	public void setGPSText(LatLong gpsInfo, JTextField gps){
		if (gps==null) return;
		if (gpsInfo==null){
			gps.setText("no GPS Data");
			gps.setToolTipText("no GPS Data");
			return;
		}
		if (vrControl.getLocationManager().getLastSearch()==LocationManager.NO_GPS_DATA_FOUND)  {
			gps.setText("no GPS Data");
			gps.setToolTipText("no GPS Data");
			return;
		}
		String type=vrControl.getLocationManager().getTypeString(vrControl.getLocationManager().getLastSearch());
		gps.setText(type+": "+ gpsInfo);
		gps.setToolTipText(type+": "+ gpsInfo);
		gps.setCaretPosition(0);

	}
	
	
	/***********Overlay Drawing************/
	
	/**
	 * Draw current animals on the image 
	 * @param g - the graphics handle.
	 */
	protected void addAnimals(Graphics g) {
		ArrayList<VRMeasurement> vrms = vrControl.getMeasuredAnimals();
		if (vrms != null) {
			for (int i = 0; i < vrms.size(); i++) {
				drawAnimal(g, vrms.get(i), VRPanel.animalSymbol.getPamSymbol());
			}
		}
		if (abstractVRMethod.getCandidateMeasurement()!= null) {
			drawAnimal(g, abstractVRMethod.getCandidateMeasurement(), VRPanel.candidateSymbol.getPamSymbol());
		}
	}
	
	/**
	 * Draw an animal on the image
	 * @param g - the graphics handle
	 * @param vr - the vr measurement
	 * @param symbol - the symbol 
	 */
	protected void drawAnimal(Graphics g, VRMeasurement vr, PamSymbol symbol) {
		Point p1, p2;
		symbol.draw(g, p1 = vrControl.getVRPanel().imageToScreen(vr.animalPoint));
		p2 = vrControl.getVRPanel().imageToScreen(vr.horizonPoint);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	/**
	 * Draw some marks and lines
	 * @param g - the graphics handle
	 * @param p1 - the first point
	 * @param p2 - the second point 
	 * @param symbol - the current symbol
	 * @param drawLine - draw a line
	 * @param currentMouse - the current mouse position
	 */
	protected void drawMarksandLine(Graphics g, Point p1, Point p2, PamSymbol symbol, boolean drawLine, Point currentMouse) {

		Point sp1 = null, sp2 = null;
		if (p1 != null) {
			symbol.draw(g, sp1 = vrControl.getVRPanel().imageToScreen(p1));
		}
		if (p2 != null) {
			symbol.draw(g, sp2 = vrControl.getVRPanel().imageToScreen(p2));
			if (sp1 != null) {
				g.setColor(symbol.getLineColor());
				g.drawLine(sp1.x, sp1.y, sp2.x, sp2.y);
			}
		}
		if (sp1 != null && sp2 == null && currentMouse != null && drawLine) {
			g.setColor(symbol.getLineColor());
			g.drawLine(sp1.x, sp1.y, currentMouse.x, currentMouse.y);
		}
	}
	
	
	/**
	 * Recieves an update flag 
	 * @param updateType - the update flag. 
	 */
	public void update(int updateType){
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			setGPSText(abstractVRMethod.getGPSinfo(),gps);
			setMapText();
			newCalibration();
			enableControls();
		break;
		case VRControl.IMAGE_CHANGE:
			setGPSText(abstractVRMethod.getGPSinfo(),gps);
		break;
		case VRControl.METHOD_CHANGED:
			break;
		}
	}

}
