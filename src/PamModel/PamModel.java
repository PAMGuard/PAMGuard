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
package PamModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFrame;

import Acquisition.DaqSystemInterface;
import Array.ArraySidePanelControl;
import GPS.GpsDataUnit;
import NMEA.NMEADataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.FileFinder;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import analogarraysensor.ArraySensorControl;
import backupmanager.BackupManager;
import beamformer.continuous.BeamFormerControl;
import bearinglocaliser.BearingLocaliserControl;
import binaryFileStorage.SecondaryBinaryStore;
import cepstrum.CepstrumControl;
import clickDetector.ClickDetection;
import dataMap.DataMapControl;
import detectiongrouplocaliser.DetectionGroupControl;
import effortmonitor.EffortControl;
import fftManager.FFTDataUnit;
import fftManager.PamFFTControl;
import group3dlocaliser.Group3DLocaliserControl;
import landMarks.LandmarkControl;
import meygenturbine.MeygenTurbine;
import printscreen.PrintScreenControl;
import ravendata.RavenControl;
import rockBlock.RockBlockControl;
import tethys.TethysControl;
import turbineops.TurbineOperationControl;
import whistlesAndMoans.AbstractWhistleDataUnit;

/**
 * @author Doug Gillespie
 * 
 * Simple creation of a PAM model, but using the correct interface for the
 * PamController.
 * 
 */
final public class PamModel implements PamSettings {

	private PamController pamController;

	private PamDataBlock gpsDataBlock;

	private DependencyManager dependencyManager;

	private PamModelSettings pamModelSettings = new PamModelSettings();
	
	/**
	 * Boolean indicating whether we are running in Viewer mode (true) or in Normal/Mixed (false)
	 */
	private boolean isViewer;
	
	/**
	 * List of all available plugins found in the plugin folder, implementing the PamPluginInterface interface
	 */
	private List<PamPluginInterface> pluginList = new ArrayList<PamPluginInterface>();
	
	/**
	 * List of all available DAQ systems found in the plugin folder, extending the DaqSystem class
	 */
	private List<DaqSystemInterface> daqList = new ArrayList<DaqSystemInterface>();
	
	/**
	 * List of all menu groups
	 */
	private ArrayList<ModulesMenuGroup> modulesMenuGroups = new ArrayList<ModulesMenuGroup>();
	
	/**
	 * Name of subfolder containing Pamguard plugin modules
	 */
	private static final String pluginsFolder = "plugins"; 
	
	/**
	 * Name of plugin currently being loaded.  "none" indicates we are not currently loading any plugins
	 */
	private String pluginBeingLoaded = "none";
	
	/**
	 * Subclass of URLClassLoader, to handle loading of plugins 
	 */
	private final PluginClassloader classLoader;

	/**
	 * @param pamController
	 *            Needs to be parsed a valid reference to a PamController
	 */
	public PamModel(PamController pamController) {
		this.pamController = pamController;
		pamModel = this;
//		createPamModel();
		classLoader = new PluginClassloader(new URL[0], this.getClass().getClassLoader());
	}

	static PamModel pamModel = null;

	public static PamModel getPamModel() {
		return pamModel;
	}

	//	public PamDataBlock<GpsDataUnit> getGpsDataBlock() {
	//		/*
	//		 * If it's a fixed array then don't get this data block, but
	//		 * get one that's sitting in the ArrayManager and return that 
	//		 * instead
	//		 */
	//		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
	//		if (currentArray != null && currentArray.getHydrophoneLocator().isStatic()) {
	//			return currentArray.getFixedPointReferenceBlock();
	//		}
	//		else {
	//			// otherwise, just return the normal gps data block that was set from the NMEA module
	//			return gpsDataBlock;
	//		}
	//	}

	public void setGpsDataBlock(PamDataBlock gpsDataBlock) {
		this.gpsDataBlock = gpsDataBlock;
	}


