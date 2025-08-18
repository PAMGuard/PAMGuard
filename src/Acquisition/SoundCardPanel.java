package Acquisition;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.DialogComponent;
import PamView.panel.WestAlignedPanel;

public class SoundCardPanel implements DialogComponent {

	private SoundCardSystem soundCardSystem;

		
		private JPanel mainPanel;
		
		private JComboBox<String> deviceNames;
		
		private JRadioButton bitButtons[];

	public SoundCardPanel(SoundCardSystem soundCardSystem) {
		this.soundCardSystem = soundCardSystem;
		mainPanel = new JPanel(new BorderLayout());
		deviceNames = new JComboBox<>();
		mainPanel.add(BorderLayout.CENTER, deviceNames);
		mainPanel.setBorder(new TitledBorder("Select audio line"));
		JPanel bitPanel = new JPanel(new FlowLayout());
		bitPanel.add(new JLabel("Bit depth: "));
		bitButtons = new JRadioButton[SoundCardParameters.BITDEPTHS.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < SoundCardParameters.BITDEPTHS.length; i++) {
			bitButtons[i] = new JRadioButton(String.format("%d bit", SoundCardParameters.BITDEPTHS[i]));
			bg.add(bitButtons[i]);
			bitPanel.add(bitButtons[i]);
		}
		// 24 bit not supported by Java sound,so this is a waste of time. 
//		mainPanel.add(BorderLayout.SOUTH, new WestAlignedPanel(bitPanel));
		setParams();
	}
	@Override
	public JComponent getComponent(Window owner) {
		return mainPanel;
	}

	@Override
	public void setParams() {
		deviceNames.removeAllItems();
		ArrayList<String> currNames = SoundCardSystem.getDevicesList();
		for (int i = 0; i < currNames.size(); i++) {
			deviceNames.addItem(currNames.get(i).toString());
		}
		if (soundCardSystem.soundCardParameters.deviceNumber < currNames.size()) {
			deviceNames.setSelectedIndex(soundCardSystem.soundCardParameters.deviceNumber);
		}
		for (int i = 0; i < SoundCardParameters.BITDEPTHS.length; i++) {
			bitButtons[i].setSelected(soundCardSystem.soundCardParameters.getBitDepth() == SoundCardParameters.BITDEPTHS[i]);
		}
		
	}

	@Override
	public boolean getParams() {
		soundCardSystem.soundCardParameters.deviceNumber = deviceNames.getSelectedIndex();
		for (int i = 0; i < SoundCardParameters.BITDEPTHS.length; i++) {
			if (bitButtons[i].isSelected()) {
				soundCardSystem.soundCardParameters.setBitDepth(SoundCardParameters.BITDEPTHS[i]);
				break;
			}
		}
		return soundCardSystem.soundCardParameters.deviceNumber >= 0;
	}
}
