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
package Layout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.border.EmptyBorder;

import PamView.PamIcon;

/**
 * @author Doug Gillespie
 *         <p>
 *         To make lots of pretty similar looking internal frames with an inner
 *         panel of graphics that sizes sensible, create a few subclasses of
 *         JInternalFrame with appropriate JPanels that handle all the bordering
 *         and sizing functions.
 *         <p>
 *         The layout functionality of this has now been moved to PamGraphLayout. 
 *         <p>
 *         The constructor requires a subclass of the abstract PamFramePlots. 
 *         PamFramePlots contains references to the main components making up a 
 *         Java border layout. 
 *         <p> For an example see SpectrogramDisplay which is a subclass of PamFramePlots
 *         SpectrogramDisplay sets 
 * 
 */
public class PamInternalFrame extends JInternalFrame implements ComponentListener {

//	/**
//	 * axisPanel is the main (outer) panel that fills the entire 
//	 * JInternalFrame centre
//	 */
//	private JPanel axisPanel;
//
//	/**
//	 * plotPanel is the inner panel containing various the actual plot,
//	 * e.g. a spectrogram, bearing time display, waveform, etc. 
//	 */
//	private JPanel plotPanel;
//
//	/**
//	 * in the case of a dual display, where there are two plot panels beside each
//	 * other sharing a common vertical axis, define left and right panel objects
//	 * UNTESTED - use at your own risk
//	 */
//	private JPanel leftPlotPanel, rightPlotPanel;


	protected EmptyBorder emptyBorder;

	private Dimension lastSize = new Dimension();

	private PamFramePlots framePlots;

	private PamGraphLayout graphLayout;

	public PamInternalFrame(PamFramePlots pamFramePlots, boolean canClose) {

		super(pamFramePlots.getName(), true, canClose, true, true);

		framePlots = pamFramePlots;

		pamFramePlots.setFrame(this);

		setFrameIcon(PamIcon.getPAMGuardImageIcon(PamIcon.SMALL));
		
		graphLayout = new PamGraphLayout(framePlots);
		
		this.add(BorderLayout.CENTER, graphLayout.getMainComponent());

		setSize(900, 400);

		setVisible(true);

		addComponentListener(this);
	}

	void setBorderSize(EmptyBorder newBorder) {

		emptyBorder = newBorder;

	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	public PamFramePlots getFramePlots() {
		return framePlots;
	}
//
//	@Override
//	public PamColor getColorId() {
//		return PamColor.BORDER;
//	}
//
//	@Override
//	public void setForeground(Color fg) {
////		// TODO Auto-generated method stub
//		super.setForeground(fg);
//	}
//
//	@Override
//	public void setBackground(Color bg) {
////		// TODO Auto-generated method stub
//		super.setBackground(bg);
//		Border border = this.getBorder();
//		if (border != null) {
//			System.out.println(border.toString());
//		}
//	}

}
