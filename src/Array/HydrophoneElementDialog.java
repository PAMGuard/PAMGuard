package Array;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog for adding / editing the details of individual hydrophone
 * elements in the array.
 * @author Doug Gillespie
 * @see Array.Hydrophone
 * @see Array.PamArray
 *
 */
public class HydrophoneElementDialog extends PamDialog {

	private static HydrophoneElementDialog singleInstance;

	private Hydrophone hydrophone;

	//	JButton okButton, cancelButton;

	static boolean newHydrophone;

	private GeneralPanel generalPanel;

	private CoordinatePanel coordinatePanel;

	//	private SensitivityPanel sensitivityPanel;

	private PamArray currentArray;

	private InterpolationDialogPanel interpolationDialogPanel;

	private HydrophoneElementDialog(Window window) {
		super(window, "Hydrophone Element", false);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		JPanel c = new JPanel();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.setBorder(new EmptyBorder(10,10,10,10));
		c.add(generalPanel = new GeneralPanel());
		c.add(coordinatePanel = new CoordinatePanel());
		//		c.add(sensitivityPanel = new SensitivityPanel());
		interpolationDialogPanel = new InterpolationDialogPanel(this);
		c.add(interpolationDialogPanel.getComponent(window));
		p.add(BorderLayout.CENTER, c);

		//		JPanel s = new JPanel();
		//		s.add(okButton = new JButton("  Ok  "));
		//		getRootPane().setDefaultButton(okButton);
		//		s.add(cancelButton = new JButton("Cancel"));
		//		okButton.addActionListener(this);
		//		cancelButton.addActionListener(this);
		//		p.add(BorderLayout.SOUTH, s);

		setDialogComponent(p);

		pack();
		//		setLocation(310, 190);
		//		this.setModal(true);
		//		this.setResizable(false);
		//		this.setAlwaysOnTop(true);
		setHelpPoint("utilities.hydrophoneArrayManagerHelp.docs.Array_NewHydrophone");
	}

	static public Hydrophone showDialog(Window window, Hydrophone oldhydrophone, 
			boolean isNew, PamArray currentArray) {
		String title;
		if (singleInstance == null) {
			singleInstance = new HydrophoneElementDialog(window);
		}
		singleInstance.hydrophone = oldhydrophone.clone();
		newHydrophone = isNew;
		if (isNew) {
			singleInstance.setTitle("New " + PamController.getInstance().getGlobalMediumManager().getRecieverString(false));
		}
		else {
			singleInstance.setTitle("Edit " + PamController.getInstance().getGlobalMediumManager().getRecieverString(false));
		}
		singleInstance.currentArray = currentArray;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.hydrophone;
	}

	private void setParams() {
		generalPanel.setParams();
		coordinatePanel.setParams();
		//		sensitivityPanel.setParams();
		interpolationDialogPanel.setSelection(currentArray.getHydrophoneInterpolation());
	}

	@Override
	public boolean getParams() {
		if (generalPanel.getParams() == false) return false;
		if (coordinatePanel.getParams() == false) return false;
		//		if (sensitivityPanel.getParams() == false) return false;
		int hi = interpolationDialogPanel.getSelection();
		if (hi >= 0) {
			currentArray.setHydrophoneInterpolation(hi);
		}
		return true;
	}

	class GeneralPanel extends JPanel {

		private JTextField iD, type;
		private JComboBox streamers;
		private JTextField hSens, preampGain;//, bandwidth0, bandwidth1;

		private JLabel recieverIDLabel;
		private JLabel recieverTypeLabel;
		private JLabel recieverSensLabel;
		private JLabel dBSensLabel;

