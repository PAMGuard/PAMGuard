package SoundRecorder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Layout.PamAxis;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.ColorManaged;
import PamView.PamTabPanel;
import PamView.PamColors.PamColor;
import PamView.dialog.PamButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import SoundRecorder.RecorderTabPanel.RecorderPanel.LevelsPanel;
import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;
import SoundRecorder.trigger.TriggerOptionsDialog;

public class RecorderTabPanel implements PamTabPanel, RecorderView {

	RecorderControl recorderControl;
	
	RecorderPanel recorderPanel;

	RecorderTriggerPanel recorderTriggerPanel;
	
	LevelsPanel levelsPanel;

	JButton buttonOff, buttonAuto, buttonStart, buttonStartBuffered;
	JButton[] actionButton = new JButton[4];
	JButton buttonSettings;
	JCheckBox enableBuffer;
	JProgressBar bufferContent;
	PamLabel bufferLength;
	PamLabel recordStatus;
	PamLabel fileSize, dataSource;
	private PamLabel folderInfo;
	JCheckBox enableChannel[] = new JCheckBox[PamConstants.MAX_CHANNELS];
	PamLabel triggerMessage;
//	ArrayList<JCheckBox> enableTrigger = new ArrayList<JCheckBox>();
	
	RecorderTabPanel(RecorderControl recorderControl) {
		
		this.recorderControl = recorderControl;
		
		recorderPanel = new RecorderPanel();
	}
	
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	public JComponent getPanel() {
		return recorderPanel;
		
	}

	public JToolBar getToolBar() {
		return null;
	}

	/* (non-Javadoc)
	 * @see SoundRecorder.RecorderView#newParams()
	 */
	public void newParams() {
		enableBuffer.setSelected(recorderControl.recorderSettings.enableBuffer);
		bufferContent.setMaximum(Math.max(1, recorderControl.recorderSettings.bufferLength));
		levelsPanel.showBars();
		bufferLength.setText(String.format(" %d s", recorderControl.recorderSettings.bufferLength));
	}

	public void enableRecording(boolean enable) {
		buttonAuto.setEnabled(enable);
		buttonStart.setEnabled(enable);
		buttonStartBuffered.setEnabled(enable);
	}

	
	public void enableRecordingControl(boolean enable) {
		for (int i = 0; i < enableChannel.length; i++) {
			if (enableChannel[i] != null) {
				enableChannel[i].setEnabled(enable);
			}
		}
		buttonSettings.setEnabled(enable);
	}
	
	public void sayStatus(String status) {
		recordStatus.setText(status);
		long fileBytes = recorderControl.recorderStorage.getFileSizeBytes();
		float fileSeconds = (recorderControl.recorderStorage.getFileMilliSeconds() / 1000 );
		if (fileBytes < 0) {
			fileSize.setText(" ");
		}
		else {
			fileSize.setText(String.format("File length %.1f seconds, %.1f MegaBytes", 
					fileSeconds, (double) fileBytes / (1<<20)));
		}
		int nChannels = PamUtils.getNumChannels(recorderControl.recorderSettings.getChannelBitmap(0xFFFFFFFF));
		dataSource.setText(String.format("%d channels at %.1fkHz from %s", 
				nChannels,
				recorderControl.recorderProcess.getSampleRate()/1000.,
				recorderControl.recorderProcess.getParentDataBlock()));
		
		File outFolder = new File(recorderControl.recorderSettings.outputFolder);
		long freeSpace = outFolder.getFreeSpace();
		int bytesPerSec = (int) (nChannels * recorderControl.recorderProcess.getSampleRate() * recorderControl.recorderSettings.bitDepth / 8);
		if (bytesPerSec > 0) {
			long secsAvailable = freeSpace / bytesPerSec;
			folderInfo.setText(String.format("Output folder \"%s\". %d day(s) %s remaining on drive", 
					recorderControl.recorderSettings.outputFolder, secsAvailable/(3600*24), PamCalendar.formatTime(secsAvailable*1000L)));
		}
	}
	