	/**
	 * Creates a list of available Pamguard modules and sets dependencies between them<p>
	 * Should ony ever be called once immediately after the model has been created !!!!!!. 
	 * <p>
	 * Also sets grouping which are used for menu construction and the minimum and 
	 * maximum numbers of each type of module that may get created. 
	 */
	public void createPamModel() {

		/*
		 * Make a series of module menu groups and add most of the 
		 * modules to one group or another
		 */
		ModulesMenuGroup mapsGroup = new ModulesMenuGroup("Maps and Mapping");
		modulesMenuGroups.add(mapsGroup);
		ModulesMenuGroup processingGroup = new ModulesMenuGroup("Sound Processing");
		modulesMenuGroups.add(processingGroup);
		ModulesMenuGroup detectorsGroup = new ModulesMenuGroup("Detectors");
		modulesMenuGroups.add(detectorsGroup);
		ModulesMenuGroup classifierGroup = new ModulesMenuGroup("Classifiers");
		modulesMenuGroups.add(classifierGroup);
		ModulesMenuGroup localiserGroup = new ModulesMenuGroup("Localisers");
		modulesMenuGroups.add(localiserGroup);
		ModulesMenuGroup displaysGroup = new ModulesMenuGroup("Displays");
		modulesMenuGroups.add(displaysGroup);
		ModulesMenuGroup utilitiesGroup = new ModulesMenuGroup("Utilities");
		modulesMenuGroups.add(utilitiesGroup);
		ModulesMenuGroup visualGroup = new ModulesMenuGroup("Visual Methods");
		modulesMenuGroups.add(visualGroup);
		ModulesMenuGroup sensorsGroup = new ModulesMenuGroup("Sensors");
		modulesMenuGroups.add(sensorsGroup);
//		ModulesMenuGroup smlGroup = new ModulesMenuGroup("Seiche Modules");
//		modulesMenuGroups.add(smlGroup);
		ModulesMenuGroup measurementGroup = new ModulesMenuGroup("Sound Measurements");
		modulesMenuGroups.add(measurementGroup);
		
		//		ModulesMenuGroup smruGroup = new ModulesMenuGroup("SMRU Stuff");		
		dependencyManager = new DependencyManager(this);

		isViewer = (pamController.getRunMode() == PamController.RUN_PAMVIEW);
		//		boolean isSMRU = PamguardVersionInfo.getReleaseType() == PamguardVersionInfo.ReleaseType.SMRU;

		PamModuleInfo mi;

		/*
		 * ************* Start Maps and Mapping Group *******************
		 */
		/*
		 * Changed to allow any number, DG 13 March 2008
		 * This cooincides with changes to AIS and GPS modules
		 * which allow them to select an NMEA source. 
		 * These changes mean you can have GPS data and AIS data comign in 
		 * over separate serial ports.
		 */
		//		mi = PamModuleInfo.registerControlledUnit("Array.ArrayManager", "Array Manager");
		//		mi.setModulesMenuGroup(mapsGroup);
		//		mi.setMinNumber(1);
		//		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit("NMEA.NMEAControl", "NMEA Data Collection");
		mi.setModulesMenuGroup(mapsGroup);
		mi.setToolTipText("Collects NMEA data from a serial port");
		mi.setMinNumber(0);

		mi = PamModuleInfo.registerControlledUnit("GPS.GPSControl", "GPS Processing");
		mi.setModulesMenuGroup(mapsGroup);
		mi.setToolTipText("Interprets NMEA data to extract GPS data");
		mi.setMinNumber(0);
		if (!isViewer) {
			mi.addDependency(new PamDependency(NMEADataUnit.class, "NMEA.NMEAControl"));	
			mi.setMaxNumber(1);		
		}

		mi = PamModuleInfo.registerControlledUnit("Map.MapController", "Map");	
		mi.addDependency(new PamDependency(GpsDataUnit.class, "GPS.GPSControl"));
		mi.setToolTipText("Displays a map of vessel position and detections");
		mi.setModulesMenuGroup(mapsGroup);
		mi.setMinNumber(0);

//		mi = PamModuleInfo.registerControlledUnit(GridbaseControl.class.getName(), GridbaseControl.unitType);	
//		mi.setToolTipText("Load a gridded map to display as an overlay");
//		mi.setModulesMenuGroup(mapsGroup);
//		mi.setMinNumber(0);
//		mi.setHidden(SMRUEnable.isEnable() == false);
		
		/**
		 * Very basic ideas for a 3D map - abandoned and modified the main Map 
		 * to incorporate 3D rotations instead. 
		 */
//		mi = PamModuleInfo.registerControlledUnit(Map3DControl.class.getName(), "3D Map Display");
//		mi.setToolTipText("Displays 3D plots of data centred around the hydrophone array");
//		mi.setModulesMenuGroup(mapsGroup);
//		mi.setMaxNumber(1);
		//		mi.setMaxNumber(1);
		
		mi = PamModuleInfo.registerControlledUnit("AIS.AISControl", "AIS Processing");
		mi.addDependency(new PamDependency(NMEADataUnit.class, "NMEA.NMEAControl"));
		mi.setToolTipText("Interprets NMEA data to extract AIS data");
		mi.setModulesMenuGroup(mapsGroup);
		mi.setMinNumber(0);
		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit("AirgunDisplay.AirgunControl", "Airgun Display");
		mi.setModulesMenuGroup(mapsGroup);
		mi.setToolTipText("Shows the position of airguns (or any other source) on the map");
		mi.setMinNumber(0);
		//		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit(LandmarkControl.class.getName(), "Fixed Landmarks");
		mi.setModulesMenuGroup(mapsGroup);
		mi.setToolTipText("Place object symbols on the PAMGuard map");
		
//		mi = PamModuleInfo.registerControlledUnit("mapgrouplocaliser.MapGroupLocaliserControl", "Map Group Localiser");
//		mi.setModulesMenuGroup(mapsGroup);
//		mi.setToolTipText("Group and localise detections on the PAMGuard map");
//		mi.addDependency(new PamDependency(MapComment.class, "Map.MapController"));

//		mi = PamModuleInfo.registerControlledUnit("WILDInterface.WILDControl", "WILD ArcGIS Interface");
//		mi.setModulesMenuGroup(mapsGroup);
//		mi.setToolTipText("Outputs data in an NMEA string via a serial port");
//		mi.setMaxNumber(1);

		//		mi = PamModuleInfo.registerControlledUnit("Map3D.Map3DControl", "3D Map");	
		//		//mi.addDependency(new PamDependency(GpsDataUnit.class, "GPS.GPSControl"));
		//		mi.setModulesMenuGroup(mapsGroup);
		//		mi.setMinNumber(0);
		//		mi.setMaxNumber(1);


		/*
		 * ************* End Maps and Mapping Group *******************
		 */

		/*
		 * ************* Start Utilities Group *******************
		 */

		mi = PamModuleInfo.registerControlledUnit("generalDatabase.DBControlUnit", "Database");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setToolTipText("Stores PAMGuard data in a database");
		mi.addGUICompatabilityFlag(PamGUIManager.FX);
		if (isViewer) {
			mi.setMinNumber(1);
		}
		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit("binaryFileStorage.BinaryStore", "Binary Storage");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setToolTipText("Stores PAMGuard data in files on the hard drive");
		//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
		//			mi.setMinNumber(1);
		//		}
		mi.setMaxNumber(1);
		mi.addGUICompatabilityFlag(PamGUIManager.FX);
//		mi.setMaxNumber(SMRUEnable.isEnable() ? 2 : 1);
		

		mi = PamModuleInfo.registerControlledUnit(SecondaryBinaryStore.class.getName(), SecondaryBinaryStore.unitType);
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setToolTipText("Additional binary data from 2nd, 3rd, etc. moorings.");
		mi.setHidden(!isViewer || !SMRUEnable.isEnable());


		//		if (isSMRU) {
		mi = PamModuleInfo.registerControlledUnit("networkTransfer.send.NetworkSender", "Network Sender");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setToolTipText("Sends PAMGuard data over a network to other computers");
		mi.setHidden(!SMRUEnable.isEnable());
		

//		mi = PamModuleInfo.registerControlledUnit("serialPortLogger.SerialLogger", "Serial Port Logger");
//		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setToolTipText("Logs data from a serial port to a timestamped database");
//		mi.setHidden(SMRUEnable.isEnable() == false);
//		mi.setMaxNumber(1);

		if (pamController.getRunMode() == PamController.RUN_NETWORKRECEIVER ||
				pamController.getRunMode() == PamController.RUN_NORMAL) {
			mi = PamModuleInfo.registerControlledUnit("networkTransfer.receive.NetworkReceiver", "Network Receiver");
			mi.setModulesMenuGroup(utilitiesGroup);
			mi.setToolTipText("Receives PAMGuard data sent over the network from the Network Sender module");
			mi.setMaxNumber(1);
			mi.setMinNumber(pamController.getRunMode() == PamController.RUN_NETWORKRECEIVER ? 1 : 0);
			mi.setHidden(!SMRUEnable.isEnable());
		}

//		mi = PamModuleInfo.registerControlledUnit("decimus.summarystring.DStrControl", "Decimus Summary Strings");
//		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setToolTipText("Displays information from Decimus summary strings");
//		mi.setHidden(SMRUEnable.isEnableDecimus() == false);

//		mi = PamModuleInfo.registerControlledUnit(DecimusMitigateControl.class.getName(), "Decimus Network Control");
//		mi.addDependency(new PamDependency(BuoyStatusDataUnit.class, NetworkReceiver.class.getName()));
//		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setToolTipText("Additional functionality for Decimus running in Mitigate mode. ");
//		mi.setHidden(SMRUEnable.isEnableDecimus() == false);
		//		}

		if (isViewer) {
			mi = PamModuleInfo.registerControlledUnit(DataMapControl.class.getName(), "Data Map");
			mi.setModulesMenuGroup(utilitiesGroup);
			mi.setToolTipText("Shows a summary of data density over time for large datasets");
			if (isViewer) {
				mi.setMinNumber(1);
			}
			mi.setMaxNumber(1);
		}

		mi = PamModuleInfo.registerControlledUnit("UserInput.UserInputController", "User input");	
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setToolTipText("Creates a form for the user to type comments into");
		mi.setMinNumber(0);
		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit("listening.ListeningControl", "Aural Listening Form");
		mi.setToolTipText("Creates a form for the user to manually log things they hear");
		mi.setModulesMenuGroup(utilitiesGroup);		
		
		mi = PamModuleInfo.registerControlledUnit(qa.QAControl.class.getName(), "Signal Injection and Detector Evaluation (SIDE)");	
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setToolTipText("Signal injection and real time performance tests");
		mi.setMaxNumber(1);
//		mi.setHidden(SMRUEnable.isEnable() == false);

		if (isViewer) {
//			mi = PamModuleInfo.registerControlledUnit("xBatLogViewer.XBatLogControl", "XBat Log Viewer");
//			mi.setToolTipText("Displays converted xBat log files");
//			mi.setModulesMenuGroup(utilitiesGroup);
//			mi.setHidden(SMRUEnable.isEnable() == false);

//			mi = PamModuleInfo.registerControlledUnit("offlineProcessing.OfflineProcessingControlledUnit", "Offline Processing");
//			mi.setModulesMenuGroup(utilitiesGroup);
//			mi.setMinNumber(0);
//			mi.setMaxNumber(1);
//			mi.setHidden(SMRUEnable.isEnable() == false);
			

			mi = PamModuleInfo.registerControlledUnit(TurbineOperationControl.class.getName(), TurbineOperationControl.unitType);
			mi.setModulesMenuGroup(utilitiesGroup);
			mi.setHidden(!SMRUEnable.isEnable());
		}

		mi = PamModuleInfo.registerControlledUnit("alarm.AlarmControl", "Alarm");
		mi.setToolTipText("Alerts the operator when certain detections are made");
		mi.setModulesMenuGroup(utilitiesGroup);

//		if (isViewer) {
			mi = PamModuleInfo.registerControlledUnit("annotationMark.spectrogram.SpectrogramAnnotationModule", "Spectrogram Annotation");
			mi.setToolTipText("Offline marking on the spectrogram display");
			mi.setModulesMenuGroup(utilitiesGroup);
			
		mi = PamModuleInfo.registerControlledUnit("quickAnnotation.QuickAnnotationModule", "Quick Spectrogram Annotation");
		mi.setToolTipText("Manual marking on the spectrogram display using user-defined 'quick' annotations");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setHidden(!SMRUEnable.isEnable());

		// now releagate to a plugin module. 
//		mi = PamModuleInfo.registerControlledUnit(ALFAControl.class.getName(), "Master Controller");
//		mi.setToolTipText("Big brother - will keep an eye on you and look after you");
//		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setHidden(SMRUEnable.isEnable() == false);
		
		mi = PamModuleInfo.registerControlledUnit(PrintScreenControl.class.getName(), "Print Screen");
		mi.setToolTipText(PrintScreenControl.getToolTip());
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setMaxNumber(1);
		
//		mi = PamModuleInfo.registerControlledUnit("resourceMonitor.ResourceMonitor", "Resource Monitor");
//		mi.setToolTipText("Monitor JAVA System resources");
//		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setHidden(SMRUEnable.isEnable() == false);
//		}

		mi = PamModuleInfo.registerControlledUnit(RockBlockControl.class.getName(), "Short Burst Data Service Communication");
		mi.setToolTipText("Communication with the Iridium SBD service via a RockBlock+ unit");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setHidden(!SMRUEnable.isEnable());
		

		mi = PamModuleInfo.registerControlledUnit(MeygenTurbine.class.getName(), MeygenTurbine.unitType);
		mi.setToolTipText("Show turbine location on map");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setHidden(!SMRUEnable.isEnable());
		mi.setMaxNumber(1);
		

		mi = PamModuleInfo.registerControlledUnit(EffortControl.class.getName(), EffortControl.unitType);
		mi.setToolTipText("Record observer monitoring effort");
		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setHidden(SMRUEnable.isEnable() == false);
		mi.setToolTipText("Enables an observer to enter their name and infomation about which displays are being monitored");
		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit(BackupManager.class.getName(), BackupManager.defaultName);
		mi.setToolTipText("Manage automated data backups");
		mi.setModulesMenuGroup(utilitiesGroup);
		mi.setMaxNumber(1);


//		mi = PamModuleInfo.registerControlledUnit(MetaDataContol.class.getName(), MetaDataContol.unitType);
//		mi.setToolTipText("Project Meta Data");
//		mi.setModulesMenuGroup(utilitiesGroup);
//		mi.setMaxNumber(1); 
		
		if (isViewer) {
			mi = PamModuleInfo.registerControlledUnit(TethysControl.class.getName(), TethysControl.defaultName);
			mi.setToolTipText("Interface to Tethys Database");
			mi.setModulesMenuGroup(utilitiesGroup);
			mi.setMaxNumber(1);
			//mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.
			mi.setHidden(!SMRUEnable.isEnable());
			
			mi = PamModuleInfo.registerControlledUnit(RavenControl.class.getName(), RavenControl.defaultName);
			mi.setToolTipText("Import data from Raven selection tables");
			mi.setModulesMenuGroup(utilitiesGroup);
			mi.setHidden(!SMRUEnable.isEnable());			
			
		}		
		
		/*
		 * ************* End Utilities  Group *******************
		 */

		/*
		 * ************* Start Sensors Group ********************
		 */		
		mi = PamModuleInfo.registerControlledUnit(ArraySensorControl.class.getName(), "Analog Array Sensors");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Reads Depth, Heading, Pitch and Roll) using analog sensors");
		mi.setMaxNumber(1);
		
