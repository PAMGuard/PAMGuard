package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import videoRangePanel.LocationManager;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.VRHorzCalcMethod;
import videoRangePanel.VRParameters;
import videoRangePanel.vrmethods.landMarkMethod.LandMark;
import videoRangePanel.vrmethods.landMarkMethod.LandMarkGroup;
import angleMeasurement.AngleDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;

public class VRParametersDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPanel;
	
	private static VRParametersDialog singleInstance;
	
	private ImagePanel imagePanel;
	
	private CalibrationPanel calibrationPanel;
	
	private CalcPanel calcPanel;
	
	private HeightPanel heightPanel;
	
	private AnglesPanel anglesPanel;
	
	private ShorePanel shorePanel; 
	
	private VRControl vrControl;
	
	private VRParameters vrParameters;
	
	private Frame parentFrams;
	
	private VRParametersDialog THIS;

	private LandGroupMarkPanel landMarkPanel;

	private ImageLocationPanel cameraLocationPanel;

	private TidePanel tidePanel;
	
	public final static int CALC_TAB=0;
	public final static int HEIGHT_TAB=1;
	public final static int CAL_TAB=2;
	public final static int LAND_MARK=3;
	public final static int CAMERA_POS=4;
	public final static int IMAGE_TAB=5;
	public final static int ANGLE_TAB=6;
	public final static int SHORE_TAB=7;

	
	
	private VRParametersDialog(Frame parentFrame, VRControl vrControl) {
		super(parentFrame, "Video Range Settings", false);
		this.vrControl = vrControl;
		this.parentFrams = parentFrame;
		THIS = this;
		tabbedPanel = new JTabbedPane();
		calcPanel = new CalcPanel();
		calibrationPanel = new CalibrationPanel();
		imagePanel = new ImagePanel();
		heightPanel = new HeightPanel();
		anglesPanel = new AnglesPanel();
		shorePanel = new ShorePanel();
		tidePanel= new TidePanel(); 
		landMarkPanel=new LandGroupMarkPanel();
		cameraLocationPanel=new ImageLocationPanel();
		tabbedPanel.add("Calculation", calcPanel);
		tabbedPanel.add("Heights", heightPanel);
		tabbedPanel.add("Calibration", calibrationPanel);
		tabbedPanel.add("Land Mark Groups",landMarkPanel);
		tabbedPanel.add("Camera Locations ",cameraLocationPanel);
		tabbedPanel.add("Display", imagePanel);
		tabbedPanel.add("Angles", anglesPanel);
		tabbedPanel.add("Shore", shorePanel);
		tabbedPanel.add("Tide", tidePanel);

		
		setResizable(true);
		
		setDialogComponent(tabbedPanel);
	}
	
	public JTabbedPane getTabbedPanel() {
		return tabbedPanel;
	}

	private static void prepDialog(Frame frame, VRControl vrControl){
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new VRParametersDialog(frame, vrControl);
		}
		singleInstance.vrControl = vrControl;
		singleInstance.vrParameters = vrControl.getVRParams().clone();
		singleInstance.setParams() ;
	}
	
	public static VRParameters showDialog(Frame frame, VRControl vrControl) {
		prepDialog(frame,  vrControl);
		singleInstance.setVisible(true);
		
		return singleInstance.vrParameters;
	}
	
	public static VRParameters showDialog(Frame frame, VRControl vrControl, int tab) {
		prepDialog(frame,  vrControl);
		if (tab<singleInstance.getTabbedPanel().getTabCount()) singleInstance.getTabbedPanel().setSelectedIndex(tab);
		singleInstance.setVisible(true);
		
		return singleInstance.vrParameters;
	}


	@Override
	public void cancelButtonPressed() {
		vrParameters = null;
	}
	
	private void setParams() {
		calibrationPanel.setParams();
		imagePanel.setParams();
		calcPanel.setParams();
		heightPanel.setParams();
		anglesPanel.setParams();
		shorePanel.setParams();
		landMarkPanel.setParams();
		cameraLocationPanel.setParams();
		tidePanel.setParams();
	}

	@Override
	public boolean getParams() {
		if (calibrationPanel.getParams() == false) {
			return false;
		}
		if (imagePanel.getParams() == false) {
			return false;
		}
		if (calcPanel.getParams() == false) {
			return false;
		}
		if (heightPanel.getParams() == false) {
			return false;
		}
		if (anglesPanel.getParams() == false) {
			return false;
		}
		if (shorePanel.getParams() == false) {
			return false;
		}
		if (landMarkPanel.getParams() == false) {
			return false;
		}
		if (cameraLocationPanel.getParams() == false) {
			return false;
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	class JCheckBoxLocation extends JCheckBox{
		
		public JCheckBoxLocation(String name){
			super(name);
			this.setName(name);
		}
		
		private int locationType;
		
		public int getLocationType(){
			return locationType;
		}
		
		public void setLocationType(int locationType){
			this.locationType=locationType;
		}
		
	}
	
	
	class ImageLocationPanel extends LandMarkPanel{
		
		private JCheckBoxLocation pamguardBoatGPS;
		private JCheckBoxLocation photoGeoTag;
		private JCheckBoxLocation manualLoc;
		private JCheckBoxLocation landMark; 
		
		protected ArrayList<JCheckBoxLocation> checkBoxes=new ArrayList<JCheckBoxLocation>();
		protected ArrayList<JSpinner> prioritySpinners=new ArrayList<JSpinner>();
		
		private  LandMarkGroup localGPSMarkList;
		private int[] localMethodsList;
		
		int currentlySelected=0;

		public ImageLocationPanel(){
			super(vrControl);
			
			pamguardBoatGPS = new JCheckBoxLocation("GPS Data Block");
			pamguardBoatGPS.setLocationType(LocationManager.ARRAY_MANAGER);
			pamguardBoatGPS.addActionListener(new CheckBoxSel());
			photoGeoTag = new JCheckBoxLocation("Image Geo Tag");
			photoGeoTag.setLocationType(LocationManager.PHOTO_TAG);
			photoGeoTag.addActionListener(new CheckBoxSel());
			manualLoc=new JCheckBoxLocation("Manual Location");
			manualLoc.setLocationType(LocationManager.MANUAL_INPUT);
			manualLoc.addActionListener(new CheckBoxSel());
			landMark=new JCheckBoxLocation("LandMark Measurment");
			landMark.setLocationType(LocationManager.LANDMARK_GROUP);
			//landMark is a mandatory method if using landmarks- set disabled and selected. 
			landMark.setSelected(true);
			landMark.setEnabled(false);
			
			checkBoxes.add(pamguardBoatGPS);
			checkBoxes.add(photoGeoTag);
			checkBoxes.add(manualLoc);
			
			vrControl.getLocationManager();
			
			//note: in the same order as checkboxes. 
			for (int i=0; i<LocationManager.getAllMethods().length; i++){
				prioritySpinners.add( new JSpinner(new SpinnerNumberModel(1, 0, LocationManager.getAllMethods().length-1, 1)));
				prioritySpinners.get(i).addChangeListener(new PrioritySpinnerChanged(prioritySpinners.get(i)));
//				prioritySpinners.get(i).add
			}
		
			
			//create the panel; 
			int insestX=60;
			int gridWidthChkBx=4;
			PamPanel priorityPanel=new PamPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.gridx=0;
			c.gridy=0;
			c.gridwidth = gridWidthChkBx;  //top padding
//			c.ipadx=100;
			PamDialog.addComponent(priorityPanel, pamguardBoatGPS, c);
			c.insets = new Insets(0,insestX,0,0);
			c.gridwidth = 1;
			c.gridx=4;
			PamDialog.addComponent(priorityPanel, prioritySpinners.get(0), c);
			c.insets = new Insets(0,0,0,0);
			c.gridy++;
			c.gridx=0;
			c.gridwidth =gridWidthChkBx;
			PamDialog.addComponent(priorityPanel, photoGeoTag, c);
			c.insets = new Insets(0,insestX,0,0);
			c.gridwidth = 1;
			c.gridx=4;
			PamDialog.addComponent(priorityPanel,  prioritySpinners.get(1), c);
			c.insets = new Insets(0,0,0,0);
			c.gridy++;
			c.gridx=0;
			c.gridwidth = gridWidthChkBx;
			PamDialog.addComponent(priorityPanel, manualLoc, c);
			c.insets = new Insets(0,insestX,0,0);
			c.gridwidth = 1;
			c.gridx=4;
			PamDialog.addComponent(priorityPanel,  prioritySpinners.get(2), c);
			c.insets = new Insets(0,0,0,0);
			c.gridy++;
			c.gridx=0;
			c.gridwidth = gridWidthChkBx;
			PamDialog.addComponent(priorityPanel, landMark, c);

			tableData=new LocationInputTable();
			super.createPanel(tableData);
			
			this.add(BorderLayout.NORTH,priorityPanel);
			addButton.addActionListener(new AddButton());
			editbutton.addActionListener(new EditButton());
			deleteButton.addActionListener(new DeleteButtonSel());
			
		}
		
		class PrioritySpinnerChanged implements ChangeListener{

			private JSpinner prioritySpinner;
			private int lastValue;

			public PrioritySpinnerChanged(JSpinner prioritySpinner) {
				this.prioritySpinner=prioritySpinner;
				this.lastValue=(int) prioritySpinner.getValue();
			}

			@Override
			public void stateChanged(ChangeEvent arg0) {
				for (int i=0; i<prioritySpinners.size(); i++){
					if (prioritySpinners.get(i).equals(prioritySpinner)) continue;
					if (prioritySpinners.get(i).getValue()==prioritySpinner.getValue()){
//						System.out.println("Same Value: spinner val: "+ prioritySpinners.get(i).getValue()+" spiner no. "+i + " lastValue: "+lastValue);
						prioritySpinners.get(i).setValue(lastValue);
					}
				}
				lastValue=(int) prioritySpinner.getValue();
			}

//			@Override
//			public void stateChanged(ChangeEvent arg0) {
//				// TODO Auto-generated method stub
//				
//			}
							
		}
		
		class CheckBoxSel implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (int i=0; i<checkBoxes.size(); i++){
					prioritySpinners.get(i).setEnabled(checkBoxes.get(i).isSelected());
				}
			}
			
		}
		
		//make sure that if the selected location is deleted then the first location in the list is selected
		class DeleteButtonSel extends DeleteButton {

			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				if (currentlySelected>=localGPSMarkList.size());
				currentlySelected=0;
				tableData.fireTableDataChanged();
			}
			
		}
		
		@Override
		public void setParams() {
			localGPSMarkList = vrParameters.getManualGPSDatas();
			currentlySelected=vrParameters.getCurrentManualGPSIndex();
			tableData.fireTableDataChanged();
			setPriorityParams();
		}
		
		public void setPriorityParams(){
			localMethodsList=vrControl.getLocationManager().getCurrentMethods();
			//set the check boxes required. 
			boolean sel=false;
			for (int i=0; i<checkBoxes.size();i++){
				sel=false;
				for (int j=0; j<localMethodsList.length;j++){
					if (localMethodsList[j]==checkBoxes.get(i).getLocationType()){
						sel=true;
					}
				}
				
				checkBoxes.get(i).setSelected(sel);
				prioritySpinners.get(i).setEnabled(sel);
				prioritySpinners.get(i).setValue(vrControl.getLocationManager().getPriority(checkBoxes.get(i).getLocationType()));
			}
		}
		
		
		@Override
		protected LandMark getDialog(LandMark landMark){
			return ManualLocationDialog.showDialog(parentFrams, vrControl, landMark);
		}
		
		@Override
		protected  LandMarkGroup getLandMarkList(){
			return localGPSMarkList;
		}
		
		@Override
		protected  void  setLandMarkList(LandMarkGroup localGPSMarkList){
			this. localGPSMarkList=localGPSMarkList;
		}
		
		public boolean getParams() {
			vrParameters.setGPSLocData(localGPSMarkList);
			vrParameters.setGPSLocDataSelIndex(currentlySelected);
			getPrioirtyParams();
			//TODO
			vrControl.getLocationManager().setCurrentMethods(localMethodsList);
			if (localGPSMarkList == null) {
				return true;
			}
			if (currentlySelected < 0 || (localGPSMarkList != null && currentlySelected >= localGPSMarkList.size()) && currentlySelected!=0) {
//				System.out.println("ImageLocationPanel getParams: "+false);
				return false;
			}
			return true;
		}
		
		
		public void getPrioirtyParams(){
			
			Integer[] methods=new Integer[LocationManager.getAllMethods().length];
			Arrays.fill(methods, null);
			
			for (int i=0; i<checkBoxes.size();i++){
				if (checkBoxes.get(i).isSelected()){
					methods[(int) prioritySpinners.get(i).getValue()]=checkBoxes.get(i).getLocationType();
				}
			}
			//so now have a methods array which is in the correct order but may have negative ones. Need to get rid of these. 
			ArrayList newMethods=new ArrayList<Integer>();
			for (int j=0; j<methods.length; j++){
				if (methods[j]!=null) newMethods.add(methods[j]);
			}
			
			localMethodsList=new int[newMethods.size()];
			for (int k=0; k<newMethods.size(); k++){
				localMethodsList[k]=(int) newMethods.get(k);
			}			
		}
		
		
		class LocationInputTable extends AbstractTableModel {


			private static final long serialVersionUID = 1L;
			
			public int getColumnCount() {
				return 4;
			}

			public int getRowCount() {
				if (localGPSMarkList == null) {
					return 0;
				}
				return localGPSMarkList.size();
			}
			
			private String[] columnNames = {"Current Loc","Name", "Lat", "Long"};
			@Override
			public String getColumnName(int column) {
				return columnNames[column];
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (localGPSMarkList == null) {
					return null;
				}
				LandMark cd = localGPSMarkList.get(rowIndex);
				if (cd == null) {
					return "Unknown";
				}
				switch (columnIndex) {
				case 0:
					return rowIndex == currentlySelected;
				case 1:
					return cd.getName();
				case 2:
					return cd.getPosition().getLatitude();
				case 3:
					return cd.getPosition().getLongitude();
				}
				return null;
			}
			
			
			@Override
			public boolean isCellEditable(int row, int col) {
				return (col == 0);
			}
			
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) {
					return Boolean.class;
				}
				return super.getColumnClass(columnIndex);
			}
			
			@Override
			public void setValueAt(Object aValue, int row, int col) {
				if (col == 0) {
					currentlySelected=row;
					tableData.fireTableDataChanged();
					list.setRowSelectionInterval(row, row);
				}
				else {
					super.setValueAt(aValue, row, col);
				}
			}
		}
		
	}
	
