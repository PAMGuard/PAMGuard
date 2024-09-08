package spectrogramNoiseReduction;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataUnit;

public class SpectrogramNoiseDialog extends PamDialog {

	private SpectrogramNoiseProcess spectrogramNoiseProcess; 

	private static SpectrogramNoiseDialog singleInstance;
	
	private SpectrogramNoiseDialogPanel spectrogramNoiseDialogPanel;

	private SpectrogramNoiseSettings spectrogramNoiseSettings;

	private SourcePanel sourcePanel;

	private SpectrogramNoiseDialog(Window parentFrame, SpectrogramNoiseProcess spectrogramNoiseProcess) {
		super(parentFrame, "Spectrogram Noise Reduction", false);
		this.spectrogramNoiseProcess = spectrogramNoiseProcess;
		spectrogramNoiseDialogPanel = new SpectrogramNoiseDialogPanel(spectrogramNoiseProcess);

		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(10,10,10,10));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		sourcePanel = new SourcePanel(this, "Source of FFT data", FFTDataUnit.class, true, true);
		spectrogramNoiseDialogPanel.setSourcePanel(sourcePanel);
		p.add(sourcePanel.getPanel());
		p.add(spectrogramNoiseDialogPanel.getPanel());
//		methods = spectrogramNoiseProcess.getMethods();
//		JPanel methodPanel;
//		SpecNoiseDialogComponent dC;
//		JComponent component;
//		enableMethod = new JCheckBox[methods.size()];

//		for (int i = 0; i < methods.size(); i++) {
//			methodPanel = new JPanel();
//			methodPanel.setBorder(new TitledBorder(methods.get(i).getName()));
//			methodPanel.setLayout(new BorderLayout());
//			methodPanel.add(BorderLayout.NORTH, enableMethod[i] = 
//				new JCheckBox("Run " + methods.get(i).getName()));
//			enableMethod[i].addActionListener(new CheckEnable());
//			dC = methods.get(i).getDialogComponent();
//			if (dC != null) {
//				component = dC.getSwingComponent();
//				if (component != null) {
//					methodPanel.add(BorderLayout.CENTER, component);
//				}
//			}
//			p.add(methodPanel);
//		}

		setDialogComponent(p);

	}

	public static SpectrogramNoiseSettings showDialog(Frame parentFrame,
			SpectrogramNoiseProcess spectrogramNoiseProcess,
			SpectrogramNoiseSettings spectrogramNoiseSettings) {

		singleInstance = new SpectrogramNoiseDialog(parentFrame, spectrogramNoiseProcess);
		singleInstance.spectrogramNoiseSettings = spectrogramNoiseSettings;
		singleInstance.setParams();
		singleInstance.setVisible(true);

		return singleInstance.spectrogramNoiseSettings;
	}

	@Override
	public void cancelButtonPressed() {
		spectrogramNoiseSettings = null;
	}

	private void setParams() {

		sourcePanel.clearExcludeList();
		sourcePanel.excludeDataBlock(spectrogramNoiseProcess.outputData, true);
		sourcePanel.setSource(spectrogramNoiseSettings.dataSource);
		sourcePanel.setChannelList(spectrogramNoiseSettings.channelList);

		spectrogramNoiseDialogPanel.setParams(spectrogramNoiseSettings);
	}

	@Override
	public boolean getParams() {
		
		PamDataBlock dataSource = sourcePanel.getSource();
		if (dataSource != null) {
			spectrogramNoiseSettings.dataSource = dataSource.getDataName();
//			spectrogramNoiseSettings.chanOrSeqList = dataSource.getChannelMap();
			spectrogramNoiseSettings.channelList = dataSource.getSequenceMap();
		}
		return spectrogramNoiseDialogPanel.getParams(spectrogramNoiseSettings);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
