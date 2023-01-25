package levelMeter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ProgressBarUI;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamView.PamSidePanel;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;

public class LevelMeterSidePanel extends PamObserverAdapter implements PamSidePanel  {

	private LevelMeterControl levelMeterControl;

	private PamPanel mainPanel;

	private PamDataBlock parentBlock;
	
	private JLabel minLab, maxLab, scaleLab;
	
	private LevelDisplay[] levelDisplays = new LevelDisplay[PamConstants.MAX_CHANNELS];

	private JLabel[] chLab = new JLabel[PamConstants.MAX_CHANNELS];
	
	private double[] gains = new double[PamConstants.MAX_CHANNELS];
	
	private double[] dbFullScale = new double[PamConstants.MAX_CHANNELS];

	private double voltsPeak2Peak;
	
	private double maxFullScale;

	public LevelMeterSidePanel(LevelMeterControl levelMeterControl) {
		this.levelMeterControl = levelMeterControl;
		mainPanel = new PamPanel(PamColor.BORDER);
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder(levelMeterControl.getUnitName()));
		GridBagConstraints c = new PamGridBagContraints();
		minLab = new PamLabel(" ", JLabel.LEFT);
		scaleLab = new PamLabel(" ", JLabel.CENTER);
		maxLab = new PamLabel("0", JLabel.RIGHT);
		PamPanel scalePanel = new PamPanel(new BorderLayout());
		scalePanel.add(minLab, BorderLayout.WEST);
		scalePanel.add(scaleLab, BorderLayout.CENTER);
		scalePanel.add(maxLab, BorderLayout.EAST);
		c.gridx = 1;
		c.gridwidth = 3;
		mainPanel.add(scalePanel, c);
//		PamDialog.addComponent(mainPanel, minLab, c);
//		c.gridx += 1;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.anchor = GridBagConstraints.CENTER;
//		PamDialog.addComponent(mainPanel, scaleLab, c);
//		c.gridx += 1;
//		PamDialog.addComponent(mainPanel, maxLab, c);
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			PamDialog.addComponent(mainPanel, chLab[i] = new PamLabel(String.format("%d", i), JLabel.LEFT), c);
			c.gridx = 1;
			c.gridwidth = 3;
			levelDisplays[i] = new LevelDisplay(i);
			PamDialog.addComponent(mainPanel, levelDisplays[i].getComponent(), c);
			chLab[i].setVisible(false);
			levelDisplays[i].getComponent().setVisible(false);
		}
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		mainPanel.setBorder(new TitledBorder(levelMeterControl.getUnitName()));
		
	}

	public void setup() {
		if (parentBlock != null) {
			parentBlock.deleteObserver(this);
		}
		parentBlock = PamController.getInstance().getDataBlock(RawDataUnit.class, levelMeterControl.levelMeterParams.dataName);
		if (parentBlock != null) {
			parentBlock.addObserver(this);
		}
		
		minLab.setText(String.format("%d dB", levelMeterControl.levelMeterParams.minLevel));
		int channelMap = 0;
		if (parentBlock != null) {
			channelMap = parentBlock.getChannelMap();
		}
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & channelMap) != 0) {
				levelDisplays[i].getComponent().setVisible(true);
				chLab[i].setVisible(true);
			}
			else {
				levelDisplays[i].getComponent().setVisible(false);
				chLab[i].setVisible(false);
			}
			levelDisplays[i].setLimits(levelMeterControl.levelMeterParams.minLevel, 0);
		}
		
		checkConstants();
	}
	private void checkConstants() {
		// now find the source acquisition module and any gain that's in between them. 
		PamRawDataBlock rawBlock = (PamRawDataBlock) parentBlock;		
		if (rawBlock==null) {
			return;
		}
		int channelMap = parentBlock.getChannelMap();
		AcquisitionProcess daqProcess = (AcquisitionProcess) rawBlock.getSourceProcess();
		AcquisitionControl daqControl = daqProcess.getAcquisitionControl();
		voltsPeak2Peak = daqControl.acquisitionParameters.voltsPeak2Peak;
		int nChan = PamUtils.getNumChannels(channelMap);
		maxFullScale = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < nChan; i++) {
			int chan = PamUtils.getNthChannel(i, channelMap);
			gains[chan] = rawBlock.getDataGain(chan);
			dbFullScale[chan] = daqProcess.rawAmplitude2dB(1, chan, false);
			maxFullScale = Math.max(maxFullScale, dbFullScale[chan]);
		}
		setupLabels();
	}

	private void setupLabels() {
		double maxScaleVal = 0;
		double minScaleVal = 0;
		String unit = "unknown";
		String tip = null;
		switch (levelMeterControl.levelMeterParams.scaleReference) {
		case LevelMeterParams.DISPLAY_FULLSCALE:
			maxScaleVal = 0;
			unit = "FS";
			tip = "Relative to acquisition full scale";
			break;
		case LevelMeterParams.DISPLAY_VOLTS:
			maxScaleVal = 20*Math.log10(Math.ceil(voltsPeak2Peak/2));
			unit = "1V";
			tip = "Relative to 1 Volt";
			break;
		case LevelMeterParams.DISPLAY_MICROPASCAL:
			maxScaleVal = Math.ceil(maxFullScale);
			unit = "1\u03BCPa"; // \u03BC is unicode micro symbol
			tip = "Relative to 1 micropascal";
			break;
		}
		maxScaleVal = Math.ceil(maxScaleVal);
		minScaleVal = maxScaleVal + levelMeterControl.levelMeterParams.minLevel;
		
		String type, longtype;
		if (levelMeterControl.levelMeterParams.scaleType == LevelMeterParams.DISPLAY_PEAK) {
			type = "peak";
			longtype = "Zero to peak, ";
		}
		else {
			type = "RMS";
			longtype = "RMS, ";
		}
		tip = longtype + tip;
		
		minLab.setText(String.format("%3.0f", minScaleVal));
		scaleLab.setText(String.format("dB re.%s %s", unit, type));
		maxLab.setText(String.format("%3.0f", maxScaleVal));
		minLab.setToolTipText(tip);
		maxLab.setToolTipText(tip);
		scaleLab.setToolTipText(tip);
		for (int i = 0; i < levelDisplays.length; i++) {
			if (levelDisplays[i] == null) {
				continue;
			}
			levelDisplays[i].progressBar.setToolTipText(tip);
			levelDisplays[i].setLimits((int) minScaleVal, (int) maxScaleVal);
		}
		
		
	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
		RawDataUnit rawDataUnit = (RawDataUnit) arg;
		int chan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		double level = 0;
		double[] data = rawDataUnit.getRawData();
		if (levelMeterControl.levelMeterParams.scaleType == LevelMeterParams.DISPLAY_PEAK) {
			for (int i = 0; i < data.length; i++) {
				level = Math.max(level, Math.abs(data[i]));
			}
		}
		else { // it's RMS
			for (int i = 0; i < data.length; i++) {
				level += Math.pow(data[i],2);
			}
			level = Math.sqrt(level / data.length);
		}
		level = convertLevel2dB(level, chan);
		
		levelDisplays[chan].setLevel(level);
	}
	
	private double convertLevel2dB(double level, int channel) {
		switch (levelMeterControl.levelMeterParams.scaleReference) {
		case LevelMeterParams.DISPLAY_FULLSCALE:
			return 20.*Math.log10(level);
		case LevelMeterParams.DISPLAY_VOLTS:
			return 20.*Math.log10(level*(voltsPeak2Peak/2));
		case LevelMeterParams.DISPLAY_MICROPASCAL:
			return 20.*Math.log10(level) + dbFullScale[channel];
		}
		return 0;
	}

	@Override
	public void noteNewSettings() {
		setup();
	}

	@Override
	public String getObserverName() {
		return levelMeterControl.getUnitName();
	}

	@Override
	public PamObserver getObserverObject() {
		// TODO Auto-generated method stub
		return null;
	}

	class LevelDisplay {
		
		private JProgressBar progressBar;
		private ProgressBarUI pbUI;
		private Color normalColor;
		private Color highColor = Color.red;
		
		public LevelDisplay(int chan) {
			progressBar = new JProgressBar();
			progressBar.setStringPainted(true);
			progressBar.setString("");
			normalColor = progressBar.getForeground();
		}
		
		Component getComponent() {
			return progressBar;
		}
		
		void setLimits(int min, int max) {
			progressBar.setMinimum(min);
			progressBar.setMaximum(max);
			progressBar.setValue(min+5);
			progressBar.setString("no data");
//			setLevel(min);
		}
		
		void setLevel(double level) {
//			System.out.printf("Min %d, max %d, level %3.1f\n", progressBar.getMinimum(), progressBar.getMaximum(), level);
			progressBar.setValue((int) level);
			progressBar.setString(String.format("%3.1fdB",level));
			progressBar.setForeground(level >= progressBar.getMaximum()-10. ? highColor: normalColor);
		}
		
	}
	
	class LevelBarUI extends ProgressBarUI {
		
	}
}
