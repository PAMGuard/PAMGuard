/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Map;

import geoMag.MagneticVariation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import GPS.GPSControl;
import GPS.GpsData;
import PamController.masterReference.MasterReferencePoint;
import PamModel.SMRUEnable;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class GpsTextDisplay extends JPanel {

	private LatLong lastMouseLatLong;

	private double mouseX;

	private double mouseY;

	private MapController mapController;
	private SimpleMap simpleMap;
	
	private GpsTextAreaLabel latitude, longitude, time, date, course, heading, speed, 
	cursorLat, cursorLong, cursorRange, cursorBearing, magneticDeviation;
	private Color labelColor=Color.white;
	private GpsTextAreaLabel cursorReference;
	private GpsTextAreaLabel lastFixTime;
	private TextAreaPanel gpsPanel;
	private TextAreaPanel cursorPanel;

	private GpsTextAreaLabel headingLabel;

	private GpsTextAreaLabel cursorRangeNmi;

	private String lastFixType;

//	private TitledBorder gpsBorder, cursorBorder;


	/**
	 * 
	 */
	public GpsTextDisplay(MapController mapController, SimpleMap simpleMap) {
		super();
		this.simpleMap = simpleMap;
		this.mapController = mapController;
		this.setOpaque(false);
		this.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		GridBagConstraints c = new GridBagConstraints();
		gpsPanel = new TextAreaPanel("GPS data");
//		gpsPanel.setBorder(gpsBorder = BorderFactory.createTitledBorder("GPS data"));
//		gpsBorder.setTitleColor(labelColor);
		gpsPanel.setLayout(new GridBagLayout());
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = c.gridy = 0;
		c.ipadx = 4;
		addComponent(gpsPanel, new GpsTextAreaLabel("Latitude"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Longitude"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Time"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Last Fix"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Date"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Course"), c);
		c.gridy++;
		addComponent(gpsPanel, headingLabel = new GpsTextAreaLabel("Heading"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Speed"), c);
		c.gridy++;
		addComponent(gpsPanel, new GpsTextAreaLabel("Mag. Var."), c);
		c.gridy++;
		c.gridx = 1;
		c.gridy = 0;
		addComponent(gpsPanel, latitude = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, longitude = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, time = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, lastFixTime = new LastFixLabel("  "), c);
		lastFixTime.setToolTipText(" ");
//		lastFixTime.setBackground(Color.RED);
//		lastFixTime.setForeground(Color.RED);
		c.gridy++;
		addComponent(gpsPanel, date = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, course = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, heading = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, speed = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(gpsPanel, magneticDeviation = new GpsTextAreaLabel(), c);
		gpsPanel.setOpaque(false);
		this.add(gpsPanel);
		
		heading.setToolTipText("<html>Heading data will only be available if you have a vector gps, or a gyro or <p>"
				+ "magnetic compass configured it is not available from standard GPS receivers.");
		headingLabel.setToolTipText(heading.getToolTipText());
		
		cursorPanel = new TextAreaPanel("Cursor Position");
//		cursorPanel.setPreferredBorderColour(Color.white, 1);
//		cursorPanel.setBorder(cursorBorder = BorderFactory.createTitledBorder("Cursor Position"));
//		cursorBorder.setTitleColor(labelColor);
		cursorPanel.setLayout(new GridBagLayout());
		c.anchor = GridBagConstraints.WEST;
		c.gridx = c.gridy = 0;
		c.ipadx = 4;
		addComponent(cursorPanel, new GpsTextAreaLabel("Latitude"), c);
		c.gridy++;
		addComponent(cursorPanel, new GpsTextAreaLabel("Longitude"), c);
		c.gridy++;
		c.gridwidth = 2;
//		addComponent(cursorPanel, new GpsTextAreaLabel("Range and bearing from ship"), c);
		c.gridy++;
		c.gridwidth = 1;
		addComponent(cursorPanel, new GpsTextAreaLabel("Bearing"), c);
		c.gridy++;
		addComponent(cursorPanel, new GpsTextAreaLabel("Range"), c);
		c.gridy++;
		if ( SMRUEnable.isEnable() ) {
			addComponent(cursorPanel, new GpsTextAreaLabel(" "), c);
			c.gridy++;
		}
		c.gridx = 1;
		c.gridy = 0;
		addComponent(cursorPanel, cursorLat = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(cursorPanel, cursorLong = new GpsTextAreaLabel(""), c);
		c.gridy+=2;
		addComponent(cursorPanel, cursorBearing = new GpsTextAreaLabel(""), c);
		c.gridy++;
		addComponent(cursorPanel, cursorRange = new GpsTextAreaLabel(""), c);
		c.gridy++;
		if ( SMRUEnable.isEnable() ) {
			addComponent(cursorPanel, cursorRangeNmi = new GpsTextAreaLabel(""), c);
			c.gridy++;
		}
		c.gridx = 0;
		c.gridwidth = 2;
		addComponent(cursorPanel, cursorReference = new GpsTextAreaLabel(" "), c);
		cursorPanel.setOpaque(false);
		this.add(cursorPanel);
		
	}

	private class TextAreaPanel extends JPanel {

		
		private TitledBorder titledBorder;

		public TextAreaPanel(String borderTitle) {
			super();
			if (borderTitle != null) {
				setBorder(titledBorder = BorderFactory.createTitledBorder(borderTitle));
			}
			setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
			titledBorder.setTitleColor(Color.white);
		}
		
		public void setTitle(String newTitle) {
			if (titledBorder == null) {
				setBorder(titledBorder = BorderFactory.createTitledBorder(newTitle));
			}
			else {
				titledBorder.setTitle(newTitle);
			}
		}
		
	}
	
	private class LastFixLabel extends GpsTextAreaLabel {

		/**
		 * @param text
		 */
		public LastFixLabel(String text) {
			super(text);
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText()
		 */
		@Override
		public String getToolTipText() {
			if (lastFixType == null) {
				return null;
			}
			else {
				return "Fix type: " + GpsData.getLongFixType(lastFixType);
			}
		}
		
	}
	
	private class GpsTextAreaLabel extends JLabel {

		public GpsTextAreaLabel() {
			super();
			// TODO Auto-generated constructor stub
		}

		public GpsTextAreaLabel(Icon image, int horizontalAlignment) {
			super(image, horizontalAlignment);
			// TODO Auto-generated constructor stub
		}

		public GpsTextAreaLabel(Icon image) {
			super(image);
			// TODO Auto-generated constructor stub
		}

		public GpsTextAreaLabel(String text, Icon icon, int horizontalAlignment) {
			super(text, icon, horizontalAlignment);
			// TODO Auto-generated constructor stub
		}

		public GpsTextAreaLabel(String text, int horizontalAlignment) {
			super(text, horizontalAlignment);
			// TODO Auto-generated constructor stub
		}

		public GpsTextAreaLabel(String text) {
			super(text);
			// TODO Auto-generated constructor stub
		}
		
	}
	public static void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		// TODO Auto-generated method stub
		super.setPreferredSize(preferredSize);
	}

	@Override
	public void setSize(Dimension d) {
		// TODO Auto-generated method stub
		super.setSize(d);
	}

	@Override
	public void setSize(int width, int height) {
		// TODO Auto-generated method stub
		super.setSize(width, height);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		Rectangle r = g.getClipBounds();
		g.fillRect(r.x, r.y, r.width, r.height);
		super.paintComponent(g);
	}

	public void updateGpsTextArea() {
		// TODO Need to sort this out to display something useful in the viewer. 
		gpsPanel.setTitle(MasterReferencePoint.getName());
//		gpsBorder.setTitle();
		LatLong latLong = MasterReferencePoint.getLatLong();
		lastFixType = null;
		if (latLong == null) {
			latitude.setText("");
			longitude.setText("");
		}
		else {
			latitude.setText(LatLong.formatLatitude(latLong.getLatitude()));
			longitude.setText(LatLong.formatLongitude(latLong.getLongitude()));
		}
		if (latLong instanceof GpsData) {
			lastFixType = ((GpsData) latLong).getFixType();
		}
		Long lf = MasterReferencePoint.getFixTime();
		if (lf == null) {
			lastFixTime.setText("");
		}
		else {
			String lastFix = PamCalendar.formatTime(lf, true);
			if (lastFixType != null) {
				lastFix += String.format(" (%s)", lastFixType);
			}
			lastFixTime.setText(lastFix);
		}
		long now = PamCalendar.getTimeInMillis();
		time.setText(PamCalendar.formatTime(now, true));
		date.setText(PamCalendar.formatDBDate(now));
		Double course = MasterReferencePoint.getCourse();
		if (course == null) {
			this.course.setText("-");
		}
		else {
			this.course.setText(String.format("%.1f \u00B0T", course));
		}
		Double head = MasterReferencePoint.getHeading();
		if (head == null) {
			this.heading.setText("-no data-");
		}
		else {
			this.heading.setText(String.format("%.1f \u00B0T", head));
		}
		Double speed = MasterReferencePoint.getSpeed();
		if (speed == null) {
			this.speed.setText("-");
		}
		else {
			this.speed.setText(String.format("%.1f kn", speed));
		}
		Double magneticDeviation = null;
		if (latLong != null) {
			long t = now;
			magneticDeviation = MagneticVariation.getInstance().getVariation(t, latLong.getLatitude(), latLong.getLongitude());
		}
		if (magneticDeviation == null) {
			this.magneticDeviation.setText("-");
		}
		else {
			this.magneticDeviation.setText(String.format("%.1f \u00B0", magneticDeviation));
		}
		
		String errString = MasterReferencePoint.getError();
		setToolTipText(errString);
		if (errString != null) {
			//set panel forground colors for warning.
			setForground(gpsPanel , Color .black);
			setForground(cursorPanel , Color .black);
			gpsPanel.setBackground(PamColors.getInstance().getColor(PamColor.WARNINGBORDER));
			setBackground(PamColors.getInstance().getColor(PamColor.WARNINGBORDER));
		}
		else {
			//set panel background colors for normal operation. 
			setForground(gpsPanel , labelColor);
			setForground(cursorPanel , labelColor);
			gpsPanel.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
			setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
		}
		if (head == null && wantGpsHeading()) {
			heading.setForeground(Color.RED);
			headingLabel.setForeground(Color.RED);
		}
		else {
			heading.setForeground(labelColor);
			headingLabel.setForeground(labelColor);
		}
	}
	
	/**
	 * work out if user WANTED to read gps heading data and only 
	 * colour label in red if they did want it. 
	 * @return
	 */
	boolean wantGpsHeading() {
		GPSControl gpsControl = simpleMap.getGpsControl();
		if (gpsControl == null) {
			return false;
		}
		return gpsControl.getGpsParameters().readHeading;
	}

	public void displayZoomedorRotated() {
		if (lastMouseLatLong != null) {
			updateMouseCoords(lastMouseLatLong);
		}
	}
	
	
	public void updateMouseCoords(LatLong mouseLatLong) {
		if (mouseLatLong == null) {
			cursorRange.setText("           ");
			cursorBearing.setText("           ");
			
			if ( SMRUEnable.isEnable() )
				cursorRangeNmi.setText("           ");
			return;
		}

		cursorLat.setText(LatLong.formatLatitude(mouseLatLong.getLatitude()));
		cursorLong.setText(LatLong.formatLongitude(mouseLatLong.getLongitude()));
		
		LatLong refLatLong = MasterReferencePoint.getLatLong();
		if (refLatLong == null) {
			refLatLong = new LatLong();
		}
//		else {

		double vessel2CursorRange = refLatLong.distanceToMetres(mouseLatLong);
		double vessel2CursorRangeNmi = refLatLong.distanceToMiles(mouseLatLong);
		double v2cAngle = refLatLong.bearingTo(mouseLatLong);

		cursorRange.setText(String.format("%.0f m", vessel2CursorRange));
		cursorBearing.setText(String.format("%.1f \u00B0T", v2cAngle));
		if ( SMRUEnable.isEnable() )
		cursorRangeNmi.setText(String.format("%.1f nmi", vessel2CursorRangeNmi));
//		}
		
		String cursorRef = MasterReferencePoint.getName();
		if (cursorRef == null) {
			cursorReference.setText(" ");
		}
		else {
			cursorReference.setText(String.format("(re. %s)", cursorRef));
		}
		
		lastMouseLatLong = mouseLatLong;
		
	}
	public void mouseExited() {
		cursorLat.setText("              ");
		cursorLong.setText("");
		cursorBearing.setText("");
		cursorRange.setText("");
		
		if ( SMRUEnable.isEnable() )
			cursorRangeNmi.setText("");
	}
	public void newShipGps() {
//		this.shipGps = shipGps;
	}

//	public void setPixelsPerMetre(double pixelsPerMetre) {
//		this.pixelsPerMetre = pixelsPerMetre;
//	}

//	public void setShipPosition(Coordinate3d shipPosition) {
//		this.shipPosition = shipPosition;
//	}

	public void setMouseX(double mouseX) {
		this.mouseX = mouseX;
	}

	public void setMouseY(double mouseY) {
		this.mouseY = mouseY;
	}

public void copyMouseMapPositionToClipboard(double mouseLat, double mouseLon){
		
	
	Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	cb.setContents(new LatLong(mouseLat, mouseLon)
//												.getTransferData(LatLong.latLongFlavor)
												, simpleMap.clipboardCopier);
	
		//TODO added this code to copy the last clicked lat long string to the clipboard - find a better way!! David McL
//		String mouseLLString = "Lat:" + LatLong.formatLatitude(mouseLat) 
//		+ ", Lon:" + LatLong.formatLongitude(mouseLon)
//		+ ", Range:"
//		+ (int) vessel2CursorRange + "m"
//		+ ", Bearing:" + (int) v2cAngle + "\u00B0";
////		System.out.println(mouseLLString);
//		JTextField tempField = new JTextField(mouseLLString);
//		tempField.selectAll();
//		tempField.copy();
	}

//@Override
//public void setBackground(Color bg) {
//	super.setBackground(bg);
//	if (gpsBorder == null || cursorBorder == null) return;
//	gpsBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
//	cursorBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
//}

/**
 * Sets the forground color of the first layer of components within an panel. For secondary and tertiary layers etc.. will need to loop this function within iteslef. 
 * @param panel - panel to change forground color of.
 * @param colour -forground colour. 
 */
private static void setForground(JPanel panel, Color colour){
	for (int i=0; i<panel.getComponentCount(); i++){
		panel.getComponent(i).setForeground(colour);
	}
}

//public void setLastFix(GpsData lastFix) {
//	this.lastFix = lastFix;
//}


}