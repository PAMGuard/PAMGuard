package loggerForms.cameragrabber.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.github.sarxos.webcam.Webcam;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import loggerForms.cameragrabber.CameraGrabber;
import loggerForms.cameragrabber.CameraParams;
import loggerForms.cameragrabber.source.CameraSourcePanel;
import loggerForms.cameragrabber.source.CameraSourceType;
import loggerForms.cameragrabber.source.WebcamDialogPanel;

/**
 * Dialog panel for camera setup. this in turn will have to have a 
 * dynamically changing panel depending on what type of source is selected. 
 */
public class CameraPanel {

	private int camIndex;
	
	private JPanel mainPanel;
	
	private JTextField initials;
	
	private CameraSourcePanel cameraSourcePanel;

	private JPanel centralPanel;
	
	private JComboBox<CameraSourceType> sourceTypes;

	private GrabberDialog grabberDialog;

	private CameraParams cameraParams;

	public CameraPanel(GrabberDialog grabberDialog, CameraGrabber cameraGrabber, int camIndex) {
		super();
		this.grabberDialog = grabberDialog;
		this.camIndex = camIndex;
		mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new GridBagLayout());
		centralPanel = new JPanel();
		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, centralPanel);
		mainPanel.setBorder(new TitledBorder("Camera " + camIndex));
		GridBagConstraints c = new PamGridBagContraints();
		
		sourceTypes = new JComboBox<>();
		topPanel.add(new JLabel("Camera Type: ", JLabel.RIGHT),c);
		c.gridx++;
		c.gridwidth = 2;
		topPanel.add(sourceTypes);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		topPanel.add(new JLabel("Image initials ", JLabel.RIGHT),c);
		c.gridx++;
		topPanel.add(initials = new JTextField(7), c);

		ArrayList<CameraSourceType> camTypes = cameraGrabber.getCameraSouceTypes();
		for (CameraSourceType ct : camTypes) {
			sourceTypes.addItem(ct);
		}
		
		sourceTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newSourceType();
			}
		});
	}


	protected void newSourceType() {
		CameraSourceType ct = (CameraSourceType) sourceTypes.getSelectedItem();
		centralPanel.removeAll();
		if (ct == null) {
			cameraSourcePanel = null;
			return;
		}
		cameraSourcePanel = ct.getDialogPanel(null);
		if (cameraSourcePanel != null) {
			centralPanel.add(cameraSourcePanel.getDialogComponent());
			cameraSourcePanel.setParams(cameraParams);
		}
		grabberDialog.pack();
	}


	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(CameraParams cameraParams) {
		this.cameraParams = cameraParams;
		initials.setText(cameraParams.imageInitials);
		if (cameraParams != null) {
			CameraSourceType ct = grabberDialog.getCameraGrabber().findSourceType(cameraParams.sourceType);
			if (ct != null) {
				sourceTypes.setSelectedItem(ct);
			}
		}
		newSourceType();
	}

	public CameraParams getParams() {
		cameraParams.imageInitials = initials.getText();
		if (cameraParams.imageInitials == null) {
			 PamDialog.showWarning(null, "Camera " + camIndex, "you must set initals for camera image files");
			 return null;
		}

		CameraSourceType ct = (CameraSourceType) sourceTypes.getSelectedItem();
		CameraParams newParams = null;
		if (cameraSourcePanel != null) {
			newParams = cameraSourcePanel.getParams(); 
		}
		if (newParams == null) {
			return null;
		}
		newParams.sourceType = ct.getName();
		
//		String name = (String) cameraList.getSelectedItem();
//		if (name == null) {
//			return PamDialog.showWarning(null, "Camera " + camIndex, "No camera selected");
//		}
//		cameraParams.cameraName = name;
//		cameraParams.cameraIndex = cameraList.getSelectedIndex();
//		int dInd = camDimensions.getSelectedIndex();
//		if (currentDimensions != null && dInd >= 0 && dInd < currentDimensions.length) {
//			cameraParams.dimension = currentDimensions[dInd];
//		}
		return newParams;
	}
}
