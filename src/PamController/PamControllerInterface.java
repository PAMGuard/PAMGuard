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
package PamController;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JFrame;

import PamModel.PamModel;
import PamModel.PamModuleInfo;
import PamView.GuiFrameManager;
import PamView.PamViewInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

/**
 * @author Doug Gillespie
 *         <p>
 *         Contoller interface for Pamguard. Any controller working with the
 *         Pamguard MVC should implement these methods in order that the model
 *         and the view can communicate with the controller
 * 
 */
public interface PamControllerInterface {
	// Commands received from the View to send to Model


	/**
	 * Adds a PamControlledUnit to the controller.
	 * 
	 * @param controlledUnit -
	 *            Reference to a PamcontrolledUnit
	 */
	public void addControlledUnit(PamControlledUnit controlledUnit);
	
	/**
	 * Adds a new view to the system
	 * @param newView
	 */
	public void addView(PamViewInterface newView);
	
	/**
	 * Removes a PamControlledUnit from the controller
	 * @param controlledUnit
	 */
	public void removeControlledUnt(PamControlledUnit controlledUnit);

	/**
	 * Returns a reference to a PamControlledUnit within the COntroller
	 * 
	 * @param iUnit
	 *            Index of the unit
	 * @return reference to a PamControlledUnit
	 */
	public PamControlledUnit getControlledUnit(int iUnit);


	/**
	 * Finds a PamControlledUnit of a given type but with any name
	 * @param unitType Type of PamControlledUnit
	 * @return reference to PamControlledUnit
	 */
	public PamControlledUnit findControlledUnit(String unitType);

	/**
	 * Finds a PamControlledUnit of a given type and name
	 * @param unitType Type of PamControlledUnit
	 * @param unitName Name of PamControlledUnit
	 * @return reference to PamControlledUnit
	 */
	public PamControlledUnit findControlledUnit(String unitType, String unitName);
	/**
	 * Gets the total number of PamControlledUnits
	 * 
	 * @return the number of PamControlledUnits
	 */
//	public int pamControlledUnitCount();
	public int getNumControlledUnits();

	/**
	 * Gets a reference to the PamModel (where all the data are stored and the
	 * algorithms are running)
	 * 
	 * @return Reference to the PamGuard model
	 */
	public PamModel getModelInterface();

	/**
	 * Instruction to the controller (probably from a menu command inthe view)
	 * that data collection should start.
	 * 
	 * @return true if successful
	 */
	public boolean pamStart();

	/**
	 * Instruction to the controller (probably from a menu command inthe view)
	 * that data collection should stop.
	 */
	public void pamStop();

	/**
	 * Menu command to open dialog to adjust model settings
	 * @return true if dialog returned Ok. 
	 */
	public boolean modelSettings(JFrame frame);
	
	/**
	 * Notification received from the model that data collection has started.
	 */
	public void pamStarted();

	/**
	 * Sent from the model when Pam ends - this can happen when a file finishes
	 * or after Pam ends following a PamStop command sent by the controller
	 */
	public void pamEnded();
	
	/**
	 * Take actions to alow the user to change the order modules apear in.
	 *
	 */
	public boolean orderModules(Frame parentFrame);
	
	/**
	 * Add a new PamControlledUnit
	 * @param moduleInfo Information about the PamControlled unit to add 
	 * @return true if created sucessfully
	 */
	public PamControlledUnit addModule(Frame parentFrame, PamModuleInfo moduleInfo);
	
	/**
	 * new viewing times for the Pamguard Viewer
	 */
//	public void getNewViewTimes(Frame frame);


	/**
	 * Get the GUI Frame manager.
	 * @return GUIFrameManager
	 */
	public GuiFrameManager getGuiFrameManager();
	
	/**
	 * @return List of PamDataBlocks in all PamProcesses in all
	 *         PamControlledUnits that contain FFT data.
	 */
	public ArrayList<PamDataBlock> getFFTDataBlocks();

	/**
	 * Gets a specific data block from the list, or null.
	 * @param id
	 * @return an FFT data block at index id within Pamguards
	 * list of FFT type data blocks
	 */
	public PamDataBlock getFFTDataBlock(int id);
	
	/**
	 * Gets a specific data block from the list, or null.
	 * @param name
	 * @return the first FFT data block at with the given name within Pamguards
	 * list of FFT type data blocks
	 */
	public PamDataBlock getFFTDataBlock(String name);
	/**
	 * @return List of PamDataBlocks in all PamProcesses in all
	 *         PamControlledUnits that contain raw audio data.
	 */
	public ArrayList<PamDataBlock> getRawDataBlocks();

	/**
	 * Gets a specific data block from the list, or null.
	 * @param id
	 * @return a raw data block at index id within Pamguards
	 * list of RAW type data blocks
	 */
	public PamRawDataBlock getRawDataBlock(int id);
	
