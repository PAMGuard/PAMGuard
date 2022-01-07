package PamUtils;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;

public class LatLongDialog extends PamDialog implements ActionListener {

	private static LatLong latLong;
	
	static LatLongDialog singleInstance;
	
	private JRadioButton decimalMinutes;
	private JRadioButton minutesSeconds;
	private JRadioButton decimal;

	LatLongDialogStrip latStrip, longStrip;

	

	private LatLongDialog(Window parentFrame, String title) {
		super(parentFrame, title, false);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setBorder(new TitledBorder("Unit types"));
		//top.add(new JLabel("Unit type :")); //not needed with titled border
		top.add(decimalMinutes = new JRadioButton("Degrees, Decimal minutes"));
		top.add(minutesSeconds = new JRadioButton("Degrees, Minutes, Seconds"));
		top.add(decimal = new JRadioButton("Decimal"));

		ButtonGroup bg = new ButtonGroup();
		bg.add(decimalMinutes);
		bg.add(minutesSeconds);
		bg.add(decimal);
		decimalMinutes.addActionListener(this);
		minutesSeconds.addActionListener(this);
		decimal.addActionListener(this);
		panel.add(BorderLayout.NORTH, top);
		
		JPanel cent = new JPanel();
		cent.setLayout(new GridLayout(2,1));
		cent.add(latStrip = new LatLongDialogStrip(true));
		cent.add(longStrip = new LatLongDialogStrip(false));
		panel.add(BorderLayout.CENTER, cent);
		
		setDialogComponent(panel);
		
	}
	
	public static LatLong showDialog(Window parentFrame, LatLong latLong, String title) {
		
		if (latLong == null){
			latLong = new LatLong(57.33, -10.8);
		}
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new LatLongDialog(parentFrame, title);
		}
		LatLongDialog.latLong = latLong;
		singleInstance.setTitle(title);
		singleInstance.showLatLong();
		singleInstance.setVisible(true);
		
		return LatLongDialog.latLong;
	}
	
	
	public static LatLong showDialog(Frame parentFrame, LatLong latLong) {
		return showDialog(parentFrame, latLong, "Enter Lat Long");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == decimalMinutes) {
			LatLong.setFormatStyle(LatLong.FORMAT_DECIMALMINUTES);
//			if (latStrip != null) {
//				latStrip.setDecimalMinutes(true);
//				longStrip.setDecimalMinutes(true);
//			}
		}
		else if (e.getSource() == minutesSeconds) {
			LatLong.setFormatStyle(LatLong.FORMAT_MINUTESSECONDS);
//			if (latStrip != null) {
//				latStrip.setDecimalMinutes(false);
//				longStrip.setDecimalMinutes(false);
////				latStrip.showControls();
////				longStrip.showControls();
//			}
		}
		else if (e.getSource() == decimal) {
			LatLong.setFormatStyle(LatLong.FORMAT_DECIMAL);
		}
		latStrip.showControls();
		longStrip.showControls();
		
	}
	

	private void showLatLong() {
		decimalMinutes.setSelected(LatLong.getFormatStyle() == LatLong.FORMAT_DECIMALMINUTES);
		minutesSeconds.setSelected(LatLong.getFormatStyle() == LatLong.FORMAT_MINUTESSECONDS);
		decimal.setSelected(LatLong.getFormatStyle() == LatLong.FORMAT_DECIMAL);
		latStrip.showControls();
		longStrip.showControls();
		latStrip.sayValue(latLong.getLatitude(), false);
		longStrip.sayValue(latLong.getLongitude(), false);
	}
	
