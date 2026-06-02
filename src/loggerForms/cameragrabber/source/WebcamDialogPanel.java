package loggerForms.cameragrabber.source;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import loggerForms.cameragrabber.CameraParams;

public class WebcamDialogPanel implements CameraSourcePanel {

	private JPanel mainPanel;

	private JComboBox<String> cameraList;

	private JComboBox<String> camDimensions;

	private CameraParams cameraParams;

	private Dimension[] currentDimensions;
		
	public WebcamDialogPanel(WebcamSource webcamSource) {
		mainPanel = new JPanel(new GridBagLayout());
	
		cameraList = new JComboBox<String>();
		camDimensions = new JComboBox<String>();
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Camera ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(cameraList, c);
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel("Dimension ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(camDimensions, c);
		c.gridy++;
		c.gridx = 0;

		cameraList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectCamera();
			}
		});
	}

	protected void selectCamera() {
		try {
			String name = (String) cameraList.getSelectedItem();
			if (name == null) {
				return;
			}
			Webcam wc = Webcam.getWebcamByName(name);
			if (wc != null) {
				currentDimensions = wc.getViewSizes();
				camDimensions.removeAllItems();
				for (int i = 0; i < currentDimensions.length; i++) {
					String tx = formatDimension(currentDimensions[i]);
					camDimensions.addItem(tx);
				}
			}
			Dimension d = cameraParams.dimension;
			if (d != null) {
				camDimensions.setSelectedItem(formatDimension(d));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fillCameraList() {
		cameraList.removeAllItems();
		List<Webcam> cams = Webcam.getWebcams();
		for (Webcam cam : cams) {
			cameraList.addItem(cam.getName());
		}
	}
	
	private String formatDimension(Dimension d) {
		if (d == null) {
			return null;
		}
		String tx = String.format("%d x %d", d.width, d.height);
		return tx;
	}


	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	/*
	 * 
	public void setParams(CameraParams cameraParams) {
		this.cameraParams = cameraParams;
		initials.setText(cameraParams.imageInitials);
		fillCameraList();
		cameraList.setSelectedItem(cameraParams.cameraName);
		selectCamera();
	}

	 */
	@Override
	public void setParams(CameraParams cameraParams) {
		// TODO Auto-generated method stub
		this.cameraParams = cameraParams;
		fillCameraList();
		cameraList.setSelectedItem(cameraParams.cameraName);
		selectCamera();
	}

	/*
	public boolean getParams(CameraParams cameraParams) {
		cameraParams.imageInitials = initials.getText();
		if (cameraParams.imageInitials == null) {
			return PamDialog.showWarning(null, "Camera " + camIndex, "you must set initals for camera image files");
		}
		String name = (String) cameraList.getSelectedItem();
		if (name == null) {
			return PamDialog.showWarning(null, "Camera " + camIndex, "No camera selected");
		}
		cameraParams.cameraName = name;
		cameraParams.cameraIndex = cameraList.getSelectedIndex();
		int dInd = camDimensions.getSelectedIndex();
		if (currentDimensions != null && dInd >= 0 && dInd < currentDimensions.length) {
			cameraParams.dimension = currentDimensions[dInd];
		}
		return true;
	}
	*/
	@Override
	public CameraParams getParams() {
		String name = (String) cameraList.getSelectedItem();
		if (name == null) {
			 PamDialog.showWarning(null, "Error", "No camera selected");
			 return null;
		}
		cameraParams.cameraName = name;
		cameraParams.cameraIndex = cameraList.getSelectedIndex();
		int dInd = camDimensions.getSelectedIndex();
		if (currentDimensions != null && dInd >= 0 && dInd < currentDimensions.length) {
			cameraParams.dimension = currentDimensions[dInd];
		}
		return cameraParams;
	}

}