//	protected createImageLocationSel(){
//		
//			PamPanel panel=new PamPanel(new GridBagLayout());
//			GridBagConstraints c = new GridBagConstraints();
//			c.gridy++;
//			c.gridx=0;
//			c.gridwidth = 2;
//			addComponent(panel, new JCheckBo, c);
//		
//	}
	
	class LandGroupMarkPanel extends AbstractVRTabelPanel{
			
		ArrayList<LandMarkGroup> landMarkGroupList;
		protected AbstractTableModel tableData;
		private JButton importGroup;

		
		public LandGroupMarkPanel() {
			super();
			this.add(BorderLayout.NORTH, new JLabel("Select and manage calibration data"));
			this.tableData=new LandMarkGroupTable() ;
			super.createPanel(tableData);
			buttonPanel.removeAll();
			buttonPanel.add(importGroup=new JButton("Import..."));
			buttonPanel.add(addButton);
			buttonPanel.add(editbutton);
			buttonPanel.add(deleteButton);
			addButton.addActionListener(new AddButton());
			editbutton.addActionListener(new EditButton());
			deleteButton.addActionListener(new DeleteButton());
		}
		
		class AddButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				LandMarkGroup landMarkGroup = LandMarkGroupDialog.showDialog(parentFrams, vrControl, new LandMarkGroup());
				if (landMarkGroupList == null) {
					landMarkGroupList = new ArrayList<LandMarkGroup>();
				}
				if (landMarkGroup != null) {
					landMarkGroupList.add(landMarkGroup);
					tableData.fireTableDataChanged();
					int lastRow = landMarkGroupList.size()-1;
					list.setRowSelectionInterval(lastRow, lastRow);
				}
			}
		}
		
		class EditButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				int row = list.getSelectedRow();
				if (row >= 0&& row < landMarkGroupList.size()) {
					LandMarkGroup lndMrkG = landMarkGroupList.get(row);
					LandMarkGroup landMarkGroup  = LandMarkGroupDialog.showDialog(parentFrams, vrControl, lndMrkG);
					if (landMarkGroup!= null) {
						landMarkGroupList.get(row).update(landMarkGroup);
						tableData.fireTableDataChanged();
						list.setRowSelectionInterval(row, row);
					}
				}
			}
			
		}
		
		class DeleteButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				int row = list.getSelectedRow();
				if (landMarkGroupList == null) return;
				if (row < 0 || row >= landMarkGroupList.size()) {
					return;
				}
				landMarkGroupList.remove(row);

				tableData.fireTableDataChanged();
			}
			
		}
		
		public void setParams(){
			landMarkGroupList = vrParameters.getLandMarkDatas();
			tableData.fireTableDataChanged();
		}
		
		public boolean getParams(){
			vrParameters.setLandMarkDatas(landMarkGroupList);
			return true;
		}
		
		class LandMarkGroupTable extends AbstractTableModel {

			public int getColumnCount() {
				return 3;
			}

			public int getRowCount() {
				if (landMarkGroupList == null) {
					return 0;
				}
				return landMarkGroupList.size();
			}
			private String[] columnNames = {"Name","No. LandMarks", "Comment"};
			@Override
			public String getColumnName(int column) {
				return columnNames[column];
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (landMarkGroupList == null) {
					return null;
				}
				LandMarkGroup cd = landMarkGroupList.get(rowIndex);
				if (cd == null) {
					return "Unknown";
				}
				switch (columnIndex) {
				case 0:
					return cd.getName();
				case 1:
					return cd.size();
				case 2:
					return cd.getComment();
				}
				return null;
			}
			
			@Override
			public boolean isCellEditable(int row, int col) {
				return (col == 2);
			}
			
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 2) {
					return String.class;
				}
				return super.getColumnClass(columnIndex);
			}
			
			@Override
			public void setValueAt(Object aValue, int row, int col) {
				if (col == 2) {
					landMarkGroupList.get(row).setCommentText((String) aValue);
					tableData.fireTableDataChanged();
					list.setRowSelectionInterval(row, row);
				}
				else {
					super.setValueAt(aValue, row, col);
				}
			}
			
		}

	}
	
	
	class CalibrationPanel extends AbstractVRTabelPanel {
		
		ArrayList<VRCalibrationData> localCalList;
		AbstractTableModel tableData;
		
		public CalibrationPanel() {
			super();
			this.add(BorderLayout.NORTH, new JLabel("Select and manage calibration data"));
			this.tableData=new CalTableData() ;
			super.createPanel(tableData);
			addButton.addActionListener(new AddButton());
			editbutton.addActionListener(new EditButton());
			deleteButton.addActionListener(new DeleteButton());
		}
		
		public void setParams() {
//			list.removeAll();
			localCalList = vrParameters.getCalibrationDatas();
			tableData.fireTableDataChanged();
			if (localCalList != null && vrParameters.getCurrentCalibrationIndex() < tableData.getRowCount()) {
				list.setRowSelectionInterval(vrParameters.getCurrentCalibrationIndex(), 
						vrParameters.getCurrentCalibrationIndex());
			}
		}
		
		public boolean getParams() {
			vrParameters.setCalibrationDatas(localCalList);
			int row = list.getSelectedRow();
			if (localCalList == null) {
				// have to allow this at start so that height data can be entered and
				// the first calibration made.
				return true;
			}
			if (row < 0 || (localCalList != null && row >= localCalList.size())) {
				return false;
			}
			vrParameters.setCurrentCalibrationIndex(row);
			return true;
		}
		
		class AddButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				VRCalibrationData newCalibration = VRCalibrationDialog.showDialog(parentFrams, vrControl, new VRCalibrationData());
				if (localCalList == null) {
					localCalList = new ArrayList<VRCalibrationData>();
				}
				if (newCalibration != null) {
					localCalList.add(newCalibration);
					tableData.fireTableDataChanged();
					int lastRow = localCalList.size()-1;
					list.setRowSelectionInterval(lastRow, lastRow);
				}
			}
			
		}
		
		class EditButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				int row = list.getSelectedRow();
				if (row >= 0&& row < localCalList.size()) {
					VRCalibrationData vrcd = localCalList.get(row);
					VRCalibrationData newCalibration = VRCalibrationDialog.showDialog(parentFrams, vrControl, vrcd);
					if (newCalibration != null) {
						vrcd.update(newCalibration);
						tableData.fireTableDataChanged();
						list.setRowSelectionInterval(row, row);
					}
				}
			}
			
		}
		
		class DeleteButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				int row = list.getSelectedRow();
				if (localCalList == null) return;
				if (row <= 0 || row >= localCalList.size()) {
					return;
				}
				localCalList.remove(row);

				tableData.fireTableDataChanged();
			}
			
		}
			
		class CalTableData extends AbstractTableModel {

			public int getColumnCount() {
				return 2;
			}

			public int getRowCount() {
				if (localCalList == null) {
					return 0;
				}
				return localCalList.size();
			}
			private String[] columnNames = {"Name", "Calibration"};
			@Override
			public String getColumnName(int column) {
				return columnNames[column];
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (localCalList == null) {
					return null;
				}
				VRCalibrationData cd = localCalList.get(rowIndex);
				if (cd == null) {
					return "Unknown";
				}
				switch (columnIndex) {
				case 0:
					return cd.name;
				case 1:
					return String.format("%.4f\u00B0/pix", cd.degreesPerUnit);
				}
				return null;
			}
			
		}
		
	}
	
	class CalcPanel extends JPanel  {

		JComboBox<String> methodList;
		RangeDialogPanel rangeDialogPanel = null;
		JPanel panel;
		
		public CalcPanel () {
			
			setLayout(new BorderLayout());

			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			JPanel q = new JPanel();
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;

			q.setLayout(new BoxLayout(q, BoxLayout.Y_AXIS));
			q.setBorder(new TitledBorder("Calculation method"));
			q.add(methodList = new JComboBox<String>());
			methodList.addActionListener(new MethodAction());
//			addComponent(panel, q, c);
			panel.add(q);
			for (int i = 0; i < vrControl.getRangeMethods().getNames().size(); i++) {
				methodList.addItem(vrControl.getRangeMethods().getNames().get(i));
			}
			
			this.add(BorderLayout.NORTH, panel);
			
		}
		void setParams() {
			methodList.setSelectedIndex(vrParameters.rangeMethod);
			setMethod();
		}
		boolean getParams() {
			vrParameters.rangeMethod = methodList.getSelectedIndex();
			if (rangeDialogPanel != null) {
				if (rangeDialogPanel.getParams() == false) {
					return false;
				}
			}
			return true;
		}
		
		class MethodAction implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				setMethod();
			}
		}
		
		private void setMethod() {
			int index = methodList.getSelectedIndex();
			if (index < 0) return;
			VRHorzCalcMethod selMethod = vrControl.getRangeMethods().getMethod(index);
			RangeDialogPanel newRangePanel = selMethod.dialogPanel();
			if (newRangePanel == rangeDialogPanel) {
				return;
			}
			if (rangeDialogPanel != null) {
				panel.remove(rangeDialogPanel.getPanel());
			}
			if (newRangePanel != null) {
				panel.add(newRangePanel.getPanel());
			}
			rangeDialogPanel = newRangePanel;
			if (rangeDialogPanel != null) {
				rangeDialogPanel.setParams();
			}
			pack();
		}
	}

	class ImagePanel extends JPanel {

		JComboBox scaleList;
		
		JCheckBox drawHorizon;
				
		public ImagePanel() {
			super();
//			setBorder(new TitledBorder("Image Scaling"));
			this.setLayout(new BorderLayout());

			JPanel op = new JPanel();
			op.setLayout(new GridBagLayout());
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.setBorder(new TitledBorder("Image scaling"));
			p.add(BorderLayout.NORTH, scaleList = new JComboBox());
			
			addComponent(op, p, c);
			
			for (int i = 0; i < VRParameters.scaleNames.length; i++) {
				 scaleList.addItem(VRParameters.scaleNames[i]);
			}
			
			c.gridy++;
			JPanel q = new JPanel();
			q.setLayout(new BoxLayout(q, BoxLayout.Y_AXIS));
			q.setBorder(new TitledBorder("Options"));
			q.add(drawHorizon = new JCheckBox("Draw horizon line during selection"));			
			drawHorizon.setToolTipText("Draws the horizon line while you are moving the mouse to the second horizon point");
			addComponent(op, q, c);
			
			this.add(BorderLayout.NORTH, op);
		}
		
		void setParams() {
			scaleList.setSelectedIndex(vrParameters.imageScaling);
			drawHorizon.setSelected(vrParameters.drawTempHorizon);
		}
		boolean getParams() {
			vrParameters.imageScaling = scaleList.getSelectedIndex();
			vrParameters.drawTempHorizon = drawHorizon.isSelected();
			return true;
		}
	}
	
	/**
	 * Panel which allows users to add different heights. 
	 * @author Jamie Macaulay
	 *
	 */
	class HeightPanel extends JPanel {

		JTable list;
		ArrayList<VRHeightData> localHeightList;
		JButton deleteButton, addButton, editbutton;
		AbstractTableModel tableData;
		
		HeightPanel() {

			super();
//			setBorder(new TitledBorder("Calibration"));
			setLayout(new BorderLayout());
			list = new JTable(tableData = new HeightTableData());
			list.setRowSelectionAllowed(true);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scrollPane = new JScrollPane(list);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setPreferredSize(new Dimension(1, 130));
			add(BorderLayout.CENTER, scrollPane);
			
			add(BorderLayout.NORTH, new JLabel("Manage camera heights"));
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(addButton = new JButton("Add"));
			addButton.addActionListener(new AddButton());
			buttonPanel.add(editbutton = new JButton("Edit"));
			editbutton.addActionListener(new EditButton());
			buttonPanel.add(deleteButton = new JButton("Delete"));
			deleteButton.addActionListener(new DeleteButton());
			this.add(BorderLayout.SOUTH, buttonPanel);
		}
		
		protected void setParams() {
			localHeightList = vrParameters.getHeightDatas();
			tableData.fireTableDataChanged();
			if (localHeightList != null && vrParameters.getCurrentHeightIndex() < tableData.getRowCount()) {
				list.setRowSelectionInterval(vrParameters.getCurrentHeightIndex(), vrParameters.getCurrentHeightIndex());
			}
		}
		
		protected boolean getParams() {
			vrParameters.setHeightDatas(localHeightList);
			int row = list.getSelectedRow();
			if (row < 0 || (localHeightList != null && row >= localHeightList.size())) {
				return false;
			}
			vrParameters.setCurrentHeightIndex(row);
			return true;
		}
		
		class HeightTableData extends AbstractTableModel {

			private static final long serialVersionUID = 1L;
			
			private String[] columnNames = {"Name", "Height (m)"};
			@Override
			public String getColumnName(int column) {
				return columnNames[column];
			}

			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				if (localHeightList == null) {
					return 0;
				}
				return localHeightList.size();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (localHeightList == null) {
					return null;
				}
				if (rowIndex < 0 || rowIndex >= localHeightList.size()) {
					return null;
				}
				VRHeightData vrh = localHeightList.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return vrh.name;
				case 1:
					return String.format("%.1f m", vrh.height);
				}
				return null;
			}
			
		}
		
		class DeleteButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				int row = list.getSelectedRow();
				if (row < 0 || row >= localHeightList.size()) {
					return;
				}
				localHeightList.remove(row);
				tableData.fireTableDataChanged();
			}
		}
		
		class AddButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				VRHeightData newData = HeightDialog.showDialog(null, null);
				if (newData != null) {
					if (localHeightList == null) {
						localHeightList = new ArrayList<VRHeightData>();
					}
					localHeightList.add(newData);
					int row = localHeightList.size()-1;
					tableData.fireTableDataChanged();
					list.setRowSelectionInterval(row, row);
				}
			}
		}
		
		class EditButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				int row = list.getSelectedRow();
				if (row < 0 || row >= localHeightList.size()) {
					return;
				}
				VRHeightData heightData = localHeightList.get(row);
				VRHeightData newData = HeightDialog.showDialog(null, heightData);
				if (newData != null) {
					heightData.update(newData);
					tableData.fireTableDataChanged();
				}
				
			}
		}
	}
	
	class AnglesPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		
		JCheckBox readAngles;
		SourcePanel angleSource;
		public AnglesPanel() {
			super();
			setBorder(new TitledBorder("Angle Measurement"));
			setLayout(new BorderLayout());
			add(BorderLayout.NORTH, readAngles = new JCheckBox("Read angles"));
			angleSource = new SourcePanel(THIS, AngleDataUnit.class, false, false);
			add(BorderLayout.CENTER, angleSource.getPanel());
			readAngles.addActionListener(new ReadAngles());
		}
		
		void setParams() {
			readAngles.setSelected(vrParameters.measureAngles);
			angleSource.setSource(vrParameters.angleDataBlock);
			enableControls();
		}
		
		boolean getParams() {
			vrParameters.measureAngles = readAngles.isSelected();
			PamDataBlock dataBlock = angleSource.getSource();
			if (dataBlock != null) {
				vrParameters.angleDataBlock = dataBlock.getDataName();
			}
			return true;
		}
		
		void enableControls() {
			readAngles.setEnabled(angleSource.getSourceCount() > 0);
			if (readAngles.isEnabled() == false) {
				readAngles.setSelected(false);
			}
			angleSource.setEnabled(readAngles.isSelected());
		}
		
		class ReadAngles implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		}
		
	}
	
	class ShorePanel extends JPanel {
		

		private static final long serialVersionUID = 1L;
		
		JButton browseButton;
		JTextField gebcoFile;
		JCheckBox ignoreClosest, drawShore, drawShorePoints;
		
		public ShorePanel() {
			setBorder(new TitledBorder("Shore"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 2;
			addComponent(this, new JLabel("Ascii file for shoreline data"), c);
			c.gridy++;
			addComponent(this, gebcoFile = new JTextField(20), c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
//			addComponent(this, new JLabel("                            "), c);
			c.gridx++;
			c.fill = GridBagConstraints.NONE;
			addComponent(this, browseButton = new JButton("Browse..."), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 2;
			addComponent(this, ignoreClosest = new JCheckBox("Ignore closest shore segments"), c);
			c.gridy++;
			addComponent(this, new JLabel("(Check this if working from land station)"), c);
			c.gridy++;
			addComponent(this, drawShore = new JCheckBox("Draw Shore on image"), c);
			c.gridy++;
			addComponent(this, drawShorePoints = new JCheckBox("Highlight shore vector points"), c);
			
			gebcoFile.setEnabled(false);
			browseButton.addActionListener(new BrowseGebcoFile());
			drawShore.addActionListener(new DrawShoreListener());
		}
		
		class BrowseGebcoFile implements ActionListener {

			public void actionPerformed(ActionEvent e) {

				File newFile = vrControl.getMapFileManager().selectMapFile(getMapFile());
				if (newFile != null) {
					gebcoFile.setText(newFile.getAbsolutePath());
					vrParameters.shoreFile = newFile;
					vrControl.getMapFileManager().readFileData(newFile, false);
				}
				enableControls();
			}

		}
		
		public File getMapFile() {
			if (gebcoFile.getText() == null || gebcoFile.getText().length() == 0) {
				return null;	
			}
			else {
				return new File(gebcoFile.getText());
			}
		}

		void setParams() {
			ignoreClosest.setSelected(vrParameters.ignoreClosest);
			drawShore.setSelected(vrParameters.getShowShore());
			drawShorePoints.setSelected(vrParameters.showShorePoints);
			if (vrParameters.shoreFile != null) {
				gebcoFile.setText(vrParameters.shoreFile.getAbsolutePath());
			}
			else {
				gebcoFile.setText("");
			}
			enableControls();
		}
		
		boolean getParams() {
			vrParameters.ignoreClosest = ignoreClosest.isSelected();
			vrParameters.showShore = drawShore.isSelected();
			vrParameters.showShorePoints = drawShorePoints.isSelected();
			return true;
		}
		
		void enableControls() {
			boolean en = gebcoFile.getText() != null && gebcoFile.getText().length() > 0;
			ignoreClosest.setEnabled(en);
			drawShore.setEnabled(en);
			if (en == false) {
				ignoreClosest.setSelected(false);
				drawShore.setSelected(false);
			}
			drawShorePoints.setEnabled(drawShore.isSelected());
			if (drawShorePoints.isEnabled() == false) {
				drawShorePoints.setSelected(false);
			}
		}
		
		class DrawShoreListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		}
		
	}
	
	class TidePanel extends JPanel{

	
		private static final long serialVersionUID = 1L;
		
		//JTextField polPred;
		private JButton browseButton;
		
		public TidePanel() {
			
			setBorder(new TitledBorder("Tide"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			//c.gridwidth = 2;
			//addComponent(this, new JLabel("POLPRED tide data"), c);
			//c.gridy++;
			//addComponent(this, polPred = new JTextField(20), c);
			//c.gridy++;
			//c.gridx = 0;
			//c.gridwidth = 1;
//			addComponent(this, new JLabel("                            "), c);
			//c.gridx++;
			c.fill = GridBagConstraints.NONE;
			addComponent(this, browseButton = new JButton("Import..."), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 2;

			
			//polPred.setEnabled(false);
			browseButton.addActionListener(new BrowsePolPredFile());
		}
		
		void setParams() {
//			if (vrParameters.currTideFile!=null){
//				if (vrParameters.currTideFile.exists()){
//					polPred.setText(vrParameters.currTideFile.getPath());
//				}
//			}
		}
		
	
		class BrowsePolPredFile implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				
				vrControl.getTideImport().showImportDialog();
				
//				String file=vrControl.getTideManager().findFile(vrParameters.currTideFile);
//				
//				if (file!=null){
//					vrControl.getTideManager().importTideTxtFile(file);
//					vrParameters.currTideFile = new File(file);
//					setParams();
//				}
//		
//				else return; 
				
			}

		}
		
	}
}