		public GeneralPanel() {


			setBorder(new TitledBorder("General info"));
			GridBagLayout layout;
			setLayout(layout = new GridBagLayout());
			GridBagConstraints constraints = new PamGridBagContraints();
			constraints.anchor = GridBagConstraints.WEST;
			//			constraints.insets = new Insets(2,2,2,2);
			constraints.gridx = 0;
			constraints.gridy = 0;
			addComponent(this, recieverIDLabel = new JLabel("", SwingConstants.RIGHT), constraints);
			constraints.gridx++;
			constraints.fill = GridBagConstraints.NONE;
			addComponent(this, iD = new JTextField(3), constraints);
			iD.setEditable(false);
			constraints.fill = GridBagConstraints.HORIZONTAL;
			//			constraints.anchor = GridBagConstraints.;
			constraints.gridx = 0;
			constraints.gridy++;
			addComponent(this, new JLabel("Streamer ", SwingConstants.RIGHT), constraints);
			constraints.gridx++;
			constraints.gridwidth = 2;
			addComponent(this, streamers = new JComboBox(), constraints);
			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy++;
			addComponent(this, recieverTypeLabel = new JLabel("",  JLabel.RIGHT), constraints);
			constraints.gridx++;
			constraints.gridwidth = 2;
			addComponent(this, type = new JTextField(12), constraints);
			constraints.gridwidth = 1;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.insets = new Insets(2,2,2,2);

			constraints.gridx = 0;
			constraints.gridy++;
			constraints.anchor = GridBagConstraints.EAST;
			addComponent(this, recieverSensLabel = new JLabel("", JLabel.RIGHT), constraints);
			constraints.gridx++;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(this, hSens = new JTextField(5), constraints);
			constraints.gridx++;
			constraints.gridwidth = 2;
			addComponent(this, dBSensLabel  = new JLabel(""), constraints);

			constraints.gridwidth = 1;
			constraints.gridx = 0;
			constraints.gridy++;
			constraints.anchor = GridBagConstraints.EAST;
			addComponent(this, new JLabel("Preamplifier gain ", JLabel.RIGHT), constraints);
			constraints.gridx++;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(this, preampGain = new JTextField(5), constraints);
			constraints.gridx++;
			constraints.gridwidth = 2;
			addComponent(this, new JLabel(" dB"), constraints);

			//			constraints.gridwidth = 1;
			//			constraints.gridx = 0;
			//			constraints.gridy++;
			//			constraints.anchor = GridBagConstraints.EAST;
			//			addComponent(this, new JLabel("Combined bandwidth "), constraints);
			//			constraints.gridx++;
			//			constraints.anchor = GridBagConstraints.WEST;
			//			addComponent(this, bandwidth0 = new JTextField(7), constraints);
			//			constraints.gridx++;
			//			addComponent(this, new JLabel(" to "), constraints);
			//			constraints.gridx++;
			//			addComponent(this, bandwidth1 = new JTextField(7), constraints);
			//			constraints.gridx++;
			//			addComponent(this, new JLabel(" Hz "), constraints);
			
			//set the test values for the recievers and dB references based on the current medium 
			setRecieverLabelText() ;

		}

		void setParams() {
			iD.setText(String.format("%d", hydrophone.getID()));
			type.setText(hydrophone.getType());
			streamers.removeAllItems();
			
			//set thre text values for the recieevrs. 
			setRecieverLabelText();
			if (currentArray != null) {
				Streamer s;
				for (int i = 0; i < currentArray.getNumStreamers(); i++) {
					s = currentArray.getStreamer(i);
					streamers.addItem(String.format("Streamer %d, x=%3.1f", i, s.getX()));
				}
			}
			if (hydrophone.getStreamerId() < currentArray.getNumStreamers()) {
				streamers.setSelectedIndex(hydrophone.getStreamerId());
			}
			hSens.setText(String.format("%.1f", hydrophone.getSensitivity()-PamController.getInstance().getGlobalMediumManager().getdBSensOffset()));
			preampGain.setText(String.format("%.1f", hydrophone.getPreampGain()));
			//			bandwidth0.setText(String.format("%.1f", hydrophone.getBandwidth()[0]));
			//			bandwidth1.setText(String.format("%.1f", hydrophone.getBandwidth()[1]));
		}


		boolean getParams() {
			try {
				hydrophone.setID(Integer.valueOf(iD.getText()));
				hydrophone.setType(type.getText());
			}
			catch (Exception Ex) {
				JOptionPane.showMessageDialog(this, "There is a problem with one of the parameters in the General panel");
				return false;
			}
			hydrophone.setStreamerId(streamers.getSelectedIndex());
			try {
				hydrophone.setSensitivity(Double.valueOf(hSens.getText())+PamController.getInstance().getGlobalMediumManager().getdBSensOffset());
				hydrophone.setPreampGain(Double.valueOf(preampGain.getText()));
				double[] bw = new double[2];
				//				bw[0] = Double.valueOf(bandwidth0.getText());
				//				bw[1] = Double.valueOf(bandwidth1.getText());
				//				hydrophone.setBandwidth(bw);
			}
			catch (Exception Ex) {
				return showWarning("There is a problem with one of the parameters");
			}
			return true;
		}

