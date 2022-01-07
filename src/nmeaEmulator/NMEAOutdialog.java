package nmeaEmulator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;

public class NMEAOutdialog extends PamDialog {

	private static NMEAOutdialog singleInstance;
	
	private NMEAFrontEnd nmeaFrontEnd;
	
	private InfoPanel infoPanel;
	
	private JButton startButton, stopButton, closeButton;
	
	private NMEAOutdialog(Window parentFrame, NMEAFrontEnd nmeaFrontEnd) {
		super(parentFrame, "NMEA Output", false);
		this.nmeaFrontEnd = nmeaFrontEnd;
		infoPanel = new InfoPanel(nmeaFrontEnd);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, infoPanel.getPanel());

		getButtonPanel().setVisible(false);

		JPanel buttonPanel = new JPanel();

//		JPanel bp2 = new JPanel(new BorderLayout());
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		startButton = new JButton("Start");
		stopButton = new JButton("Stop");
		closeButton = new JButton("Close");
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(closeButton);
		startButton.addActionListener(new StartButton());
		stopButton.addActionListener(new StopButton());
		closeButton.addActionListener(new CloseButton());

		mainPanel.add(BorderLayout.SOUTH, buttonPanel);
		setDialogComponent(mainPanel);
	}
	
	public static void showDialog(Frame parentFrame, NMEAFrontEnd nmeaFrontEnd) {
		if (singleInstance == null || singleInstance.nmeaFrontEnd != nmeaFrontEnd || singleInstance.getOwner() != parentFrame) {
			singleInstance = new NMEAOutdialog(parentFrame, nmeaFrontEnd);
		}
		
		singleInstance.infoPanel.fillPanel();
		singleInstance.setVisible(true);
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private class StartButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			start();
		}
	}
	
	private void start() {
		nmeaFrontEnd.startSim(this);
	}
	
	private void stop() {
		nmeaFrontEnd.stopSim();
	}
	
	private class StopButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			stop();
		}
	}
	
	private class CloseButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			setVisible(false);
		}
	}

	public void setProgress(EmulationProgress emulationProgress) {
		infoPanel.setProgress(emulationProgress);
	}

	public boolean isRepeat() {
		return infoPanel.isRepeat();
	}

}
