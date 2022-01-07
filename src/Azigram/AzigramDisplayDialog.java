package Azigram;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import noiseMonitor.ResolutionPanel;


public class AzigramDisplayDialog extends PamDialog {
	
	private static AzigramDisplayDialog singleInstance;
	
	private AzigramControl azigramControl;
	
	private AzigramParameters azigramParameters;
	
	private JComboBox<Float> sampleRateCombo;
	Float[] outputRates;
	
//	private SourcePanel sourcePanel;
	private GroupedSourcePanel sourcePanel;
	
	private ResolutionPanel resolutionPanel = new ResolutionPanel();
	
	public AzigramDisplayDialog(Window parentFrame, AzigramControl azigramControl) {
		super(parentFrame, "Azigram Settings", true);
		this.azigramControl = azigramControl;
		JPanel p = new JPanel(new BorderLayout());
//		p.setBorder(new TitledBorder("Input"));
		sourcePanel = new GroupedSourcePanel(this, "Data Source", FFTDataUnit.class, true, true, false);
		p.add(BorderLayout.NORTH, sourcePanel.getPanel());
//		this.add(p);
		
		outputRates = azigramControl.azigramParameters.getOutputRateList();
		sampleRateCombo = new JComboBox<Float>(outputRates);
		JPanel outputPanel = new PamPanel();
		outputPanel.setBorder(new TitledBorder("Output") );
		outputPanel.setLayout(new BoxLayout(outputPanel,BoxLayout.Y_AXIS));

		PamPanel sampleRatePanel = new PamPanel(new FlowLayout());
		sampleRatePanel.add(new PamLabel("Sample Rate:"));
		sampleRatePanel.add(sampleRateCombo);
		sampleRatePanel.add(new PamLabel("Hz"));
		String tooltip = "<HTML><i>FFT Length</i> and <i>hop</i> are <br>controlled by the Source FFT module.</HTML>";
		
		outputPanel.add(BorderLayout.NORTH,sampleRatePanel);
		outputPanel.add(BorderLayout.CENTER,resolutionPanel.getPanel());

		sampleRatePanel.setToolTipText(tooltip);
		sampleRateCombo.setToolTipText(tooltip);
		resolutionPanel.getPanel().setToolTipText(tooltip);
		
		sampleRateCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FFTDataBlock inputBlock = (FFTDataBlock) azigramControl.azigramProcess.getInputDataBlock();
				float outputSampleRate = (float) sampleRateCombo.getSelectedItem();	
				int decimateFactor = (int) (inputBlock.getSampleRate()/outputSampleRate);
				resolutionPanel.setParams(outputSampleRate, inputBlock.getFftLength()/decimateFactor,
						inputBlock.getFftHop()/decimateFactor);
			}
			
		});
		
		p.add(BorderLayout.CENTER,outputPanel);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Data", p);
		setDialogComponent(tabbedPane);
		AzigramPlugin azPlug = new AzigramPlugin();
		
		// Can't for the life of me figure out how to reference help for a 
		// plugin, so just pick the next nearest thing
		setHelpPoint("sound_processing.DifarAudio.docs.directionalAudio");
		
	}


	@Override
	public boolean getParams() {
		PamDataBlock dataSource = sourcePanel.getSource();
		if (dataSource != null) {
			sourcePanel.getParams(azigramParameters.dataSource);
		}
		
		try{
			azigramParameters.outputSampleRate = (float) sampleRateCombo.getSelectedItem();
		}catch(Exception e ){
			
			return showWarning("Parameter problem: sample rate ");
		}
		return true;
	}
	
	private void setParams() {
		sourcePanel.setParams(azigramParameters.dataSource);
		
		sampleRateCombo.setSelectedItem(azigramParameters.outputSampleRate);			
		
		FFTDataBlock datablock = azigramControl.azigramProcess.getOutputDataBlock();
		if (datablock == null) {
			resolutionPanel.setParams(null);
		}
		else {
			resolutionPanel.setParams(datablock.getSampleRate(), datablock.getFftLength(),
					datablock.getFftHop());
		}
		
	}

	@Override
	public void cancelButtonPressed() {
		azigramParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		PamDataBlock dataSource = sourcePanel.getSource();
		if (dataSource == null) {
			return;
		}
		AzigramParameters dummyParams = new AzigramParameters();
//		azigramParameters.dataSourceName = dataSource.getDataName();
		azigramParameters = dummyParams;

	}


	public static AzigramParameters showDialog(AzigramControl azigramControl2, Frame parentFrame) {
		if (singleInstance == null || azigramControl2 != singleInstance.azigramControl ||
				parentFrame != singleInstance.getOwner()) {
			singleInstance = new AzigramDisplayDialog(parentFrame, azigramControl2);
		}
		singleInstance.azigramParameters = azigramControl2.azigramParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		singleInstance.azigramControl.azigramProcess.setupProcess();
		return singleInstance.azigramParameters;
	}





}
