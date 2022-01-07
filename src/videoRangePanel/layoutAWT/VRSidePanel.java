package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

import PamUtils.PamFileChooser;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.pamImage.ImageFileFilter;
import PamView.PamColors;
import PamView.PamSidePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;

@SuppressWarnings("rawtypes")
public class VRSidePanel implements PamSidePanel {

	private VRControl vrControl;

	private VRControlPanel vrControlPanel;
	
	private PamLabel mouseLabel, imageNameLabel, imageTime;
	
	private JComboBox heights;
	
	//components for time and date label and spinners
	private JPanel photoTimePanel;
	boolean cursorOnTimePanel=false; 
	boolean spinnerDisplayed=false;
	private SpinnerDateModel photoTimeModel;
	private JSpinner photoTimeSpinner;
	private PamPanel photoTimeSpinnerPnl;

	
	public static ImageIcon settings = new ImageIcon(ClassLoader
			.getSystemResource("Resources/SettingsButtonSmall2.png"));

	public VRSidePanel(VRControl vrControl) {
		super();
		this.vrControl = vrControl;
		vrControlPanel = new VRControlPanel();
	}

	public JComponent getPanel() {
		return vrControlPanel;
	}

	public void rename(String newName) {
		vrControlPanel.panelBorder.setTitle(newName);
	}
	
	
	void setImageName() {
		if (vrControl.getCurrentImage() == null) {
			imageNameLabel.setText("No Image");
		}
		else if (vrControl.getImageName()!=null){
			imageNameLabel.setText(vrControl.getImageName());
		}
		else {
			imageNameLabel.setText("PAMImage ");
		}
	}
	
	void setImageTime() {
		if (vrControl.getCurrentImage() == null) {
			imageNameLabel.setText("No Image");
		}
		else if (vrControl.getImageTimeString() == null) {
			imageTime.setText("No Image time");
		}
		else {
			imageTime.setText(vrControl.getImageTimeString());
		}
	}

	@SuppressWarnings("unchecked")
	void update(int updateType) {
		switch (updateType){
			case VRControl.SETTINGS_CHANGE:
				VRHeightData currentheight = vrControl.getVRParams().getCurrentheightData();
				heights.removeAllItems();
				ArrayList<VRHeightData> heightDatas = vrControl.getVRParams().getHeightDatas();
				for (int i = 0; i < heightDatas.size(); i++) {
					heights.addItem(heightDatas.get(i));
				}
				if (currentheight != null) {
					heights.setSelectedItem(currentheight);
				}
				
			break;
			case VRControl.IMAGE_CHANGE:
				setImageName();
				setImageTime();
				vrControlPanel.enableArrowButtons();
				getPanel().revalidate();
			break;
			case VRControl.METHOD_CHANGED:
				changeMethod();
				break;
		}
	}
	
	
	/**
	 * Opens a file browser and loads the selected image/folder. Also, looks at the folder the image is in and determines the number of image files. 
	 */
	public void fileButton() {
		
		ImageFileFilter filefilter=new ImageFileFilter();
		
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(filefilter);
		fileChooser.setCurrentDirectory(vrControl.getVRParams().imageDirectory);
		fileChooser.setDialogTitle("Select image...");
		//fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileHidingEnabled(true);
		fileChooser.setApproveButtonText("Select");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		javax.swing.filechooser.FileFilter[] filters = fileChooser
		.getChoosableFileFilters();
		for (int i = 0; i < filters.length; i++) {
			fileChooser.removeChoosableFileFilter(filters[i]);
		}
		fileChooser.setFileFilter(filefilter);

		int state = fileChooser.showOpenDialog(this.getPanel());
		
		if (state == JFileChooser.APPROVE_OPTION) {
			vrControl.getVRParams().currentImageFile=null;
			
			//check if this is a folder in which case take the first image in that folder. 
			if (fileChooser.getSelectedFile().isDirectory()){
				File[] files=fileChooser.getSelectedFile().listFiles();
				for (int i=0; i<files.length; i++){
					if (filefilter.accept(files[i])){
						vrControl.getVRParams().currentImageFile=files[i];
						vrControl.getVRParams().imageDirectory = fileChooser.getSelectedFile();
						break;
					}
				}
				
			}
			else {
				vrControl.getVRParams().currentImageFile = fileChooser.getSelectedFile();
				vrControl.getVRParams().imageDirectory = fileChooser.getCurrentDirectory();
			}
			
			if (vrControl.getVRParams().currentImageFile==null) {
				PamDialog.showWarning(vrControl.getPamView().getGuiFrame(), "No image found", "The folder selected did not contain any compatible images");
				return;
			}
			vrControl.loadFile(vrControl.getVRParams().currentImageFile);
		}
		
		vrControl.update(VRControl.IMAGE_CHANGE);
	}
	
	
	public void enablePanel(){
		//TODO-DISABLE PANELS
	}
	
