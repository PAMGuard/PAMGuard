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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import PamView.PamColors;
import PamView.PamColors.PamColor;

public class DisplayPanZoom extends JPanel {

	private PanZoomMouseAdapter mouseAdapter = new PanZoomMouseAdapter();

	private PanZoomBehaviour handler;

	private JButton upLeft;

	private JButton up;

	private JButton upRight;

	private JButton left;

	private JButton right;

	private JButton downLeft;

	private JButton down;

	private JButton downRight;

	private JButton zoomIn;

	private JButton zoomOut;

	private JButton headUp;

	private JButton northUp;

	private JButton centreOnShip;

	private JButton rotateClockwise;

	private JButton rotateAntiClockwise;
	
	private JButton panWithMouse, measureWithMouse;
	
	/**
	 * 
	 */
	public DisplayPanZoom() {
		super();

		this.setOpaque(false);
		this.setLayout(new GridLayout(3, 3));
//		this.setBorder(BorderFactory.createRaisedBevelBorder());
		this.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));


		// Create all buttons and add tool tips
		zoomIn = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomZoomIn.png")));
		zoomIn.setForeground(new Color(50,50,50,150));
		// zoomIn = new JButton("", new ImageIcon("PamPanZoomZoomIn.png"));
		zoomIn.setToolTipText("zoom in");
//		zoomIn.setSelected(true);
		
		zoomOut = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomZoomOut.png")));
		// zoomOut = new JButton("", new ImageIcon("PamPanZoomZoomOut.png"));
		zoomOut.setToolTipText("zoom out");

		upLeft = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomUpLeft.png")));
		// upLeft = new JButton("", new ImageIcon("PamPanZoomUpLeft.png"));
		upLeft.setToolTipText("pan up-left");

		up = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomUp.png")));
		// up = new JButton("", new ImageIcon("PamPanZoomUp.png"));
		up.setToolTipText("pan up");

		upRight = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomUpRight.png")));
		// upRight = new JButton("", new ImageIcon("PamPanZoomUpRight.png"));
		upRight.setToolTipText("pan up-right");

		left = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomLeft.png")));
		// left = new JButton("", new ImageIcon("PamPanZoomLeft.png"));
		left.setToolTipText("pan left");

		right = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomRight.png")));
		// right = new JButton("", new ImageIcon("PamPanZoomRight.png"));
		right.setToolTipText("pan right");

		downLeft = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomDownLeft.png")));
		// downLeft = new JButton("", new ImageIcon("PamPanZoomDownLeft.png"));
		downLeft.setToolTipText("pan down-left");

		down = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomDown.png")));
		// down = new JButton("", new ImageIcon("PamPanZoomDown.png"));
		down.setToolTipText("pan down");

		downRight = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomDownRight.png")));
		// downRight = new JButton("", new
		// ImageIcon("PamPanZoomDownRight.png"));
		downRight.setToolTipText("pan down-right");

		headUp = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomHeadUp.png")));
		// headUp = new JButton("",new ImageIcon("PamPanZoomHeadUp.png"));
		headUp.setToolTipText("rotate map to head-up and flatten");

		northUp = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomNorthUp.png")));
		// northUp = new JButton("",new ImageIcon("PamPanZoomNorthUp.png"));
		northUp.setToolTipText("rotate map to north-up and flatten");

		centreOnShip = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomCentreOnShip.png")));
		// centreOnShip = new JButton("",new
		// ImageIcon("PamPanZoomCentreOnShip.png") );
		centreOnShip.setToolTipText("centre map on ship");

		rotateClockwise = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomRotateClockwise.png")));
		// rotateClockwise = new JButton("",new
		// ImageIcon("PamPanZoomRotateClockwise.png"));
		rotateClockwise.setToolTipText("rotate map clockwise");

		rotateAntiClockwise = new MapButton("", new ImageIcon(ClassLoader
				.getSystemResource("Resources/PamPanZoomRotateAnticlockwise.png")));
		// rotateAntiClockwise = new JButton("",new
		// ImageIcon("PamPanZoomRotateAnticlockwise.png"));
		rotateAntiClockwise.setToolTipText("rotate map anti-clockwise");
		

		panWithMouse = new MapButton(new ImageIcon(ClassLoader
				.getSystemResource("Resources/PanWithMouse.png")));
		panWithMouse.setToolTipText("Drag display with mouse");
		measureWithMouse = new MapButton(new ImageIcon(ClassLoader
				.getSystemResource("Resources/MeasureWithMouse.png")));
		measureWithMouse.setToolTipText("Measure distance and bearing with mouse");
		ButtonGroup mouseButtonGroup = new ButtonGroup();
		mouseButtonGroup.add(panWithMouse);
		mouseButtonGroup.add(measureWithMouse);
		panWithMouse.setSelected(true);

		// Add listener to all buttons
		upLeft.addMouseListener(mouseAdapter);
		up.addMouseListener(mouseAdapter);
		upRight.addMouseListener(mouseAdapter);
		left.addMouseListener(mouseAdapter);
		right.addMouseListener(mouseAdapter);
		downLeft.addMouseListener(mouseAdapter);
		down.addMouseListener(mouseAdapter);
		downRight.addMouseListener(mouseAdapter);
		zoomIn.addMouseListener(mouseAdapter);
		zoomOut.addMouseListener(mouseAdapter);
		rotateClockwise.addMouseListener(mouseAdapter);
		rotateAntiClockwise.addMouseListener(mouseAdapter);
		northUp.addMouseListener(mouseAdapter);
		headUp.addMouseListener(mouseAdapter);
		centreOnShip.addMouseListener(mouseAdapter);
		panWithMouse.addMouseListener(mouseAdapter);
		measureWithMouse.addMouseListener(mouseAdapter);

		// Add button to JPanel using grid layout
		// *Note* order is important.
