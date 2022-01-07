package SoundRecorder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.dialog.PamButton;
import PamView.dialog.PamLabel;
import PamView.dialog.ScrollingPamLabel;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class RecorderSidePanel implements PamSidePanel , RecorderView {
	
	RecorderControl recorderControl;
	
	SidePanel sidePanel;
	
	JLabel recordStatus;
	
	PamButton buttonOff, buttonAuto, buttonStart, buttonStartBuffered;
	
	TitledBorder titledBorder;
	
	RecorderSidePanel(RecorderControl recorderControl) {
		
		this.recorderControl = recorderControl;
		
		sidePanel = new SidePanel();
		
	}
	
	public JComponent getPanel() {
		return sidePanel;
	}

	class SidePanel extends PamBorderPanel {
		SidePanel() {
			super();
			setBorder(titledBorder = new TitledBorder(recorderControl.getUnitName()));
			GridBagLayout gb = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			setLayout(gb);
			recordStatus = new ScrollingPamLabel(20);
			recordStatus.setBorder(PamBorder.createInnerBorder());
//			recordStatus.setEditable(false);
			buttonOff = new RecorderButton("Off");
			buttonAuto = new RecorderButton("Auto");
			buttonStart = new RecorderButton("Continuous");
			buttonStartBuffered = new RecorderButton("Cont'+Buffer");
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(this, recordStatus, c);
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 1;
			addComponent(this, buttonOff, c);
			c.gridy = 2;
			addComponent(this, buttonAuto, c);
			c.gridx = 1;
			c.gridy = 1;
			addComponent(this, buttonStart, c);
			c.gridx = 1;
			c.gridy = 2;
			addComponent(this, buttonStartBuffered, c);
			buttonOff.addActionListener(new MainButtonListener(BUTTON_OFF));
			buttonAuto.addActionListener(new MainButtonListener(BUTTON_AUTO));
			buttonStart.addActionListener(new MainButtonListener(BUTTON_START));
			buttonStartBuffered.addActionListener(new MainButtonListener(BUTTON_START_BUFFERED));
			
			addMouseListener(new SideMouse());
		}
		class SideMouse extends MouseAdapter {

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					// jump to the main tab for this unit.
					recorderControl.gotoTab();
				}
			}
			
		}
	}

	public void newData(PamDataBlock dataBlock, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		
	}

	public void enableRecording(boolean enable) {
		buttonAuto.setEnabled(enable);
		buttonStart.setEnabled(enable);
		buttonStartBuffered.setEnabled(enable);
	}
	
	public void enableRecordingControl(boolean enable) {
	}
		
	public void newParams() {
		// TODO Auto-generated method stub
		
	}

	public void setButtonStates(int pressedButton) {
			buttonOff.setSelected(pressedButton == BUTTON_OFF);
			buttonAuto.setSelected(pressedButton == BUTTON_AUTO);
			buttonStart.setSelected(pressedButton == BUTTON_START);
			buttonStartBuffered.setSelected(pressedButton == BUTTON_START_BUFFERED);
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
	

	public void sayStatus(String status) {
		String str = "File Error";
		switch (recorderControl.getRecorderStatus()) {
		case RecorderControl.IDLE:
			str = "Recorder idle";
			break;
		case RecorderControl.RECORDING:
			String fileName = recorderControl.recorderStorage.getFileName();
			File f = null;
			if (fileName != null) {
				f = new File(fileName);
			}
			if (f != null) {
				str = f.getName();
			}
			else {
				str = "File closed";
			}
			break;
		}
		recordStatus.setText(str);
	}

	public void rename(String newName) {
		titledBorder.setTitle(newName);	
		sidePanel.repaint();
	}
}