		mi = PamModuleInfo.registerControlledUnit("depthReadout.DepthControl", "Hydrophone Depth Readout");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Reads and displays hydrophone depth information");
		
		mi = PamModuleInfo.registerControlledUnit("ArrayAccelerometer.ArrayAccelControl", "Array Accelerometer");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Reads and accelerometer to orientate a hydrophone array");
//		mi.setHidden(SMRUEnable.isEnable() == false);
		mi.setMaxNumber(1);
		
		mi = PamModuleInfo.registerControlledUnit("angleMeasurement.AngleControl", "Angle Measurement");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Reads angles from a Fluxgate World shaft angle encoder. (Can be used to read angle of binocular stands)");

		mi = PamModuleInfo.registerControlledUnit("IMU.IMUControl", "IMU Measurement");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Reads IMU data (heading, pitch and roll) from file or instrument");
		mi.setHidden(!SMRUEnable.isEnable());
//		mi.setHidden(SMRUEnable.isEnable() == false);

		mi = PamModuleInfo.registerControlledUnit("d3.D3Control", "D3 Sensor Data");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Display sensor data from D3 recorders / DTags, etc");
		mi.setHidden(!SMRUEnable.isEnable());

		mi = PamModuleInfo.registerControlledUnit("soundtrap.STToolsControl", "SoundTrap Detector Import");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Tools for import of SoundTrap detector data");
		mi.setHidden(!isViewer);
		mi.setMaxNumber(1);
		
		mi = PamModuleInfo.registerControlledUnit("cpod.CPODControl2", "CPOD Detector Import");
		mi.setModulesMenuGroup(sensorsGroup);
		mi.setToolTipText("Imports CPOD data");
		//mi.setHidden(SMRUEnable.isEnable() == false);
		mi.setHidden(!isViewer);
		mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.

		/*
		 * ************* Start Displays  Group *******************
		 */

		mi = PamModuleInfo.registerControlledUnit("userDisplay.UserDisplayControl", "User Display");
		mi.setToolTipText("Creates an empty display panel which the user can add spectrograms and other displays to");		
		mi.setModulesMenuGroup(displaysGroup);

		mi = PamModuleInfo.registerControlledUnit("localTime.LocalTime", "Local Time");		
		mi.setToolTipText("Shows local time on the display");
		mi.setModulesMenuGroup(displaysGroup);

		mi = PamModuleInfo.registerControlledUnit("levelMeter.LevelMeterControl", "Level Meter");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Shows signal level meters");
		mi.setModulesMenuGroup(displaysGroup);

		mi = PamModuleInfo.registerControlledUnit(ArraySidePanelControl.class.getName(), "Array Orientation");
		mi.setModulesMenuGroup(displaysGroup);
		mi.setToolTipText("Displays array depth and orientation data");
		mi.setMaxNumber(1);
//		mi.setHidden(SMRUEnable.isEnable() == false);