		/**
		 * Set the receiver labels depending on whether air or water is being used. 
		 */
		private void setRecieverLabelText() {
			String recieverString = PamController.getInstance().getGlobalMediumManager().getRecieverString(); 
			String dbSens = PamController.getInstance().getGlobalMediumManager().getdBSensString(); 

			recieverIDLabel.setText(recieverString+ " ID "); 
			recieverTypeLabel.setText(recieverString + " type "); 
			recieverSensLabel.setText(recieverString + " sensitivity ");
			dBSensLabel.setText(dbSens); 
		}

	}



	class CoordinatePanel extends JPanel {
		JTextField x, y, depth, dx, dy, dz;
		private JLabel depthLabel;
		private JLabel depthLabel2;
		static public final int textLength = 6;
		static public final int errorTextLength = 6;
		public CoordinatePanel() {
			setBorder(new TitledBorder("Coordinates (m)"));
			GridBagLayout layout;
			setLayout(layout = new GridBagLayout());
			GridBagConstraints constraints = new PamGridBagContraints();
			constraints.fill = GridBagConstraints.NONE;
			//			addComponent(this, new JLabel("Coordinate "), constraints);
			//			constraints.gridx++;
			//			constraints.gridwidth = 4;
			//			addComponent(this, new JLabel("x - port -ve, starboard +ve "), constraints);
			//			constraints.gridx = 0;
			//			constraints.gridwidth = 1;
			//			constraints.gridy++;
			//			addComponent(this, new JLabel("System: "), constraints);
			//			constraints.gridx++;
			//			constraints.gridwidth = 4;
			//			addComponent(this, new JLabel("y - distance ahead of the vessel "), constraints);
			//			constraints.gridx = 0;
			//			constraints.gridwidth = 1;
			//			constraints.gridy++;
			////			addComponent(this, new JLabel("Coordinate System: "), constraints);
			//			constraints.gridx++;
			//			constraints.gridwidth = 4;
			//			addComponent(this, new JLabel("depth is positive "), constraints);
			//			constraints.gridx = 0;
			//			constraints.gridwidth = 1;
			//			constraints.gridy++;

			constraints.anchor = GridBagConstraints.WEST;
			//			constraints.insets = new Insets(2,2,2,2);
			constraints.anchor = GridBagConstraints.EAST;
			addComponent(this, new JLabel("x ", SwingConstants.RIGHT), constraints);
			constraints.gridx++;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(this, x = new JTextField(textLength), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel(" +/- "), constraints);
			constraints.gridx++;
			addComponent(this, dx = new JTextField(errorTextLength), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel(" m (right of streamer)"), constraints);

			constraints.gridx = 0;
			constraints.gridy ++;
			constraints.anchor = GridBagConstraints.EAST;
			addComponent(this, new JLabel("y ", SwingConstants.RIGHT), constraints);
			constraints.gridx++;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(this, y = new JTextField(textLength), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel(" +/- "), constraints);
			constraints.gridx++;
			addComponent(this, dy = new JTextField(errorTextLength), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel(" m (ahead of streamer)"), constraints);

			constraints.gridx = 0;
			constraints.gridy ++;
			constraints.anchor = GridBagConstraints.EAST;
			addComponent(this, depthLabel = new JLabel("", SwingConstants.RIGHT), constraints);
			constraints.gridx++;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(this, depth = new JTextField(textLength), constraints);
			constraints.gridx++;
			addComponent(this, new JLabel(" +/- "), constraints);
			constraints.gridx++;
			addComponent(this, dz = new JTextField(errorTextLength), constraints);
			constraints.gridx++;
			addComponent(this, depthLabel2 = new JLabel(""), constraints);
			
			setRecieverLabelText(); 

		}
		
		

		/**
		 * Set the receiver labels depending on whether air or water is being used. 
		 */
		private void setRecieverLabelText() {
			String recieverDepthString = PamController.getInstance().getGlobalMediumManager().getZString(); 

			depthLabel.setText(recieverDepthString + " "); 
			
			switch (PamController.getInstance().getGlobalMediumManager().getCurrentMedium()) {
			case Air:
				depthLabel2.setText(" m (height above streamer)"); 
				break;
			case Water:
				depthLabel2.setText(" m (depth below streamer)"); 
				break;
			}
		
		}

