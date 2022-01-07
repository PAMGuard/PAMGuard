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
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

/**
 * 
 * @author Doug Gillespie
 * <p>
 * PamAxisPanel is used to lay out a panel with surrounding axis. 
 * <p>
 * 
 *
 */
public class PamAxisPanel extends JPanel implements ColorManaged {

	/**
	 * axis to be displayed on any or all of the four sides of
	 * a plot window (innerPanel below)
	 */
	private PamAxis northAxis, southAxis, westAxis, eastAxis;

	private int minNorth, minSouth, minEast, minWest;

	private Border border;
	
	private JLabel titleLabel;

	/**
	 * The main plot panel that the axis are drawn around.
	 */
	private JPanel innerPanel;
	
	/**
	 * If the plot panel contains border components such as a scroll
	 * bar, then the axis may need to be drawn relative to a slightly
	 * different panel. Plot panel is basically a component of 
	 * innerPanel and if it is not null, axis will be drawn outside the
	 * inner panel, but lined up with the plotPanel.
	 */
	private JComponent plotPanel;
	
	/**
     * Define the left plot panel for a dual-display.
     * A dual-display frame has 2 plot panels beside each other (left and right).
     * The plot panels typically share a common vertical axis.
     * UNTESTED - use at your own risk
     */
    private JComponent leftPlotPanel = null;

    /**
     * Define the right plot panel for a dual-display.
     * A dual-display frame has 2 plot panels beside each other (left and right).
     * The plot panels typically share a common vertical axis.
     * UNTESTED - use at your own risk
     */
    private JComponent rightPlotPanel = null;

    /**
     * Boolean indicating whether or not the axisPanel holds a dual display.
     * A dual-display frame has 2 plot panels beside each other (left and right).
     * The plot panels typically share a common vertical axis.
     * Default to false so that existing classes don't get screwed up.
     * UNTESTED - use at your own risk
     */
	private boolean dualDisplay = false;

	/**
	 * @return the plotPanel or if the plotPanel is
	 * null, return the innerPanel
	 */
	public JComponent getPlotPanel() {
		if (plotPanel == null && !dualDisplay) {
			return innerPanel;
		}
		else {
			return plotPanel;
		}
	}

	/**
	 * @param plotPanel the plotPanel to set
	 * Note that the plot panel is not necessarily the same as the inner
	 * panel which is the main component held within the axis panel. The actual 
	 * plot panel may be smaller than the inner panel since the inner panel may
	 * contain scroll bars or other components around the plot panel. 
	 */
	public void setPlotPanel(JComponent plotPanel) {
		this.plotPanel = plotPanel;
	}

    /**
     * Return the left plot panel
     * @return leftPlotPanel object
     */
    public JComponent getLeftPlotPanel() {
        return leftPlotPanel;
    }

    /**
     * Note that in the case of dual displays, there is no separately-defined
     * inner panel;
     * @param leftPlotPanel the left plotPanel to set
     */
    public void setLeftPlotPanel(JComponent leftPlotPanel) {
        this.leftPlotPanel = leftPlotPanel;
    }

    /**
     * Return the right plot panel
     * @return leftPlotPanel object
     */
    public JComponent getRightPlotPanel() {
        return rightPlotPanel;
    }

    /**
     * Note that in the case of dual displays, there is no separately-defined
     * inner panel;
     * @param leftPlotPanel the left plotPanel to set
     */
    public void setRightPlotPanel(JComponent rightPlotPanel) {
        this.rightPlotPanel = rightPlotPanel;
    }

	private boolean autoInsets = false;

	public PamAxisPanel() {
		super();
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setLayout(new BorderLayout());
	}

