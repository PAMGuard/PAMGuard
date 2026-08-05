package loggerForms.cameragrabber.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.GrabberParams;

public class GrabberDialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;
	
	private static GrabberDialog singleInstance;
	private GrabberParams grabberParams;
	
	private JPanel mainPanel;
	private JPanel camPanel; // panel for 1 - n panes, one for each camera. 
	private JTextField nCameras;
	private JCheckBox timeStamp;
	
	private SelectFolder selectFolder;
	
	private JCheckBox autoGrab, autoGrabRandom;
	private JTextField autoInterval;
	
	private JTextField bufferSeconds, sequenceSeconds; // pre and post times for getting sequences of images. 
	
	private CameraPanel[] cameraPanels;

	private CameraGrabber cameraGrabber;

	/**
	 * @return the cameraGrabber
	 */
	public CameraGrabber getCameraGrabber() {
		return cameraGrabber;
	}


	private GrabberDialog(CameraGrabber cameraGrabber, Window parentFrame) {
		super(parentFrame, "Camera grabber options", false);
		this.cameraGrabber = cameraGrabber;
		mainPanel = new JPanel(new BorderLayout());
		camPanel = new JPanel();
		camPanel.setLayout(new BoxLayout(camPanel, BoxLayout.Y_AXIS));
		selectFolder = new SelectFolder(50);
		selectFolder.setShowSubFolderOption(true);
		selectFolder.setSubFolderButtonName("Store in sub folders by date");
		selectFolder.setSubFolderButtonToolTip("Create a new sub folder for stored images each day");
		
		JPanel nPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 6;
		nPanel.add(selectFolder.getFolderPanel(), c);
		c.gridwidth = 1;
		c.gridy++;
		nPanel.add(new JLabel("Number of cameras", JLabel.RIGHT), c);
		c.gridx++;
		nPanel.add(nCameras = new JTextField(2), c);
		c.gridx++;
		nPanel.add(new JLabel(" (hit enter)"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		nPanel.add(timeStamp = new JCheckBox("Timestamp images"), c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx = 0;
		nPanel.add(autoGrab = new JCheckBox("Auto every"), c);
		c.gridx++;
		nPanel.add(autoInterval = new JTextField(3), c);
		c.gridx++;
		nPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);
		c.gridx++;
		c.gridwidth = 2;
		nPanel.add(autoGrabRandom = new JCheckBox("Randomise"),c);
		// sequence stuff
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		nPanel.add(new JLabel("Sequences: pre ",JLabel.RIGHT), c);
		c.gridx++;
		nPanel.add(bufferSeconds = new JTextField(2), c);
		c.gridx++;
		nPanel.add(new JLabel(" and post ",JLabel.CENTER), c);
		c.gridx++;
		nPanel.add(sequenceSeconds = new JTextField(2), c);
		c.gridx++;
		nPanel.add(new JLabel(" sampling (seconds) ",JLabel.LEFT), c);
		
		
		

		nPanel.setBorder(new TitledBorder("General"));
		
		mainPanel.add(BorderLayout.NORTH, nPanel);
		mainPanel.add(camPanel);
		
		nCameras.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newNCamera();
			}
		});
		
		setDialogComponent(mainPanel);
	}
	

	public static GrabberParams showDialog(Window parentFrame, CameraGrabber cameraGrabber, GrabberParams gabberParams) {
//		if (singleInstance == null) {
			singleInstance = new GrabberDialog(cameraGrabber, parentFrame);
//		}
		singleInstance.setParams(gabberParams);
		singleInstance.setVisible(true);
		return singleInstance.grabberParams;
	}

	private void newNCamera() {
		getParams();
		setParams(grabberParams);
	}
	
	private void setParams(GrabberParams grabberParams) {
		this.grabberParams = grabberParams;
		selectFolder.setFolderName(grabberParams.outputFolder);
		selectFolder.setIncludeSubFolders(grabberParams.foldersByDate);
		timeStamp.setSelected(grabberParams.timeStampImages);
		nCameras.setText(String.format("%d", grabberParams.nCameras));
		autoGrab.setSelected(grabberParams.autoGrab);
		autoInterval.setText(String.format("%d", grabberParams.autoGrabSeconds));
		autoGrabRandom.setSelected(grabberParams.autoGrabRandomise);
		bufferSeconds.setText(String.format("%d", grabberParams.bufferSeconds));
		sequenceSeconds.setText(String.format("%d", grabberParams.sequenceSeconds));
		
		createComponents();
		if (cameraPanels == null) {
			return;
		}
		for (int i = 0; i < cameraPanels.length; i++) {
			cameraPanels[i].setParams(grabberParams.getCameraParams(i));
		}
	}

	private void createComponents() {
		int nCam = 0;
		try {
			nCam = Integer.valueOf(nCameras.getText());
		}
		catch (NumberFormatException e) {
			showWarning("Invalid number of cameras : " + nCameras.getText());
		}
		grabberParams.nCameras = nCam;
		camPanel.removeAll();
		cameraPanels = new CameraPanel[nCam];
		for (int i = 0; i < nCam; i++) {
			cameraPanels[i] = new CameraPanel(this, cameraGrabber, i);
			camPanel.add(new PamAlignmentPanel(cameraPanels[i].getDialogComponent(), BorderLayout.WEST, true));
		}
		pack();
	}

	@Override
	public boolean getParams() {
		
		grabberParams.outputFolder = selectFolder.getFolderName(true);
		grabberParams.foldersByDate = selectFolder.isIncludeSubFolders();
		grabberParams.timeStampImages = timeStamp.isSelected();
		grabberParams.autoGrab = autoGrab.isSelected();
		try {
			grabberParams.autoGrabSeconds = Integer.valueOf(autoInterval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid grab interval. Must be integer");
		}
		grabberParams.autoGrabRandomise = autoGrabRandom.isSelected();
		grabberParams.autoGrab = autoGrab.isSelected();
		try {
			grabberParams.bufferSeconds = Integer.valueOf(bufferSeconds.getText());
			grabberParams.sequenceSeconds = Integer.valueOf(sequenceSeconds.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Sequence pre and post sampling times must be integer");
		}
		
		if (grabberParams.outputFolder == null) {
			return showWarning("You must select a storage folder for camera images");
		}
		
		int nCam = 0;
		try {
			nCam = Integer.valueOf(nCameras.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid number of cameras : " + nCameras.getText());
		}
		grabberParams.nCameras = nCam;
		if (cameraPanels == null) {
			return false;
		}

		for (int i = 0; i < Math.min(nCam, cameraPanels.length); i++) {
			CameraParams cameraParams = cameraPanels[i].getParams();
			if (cameraParams == null) {
				return false;
			}
			grabberParams.setCameraParams(i, cameraParams);
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		grabberParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
