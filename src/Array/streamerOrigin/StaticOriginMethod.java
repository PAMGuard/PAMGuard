package Array.streamerOrigin;

import java.awt.BorderLayout;
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

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import GPS.NavDataSynchronisation;
import PamUtils.LatLong;
import PamUtils.LatLongDialog;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import javafx.scene.layout.Pane;

public class StaticOriginMethod extends HydrophoneOriginMethod {

	private StaticHydrophoneComponent staticHydrophoneDialogComponent;

	private StaticOriginSettings staticOriginSettings = new StaticOriginSettings();

	public StaticOriginMethod(PamArray pamArray, Streamer streamer) {
		super(pamArray, streamer);
	}

	@Override
	public String getName() {
		return StaticOriginSystem.systemName;
	}

	@Override
	public OriginDialogComponent getDialogComponent() {
		if (staticHydrophoneDialogComponent == null) {
			staticHydrophoneDialogComponent = new StaticHydrophoneComponent();
		}
		return staticHydrophoneDialogComponent;
	}


	@Override
	public OriginSettings getOriginSettings() {
		return staticOriginSettings;
	}

	@Override
	public void setOriginSettings(OriginSettings originSettings) {
		staticOriginSettings = (StaticOriginSettings) originSettings;
	}

	@Override
	public boolean prepare() {
		return true;
	}

	@Override
	public StreamerDataUnit getLastStreamerData() {	
		StreamerDataUnit sdu;
		GpsDataUnit setPosition = staticOriginSettings.getStaticPosition();
		if (setPosition != null) {
			sdu = new StreamerDataUnit(setPosition.getTimeMilliseconds(), streamer);
			sdu.setGpsData(setPosition.getGpsData());
			return sdu;
		}
		StreamerDataBlock streamerDataBlock = ArrayManager.getArrayManager().getStreamerDatabBlock();
		sdu = streamerDataBlock.getLastUnit(1<<streamer.getStreamerIndex());		
		if (sdu == null) {
		 sdu = new StreamerDataUnit(PamCalendar.getTimeInMillis(), streamer);
		//System.out.println("StaticOriginMethod: Streamer rotation: " +sdu.getGpsData().getQuaternion().toHeading());
		}
		return sdu;
	}

	@Override
	public OriginIterator getGpsDataIterator(int wherefrom) {
		return new StreamerDataIterator(wherefrom, streamer);
	}

	@Override
	public Object getSynchronizationObject() {
		return NavDataSynchronisation.getSynchobject();
	}
	
	
	/**
	 * GUI components for the static hydrophone locator. This returns a JavaFX or Swing component depending on the current GUI. 
	 */
	private class StaticHydrophoneComponent extends OriginDialogComponent {
		
		/**
		 * Swing component. 
		 */
		private StaticHydrophoneDialogComponent staticHydrophoneDialog;
		
		/**
		 * JavaFX pane for static hydrophones.
		 */
		private StaticHydrophonePane staticHydrophonePane;
		
		
		@Override
		public JComponent getComponent(Window owner) {
			if (staticHydrophoneDialog==null) {
				staticHydrophoneDialog = new StaticHydrophoneDialogComponent(); 
			}
			return staticHydrophoneDialog.getComponent(owner);
		}

		@Override
		public void setParams() {
			if (staticHydrophoneDialog!=null) {
				staticHydrophoneDialog.setParams();
			}
			if (staticHydrophonePane!=null) {
				staticHydrophonePane.setParams(); 
			}
		}

		@Override
		public boolean getParams() {
			if (staticHydrophoneDialog!=null) {
				return staticHydrophoneDialog.getParams();
			}
			if (staticHydrophonePane!=null) {
				return staticHydrophonePane.getParams();
			}
			return false;
		}

		@Override
		public Pane getSettingsPane() {
			if (staticHydrophonePane==null) {
				staticHydrophonePane = new StaticHydrophonePane(StaticOriginMethod.this); 
			}
			return staticHydrophonePane;
		}
	}
	

