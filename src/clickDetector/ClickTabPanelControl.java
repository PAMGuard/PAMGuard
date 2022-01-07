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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import PamView.PamTabPanel;
import clickDetector.dialogs.ClickMapDialog;
import clickDetector.dialogs.OverlayOptionsDialog;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Tab Panel controller for the click detector.
 * @see clickDetector.ClickControl
 * @see clickDetector.ClickTabPanel
 */
public class ClickTabPanelControl implements PamTabPanel {

	ClickTabPanel clickPanel;
	
	/**
	 * Main ClickControl instance
	 */
	ClickControl clickControl;

	private JPanel plainPanel;
	
//	JScrollPane scrollPane;
	
//	ClickToolBar clickToolBar;
	
	ClickDisplayManager clickDisplayManager;

	ClickTabPanelControl(ClickControl clickControl) {

		this.clickControl = clickControl;
		
		clickDisplayManager = new ClickDisplayManager(clickControl, this);

		clickPanel = new ClickTabPanel(this, clickControl);

//		scrollPane = new JScrollPane(clickPanel);
		
//		clickToolBar = new ClickToolBar(this);
//		clickPanel.add(clickToolBar.getToolBar());

//		scrollPane.createHorizontalScrollBar();
		
		plainPanel = new JPanel();
		plainPanel.setLayout(new BorderLayout());
		//plainPanel.add(BorderLayout.NORTH, getToolBar());
		plainPanel.add(BorderLayout.CENTER, clickPanel);
		
		if (clickDisplayManager.totalCount == 0) {
//			clickDisplayManager.createStandardDisplay();
			clickDisplayManager.createDisplays();
		}
	}

	public JMenu createMenu(Frame parentFrame) {
		
		JMenuItem menuItem;
		JMenu menu = new JMenu(clickControl.getUnitName());

//		menuItem = new JMenuItem("Display Settings ...");
//		menuItem.addActionListener(new DisplayOptions(parentFrame));
//		menu.add(menuItem);
		
//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
//			menuItem = new JMenuItem("Open Click File...");
//			menuItem.addActionListener(new OpenFile(parentFrame));
//			menu.add(menuItem);
//
//			menuItem = new JMenuItem("Rerun Click Classification...");
//			menuItem.addActionListener(new RunClickClassification(parentFrame));
//			menu.add(menuItem);
//		}
		
//		menuItem = new JMenuItem("Map Options ...");
//		menuItem.addActionListener(new MapOptions(parentFrame));
//		menu.add(menuItem);
		
		menuItem = new JMenuItem("Display Overlays ...");
		menuItem.addActionListener(new OverlayOptions(parentFrame));
		menuItem.setToolTipText("Overlay options for Radar and Spectrogram displays");
		menu.add(menuItem);
		
		menu.add(clickDisplayManager.getModulesMenu());
		
		menuItem = new JMenuItem("Arrange Windows ...");
		menuItem.addActionListener(new ArrangeWindows(parentFrame));
		menu.add(menuItem);
		
		menu.add(clickControl.angleVetoes.getDisplayMenuItem(parentFrame));

		return menu;
	}

	public JComponent getPanel() {
		return plainPanel;
	}


	class DisplayOptions implements ActionListener {
		
		Frame parentFrame;
		
		public DisplayOptions(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
//			ClickParameters newParameters = 
//				ClickDisplayDialog.showDialog(parentFrame, clickControl.clickParameters);
//			if (newParameters != null){
//				clickControl.clickParameters = newParameters.clone();
////				clickToolBar.setControls(newParameters);
//				clickPanel.noteNewSettings();
//			}
		}
	}
	
//	public class MapOptions implements ActionListener {
//		
//		private Frame parentFrame;
//		
//		public MapOptions(Frame parentFrame) {
//			this.parentFrame = parentFrame;
//		}
//		
//		public void actionPerformed(ActionEvent ev) {
//			ClickParameters newParameters = 
//				ClickMapDialog.showDialog(parentFrame ,clickControl.clickParameters);
//			if (newParameters != null){
//				clickControl.clickParameters = newParameters.clone();
//			}
//		}
//	}

	public class OverlayOptions implements ActionListener {
		
		private Frame parentFrame;
		
		public OverlayOptions(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		
		public void actionPerformed(ActionEvent ev) {
			ClickParameters newParameters = 
				OverlayOptionsDialog.showDialog(parentFrame ,clickControl.clickParameters);
			if (newParameters != null){
				clickControl.clickParameters = newParameters.clone();
			}
		}
	}
	
	public class ArrangeWindows implements ActionListener {
		
		Frame parentFrame;
		
		public ArrangeWindows(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		
		public void actionPerformed(ActionEvent ev) {
			clickPanel.arrangeWindows();
		}
	}

	public ClickTabPanel getClickPanel() {
		return clickPanel;
	}

	public JToolBar getToolBar() {
//		return clickToolBar.getToolBar();
		return null;
	}
	
	void toolBarNotify(ClickParameters clickParameters) {
		clickControl.clickParameters = clickParameters.clone();
		clickPanel.noteNewSettings();
	}

//	/** 
//	 * Called when an offline store has opened or closed. 
//	 */
//	public void newOfflineStore() {
//		clickPanel.newOfflineStore();
//	}

	/** 
	 * Called from clicksOffline when data have changed (eg from re doing click id). 
	 * Needs to notify the display and maybe some other classes. 
	 */
	public void offlineDataChanged() {
		clickPanel.offlineDataChanged();
	}
	
	public ClickDisplayManager getDisplayManager(){
		return clickDisplayManager;
	}

}
