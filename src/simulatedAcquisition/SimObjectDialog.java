package simulatedAcquisition;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import simulatedAcquisition.movement.MovementModel;
import simulatedAcquisition.sounds.SimSignal;
import simulatedAcquisition.sounds.SimSignals;
import PamModel.SMRUEnable;
import PamUtils.LatLongDialogStrip;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class SimObjectDialog extends PamDialog {

	private static SimObjectDialog singleInstance;

	private SimObject simObject;
	private SimProcess simProcess;

	private JTextField name;
	private LatLongDialogStrip latDialogStrip, longDialogStrip;
	private JTextField cog, spd, depth, amplitude, slant;
	private JComboBox soundTypes;
	private JTextField meanInterval;
	private JCheckBox randomIntervals;
	private JCheckBox pistonModel;
	private JTextField pistonRadius;
	private JComboBox<String> movementModel;
	private JButton advancedButton;

	private JCheckBox enableEchoes;

	private JTextField echoTextField;

	private JCheckBox seperateEchoes;

	private SimObjectDialog(Window parentFrame, SimProcess simProcess) {
		super(parentFrame, "Simulated Object", false);
		this.simProcess = simProcess;

		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Object parameters"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(p, new JLabel("Name ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 5;
		addComponent(p, name = new JTextField(10), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Sound Type ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 5;
		addComponent(p, soundTypes = new JComboBox(), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Source Level", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, amplitude = new JTextField(5), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(p, new JLabel("<html>dB re.1&mu;Pa p-p</html>", JLabel.LEFT), c);
		if (SMRUEnable.isEnable()) {
			c.gridy++;
			c.gridx= 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("Beam Pattern ", JLabel.RIGHT), c);
			c.gridx++;
			c.gridwidth = 3;
			addComponent(p, pistonModel = new JCheckBox("Use Piston Model radius"), c);
			c.gridx += c.gridwidth;
			c.gridwidth = 1;
			addComponent(p, pistonRadius = new JTextField(3), c);
			c.gridx++;
			addComponent(p, new JLabel(" cm", JLabel.LEFT), c);
		}
		
		c.gridy++;
		c.gridx= 0;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Mean Interval ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, meanInterval = new JTextField(5), c);
		c.gridx++;
		addComponent(p, new JLabel(" s ", JLabel.LEFT), c);
		c.gridx++;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		addComponent(p, randomIntervals = new JCheckBox("Randomise"), c);

		c.gridy++; 
		c.gridx=0; 
		c.gridwidth=1; 
		addComponent(p, new JLabel("Echos", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, enableEchoes=new JCheckBox("Add Echoes"), c);
		enableEchoes.addActionListener((action)->{
			enableControls(); 
		});
		enableEchoes.setToolTipText("Add some echoes to the simulated data");
		c.gridx++; 
		addComponent(p, new JLabel("Delay", JLabel.RIGHT), c); 
		c.gridx++; 
		addComponent(p, echoTextField = new JTextField(5), c); 
		c.gridx++; 
		addComponent(p, new JLabel("ms", JLabel.LEFT), c); 
		c.gridx++; 
		addComponent(p, seperateEchoes=new JCheckBox("Seperate"), c); 
		seperateEchoes.setToolTipText("Click to have seperate detections which are echoes. "
				+ "If unticked then echoes are included in the click detection as part of the waveform");


		c.gridx = 0;
		c.gridwidth = 6;
		c.gridy++;
		latDialogStrip = new LatLongDialogStrip(true);
		addComponent(p, latDialogStrip, c);
		c.gridy++;
		longDialogStrip = new LatLongDialogStrip(false);
		addComponent(p, longDialogStrip, c);
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Depth ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, depth = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" m  "), c);
		c.gridx++;
		addComponent(p, new JLabel(" Speed ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, spd = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" m/sec "), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Course ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, cog = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" deg'  "), c);
		c.gridx++;
		addComponent(p, new JLabel(" Slant ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(p, slant = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" deg'"), c);
		if (SMRUEnable.isEnable()) {
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("Movement Model ", JLabel.RIGHT), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(p, movementModel = new JComboBox<String>(), c);
			c.gridx += c.gridwidth;
			c.gridwidth = 2;
			addComponent(p, advancedButton = new JButton("Options..."), c);
		}

		// fill the types comboBox
		SimSignals simSignals = simProcess.simSignals;
		int n = simSignals.getNumSignals();
		for (int i = 0; i < n; i++) {
			soundTypes.addItem(simSignals.getSignal(i));
		}
		if (pistonModel != null) {
			pistonModel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					enableControls();
				}
			});
		}
		if (movementModel != null) {
			movementModel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					enableControls();
				}
			});
		
		advancedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				advancedButton();
			}
		});
		}

		setDialogComponent(p);
		setModal(true);

	}


	protected void advancedButton() {
		MovementModel mM = simObject.getMovementModels().getModel(movementModel.getSelectedIndex());
		if (mM.hasOptions()) {
			mM.showOptions(this, simObject);
		}
	}


	protected void enableControls() {
		boolean adv = false;
		if (SMRUEnable.isEnable()) {
			pistonRadius.setEnabled(pistonModel.isSelected());
			adv = movementModel.getSelectedIndex() > 0;
		}
	
		echoTextField.setEnabled(enableEchoes.isSelected());
		seperateEchoes.setEnabled(enableEchoes.isSelected());

		latDialogStrip.setEnabled(adv == false);
		longDialogStrip.setEnabled(adv == false);
		depth.setEnabled(adv == false);
		spd.setEnabled(adv == false);
		cog.setEnabled(adv == false);
		slant.setEnabled(adv == false);
		if (advancedButton != null) {
			advancedButton.setEnabled(adv == true);
		}
	}


	public static SimObject showDialog(Window parentFrame, SimProcess simProcess, SimObject simObject) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame ||
				singleInstance.simProcess != simProcess) {
			singleInstance = new SimObjectDialog(parentFrame,simProcess);
		}
		singleInstance.simObject = simObject;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.simObject;
	}

	@Override
	public void cancelButtonPressed() {
		simObject = null;
	}

	private void setParams() {

		// fill movement combobox
		if (movementModel != null) {
			movementModel.removeAllItems();
			for (int i = 0; i < simObject.getMovementModels().getNumModels(); i++) {
				movementModel.addItem(simObject.getMovementModels().getModel(i).getName());
			}
		}

		name.setText(simObject.name);
		latDialogStrip.sayValue(simObject.startPosition.getLatitude());
		longDialogStrip.sayValue(simObject.startPosition.getLongitude());
		amplitude.setText(String.format("%3.1f", simObject.amplitude));
		meanInterval.setText(String.format("%3.1f", simObject.meanInterval));
		randomIntervals.setSelected(simObject.randomIntervals);
		//echoes
		echoTextField.setText(String.format("%3.3f", simObject.echoDelay));
		enableEchoes.setSelected(simObject.echo);
		seperateEchoes.setSelected(simObject.seperateEcho);

		depth.setText(String.format("%3.1f", -simObject.getHeight()));
		spd.setText(String.format("%3.1f", simObject.speed));
		cog.setText(String.format("%3.1f", simObject.course));
		slant.setText(String.format("%3.1f", simObject.slantAngle));
		soundTypes.setSelectedItem(simProcess.simSignals.findSignal(simObject.signalName));
		if (SMRUEnable.isEnable()) {
			pistonModel.setSelected(simObject.pistonBeam);
			pistonRadius.setText(String.format("%3.1f", simObject.pistonRadius*100));
			movementModel.setSelectedIndex(simObject.movementModel);
		}
		enableControls();
	}

	@Override
	public boolean getParams() {
		simObject.name = name.getText();
		simObject.startPosition.setLatitude(latDialogStrip.getValue());
		simObject.startPosition.setLongitude(longDialogStrip.getValue());
		simObject.randomIntervals = randomIntervals.isSelected();
		try {
			simObject.amplitude = Double.valueOf(amplitude.getText());
			simObject.meanInterval = Double.valueOf(meanInterval.getText());
			simObject.setHeight(-Double.valueOf(depth.getText()));
			simObject.speed = Double.valueOf(spd.getText());
			simObject.course = Double.valueOf(cog.getText());
			simObject.slantAngle = Double.valueOf(slant.getText());
			//echoes
			simObject.echo=enableEchoes.isSelected(); 
			simObject.echoDelay=Double.valueOf(this.echoTextField.getText());
			simObject.seperateEcho=seperateEchoes.isSelected();
		}
		catch (NumberFormatException e) {
			return false;
		}
		if (simObject.meanInterval <= 0) {
			JOptionPane.showMessageDialog(this, "Mean interval must be > 0");
			return false;
		}
		SimSignal simSignal = (SimSignal) soundTypes.getSelectedItem();
		if (simSignal == null) {
			return false;
		}
		simObject.signalName = simSignal.getName();
		if (SMRUEnable.isEnable()) {
			simObject.pistonBeam = pistonModel.isSelected();
			if (simObject.pistonBeam) {
				try {
					simObject.pistonRadius = Double.valueOf(pistonRadius.getText()) / 100;
				}
				catch (NumberFormatException e) {
					return showWarning("Invalid piston radius");
				}
			}
			simObject.movementModel = movementModel.getSelectedIndex();
		}
		else {
			simObject.pistonBeam = false;
			simObject.movementModel = 0;
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
