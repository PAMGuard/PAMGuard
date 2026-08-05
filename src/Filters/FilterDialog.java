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
package Filters;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class FilterDialog extends PamDialog {


	private static FilterDialog singleInstance;
	
	private FilterDialogPanel filterDialogPanel;
	
	private SourcePanel sourcePanel;

	private FilterParameters_2 filterParams2;
	
	private FilterDialog(Window ownerWindow, boolean showSource) {
		super(ownerWindow, "Filter Settings", false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		if (showSource) {
			sourcePanel = new SourcePanel(this, "Data input", RawDataUnit.class, true, true);
			mainPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());
			sourcePanel.addSelectionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sourceSelection();
				}
			});
		}
		filterDialogPanel = new FilterDialogPanel(ownerWindow, 1);
		mainPanel.add(BorderLayout.CENTER, filterDialogPanel.getMainPanel());

		setSize(800, 500);
		setLocation(300, 200);
		this.setResizable(true);
		

		setDialogComponent(mainPanel);
		setHelpPoint("sound_processing.FiltersHelp.Docs.Filters_filters");
	}
	
	/**
	 * Called when source is selected
	 */
	protected void sourceSelection() {
		PamDataBlock source = sourcePanel.getSource();
		if (source == null) {
			return;
		}
		filterDialogPanel.setSampleRate(source.getSampleRate());
	}

	public static FilterParameters_2 showDialog(Window ownerFrame, FilterParameters_2 filterParams2, float sampleRate) {
		if (singleInstance == null || singleInstance.getOwner() != ownerFrame || singleInstance.sourcePanel == null) {
			singleInstance = new FilterDialog(ownerFrame, true);
		}
		singleInstance.filterDialogPanel.setSampleRate(sampleRate);
		singleInstance.filterParams2 = filterParams2;
		singleInstance.setParams(filterParams2);
		singleInstance.setVisible(true);
		singleInstance.filterParams2.filterParams = singleInstance.filterDialogPanel.getFilterParams();
		return singleInstance.filterParams2;
	}
	

	private void setParams(FilterParameters_2 filterParams2) {
		filterDialogPanel.setParams(filterParams2.filterParams);
		sourcePanel.setSource(filterParams2.rawDataSource);
		sourcePanel.setChannelList(filterParams2.channelBitmap);
	}

	public static FilterParams showDialog(Window ownerFrame, FilterParams filterParams, float sampleRate) {
		if (singleInstance == null || singleInstance.getOwner() != ownerFrame || singleInstance.sourcePanel != null) {
			singleInstance = new FilterDialog(ownerFrame, false);
		}
//		singleInstance.filterDialogPanel.filterParams = filterParams.clone();
//		singleInstance.filterDialogPanel.sampleRate = Math.max(sampleRate, 1.f);
		singleInstance.filterDialogPanel.setSampleRate(sampleRate);
		singleInstance.filterDialogPanel.setParams(filterParams.clone());
		singleInstance.setVisible(true);
		return singleInstance.filterDialogPanel.getFilterParams();
	}

	@Override
	public boolean getParams() {
		if (sourcePanel != null && filterParams2 != null) {
			filterParams2.rawDataSource = sourcePanel.getSourceName();
			if (filterParams2.rawDataSource == null) {
				return showWarning("No data source selected");
			}
			filterParams2.channelBitmap = sourcePanel.getChannelList();
			if (filterParams2.channelBitmap == 0) {
				return showWarning("you must select at least one channel");
			}
		}
		return filterDialogPanel.getParams();
	}

	@Override
	public void cancelButtonPressed() {
		filterDialogPanel.cancelButtonPressed();
	}

	@Override
	public void restoreDefaultSettings() {
		filterDialogPanel.restoreDefaultSettings();
	}
}