	protected void timerActions() {	
		checkBuffer();
	}
	private void checkBuffer() {
		if (recorderControl.recorderSettings.enableBuffer == false) {
			bufferContent.setValue(0);
		}
		else {
			// work out how many seconds of data there are available. 
			PamRawDataBlock prd = (PamRawDataBlock) recorderControl.recorderProcess.getParentDataBlock();
			if (prd == null) return;
			// check the times on the first and last data units
			int nDataUnits = prd.getUnitsCount();
			if (nDataUnits == 0) {
				bufferContent.setValue(0);
				return;
			}
			long start = prd.getFirstUnit().getTimeMilliseconds();
			long end =  prd.getLastUnit().getTimeMilliseconds();
			int s = (int) ((end - start) / 1000);
			bufferContent.setValue(s);
		}
	}


	/* (non-Javadoc)
	 * @see SoundRecorder.RecorderView#newData(PamguardMVC.PamDataBlock, PamguardMVC.PamDataUnit)
	 */
	public void newData(PamDataBlock dataBlock, PamDataUnit dataUnit) {

		RawDataUnit rawDataUnit = (RawDataUnit) dataUnit;
		double logAmplitude = -100;
		if(rawDataUnit.getMeasuredAmplitude() > 0 && 
				(rawDataUnit.getChannelBitmap() & recorderControl.recorderSettings.getChannelBitmap(dataBlock.getChannelMap())) > 0) {
			logAmplitude = 20. * Math.log10(rawDataUnit.getMeasuredAmplitude());
		}
		levelsPanel.showLevel(PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap()), logAmplitude);
	}
	
	public void setButtonStates(int pressedButton) {
			buttonOff.setSelected(pressedButton == BUTTON_OFF);
			buttonAuto.setSelected(pressedButton == BUTTON_AUTO);
			buttonStart.setSelected(pressedButton == BUTTON_START);
			buttonStartBuffered.setSelected(pressedButton == BUTTON_START_BUFFERED);
	}
	
	class FolderButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			File file = new File (recorderControl.recorderSettings.outputFolder);
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
			} catch (IOException e1) {
				System.out.println("Unable to open folder " + recorderControl.recorderSettings.outputFolder);
			}
		}
	}
	
	class RecorderPanel extends RPanel {
		
		RecorderPanel () {
			super();
//			PamColors.getInstance().registerComponent(this, PamColor.BORDER);
//			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//			add(new ButtonsPanel());
//			add(new LevelsPanel());
//			setBorder(new TitledBorder("Sound Recorder"));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			setLayout(layout);
			constraints.anchor = GridBagConstraints.WEST;

			constraints.gridy = 0;
			constraints.gridx = 0;
			constraints.gridwidth = 2;
			addComponent(this, new ButtonsPanel(), constraints);
			constraints.gridy = 1;
			constraints.gridwidth = 2;
			constraints.insets = new Insets(10, 0, 0, 0);
			levelsPanel = new LevelsPanel();
			recorderTriggerPanel = new RecorderTriggerPanel();
			//PamBorderPanel levelsandtriggersPanel = new PamBorderPanel(fl = new FlowLayout(FlowLayout.LEFT, 10, 10));
			
//			levelsandtriggersPanel.add();			
//			levelsandtriggersPanel.add();

			constraints.gridwidth = 1;
			addComponent(this, levelsPanel, constraints);
			constraints.fill = GridBagConstraints.VERTICAL;
			constraints.anchor = GridBagConstraints.EAST;
			constraints.gridx++;
			constraints.insets = new Insets(10, 20, 0, 0);
			addComponent(this, recorderTriggerPanel, constraints);
		}
		class ButtonsPanel extends RPanel {
			private JButton folderButton;

			ButtonsPanel () {
				super();
//				PamColors.getInstance().registerComponent(this, PamColor.BORDER);

				GridBagLayout layout = new GridBagLayout();
				GridBagConstraints constraints = new GridBagConstraints();
				setLayout(layout);
//				constraints.anchor = GridBagConstraints.WEST;
				constraints.fill = GridBagConstraints.BOTH;
				constraints.weightx = 1;
				//constraints.insets = new Insets(0,20,0,20);
				recordStatus = new PamLabel();
				constraints.gridy = 0;
				constraints.gridwidth = 6;
				addComponent(this, recordStatus, constraints);
				
				fileSize = new PamLabel();
				constraints.gridy ++;
				constraints.gridwidth = 6;
				addComponent(this, fileSize, constraints);
				
				dataSource = new PamLabel();
				constraints.gridy ++;
				constraints.gridwidth = 6;
				addComponent(this, dataSource, constraints);
				
				folderInfo = new PamLabel("  ");
				constraints.gridy ++;
				constraints.gridwidth = 6;
				addComponent(this, folderInfo, constraints);
				
				folderButton = new PamButton("View Files ...");
				folderButton.setToolTipText("View recordings using the systems file explorer");
				folderButton.addActionListener(new FolderButton());
				constraints.gridy ++;
				constraints.gridx = 0;
				constraints.gridwidth = 1;
				addComponent(this, folderButton, constraints);
				
				constraints.gridy++;
				addComponent(this, new JLabel(" "), constraints);
			
				constraints.gridy ++;
				constraints.gridx = 0;
				constraints.ipady = 10;
				constraints.gridwidth = 1;
				addComponent(this, actionButton[0] = buttonOff = new RecorderButton("Off"), constraints);
				buttonOff.setSelected(true);

				constraints.gridx = 1;
				addComponent(this, actionButton[1] = buttonAuto = new RecorderButton("Automatic Cycle"), constraints);
				constraints.gridx = 2;
				addComponent(this, actionButton[2] = buttonStart = new RecorderButton("Continuous"), constraints);
				constraints.gridx = 3;
				addComponent(this, actionButton[3] = buttonStartBuffered = new RecorderButton("Continuous + Buffer"), constraints);
				constraints.gridy += 1;
				constraints.ipady = 0;
				constraints.insets = new Insets(10, 0, 0, 0);
				constraints.gridx = 0;
				addComponent(this, buttonSettings = new PamButton("Settings"), constraints);
				constraints.gridx = 2;
				addComponent(this, enableBuffer = new PamCheckBox("Enable Buffer"), constraints);
				enableBuffer.setSelected(recorderControl.recorderSettings.enableBuffer);
				enableBuffer.addActionListener(new BufferButtonListener());
				constraints.gridx = 3;
				addComponent(this, bufferContent = new JProgressBar(0,100), constraints);
				constraints.gridx = 4;
				addComponent(this, bufferLength = new PamLabel("   "), constraints);
				
				ButtonGroup buttonGroup = new ButtonGroup();
				buttonOff.addActionListener(new MainButtonListener(BUTTON_OFF));
				buttonAuto.addActionListener(new MainButtonListener(BUTTON_AUTO));
				buttonStart.addActionListener(new MainButtonListener(BUTTON_START));
				buttonStartBuffered.addActionListener(new MainButtonListener(BUTTON_START_BUFFERED));
				for (int i = 0; i < actionButton.length; i++) {
					buttonGroup.add(actionButton[i]);
				}
				buttonSettings.addActionListener(new SettingsButtonListener());
				
			}
		}
		class MainButtonListener implements ActionListener {

			int buttonCommand;
			
			MainButtonListener(int buttonCommand) {
				this.buttonCommand = buttonCommand;
			}
			public void actionPerformed(ActionEvent e) {
				recorderControl.buttonCommand(buttonCommand);
			}
		}
		class SettingsButtonListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				recorderControl.recordSettingsDialog(recorderControl.getGuiFrame());
			}
			
		}
		class BufferButtonListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				recorderControl.recorderSettings.enableBuffer = enableBuffer.isSelected();
			}
			
		}
		class LevelsPanel extends RPanel {
			JProgressBar levelMeters[] = new JProgressBar[PamConstants.MAX_CHANNELS];
			int charHeight = 10;
			LevelsPanel () {
				super();
//				PamColors.getInstance().registerComponent(this, PamColor.BORDER);
				showBars();
			}
			protected void showBars() {
				// called whenever settings change. 
				PamRawDataBlock prd = (PamRawDataBlock) recorderControl.recorderProcess.getParentDataBlock();
//				PamRawDataBlock rb = (PamRawDataBlock) recorderControl.recorderProcess.getParentDataBlock();
//				int nChan = PamUtils.getNumChannels(rb.getChannelMap());
				int nChan = PamConstants.MAX_CHANNELS/2;
				int channelMap = 1;
				if (prd != null) {
					nChan = PamUtils.getNumChannels(prd.getChannelMap());
					channelMap = prd.getChannelMap();
				}
				this.removeAll();
				GridBagLayout layout = new GridBagLayout();
				GridBagConstraints constraints = new GridBagConstraints();
				setLayout(layout);
				constraints.anchor = GridBagConstraints.CENTER;
				constraints.fill = GridBagConstraints.BOTH;
				constraints.insets = new Insets(0,0,0,0);

				constraints.gridx = 0;
				constraints.gridy = 0;
				addComponent(this, new PamLabel("Enable"), constraints);
				constraints.gridy = 1;
				addComponent(this, new LevelAxis(), constraints);
				constraints.gridy = 2;
				addComponent(this, new PamLabel("Channel"), constraints);

				constraints.fill = GridBagConstraints.VERTICAL;
				constraints.gridx ++;
				for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
					if ((1<<i & channelMap) == 0) continue;
					if (levelMeters[i] == null) {
						levelMeters[i] = new JProgressBar(-60, 0);
						levelMeters[i].setOrientation(SwingConstants.VERTICAL);
					}
					if (enableChannel[i] == null) {
						enableChannel[i] = new PamCheckBox();
//						PamColors.getInstance().registerComponent(enableChannel[i], PamColor.BORDER);
					}

					constraints.gridy = 0;
					constraints.insets = new Insets(0,0,0,0);
					addComponent(this, enableChannel[i], constraints);
					constraints.gridy = 1;
					constraints.insets = new Insets(charHeight,0,charHeight,0);
					addComponent(this, levelMeters[i], constraints);
					constraints.gridy = 2;
					constraints.insets = new Insets(0,0,0,0);
					addComponent(this, new PamLabel(String.format("%d",i)), constraints);
					
					constraints.gridx ++;

					enableChannel[i].addActionListener(new BarEnableListener(i));
					enableChannel[i].setSelected(recorderControl.recorderSettings.getChannelBitmap(0xFFFFFFFF, i));
				}
			}
			void showLevel(int channel, double leveldB) {
				if (levelMeters[channel] != null) {
					levelMeters[channel].setValue((int) leveldB);
				}
			}
			class BarEnableListener implements ActionListener {
				int barId;
				public BarEnableListener(int barId) {
					this.barId = barId;
				}
				public void actionPerformed(ActionEvent e) {
					recorderControl.recorderSettings.setChannelBitmap(barId, enableChannel[barId].isSelected());
//					recorderControl.recorderSettings.channelBitmap = 
//						PamUtils.SetBit(recorderControl.recorderSettings.channelBitmap, barId, 
//								enableChannel[barId].isSelected());
					recorderControl.enableRecording();
				}
				
			}
			class LevelAxis extends RPanel {
				PamAxis levelAxis;
				public LevelAxis() {
					super();
//					PamColors.getInstance().registerComponent(this, PamColor.BORDER);
					levelAxis = new PamAxis(0, 0, 0, 10,-60, 0, true, "dB", "%.0f");
					levelAxis.setInterval(20);
//					levelAxis.s
				}
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Rectangle r = getBounds();
					Graphics2D g2D = (Graphics2D) g;
//					Graphics gb = g2D.create(0, -charHeight, r.width, r.height + 2 * charHeight);
					levelAxis.drawAxis(g2D, r.width, r.height - charHeight*1, r.width, charHeight);
//					g.drawLine(0,0,r.width,r.height);
					
				}
				
			}
		}
	}
	
	class RPanel extends JPanel implements ColorManaged {

		private PamColor defaultColor = PamColor.BORDER;
		
		public PamColor getDefaultColor() {
			return defaultColor;
		}

		public void setDefaultColor(PamColor defaultColor) {
			this.defaultColor = defaultColor;
		}

		@Override
		public PamColor getColorId() {
			return defaultColor;
		}
	}

	class RecorderTriggerPanel extends PamBorderPanel {
		PamBorderPanel innerPanel;
		RecorderTriggerPanel () {
			setBorder(new TitledBorder("Triggered recordings"));
			setLayout(new BorderLayout());
			innerPanel = new PamBorderPanel();
			innerPanel.setLayout(new GridBagLayout());
			add(BorderLayout.NORTH, innerPanel);
			add(BorderLayout.SOUTH, triggerMessage = new PamLabel(" "));
			setVisible(false);
		}
	}
	
	public void setTriggerMessage(String message) {
		triggerMessage.setText(message);
	}
	
	private class TriggerEnableAction implements ActionListener {

		private RecorderTriggerData recorderTriggerData;
		
		private JCheckBox checkBox;
		
		
		/**
		 * @param recorderTriggerData
		 * @param checkBox
		 */
		public TriggerEnableAction(RecorderTriggerData recorderTriggerData,
				JCheckBox checkBox) {
			super();
			this.recorderTriggerData = recorderTriggerData;
			this.checkBox = checkBox;
		}


		@Override
		public void actionPerformed(ActionEvent arg0) {
			// need to check we've got the correct trigger data and that it's not been updated. 
			recorderTriggerData = recorderControl.recorderSettings.findTriggerData(recorderTriggerData.getTriggerName());
			recorderTriggerData.setEnabled(checkBox.isSelected());
		}
		
	}
	
	private class TriggerOptionsAction implements ActionListener {
		private RecorderTriggerData recorderTriggerData;
		private JButton button;
		private TriggerCheckBox checkBox;

		/**
		 * @param recorderTriggerData
		 */
		public TriggerOptionsAction(RecorderTriggerData recorderTriggerData, JButton button, TriggerCheckBox checkBox) {
			super();
			this.recorderTriggerData = recorderTriggerData;
			this.button = button;
			this.checkBox = checkBox;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Window frame = PamController.getInstance().getGuiFrameManager().getFrame(recorderControl.getFrameNumber());
			RecorderTriggerData newData = TriggerOptionsDialog.showDialog(frame, recorderTriggerData);
			if (newData != null) {
				recorderTriggerData = newData.clone();
				recorderControl.recorderSettings.replaceTriggerData(recorderTriggerData);
				checkBox.setTriggerData(recorderTriggerData);
				setHints(recorderTriggerData, checkBox, button);
			}
			
		}
		
	}

	void addRecorderTrigger() {
		recorderTriggerPanel.innerPanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
		RecorderTriggerData rtd;
		TriggerCheckBox cb;
		JButton button;
		for (RecorderTrigger rt:recorderControl.recorderTriggers) {

			rtd = recorderControl.recorderSettings.findTriggerData(rt);

			cb = new TriggerCheckBox(rt.getName(), rtd);
			cb.setSelected(rtd.isEnabled());
			cb.addActionListener(new TriggerEnableAction(rtd, cb));
			c.gridx = 0;
			PamDialog.addComponent(recorderTriggerPanel.innerPanel, cb, c);
			
			button = new PamButton("Options");
			button.addActionListener(new TriggerOptionsAction(rtd, button, cb));
			c.gridx = 1;
			PamDialog.addComponent(recorderTriggerPanel.innerPanel, button, c);
			
			setHints(rtd, cb, button);
			
//			recorderTriggerPanel.add(cb);
			
			c.gridy++;
		}
		recorderTriggerPanel.invalidate();
		recorderTriggerPanel.setVisible(c.gridy > 0);
	}
	
	private class TriggerCheckBox extends PamCheckBox {

		private RecorderTriggerData triggerData;
		/**
		 * @param text
		 */
		public TriggerCheckBox(String text, RecorderTriggerData triggerData) {
			super(text);
			this.triggerData = triggerData;
		}


		/**
		 * @return the triggerData
		 */
		public RecorderTriggerData getTriggerData() {
			return triggerData;
		}


		/**
		 * @param triggerData the triggerData to set
		 */
		public void setTriggerData(RecorderTriggerData triggerData) {
			this.triggerData = triggerData;
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			return triggerData.getSummaryString();
		}

		
	}
	
	private void setHints(RecorderTriggerData rtd, JCheckBox cb,
			JButton button) {
		String txt = rtd.getSummaryString();
		cb.setToolTipText(txt);
//		button.setToolTipText(txt);
	}

	void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
}
