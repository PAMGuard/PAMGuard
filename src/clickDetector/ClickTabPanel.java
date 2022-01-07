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

package clickDetector;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JDesktopPane;

import PamguardMVC.PamObservable;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         ClickTabPanel is what ultimately get's taken by the PamGui and added
 *         to the main tab panel. ClickTabPanel contains several different
 *         windows to show bearings, trigger levels, click waveforms, etc. These
 *         are automatically arranged by ClickTabPanel whenever the main window
 *         resizes.
 * 
 * @see PamView.PamGui
 */
public class ClickTabPanel extends JDesktopPane implements ComponentListener {
	
	ClickControl clickControl;
	
	ClickTabPanelControl clickTabPanelControl;
	
	float sampleRate;
	
	private int resizeCount = 0;
	
	private boolean firstMaximise = true;
	
	public ClickTabPanel(ClickTabPanelControl clickTabPanelControl, ClickControl clickControl) {
		
		this.clickTabPanelControl = clickTabPanelControl;
		
		this.clickControl = clickControl;
				
		addComponentListener(this);
		
	}
	
	
	public void componentHidden(ComponentEvent e) {
//		System.out.println("componentHidden");
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentResized(ComponentEvent e) {
		// if (++resizeCount < 5) {
		arrangeWindows();
		// }
	}
	
	public void componentShown(ComponentEvent e) {
	}
	
	public void arrangeWindows() {
		Dimension d = getSize();
		
		double r = .6;
		double r1 = 1 - r;
		
		int smallWindows = 0;
		
		ArrayList<ClickDisplay> dw = clickControl.tabPanelControl.clickDisplayManager.getWindowList();
		
		if (dw.size() == 0) return;
		
		for (int i = 0; i < dw.size(); i++) {
			if (dw.get(i).getClickDisplayInfo().isSmallWindow()) {
				smallWindows++;
			}
		}
		int largeWindows = dw.size() - smallWindows;

		int x, y, w, h = 0;
		if (largeWindows > 0) {
			x = 0;
			y = 0;
			w = (d.width / largeWindows);
			if (smallWindows == 0) {
				h = d.height;
			}
			else {
				h = (int) (d.height * r);
			}
			for (int i = 0; i < dw.size(); i++) {
				if (dw.get(i).getClickDisplayInfo().isSmallWindow() == true) continue;
				dw.get(i).getFrame().setLocation(x, y);
				dw.get(i).getFrame().setSize(w, h);
				x += w;
			}
		}
		
		if (smallWindows > 0) {
			x = 0;
			y = h;
			w = (d.width / smallWindows);
			if (largeWindows > 0) {
				h = d.height - h;
			}
			else {
				h = d.height;
			}
			for (int i = 0; i < dw.size(); i++) {
				if (dw.get(i).getClickDisplayInfo().isSmallWindow() == false) continue;
				dw.get(i).getFrame().setLocation(x, y);
				dw.get(i).getFrame().setSize(w, h);
				x += w;
			}
		}
//		
//		btDisplay.getFrame().setSize(
//		new Dimension(d.width, (int) (d.height * r)));
//		
//		waveformDisplay.getFrame().setSize(
//		new Dimension(d.width / 3, (int) (d.height * r1)));
//		waveformDisplay.getFrame().setLocation(0, (int) (d.height * r));
//		
//		spectrumDisplay.getFrame().setSize(
//		new Dimension(d.width / 3, (int) (d.height * r1)));
//		spectrumDisplay.getFrame().setLocation(d.width / 3, (int) (d.height * r));
//		
//		triggerDisplay.getFrame().setSize(
//		new Dimension(d.width - d.width * 2/3, (int) (d.height * r1)));
//		triggerDisplay.getFrame()
//		.setLocation(d.width * 2 / 3, (int) (d.height * r));
	}
	
	public void noteNewSettings() {
//		btDisplay.noteNewSettings();
//		waveformDisplay.NoteNewSettings();
//		triggerDisplay.noteNewSettings();
//		spectrumDisplay.NoteNewSettings();
		// get a list of all children, which should all be
		// subclasses of ClickDisplay
		// and tell them about new settings ...
		ArrayList<ClickDisplay> dw = clickControl.tabPanelControl.clickDisplayManager.getWindowList();
		for (int i = 0; i < dw.size(); i++) {
			dw.get(i).noteNewSettings();
		}
	}
	
	public String getObserverName() {
		return "Tab panel for click detector";
	}
	
	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub
		
	}


//	/** 
//	 * Called when an offline store has opened or closed. 
//	 */
//	public void newOfflineStore() {
//		ArrayList<ClickDisplay> dw = clickControl.tabPanelControl.clickDisplayManager.getWindowList();
//		for (int i = 0; i < dw.size(); i++) {
//			dw.get(i).newOfflineStore();
//		}
//	}


	/** 
	 * Called from clicksOffline when data have changed (eg from re doing click id). 
	 * Needs to notify the display and maybe some other classes. 
	 */
	public void offlineDataChanged() {
		ArrayList<ClickDisplay> dw = clickControl.tabPanelControl.clickDisplayManager.getWindowList();
		for (int i = 0; i < dw.size(); i++) {
			dw.get(i).offlineDataChanged();
		}		
	}
}