	/**
	 * Gets a specific data block from the list, or null.
	 * @param name
	 * @return the first raw data block at with the given name within Pamguards
	 * list of RAW type data blocks
	 */
	public PamRawDataBlock getRawDataBlock(String name);

	/**
	 * @return List of PamDataBlocks in all PamProcesses in all
	 *         PamControlledUnits that contain detector output data.
	 */
	public ArrayList<PamDataBlock> getDetectorDataBlocks();

	/**
	 * Gets a specific data block from the list, or null.
	 * @param id
	 * @return PamDataBlock -- a detector data block at index id 
	 * within Pamguard's list of RAW type data blocks.
	 */
	public PamDataBlock getDetectorDataBlock(int id);
	
	/**
	 * Gets a specific data block from the list, or null.
	 * @param name
	 * @return PamDataBlock -- the first detector data block with the
	 * given name within Pamguard's list of DETECTOR type data blocks.
	 */
	public PamDataBlock getDetectorDataBlock(String name);

	/**
	 * @return List of PamDataBlocks in all PamProcesses in all
	 *         PamControlledUnits that contain data of a specific type.
	 */
	public ArrayList<PamDataBlock> getDetectorEventDataBlocks() ;
	

	public PamDataBlock getDetectorEventDataBlock(int id);

	public PamDataBlock getDetectorEventDataBlock(String name) ;
	/**
	 * Returns an ArrayList of PamDataBlocks from all PamProcesses in all PamControlledUnits
	 *  that contain data of a specific type.  In order to return a list of PamDataBlocks that contain
	 *   objects implementing a certain interface (such as AcousticDataUnit or PamDetection),
	 *   the includeSubClasses boolean must be TRUE
	 * @param blockType DataType of PamDatablock
	 * @param includeSubClasses whether or not to include classes that extend/implement the
	 *  blockType parameter.  
	 * @return List of PamDataBlocks in all PamProcesses in all
	 *         PamControlledUnits that contain data of a specific type.
	 */
	public ArrayList<PamDataBlock> getDataBlocks(Class blockType, boolean includeSubClasses);

	/** Find a block of a given type with the id number, or null if the number
	 * is out of range.
	 * 
	 * @param blockType
	 * @param id -- the block id number
	 * @return PamDataBlock block, which you may want to cast to a subtype
	 */
	public PamDataBlock getDataBlock(Class blockType, int id);


	/** Find a block of a given type with the given name, or null if it
	 * doesn't exist.
	 * @param  blockType -- RAW, FFT, DETECTOR, null, etc.
	 * @param name -- the block name
	 * @return PamDataBlock block, which you may want to cast to a subtype
	 */
	public PamDataBlock getDataBlock(Class blockType, String name);
	
	/**
	 * @return All data blocks
	 */
	public ArrayList<PamDataBlock> getDataBlocks();
	

	/**
	 * Should be sent by the developer when a process alters it's configuration in any way
	 * so that downstream processes can adjust acordingly
	 */
	public static final int CHANGED_PROCESS_SETTINGS = 1;
	/**
	 * Automatically sent when a PamProcess is added 
	 */
	public static final int ADD_PROCESS = 2;
	/**
	 * Automatically sent when a PamProcess is removed 
	 */
	public static final int REMOVE_PROCESS = 3;
	/**
	 * Automatically sent when a PamDataBlock is added 
	 */
	public static final int ADD_DATABLOCK = 4;
	/**
	 * Automatically sent when a PamDataBlock is removed 
	 */
	public static final int REMOVE_DATABLOCK = 5;
	/**
	 * Automatically sent when a PamControlledUnit is added 
	 */
	public static final int ADD_CONTROLLEDUNIT = 6;
	/**
	 * Automatically sent when a PamControlledUnit is removed 
	 */
	public static final int REMOVE_CONTROLLEDUNIT = 7;
	/**
	 * Automatically added when PamControlledUnits are re-ordered
	 */
	public static final int REORDER_CONTROLLEDUNITS = 8;
	/**
	 * Automatically sent when PAMGAURD has finished loading it's
	 * initial settings file and created the GUI. It's a good time for modules
	 * to subscribe to their data sources, but they shouldn't do much else since 
	 * these go around in order, so when this arrives in the first module, other
	 * modules may not yet be setup. 
	 */
	public static final int INITIALIZATION_COMPLETE = 9;
	/**
	 * Sent when all PamControlledUnits have been removed from the model. 
	 */
	public static final int DESTROY_EVERYTHING = 10;
	/**
	 * Automatically sent when a PamControlledUnit is renamed. 
	 */
	public static final int RENAME_CONTROLLED_UNIT = 11;
	