		void setParams() {
			
			double zCoeff = PamController.getInstance().getGlobalMediumManager().getZCoeff(); 
			setRecieverLabelText(); 

			if (newHydrophone == false) {
				x.setText(formatDouble(hydrophone.getX()));
				y.setText(formatDouble(hydrophone.getY()));
				depth.setText(formatDouble(zCoeff*hydrophone.getZ()));
				dx.setText(formatDouble(hydrophone.getdX()));
				dy.setText(formatDouble(hydrophone.getdY()));
				dz.setText(formatDouble(hydrophone.getdZ()));
			}
			else {
				x.setText(null);
				y.setText(null);
				depth.setText(null);	
				dx.setText(null);
				dy.setText(null);
				dz.setText(null);		
			}
		}
		
		
		boolean getParams() {
			
			double zCoeff = PamController.getInstance().getGlobalMediumManager().getZCoeff(); 

			try {
				hydrophone.setX(Double.valueOf(x.getText()));
				hydrophone.setY(Double.valueOf(y.getText()));
				hydrophone.setZ(zCoeff*Double.valueOf(depth.getText()));
				hydrophone.setdX(Double.valueOf(dx.getText()));
				hydrophone.setdY(Double.valueOf(dy.getText()));
				hydrophone.setdZ(Double.valueOf(dz.getText()));
			}
			catch (Exception Ex) {
				return showWarning("There is a problem with one of the parameters in the Coordinates panel");
			}
			return true;
		}
	}

	class SensitivityPanel extends JPanel {


		public SensitivityPanel() {
			setBorder(new TitledBorder("Sensitivity"));
			GridBagLayout layout;
			setLayout(layout = new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			//			constraints.anchor = GridBagConstraints.WEST;
			//			constraints.insets = new Insets(2,2,2,2);
			//			constraints.gridx = 0;
			//			constraints.gridy = 0;
			//
			//			constraints.gridx = 0;
			//			constraints.gridy = 0;
			//			constraints.anchor = GridBagConstraints.EAST;
			//			addComponent(this, new JLabel("Hydrophone sensitivity "), constraints);
			//			constraints.gridx++;
			//			constraints.anchor = GridBagConstraints.WEST;
			//			addComponent(this, hSens = new JTextField(5), constraints);
			//			constraints.gridx++;
			//			constraints.gridwidth = 2;
			//			addComponent(this, new JLabel(" dB re 1V/uPa "), constraints);
			//
			//			constraints.gridwidth = 1;
			//			constraints.gridx = 0;
			//			constraints.gridy = 1;
			//			constraints.anchor = GridBagConstraints.EAST;
			//			addComponent(this, new JLabel("Preamplifier gain "), constraints);
			//			constraints.gridx++;
			//			constraints.anchor = GridBagConstraints.WEST;
			//			addComponent(this, preampGain = new JTextField(5), constraints);
			//			constraints.gridx++;
			//			constraints.gridwidth = 2;
			//			addComponent(this, new JLabel(" dB"), constraints);
			//
			//			constraints.gridwidth = 1;
			//			constraints.gridx = 0;
			//			constraints.gridy = 2;
			//			constraints.anchor = GridBagConstraints.EAST;
			//			addComponent(this, new JLabel("Combined bandwidth "), constraints);
			//			constraints.gridx++;
			//			constraints.anchor = GridBagConstraints.WEST;
			//			addComponent(this, bandwidth0 = new JTextField(7), constraints);
			//			constraints.gridx++;
			//			addComponent(this, new JLabel(" to "), constraints);
			//			constraints.gridx++;
			//			addComponent(this, bandwidth1 = new JTextField(7), constraints);
			//			constraints.gridx++;
			//			addComponent(this, new JLabel(" Hz "), constraints);
			//			
		}

		void setParams() {
		}
		boolean getParams() {

			return true;
		}
	}


	//	/* (non-Javadoc)
	//	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	//	 */
	//	public void actionPerformed(ActionEvent e) {
	//		if (e.getSource() == cancelButton) {
	//			hydrophone = null;
	//			this.setVisible(false);
	//		}
	//		else if (e.getSource() == okButton) {
	//			if (getParams()) {
	//				this.setVisible(false);
	//			}
	//		}
	//	}

	@Override
	public void cancelButtonPressed() {
		hydrophone = null;

	}


	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
