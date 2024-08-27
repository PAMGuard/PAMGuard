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
package Spectrogram;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.Timer;

public class ScalePanel extends JPanel {
	Image bgImage;

	Graphics2D overlayG2d;

	JPanel imagePanel;

	public ScalePanel(Image bgImage) {
		this.bgImage = bgImage;
		imagePanel = new JPanel();
	}

	boolean direction;

	// PRIVATE
	private int topBorder = 0; // border above image

	private int bottomBorder = 0; // border below image

	private double scaleWidthfactor = 1.0; // 0.8; // determines the width of
											// the image

	// representation relative to the frame
	private double scaleX;

	private double scaleY;

	private double barPosition = 0;

	private int PlotRectangleLHS;

	private int PlotRectangleRHS;

	private int PlotRectangleTop;

	private int PlotRectangleBottom;

	// this.get
	Timer t = new Timer(100, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {

			// paintImmediately(getBounds());
		}
	});

	void setBarPosition(double currentBarPos) {
		barPosition = currentBarPos;
	}

	private void setPlotSizes() {
		PlotRectangleLHS = (int) (scaleX * ((1 - scaleWidthfactor) / 2) * bgImage
				.getWidth(null));
		PlotRectangleRHS = (PlotRectangleLHS + (int) (scaleWidthfactor * scaleX * this.bgImage
				.getWidth(null)));
		PlotRectangleTop = topBorder;
		PlotRectangleBottom = topBorder
				+ (int) (bgImage.getHeight(null) * scaleY);
	}

	float stringWidth, stringHeight;

	int squareXpos = 0;

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		scaleX = this.getWidth() / (double) bgImage.getWidth(null);
		scaleY = (this.getHeight() - (topBorder + bottomBorder))
				/ (double) bgImage.getHeight(null);
		AffineTransform xform = new AffineTransform();
		xform.translate(scaleX * ((1 - scaleWidthfactor) / 2)
				* bgImage.getWidth(null), topBorder);
		xform.scale(scaleX * scaleWidthfactor, scaleY);
		g2d.drawImage(bgImage, xform, this);

	}

}
