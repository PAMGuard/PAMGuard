package Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;

public class MouseMeasureDisplay extends PamBorderPanel {
	
	MapController mapController;
	SimpleMap simpleMap;

	PamLabel range, rangeNmi, bearing, latitude, longitude;
	TitledBorder border;
	
	public MouseMeasureDisplay(MapController mapController, SimpleMap simpleMap) {
		this.mapController = mapController;
		this.simpleMap = simpleMap;
		
		JPanel p = new PamBorderPanel();
		p.setBorder(border = new TitledBorder("Mouse range and bearing"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = c.gridy = 0;
		c.ipadx = 4;
		addComponent(p, new PamLabel("Range "), c);
		c.gridx++;
		addComponent(p, range = new PamLabel("    "), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(p, new PamLabel("      "), c);
		c.gridx++;
		addComponent(p, rangeNmi = new PamLabel("    "), c);
		c.gridy++;
		c.gridx = 0;
		
		addComponent(p, new PamLabel("Bearing "), c);
		c.gridx++;
		addComponent(p, bearing = new PamLabel("    "), c);
		
		this.setLayout(new BorderLayout());
		add(BorderLayout.CENTER, p);
		this.setBorder(BorderFactory.createRaisedBevelBorder());
		
		setBackground(getBackground());
	}
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (border != null) {
			border.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
		}
	}

	public void showMouseData(Point start, Point end) {
		if (start == null || end == null) return;
		LatLong startLL = simpleMap.mapPanel.getRectProj().panel2LL(new Coordinate3d(start.getX(), start.getY(), 0.0));
		LatLong endLL = simpleMap.mapPanel.getRectProj().panel2LL(new Coordinate3d(end.getX(), end.getY(), 0.0));
		double r = startLL.distanceToMetres(endLL);
		double b = startLL.bearingTo(endLL);
		range.setText(String.format("%.0f m", r));
		rangeNmi.setText(String.format("%.1f nmi", r/1853.0));
		bearing.setText(String.format("%.1f \u00B0T", b));
	}

	public static void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
}
