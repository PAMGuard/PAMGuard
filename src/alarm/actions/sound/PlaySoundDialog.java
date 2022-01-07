package alarm.actions.sound;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import alarm.AlarmParameters;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.dialog.PamDialog;

public class PlaySoundDialog extends PamDialog {

	private static PlaySoundDialog singleInstance;
	private PlaySoundParams playSoundParams;
	private PlaySound playSound;
	private LevelPanel[] levelPanels = new LevelPanel[AlarmParameters.COUNT_LEVELS];

	private PlaySoundDialog(Window parentFrame, PlaySound playSound) {
		super(parentFrame, "Select ALarm Sound Files", false);
		this.playSound = playSound;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
			levelPanels[i] = new LevelPanel(i);
			mainPanel.add(levelPanels[i]);
		}
		
		setDialogComponent(mainPanel);
//		setResizable(true);
	}

	public static PlaySoundParams showDialog(Window parentFrame, PlaySound playSound) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner() || playSound != singleInstance.playSound) {
			singleInstance = new PlaySoundDialog(parentFrame, playSound);
		}
		singleInstance.playSoundParams = playSound.playSoundParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.playSoundParams;
	}

	private void setParams() {
		for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
			levelPanels[i].setParams();
		}
	}

	@Override
	public boolean getParams() {
		boolean ok = true;
		for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
			ok &= levelPanels[i].getParams();
		}
		// no real need to have both files there. People may only want to 
		// define one. 
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		playSoundParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class LevelPanel extends JPanel {
		private int alarmLevel;
		private JTextField soundFile;
		private JButton browseButton;
		private JButton testButton;
		public LevelPanel(int level) {
			this.alarmLevel = level;
			setLayout(new BorderLayout());
			setBorder(new TitledBorder(AlarmParameters.levelNames[level] + " Sound File"));
			add(BorderLayout.NORTH, soundFile = new JTextField(60));
			JPanel sPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			add(BorderLayout.SOUTH, sPanel);
			sPanel.add(browseButton = new JButton("Browse..."));
			sPanel.add(testButton = new JButton("Test"));
			browseButton.addActionListener(new BrowseButton(level));
			testButton.addActionListener(new TestButton(level));
		}
		
		protected void setParams() {
			if (playSoundParams.soundFile[alarmLevel] != null) {
				soundFile.setText(playSoundParams.soundFile[alarmLevel].getAbsolutePath());
			}
			else {
				soundFile.setText(null);
			}
		}

		public boolean getParams() {
			String str = soundFile.getText();
			if (str == null || str.length() == 0) {
				return showWarning("No sound file selected");
			}
			playSoundParams.soundFile[alarmLevel] = new File(str);
			if (playSoundParams.soundFile[alarmLevel].exists() == false) {
				return showWarning(str + " does not exist on the file system");
			}
			return true;
		}
	}
	
	private class BrowseButton implements ActionListener {
		private int alarmLevel;

		public BrowseButton(int level) {
			this.alarmLevel = level;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			browseButtonPressed(alarmLevel);
		}
	}

	private class TestButton implements ActionListener {
		int alarmLevel;
		public TestButton(int level) {
			this.alarmLevel = level;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			testButtonPressed(alarmLevel);
		}
	}

	private void browseButtonPressed(int alarmLevel) {
		PamFileFilter fileFilter = new PamFileFilter("Sound Files", ".wav");
		fileFilter.addFileType(".WAV");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		JTextField soundFile = levelPanels[alarmLevel].soundFile;
		String str = soundFile.getText();
		if (str != null) {
			fileChooser.setSelectedFile(new File(str));
		}
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showOpenDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			//System.out.println(currFile);
			soundFile.setText(currFile.getAbsolutePath());
		}
	}

	public void testButtonPressed(int alarmLevel) {
		if (levelPanels[alarmLevel].getParams()) {
			boolean ok = playSound.playSound(playSoundParams.soundFile[alarmLevel]);
			if (!ok) {
				showWarning("Sound playback failed");
			}
		}
	}

}
