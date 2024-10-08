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
package userDisplay;

import java.awt.Frame;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import Layout.PamInternalFrame;
import PamView.PamPanelTiler;
import PamView.PamTabPanel;
import Spectrogram.SpectrogramDiplayProvider;
import Spectrogram.SpectrogramDisplay;
import Spectrogram.SpectrogramParameters;
import radardisplay.RadarDisplayProvider;
import radardisplay.RadarParameters;

public class UserDisplayTabPanelControl implements PamTabPanel {

	private UserDisplayTabPanel userDisplayTabPanel;

	protected PamPanelTiler panelTiler;

	private UserDisplayControl userDisplayControl;

	UserDisplayTabPanelControl(UserDisplayControl userDisplayControl) {
		userDisplayTabPanel = new UserDisplayTabPanel();
		this.userDisplayControl = userDisplayControl;
		panelTiler = new PamPanelTiler(userDisplayTabPanel);
	}

	void addSpectrogram(SpectrogramParameters spectrogramParameters) {
		// tabPanelControl.AddSpectrogram();
//		PamInternalFrame frame;
//		SpectrogramDisplay spectrogramDisplay;
//		spectrogramDisplay = new SpectrogramDisplay(userDisplayControl,
//				spectrogramParameters);
//		frame = new PamInternalFrame(
//				spectrogramDisplay , true);
//			userDisplayTabPanel.add(frame , true);
//		userDisplayTabPanel.setPosition(frame, 0);
//		//spectrogramDisplay.SetParams(spectrogramParameters);
//		if (spectrogramParameters.boundingRectangle != null &&
//				spectrogramParameters.boundingRectangle.width > 0) {
//			frame.setBounds(spectrogramParameters.boundingRectangle);
//		}
//		frame.addInternalFrameListener(spectrogramDisplay);
		SpectrogramDiplayProvider sdp = UserDisplayControl.getSpectrogramDiplayProvider();
		DisplayProviderParameters dpp = new DisplayProviderParameters(sdp.getComponentClass().getName(), sdp.getName(), sdp.getName());
		dpp.location = spectrogramParameters.boundingRectangle.getLocation();
		dpp.size = spectrogramParameters.boundingRectangle.getSize();
		sdp.setReadyParameters(spectrogramParameters);
		addUserDisplay(sdp, dpp);
		
	}
	void addRadar(RadarParameters radarParameters) {
		RadarDisplayProvider rdp = UserDisplayControl.getRadarDisplayProvider();
		DisplayProviderParameters dpp = new DisplayProviderParameters(rdp.getComponentClass().getName(), rdp.getName(), rdp.getName());
		dpp.location = radarParameters.boundingRectangle.getLocation();
		dpp.size = radarParameters.boundingRectangle.getSize();
		rdp.setReadyParams(radarParameters);
		addUserDisplay(rdp, dpp);
	}

	/**
	 * Ad a display generated by a different module
	 * @param userDisplayProvider
	 * @param dpParams 
	 */
	public void addUserDisplay(UserDisplayProvider userDisplayProvider, DisplayProviderParameters dpParams) {
		if (!userDisplayProvider.canCreate()) {
			return;
		}
		String uniqueName = DisplayNameManager.getInstance().getUniqueName(userDisplayProvider.getName(), dpParams);
//		System.out.println("Adding display " + userDisplayProvider.getName());
		UserDisplayFrame frame = new UserDisplayFrame(userDisplayControl, userDisplayProvider, uniqueName);
		frame.getUserDisplayComponent().setUniqueName(uniqueName);
		UserDisplayComponent displayComponent = frame.getUserDisplayComponent();
		if (displayComponent == null) {
			return;
		}
		if (displayComponent instanceof UserFramePlots) {
			UserFramePlots userFramePlots = (UserFramePlots) displayComponent;
//			userFramePlots.setFrame(frame.get);
			
		}
		userDisplayTabPanel.add(frame);
		userDisplayTabPanel.setPosition(frame, 0);
		if (dpParams != null) {
			frame.setLocation(dpParams.location);
			frame.setSize(dpParams.size);
		}
		frame.addInternalFrameListener(new UserDisplayListener(frame));
		frame.setTitle(frame.getUserDisplayComponent().getFrameTitle());
		userDisplayControl.enableUserMenus();
	}

