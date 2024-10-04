package seismicVeto;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataUnit;

public class VetoParametersDialog extends PamDialog {
	
	private SourcePanel sourcePanel;
	
	private JTextField threshold;
	
	private JTextField f1, f2;
	
	private JTextField timeConstant;
	
	private JTextField preTime, postTime;
	
	private JRadioButton specFill, specZeros, waveFill, waveZeros;
	
	private static VetoParametersDialog singleInstance;
	
	private VetoParameters vetoParameters;
	
	private PamDataBlock excludedSource;

	private VetoParametersDialog(Frame parentFrame) {
		
		super(parentFrame, "Seismic Veto Parameters", false);

		createDialog();
		
	}
	
	private void createDialog() {
		JPanel mainPanel = new JPanel();
		
//		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setLayout(new BorderLayout());
//		GridBagConstraints con = new GridBagConstraints();
		
		// source panel
		sourcePanel = new SourcePanel(this, "FFT Data Source", FFTDataUnit.class, true, false);
		
		// veto trigger panel
		JPanel trigPanel = new JPanel();
		trigPanel.setLayout(new GridBagLayout());
		trigPanel.setBorder(new TitledBorder("Veto Trigger"));
		GridBagConstraints tCon = new GridBagConstraints();
		tCon.fill = GridBagConstraints.HORIZONTAL;
		tCon.gridx = tCon.gridy = 0;
		addComponent(trigPanel, new JLabel("Trigger threshold "), tCon);
		tCon.gridx++;
		addComponent(trigPanel, threshold = new JTextField(5), tCon);
		tCon.gridx++;
		addComponent(trigPanel, new JLabel(" dB"), tCon);
		tCon.gridx = 0;
		tCon.gridy ++;
		addComponent(trigPanel, new JLabel("Background smoothing constant "), tCon);
		tCon.gridx++;
		addComponent(trigPanel, timeConstant = new JTextField(5), tCon);
		tCon.gridx++;
		addComponent(trigPanel, new JLabel(" s"), tCon);
		tCon.gridx = 0;
		tCon.gridy ++;
		addComponent(trigPanel, new JLabel("low frequency "), tCon);
		tCon.gridx++;
		addComponent(trigPanel, f1 = new JTextField(5), tCon);
		tCon.gridx++;
		addComponent(trigPanel, new JLabel(" Hz"), tCon);
		tCon.gridx = 0;
		tCon.gridy ++;
		addComponent(trigPanel, new JLabel("high frequency "), tCon);
		tCon.gridx++;
		addComponent(trigPanel, f2 = new JTextField(5), tCon);
		tCon.gridx++;
		addComponent(trigPanel, new JLabel(" Hz"), tCon);

		
		// veto action panel
		JPanel vetoPanel = new JPanel();
		vetoPanel.setLayout(new GridBagLayout());
		vetoPanel.setBorder(new TitledBorder("Veto Actions"));
		GridBagConstraints vCon = new GridBagConstraints();
		vCon.fill = GridBagConstraints.HORIZONTAL;
		vCon.gridx = vCon.gridy = 0;
		addComponent(vetoPanel, new JLabel("Veto time before trigger "), vCon);
		vCon.gridx++;
		addComponent(vetoPanel, preTime = new JTextField(5), vCon);
		vCon.gridx++;
		addComponent(vetoPanel, new JLabel(" s"), vCon);
		vCon.gridx = 0;
		vCon.gridy ++;
		addComponent(vetoPanel, new JLabel("Veto time after trigger "), vCon);
		vCon.gridx++;
		addComponent(vetoPanel, postTime = new JTextField(5), vCon);
		vCon.gridx++;
		addComponent(vetoPanel, new JLabel(" s"), vCon);
		vCon.gridx = 0;
		vCon.gridy ++;
		vCon.gridwidth = 3;
		addComponent(vetoPanel, specFill = new JRadioButton("Fill FFT data with random noise"), vCon);
		vCon.gridy++;
		addComponent(vetoPanel, specZeros = new JRadioButton("Fill FFT data with zeros"), vCon);
		vCon.gridy++;
		addComponent(vetoPanel, waveFill = new JRadioButton("Fill waveform data with random noise"), vCon);
		vCon.gridy++;
		addComponent(vetoPanel, waveZeros = new JRadioButton("Fill waveform data with zeros"), vCon);
		
		ButtonGroup bgSpec = new ButtonGroup();
		bgSpec.add(specFill);
		bgSpec.add(specZeros);
		ButtonGroup bgWave = new ButtonGroup();
		bgWave.add(waveFill);
		bgWave.add(waveZeros);
		
//		con.gridy = 0;
//		addComponent(mainPanel, sourcePanel.getPanel(), con);
//		con.gridy = 1;
//		addComponent(mainPanel, trigPanel, con);
//		con.gridy = 2;
//		addComponent(mainPanel, vetoPanel, con);
		mainPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
		mainPanel.add(BorderLayout.CENTER, trigPanel);
		mainPanel.add(BorderLayout.SOUTH, vetoPanel);
		

		setHelpPoint("sound_processing.seismicveto.docs.veto_overview");
		setDialogComponent(mainPanel);
	}
	
	public static VetoParameters showDialog(Frame parentFrame, VetoParameters vetoParameters, PamDataBlock excludedFFTSource) {

		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new VetoParametersDialog(parentFrame);
		}
		singleInstance.vetoParameters = vetoParameters;
		singleInstance.excludedSource = excludedFFTSource;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.vetoParameters;
	}
	

	@Override
	public void cancelButtonPressed() {
		vetoParameters = null;
	}

	private void setParams() {
		sourcePanel.clearExcludeList();
		sourcePanel.excludeDataBlock(excludedSource, true);
		sourcePanel.setSource(vetoParameters.dataSourceName);
		sourcePanel.setChannelList(vetoParameters.channelBitmap);
		
		threshold.setText(String.format("%3.1f", vetoParameters.threshold));
		timeConstant.setText(String.format("%3.1f", vetoParameters.backgroundConstant));
		f1.setText(String.format("%3.1f", vetoParameters.f1));
		f2.setText(String.format("%3.1f", vetoParameters.f2));	
		

		preTime.setText(String.format("%3.1f", vetoParameters.vetoPreTime));	
		postTime.setText(String.format("%3.1f", vetoParameters.vetoPostTime));	
		specFill.setSelected(vetoParameters.randomFillSpectorgram);
		specZeros.setSelected(!vetoParameters.randomFillSpectorgram);
		waveFill.setSelected(vetoParameters.randomFillWaveform);
		waveZeros.setSelected(!vetoParameters.randomFillWaveform);
		
	}
	
	@Override
	public boolean getParams() {
		PamDataBlock sourceBlock = sourcePanel.getSource();
		if (sourceBlock == null) {
			return false;
		}
		vetoParameters.dataSourceName = sourceBlock.getDataName();
		vetoParameters.channelBitmap = sourcePanel.getChannelList();
		
		try {
			vetoParameters.threshold = Double.valueOf(threshold.getText());
			vetoParameters.backgroundConstant = Double.valueOf(timeConstant.getText());
			vetoParameters.f1 = Double.valueOf(f1.getText());
			vetoParameters.f2 = Double.valueOf(f2.getText());
			
			vetoParameters.vetoPreTime = Double.valueOf(preTime.getText());
			vetoParameters.vetoPostTime = Double.valueOf(postTime.getText());
			vetoParameters.randomFillSpectorgram = specFill.isSelected();
			vetoParameters.randomFillWaveform = waveFill.isSelected();
		}
		catch (NumberFormatException ex) {
			return false;
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