	public void newMousePoint(Point mousePoint) {
		if (mousePoint == null) {
			mouseLabel.setText(" ");
		}
		else {
			mouseLabel.setText(String.format("Cursor: (%d,%d)", mousePoint.x, mousePoint.y));
		}
	}
	
	public void changeMethod(){
		vrControlPanel.changeMethod();
	}
	
	
	@SuppressWarnings("serial")
	class VRControlPanel extends PamBorderPanel {
		JButton pasteButton, fileButton, settingsButton;
		
		JComboBox scaleStyle;
		JSlider brightness, contrast;
		JSpinner spinBrightness, spinContrast;
		TitledBorder panelBorder;
		JComboBox<String> measurementType;
		//arrow buttons
		private BasicArrowButton imageForward;
		private BasicArrowButton imageBackward;
		private PamPanel currentPanel;
		private PamPanel methodPanel;


		VRControlPanel() {
			
			this.setLayout(new BorderLayout());
			
			PamLabel lab;
			PamPanel mp = new PamBorderPanel();
			this.setBorder(panelBorder = new TitledBorder(vrControl.getUnitName()));
			mp.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = c.gridy = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			
			addComponent(mp, lab = new PamLabel("Image"), c);
			lab.setFont(PamColors.getInstance().getBoldFont());
			c.gridx=0;
			c.gridy++;
			addComponent(mp, fileButton = new JButton("File ..."), c);
			c.gridx++;
			addComponent(mp, pasteButton = new JButton("Paste"), c);
			c.gridx=0;
			c.gridy++;
			c.ipady=15;
			addComponent(mp, imageBackward = new BasicArrowButton(BasicArrowButton.WEST), c);
			imageBackward.addActionListener(new ArrowListener(-1));
			imageBackward.setPreferredSize(new Dimension(1,20));
			c.gridx++;
			addComponent(mp, imageForward = new BasicArrowButton(BasicArrowButton.EAST), c);
			imageForward.addActionListener(new ArrowListener(1));
			imageForward.setPreferredSize(new Dimension(1,20));
			c.gridx=0;
			c.ipady=0;
			c.gridy++;
//			c.fill = GridBagConstraints.NONE;
//			c.anchor = GridBagConstraints.EAST;
			
			c.insets = new Insets(5,0,0,0);  //top padding
			c.gridy++;
			c.gridwidth = 2;
			PamPanel photoInfo=new PamPanel(new GridLayout(2,1));
			photoInfo.add(imageNameLabel=new PamLabel("Image: ")); 
			photoTimePanel=new JPanel(new GridLayout(1,1)); 
			photoTimePanel.add(imageTime=new PamLabel("Time: ")); 
			photoTimePanel.addMouseListener(new PhotoTimeListener());
			photoInfo.add(photoTimePanel);
			addComponent(mp, photoInfo, c);
			
			//create the spinner but don't add to panel
			photoTimeSpinnerPnl= createDateSpinner();
			
			//create the analysis JComboBox box.
			c.insets = new Insets(20,0,0,0);  //top padding
			c.gridy++;
			c.gridx=0;
			c.gridwidth = 2;
			addComponent(mp, lab = new PamLabel("Analysis Method"), c);
			lab.setFont(PamColors.getInstance().getBoldFont());
			c.gridy++;
			c.gridx=0;
			c.insets = new Insets(0,0,0,0);  // reset top padding
			addComponent(mp, measurementType = new JComboBox<String>(), c);
			for (int i=0; i<vrControl.getMethods().size(); i++){
				measurementType.addItem(vrControl.getMethods().get(i).getName());
			}
			measurementType.setSelectedIndex(vrControl.getVRParams().methodIndex);//set to the correct index before action listner. 
			measurementType.addActionListener(new MeasurementType());
			
			c.gridy++;
			c.gridx=0;
			c.gridwidth = 2;
			c.insets = new Insets(5,0,0,0);  //top padding
			addComponent(mp, lab = new PamLabel("Height"), c);
			c.gridy++;
			c.insets = new Insets(0,0,0,0);  //top padding
			PamDialog.addComponent(mp, heights = new JComboBox<String>(), c);
			heights.addActionListener(new SelectHeight());
			
			//add the panel for the particular type of analysis
			c.gridx=0;
			c.gridy++;
			c.gridwidth = 2;
			c.insets = new Insets(10,0,0,0); 
			
			methodPanel=new PamPanel(new BorderLayout());
			currentPanel= getCurrentSidePanel();	
			if (currentPanel!=null) methodPanel.add(BorderLayout.CENTER, currentPanel);
			addComponent(mp,methodPanel , c);
		
			//add the settings button
			c.gridx=0;
			c.gridy++;
			c.insets = new Insets(15,0,0,0);  //top padding
			c.gridwidth = 2;
			addComponent(mp, lab = new PamLabel("Settings"), c);
			lab.setFont(PamColors.getInstance().getBoldFont());
			c.gridy++;
			c.insets = new Insets(0,0,0,0);  //top padding
			addComponent(mp, settingsButton = new JButton("  Settings...",settings), c);
			c.gridy++;
			addComponent(mp, mouseLabel = new PamLabel(" "), c);
			//
				
			//add action listeners. 
			settingsButton.addActionListener(new SettingsButton());
			pasteButton.addActionListener(new PasteButton());
			fileButton.addActionListener(new FileButton());		
			
//			enableControls();
			enableArrowButtons();

			this.add(BorderLayout.NORTH, mp);
			this.addMouseListener(new SidePanelMouseListener());
//			this.setPreferredSize(new Dimension(150,1));
			
		}	
		