	public Serializable getSettingsReference() {

//		ArrayList<SpectrogramParameters> spectrogramsList = new ArrayList<SpectrogramParameters>();
//		ArrayList<RadarParameters> radarList = new ArrayList<RadarParameters>();
		ArrayList<DisplayProviderParameters> providerList = new ArrayList<DisplayProviderParameters>();

		UserFramePlots userDisplay;
		UserDisplayFrame userFrame;

		JInternalFrame[] allFrames = userDisplayTabPanel.getAllFrames();
		// reverse order of this loop so frames get reconstructed in correct order.
		for (int i = allFrames.length-1; i >= 0; i--) {
			if (UserDisplayFrame.class.isAssignableFrom(allFrames[i].getClass())) {
				userFrame = (UserDisplayFrame) allFrames[i];
				UserDisplayProvider udp = userFrame.getUserDisplayProvider();
				DisplayProviderParameters dp = new DisplayProviderParameters(udp.getComponentClass().getName(), udp.getName(), udp.getName());
				dp.size = userFrame.getSize();
				dp.location = userFrame.getLocation();
				dp.setDisplayName(userFrame.getUserDisplayComponent().getUniqueName());
				providerList.add(dp);
			}
//			else {
//				userDisplay = (UserFramePlots) ((PamInternalFrame) allFrames[i]).getFramePlots();
//				if (userDisplay.getFrameType() == UserFramePlots.FRAME_TYPE_SPECTROGRAM) {
//
//					spectrogramsList
//					.add(((SpectrogramDisplay) ((PamInternalFrame) allFrames[i])
//							.getFramePlots()).getSpectrogramParameters());
//				}
//				else if (userDisplay.getFrameType() == UserFramePlots.FRAME_TYPE_RADAR) {
//					radarList
//					.add(((RadarDisplay) ((PamInternalFrame) allFrames[i])
//							.getFramePlots()).getRadarParameters());
//				}
//			}
		}

		UserDisplayParameters udp = new UserDisplayParameters();
//		udp.spectrogramParameters = spectrogramsList;
//		udp.radarParameters = radarList;
		udp.displayProviderParameters = providerList;

		return udp;
	}

	public void notifyModelChanged(int changeType) {
//		System.out.println("UserDisplayTabPanelControl: notify change: " +  changeType);
		JInternalFrame[] allFrames = userDisplayTabPanel.getAllFrames();
		for (int i = 0; i < allFrames.length; i++) {
			if (PamInternalFrame.class.isAssignableFrom(allFrames[i].getClass())) {
				((UserFramePlots) ((PamInternalFrame) allFrames[i])
						.getFramePlots()).notifyModelChanged(changeType);
			}
			else if (UserDisplayFrame.class.isAssignableFrom(allFrames[i].getClass())) {
				UserDisplayFrame userDisplayFrame = (UserDisplayFrame) allFrames[i];
				userDisplayFrame.getUserDisplayComponent().notifyModelChanged(changeType);
			}
		}
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	/**
	 * @return Reference to a graphics component that can be added to the view.
	 *         This will typically be a JPanel or a JInternalFrame;
	 */
	@Override
	public JComponent getPanel() {
		return userDisplayTabPanel;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canPlayViewerSound() {
		SpectrogramDisplay sd = findFirstSpectrogram();
		return (sd != null);
	}

	public void playViewerSound() {
		SpectrogramDisplay sd = findFirstSpectrogram();
		if (sd != null) {
			sd.playViewerSound();
		}
	}

	public SpectrogramDisplay findFirstSpectrogram() {
		JInternalFrame[] allFrames = userDisplayTabPanel.getAllFrames();
		UserFramePlots userDisplay;
		for (int i = 0; i < allFrames.length; i++) {
			if (!PamInternalFrame.class.isAssignableFrom(allFrames[i].getClass())) {
				continue;
			}
			userDisplay = (UserFramePlots) ((PamInternalFrame) allFrames[i]).getFramePlots();
			if (userDisplay.getFrameType() == UserFramePlots.FRAME_TYPE_SPECTROGRAM) {
				return (SpectrogramDisplay) userDisplay;
			}
		}
		return null;
	}
	
	class UserDisplayListener extends InternalFrameAdapter {

		private UserDisplayFrame userFrame;
		
		public UserDisplayListener(UserDisplayFrame frame) {
			this.userFrame = frame;
		}
		
		@Override
		public void internalFrameClosed(InternalFrameEvent e) {
//			System.out.println("internalFrameClosed " + e);
			UserDisplayProvider dp = userFrame.getUserDisplayProvider();
			if (dp != null) {
				dp.removeDisplay(userFrame.getUserDisplayComponent());
			}
			userDisplayControl.enableUserMenus();			
		}

	}

	/**
	 * Remove all displays without saving any of them.
	 * Needed for successful import only - generally does nothing. 
	 */
	public void removeAllDisplays() {
		userDisplayTabPanel.removeAll();
	}
}