	/**
	 * Called at startup in the viewer in the AWT thread should come 
	 * after the data maps have been made. 
	 */
	public static final int INITIALIZE_LOADDATA = 12;
	
	/**
	 * Notification sent out when new offline data have
	 * been loaded. This is sent in the AWT thread after the 
	 * worker thread loading the data has completed. 
	 * <p>Also see DATA__LOAD_COMPLETE 
	 * which is sent around 
	 * within the same worker thread and therefore
	 * executes BEFORE this one. 
	 */
	public static final int OFFLINE_DATA_LOADED = 13;
//	/**
//	 * Sent in Viwer mode when the start and end times of data viewing have
//	 * been changed.
//	 */
//	public static final int NEW_VIEW_TIMES = 12;
	/**
	 * Sent when display settings are changed (for instance altering of a symbol) 
	 * so that graphic components can redraw.
	 */
	public static final int CHANGED_DISPLAY_SETTINGS = 14;
	
	/**
	 * Sent when the data model has changed between single and multi threading.
	 */
	public static final int CHANGED_MULTI_THREADING = 15;
	
	/**
	 * Sent when either the database or the binary store has updated
	 * and finished making a new map of its data.  
	 */
	public static final int CHANGED_OFFLINE_DATASTORE = 16;

	/**
	 * Sent in viewer mode when the view time slider is dragged
	 */
	public static final int NEW_SCROLL_TIME = 17;
	
	/**
	 * Notification sent out when new offline data have
	 * been loaded. This is sent in the Swing Worker thread once data 
	 * have been loaded for every changed data block . 
	 * <p>also see OFFLINE_DATA_LOADED 
	 * which is sent around a little later in the AWT
	 * thread once the worker thread has completed. 
	 */
	public static final int DATA_LOAD_COMPLETE = 18;
	
	/**
	 * Called whenever the dialog box of the hydrophone array editor is closed (not cancelled). 
	 * Primarily designed to inform localisers of possible changes to a hydrophone array values. 
	 */
	public static final int HYDROPHONE_ARRAY_CHANGED = 19;
	
	/**
	 * Master reference point has updated (either changed type of
	 * reference or been updated in some way). 
	 */
	public static final int MASTER_REFERENCE_CHANGED = 20;
	
	/**
	 *Called whenever external data is imported into PAMGUARD and saved into the database and/or binary store. 
	 *Usually occurs in viewer mode. 
	 */
	public static final int EXTERNAL_DATA_IMPORTED = 21;
	
	/**
	 *Notification that offline processing has ended. 
	 */
	public static final int OFFLINE_PROCESS_COMPLETE = 22;
	
	/**
	 * Notification sent whenever the PC clock offset is updated. 
	 */
	public static final int GLOBAL_TIME_UPDATE = 23;
	
	/**
	 * The medium has been updated. 
	 */
	public static final int GLOBAL_MEDIUM_UPDATE = 24;
	
	/**
	 * Sent shortly after the main PAMGUard setup has been completed, but this point
	 * all modules will have received INITIALIZATION_COMPLETE and should be ready to 
	 * go. 
	 */
	public static final int READY_TO_RUN = 25;

	

	/**
	 * Tell the controller that the model may have changed (i.e. a process
	 * connection changed, or a process added, etc.) This will be 
	 * passes on to the view and used by the controller as necessary.
	 * <p> 
	 * @see
	 * Possible values are
	 * <ol>
	 * <li>CHANGED_PROCESS_SETTINGS</li>
	 * <li>ADD_PROCESS</li>
	 * <li>REMOVE_PROCESS</li>
	 * <li>ADD_DATABLOCK</li>
	 * <li>REMOVE_DATABLOCK</li>
	 * <li>ADD_CONTROLLEDUNIT</li>
	 * <li>REMOVE_CONTROLLEDUNIT</li>
	 * <li>REORDER_CONTROLLEDUNITS</li>
	 * <li>INITIALIZATION_COMPLETE</li>
	 * <li>DESTROY_EVERYTHING</li>
	 * <li>RENAME_CONTROLLED_UNIT</li>
	 * <li>NEW_VIEW_TIMES</li>
	 * <li>NEW_VIEW_TIME</li>
	 * <li>CHANGED_DISPLAY_SETTINGS</li>
	 * <li>CHANGED_MULTI_THREADING</li>
	 * <li>HYDROPHONE_ARRAY_CHANGED</li>
	 * <li>EXTERNAL_DATA_LOADED</li>
	 * </ol>
	 *
	 */
	public void notifyModelChanged(int changeType);
	
	/**
	 * Do a total rebuild of the Pam model based on the contents of 
	 * the set settings file. 
	 *
	 */
	public void totalModelRebuild();

	/**
	 * Close all modules and free up resources. 
	 */
	public void pamClose();
	
	//public void controllerAddFileMenuItem();

}
