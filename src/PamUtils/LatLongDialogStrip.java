package PamUtils;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamPlainTextField;

/**
 * Shows the value of a latitude or longitude in various standard formats.
 * <p>
 * Allows users to input values in various formats. 
 * 
 * @author Doug Gillespie
 *
 */
public class LatLongDialogStrip extends JPanel implements KeyListener, ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	JLabel formattedText;
	PamPlainTextField degrees;
	PamPlainTextField minutes, seconds, decminutes, decimal;
	JLabel dl, ml, sl, dml, dec;
	JComboBox nsew;
	boolean isLatitude;
//	boolean decimalMinutes = true;
	

	/**
	 * Construct a strip of controls to include in a larger dialog. 
	 * <p>With this version of the constructor there is an option
	 * to display a titled border with the 
	 * word Latitude or Longitude
	 * @param latitude true if it's latitude, false for longitude. 
	 * @param showBorder true to show the titled border. 
	 */
	public LatLongDialogStrip(boolean latitude, boolean showBorder) {
		isLatitude = latitude;
		createDialogStrip(showBorder);
	}
	
	/**
	 * Construct a strip of controls to include in a larger dialog. 
	 * <p>By default the strip will have a titled border with the 
	 * word Latitude or Longitude
	 * @param latitude true if it's latitude, false for longitude. 
	 */
	public LatLongDialogStrip(boolean latitude) {
		isLatitude = latitude;
		createDialogStrip(true);
	}
	
	private void createDialogStrip(boolean showBorder) {
		
		String borderTitle;
		if (isLatitude) borderTitle = "Latitude";
		else borderTitle = "Longitude";
		if (showBorder) {
			this.setBorder(new TitledBorder(borderTitle));
		}
		
		degrees = new PamPlainTextField(4);
		minutes = new PamPlainTextField(3);
		seconds = new PamPlainTextField(6);
		decminutes = new PamPlainTextField(8);
		decimal=new PamPlainTextField(12); 
		nsew = new JComboBox<String>();
		Dimension d = nsew.getPreferredSize();
		d.width = 45;
		nsew.setPreferredSize(d);
		dl = new JLabel("deg.");
		ml = new JLabel("min.");
		sl = new JLabel("sec.");
		dml = new JLabel("dec min.");
		dec = new JLabel("decimal deg."); 
		formattedText = new JLabel("Position");
		if (isLatitude) {
			nsew.addItem("N");
			nsew.addItem("S");
		}
		else{
			nsew.addItem("E");
			nsew.addItem("W");
		}
		setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(this, dl, c);
		c.gridx++;
		PamDialog.addComponent(this, degrees, c);
		c.gridx++;
		PamDialog.addComponent(this, ml, c);
		c.gridx++;
		PamDialog.addComponent(this, minutes, c);
		c.gridx++;
		PamDialog.addComponent(this, sl, c);
		c.gridx++;
		PamDialog.addComponent(this, seconds, c);
		c.gridx++;
		PamDialog.addComponent(this, dml, c);
		c.gridx++;
		PamDialog.addComponent(this, decminutes, c);
		c.gridx++;
		PamDialog.addComponent(this, dec, c);
		c.gridx++;
		PamDialog.addComponent(this, decimal, c);
		c.gridx++;
		PamDialog.addComponent(this, nsew, c);
		c.gridx++;
		
//		mp.add(dl);
//		mp.add(degrees);
//		mp.add(ml);
//		mp.add(minutes);
//		mp.add(sl);
//		mp.add(seconds);
//		mp.add(dml);
//		mp.add(decminutes);
//		mp.add(nsew);
		
		degrees.addKeyListener(this);
		minutes.addKeyListener(this);
		seconds.addKeyListener(this);
		decminutes.addKeyListener(this);
		nsew.addActionListener(this);
		decimal.addActionListener(this);
		
//		this.add(BorderLayout.CENTER, mp);
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 8;
		PamDialog.addComponent(this, formattedText, c);
//		this.add(BorderLayout.SOUTH, formattedText);
		
		showControls();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		
		newTypedValues(null);
		
	}
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		
		newTypedValues(e);
		
	}
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		
		newTypedValues(e);
		
	}
	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		
		newTypedValues(e);
		
	}
	
	
	private void newTypedValues(KeyEvent e) {
		double v = getValue();
		// now need to put that into the fields that
		// are not currently shown so that they are
		// ready if needed. 
		if (e != null) {
			sayValue(v, true);
		}
		// and say the formated version
		sayFormattedValue(v);
	}
	
	
	public void showControls() {
		int format = LatLong.getFormatStyle(); 
		//boolean decimalMins = (LatLong.getFormatStyle() == LatLong.FORMAT_DECIMALMINUTES);
				
		degrees.setVisible(format == LatLong.FORMAT_MINUTESSECONDS || format == LatLong.FORMAT_DECIMALMINUTES); 
		dl.setVisible(format == LatLong.FORMAT_MINUTESSECONDS || format == LatLong.FORMAT_DECIMALMINUTES); 

		minutes.setVisible(format == LatLong.FORMAT_MINUTESSECONDS );
		ml.setVisible(format == LatLong.FORMAT_MINUTESSECONDS );
		seconds.setVisible(format == LatLong.FORMAT_MINUTESSECONDS );
		
		sl.setVisible(format == LatLong.FORMAT_DECIMALMINUTES);
		decminutes.setVisible(format == LatLong.FORMAT_DECIMALMINUTES);
		dml.setVisible(format == LatLong.FORMAT_DECIMALMINUTES);
		
		dec.setVisible(format == LatLong.FORMAT_DECIMAL);
		decimal.setVisible(format == LatLong.FORMAT_DECIMAL);
		
		sayFormattedValue(getValue());
	}
	/**
	 * Set data in the lat long dialog strip
	 * @param value Lat or Long in decimal degrees.
	 */
	public void sayValue(double value) {
		sayValue(value, false);
	}
	
	
	public void sayValue(double value, boolean hiddenOnly) {
		if (value >= 0) {
			nsew.setSelectedIndex(0);
		}
		else {
			nsew.setSelectedIndex(1);
		}
		double deg = LatLong.getSignedDegrees(value);
		if (degrees.isVisible() == false || !hiddenOnly) degrees.setText(String.format("%d", (int)Math.abs(deg)));
		if (minutes.isVisible() == false || !hiddenOnly) minutes.setText(String.format("%d", LatLong.getIntegerMinutes(value)));
		if (decminutes.isVisible() == false || !hiddenOnly) decminutes.setText(String.format("%3.5f", LatLong.getDecimalMinutes(value)));
		if (seconds.isVisible() == false || !hiddenOnly) seconds.setText(String.format("%3.5f", LatLong.getSeconds(value)));
		if (nsew.isVisible() == false || !hiddenOnly) nsew.setSelectedIndex(deg >= 0 ? 0 : 1);
		if (decimal.isVisible() == false || !hiddenOnly) decimal.setText(String.format("%.8f", value));
		
		sayFormattedValue(value);
	}
	
	/**
	 * Get the value from the visible text fields 
	 * @return the latitude/longitude in decimal format. 
	 */
	public double getValue() {
		double deg = 0;
		double min = 0;
		double sec = 0;
		double sin = 1.;
		if (nsew.getSelectedIndex() == 1) sin = -1.;
		
		
		if (LatLong.getFormatStyle() == LatLong.FORMAT_DECIMAL){
			try {
				deg = Double.valueOf(decimal.getText());
				return deg; 
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
		}
		
		try {
		  deg = Integer.valueOf(degrees.getText());
		}
		catch (NumberFormatException Ex) {
			return Double.NaN;
		}
		
		
		if (LatLong.getFormatStyle() == LatLong.FORMAT_DECIMALMINUTES){
			try {
				min = Double.valueOf(decminutes.getText());
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
		}
		else {
			try {
				min = Integer.valueOf(minutes.getText());
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
			try {
				sec = Double.valueOf(seconds.getText());
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
			
		}
		deg += min/60 + sec/3600;
		deg *= sin;
		return deg;
	}
	public void clearData() {
		degrees.setText("");
		minutes.setText("");
		seconds.setText("");
		decminutes.setText("");
		decimal.setText("");
	}
	public void sayFormattedValue(double value) {
		if (isLatitude) {
			formattedText.setText(LatLong.formatLatitude(value));
		}
		else {
			formattedText.setText(LatLong.formatLongitude(value));
		}
	}
//	public boolean isDecimalMinutes() {
//		return decimalMinutes;
//	}
//	public void setDecimalMinutes(boolean decimalMinutes) {
//		this.decimalMinutes = decimalMinutes;
//		showControls();
//	}
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		degrees.setEnabled(enabled);
		minutes.setEnabled(enabled);
		seconds.setEnabled(enabled);
		decminutes.setEnabled(enabled);
		nsew.setEnabled(enabled);
		decimal.setEnabled(enabled);
	}
}