		/*
		 * ************* End Displays Group *******************
		 */

		/*
		 * ************* Start Sound Processing  Group *******************
		 */

		mi = PamModuleInfo.registerControlledUnit("Acquisition.AcquisitionControl", "Sound Acquisition");	
		mi.setToolTipText("Controls input of sound data from sound cards, NI cards, etc. ");
		mi.setModulesMenuGroup(processingGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.

//		mi = PamModuleInfo.registerControlledUnit("soundtrap.STAcquisitionControl", "SoundTrap Sound Acquisition");
//		mi.setModulesMenuGroup(processingGroup);
//		mi.setToolTipText("Acquisition module for Soundtrap detector data");
//		mi.setHidden(isViewer == false);
//		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit("soundPlayback.PlaybackControl", "Sound Output");	
		mi.setToolTipText("Controls output of sound data for listening to on headphones");
		mi.setModulesMenuGroup(processingGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.
		if (isViewer) {
			mi.setMinNumber(1);
			mi.setMaxNumber(1);
		}

		mi = PamModuleInfo.registerControlledUnit("fftManager.PamFFTControl", "FFT (Spectrogram) Engine");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.
		mi.setToolTipText("Computes spectrograms of audio data");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("Filters.FilterControl", "Filters (IIR and FIR)");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Filters audio data");
		mi.setModulesMenuGroup(processingGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.


		mi = PamModuleInfo.registerControlledUnit("decimator.DecimatorControl", "Decimator");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Decimates (reduces the frequency of) audio data");
		mi.setModulesMenuGroup(processingGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX); //has FX enabled GUI.

		mi = PamModuleInfo.registerControlledUnit(CepstrumControl.class.getName(), CepstrumControl.unitType);	
		mi.addDependency(new PamDependency(FFTDataUnit.class, PamFFTControl.class.getName()));	
		mi.setToolTipText("Calculates a continuous Cepstrum from FFT Data");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("SoundRecorder.RecorderControl", "Sound recorder");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Records audio data to wav of AIF files");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("clipgenerator.ClipControl", "Clip generator");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Generates and stores short clips of sound data in response to detections");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("amplifier.AmpControl", "Signal Amplifier");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Amplifies (or attenuates) audio data");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("patchPanel.PatchPanelControl", "Patch Panel");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Reorganises and mixes audio data between channels");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("KernelSmoothing.KernelSmoothingControl", "Spectrogram smoothing kernel");	
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		mi.setToolTipText("Smooths a spectrogram of audio data");
		mi.setModulesMenuGroup(processingGroup);

		//		mi = PamModuleInfo.registerControlledUnit("spectrogramNoiseReduction.SpectrogramNoiseControl", "Spectrogram noise reduction");	
		//		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		//		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("seismicVeto.VetoController", "Seismic Veto");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));	
		mi.setToolTipText("Cuts out loud sounds from audio data");	
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("noiseMonitor.NoiseControl", "Noise Monitor");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));
		mi.setToolTipText("Measures noise in predefined frequency bands (e.g. thrid octave)");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("noiseBandMonitor.NoiseBandControl", "Noise Band Monitor");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.setToolTipText("");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("dbht.DbHtControl", "dBHt Measurement");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.setToolTipText("Measure noise relative to animal hearing threshold");		
		mi.setModulesMenuGroup(processingGroup);
		mi.setHidden(!SMRUEnable.isEnable());

		mi = PamModuleInfo.registerControlledUnit("noiseOneBand.OneBandControl", "Filtered Noise Measurement");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.setToolTipText("Measure noise in a single arbitrary filter band (replaces dBHt module)");		
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("ltsa.LtsaControl", "Long Term Spectral Average");
		mi.addDependency(new PamDependency(RawDataUnit.class, "fftManager.PamFFTControl"));	
		mi.setToolTipText("Make Long Term Spectral Average Measurements");
		mi.setModulesMenuGroup(processingGroup);

		mi = PamModuleInfo.registerControlledUnit("envelopeTracer.EnvelopeControl", "Envelope Tracing");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("");
		mi.setModulesMenuGroup(processingGroup);
		mi.setModulesMenuGroup(processingGroup);
//		mi.setHidden(SMRUEnable.isEnable() == false);
		
		mi = PamModuleInfo.registerControlledUnit(BeamFormerControl.class.getName(), BeamFormerControl.unitType);	
		mi.setModulesMenuGroup(processingGroup);
		mi.setToolTipText("Continuous Frequency Domain Beamforming");
//		mi.setHidden(SMRUEnable.isEnable() == false);

		
		
//		mi.setHidden(SMRUEnable.isEnable() == false);
		/*
		 * ************* End Sound Processing Group *******************
		 */

		/*
		 * ************* Start Detectors Group *******************
		 */

		mi = PamModuleInfo.registerControlledUnit("clickDetector.ClickControl", "Click Detector");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setToolTipText("Searches for transient sounds, attempts to assign species, measure "
				+ "bearings to source, group into click trains, etc.");
		mi.setModulesMenuGroup(detectorsGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX);

		mi = PamModuleInfo.registerControlledUnit("clickTrainDetector.ClickTrainControl", "Click Train Detector");
		mi.addDependency(new PamDependency(RawDataUnit.class, "clickDetector.ClickControl"));	
		mi.setToolTipText("Searches for click trains in detected clicks.");
		mi.addGUICompatabilityFlag(PamGUIManager.FX);
		mi.setModulesMenuGroup(detectorsGroup);

		mi = PamModuleInfo.registerControlledUnit("whistlesAndMoans.WhistleMoanControl", 
				"Whistle and Moan Detector");	
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		mi.setToolTipText("Searches for tonal noises. Measures bearings and locations of source. Replaces older Whistle Detector");
		mi.setModulesMenuGroup(detectorsGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX);

		mi = PamModuleInfo.registerControlledUnit("whistleDetector.WhistleControl", "Whistle Detector");	
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		mi.setToolTipText("Searches for tonal noises. Measures bearings and locations of source");
		mi.setModulesMenuGroup(detectorsGroup);

		mi = PamModuleInfo.registerControlledUnit("IshmaelDetector.EnergySumControl", "Ishmael energy sum");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		mi.setModulesMenuGroup(detectorsGroup);
		mi.setToolTipText("Detects sounds with energy in a specific frequency band");

		mi = PamModuleInfo.registerControlledUnit("IshmaelDetector.SgramCorrControl", "Ishmael spectrogram correlation");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		mi.setToolTipText("Detects sounds matching a user defined 'shape' on a spectrogram");
		mi.setModulesMenuGroup(detectorsGroup);

		mi = PamModuleInfo.registerControlledUnit("IshmaelDetector.MatchFiltControl", "Ishmael matched filtering");	
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.setToolTipText("Detects sounds using a user defined matched filter");	
		mi.setModulesMenuGroup(detectorsGroup);

		mi = PamModuleInfo.registerControlledUnit("likelihoodDetectionModule.LikelihoodDetectionUnit", "Likelihood Detector" );
		mi.addDependency( new PamDependency( RawDataUnit.class, "Acquisition.AcquisitionControl" ) );
		mi.setToolTipText("An implementation of a likelihood ratio test");
		mi.setModulesMenuGroup(detectorsGroup);

		mi = PamModuleInfo.registerControlledUnit("RightWhaleEdgeDetector.RWEControl", "Right Whale Edge Detector");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));
		mi.setToolTipText("Detects right whale upsweep calls");
		mi.setModulesMenuGroup(detectorsGroup);	
