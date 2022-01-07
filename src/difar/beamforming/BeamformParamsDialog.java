/**
 * 
 */
package difar.beamforming;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import Filters.Filter;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;


/**
 * @author gw
 *
 */
public class BeamformParamsDialog extends PamDialog{

	
	private BeamformControl beamformControl;
	private BeamformParameters beamformParameters;
	private static BeamformParamsDialog singleInstance;
	
	private SourcePanel rawDataSource;
	private JLabel sourceSampleRate;
	private float sampleRate = 1;
	private JLabel newSampleRate;
	private JCheckBox useGpsNoiseSource;
	private SourcePanel noiseGpsSource;
	
	/**
	 * @param parentFrame
	 * @param title
	 * @param hasDefault
	 */
	private BeamformParamsDialog(Window parentFrame, BeamformControl beamformControl) {
		super(parentFrame, beamformControl.getUnitName() + " Parameters", true);
		this.beamformControl=beamformControl;
		JPanel dispPanel=new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		dispPanel.setBorder(new TitledBorder("DIFAR Direcional Audio"));
		c.gridwidth=2;
		c.gridx = 0;
		c.gridy = 0;
		rawDataSource = new SourcePanel(this, "Data Source", RawDataUnit.class, true, true);
		dispPanel.add(rawDataSource.getPanel(),c);
		rawDataSource.addSelectionListener(new SPSelection());
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 0;
		dispPanel.add(new JLabel("Source sample rate "), c);
		c.gridx++;
		dispPanel.add(sourceSampleRate = new JLabel(" - Hz"), c);
		c.gridx = 0;
		c.gridy++;
		dispPanel.add(new JLabel("Output sample rate "), c);
		c.gridx++;
		dispPanel.add(newSampleRate = new JLabel("" + BeamformParameters.outputSampleRate), c);
		c.gridx++;
		dispPanel.add(new JLabel(" Hz"), c);
		c.gridy++;
		c.gridwidth = 2;
		c.gridx = 0;
		useGpsNoiseSource = new JCheckBox("Use GPS data for noise reduction");
		useGpsNoiseSource.addActionListener(new UseGpsNoiseSource());
		dispPanel.add(useGpsNoiseSource,c);
		c.gridy++;
		noiseGpsSource = new SourcePanel(null, "GPS Source", GpsDataUnit.class, false, false);
		dispPanel.add(noiseGpsSource.getPanel(),c);
		c.gridy++;
		setDialogComponent(dispPanel);
		
	}
	
	public static final BeamformParameters showDialog(Window frame, BeamformControl beamformControl, BeamformParameters beamformParameters) {
		if (singleInstance == null || singleInstance.beamformControl != beamformControl || singleInstance.getOwner() != frame) {
			singleInstance = new BeamformParamsDialog(frame, beamformControl);
		}
		singleInstance.beamformParameters = beamformParameters;
		singleInstance.setParams(beamformParameters);
//		singleInstance.enableControls();
		singleInstance.pack();
		singleInstance.enableControls();
		singleInstance.setVisible(true);
		return singleInstance.beamformParameters;
	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
		try {
			PamDataBlock rawBlock = rawDataSource.getSource();
			if (rawBlock == null) {
				return showWarning("You must select a raw data source of DIFAR data");
			}
			
			beamformParameters.rawDataName = rawBlock.getDataName();
			ArrayList<PamDataBlock> rawBlocks = PamController.getInstance().getRawDataBlocks();
			beamformParameters.channelMap = rawDataSource.getChannelList();
			((BeamformSidePanel) beamformControl.getSidePanel()).newSource();
		}catch(NumberFormatException e){
			return showWarning("Bad parameter in Beamform display dialog");
		}
		try {
			beamformParameters.useGpsNoiseSource = useGpsNoiseSource.isSelected();
			beamformParameters.noiseGpsSource = noiseGpsSource.getSource().getDataName();
		}catch(Exception e){
			return showWarning("Calibration GPS Problem");
		}
		return true;
	}
	
	private void setParams(BeamformParameters beamformParams) {
		if (beamformParams == null) {
			restoreDefaultSettings();
			return;
		}
		rawDataSource.setSource(beamformParameters.rawDataName);
		rawDataSource.excludeDataBlock(beamformControl.getBeamformProcess().getOutputDataBlock(0), true);
		rawDataSource.setSourceList();
		PamRawDataBlock currentBlock = PamController.getInstance().getRawDataBlock(beamformParameters.rawDataName);
		rawDataSource.setSource(currentBlock);
		rawDataSource.setChannelList(beamformParameters.channelMap);
		useGpsNoiseSource.setSelected(beamformParameters.useGpsNoiseSource);
		noiseGpsSource.setSource(beamformParameters.noiseGpsSource);
		
		newDataSource();
//		if (offlineDAQDialogPanel != null) {
//			offlineDAQDialogPanel.setParams();
//		}
	}
	

	private class SPSelection implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			newDataSource();
		}
	}
	
	private void newDataSource() {
		PamDataBlock block = rawDataSource.getSource();
		if (block != null) {
			sourceSampleRate.setText(String.format("%.1f Hz", 
					sampleRate = block.getSampleRate()));
		}
	}
	
	private class UseGpsNoiseSource implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	
	void enableControls(){
		noiseGpsSource.setEnabled(useGpsNoiseSource.isSelected());
	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
		beamformParameters = null;
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		BeamformParameters newBeamformParameters = new BeamformParameters();
		rawDataSource.setSource(newBeamformParameters.rawDataName);
		setParams(newBeamformParameters);
	}
	
}
