package Array.sensors.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Array.sensors.ArrayDisplayParameters;
import PamUtils.LatLong;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamFilePanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.RemoveButton;
import PamView.panel.PamAlignmentPanel;

public class PitchRollDialogPanel implements PamDialogPanel {
	
	private static final String[] imageTypes = {".jpg",".png",".bmp"};
	private JPanel mainPanel;
	private ArrayDisplayParamsDialog arrayDisplayParamsDialog;
	private PamFilePanel imageFilePanel;
	private JTextField[] pitchRange;
	private JTextField pitchStep;

	public PitchRollDialogPanel(ArrayDisplayParamsDialog arrayDisplayParamsDialog) {
		this.arrayDisplayParamsDialog = arrayDisplayParamsDialog;
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Pitch and Roll"));
		mainPanel.setLayout(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		imageFilePanel = new PamFilePanel(arrayDisplayParamsDialog.getOwner(), imageTypes[0], null);
		northPanel.add(BorderLayout.CENTER, imageFilePanel);
		northPanel.add(BorderLayout.NORTH, new JLabel("Optional image file"));
		imageFilePanel.setFileTypes(imageTypes);
		JButton removeButton = new RemoveButton();
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 2;
		imageFilePanel.add(removeButton, c);
		removeButton.setToolTipText("No image (use default linedrawing)");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearImageFile();
			}
		});
		JPanel centPanel = new JPanel(new GridBagLayout());
		pitchRange = new JTextField[2];
		c = new PamGridBagContraints();
		centPanel.add(new JLabel("Pitch range ", JLabel.RIGHT), c);
		c.gridx++;
		centPanel.add(pitchRange[0] = new JTextField(4), c);
		c.gridx++;
		centPanel.add(new JLabel(" to ", JLabel.CENTER), c);
		c.gridx++;
		centPanel.add(pitchRange[1] = new JTextField(4), c);
		c.gridx++;
		centPanel.add(new JLabel(LatLong.deg, JLabel.LEFT), c);
		c.gridx++;
		centPanel.add(new JLabel(",  Scale step ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridx++;
		centPanel.add(pitchStep = new JTextField(3), c);
		c.gridx++;
		centPanel.add(new JLabel(LatLong.deg, JLabel.LEFT), c);
		
		mainPanel.add(BorderLayout.CENTER, new PamAlignmentPanel(centPanel, BorderLayout.WEST));
		mainPanel.add(BorderLayout.NORTH, northPanel);
	}

	protected void clearImageFile() {
		imageFilePanel.setFile(null);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		ArrayDisplayParameters params = arrayDisplayParamsDialog.currentParams;
		imageFilePanel.setFile(params.getPitchRollImageFile());
		double[] range = params.getPitchRange();
		for (int i = 0; i < 2; i++) {
			pitchRange[i].setText(Double.valueOf(range[i]).toString());
		}
		pitchStep.setText(Double.valueOf(params.getPitchStep()).toString());
		
	}

	@Override
	public boolean getParams() {
		ArrayDisplayParameters params = arrayDisplayParamsDialog.currentParams;
		File imageFile = imageFilePanel.getFile(false);
		if (imageFile!=null && imageFile.exists() == false) {
			arrayDisplayParamsDialog.showWarning("Image file does not exist");
		}
		if (imageFile != null) {
			params.setPitchRollImageFile(imageFile.getAbsolutePath());
		}
		else {
			params.setPitchRollImageFile(null);
		}
		double[] range = new double[2];
		double step = 0;
		try {
			for (int i = 0; i < 2; i++) {
				range[i] = Double.valueOf(pitchRange[i].getText());
			}
			step = Double.valueOf(pitchStep.getText());
		}
		catch (NumberFormatException e) {
			return arrayDisplayParamsDialog.showWarning("Invalid angle in pitch roll dialog");
		}
		if (range[1] <= range[0]) {
			return arrayDisplayParamsDialog.showWarning("The second pitch range angle nust be greater than the first");
		}
		if (step <= 0) {
			return arrayDisplayParamsDialog.showWarning("The angle step for the scale must be > 0");
		}
		params.setPitchRange(range);
		params.setPitchStep(step);
		return true;
	}

}