//		mi.setHidden(SMRUEnable.isEnable() == false);
		
		// remove GPL detector - too slow for real-time use
		mi = PamModuleInfo.registerControlledUnit("gpl.GPLControlledUnit", "Generalised Power Law Detector");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));
		mi.setToolTipText("Generalised Power Law Detector for tonal sounds");
		mi.setModulesMenuGroup(detectorsGroup);	

		mi = PamModuleInfo.registerControlledUnit("soundtrap.STClickControl", "SoundTrap Click Detector");
		mi.setModulesMenuGroup(detectorsGroup);
		mi.setToolTipText("Click Detector module for Soundtrap detector data only");
//		mi.setHidden(isViewer == false);

//		mi = PamModuleInfo.registerControlledUnit("WorkshopDemo.WorkshopController", "Workshop Demo Detector");
//		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));
//		mi.setToolTipText("Simple demo detector for programmers");
//		mi.setModulesMenuGroup(detectorsGroup);	


		//		mi = PamModuleInfo.registerControlledUnit("EdgeDetector.EdgeControl", "Edge Detector");		
		//		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));		
		//		mi.setModulesMenuGroup(detectorsGroup);

		

		/*
		 * ************* End Detectors Group *******************
		 */

		/*
		 * ************* Start Classifiers Group **************
		 * 
		 */
		
		mi = PamModuleInfo.registerControlledUnit("whistleClassifier.WhistleClassifierControl", "Whistle Classifier");	
		mi.addDependency(new PamDependency(AbstractWhistleDataUnit.class, "whistlesAndMoans.WhistleMoanControl"));	
		mi.setToolTipText("Analyses multiple whistle contours to assign to species");
		mi.setModulesMenuGroup(classifierGroup);

		mi = PamModuleInfo.registerControlledUnit("rocca.RoccaControl", "Rocca");
		mi.addDependency(new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));	
		mi.setToolTipText("Classifies dolphin whistles selected from the spectrogram display");
		mi.setToolTipText("Real-time acoustic species identification of delphinid whistles and clicks");
		mi.setModulesMenuGroup(classifierGroup);
		
		mi = PamModuleInfo.registerControlledUnit("matchedTemplateClassifer.MTClassifierControl", "Matched Template Click Classifer");
		mi.addDependency(new PamDependency(ClickDetection.class, "clickDetector.ClickControl"));	
		mi.setToolTipText("Classifies clicks based on an ideal template to match and a template to reject. "
				+ "An example of this is to classify beaked whale clicks in an environment with dolphin clicks");
		mi.addGUICompatabilityFlag(PamGUIManager.FX);
		mi.setModulesMenuGroup(classifierGroup);
		
		
		mi = PamModuleInfo.registerControlledUnit("rawDeepLearningClassifier.DLControl", "Deep Learning Classifier");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.setToolTipText("Classifies sections of raw acoustic data based on an imported deep learning classifier");
		mi.setModulesMenuGroup(classifierGroup);
		mi.addGUICompatabilityFlag(PamGUIManager.FX);
		

		/*
		 * ************* End Classifiers Group *******************
		 */

		/*
		 * ************* Start Localisation Group **************
		 */
		mi = PamModuleInfo.registerControlledUnit(BearingLocaliserControl.class.getName(), "Bearing Localiser");	
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("Estimate bearing to detections or spectrogram marks from small aperture arrays");
//		mi.setHidden(SMRUEnable.isEnable() == false);

		
		mi = PamModuleInfo.registerControlledUnit(Group3DLocaliserControl.class.getName(), Group3DLocaliserControl.unitType);	
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("2D and 3D Localisation for large aperture arrays");
//		mi.setHidden(SMRUEnable.isEnable() == false);

		mi = PamModuleInfo.registerControlledUnit(DetectionGroupControl.class.getName(), "Detection Grouper");	
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("Groups detections and other data using manual annotations on PAMGuard displays");

//		mi = PamModuleInfo.registerControlledUnit(BeamFormLocaliserControl.class.getName(), BeamFormLocaliserControl.unitType);	
//		mi.setModulesMenuGroup(localiserGroup);
//		mi.setToolTipText("Localise detections or spectrogram marks with beamforming algorithms");
//		mi.setHidden(true); // now obsolete.
//		mi.setHidden(SMRUEnable.isEnable() == false);

//		mi = PamModuleInfo.registerControlledUnit(CBLocaliserControl.class.getName(), CBLocaliserControl.unitType);	
//		mi.setModulesMenuGroup(localiserGroup);
//		mi.setToolTipText("Localise by crossing bearings from multiple hydrophone groups");
//		mi.setHidden(SMRUEnable.isEnable() == false);

		mi = PamModuleInfo.registerControlledUnit("IshmaelLocator.IshLocControl", "Ishmael Locator");	
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("Locates sounds extracted either from areas marked out on a spectrogram display or using output from a detector");

		mi = PamModuleInfo.registerControlledUnit("loc3d_Thode.TowedArray3DController", "Multipath 3D Localiser");
		mi.addDependency(new PamDependency(ClickDetection.class, "clickDetector.ClickControl"));
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("Locates sounds detected by the click detector using surface echo's to obtain slant angles and generate a 3-D location");

//		mi = PamModuleInfo.registerControlledUnit("staticLocaliser.StaticLocaliserControl", "Large Aperture 3D Localiser");
//		//mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
//		mi.setModulesMenuGroup(localiserGroup);
//		mi.setToolTipText("Locates sounds using wide aperture arrays of hydrophones");
//		mi.setMaxNumber(1);

