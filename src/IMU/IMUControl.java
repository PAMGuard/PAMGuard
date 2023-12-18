package IMU;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import angleMeasurement.AngleDataUnit;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettings;

public class IMUControl  extends PamControlledUnit implements PamSettings {

	private IMUProcess imuProcess;
	private IMUParams imuParams;
	private IMUImportMananger importCSV;
	private IMUControl THIS;
	
	private static final int CAL_VALUES_CHANGED=0; 
	
	public ArrayList<IMUAquisitionMthd> methods =new ArrayList<IMUAquisitionMthd>();

	public IMUControl(String unitName) {
		super("IMU measurment", unitName);
		
		imuParams=new IMUParams();
		addPamProcess(imuProcess = new IMUProcess(this));
		setMethods();
		if (isViewer){
			importCSV=new IMUImportMananger(this);
		}
		THIS=this;

	}
	
	private void setMethods(){
//		ImportCSVMethod importCSVMethod=new ImportCSVMethod(this);
//		methods.add(importCSVMethod);
	}

	
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem;
		//menu item for the real time acquisition methods.
		if (methods.size()>0){
			menuItem = new JMenuItem("Settings ...");
			menuItem.addActionListener(new SettingsMenu(parentFrame));
			menu.add(menuItem);
		}
		//add import capability if in viewer mode
		if (this.isViewer){
			menuItem = new JMenuItem("Import IMU Data...");
			menuItem.addActionListener(new ImportIMUData(parentFrame));
			menu.add(menuItem);
		}
		menuItem = new JMenuItem("Calibration ...");
		menuItem.addActionListener(new CalibrationMenu(parentFrame));
		menu.add(menuItem);
		return menu;
	}
	
	class SettingsMenu implements ActionListener {
		
		public SettingsMenu(Frame parent) {
			super();		
		}

		public void actionPerformed(ActionEvent e) {
			IMUSettingsDialog.showDialog(getGuiFrame(),THIS);
		}
	}
	
	/**
	 * Open the import data dialog. 
	 * @author Jamie Macaulay
	 *
	 */
	class ImportIMUData implements ActionListener {
		
		public ImportIMUData(Frame parent) {
			super();
		}

		public void actionPerformed(ActionEvent e) {
			IMUParams newIMUParams=IMUImportDialog.showDialog(getGuiFrame(),THIS, importCSV);
			//if params are not null try and load data 
			if (newIMUParams!=null){
				imuParams=newIMUParams; 
				//check that we have a file in the list. 
				if (imuParams.lastCSVFiles.size()>0) {
					importCSV.startImport( imuParams.lastCSVFiles.get(0));
				}
			}
		}
	}
	 
	/**
	 * Open the calibration dialog
	 * @author Jamie Macaulay
	 *
	 */
	class CalibrationMenu implements ActionListener {
		Frame parent;
		
		public CalibrationMenu(Frame parent) {
			super();
			this.parent = parent;
		}

		public void actionPerformed(ActionEvent e) {
			IMUParams newIMUParams=IMUCalibrationDialog.showDialog(getGuiFrame(),imuParams);
			if (newIMUParams!=null) imuParams=newIMUParams; 
			updateProcesses(CAL_VALUES_CHANGED);
		}
	}
	
	public void updateProcesses(int updateType){
		for (int i=0; i<THIS.getNumPamProcesses(); i++){
			THIS.getPamProcess(0).noteNewSettings();;
		}
	
	}
	

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IMUParams getParams() {
		return imuParams;
	}

	public ArrayList<IMUAquisitionMthd> getMethods() {
		return methods;
	}

	public IMUDataBlock getDataBlock() {
		return 	imuProcess.getIMUDataBlock();
	}

	public void addDataToDataBlock(AngleDataUnit pamDataUnit) {
		imuProcess.getIMUDataBlock().addPamData(pamDataUnit);
		
	}

}
