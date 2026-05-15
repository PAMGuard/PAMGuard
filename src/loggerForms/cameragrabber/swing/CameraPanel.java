package loggerForms.cameragrabber.swing;

import java.awt.BorderLayout;
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
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.github.sarxos.webcam.Webcam;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import loggerForms.cameragrabber.CameraParams;

public class CameraPanel implements PamDialogPanel {

	private int camIndex;

	private JPanel mainPanel;

	private JComboBox<String> cameraList;

	private JComboBox<String> camDimensions;

	private CameraParams cameraParams;

	private Dimension[] currentDimensions;
	
	private JTextField initials;

	public CameraPanel(int camIndex) {
		super();
		this.camIndex = camIndex;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Camera " + camIndex));
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
		mainPanel.add(new JLabel("Image initials ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(initials = new JTextField(7), c);

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

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		fillCameraList();

	}

	@Override
	public boolean getParams() {
		return false;
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

	public void setParams(CameraParams cameraParams) {
		this.cameraParams = cameraParams;
		initials.setText(cameraParams.imageInitials);
		fillCameraList();
		cameraList.setSelectedItem(cameraParams.cameraName);
		selectCamera();
	}

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
}
