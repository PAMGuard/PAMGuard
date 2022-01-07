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

import java.awt.Window;
import PamView.dialog.PamDialog;

public class FilterDialog extends PamDialog {


	private static FilterDialog singleInstance;
	
	private FilterDialogPanel filterDialogPanel;
	
	private FilterDialog(Window ownerWindow, FilterParams filterParams, float sampleRate) {
		super(ownerWindow, "Filter Settings", false);
		filterDialogPanel = new FilterDialogPanel(ownerWindow, sampleRate);

		setSize(800, 500);
		setLocation(300, 200);
		this.setResizable(true);

		setDialogComponent(filterDialogPanel.getMainPanel());
		setHelpPoint("sound_processing.FiltersHelp.Docs.Filters_filters");
	}

	public static FilterParams showDialog(Window ownerFrame, FilterParams filterParams, float sampleRate) {
		if (singleInstance == null || singleInstance.getOwner() != ownerFrame) {
			singleInstance = new FilterDialog(ownerFrame, filterParams, (float) Math.max(
					sampleRate, 1.));
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