    /**
     * Overloaded constructor, to handle dual-display frames.  A dual-display
     * frame has 2 plot panels beside each other (left and right).  The plot
     * panels typically share a common vertical axis.  See IDI_Display class
     * for examples.
     * UNTESTED - use at your own risk
     *
     * @param dualDisplay true if the frame is dual-display
     */
	public PamAxisPanel(boolean dualDisplay) {
		super();
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setLayout(new BorderLayout());

        this.dualDisplay = dualDisplay;
	}

	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		/*
		 * Check that there is enough space for each of up to four axis and draw
		 * it.
		 */
		Rectangle r = getBounds();
		Insets currentInsets = getInsets();
		if (autoInsets) setAutoInsets(g);
		Insets newInsets = new Insets(minNorth, minWest, minSouth, minEast);
		Insets plotInsets;
		JComponent innerComponent = getInnerPanel();
        JComponent plotComponent = null;
        JComponent leftPlotComponent = null;
        JComponent rightPlotComponent = null;
		
        if (dualDisplay) {
            leftPlotComponent = getLeftPlotPanel();
            rightPlotComponent = getRightPlotPanel();
        } else {
            plotComponent = getPlotPanel();
        }

		if (innerComponent != null) {
			plotInsets = innerComponent.getInsets();
		} 
		else {
			plotInsets = new Insets(0, 0, 0, 0);
		}
		if (northAxis != null) {
			newInsets.top = Math.max(newInsets.top, northAxis.getExtent(g));
		}
		if (westAxis != null) {
			newInsets.left = Math.max(newInsets.left, westAxis.getExtent(g));
		}
		if (southAxis != null) {
			newInsets.bottom = Math.max(newInsets.bottom, southAxis
					.getExtent(g));
		}
		if (eastAxis != null) {
			newInsets.right = Math.max(newInsets.right, eastAxis.getExtent(g));
		}
		if (!currentInsets.equals(newInsets)) {
			setBorder(new EmptyBorder(newInsets));
		}
		
		/*
		 * The actual drawing of axes is around the plotPanel, not the innerPanel
		 */
		if (plotPanel != null && innerComponent != null) {
			Rectangle plotBounds = plotPanel.getBounds();
			newInsets.left += plotBounds.x;
			newInsets.top += plotBounds.y;
			newInsets.right += (innerComponent.getWidth() - plotBounds.width - plotBounds.x);
			newInsets.bottom += (innerComponent.getHeight() - plotBounds.height - plotBounds.y);
		}
		if (plotComponent != null) {
			Insets moreInsets = plotComponent.getInsets();
			if (moreInsets != null) {
				newInsets.left += moreInsets.left;
				newInsets.right += moreInsets.right;
				newInsets.bottom += moreInsets.bottom;
				newInsets.top += moreInsets.top;
			}
		}

