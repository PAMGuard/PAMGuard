package PamUtils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

/**
 * A better Lat Long editable dialog strip, containing
 * the functionality developed for the new Array Manager. 
 * @author Doug Gillespie
 *
 */
public class LatLongEditableDialogStrip implements PamDialogPanel {

	private JPanel mainPanel;
	private JTextField latitude, longitude;
	private JButton menuButton;
	private LatLong currentLatLong;
	private Window owner;
	
	public LatLongEditableDialogStrip(Window owner, String borderTitle) {
		mainPanel = new JPanel(new GridBagLayout());
		this.owner = owner;
		if (borderTitle != null) {
			mainPanel.setBorder(new TitledBorder(borderTitle));
		}
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, new JLabel("Latitude: ", SwingConstants.RIGHT), c);
		c.gridx ++;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, latitude = new JTextField(12), c);
		latitude.setEditable(false);
		c.gridx+=c.gridwidth;
		c.gridheight = 2;
		c.fill = GridBagConstraints.VERTICAL;
		menuButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("Resources/MenuButton.png")));
		PamDialog.addComponent(mainPanel, menuButton, c);
		menuButton.setToolTipText("Lat Long Edit and paste options...");
		menuButton.addActionListener(new MenuButton());
		c.gridheight = 1;

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(mainPanel, new JLabel("Longitude: ", SwingConstants.RIGHT), c);
		c.gridx ++;
		c.gridwidth = 2;
		PamDialog.addComponent(mainPanel, longitude = new JTextField(12), c);
		longitude.setEditable(false);
	}

	private class MenuButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showOptionsMenu(e);
		}


		private void showOptionsMenu(ActionEvent e) {
			JPopupMenu menu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Edit");
			menuItem.addActionListener(new MenuEdit());
			menuItem.setToolTipText("Edit/enter values manually");
			menu.add(menuItem);

			menuItem = new JMenuItem("Current GPS Pos");
			CurrentGPS currGps = new CurrentGPS();
			menuItem.addActionListener(currGps);
			menuItem.setEnabled(currGps.getCurrentGpsData() != null);
			menuItem.setToolTipText("Use the gurrent GPS position");				
			menu.add(menuItem);

			menuItem = new JMenuItem("Paste from map");
			MapPaste mapP = new MapPaste();
			menuItem.addActionListener(mapP);
			menuItem.setEnabled(mapP.getCurrentGpsData() != null);
			menuItem.setToolTipText("Use the position where you last clicked the PAMGuard map with the mouse");	
			menu.add(menuItem);

			menu.show(menuButton, menuButton.getWidth()/2, menuButton.getHeight()/2);
		}
	}
	private class MenuEdit implements ActionListener {


		@Override
		public void actionPerformed(ActionEvent e) {
			menuEdit(e);
		}


	}
	private class CurrentGPS implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			currentGPS(e, currentGpsData);
		}
		private GpsData currentGpsData;

		CurrentGPS() {
			// find the current gps data. 
			GPSDataBlock gpsBlock = ArrayManager.getGPSDataBlock();
			if (gpsBlock != null) {
				GpsDataUnit lastUnit = gpsBlock.getLastUnit();
				if (lastUnit != null) {
					currentGpsData = new GpsData(lastUnit.getGpsData());
				}
			}
		}

		/**
		 * @return the currentGpsData
		 */
		public GpsData getCurrentGpsData() {
			return currentGpsData;
		}
	}
	private class MapPaste implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mapPaste(e, currentGpsData);
		}
		private GpsData currentGpsData;

		MapPaste() {
			// see if it's possible to get the latlong out 
			// of the clipboard. ...
			LatLong ll = LatLong.getPastedValue();
			if (ll != null) {
				currentGpsData = new GpsData(ll);
			}
		}

		/**
		 * @return the currentGpsData
		 */
		public GpsData getCurrentGpsData() {
			return currentGpsData;
		}
	}
	public void menuEdit(ActionEvent e) {
		if (currentLatLong == null) {
			currentLatLong = new LatLong();
		}
		LatLong newLatLong = LatLongDialog.showDialog(owner, currentLatLong, "Enter fixed position");
		if (newLatLong != null) {
			setLatLong(newLatLong);
		}
	}

	public void currentGPS(ActionEvent e, GpsData currentGpsData) {
		if (currentGpsData != null) {
			setLatLong(currentGpsData);
		}
	}

	public void mapPaste(ActionEvent e, GpsData pastedGpsData) {
		if (pastedGpsData != null) {
			setLatLong(pastedGpsData);
		}
	}
	
	/**
	 * Just set the lat long without resetting the heading. 
	 * @param latLong
	 */
	public void setLatLong(LatLong latLong) {
		currentLatLong = latLong;
		if (currentLatLong != null) {
			latitude.setText(latLong.formatLatitude());
			longitude.setText(latLong.formatLongitude());
		}
		else {
			latitude.setText("");
			longitude.setText("");
		}
	}
	
	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}
	
	/**
	 * Get teh current value. 
	 * @return
	 */
	public LatLong getLatLong() {
		return currentLatLong;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	public void enableControls(boolean enable) {
		menuButton.setEnabled(enable);
		latitude.setEnabled(enable);
		longitude.setEnabled(enable);
	}
}