		/**
		 * Get the current side panel
		 * @return the current side panel
		 */
		private PamPanel getCurrentSidePanel(){
			if (vrControl.getCurrentMethod().getOverlayAWT()!=null)
			return vrControl.getCurrentMethod().getOverlayAWT().getSidePanel();	
			else return null;
		}
		
		private PamPanel createDateSpinner(){
			
	        Calendar cal = Calendar.getInstance();
	        Date date = cal.getTime();
	        photoTimeModel = new SpinnerDateModel();
	        photoTimeModel.setValue(date);
	        photoTimeSpinner = new JSpinner(photoTimeModel);
	        photoTimeSpinner.addChangeListener(new PhotoDateListener());
	        
	        Format format = ((JSpinner.DateEditor) photoTimeSpinner.getEditor()).getFormat();
	        ((SimpleDateFormat) format).applyPattern("yyyy-MM-dd HH:mm:ss");
	        //We want the time zone to be in UTC.
	        ((SimpleDateFormat) format).setTimeZone(TimeZone.getTimeZone("GMT"));
	        
	        PamPanel photoTimePanel=new PamPanel(new GridLayout(1,1));
	        photoTimePanel.add(photoTimeSpinner);
	        return photoTimePanel;
	        
		}
		
		
		
		private class PhotoDateListener implements ChangeListener{

			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		}
		
		/**
		 * Change the side panel to the current method. 
		 */
		public void changeMethod(){
			methodPanel.remove(currentPanel);
			currentPanel=vrControl.getCurrentMethod().getOverlayAWT().getSidePanel();
			methodPanel.add(BorderLayout.CENTER, currentPanel);
//			currentPanel.validate();
//			vrControlPanel.validate();
			this.validate();
			this.repaint();
		}
		
		public void enableArrowButtons(){
			if (vrControl.getVRParams().currentImageFile==null) {
				imageForward.setEnabled(false);
				imageBackward.setEnabled(false);
				return;
			}
			imageForward.setEnabled(vrControl.findNextFile(vrControl.getVRParams().currentImageFile, new ImageFileFilter(), true)!=null);
			imageBackward.setEnabled(vrControl.findNextFile(vrControl.getVRParams().currentImageFile, new ImageFileFilter(), false)!=null);
		}
		 
		
	
		
		private class ArrowListener implements ActionListener{
			//1==forward, -1 is backward. 
			private int imageMove=1;
			