//	class LatLongStrip extends JPanel implements KeyListener, ActionListener{
//		
//		JLabel formattedText;
//		JTextField degrees, minutes, seconds, decminutes;
//		JLabel dl, ml, sl, dml;
//		JComboBox nsew;
//		boolean isLatitude;
//		LatLongStrip(boolean latitude) {
//			
//			isLatitude = latitude;
//			String borderTitle;
//			if (isLatitude) borderTitle = "Latitude";
//			else borderTitle = "Longitude";
//			this.setBorder(new TitledBorder(borderTitle));
//			
//			degrees = new JTextField(4);
//			minutes = new JTextField(3);
//			seconds = new JTextField(6);
//			decminutes = new JTextField(8);
//			nsew = new JComboBox();
//			dl = new JLabel("deg.");
//			ml = new JLabel("min.");
//			sl = new JLabel("sec.");
//			dml = new JLabel("dec min.");
//			formattedText = new JLabel("Position");
//			if (isLatitude) {
//				nsew.addItem("N");
//				nsew.addItem("S");
//			}
//			else{
//				nsew.addItem("E");
//				nsew.addItem("W");
//			}
//			setLayout(new BorderLayout());
//			
//			JPanel mp = new JPanel();
//			mp.setLayout(new BoxLayout(mp, BoxLayout.X_AXIS));
//			mp.setLayout(new FlowLayout(FlowLayout.LEFT));
//			mp.add(dl);
//			mp.add(degrees);
//			mp.add(ml);
//			mp.add(minutes);
//			mp.add(sl);
//			mp.add(seconds);
//			mp.add(dml);
//			mp.add(decminutes);
//			mp.add(nsew);
//			
//			degrees.addKeyListener(this);
//			minutes.addKeyListener(this);
//			seconds.addKeyListener(this);
//			decminutes.addKeyListener(this);
//			nsew.addActionListener(this);
//			
//			this.add(BorderLayout.CENTER, mp);
//			this.add(BorderLayout.SOUTH, formattedText);
//			
//			showControls();
//		}
//		/* (non-Javadoc)
//		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//		 */
//		public void actionPerformed(ActionEvent e) {
//			
//			newTypedValues(null);
//			
//		}
//		/* (non-Javadoc)
//		 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
//		 */
//		public void keyPressed(KeyEvent e) {
//			
//			newTypedValues(e);
//			
//		}
//		/* (non-Javadoc)
//		 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
//		 */
//		public void keyReleased(KeyEvent e) {
//			
//			newTypedValues(e);
//			
//		}
//		/* (non-Javadoc)
//		 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
//		 */
//		public void keyTyped(KeyEvent e) {
//			
//			newTypedValues(e);
//			
//		}
//		private void newTypedValues(KeyEvent e) {
//			double v = getValue();
//			// now need to put that into the fields that
//			// are not currently shown so that they are
//			// ready if needed. 
//			if (e != null) {
//				sayValue(v, true);
//			}
//			// and say the formated version
//			sayFormattedValue(v);
//		}
//		public void showControls() {
//			boolean decimal = decimalMinutes.isSelected();
//			minutes.setVisible(decimal == false);
//			ml.setVisible(decimal == false);
//			seconds.setVisible(decimal == false);
//			sl.setVisible(decimal == false);
//			decminutes.setVisible(decimal);
//			dml.setVisible(decimal);
//			sayFormattedValue(getValue());
//		}
//		public void sayValue(double value, boolean hiddenOnly) {
//			if (value >= 0) {
//				nsew.setSelectedIndex(0);
//			}
//			else {
//				nsew.setSelectedIndex(1);
//			}
//			double deg = LatLong.getSignedDegrees(value);
//			if (degrees.isVisible() == false || !hiddenOnly) degrees.setText(String.format("%d", (int)Math.abs(deg)));
//			if (minutes.isVisible() == false || !hiddenOnly) minutes.setText(String.format("%d", LatLong.getIntegerMinutes(value)));
//			if (decminutes.isVisible() == false || !hiddenOnly) decminutes.setText(String.format("%3.5f", LatLong.getDecimalMinutes(value)));
//			if (seconds.isVisible() == false || !hiddenOnly) seconds.setText(String.format("%3.5f", LatLong.getSeconds(value)));
//			if (nsew.isVisible() == false || !hiddenOnly) nsew.setSelectedIndex(deg >= 0 ? 0 : 1);
//			sayFormattedValue(value);
//		}
//		public double getValue() {
//			double deg = 0;
//			double min = 0;
//			double sec = 0;
//			double sin = 1.;
//			if (nsew.getSelectedIndex() == 1) sin = -1.;
//			try {
//			  deg = Integer.valueOf(degrees.getText());
//			}
//			catch (NumberFormatException Ex) {
//				//return deg * sin;
//				deg = 0;
//			}
//			if (decimalMinutes.isSelected()){
//				try {
//					min = Double.valueOf(decminutes.getText());
//				}
//				catch (NumberFormatException ex) {
//					min = 0;
//				}
//			}
//			else {
//				try {
//					min = Integer.valueOf(minutes.getText());
//				}
//				catch (NumberFormatException ex) {
//					min = 0;
//				}
//				try {
//					sec = Double.valueOf(seconds.getText());
//				}
//				catch (NumberFormatException ex) {
//					sec = 0;
//				}
//				
//			}
//			deg += min/60 + sec/3600;
//			deg *= sin;
//			return deg;
//		}
//		public void sayFormattedValue(double value) {
//			if (isLatitude) {
//				formattedText.setText(LatLong.formatLatitude(value));
//			}
//			else {
//				formattedText.setText(LatLong.formatLongitude(value));
//			}
//		}
//	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {

		latLong = null;
		
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
//		System.out.println("Lat long get value: "  + latStrip.getValue() +  "  " + longStrip.getValue());
		LatLongDialog.latLong = new LatLong(latStrip.getValue(), longStrip.getValue());
		if (Double.isNaN(LatLongDialog.latLong.getLatitude()) || Double.isNaN(LatLongDialog.latLong.getLongitude())) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	
	
}
