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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

//import localiserDisplay.LocaliserDisplayProvider;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamModel.PamDependency;
import PamModel.PamDependent;
import PamModel.PamModel;
import PamView.PamGui;
import Spectrogram.SpectrogramDiplayProvider;
import Spectrogram.SpectrogramParameters;
import Spectrogram.SpectrogramParamsDialog;
import dataPlots.TDDisplayProvider;
import dataPlotsFX.TDDisplayProviderFX;
import fftManager.FFTDataUnit;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.coupling.ScrollerCoupling;
import radardisplay.RadarDisplayProvider;
import radardisplay.RadarParameters;
import radardisplay.RadarParametersDialog;
import radardisplay.RadarProjector;
import soundPlayback.PlaybackControl;

public class UserDisplayControl extends PamControlledUnit implements
		PamSettings {

	UserDisplayTabPanelControl tabPanelControl;
	
	SpectrogramDependency spectrogramDependency = new SpectrogramDependency();

	private UserDisplayParameters displayParameters;
	
	private static ArrayList<UserDisplayProvider> userDisplayProviders = new ArrayList<UserDisplayProvider>();

	private static SpectrogramDiplayProvider spectrogramDiplayProvider;

	private static RadarDisplayProvider radarDisplayProvider;
	
	private ArrayList<UserMenuItemEnabler> userMenuEnablers = new ArrayList<UserMenuItemEnabler>();
	
	static {
		UserDisplayControl.addUserDisplayProvider(spectrogramDiplayProvider = new SpectrogramDiplayProvider());
		UserDisplayControl.addUserDisplayProvider(radarDisplayProvider = new RadarDisplayProvider());
	}
	
	public UserDisplayControl(String name) {
		
		super("User Display", name);

		setTabPanel(tabPanelControl = new UserDisplayTabPanelControl(this));

		PamSettingManager.getInstance().registerSettings(this);
		
		TDDisplayProvider.register();
		TDDisplayProviderFX.register();
	
//		//register the localiser display. 
//		if (JamieDev.isEnabled()) {
//			LocaliserDisplayProvider.register(); 
//		}
			
				
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		/*
		 * It's possible (likely) that the spectrograms were created
		 * before thier data source, so check with each display
		 * that it's got it's data source, and if not, try again
		 */
		tabPanelControl.notifyModelChanged(changeType);
//		if (changeType == PamController.ADD_CONTROLLEDUNIT) {
		if (changeType == PamControllerInterface.ADD_CONTROLLEDUNIT) {
		}
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			createDisplays();
		}
		
//		}
	}

	void AddSpectrogram(SpectrogramParameters spectrogramParameters) {
		tabPanelControl.addSpectrogram(spectrogramParameters);
	}
	
	void AddRadar(RadarParameters radarParameters) {
		tabPanelControl.addRadar(radarParameters);
	}
	
	protected void enableUserMenus() {
		for (UserMenuItemEnabler umo:userMenuEnablers) {
			umo.enableItem();
		}
	}
	
	class UserMenuItemEnabler {
		JMenuItem menuItem;
		UserDisplayProvider userDisplayProvider;
		public UserMenuItemEnabler(UserDisplayProvider userDisplayProvider,
				JMenuItem menuItem) {
			this.userDisplayProvider = userDisplayProvider;
			this.menuItem = menuItem;
		}
		public void enableItem() {
			menuItem.setEnabled(userDisplayProvider.canCreate());
		}
	}
	
	@Override
	public JMenu createDisplayMenu(Frame parentFrame) {

		JMenuItem menuItem;
		JMenu menu = new JMenu(getUnitName());

//		menuItem = new JMenuItem("New Spectrogram ...");
//		menuItem.addActionListener(new MenuNewSpectrogram(parentFrame));
//		menu.add(menuItem);
//
//		menuItem = new JMenuItem("New Radar display ...");
//		menuItem.addActionListener(new MenuNewRadar(parentFrame));
//		menu.add(menuItem);
		
		userMenuEnablers.clear();
		for (UserDisplayProvider ud:userDisplayProviders) {
			menuItem = new JMenuItem("New " + ud.getName());
			menuItem.addActionListener(new UserDisplayAction(ud));
//			menuItem.setEnabled(ud.canCreate());
			userMenuEnablers.add(new UserMenuItemEnabler(ud, menuItem));
			menu.add(menuItem);
		}
		enableUserMenus();
		
		menu.addSeparator();
		
		menu.add(tabPanelControl.panelTiler.getMenu());
		
		if (isViewer()) {
		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
		ScrollerCoupling coupler = scrollManager.findCoupling(getUnitName(), true);
		menuItem = coupler.getSwingOptionsMenu(parentFrame);
		if (menuItem != null) {
			menu.add(menuItem);
		}
		}

		return menu;
	}

	class MenuNewSpectrogram implements ActionListener {
		
		Frame parentFrame;
		
		public MenuNewSpectrogram(Frame parentFrame) {
			super();
			// TODO Auto-generated constructor stub
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			/*
			 * First check necessary dependents are all in place
			 * for creating a Spectrogram (fft data - requires raw data)
			 */
			PamModel.getPamModel().getDependencyManager().checkDependency(parentFrame, spectrogramDependency, true);
			SpectrogramParameters params = new SpectrogramParameters();
			SpectrogramParameters newParams = SpectrogramParamsDialog.showDialog(parentFrame, null, params);
			if (newParams != null) {
				AddSpectrogram(newParams.clone());
			}
		}
	}
	class MenuNewRadar implements ActionListener {
		Frame parentFrame;
		
		public MenuNewRadar(Frame parentFrame) {
			super();
			// TODO Auto-generated constructor stub
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			RadarParameters params = new RadarParameters();
			RadarProjector radarProjector = new RadarProjector(null);
			RadarParameters newParams = RadarParametersDialog.showDialog(null, parentFrame, params, radarProjector);
			if (newParams != null) {
				AddRadar(newParams.clone());
			}
		}
	}
	
	/**
	 * Generic action listener for launching general user displays 
	 * registered from othe rmodules. 
	 * @author Doug Gillespie
	 *
	 */
	class UserDisplayAction implements ActionListener {

		private UserDisplayProvider userDisplayProvider;
		public UserDisplayAction(UserDisplayProvider ud) {
			this.userDisplayProvider = ud;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			tabPanelControl.addUserDisplay(userDisplayProvider, null);
		}
		
	}
	
	class SpectrogramDependency implements PamDependent {

		PamDependency dependency;
		
		public SpectrogramDependency() {
			dependency = new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl");
		}

		/* (non-Javadoc)
		 * @see PamModel.PamDependent#addDependency(PamModel.PamDependency)
		 */
		@Override
		public void addDependency(PamDependency dependancy) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see PamModel.PamDependent#getDependency()
		 */
		@Override
		public PamDependency getDependency() {
			return dependency;
		}

		/* (non-Javadoc)
		 * @see PamModel.PamDependent#getDependentUserName()
		 */
		@Override
		public String getDependentUserName() {
			return "Spectrogram";
		}
		
	}

	/*
	 * Stuff for settings interface
	 */
	
	@Override
	public long getSettingsVersion() {
		return UserDisplayParameters.serialVersionUID;
	}

	@Override
	public Serializable getSettingsReference() {
		return tabPanelControl.getSettingsReference();
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		/*
		 * Can do a final check to see if this is really settings for this
		 * detector, then cast and assign
		 * don't make the displays yet - wait until the load has finished so that
		 * user displays are all correctly registerd. 
		 */

		displayParameters = (UserDisplayParameters) pamControlledUnitSettings.getSettings();
		return true;
	}
	private boolean createDisplays() {	
		if (displayParameters == null) {
			return false;
		}
		/**
		 * First remove all the displays. Generally this will never need to do anything except
		 * when new displays are imported over the top of existing ones from other configurations
		 */
		tabPanelControl.removeAllDisplays();
		
		ArrayList<SpectrogramParameters> parametersList = displayParameters.spectrogramParameters;
		if (parametersList != null) for (int i = 0; i < parametersList.size(); i++) {
			AddSpectrogram(parametersList.get(i));
		}
		ArrayList<RadarParameters> radarList = displayParameters.radarParameters;
		if (radarList != null) for (int i = 0; i < radarList.size(); i++) {
			AddRadar(radarList.get(i));
		}
		
		ArrayList<DisplayProviderParameters> providerList = displayParameters.displayProviderParameters;
		if (providerList != null) {
			for (DisplayProviderParameters dpp:providerList) {
				UserDisplayProvider dp = findDisplayProvider(dpp);
				if (dp == null) {
					System.out.println(String.format("Unable to create user display %s for class %s", 
							dpp.getDisplayName(), dpp.getProviderClassName()));
					continue;
				}
				tabPanelControl.addUserDisplay(dp, dpp);
			}
		}
		return true;
	}


	JMenuBar spectrogramTabMenu = null;
	@Override
	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar standardMenu, PamGui pamGui) {

		// always make a new manu - modules may have been added or removed
		// start bymaking a completely new copy.
		spectrogramTabMenu = standardMenu;//pamGui.makeGuiMenu();
//		if (spectrogramTabMenu == null) {
			for (int i = 0; i < spectrogramTabMenu.getMenuCount(); i++) {
				if (spectrogramTabMenu.getMenu(i).getText().equals("Display")) {
					//spectrogramTabMenu.remove(spectrogramTabMenu.getMenu(i));
					
					JMenu aMenu = createDisplayMenu(parentFrame);
					aMenu.setText(getUnitName());
					spectrogramTabMenu.add(aMenu, i+1);
					
					break;
				}
			}
//		}
		return spectrogramTabMenu;
	}

	@Override
	public void pamToStart() {
		//tabPanelControl.spectrogramPanel. PamToStart();
	}


	@Override
	public boolean canPlayViewerSound() {
		if (!PlaybackControl.getViewerPlayback().hasPlayDataSource()) {
			return false;
		}
		return tabPanelControl.canPlayViewerSound();
	}

	@Override
	public void playViewerSound() {
		tabPanelControl.playViewerSound();
	}

	@Override
	public void stopViewerSound() {
		// TODO Auto-generated method stub
		super.stopViewerSound();
	}

	/**
	 * Add a reference to the list of providers of user displays. 
	 * @param userDisplayProvider reference to a provider of user displays. 
	 */
	public static void addUserDisplayProvider(UserDisplayProvider userDisplayProvider) {
		userDisplayProviders.add(userDisplayProvider);
	}
	
	/**
	 * Remove a reference to a list of user display provider
	 * @param userDisplayProvider reference to a provider of user displays. 
	 */
	public static void removeDisplayProvider(UserDisplayProvider userDisplayProvider) {
		userDisplayProviders.remove(userDisplayProvider); 
	}

	private static UserDisplayProvider findDisplayProvider(DisplayProviderParameters displayProviderParameters) {
		String providerClassName = displayProviderParameters.getProviderClassName();
		String providerName = displayProviderParameters.getProviderName();
		for (UserDisplayProvider ap:userDisplayProviders) {
//			if (ap.getComponentClass().getName().equals(providerClassName) && ap.getName().equals(displayName)) {
//				return ap;
//			}
			/**
			 * Only search on class name since the getName is used to store a unique 
			 * name, not an identifier of the provider. 
			 * Jan 2018 - not sure about the above - seems that the name is not always set right. 
			 * Am going to start using a specifically provider name too ...
			 * For back compatibility, if providername is null, only search on 
			 * class name, otherwise require both. 
			 */
			if (!ap.getComponentClass().getName().equals(providerClassName)) {
				continue;
			}
			if (providerName == null ||	ap.getName().equals(providerName)) {
				return ap;
			}
		}
		return null;
	}
	
	
	/**
	 * Called for all controlled units after Pam acquisition has stopped
	 *
	 */
	@Override
	public void pamHasStopped() {
		this.notifyModelChanged(PamController.PAM_IDLE);
	}

	/**
	 * @return the spectrogramDiplayProvider
	 */
	public static SpectrogramDiplayProvider getSpectrogramDiplayProvider() {
		return spectrogramDiplayProvider;
	}

	/**
	 * @return the radarDisplayProvider
	 */
	public static RadarDisplayProvider getRadarDisplayProvider() {
		return radarDisplayProvider;
	}
}