//		add(upLeft);
//		add(up);
//		add(upRight);
//		add(left);
//		add(centreOnShip);
//		add(right);
//		add(downLeft);
//		add(down);
//		add(downRight);
		add(rotateClockwise);
		add(zoomIn);
		add(headUp);
		add(rotateAntiClockwise);
		add(zoomOut);
		add(northUp);
		add(panWithMouse);
		add(measureWithMouse);
		add(centreOnShip);
		
		setPreferredSize(new Dimension(3 * 48 + 10, 3 * 48 + 10));

	}
	
	private class MapButton extends JButton {
		
		Color background=new Color(222,222,222,220);
		Color highlight=new Color(0,222,222,180);
		
		public MapButton(String string, ImageIcon imageIcon) {
			super(string,imageIcon);
//			this.setOpaque(false);
			this.setContentAreaFilled(false);

		}

		public MapButton(ImageIcon imageIcon) {
			super(imageIcon);
			this.setContentAreaFilled(false);
		}

		@Override
		public void paintComponent(Graphics g) {
				if (getModel().isRollover()){
					g.setColor(highlight);
				}
				else{
					g.setColor(background);
				}
		        Rectangle r = g.getClipBounds();
		        g.fillRect(r.x, r.y, r.width, r.height);
		        super.paintComponent(g);
		}
		
		
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
	        g.setColor(getBackground());
	        Rectangle r = g.getClipBounds();
	        g.fillRect(r.x, r.y, r.width, r.height);
	        super.paintComponent(g);
	}

	class PanZoomMouseAdapter extends MouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			// System.out.println("mouseReleased");
			handler.handleMBReleased();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// System.out.println("mousePressed");
			if (e.getButton() == MouseEvent.BUTTON1) {

				if (e.getSource() == upLeft) {
					// System.out.println("source == upLeft");
					handler.handleUpLeft();
				}
				if (e.getSource() == up) {
					// System.out.println("source == up");
					handler.handleUp();
				}
				if (e.getSource() == upRight) {
					// System.out.println("source == upRight");
					handler.handleUpRight();
				}
				if (e.getSource() == left) {
					// System.out.println("source == left");
					handler.handleLeft();
				}
				if (e.getSource() == right) {
					// System.out.println("source == right");
					handler.handleRight();
				}
				if (e.getSource() == downLeft) {
					// System.out.println("source == downLeft");
					handler.handleDownLeft();
				}
				if (e.getSource() == down) {
					// System.out.println("source == down");
					handler.handleDown();
				}
				if (e.getSource() == downRight) {
					// System.out.println("source == downRight");
					handler.handleDownRight();
				}
				if (e.getSource() == zoomIn) {
					// System.out.println("source == zoomIn");
					handler.handleZoomIn();
				}
				if (e.getSource() == zoomOut) {
					// System.out.println("source == zoomOut");
					handler.handleZoomOut();
				}
				if (e.getSource() == rotateClockwise) {
					// System.out.println("source == rotateClockwise");
					handler.handleRotateClockwise();
				}
				if (e.getSource() == rotateAntiClockwise) {
					// System.out.println("source == rotateAntiClockwise");
					handler.handleRotateAntiClockwise();
				}

				if (e.getSource() == northUp) {
					// System.out.println("source == northUp");
					handler.handleNorthUp();
				}
				if (e.getSource() == headUp) {
					// System.out.println("source == headUp");
					handler.handleHeadUp();
				}
				if (e.getSource() == centreOnShip) {
					// System.out.println("source == centreOnShip");
					handler.handleCentreOnShip();
				}
				if (e.getSource() == measureWithMouse) {
					// System.out.println("source == centreOnShip");
					handler.handleMeasureWithMouse();
				}
				if (e.getSource() == panWithMouse) {
					// System.out.println("source == centreOnShip");
					handler.handlePanWithMouse();
				}
			} // button1
		} // mousePressed
	} // PanZoomMouseAdapter

	public PanZoomBehaviour getHandler() {
		return handler;
	}

	public void setHandler(PanZoomBehaviour handler) {
		this.handler = handler;
	}
} // DisplayPanZoom
