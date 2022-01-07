package effortmonitor.swing;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ListIterator;
import java.util.Vector;

import PamguardMVC.PamDataBlock;
import dataMap.BespokeDataMapGraphic;
import dataMap.DataStreamPanel.DataGraph;
import effortmonitor.EffortControl;
import effortmonitor.EffortDataUnit;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractScrollManager;

public class EffortDataMapGraph implements BespokeDataMapGraphic {

	
	private EffortControl effortControl;

	public EffortDataMapGraph(EffortControl effortControl) {
		this.effortControl = effortControl;
	}

	@Override
	public void paint(Graphics g, DataGraph dataGraph) {
		int h = dataGraph.getHeight();
		/*
		 * Draw separate bars for all scrollers. 
		 */
		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
		Vector<AbstractPamScroller> allScrollers = scrollManager.getPamScrollers();
		if (allScrollers == null) {
			return;
		}
		PamDataBlock<EffortDataUnit> dataBlock = effortControl.getEffortDataBlock();
		AbstractPamScroller lastScroller = null;
		int nScrollers = allScrollers.size();
		int lastScrollerindex = 0;
		double hPerScroller = (double) h / (double) nScrollers;
		g.setColor(Color.LIGHT_GRAY);
		int[] xText = new int[allScrollers.size()+1];
		int[] yText = new int[allScrollers.size()+1];
		synchronized (dataBlock.getSynchLock()) {
			ListIterator<EffortDataUnit> it = dataBlock.getListIterator(0);
			while (it.hasNext()) {
				EffortDataUnit dataUnit = it.next();
				AbstractPamScroller effortScroller = dataUnit.getScroller();
//				if (effortScroller != lastScroller) {
					lastScrollerindex = allScrollers.indexOf(effortScroller) + 1;
					lastScroller = effortScroller;
//				}
				int y1 = (int) Math.round(lastScrollerindex * hPerScroller);
				int y2 = (int) Math.round((lastScrollerindex+1) * hPerScroller);
				int x1 = dataGraph.getXCoord(dataUnit.getTimeMilliseconds());
				int x2 = dataGraph.getXCoord(dataUnit.getEndTimeInMilliseconds());
				FontMetrics fm = g.getFontMetrics();
				if (!(x1>dataGraph.getWidth() || x2 < 0)) {
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(x1, y1, x2-x1, y2-y1);
					g.setColor(Color.DARK_GRAY);
					g.drawRect(x1, y1, x2-x1, y2-y1);
					if (yText[lastScrollerindex] == 0) {
						xText[lastScrollerindex] = Math.max(0, x1);
						yText[lastScrollerindex] = (y1+y2)/2+fm.getAscent()/2;
					}
				}
				// now draw the text on top ...
				g.setColor(Color.BLACK);
				for (int i = 0; i < xText.length; i++) {
					if (yText[i] == 0) {
						continue;
					}
					if (i > 0) {
						lastScroller = allScrollers.get(i-1);
						g.drawString(" " + lastScroller.getScrollerData().getName(), xText[i], yText[i]);
					}
					else {
						g.drawString(" Unknown scroller(s)", xText[i], yText[i]);
					}
					
				}
			}
		}
	}

}