		if (leftPlotComponent != null && rightPlotComponent != null) {
			Insets leftInsets = leftPlotComponent.getInsets();
            Insets rightInsets = rightPlotComponent.getInsets();
			if (leftInsets != null && rightInsets != null) {
				newInsets.left += leftInsets.left;
				newInsets.right += rightInsets.right;
				newInsets.bottom += leftInsets.bottom;
				newInsets.top += leftInsets.top;
			}
		}
		drawNorthAxis(g, newInsets);
		drawSouthAxis(g, newInsets);
		drawEastAxis(g, newInsets);
		drawWestAxis(g, newInsets);

	}
	/**
	 * Draw the North Axis
	 * @param g graphics object
	 * @param insets insets of the inner (plot) windows. 
	 */
	protected void drawNorthAxis(Graphics g, Insets insets) {
		int panelRight = getWidth() - insets.right;
		if (northAxis != null) {
			northAxis.drawAxis(g, insets.left, insets.top, panelRight,
					insets.top);
		}
	}
	/**
	 * Draw the South Axis
	 * @param g graphics object
	 * @param insets insets of the inner (plot) windows. 
	 */
	protected void drawSouthAxis(Graphics g, Insets insets) {
		int panelRight = getWidth() - insets.right;
		int panelBottom = getHeight() - insets.bottom;
		if (southAxis != null) {
			southAxis.drawAxis(g, insets.left, panelBottom, panelRight,
					panelBottom);
		}
	}
	/**
	 * Draw the East Axis
	 * @param g graphics object
	 * @param insets insets of the inner (plot) windows. 
	 */
	protected void drawEastAxis(Graphics g, Insets insets) {
		int panelRight = getWidth() - insets.right;
		int panelBottom = getHeight() - insets.bottom;
		if (eastAxis != null) {
			eastAxis.drawAxis(g, panelRight, panelBottom, panelRight,
					insets.top);
		}
	}
	/**
	 * Draw the West Axis
	 * @param g graphics object
	 * @param insets insets of the inner (plot) windows. 
	 */
	protected void drawWestAxis(Graphics g, Insets insets) {
		int panelBottom = getHeight() - insets.bottom;
		if (westAxis != null) {
			westAxis.drawAxis(g, insets.left, panelBottom, insets.left,
					insets.top);
		}
	}
	

	/**
	 * Work out how much space is required around the central plot
	 * for axis. 
	 */
	public void setAutoInsets(Graphics g) {
		int minInset = 1;
		minSouth = minInset;
		minNorth = minInset;
		minWest = minInset;
		minEast = minInset;
		if (eastAxis != null){
			minEast = Math.max(minEast, eastAxis.getExtent(g));
			minSouth = Math.max(minSouth, eastAxis.getExtent2(g));
			minNorth = Math.max(minNorth, eastAxis.getExtent2(g));
		}
		if (southAxis != null){
			minSouth = Math.max(minSouth, southAxis.getExtent(g));
			minWest = Math.max(minWest, southAxis.getExtent2(g));
			minEast = Math.max(minEast, southAxis.getExtent2(g));
		}
		
		if (northAxis != null){
			minNorth = Math.max(minNorth, northAxis.getExtent(g));
			minWest = Math.max(minWest, northAxis.getExtent2(g));
			minEast = Math.max(minEast, northAxis.getExtent2(g));
		}
		if (westAxis != null){
			minWest = Math.max(minWest, westAxis.getExtent(g));
			minSouth = Math.max(minSouth, westAxis.getExtent2(g));
			minNorth = Math.max(minNorth, westAxis.getExtent2(g));
		}
	}
	
	public void SetBorderMins(int minNorth, int minWest, int minSouth,
			int minEast) {
		this.minNorth = minNorth;
		this.minSouth = minSouth;
		this.minWest = minWest;
		this.minEast = minEast;
	}

	public JPanel getInnerPanel() {
		return innerPanel;
	}

	public void setInnerPanel(JPanel innerPanel) {
		this.innerPanel = innerPanel;
		add(BorderLayout.CENTER, innerPanel);
		
	}

	public PamAxis getEastAxis() {
		return eastAxis;
	}

	public void setEastAxis(PamAxis eastAxis) {
		this.eastAxis = eastAxis;
	}

	public PamAxis getNorthAxis() {
		return northAxis;
	}

	public void setNorthAxis(PamAxis northAxis) {
		this.northAxis = northAxis;
	}

	public PamAxis getSouthAxis() {
		return southAxis;
	}

	public void setSouthAxis(PamAxis southAxis) {
		this.southAxis = southAxis;
	}

	public PamAxis getWestAxis() {
		return westAxis;
	}

	public void setWestAxis(PamAxis westAxis) {
		this.westAxis = westAxis;
	}

	public boolean isAutoInsets() {
		return autoInsets;
	}

	public void setAutoInsets(boolean autoInsets) {
		this.autoInsets = autoInsets;
	}

	public int getMinEast() {
		return minEast;
	}

	public void setMinEast(int minEast) {
		this.minEast = minEast;
	}

	public int getMinNorth() {
		return minNorth;
	}

	public void setMinNorth(int minNorth) {
		this.minNorth = minNorth;
	}

	public int getMinSouth() {
		return minSouth;
	}

	public void setMinSouth(int minSouth) {
		this.minSouth = minSouth;
	}

	public int getMinWest() {
		return minWest;
	}

	public void setMinWest(int minWest) {
		this.minWest = minWest;
	}
}
