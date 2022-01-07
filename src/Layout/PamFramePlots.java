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

import java.awt.Component;
import javax.swing.JMenuItem;

import PamView.ClipboardCopier;
/**
 * 
 * @author Doug Gillespie
 *<p>
 *Abstract class PamFramePlots is used to layout graphics windows which have
 *a border (generally grey) and some kind of inner display panel. Normally the outer 
 *panel will contain the axis and the inner panel any other graphics, such as a spectrogram.
 *<p> For an example see SpectrogramDisplay which is a subclass of PamFramePlots
 *SpectrogramDisplay sets axisPanel to a SpectrogramAxis, which is in turn subclassed from PamAxisPanel.
 *it also sets plotPanel to SpectrogramPlotPanel, which is a subclass of JPanel and it sets 
 *eastPanel to AmplitudePanel which is used for drawing a colour bar representing amplitude.
 *
 *@see Layout.PamInternalFrame
 *@see Layout.PamAxisPanel
 *@see Layout.PamAxis
 *@see java.awt.BorderLayout
 *@see Spectrogram.SpectrogramDisplay
 */
abstract public class PamFramePlots {

	/**
	 * The panel filling the entire PamInternalFrame
	 */
	private Component axisPanel;

	/**
	 * Inner panel nested inside axisPanel with a border large
	 * enough to contain any required axis.
	 * @see java.awt.BorderLayout
	 */
	private Component plotPanel;

	/**
     * Left inner panel of a dual display nested inside axisPanel.  A dual
     * display has a left and right plot panel, which share a common vertical
     * axis
     * UNTESTED - use at your own risk
     */
    private Component leftPlotPanel = null;

    /**
     * Right inner panel of a dual display nested inside axisPanel.  A dual
     * display has a left and right plot panel, which share a common vertical
     * axis
     * UNTESTED - use at your own risk
     */
    private Component rightPlotPanel = null;

	/**
	 * @see java.awt.BorderLayout
	 */
	private Component northPanel;

	/**
	 * @see java.awt.BorderLayout
	 */
	private Component southPanel;

	/**
	 * @see java.awt.BorderLayout
	 */
	private Component eastPanel;

	/**
	 * @see java.awt.BorderLayout
	 */
	private Component westPanel;

	private PamInternalFrame pamFrame;

	/**
	 * Get a unique name for the display
	 * @return a unique name for the display. 
	 */
	abstract public String getName();
	
	private ClipboardCopier clipboardCopier;


	public PamInternalFrame getFrame() {
		return pamFrame;
	}
	
	public JMenuItem getCopyMenuItem() {
		if (clipboardCopier == null) {
			return null;
		}
		return clipboardCopier.getCopyMenuItem();
	}
	
	public void setFrame(PamInternalFrame pamFrame) {

		this.pamFrame = pamFrame;
		
		clipboardCopier = new ClipboardCopier(pamFrame);

		
	}
	
	/**
	 * Repaint all windows in the frame plot with delay tm
	 * @param tm time delay in millis
	 */
	public void repaint(int tm) {
		if (axisPanel != null) axisPanel.repaint(tm);
		if (plotPanel != null) plotPanel.repaint(tm);
		if (northPanel != null) northPanel.repaint(tm);
		if (southPanel != null) southPanel.repaint(tm);
		if (eastPanel != null) eastPanel.repaint(tm);
		if (westPanel != null) westPanel.repaint(tm);
        if (leftPlotPanel != null) leftPlotPanel.repaint(tm);
        if (rightPlotPanel != null) rightPlotPanel.repaint(tm);
	}
		
    public boolean checkDualDisplay() {
        if (plotPanel == null && leftPlotPanel != null && rightPlotPanel != null) {
            return true;
        } else {
            return false;
        }
    }

	public Component getAxisPanel() {
		return axisPanel;
	}

	public void setAxisPanel(Component axisPanel) {
		this.axisPanel = axisPanel;
	}

	public Component getEastPanel() {
		return eastPanel;
	}

	public void setEastPanel(Component eastPanel) {
		this.eastPanel = eastPanel;
	}

	public Component getNorthPanel() {
		return northPanel;
	}

	public void setNorthPanel(Component northPanel) {
		this.northPanel = northPanel;
	}

	public Component getPlotPanel() {
		return plotPanel;
	}

	public void setPlotPanel(Component plotPanel) {
		this.plotPanel = plotPanel;
	}

	public Component getSouthPanel() {
		return southPanel;
	}

	public void setSouthPanel(Component southPanel) {
		this.southPanel = southPanel;
	}

	public Component getWestPanel() {
		return westPanel;
	}

	public void setWestPanel(Component westPanel) {
		this.westPanel = westPanel;
	}
	
    public Component getLeftPlotPanel() {
        return leftPlotPanel;
}

    public void setLeftPlotPanel(Component leftPlotPanel) {
        this.leftPlotPanel = leftPlotPanel;
    }

    public Component getRightPlotPanel() {
        return rightPlotPanel;
    }

    public void setRightPlotPanel(Component rightPlotPanel) {
        this.rightPlotPanel = rightPlotPanel;
    }

}