			public ArrowListener(int imageMove){
				this.imageMove=imageMove;
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				boolean forward=true; 
				if (imageMove==-1) forward=false;   
				//check file is not null;
				if (vrControl.getVRParams().currentImageFile==null) return;
				//check the file exists.
				if(!vrControl.getVRParams().currentImageFile.exists()) return; 

				File newFile=vrControl.findNextFile(vrControl.getVRParams().currentImageFile, new ImageFileFilter(), forward);
				
				if (newFile!=null){
			    vrControl.loadFile(newFile);	
				vrControl.update(VRControl.IMAGE_CHANGE);
				}
				//now disable or enable arrow buttons if needs be
				enableArrowButtons();
			}
		}

		private class FileButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				fileButton();
			}
		}

		private class PasteButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				vrControl.pasteButton();
				enableArrowButtons();
			}
			
		}
		
		/**
		 * Class to close the JSpinner panel if mouse is clicked outside. 
		 * @author Jamie Macaulay
		 *
		 */
		private class SidePanelMouseListener extends  MouseAdapter{
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//System.out.println("SidePanelMouseListener: MouseClicked: ");
				if (!cursorOnTimePanel && spinnerDisplayed){
					spinnerDisplayed=false;
					showDateSpinner(false);
				}
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				if (!cursorOnTimePanel && spinnerDisplayed){
					spinnerDisplayed=false;
					showDateSpinner(false);
				}
			}

			
		}
		
		
		/**
		 * Class to check whether the mouse is on the photo time panel. 
		 * @author Jamie Macaulay. 
		 *
		 */
		private class PhotoTimeListener extends MouseAdapter{
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount()>1 && !spinnerDisplayed && cursorOnTimePanel){
					spinnerDisplayed=true;
					showDateSpinner(true);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				if (!spinnerDisplayed && cursorOnTimePanel){
					spinnerDisplayed=true;
					showDateSpinner(true);
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				cursorOnTimePanel=true;
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				cursorOnTimePanel=false;
				
			}
		}
		
		/**
		 * Show the date/time spinner in the side or show a label showing date and time. 
		 * @param show-true to show the spinner or false to show the label. 
		 */
		private void showDateSpinner(boolean show){
			if (show){
				photoTimePanel.remove(imageTime);
				photoTimePanel.add(photoTimeSpinnerPnl);
				Date date=vrControl.getImageDate();
				if (date==null) photoTimeModel.setValue(Calendar.getInstance().getTime());
				else photoTimeModel.setValue(vrControl.getImageDate());
			}
			else{
				photoTimePanel.remove(photoTimeSpinnerPnl);
				photoTimePanel.add(imageTime);
				if (photoTimeModel.getValue()!=null && vrControl.getCurrentImage()!=null){
					vrControl.getCurrentImage().setTimeMilliseconds(((Date) photoTimeModel.getValue()).getTime());
					vrControl.getCurrentImage().setDate(((Date) photoTimeModel.getValue()));
				}
				setImageTime();
			}
			this.validate();
			photoTimePanel.validate();
			photoTimePanel.repaint();
			//update the rest of the panels and sections of the module as the time may have changed. 
			vrControl.update(VRControl.SETTINGS_CHANGE);
		}

		/**
		 * Selects which method to use. 
		 * @author Jamie Macaulay
		 *
		 */
		private class MeasurementType implements ActionListener {
			private int lastSelection = -1;
			public void actionPerformed(ActionEvent e) {
				if (lastSelection != measurementType.getSelectedIndex()) {
					lastSelection = measurementType.getSelectedIndex();
					vrControl.setVRMethod(lastSelection);
				}
			}
		}
		
		/**
		 * Selects which height to use.
		 * @author Jamie Macaulay
		 *
		 */
		private class SelectHeight implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				vrControl.selectHeight(heights.getSelectedIndex());
//				vrControl.vrParameters.setCurrentHeightIndex(heights.getSelectedIndex());
			}
		}
		
		/**
		 * Open the settings dialog. 
		 * @author Jamie Macaulay
		 *
		 */
		private class SettingsButton implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				vrControl.settingsButtonAWT(null,-1);
			}
			
		}

		
	}
	


}
