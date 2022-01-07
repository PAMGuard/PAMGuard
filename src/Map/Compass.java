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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;

import PamUtils.Coordinate3d;
import PamView.PamColors;
import PamView.PamColors.PamColor;

/**
 * 
 * Draws a compass given the rotation of the map in degrees.
 * 
 * 
 * @author David McLaren
 *
 */


public class Compass {

	int compassRadius = 40;

	double mapRotationDegrees;

	int panelWidth;

	Coordinate3d compassCentre = new Coordinate3d();

	Coordinate3d arrowTail = new Coordinate3d();

	Coordinate3d arrowHead = new Coordinate3d();

	Coordinate3d arrowHeadEndLeft = new Coordinate3d();

	Coordinate3d arrowHeadEndRight = new Coordinate3d();

	String northString;

	float stringWidth, stringHeight;

	Font northStringFont = new Font("Arial", Font.BOLD, 12);
	
	static BasicStroke solid =
		        new BasicStroke(2f);
	 
	
	public Compass() {
		super();
	}

	public void drawCompass(Graphics2D g2d, MapRectProjector rectProj) {

		compassCentre.x = panelWidth - 30;
		compassCentre.y = 30;
		Stroke oldStroke = g2d.getStroke();

		Color currentColor, northColor, fontColor, compassColor, mountColor;
		compassColor = new Color(222, 222, 222);
		fontColor = new Color(0, 128, 192);
		northColor = new Color(255, 0, 0);
		mountColor = PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA);
		currentColor = g2d.getColor();
		// ((Graphics2D) g2d).setColor(northColor);

		arrowTail.x = 0;
		arrowTail.y = 20;
		arrowHead.x = 0;
		arrowHead.y = -20;
		arrowHeadEndLeft.x = -5;
		arrowHeadEndLeft.y = -15;
		arrowHeadEndRight.x = 5;
		arrowHeadEndRight.y = -15;

		TransformUtilities.rotateDegreesZ(arrowTail,
				mapRotationDegrees);
		arrowTail.x = compassCentre.x - arrowTail.x;
		arrowTail.y = compassCentre.y + arrowTail.y;
		TransformUtilities.rotateDegreesZ(arrowHead,
				mapRotationDegrees);
		arrowHead.x = compassCentre.x - arrowHead.x;
		arrowHead.y = arrowHead.y + compassCentre.y;
		TransformUtilities.rotateDegreesZ(
				arrowHeadEndLeft, mapRotationDegrees);
		arrowHeadEndLeft.x = compassCentre.x - arrowHeadEndLeft.x;
		arrowHeadEndLeft.y = arrowHeadEndLeft.y + compassCentre.y;
		TransformUtilities.rotateDegreesZ(
				arrowHeadEndRight, mapRotationDegrees);
		arrowHeadEndRight.x = compassCentre.x - arrowHeadEndRight.x;
		arrowHeadEndRight.y = arrowHeadEndRight.y + compassCentre.y;

		(g2d).setColor(mountColor);
		(g2d).fill3DRect((int) (compassCentre.x - 25.0),
				(int) (compassCentre.y - 25.0), 50, 50, true);

		(g2d).setColor(compassColor);
		(g2d).fillOval(
				(int) (compassCentre.x - (double) compassRadius / 2),
				(int) (compassCentre.y - compassRadius / 2.0),
				compassRadius, compassRadius);
		(g2d).setColor(northColor);
		(g2d).setStroke(solid);
		(g2d).drawLine((int) arrowHead.x, (int) arrowHead.y,
				(int) arrowTail.x, (int) arrowTail.y);
		(g2d).drawLine((int) arrowHead.x, (int) arrowHead.y,
				(int) arrowHeadEndLeft.x, (int) arrowHeadEndLeft.y);
		(g2d).drawLine((int) arrowHead.x, (int) arrowHead.y,
				(int) arrowHeadEndRight.x, (int) arrowHeadEndRight.y);
		(g2d).drawLine((int) arrowHead.x, (int) arrowHead.y,
				(int) arrowHeadEndRight.x, (int) arrowHeadEndRight.y);
		(g2d).setColor(compassColor);
		(g2d).fillOval(
				(int) (compassCentre.x - (double) compassRadius / 4),
				(int) (compassCentre.y - compassRadius / 4.0),
				(int) (compassRadius / 2.0), (int) (compassRadius / 2.0));
		(g2d).setFont(northStringFont);
		String northString = "N";
		FontRenderContext frc = (g2d).getFontRenderContext();
		stringWidth = (float) northStringFont.getStringBounds(northString, frc)
				.getWidth();
		stringHeight = (float) northStringFont
				.getStringBounds(northString, frc).getHeight();

		(g2d).setColor(fontColor);
		(g2d).drawString(northString,
				(int) (compassCentre.x - stringWidth / 2.0),
				(int) (compassCentre.y + stringHeight / 2));

		(g2d).setColor(currentColor);
		g2d.setStroke(oldStroke);

	}

	public void setMapRotationDegrees(double mapRotationDegrees) {
		this.mapRotationDegrees = mapRotationDegrees;
		// System.out.println("Compass, mapRotationDegrees: " +
		// mapRotationDegrees);
	}

	public void setPanelWidth(int panelWidth) {
		this.panelWidth = panelWidth;
	}

}