//		mi = PamModuleInfo.registerControlledUnit("targetMotionModule.TargetMotionControl", "Target Motion Localiser");
//		//mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
//		mi.setModulesMenuGroup(localiserGroup);
//		mi.setToolTipText("Locates sounds in 2D and 3D detected using towed hydrophone arrays");
//		mi.setMaxNumber(1);
//		mi.setHidden(SMRUEnable.isEnable() == false);
		// TODO: Move all DIFAR modules into sub-menu under localisation>DIFAR>
		mi = PamModuleInfo.registerControlledUnit("Azigram.AzigramControl", "DIFAR Azigram Engine");
		mi.addDependency( new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl"));
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("(BETA) Azigram engine for multiplexed DIFAR data (BETA)");
		
		mi = PamModuleInfo.registerControlledUnit("difar.beamforming.BeamformControl", "DIFAR Directional Audio");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));
		mi.addDependency(new PamDependency(GpsDataUnit.class, "GPS.GPSControl"));
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("Audio from a DIFAR sonobuoy that has been beamformed at a user-specified single steering angle. This module can be used to reduce directional masking noise.");
		
		mi = PamModuleInfo.registerControlledUnit("difar.DifarControl", "DIFAR Localisation");
		mi.addDependency(new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl"));	
		mi.setModulesMenuGroup(localiserGroup);
		mi.setToolTipText("DIFAR Sonobuoy localisation module - takes raw data source with multiplexed directional audio data");
		mi.setMaxNumber(1);
		
		/*
		 *************** End Localisation Group **************** 
		 */
		
		/*
		 * ************* Start Display Group ********************
		 */
		//the displays group is only used in the PAMGuard FX gui so far.  
		if (PamGUIManager.isFX()) {
			mi = PamModuleInfo.registerControlledUnit("dataPlotsFX.TDDisplayController", "Time Display" );
			mi.setToolTipText("Display time series data");
			mi.setModulesMenuGroup(displaysGroup);
			mi.addGUICompatabilityFlag(PamGUIManager.FX);

			mi = PamModuleInfo.registerControlledUnit("detectionPlotFX.DetectionDisplayControl2", "Detection Display" );
			mi.setToolTipText("Display detection data");
			mi.setModulesMenuGroup(displaysGroup);
			mi.addGUICompatabilityFlag(PamGUIManager.FX);
		}
		/**
		 ************* End Display Group ********************
		 */

		/*
		 * Visual group
		 */

//		mi = PamModuleInfo.registerControlledUnit("beakedWhaleProtocol.BeakedControl", "Beaked Whale Protocol");
//		//	mi.addDependency(new PamDependency(GpsDataUnit.class, "GPS.GPSControl"));
//		mi.setModulesMenuGroup(visualGroup);
//		mi.setToolTipText("");
//		mi.setHidden(SMRUEnable.isEnable() == false);
		//	mi.setMaxNumber(1);
		//		}

		mi = PamModuleInfo.registerControlledUnit("videoRangePanel.VRControl", "Video Range");
		mi.setModulesMenuGroup(visualGroup);
		mi.setToolTipText("Calculates ranges based on angles measured from video, observer height and earth radius");
		//		mi.setMaxNumber(1);

		mi = PamModuleInfo.registerControlledUnit("loggerForms.FormsControl", "Logger Forms");
		mi.setModulesMenuGroup(visualGroup);
		mi.addDependency(new PamDependency(null, "generalDatabase.DBControlUnit"));
		mi.setToolTipText("Replicates the functionality of User Defined Forms in the IFAW Logger software");
		mi.setMaxNumber(1);

		//		}

		//		mi = PamModuleInfo.registerControlledUnit("autecPhones.AutecPhonesControl", "AUTEC Phones");
		//		mi.setModulesMenuGroup(visualGroup);
		//		mi.setMaxNumber(1);

		/*
		 * ************* End Visual Group ********************
		 */
//		mi = PamModuleInfo.registerControlledUnit("smlGainControl.SMLGainControl", "SML Gain Control");
//		mi.setModulesMenuGroup(smlGroup);
//		mi.setToolTipText("Automatically controls the gain and filter settings of Seiche Measurements Ltd preamlifiers");
//		mi.setMaxNumber(1);
//		mi.setHidden(SEICHEEnable.isEnable() == false);

//		mi = PamModuleInfo.registerControlledUnit("smlPingerControl.SMLPingerControl", "SML Pinger Control");
//		mi.setModulesMenuGroup(smlGroup);
//		mi.setToolTipText("Automatically controls an Seiche Measurements Ltd Pinger");
//		mi.setMaxNumber(1);
//		mi.setHidden(SEICHEEnable.isEnable() == false);

		// two modules from Brian Miller ...
//		mi = PamModuleInfo.registerControlledUnit("echoDetector.EchoController", "Echo Detector" );
//		mi.addDependency( new PamDependency( ClickDetection.class, "clickDetector.ClickControl" ) );
//		mi.setToolTipText("Detects echos from the click detector");
//		mi.setModulesMenuGroup(measurementGroup);
		//				mi.setHidden(SMRUEnable.isEnable() == false);

//		mi = PamModuleInfo.registerControlledUnit("ipiDemo.IpiController", "Sperm whale IPI computation" );
//		mi.addDependency( new PamDependency( EchoDataUnit.class, "echoDetector.EchoController" ) );
//		mi.setToolTipText("Measures inter pulse interval from the click detector");
//		mi.setModulesMenuGroup(measurementGroup);
		//				mi.setHidden(SMRUEnable.isEnable() == false);
		
		// load any plugins in the plugin folder
		loadPlugins(mi);
		
	}



	/**
	 * Add any remaining REQUIRED modules.<br>
	 * this get's called after the PamController has loaded it's main settings. 
	 * So at this point, go through all the PamModuleInfo's and check that 
	 * all have at least the minimum number required 
	 * @return true
	 */
	public synchronized boolean startModel() {
		/*
		 */

		PamSettingManager.getInstance().registerSettings(this);

		ArrayList<PamModuleInfo> moduleInfoList = PamModuleInfo.getModuleList();
		PamModuleInfo moduleInfo;
		for (int i = 0; i < moduleInfoList.size(); i++) {
			moduleInfo = moduleInfoList.get(i);
			while (moduleInfo.getNInstances() < moduleInfo.getMinNumber()) {
				pamController.addControlledUnit(moduleInfo.create(moduleInfo.getDefaultName()));
			}
		}
		pamController.notifyModelChanged(PamControllerInterface.CHANGED_MULTI_THREADING);
		
//		writeModuleList();
		
		return true;
	}

	/**
	 * Really just debug output to make a list of all modules ...
	 */
	private void writeModuleList() {
		ArrayList<PamModuleInfo> moduleInfoList = PamModuleInfo.getModuleList();
		System.out.println("No.,Module Group,Module Name,Development,Tip");
		for (int i = 0; i < moduleInfoList.size(); i++) {
			PamModuleInfo moduleInfo = moduleInfoList.get(i);
			ModulesMenuGroup menuGroup = moduleInfo.getModulesMenuGroup();
			String groupName = "";
			if (menuGroup != null) {
				groupName = menuGroup.getMenuName();
			}
			Boolean isDev = moduleInfo.isHidden();
			String devStr = "";
			if (isDev) devStr = "Development";
			
			System.out.printf("%d,%s,%s,%s,%s\n",i+1,groupName, moduleInfo.getDefaultName(),devStr,moduleInfo.getToolTipText());
			
		}
	}

	/* (non-Javadoc)
	 * @see PamModel.PamModelInterface#stopModel()
	 */
	public void stopModel() {

	}

	public boolean modelSettings(JFrame frame) {
		PamModelSettings newSettings = ThreadingDialog.showDialog(frame, pamModelSettings);
		if (newSettings != null) {
			boolean changed = (!newSettings.equals(pamModelSettings));
			pamModelSettings = newSettings.clone();
			if (changed) {
				pamController.notifyModelChanged(PamControllerInterface.CHANGED_MULTI_THREADING);
			}
			pamController.setupGarbageCollector();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Set multithreading for all of PAMGUARD. 
	 * <p>
	 * Be warned that this function should very rarely ever be called and 
	 * has been included only so that the Likelihood detector can turn off
	 * multithreading. Once multithreading has been debugged in the Likelihood 
	 * detector, this function will be removed or deprecated. 
	 * @param multithreading
	 */
	public void setMultithreading(boolean multithreading) {
		pamModelSettings.multiThreading = multithreading;
		pamController.notifyModelChanged(PamControllerInterface.CHANGED_MULTI_THREADING);
	}
	/**
	 * @return Returns the dependencyManager.
	 */
	public DependencyManager getDependencyManager() {
		return dependencyManager;
	}

	@Override
	public Serializable getSettingsReference() {
		return pamModelSettings;
	}

	@Override
	public long getSettingsVersion() {
		return PamModelSettings.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "PAMGUARD Data Model";
	}

	@Override
	public String getUnitType() {
		return "PAMGUARD Data Model";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		pamModelSettings = ((PamModelSettings)pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	public boolean isMultiThread() {
		return pamModelSettings.multiThreading;
	}

	public PamModelSettings getPamModelSettings() {
		return pamModelSettings;
	}
	
	/**
	 * Method to load jar files containing plugins, found in the plugins folder
	 */
	public void loadPlugins(PamModuleInfo mi) {
		
		// clear the current list
		pluginList.clear();
		daqList.clear();
		/*
		 * If developing a new PAMPlugin in eclipse, the easiest way to do it is to make a new
		 * Eclipse project for your plugin code. Within that project, copy this PamModel class 
		 * and put it into a package with exactly the same name within your new project (i.e. PamModel)
		 * then add a line to the copy of this class to explicitly add the class name of your new
		 * plugin, e.g. 
		pluginList.add(new CollatorPlugin());
		 * The plugin information will then be extracted from the PamPluginInterface in the same way as
		 * it would should it be being used as a real plugin, inclusing help information which will be 
		 * added to the PAMGuard help 
		 * 
		 * When you export the code for your plugin to a jar file, remember to NOT inlcude the copy of 
		 * PamModel !
		 */
		
//		pluginList.add(new MorlaisWP1aPlugin());

		// Load up whatever default classloader was used to create this class.  Must use the same classloader
		// for all plugins, or else we will not be able to create proper dependencies between them or be able
		// to save properties in the psf file.  Found this problem because ipiDemo requires the
		// echoDetector, but when they were loaded by different URLClassLoaders the ipiDemo process could not find
		// an echoDetector data unit.
//		URLClassLoader cl = (URLClassLoader) this.getClass().getClassLoader();
//		ClassLoader cl = this.getClass().getClassLoader();
		
		
		// first, compile a list of all classes implementing the PamPluginInterface.  Have to jump through a
		// lot of hoops to try and get the installed directory of the jar from a class running inside the
		// jar.  This 
//		String jarPath = null;
//		try {
//			File jarURL = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//			jarPath = jarURL.getParentFile().getPath();
//		} catch (URISyntaxException e1) {
//			System.out.println("Error finding installation folder of jar file: " + e1.getMessage());
//			e1.printStackTrace();
//		} catch (NullPointerException e) {
//			System.out.println("null pointer Error finding installation folder of jar file: " + e.getMessage());
//		}
		File dir = new File(PamController.getInstance().getInstallFolder() + pluginsFolder);
		System.out.println("Searching " + dir.getAbsolutePath() + " for plugins...");
		if (dir.exists() && dir.isDirectory()) {
			
			List<File> jarList;
			try {
				jarList = (List<File>) FileFinder.findFileExt(dir, "jar");
			} catch (FileNotFoundException e) {
				//System.out.println("Folder " + dir.getAbsolutePath() + " does not contain any jar files.");
				return;
			}
			if (jarList.size()==0) {
				System.out.println("   Folder does not contain any jar files.");
//				return;
			}
			
			// loop through the jar files, looking for class files implementing PamPluginInterface or DaqSystemInterface.
			// There are a lot of references to the method getPluginBeingLoaded, which should return the name of the
			// class that is currently being accessed.  If the PamExceptionHandler catches a runtime error it will
			// clear this variable, which means we can use it as a de facto flag to indicate whether or not there are
			// problems with the plugin.  Since the plugin could fail in any spot, the variable is tested in quite
			// a few places to stop execution in case of an error
			for (int i=0; i<jarList.size(); i++) {
				try {
					
					String jarName = jarList.get(i).getAbsolutePath();
					JarFile jarFile = new JarFile(jarName);
					Enumeration<JarEntry> e = jarFile.entries();
					
					// cycle through the jar file.  Load the classes found and test all interfaces
					while (e.hasMoreElements()) {
					    JarEntry je = e.nextElement();
					    if(je.isDirectory() || !je.getName().endsWith(".class")){
					        continue;
					    }

					    // convert the controller class name to binary format
						String className = je.getName().substring(0,je.getName().length()-6);	// get rid of the .class at the end
					    className = className.replace('/', '.');	// convert to binary file name, as required by loadClass method					
					    
					    // get the URL to look at the plugin class inside the jar file, and use the Java Reflection API
					    // to add that URL to the default classloader path.
					    URL newURL = jarList.get(i).toURI().toURL();
					    
					    // original method
//					    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
//					    method.setAccessible(true);
//					    method.invoke(cl, newURL);
					    
					    // first fix attempt - create a brand new URLClassLoader. As expected, we get a ClassCastException when trying
					    // to load the parameters so we can't save params using this method
//					    URL[] newURLArray = new URL[1];
//					    newURLArray[0] = newURL;
//						cl = new URLClassLoader(newURLArray);
						
					    // second attempt - custom class loader with the system app loader specified as the parent.  Loads controlled unit, but
					    // as before it doesn't load the parameters
					    classLoader.addURL(newURL);

					    // third attempt
//					    Class<?> genericClass = cl.getClass();
//					    Method method = genericClass.getSuperclass().getDeclaredMethod("addURL", new Class[] {URL.class});
//					    method.setAccessible(true);
//					    method.invoke(cl, new Object[] {newURL});

					    
					    
					    
					    // Save the name of the class to the global pluginBeingLoaded variable, and load the class.
					    this.setPluginBeingLoaded(className);
//						Class c = cl.loadClass(className);
					    /*
					     * Was Failing here  if a plugin is loaded before a plugin that has classes
					     * this one is dependent on. Seems that if we set the second parameter to 
					     * false then it doesn't fully initialize the class, so will be OK, get past
					     * this stage and fully load the class when it's used.  
					     */
						Class c = Class.forName(className, false, classLoader);
						if (getPluginBeingLoaded()==null) {
							continue;
						}
						
						// loop through the interfaces
						Class[] intf = c.getInterfaces();
						for (int j=0; j<intf.length; j++) {
							
							// Put this entire section in a try/catch, in case the developer hasn't coded
							// the plugin properly.  We need to catch Throwable, not Exception, in order
							// to catch everything.
							try {
								
								// first check for interfaces that implement PamPluginInterface
								if (intf[j].getName().equals("PamModel.PamPluginInterface")) {
									
									// create an instance of the interface class.  
									Constructor constructor = c.getDeclaredConstructor(null);
									PamPluginInterface pf = (PamPluginInterface) constructor.newInstance(null);
									if (getPluginBeingLoaded()==null) {
										continue;
									}

									// Let the user know which valid plugins have been found
									System.out.printf("   Loading plugin interface for %s : %s version %s\n",
											pf.getDefaultName(), pf.getClassName(), pf.getVersion());
									if (getPluginBeingLoaded()==null) {
										continue;
									}

									// only add the plugin to the list if this is a valid run mode
//									if (isAllowedMode(pf)) {
										pf.setJarFile(jarName);	// save the name of the jar, so that javahelp can find the helpset
										if (getPluginBeingLoaded()==null) {
											continue;
										}

										pluginList.add(pf); // add it to the list
//									} else {
//										System.out.println("     Warning: " + pf.getDefaultName()+" cannot run in this mode.  Skipping module.");									
//									}
									if (getPluginBeingLoaded()==null) {
										continue;
									}
								}
								
								// now check for interfaces that implement DaqSystemInterface
								if (intf[j].getName().equals("Acquisition.DaqSystemInterface")) {
									Constructor constructor = c.getDeclaredConstructor(null);
									DaqSystemInterface pf = (DaqSystemInterface) constructor.newInstance(null);
//									DaqSystemInterface pf = (DaqSystemInterface) c.newInstance(); // create an instance of the interface class
									if (getPluginBeingLoaded()==null) {
										continue;
									}

									System.out.printf("   Loading daq plugin interface for %s version %s\n",
											pf.getDefaultName(), pf.getVersion());
//									System.out.println("   Creating instance of " + pf.getDefaultName() + ": "  + className);
									if (getPluginBeingLoaded()==null) {
										continue;
									}

									pf.setJarFile(jarName);	// save the name of the jar, so that javahelp can find the helpset
									if (getPluginBeingLoaded()==null) {
										continue;
									}
									
									daqList.add(pf); // add it to the list
								}
								
							// if there were any errors while accessing the plugin, let the user know and then move
							// on to the next plugin.
							} catch (Throwable e1) {
								e1.printStackTrace();
								String title = "Error accessing plug-in module";
								String msg = "There is an error with the plug-in module " + className + ".<p>" +
										"This may have been caused by an incompatibility between " +
										"the plug-in and this version of PAMGuard.  Please check the developer's website " +
										"for help.<p>" +
										"This plug-in will not be available for loading<p>" + 
										e1.getClass().getName() + ": " + e1.getLocalizedMessage();
								String help = null;
								int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, e1);
								System.err.println("Exception while loading " +	className);
								System.err.println(e1.getMessage());								
								continue;
							}
						}						
					}
				} catch (Throwable ex) {
					ex.printStackTrace();
					String title = "Error accessing plug-in module";
					String msg = "There is an error with the plug-in module " + jarList.get(i).getName() + ".<p>" +
							"This may have been caused by an incompatibility between " +
							"the plug-in and this version of PAMGuard.  Please check the developer's website " +
							"for help.<p>" +
							"This plug-in will not be available for loading<p>"  + 
							ex.getClass().getName() + ": " + ex.getLocalizedMessage();
					String help = null;
					int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, ex);
					System.err.println("Exception while loading " +	jarList.get(i).getName());
					continue;
				}
			}
			
			// if there weren't any valid files, warn the user
			if (pluginList.isEmpty()) {
				System.out.println("Folder does not contain any jar files with a valid PamPluginInterface class.");
				
			// otherwise go through the plugin list and add each plugin to the menu items.
			// Put this entire section in a try/catch, in case the developer hasn't coded
			// the plugin properly.  We need to catch Throwable, not Exception, in order
			// to catch everything (e.g. if one of the abstract methods is missing, java
			// throws AbstractMethodError.  This is an error, not an exception, so if
			// we want to catch it we need to catch Throwable)
			} else {
				for (PamPluginInterface pf : pluginList ) {
					
				    try {
						// Save the name of the class to the global pluginBeingLoaded variable
						this.setPluginBeingLoaded(pf.getClassName());
						
						// instantiate the plugin control class using the custom class loader
						try {
							File classFile = new File(pf.getJarFile());		
							//URLClassLoader cl = new URLClassLoader(new URL[]{classFile.toURI().toURL()});
//							mi = PamModuleInfo.registerControlledUnit(pf.getClassName(), pf.getDescription(),cl);
							mi = PamModuleInfo.registerControlledUnit(pf.getClassName(), pf.getDescription(),classLoader);
							if (!isAllowedMode(pf)) {
								mi.setHidden(true);
								System.out.println("     Warning: " + pf.getDefaultName()+" cannot run in this mode.  hiding module.");	
							}
						} catch (Exception e) {
							System.err.println("   Error accessing " + pf.getJarFile());
							e.printStackTrace();
							pluginList.remove(pf);
							continue;
						}
						
						// if we weren't able to access the control class, warn the user and go to the next plugin
						if (mi==null) {
							System.err.println("   Error loading " + pf.getClassName());
							pluginList.remove(pf);
							continue;
						}
						
						// set the rest of the parameters
						mi.setToolTipText(pf.getToolTip());
						mi.setHidden(pf.isItHidden());
						mi.setMinNumber(pf.getMinNumber());
						mi.setMaxNumber(pf.getMaxNumber());
						if (getPluginBeingLoaded()==null) {
							pluginList.remove(pf);
							continue;
						}

						// find the appropriate ModuleMenuGroup, or create a new one if needed
						boolean found=false;
						for (int j=0; j<modulesMenuGroups.size(); j++) {
							if(modulesMenuGroups.get(j).getMenuName()==pf.getMenuGroup()) {
								mi.setModulesMenuGroup(modulesMenuGroups.get(j));
								found=true;
								break;
							}
						}
						if (!found) {
							ModulesMenuGroup pluginGroup = new ModulesMenuGroup(pf.getMenuGroup());
							modulesMenuGroups.add(pluginGroup);
							mi.setModulesMenuGroup(pluginGroup);
						}
						if (getPluginBeingLoaded()==null) {
							pluginList.remove(pf);
							continue;
						}

						// set the dependency, if there is one
						if (pf.getDependency()!=null) {
							mi.addDependency(pf.getDependency());
						}
						if (getPluginBeingLoaded()==null) {
							pluginList.remove(pf);
							continue;
						}
						
					// if there were any errors while accessing the plugin, let the user know and remove
					// the plugin from the list.
					} catch (Throwable e1) {
						String title = "Error accessing plug-in module";
						String msg = "There is an error with the plug-in module " + pf.getDefaultName() + ".<p>" +
								"This may have been caused by an incompatibility between " +
								"the plug-in and this version of PAMGuard.  Please check the developer's website " +
								"for help.<p>" +
								"This plug-in will not be available for loading";
						String help = null;
						int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, e1);
						System.err.println("Exception while loading " +	pf.getDefaultName());
						pluginList.remove(pf);
						continue;
					}
				}
				
			}

			// if there weren't any valid files, warn the user.  Unlike the plugins which were created above, we don't
			// need to do anything else with the DAQ System list here.  The list will be queried if the AcquisitionControl
			// module is created
			if (daqList.isEmpty()) {
				System.out.println("Folder does not contain any jar files with a valid DaqSystemInterface class.");
			}
		} else {
			System.out.println("Error - can't find plugins folder " + dir.getAbsolutePath());
		}
		this.clearPluginBeingLoaded();
	}

	/**
	 * Can this plugin run in this mode ? 
	 * Even if it can't, leave it in the model, but don't allow any to be created. 
	 * @param plugin
	 * @return
	 */
	private boolean isAllowedMode(PamPluginInterface pf) {
		return (pf.allowedModes()==PamPluginInterface.ALLMODES ||
				(pf.allowedModes()==PamPluginInterface.VIEWERONLY && isViewer ) ||
				(pf.allowedModes()==PamPluginInterface.NOTINVIEWER && !isViewer));
	}
	/**
	 * Return a list of the plugins found in the plugin folder
	 * @return
	 */
	public List<PamPluginInterface> getPluginList() {
		return pluginList;
	}

	/**
	 * Return a list of DAQ Systems found in the plugins folder
	 * @return
	 */
	public List<DaqSystemInterface> getDaqList() {
		return daqList;
	}

	
	public String getPluginBeingLoaded() {
		return pluginBeingLoaded;
	}

	public void setPluginBeingLoaded(String pluginBeingLoaded) {
		this.pluginBeingLoaded = pluginBeingLoaded;
	}
	
	public void clearPluginBeingLoaded() {
		this.pluginBeingLoaded="none";
	}
	
	
	public class PluginClassloader extends URLClassLoader {

	    public PluginClassloader(URL[] urls, ClassLoader parent) {
	        super(urls, parent);
	    }

	    @Override
		public void addURL(URL url) {
	        super.addURL(url);
	    }
	}


	public PluginClassloader getClassLoader() {
		return classLoader;
	}
	
	
}