	/**
	 * Swing components for the static hydrophone locator. 
	 */
	private class StaticHydrophoneDialogComponent extends OriginDialogComponent {

		private JPanel outerPanel;
		//		private LatLongDialogStrip latStrip;
		//		private LatLongDialogStrip longStrip;
		private JTextField latitude, longitude;
		private JButton menuButton;

		public StaticHydrophoneDialogComponent() {
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			PamDialog.addComponent(mainPanel, new JLabel("Latitude: ", JLabel.RIGHT), c);
			c.gridx ++;
			c.gridwidth = 2;
			PamDialog.addComponent(mainPanel, latitude = new JTextField(12), c);
			latitude.setEditable(false);
			c.gridx+=c.gridwidth;
			c.gridheight = 2;
			menuButton = new JButton(new ImageIcon(ClassLoader
					.getSystemResource("Resources/MenuButton.png")));
			PamDialog.addComponent(mainPanel, menuButton, c);
			menuButton.setToolTipText("Lat Long Edit and paste options...");
			menuButton.addActionListener(new MenuButton());
			c.gridheight = 1;

			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 1;
			PamDialog.addComponent(mainPanel, new JLabel("Longitude: ", JLabel.RIGHT), c);
			c.gridx ++;
			c.gridwidth = 2;
			PamDialog.addComponent(mainPanel, longitude = new JTextField(12), c);
			longitude.setEditable(false);
			c.gridx+=c.gridwidth;

			outerPanel = new JPanel(new BorderLayout());
			outerPanel.add(BorderLayout.WEST, mainPanel);

		}

		@Override
		public JComponent getComponent(Window owner) {
			return outerPanel;
		}

		@Override
		public void setParams() {
			GpsDataUnit dataUnit = staticOriginSettings.getStaticPosition();
			if (dataUnit == null) {
				return;
			}
			GpsData gpsData = dataUnit.getGpsData();
			if (gpsData == null) {
				return;
			}
			else {
				setLatLong(gpsData);
			}
			// set the head pitch and roll from the main bit of the streamer dialog. 
			
		}

		/**
		 * Just set the lat long without resetting the heading. 
		 * @param latLong
		 */
		private void setLatLong(LatLong latLong) {
			latitude.setText(latLong.formatLatitude());
			longitude.setText(latLong.formatLongitude());
		}

		@Override
		public boolean getParams() {
			//			String txt = trueHeading.getText();
			//			if (txt == null || txt.length() == 0) {
			////				staticOriginSettings.getStaticPosition().setTrueHeading(null);
			//				return true;
			//			}
			//			else {
			//				try {
			//					double th = Double.valueOf(txt);
			////					staticOriginSettings.getStaticPosition().setTrueHeading(new Double(th));
			//					return true;
			//				}
			//				catch (NumberFormatException e) {
			//					return PamDialog.showWarning(null, "Error", "Error in heading information");
			//				}
			//			}
			boolean ok =  staticOriginSettings != null && staticOriginSettings.getStaticPosition() != null;

			return ok;
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
			GpsDataUnit statPos = staticOriginSettings.getStaticPosition();
			GpsData gpsData = null;
			if (statPos != null) {
				gpsData = statPos.getGpsData();
			}
			LatLong newLatLong = LatLongDialog.showDialog(null, gpsData, "Hydrophone array reference position");
			if (newLatLong != null) {
				staticOriginSettings.setStaticPosition(streamer, new GpsData(newLatLong));
				setLatLong(newLatLong);
			}
		}

		public void currentGPS(ActionEvent e, GpsData currentGpsData) {
			if (currentGpsData != null) {
				staticOriginSettings.setStaticPosition(streamer, currentGpsData);
				setLatLong(currentGpsData);
			}
		}

		public void mapPaste(ActionEvent e, GpsData pastedGpsData) {
			if (pastedGpsData != null) {
				staticOriginSettings.setStaticPosition(streamer, pastedGpsData);
				setLatLong(pastedGpsData);
			}
		}

		@Override
		public Pane getSettingsPane() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
