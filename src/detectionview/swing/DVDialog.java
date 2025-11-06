package detectionview.swing;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;
import detectionview.DVControl;
import detectionview.DVParameters;

public class DVDialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;

	private DVParameters dvParameters;
	
	private DVControl dvControl;
	
	private static DVDialog singleinstance;
	
	private JCheckBox useDefault;
	private SourcePanel detectorSource, rawSource;
	private JTextField preWindow, postWindow;
	private JLabel preSamples, postSamples;

	private DVDialog(DVControl dvControl) {
		super(dvControl.getGuiFrame(), dvControl.getUnitName() + " settings", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		detectorSource = new SourcePanel(this, "Detector", PamDetection.class, false, true);
		rawSource = new SourcePanel(this, RawDataUnit.class, false, true);
		useDefault = new JCheckBox("Use Default");
		JPanel sSub = new JPanel();
		sSub.setLayout(new BoxLayout(sSub, BoxLayout.Y_AXIS));
		sSub.add(new WestAlignedPanel(useDefault));
		sSub.add(rawSource.getPanel());
		sSub.setBorder(new TitledBorder("Raw data source"));
		
		JPanel winPanel = new JPanel(new GridBagLayout());
		winPanel.setBorder(new TitledBorder("Window parameters"));
		GridBagConstraints c = new PamGridBagContraints();
		winPanel.add(new JLabel("Pre sample ", JLabel.RIGHT), c);
		c.gridx++;
		winPanel.add(preWindow = new JTextField(4), c);
		c.gridx++;
		winPanel.add(preSamples = new JLabel(" s = "), c);
		c.gridx = 0;
		c.gridy++;
		winPanel.add(new JLabel("Post sample ", JLabel.RIGHT), c);
		c.gridx++;
		winPanel.add(postWindow = new JTextField(4), c);
		c.gridx++;
		winPanel.add(postSamples = new JLabel(" s = "), c);
		
		
		mainPanel.add(detectorSource.getPanel());
		mainPanel.add(sSub);
		mainPanel.add(new PamAlignmentPanel(winPanel, BorderLayout.WEST, true));
		
		useDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				useDefaultPressed();
			}
		});
		preWindow.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				setSampleWindows();
			}
			
		});
		postWindow.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				setSampleWindows();
			}
		});
		preWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSampleWindows();				
			}
		});
		postWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSampleWindows();				
			}
		});
		
		setDialogComponent(mainPanel);
	}
	
	protected void useDefaultPressed() {
		boolean useDef = useDefault.isSelected();
		if (useDef) {
			findDefaultSource();
		}
		enableControls();
	}

	private void enableControls() {
		rawSource.setEnabled(useDefault.isSelected() == false);
	}

	/**
	 * find the first raw data source upstream of the detector. 
	 * and set it as the selected input block. 
	 */
	private void findDefaultSource() {
		// TODO Auto-generated method stub
		
	}

	public static DVParameters showDialog(DVControl dvControl) {
//		if (singleinstance == null || singleinstance.dvControl != dvControl) {
			singleinstance = new DVDialog(dvControl); 
//		}
		singleinstance.setParams(dvControl.getDvParameters());
		singleinstance.setVisible(true);
		
		return singleinstance.dvParameters;
	}

	private void setParams(DVParameters dvParameters) {
		this.dvParameters = dvParameters;
		detectorSource.setSource(dvParameters.detectorName);
		rawSource.setSource(dvParameters.rawDataName);
		preWindow.setText(Double.valueOf(dvParameters.preSeconds).toString());
		postWindow.setText(Double.valueOf(dvParameters.postSeconds).toString());
		useDefault.setSelected(dvParameters.autoFindRaw);
		
		if (dvParameters.autoFindRaw) {
			findDefaultSource();
		}
		setSampleWindows();
		
		enableControls();
	}

	private void setSampleWindows() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setSampleWindows2();
			}
		});
		
	}
		private void setSampleWindows2() {
		if (getParams() == false) {
			return;
		}
		PamDataBlock rawBlock = rawSource.getSource();
		if (rawBlock == null) {
			preSamples.setText(" s");
			postSamples.setText(" s");
			return;
		}
		float fs = rawBlock.getSampleRate();
		String fmt = "seconds = %d samples";
		preSamples.setText(String.format(fmt, (int) (fs*dvParameters.preSeconds)));
		postSamples.setText(String.format(fmt, (int) (fs*dvParameters.postSeconds)));
		
	}

	@Override
	public boolean getParams() {
		PamDataBlock b = detectorSource.getSource();
		if (b == null) {
			return showWarning("no detector data selected");
		}
		dvParameters.detectorName = b.getLongDataName();
		b = rawSource.getSource();
		if (b == null) {
			return showWarning("Unable to find a raw data source");
		}
		dvParameters.rawDataName = b.getLongDataName();
		dvParameters.autoFindRaw = useDefault.isSelected();
		try {
			dvParameters.preSeconds = Double.valueOf(preWindow.getText());
			dvParameters.postSeconds = Double.valueOf(postWindow.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid window parameter");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		dvParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
