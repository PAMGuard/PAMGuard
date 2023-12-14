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
package PamguardMVC;

import generalDatabase.SQLLogging;
import generalDatabase.external.crossreference.CrossReference;
import jsonStorage.JSONObjectDataSource;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.springframework.core.GenericTypeResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import pamScrollSystem.ViewLoadObserver;
import tethys.TethysControl;
import tethys.pamdata.AutoTethysProvider;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
import dataGram.DatagramProvider;
import dataMap.BespokeDataMapGraphic;
import dataMap.OfflineDataMap;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryOfflineDataMap;
import binaryFileStorage.SecondaryBinaryStore;
import PamController.OfflineDataStore;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamDetection.PamDetection;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.background.BackgroundDataBlock;
import PamguardMVC.background.BackgroundManager;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataOffline.OfflineDataLoading;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.dataSelector.DataSelectorSettings;
import PamguardMVC.dataSelector.NullDataSelectorCreator;
import PamguardMVC.datamenus.DataMenuParent;
import PamguardMVC.nanotime.NanoTimeCalculator;
import PamguardMVC.nanotime.NanosFromMillis;
import PamguardMVC.toad.TOADCalculator;
import PamguardMVC.uid.DataBlockUIDHandler;
import SoundRecorder.RecorderControl;
import SoundRecorder.trigger.RecorderTrigger;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         PamDataBlocks manage the data from PamProcesses.
 *         <p>
 *         New data, either from external sources (sound cards, GPS, etc.) or
 *         detector output (clicks or whistles, etc.) are placed in
 *         PamDataUnits. The job of a PamDataBlock is to manage those
 *         PamDataUnits.
 *         <p>
 *         Processes that require the data from a PamDataBlock must implement
 *         PamObserver and subscribe as listeners to the PamDataBlock. When a
 *         new PamDataUnit is added to a data block, all listeners will be
 *         notified and sent references both to the data block and the data
 *         unit.
 *         <p>
 *         Each PamDatablock is also responsible for deleting old data. Since
 *         only the observers of PamDataBlocks know how much historical data is
 *         required, before deleting any data each PamDataBlock asks all the
 *         PamObservers for their required data history in milliseconds and
 *         takes the maximum value returned by all observers. The PamDataBlock
 *         will then calculate the time of the first required data unit and
 *         delete all preceding units. This operation takes place approximately
 *         once per second.
 *         <p>
 *         For example, a whistle detector, while searching for whistles, may
 *         only require the last two or three data units from the data block
 *         containing FFT data, but when it's found a complete whistle, it may
 *         need to go back and look at the FFT data from other channels in order
 *         to calculate a location, or it may require the raw data in order to
 *         look at the original waveform. As another example, the map panel may
 *         want to hold several hours of data in memory for display purposes.
 *         <p>
 *         It is essential that PamProcesses are realistic about how much data
 *         they can ask a PamDataBlock to hold - if they consistently ask for
 *         too much data to be stored, the computer will run out of memory.
 * 
 * @see PamguardMVC.PamDataUnit
 * @see PamguardMVC.PamProcess
 */
@SuppressWarnings("rawtypes")
public class PamDataBlock<Tunit extends PamDataUnit> extends PamObservable {

	/**
	 * When getting a DataUnit from the Datablock, get the absolute data unit, i.e.
	 * the unit number as would be if none had ever been deleted
	 */
	static final public int REFERENCE_ABSOLUTE = 1;

	/**
	 * When getting a DataUnit from the Datablock, get the current data unit, i.e.
	 * the unit number in the current ArrayList
	 */
	static final public int REFERENCE_CURRENT = 2;

	/**
	 * when Pamguard is running in mixed mode, some data are being reanalysed and
	 * are being written back into the database, others are being taken out of the
	 * database.
	 * <p>
	 * These flags tell each individual datablock what it should do.
	 */
	private int mixedDirection = MIX_DONOTHING;
	static public final int MIX_DONOTHING = 0;
	static public final int MIX_OUTOFDATABASE = 1;
	static public final int MIX_INTODATABASE = 2;

	private String dataName;

	/**
	 * No data available for offline loading.
	 */
	static public final int REQUEST_NO_DATA = 0x1;
	/**
	 * Data loaded for requested time period.
	 */
	static public final int REQUEST_DATA_LOADED = 0x2;
	/**
	 * Data partially loaded for requested time period
	 */
	static public final int REQUEST_DATA_PARTIAL_LOAD = 0x4;
	/**
	 * this is exactly the same data as requested last time.
	 * <p>
	 * This flag will be used with one of the other three.
	 */
	static public final int REQUEST_SAME_REQUEST = 0x8;
	/**
	 * The request was interrupted (in multi thread load)
	 */
	static public final int REQUEST_INTERRUPTED = 0x10;
	/**
	 * The request threw an exception of some sort.
	 */
	static public final int REQUEST_EXCEPTION = 0x20;

	// protected DataType dataType;

	// The data units managed by the datablock
	protected List<Tunit> pamDataUnits;

	/**
	 * Only used in viewer mode to store a list of items which may need to be
	 * deleted from file or the databse.
	 */
	private List<Tunit> removedItems;

	private boolean isOffline = false;

	protected PamProcess parentProcess;

	private BinaryDataSource binaryDataSource;

	private NanoTimeCalculator nanoTimeCalculator = new NanosFromMillis();

	/**
	 * List of data annotation types which may be associated with this data block.
	 */
	// private List<DataAnnotationType> dataAnnotationTypes;
	private AnnotationHandler annotationHandler;

	/**
	 * Localisation info to say what localisation information may be present in data
	 * in this class. This is the maximum data which are likely to be found within
	 * each data unit and are NOT a guarantee that individual data units will have
	 * that level of localisation content.
	 */
	private LocalisationInfo localisationContents = new LocContents(0);

	/**
	 * Used in offline analysis when data are being reloaded. this list gets used to
	 * distribute data being loaded from upstream processes.
	 */
	private Vector<PamObserver> requestingObservers;

	/**
	 * Natural lifetime of data in seconds.
	 */
	protected int naturalLifetime = 0; // natural lifetime in milliseconds.

	protected int unitsRemoved = 0;

	protected int unitsAdded = 0;

	protected int unitsUpdated = 0;

	int channelMap;
	
	private BackgroundManager backgroundManager;

	/**
	 * As described in dataunit, this is a slightly different map to channelmap to
	 * be used when beamforming, where the map of available outputs may be quite
	 * different to the number of available input channels.
	 * <p>
	 * e.g. if a beam former had 4 input channels and made 6 beams then
	 * thechannelMap would be 0xF, but the sequenceMap would be 0x3F.
	 */
	private Integer sequenceMap;

	private boolean linkGpsData = true;

	private Class unitClass;

	private boolean acousticData;

	private boolean isNetworkReceive;

	/**
	 * Flag to say that this data block and trigger sound clip generator. NB clips
	 * are intended to be very short clips around a single whistle or similar and
	 * are different to automatic recordings which can go on for a lot longer.
	 */
	private boolean canClipGenerate = false;

	/**
	 * Allow recycling within this data block
	 */
	private boolean recycling;

	/**
	 * Max length for the recycling store.
	 */
	private int recyclingStoreLength = 100;

	private Vector<Tunit> recycledUnits;

	private DatagramProvider datagramProvider = null;

	/**
	 * Should log data in database if SQL Logging is set.
	 */
	private boolean shouldLog = true;

	/**
	 * Should store data in binary store if binary storage is available.
	 */
	private boolean shouldBinary = true;

	/**
	 * Flag to say that data shoul be cleared from memory whenever PAMGuard starts
	 * up.
	 */
	private boolean clearAtStart = true;

	/**
	 * Millsecond time of the last data unit to be added.
	 */
	private long lastUnitMillis;

	private DataBlockUIDHandler uidHandler;

	/**
	 * Manages offline data loading in viewer mode.
	 */
	private OfflineDataLoading<Tunit> offlineDataLoading;

	private RecorderTrigger recorderTrigger;

	private ArrayList<PamDataUnitIterator<Tunit>> storedIterators = new ArrayList<PamDataUnitIterator<Tunit>>();

	/*
	 * Having iteratorLock as a separate object was causing locks, which one thread
	 * synching on datablock and waiting for this, while another thread synchronised
	 * on this and then waited for the datablock. Have set it to be the same as the
	 * datablock in the datablock constructor.
	 */
	private Object synchronizationLock = new Object();

	/**
	 * Class of any super detections referenced by the data units held in this data
	 * block
	 */
	private Class<?> superDetectionClass;

	//	/**
	//	 * Class of any sub detections referenced by the data units held in this data
	//	 * block
	//	 */
	//	protected Class<?> subDetectionClass;

	/**
	 * Standard PamDataBlock constructor.
	 * 
	 * @param unitClass     class of data unit to hold in this data block
	 * @param dataName      name of data block
	 * @param parentProcess parent PamProcess
	 * @param channelMap    bitmap of channels which may be represented in data
	 *                      units in this data block.
	 */
	public PamDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap) {
		this(unitClass, dataName, parentProcess, channelMap, getOfflineDefault());
	}

	/**
	 * PamDataBlock constructor that allows bespoke setting of isOffline flag.
	 * 
	 * @param unitClass     class of data unit to hold in this data block
	 * @param dataName      name of data block
	 * @param parentProcess parent PamProcess
	 * @param channelMap    bitmap of channels which may be represented in data
	 *                      units in this data block.
	 * @param isOffline datablock is offline, so normal data deleting doesn't apply
	 */
	public PamDataBlock(Class unitClass, String dataName, PamProcess parentProcess, int channelMap, boolean isOffline) {

		super();

		// this.dataType = dataType;
		this.unitClass = unitClass;
		this.dataName = dataName;
		this.parentProcess = parentProcess;
		this.channelMap = channelMap;

		sortTypeInformation();
		/*
		 * Having iteratorLock as a separate object was causing locks, which one thread
		 * synching on datablock and waiting for this, while another thread synchronised
		 * on this and then waited for the datablock. Have set it to be the same as the
		 * datablock in the datablock constructor. Leave in the code for now, but could
		 * probably remove completely.
		 */
		synchronizationLock = this;

		uidHandler = new DataBlockUIDHandler(this);

		this.isOffline = isOffline;

		if (isOffline) {
			removedItems = Collections.synchronizedList(new Vector<Tunit>());
		}

		// generally only used in viewer mode but might be used in real time e.g. to
		// listen to clips.
		offlineDataLoading = new OfflineDataLoading<Tunit>(this);

		// pamDataUnits = new Vector<Tunit>();
		pamDataUnits = Collections.synchronizedList(new LinkedList<Tunit>());
		unitsRemoved = 0;

		autoSetDataBlockMixMode();

		acousticData = AcousticDataUnit.class.isAssignableFrom(unitClass);

		if (PamController.getInstance() == null)
			isNetworkReceive = false;
		else
			isNetworkReceive = PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER;

		if (!isOffline) {
			removeTimer.start();
		}
	}

	/**
	 * Get the default status of whether we're in viewer or normal mode. Called in 
	 * standard constructor get get normal behaviour for different ops modes. . 
	 * @return if we're offline. 
	 */
	private static boolean getOfflineDefault() {
		if (PamController.getInstance() == null) {
			return true;
		}
		else {    
			return (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		}
	}

	/**
	 * Called at construction to try to work out the types of the generic arguments
	 * for the sub and super detections of the data units in this data block.
	 * 
	 * @return true if it worked, false if an exception thrown.
	 */
	private boolean sortTypeInformation() {

		/**
		 * Try to automatically get the class of the generic type and also of the sub
		 * and super detections ... This is achieved by looking at the return types of
		 * two of the generic functions in PAMDataUnit which return the super type and
		 * sub type respectively.
		 */
		boolean ok = true;
		Method method;
		try {
			Class[] superDetArgs = { Class.class };
			method = unitClass.getMethod("getSuperDetection", superDetArgs);
			superDetectionClass = GenericTypeResolver.resolveReturnType(method, unitClass);
		} catch (NoSuchMethodException | SecurityException e) {
			//			ok = false;
		}
		//		try {
		//			Class[] subDetArgs = { Integer.TYPE };
		//			method = unitClass.getMethod("getSubDetection", subDetArgs);
		//			subDetectionClass = GenericTypeResolver.resolveReturnType(method, unitClass);
		//		} catch (NoSuchMethodException | SecurityException e) {
		//			ok = false;
		//		}
		//		Debug.out.printf("%s has generic type %s, superDetType %s, subDetType %s\n", dataName, 
		//				unitClass, superDetectionClass, subDetectionClass);

		return ok;
	}

	private boolean shouldDelete() {
		//		switch (PamController.getInstance().getRunMode()) {
		//		case PamController.RUN_NORMAL:
		//			return true;
		//		case PamController.RUN_PAMVIEW:
		//			return false;
		//		case PamController.RUN_MIXEDMODE:
		//			return true;
		//		}
		//		return true;
		return !isOffline;
	}

	public void remove() {
		// inform any observers that this data block has been removed from the system
		for (int i = 0; i < countObservers(); i++) {
			getPamObserver(i).removeObservable(this);
		}
		// // also clean up any managed symbol output.
		// if (overlayDraw != null) {
		// PamOldSymbolManager.getInstance().removeManagedSymbol(overlayDraw);
		// }
	}

	Timer removeTimer = new Timer(500, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			int n;
			if (shouldDelete()) {
				if (acousticData && !isNetworkReceive && masterClockSample > 0) {
					n = removeOldUnitsS(masterClockSample);
					// System.out.println(String.format("%d units removed from %s based on sample
					// number at clock sample %d",
					// n, getDataName(), masterClockSample));
					return;
				} else {
					n = removeOldUnitsT(PamCalendar.getTimeInMillis());
					// System.out.println(String.format("%d units removed from %s based on
					// millisecond time", n, getDataName()));
				}
			}
		}
	});

	/**
	 * @return The total number of PamDataUnits in the block
	 */
	public int getUnitsCount() {
		return pamDataUnits.size();
	}

	public int getUnitsCountFromTime(long countStart) {
		int count = 0;
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(pamDataUnits.size());
			Tunit unit;
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getTimeMilliseconds() < countStart) {
					break;
				}
				count++;
			}
		}
		return count;
	}

	/**
	 * @return the removedItems list
	 */
	public List<Tunit> getRemovedItems() {
		return removedItems;
	}

	/**
	 * Return the first DataUnit that is on or after the given time
	 * 
	 * @param timems Milliseconds - UTC in standard Java epoch
	 * @return a PamDataUnit or null if no data were found
	 */
	public Tunit getFirstUnitAfter(long timems) {
		/**
		 * Iterative method no longer fast with a linked list system.
		 */
		// return searchFirstUnitAfter(0, getUnitsCount()-1, timems);
		return searchFirstUnitAfter(timems);
	}

	/**
	 * 
	 * Find a unit that starts at a specific time. searchStart may help to speed
	 * things up, however, now that a LinkedList is used in place of a vector, it's
	 * likely that this speed increase will be small.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * 
	 * @param timeMS      start time of data unit
	 * @param channels    channel bitmap of data unit, or 0 for any data unit
	 * @param absStartPos start position for search, -1 if you want to start
	 *                    searching backwards from the end.
	 * @return data unit (or null if nothing found)
	 */
	public Tunit findDataUnit(long timeMS, int channels, int absStartPos) {
		if (pamDataUnits == null || pamDataUnits.size() == 0)
			return null;
		if (absStartPos < 0) {
			return findDataUnitBackwards(timeMS, channels);
		}
		Tunit unit = null;
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(absStartPos);
			while (listIterator.hasNext()) {
				unit = listIterator.next();
				if (unit.getTimeMilliseconds() == timeMS && (channels == 0 || channels == unit.getChannelBitmap())) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Find a unit that starts at a specific time, starting from the end of the
	 * list. searchStart may help to speed things up, however, now that a LinkedList
	 * is used in place of a vector, it's likely that this speed increase will be
	 * small.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * 
	 * @param timeMS   start time of data unit
	 * @param channels channel bitmap of data unit, or 0 for any data unit
	 * @return data unit (or null if nothing found)
	 */
	private Tunit findDataUnitBackwards(long timeMS, int channels) {
		Tunit unit = null;
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(pamDataUnits.size());
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getTimeMilliseconds() == timeMS && (channels == 0 || channels == unit.getChannelBitmap())) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Get an iterator which can go backwards through a datablock, but only selects
	 * dataunits that have a channel map that overlaps with channelMap. Note that
	 * only hasPrevious() and previous() work and that you cannot add, delete, or
	 * move forwards through this iterator.
	 * 
	 * @param channelMap channel map for search
	 * @return a reverese iterator to go backwards through the data.
	 */
	public ReverseChannelIterator<Tunit> getReverseChannelIterator(int channelMap) {
		return new ReverseChannelIterator<Tunit>(this, channelMap);
	}

	/**
	 * Find a data unit. By default, the search starts at the end of the list and
	 * works backwards on the assumption that we'll be more interested in more
	 * recent data.
	 * 
	 * @param timeMS   start time of PamDataUnit
	 * @param channels channel bitmap of PamDataUnit
	 * @return found data unit, or null.
	 */
	public Tunit findDataUnit(long timeMS, int channels) {
		return findDataUnit(timeMS, channels, -1);
	}

	public int getUnitIndex(Tunit dataUnit) {
		return pamDataUnits.indexOf(dataUnit);
	}

	/**
	 * Find a dataunit based on it's database index. If there have been no updates,
	 * then database indexes should be in order and a fast find canbe used. If
	 * however, there have been updates, then things will not be in order so it's
	 * necessary to go through everything from start to end.
	 * 
	 * @param databaseIndex Index to search for.
	 * @return found unit or null.
	 */
	public Tunit findByDatabaseIndex(int databaseIndex) {

		// if (unitsUpdated == 0) {
		// return fastFindByDatabaseIndex(databaseIndex, 0, getUnitsCount()-1);
		// }
		// else {
		synchronized (synchronizationLock) {
			return slowFindByDatabaseIndex(databaseIndex);
		}
		// }

	}

	/**
	 * Search all units in reverse order.
	 * 
	 * @param databaseIndex Database index to search for
	 * @return found unit
	 */
	private Tunit slowFindByDatabaseIndex(int databaseIndex) {
		if (lastSlowFoundUnit != null && lastSlowFoundUnit.getDatabaseIndex() == databaseIndex) {
			return lastSlowFoundUnit;
		}
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(pamDataUnits.size());
			Tunit unit;
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getDatabaseIndex() == databaseIndex) {
					lastSlowFoundUnit = unit;
					return unit;
				}
			}
		}
		return null;
	}

	private Tunit lastSlowFoundUnit;

	/**
	 * Search all units in reverse order for the given UID.
	 * 
	 * @param unitUID Unit UID to search for
	 * @return found unit, or null if not found
	 */
	private Tunit findUnitByUID(long unitUID) {
		if (unitUID < getFirstViewerUID() || unitUID > getLastViewerUID()) {
			return null;
		}

		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(pamDataUnits.size());
			Tunit unit;
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getUID() == unitUID) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * find a data unit based on it's UID AND on it's timestamp to deal with
	 * problems of UID's occasionally resetting and therefore not being quite as
	 * unique as we'd like them to be.
	 * <p>
	 * Allow a two second jitter on the time match to allow for some database
	 * systems not writing times to better than one second accuracy which won't
	 * match to binary file times which will remain accurate to milliseconds.
	 * 
	 * @param unitUID
	 * @param utc
	 * @return found data unit or null;
	 */
	public Tunit findUnitByUIDandUTC(long unitUID, long utc) {
		if (unitUID < getFirstViewerUID() || unitUID > getLastViewerUID()) {
			return null;
		}
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(pamDataUnits.size());
			Tunit unit;
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getUID() == unitUID) {
					long tDiff = Math.abs(utc - unit.getTimeMilliseconds());
					if (tDiff < 2000) {
						return unit;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Search through data units, starting at the last on the assumption that we'll
	 * be more interested in more recent data.
	 * 
	 * @param timems search time
	 * @return found data unit or null
	 */
	private Tunit searchFirstUnitAfter(long timems) {
		Tunit unit, prevUnit = null;
		if (getFirstUnit() == null) {
			return null;
		}
		if ((unit = getFirstUnit()).getTimeMilliseconds() >= timems) {
			return unit;
		}
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(pamDataUnits.size());
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getTimeMilliseconds() < timems) {
					return prevUnit;
				}
				prevUnit = unit;
			}
		}
		if (prevUnit.getTimeMilliseconds() >= timems) {
			return prevUnit;
		} else {
			return null;
		}
	}

	public Tunit findFirstUnitAfter(long timems, DataSelector dataSelector) {
		if (dataSelector == null) {
			return searchFirstUnitAfter(timems);
		}
		// otherwise a very similar function, but with a data selector
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = getListIterator(timems, 0, MATCH_AFTER, POSITION_BEFORE);
			if (listIterator == null) {
				return null;
			}
			while (listIterator.hasNext()) {
				Tunit unit = listIterator.next();
				if (unit.getTimeMilliseconds() < timems) {
					continue;
				}
				if (dataSelector.scoreData(unit) <= 0) {
					continue;
				}
				return unit;
			}
		}
		return null;
	}

	/**
	 * find the first data unit that is at or before the given time.
	 * 
	 * @param timems       time in milliseconds.
	 * @param dataSelector optional data selector
	 * @return unit or null.
	 */
	public Tunit findLastUnitBefore(long timems, DataSelector dataSelector) {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> lIt = getListIterator(timems, 0, MATCH_BEFORE, POSITION_AFTER);
			if (lIt == null) {
				return null;
			}
			while (lIt.hasPrevious()) {
				Tunit unit = lIt.previous();
				if (unit.getTimeMilliseconds() > timems) {
					continue;
				}
				if (dataSelector != null && dataSelector.scoreData(unit) <= 0) {
					continue;
				}
				return unit;
			}
		}
		return null;
	}

	/**
	 * Find a group of data units within a time window.
	 * 
	 * @param startTime time to search from in millis
	 * @param endTime   time to search to in millis
	 * @return An ArrayList of units between startTime and endTime. If no units are
	 *         present in this time window an empty array will be returned.
	 */
	public ArrayList<Tunit> findUnitsinInterval(long startTime, long endTime) {
		Tunit unit = null;
		ArrayList<Tunit> unitsInInterval = new ArrayList<Tunit>();

		if (getFirstUnit() == null) {
			return null;
		}

		synchronized (synchronizationLock) {
			// ListIterator<Tunit> listIterator =
			// pamDataUnits.listIterator(pamDataUnits.size());
			ListIterator<Tunit> listIterator = pamDataUnits.listIterator(0);
			while (listIterator.hasNext()) {
				unit = listIterator.next();
				if (unit.getTimeMilliseconds() < endTime && unit.getTimeMilliseconds() > startTime) {
					unitsInInterval.add(unit);
				}
			}
		}

		return unitsInInterval;
	}
	
	/**
	 * Do data exist which cover the given time range ?  
	 * @param startMillis
	 * @param endMillis
	 * @return true if data exist covering that time range. 
	 */
	public boolean hasDataRange(long startMillis, long endMillis) {
		PamDataUnit first = null, last = null;
		synchronized (synchronizationLock) {
			first = getFirstUnit();
			last = getLastUnit();
		}
		if (first == null || last == null) {
			return false;
		}
		if (first.getTimeMilliseconds() > startMillis) {
			return false;
		}
		if (last.getEndTimeInMilliseconds() < endMillis) {
			return false;
		}
		return true;
	}

	// recursive search for the correct unit
	// private Tunit searchFirstUnitAfter(int i1, int i2, long timems) {
	// /*
	// * if there are < 5 units in chain, then just seach for them
	// * or give up if nothing was found
	// */
	// if (i2-i1 < 5) {
	// for (int i = i1; i <= i2; i++) {
	// if (pamDataUnits.get(i).timeMilliseconds >= timems) {
	// return pamDataUnits.get(i);
	// }
	// }
	// return null; // nothing was found
	// }
	// /*
	// * otherwise try the mid point between i1 and i2 and
	// * continue by searching one half of remaining data
	// */
	// int testIndex = (i1 + i2) / 2;
	// if (pamDataUnits.get(testIndex).timeMilliseconds > timems) {
	// return searchFirstUnitAfter(i1, testIndex, timems);
	// }
	// else {
	// return searchFirstUnitAfter(testIndex, i2, timems);
	// }
	// }

	// recursive search for the correct unit
	// private int indexSearchFirstUnitAfter(int i1, int i2, long timems) {
	// /*
	// * if there are < 5 units in chain, then just seach for them
	// * or give up if nothing was found
	// */
	// if (i2-i1 < 5) {
	// for (int i = i1; i <= i2; i++) {
	// if (pamDataUnits.get(i).timeMilliseconds >= timems) {
	// return i;
	// }
	// }
	// return -1; // nothing was found
	// }
	// /*
	// * otherwise try the mid point between i1 and i2 and
	// * continue by searching one half of remaining data
	// */
	// int testIndex = (i1 + i2) / 2;
	// if (pamDataUnits.get(testIndex).timeMilliseconds > timems) {
	// return indexSearchFirstUnitAfter(i1, testIndex, timems);
	// }
	// else {
	// return indexSearchFirstUnitAfter(testIndex, i2, timems);
	// }
	// }

	/**
	 * Creates a new PamDataUnit which will be added to this data block
	 * 
	 * @param startSample   first sample in data unit
	 * @param duration      Duration of the data unit (samples)
	 * @param channelBitmap Bitmap of channels having data in the unit, i.e. if it's
	 *                      reading channel 0 only, channelBitmap is 1, if it's
	 *                      channel 1 only, the channelBitmap is 2 and if it's data
	 *                      from channels 0 and 1 together, then channelBitmap is 3.
	 * @return Reference to the new PamDataUnit
	 */
	// public Tunit getNewUnit(long startSample, long duration,
	// int channelBitmap) {
	//
	// long ms;
	// if (parentProcess != null) {
	// ms = parentProcess.absSamplesToMilliseconds(startSample);
	// }
	// else {
	// ms = PamCalendar.getTimeInMillis();
	// }
	// return createNewUnit(ms);
	//// return new Tunit(startSample, ms, duration, channelBitmap, null);
	// }

	// abstract Tunit createNewUnit(long timeMilliseconds);

	/**
	 * Clears all PamDataUnits from memory
	 * <p>
	 * In viewer mode, data are also re-saved.
	 */
	public void clearAll() {
		synchronized (synchronizationLock) {
			pamDataUnits.clear();
			if (isOffline) {
				removedItems.clear();
			}

			unitsRemoved = 0;
			unitsAdded = 0;
			unitsUpdated = 0;
			lastSlowFoundUnit = null;
			if (recycledUnits != null) {
				recycledUnits.clear();
			}
		}
		if (backgroundManager != null) {
			BackgroundDataBlock bdb = backgroundManager.getBackgroundDataBlock();
			if (bdb != null) {
				bdb.clearAll();
			}
		}
	}

	/**
	 * Reset a datablock. This is called at PamStart from PamController It's been
	 * added in mostly to help with some issues surrounding sample numbering and
	 * timing when receiving Network data in which case the
	 * PamCalendar.getSessionStartTime may have been initialised when the sample
	 * count is already up in the zillions, in which case a lot of the timing
	 * functions won't work.
	 */
	public void reset() {
		synchronized (synchronizationLock) {
			if (binaryDataSource != null) {
				binaryDataSource.reset();
			}
			if (logging != null) {
				logging.reset();
			}
		}
	}

	private long currentViewDataStart, currentViewDataEnd;

	private long firstViewerUID = Long.MAX_VALUE, lastViewerUID = 0;

	/**
	 * 
	 * @return the start time of data currently loaded by the viewer.
	 */
	public long getCurrentViewDataStart() {
		return currentViewDataStart;
	}

	/**
	 * 
	 * @return the end time of data currently loaded by the viewer.
	 */
	public long getCurrentViewDataEnd() {
		return currentViewDataEnd;
	}

	/**
	 * Instruction from the viewer scroll manager to load new data. <p>This just calls through
	 * to loadViewerData(OfflineDataLoadInfo ...) so this should not be overridden. Override
	 * the other function instead. 
	 * 
	 * @param dataStart    data start time in millis
	 * @param dataEnd      data end time in millis.
	 * @param loadObserver - the load obsever. Can be used as a callback for load
	 *                     progress.
	 */
	public boolean loadViewerData(long dataStart, long dataEnd, ViewLoadObserver loadObserver) {
		return loadViewerData(new OfflineDataLoadInfo(dataStart, dataEnd), loadObserver);
	}

	/**
	 * Do we need to reload offline data ? Default behaviour is to reurn true if the
	 * time periods of the data load have changed, false otherwise.
	 * 
	 * @param offlineDataLoadInfo
	 * @return true if we need to reload offline data.
	 */
	public boolean needViewerDataLoad(OfflineDataLoadInfo offlineDataLoadInfo) {
		if (pamDataUnits.size() == 0) {
			return true;
		}
		if (offlineDataLoadInfo.getStartMillis() == currentViewDataStart
				&& offlineDataLoadInfo.getEndMillis() == currentViewDataEnd) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Clear all data units on viewer load. 
	 * @return true for normal operations, may be overridded for some types
	 * of super detection
	 */
	public boolean clearOnViewerLoad() {
		return true;
	}

	/**
	 * Instruction from the viewer scroll manager to load new data.
	 * 
	 * @param offlineDataLoadInfo - the load object which contains all info on the
	 *                            data to be loaded.
	 * @param loadObserver        - the load observer. Can be used as a callback for
	 *                            load progress.
	 */
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		if (PamController.getInstance().isInitializationComplete() == false) {
			System.err.printf("Not loading %s since initialisation not yet complete\n", getDataName());
			return false;
		}

		saveViewerData();

		if (needViewerDataLoad(offlineDataLoadInfo) == false) {
			return true;
		}

		clearChannelIterators();

		if (clearOnViewerLoad()) {
			clearAll();
		}

		currentViewDataStart = offlineDataLoadInfo.getStartMillis();
		currentViewDataEnd = offlineDataLoadInfo.getEndMillis();

		// run the garbage collector immediately.
		Runtime.getRuntime().gc();

		OfflineDataMap dataMap = getPrimaryDataMap();
		if (dataMap == null) {
			return false;
		}

		OfflineDataStore dataSource = dataMap.getOfflineDataSource();
		if (dataSource == null) {
			return false;
		}
		boolean loadOk = dataSource.loadData(this, offlineDataLoadInfo, loadObserver);

		if (dataMap.getClass() == BinaryOfflineDataMap.class) {
			//			System.out.println(getLongDataName() + " uses binary store");
			loadSecondaryBinaryData(offlineDataLoadInfo, loadObserver);
		}

		ListIterator<Tunit> iter = getListIterator(0);
		firstViewerUID = Long.MAX_VALUE;
		lastViewerUID = 0;
		while (iter.hasNext()) {
			long uid = iter.next().getUID();
			firstViewerUID = Math.min(firstViewerUID, uid);
			lastViewerUID = Math.max(lastViewerUID, uid);
		}

		return loadOk;
	}

	/**
	 * Function to load data from secondary binary data blocks.
	 * 
	 * @param offlineDataLoadInfo
	 * @param loadObserver
	 */
	private void loadSecondaryBinaryData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		ArrayList<PamControlledUnit> secUnits = PamController.getInstance()
				.findControlledUnits(SecondaryBinaryStore.class);
		if (secUnits == null) {
			return;
		}
		for (int i = 0; i < secUnits.size(); i++) {
			SecondaryBinaryStore secStore = (SecondaryBinaryStore) secUnits.get(i);
			secStore.loadData(this, offlineDataLoadInfo, loadObserver);
		}
	}

	/**
	 * Saves data in this data block in offline viewer mode.
	 * 
	 * @return true if data found and saved.
	 */
	public boolean saveViewerData() {

		OfflineDataMap dataMap = getPrimaryDataMap();
		if (dataMap == null) {
			return false;
		}

		OfflineDataStore dataSource = dataMap.getOfflineDataSource();
		if (dataSource == null) {
			return false;
		}

		return dataSource.saveData(this);
	}

	/**
	 * if possible, loads old data back in from a database or other storage space.
	 * 
	 * @param loadViewerData
	 *
	 */
	// public void loadViewData(LoadViewerData loadViewerData, PamViewParameters
	// pamViewParameters) {
	// // find the database module and use it's connection
	//
	// if (logging != null) {
	// generalDatabase.DBControl dbControl =
	// (DBControl)
	// PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
	// if (dbControl != null ) {
	// logging.loadViewData(loadViewerData, dbControl.getConnection(),
	// pamViewParameters);
	// }
	// }
	// }

	/**
	 * Get the number of pam data units that are required prior to the load times
	 * that get sent to loadViewerData. This can be used for things like hydrophone
	 * data where it may be necessary to load up a few earlier points to complete a
	 * trackline, etc.
	 * 
	 * @return number of units required.
	 */
	public int getNumRequiredBeforeLoadTime() {
		return 0;
	}

	static public final int NOTIFY_NEW_DATA = 1;
	static public final int NOTIFY_UPDATE_DATA = 2;

	/**
	 * Adds a new PamDataUnit to the PamDataBlock. When the data unit is added,
	 * PamObservers that have subscribed to the block will be notified. <br>
	 * If the data unit already has a UID, it will be left as is.
	 * 
	 * @param pamDataUnit Reference to a PamDataUnit
	 */
	public void addPamData(Tunit pamDataUnit) {
		if (uidHandler != null) {
			addPamData(pamDataUnit, uidHandler.getNextUID(pamDataUnit));
		} else {
			addPamData(pamDataUnit, 0L);
		}
	}

	/**
	 * Add early PAM data to a datablock. this can be used in normal mode as well as
	 * viewer mode. UID's are not changed, and no notifications are sent out.
	 * 
	 * @param pamDataUnit
	 */
	public void addOldPamData(Tunit pamDataUnit) {
		if (pamDataUnit.getParentDataBlock() == null) {
			/**
			 * Only do this if the data unit doesn't already have a data block. This is to
			 * help clicks which are in the click block, then also get added to the
			 * trackedclickDataBlock from losing their link to their main data block.
			 */
			pamDataUnit.setParentDataBlock(this);
		}
		pamDataUnit.absBlockIndex = unitsAdded++;
		synchronized (synchronizationLock) {
			pamDataUnits.add(pamDataUnit);
		}
	}

	/**
	 * Adds a new PamDataUnit to the PamDataBlock and force the UID to a specific
	 * value. This should only be used in very specific circumstances - nromally,
	 * programmers should call addPamData(Tunit pamDataUnit) and let PAMGuard handle
	 * the UID's.
	 * 
	 * @param pamDataUnit Reference to a PamDataUnit
	 * @param uid         Unique identifier for data unit.
	 */
	public void addPamData(Tunit pamDataUnit, Long uid) {
		if (parentProcess != null && parentProcess.getProcessCheck() != null) {
			parentProcess.getProcessCheck().newOutput(this, pamDataUnit);
		}
		synchronized (synchronizationLock) {
			pamDataUnit.absBlockIndex = unitsAdded++;
			if (pamDataUnit.getParentDataBlock() == null) {
				/**
				 * Only do this if the data unit doesn't already have a data block. This is to
				 * help clicks which are in the click block, then also get added to the
				 * trackedclickDataBlock from losing their link to their main data block.
				 */
				pamDataUnit.setParentDataBlock(this);
			}
			// pamDataUnit.setUID(uidHandler.getNextUID(pamDataUnit));
			pamDataUnit.setUID(uid);
			/**
			 * Now call downstream processes which must execute in the same thread prior to
			 * anything else (such as saving) happening.
			 */
			if (shouldNotify()) {
				notifyInstantObservers(pamDataUnit);
			}

			if (offlineDataLoading.isCurrentOfflineLoadKeep()) {
				pamDataUnits.add(pamDataUnit);
			}
			if (shouldBinary && getBinaryDataSource() != null && !isOffline && pamDataUnit.isEmbryonic() == false) {
				getBinaryDataSource().saveData(pamDataUnit);
			}
		}
		lastUnitMillis = pamDataUnit.getTimeMilliseconds();
		/*
		 * Do not synchronise notification since this will lock up the whole thing if
		 * anything notified tries to access the data in a different thread.
		 */
		if (recorderTrigger != null) {
			RecorderControl.actionRecorderTrigger(recorderTrigger, pamDataUnit, pamDataUnit.getTimeMilliseconds());
		}

		/**
		 * Now notify the 'normal' observers which get the data after it's been saved.
		 */
		if (shouldNotify()) {
			setChanged();
			notifyNornalObservers(pamDataUnit);
		}
		notifyOfflineObservers(pamDataUnit);
		notifyDataMaps(pamDataUnit, true);
	}

	//	public boolean isdebug() {
	//		return false;
	//	}

	/**
	 * Notify datamaps that there is a new data unit to add to the counts of
	 * existing data units and also to add to any datagrams. Datagrams only exist in
	 * the binary stored data so datagram creation will be handled within the
	 * BinaryOfflineDataMap
	 * 
	 * @param pamDataUnit
	 * @param newData
	 */
	private void notifyDataMaps(Tunit pamDataUnit, boolean newData) {
		if (isOffline) {
			// viewer will have made entire datamaps and added them complete to the
			// datablock.
			return;
		}
		if (offlineDataMaps == null) {
			return;
		}
		for (OfflineDataMap odm : offlineDataMaps) {
			if (newData) {
				odm.newPamDataUnit(pamDataUnit);
			} else {
				odm.updatedPamDataUnit(pamDataUnit);
			}
		}
	}

	public long getLastUnitMillis() {
		return lastUnitMillis;
	}

	protected void notifyOfflineObservers(Tunit pamDataUnit) {
		offlineDataLoading.notifyOfflineObservers(pamDataUnit);

	}

	/**
	 * update a dataunit. Does little except flag that the data unit is updated (so
	 * it will get saved), and sends notifications to other modules.
	 * 
	 * @param pamDataUnit
	 * @param updateTimeMillis
	 */
	public void updatePamData(Tunit pamDataUnit, long updateTimeMillis) {
		pamDataUnit.updateDataUnit(updateTimeMillis);
		setChanged();
		if (!isOffline && pamDataUnit.isEmbryonic() == false) {
			/*
			 * Save it if it't not been saved already or we're saving updates. 
			 * Detectors can keep a dataunit in an embryonic state and add them to the 
			 * datablock so they get displayed, but they will still save when the embryonic
			 * flag is set false and an update is sent. 
			 */
			if (getBinaryDataSource() != null && (getBinaryDataSource().isSaveUpdates() || pamDataUnit.getDataUnitFileInformation() == null)) {
				getBinaryDataSource().saveData(pamDataUnit);
			}
		}
		//		if (shouldNotify()) {
		updateObservers(pamDataUnit);
		//		}
		unitsUpdated++;
	}

	public boolean shouldNotify() {
		return !isOffline;
		//		int runMode;
		//		if (PamController.getInstance() != null)
		//			runMode = PamController.getInstance().getRunMode();
		//		else
		//			runMode = PamController.RUN_PAMVIEW;
		//
		//		switch (runMode) {
		//		case PamController.RUN_NORMAL:
		//			return true;
		//		case PamController.RUN_PAMVIEW:
		//			return false;
		//		// switch (PamController.getInstance().getPamStatus()) {
		//		// case PamController.PAM_LOADINGDATA:
		//		// return false;
		//		// case PamController.
		//		// }
		//		case PamController.RUN_MIXEDMODE:
		//			return true;
		//		// return (mixedDirection == MIX_OUTOFDATABASE);
		//		}
		//		return true;
	}

	/**
	 * Remove a data unit from a data block, but NOT from the database. To also
	 * remove from database, call remove(datUnit, true);
	 * 
	 * @param aDataUnit
	 * @return
	 */
	public boolean remove(Tunit aDataUnit) {
		synchronized (synchronizationLock) {
			return remove(aDataUnit, false);
		}
	}

	/**
	 * Remove a data unit from a data block and optionally remove it's entry from
	 * the database.
	 * 
	 * @param aDataUnit     data unit to remove from the working list.
	 * @param clearDatabase entry immediately remove the data unit entry from the
	 *                      database
	 * @return true if removed successfully (false if it didn't exit).
	 */
	public boolean remove(Tunit aDataUnit, boolean clearDatabase) {
		synchronized (synchronizationLock) {
			//			if (isdebug()) {
			//				Debug.out.println("Removing data unit " + aDataUnit);
			//			}
			boolean rem = pamDataUnits.remove(aDataUnit);
			if (rem == false) {
				return false;
			}
			if (isOffline) {
				removedItems.add(aDataUnit);
			}
			if (clearDatabase && logging != null) {
				logging.deleteData(aDataUnit);
			}
			return rem;
		}
	}

	/**
	 * Removes olderPamDataUnits from memory, starting at the first unit and
	 * continuing until if finds one with data coming earlier than the given time in
	 * milliseconds.
	 * <p>
	 * If the data are acoustic, it tries to find the data source and looks to see
	 * how much data has been placed in the source data unit and does the
	 * calculation in samples.
	 * 
	 * @param currentTimeMS Time in milliseconds of the first data which must be
	 *                      kept
	 * @return the number of units removed
	 */
	protected int removeOldUnitsT(long currentTimeMS) {
		// will have to do something to see if any of the blocks are still
		// referenced !
		if (pamDataUnits.isEmpty()) {
			return 0;
		}
		Tunit pamUnit;
		long firstWantedTime = currentTimeMS - Math.max(this.getNaturalLifetimeMillis(), 1000);
		firstWantedTime = Math.min(firstWantedTime, currentTimeMS - getRequiredHistory());

		int unitsJustRemoved = 0;
		synchronized (synchronizationLock) {
			while (!pamDataUnits.isEmpty()) {
				pamUnit = pamDataUnits.get(0);
				if (pamUnit.getTimeMilliseconds() > firstWantedTime) {
					break;
				}
				// System.out.printf("%s Remove %s at %s, first wanted is %s\n",
				// PamCalendar.formatDateTime(System.currentTimeMillis()),
				// getDataName(),
				// PamCalendar.formatDateTime(pamUnit.getTimeMilliseconds()),
				// PamCalendar.formatDateTime(firstWantedTime) );
				Tunit removed = pamDataUnits.remove(0);
				//				if (isdebug()) {
				//					Debug.out.println("Removing data unit " + removed);
				//				}

				removedDataUnit((Tunit) pamUnit);

				// unitsRemoved++;
				unitsJustRemoved++;
			}
			unitsRemoved += unitsJustRemoved;
		}
		// TODO check this.
		// return unitsRemoved; //should this be the count of unitsJustRemoved not total
		// units removed?
		return unitsJustRemoved;
	}

	protected int removeOldUnitsS(long mastrClockSample) {

		if (pamDataUnits.isEmpty())
			return 0;
		PamDataUnit pamUnit;

		// now take a time back from the last unit and use this as master time if it's
		// less than mastrClockSample.
		pamUnit = (PamDataUnit) getLastUnit();

		if (pamUnit == null) {
			return 0;
		}

		mastrClockSample = Math.min(mastrClockSample, pamUnit.getStartSample());

		long minKeepSamples = 0;
		float sr = getSampleRate();
		if (getNaturalLifetimeMillis() == 0) {
			minKeepSamples = (long) (sr > 100000 ? sr / 2 : sr);
		} else {
			minKeepSamples = (long) (this.getNaturalLifetimeMillis() * sr / 1000.);
		}

		// long firstWantedTime = (long) (this.naturalLifetime/1000. * getSampleRate());
		long keepTime = (long) (getRequiredHistory() / 1000. * getSampleRate());
		long firstWantedTime = mastrClockSample - Math.max(minKeepSamples, keepTime);

		int unitsJustRemoved = 0;
		long endSample;

		/**
		 * Remove that stray unit that got stuck in there from the last file which has a
		 * sample number greater than the current clock sample This shouldn't have
		 * happened, but generally it is impossible to get a clock sample in a data unit
		 * that is greater than the masterClockSample
		 */
		synchronized (synchronizationLock) {
			while (!pamDataUnits.isEmpty()) {
				pamUnit = (PamDataUnit) pamDataUnits.get(0);
				long unitSample = pamUnit.getStartSample();
				if (unitSample > mastrClockSample + (long) getSampleRate()) {
					Tunit removed = pamDataUnits.remove(0);
					//					if (isdebug()) {
					//						Debug.out.println("Removing data unit " + removed);
					//					}
					unitsJustRemoved++;
				} else {
					break;
				}
			}

			while (!pamDataUnits.isEmpty()) {
				pamUnit = (PamDataUnit) pamDataUnits.get(0);
				endSample = pamUnit.getStartSample();
				if (pamUnit.getSampleDuration() != null) {
					endSample += pamUnit.getSampleDuration();
				}
				if (endSample > firstWantedTime) {
					break;
				}
				Tunit removed = pamDataUnits.remove(0);
				//				if (isdebug()) {
				//					Debug.out.println("Removing data unit " + removed);
				//				}

				removedDataUnit((Tunit) pamUnit);

				// unitsRemoved++;
				unitsJustRemoved++;
			}
		}
		unitsRemoved += unitsJustRemoved;
		return unitsJustRemoved;
	}

	/**
	 * Called when a data unit has just been removed. May optionally recylcle the
	 * data unit to save allocation times.
	 * 
	 * @param pamUnit Data unti removed from main list.
	 */
	protected void removedDataUnit(Tunit pamUnit) {

		if (recycling && recycledUnits.size() < recyclingStoreLength) {
			recycledUnits.add((Tunit) pamUnit);
		}

	}

	/**
	 * Gets a recycled data unit if one is available.
	 * 
	 * @return recycled unit, or null
	 */
	public Tunit getRecycledUnit() {
		int n;
		if (recycledUnits == null) {
			return null;
		}
		synchronized (recycledUnits) {
			if ((n = recycledUnits.size()) > 0) {
				return recycledUnits.remove(n - 1);
			}
		}
		return null;
	}

	public PamDataBlock getSourceDataBlock() {
		PamDataBlock<PamDataUnit> sourceBlock = parentProcess.getSourceDataBlock();
		if (sourceBlock == null) {
			return this;
		} else {
			return sourceBlock;
		}
	}

	// /**
	// * Gets the next data unit in the list
	// *
	// * @param lastObject
	// * Current unit (you get back the one after this)
	// * @return The next data unit.
	// */
	// public Tunit getNextUnit(Object lastObject) {
	// int nextIndex = pamDataUnits.lastIndexOf(lastObject) + 1;
	// Tunit pu = null;
	// if (nextIndex < pamDataUnits.size()) {
	// pu = (Tunit) pamDataUnits.get(nextIndex);
	// }
	// return pu;
	// }
	// public Tunit getNextUnit(PamDataUnit pamDataUnit) {
	// return getAbsoluteDataUnit(pamDataUnit.getAbsBlockIndex() + 1);
	// }
	//
	// public Tunit getPreceedingUnit(PamDataUnit pamDataUnit) {
	// return getAbsoluteDataUnit(pamDataUnit.getAbsBlockIndex() - 1);
	// }
	//
	/**
	 * Gets a reference to a data unit.
	 * 
	 * @param ref     number of the data unit
	 * @param refType REFERENCE_ABSOLUTE or REFERENCE_CURRENT
	 * @return DataUnit \n If refType is REFERENCE_ABSOLUTE then the data unit with
	 *         the absolute position ref is returned (if it has not yet been
	 *         deleted). This might be used to re-access a specific unit or to
	 *         access the unit coming directly before or after a previously accessed
	 *         unit. \n If refType is REFERENCE_CURRENT then the data unit at that
	 *         position in the current ArrayList is returned.
	 */
	public Tunit getDataUnit(int ref, int refType) {
		synchronized (synchronizationLock) {
			switch (refType) {
			case REFERENCE_ABSOLUTE:
				return getAbsoluteDataUnit(ref);
			case REFERENCE_CURRENT:
				return getCurrentDataUnit(ref);
			}
		}
		return null;
	}

	/**
	 * Gets a specific data unit using an absolute reference system which keeps
	 * track of data units that have been removed. Returns null if the specified
	 * unit is no longer available.
	 * 
	 * @param absReference Absolute reference to the data unit
	 * @return Requested PamDataUnit
	 */
	protected Tunit getAbsoluteDataUnit(int absReference) {
		int trueReference = absReference - unitsRemoved;
		synchronized (synchronizationLock) {
			if (trueReference >= 0 && trueReference < pamDataUnits.size()) {
				return pamDataUnits.get(trueReference);
			}
		}
		return null;
	}

	private Tunit getCurrentDataUnit(int ref) {
		synchronized (synchronizationLock) {
			if (ref >= pamDataUnits.size())
				return null;
			return pamDataUnits.get(ref);
		}
	}

	/**
	 * Gets the last data unit stored
	 * 
	 * @return data unit or null
	 */
	public Tunit getLastUnit() {
		synchronized (synchronizationLock) {
			if (pamDataUnits == null || pamDataUnits.size() == 0)
				return null;
			return pamDataUnits.get(pamDataUnits.size() - 1);
		}
	}

	/**
	 * Get the last unit for a specific channel map (any match of channels allowed).
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * 
	 * @param channelMap channel map
	 * @return last data unit with at least one channel matching, or null.
	 */
	public Tunit getLastUnit(int channelMap) {
		if (channelMap == 0) {
			return getLastUnit();
		}
		synchronized (synchronizationLock) {
			ListIterator<Tunit> it = pamDataUnits.listIterator(pamDataUnits.size());
			Tunit unit;
			while (it.hasPrevious()) {
				unit = it.previous();
				if ((unit.getChannelBitmap() & channelMap) != 0) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the first data unit stored
	 * 
	 * @return data unit or null
	 */
	public Tunit getFirstUnit() {
		synchronized (synchronizationLock) {
			if (pamDataUnits == null || pamDataUnits.size() == 0)
				return null;
			return pamDataUnits.get(0);
		}
	}

	/**
	 * Get the first unit for a specific channel map (any match of channels
	 * allowed).
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * 
	 * @param channelMap channel map
	 * @return last data unit with at least one channel matching, or null.
	 */
	public Tunit getFirstUnit(int channelMap) {
		if (channelMap == 0) {
			return getLastUnit();
		}
		synchronized (synchronizationLock) {
			ListIterator<Tunit> it = pamDataUnits.listIterator(0);
			Tunit unit;
			while (it.hasNext()) {
				unit = it.next();
				if ((unit.getChannelBitmap() & channelMap) != 0) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the data unit before the given start time.
	 * <p>
	 * This implementation is passed an iterator which has already been initialised
	 * to be at the END of the list. In this way, the calling function has access to
	 * the iterator and can access nearby elements.
	 * 
	 * @param listIterator pre initialised ListIterator
	 * @param startTime    search time in milliseconds
	 * @return data unit at or following the given time.
	 * @see ListIterator
	 */
	public Tunit getPreceedingUnit(ListIterator<Tunit> listIterator, long startTime) {
		Tunit unit;
		synchronized (synchronizationLock) {
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getTimeMilliseconds() < startTime) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the data unit before the given start time that has the same channel
	 * map.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, getPreceedingUnitFromSeq should
	 * be used instead.
	 * <p>
	 * This implementation is passed an iterator which has already been initialised
	 * to be at the END of the list. In this way, the calling function has access to
	 * the iterator and can access nearby elements.
	 * 
	 * @param listIterator pre initialised ListIterator
	 * @param startTime    search time in milliseconds
	 * @param channelMap   Channel bitmap
	 * @return data unit at or following the given time.
	 * @see ListIterator
	 */
	public Tunit getPreceedingUnit(ListIterator<Tunit> listIterator, long startTime, int channelMap) {
		Tunit unit;
		synchronized (synchronizationLock) {
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getChannelBitmap() != channelMap) {
					continue;
				}
				if (unit.getTimeMilliseconds() <= startTime) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the data unit before the given start time that has the same
	 * channel/sequence map.
	 * <p>
	 * This implementation is passed an iterator which has already been initialised
	 * to be at the END of the list. In this way, the calling function has access to
	 * the iterator and can access nearby elements.
	 * 
	 * @param listIterator pre initialised ListIterator
	 * @param startTime    search time in milliseconds
	 * @param chanOrSeqMap Channel bitmap
	 * @return data unit at or following the given time.
	 * @see ListIterator
	 */
	public Tunit getPreceedingUnitFromSeq(ListIterator<Tunit> listIterator, long startTime, int chanOrSeqMap) {
		Tunit unit;
		synchronized (synchronizationLock) {
			while (listIterator.hasPrevious()) {
				unit = listIterator.previous();
				if (unit.getSequenceBitmap() != chanOrSeqMap) {
					continue;
				}
				if (unit.getTimeMilliseconds() <= startTime) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Simple function to find the data unit at or before the given start time.
	 * 
	 * @param startTime search time in milliseconds
	 * @return data unit at or following the given time.
	 */
	public Tunit getPreceedingUnit(long startTime) {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = getListIterator(ITERATOR_END);
			return getPreceedingUnit(listIterator, startTime);
		}
	}

	/**
	 * Simple function to find the data unit at or before the given start time that
	 * has a given channel bitmap
	 * 
	 * @param startTime  search time in milliseconds
	 * @param channelMap Channel bitmap
	 * @return data unit at or following the given time.
	 */
	public Tunit getPreceedingUnit(long startTime, int channelMap) {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = getListIterator(ITERATOR_END);
			return getPreceedingUnit(listIterator, startTime, channelMap);
		}
	}

	/**
	 * Simple function to find the data unit at or before the given start time that
	 * has a given channel OR sequence bitmap
	 * 
	 * @param startTime    search time in milliseconds
	 * @param chanOrSeqMap Channel or Sequence bitmap
	 * @return data unit at or following the given time.
	 */
	public Tunit getPreceedingUnitFromSeq(long startTime, int chanOrSeqMap) {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = getListIterator(ITERATOR_END);
			return getPreceedingUnitFromSeq(listIterator, startTime, chanOrSeqMap);
		}
	}

	/**
	 * Finds the data unit after the given start time that has the same channel map.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * <p>
	 * This implementation is passed an iterator which has already been initialised
	 * to be at the END of the list. In this way, the calling function has access to
	 * the iterator and can access nearby elements.
	 * 
	 * @param listIterator pre initialised ListIterator
	 * @param startTime    search time in milliseconds
	 * @param channelMap   Channel bitmap
	 * @return data unit at or following the given time.
	 * @see ListIterator
	 */
	public Tunit getNextUnit(ListIterator<Tunit> listIterator, long startTime, int channelMap) {
		Tunit unit;
		synchronized (synchronizationLock) {
			while (listIterator.hasNext()) {
				unit = listIterator.next();
				if (unit.getChannelBitmap() != channelMap) {
					continue;
				}
				if (unit.getTimeMilliseconds() >= startTime) {
					return unit;
				}
			}
		}
		return null;
	}

	/**
	 * Simple function to find the data unit at or following the given start time
	 * that has a given channel bitmap
	 * 
	 * @param startTime  search time in milliseconds
	 * @param channelMap Channel bitmap
	 * @return data unit at or following the given time.
	 */
	public Tunit getNextUnit(long startTime, int channelMap) {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = getListIterator(0);
			return getNextUnit(listIterator, startTime, channelMap);
		}
	}

	/**
	 * Find the closest data unit to a given time.
	 * 
	 * @param startTime Start time of data unit (milliseconds)
	 * @return closest data unit
	 */
	public Tunit getClosestUnitMillis(long startTime) {
		return getClosestUnitMillis(startTime, 0xFFFFFFFF);
	}

	/**
	 * Find the closest data unit to a given time. Note that this method works
	 * specifically with the channelMap. If you need to use either channel map or
	 * sequence map, call getClosestUnitMillisUsingSeq instead.
	 * 
	 * @param startTime  Start time of data unit (milliseconds)
	 * @param channelMap Channel map - must be some overlap, not an exact match.
	 * @return closest data unit
	 */
	public Tunit getClosestUnitMillis(long startTime, int channelMap) {
		synchronized (synchronizationLock) {
			if (pamDataUnits.size() == 0)
				return null;
			/*
			 * start at the last unit, the work back and if the interval starts getting
			 * bigger again, stop
			 */
			Tunit unit = null;
			Tunit preceedingUnit = null;
			long newdifference;
			long difference;

			ListIterator<Tunit> listIterator = getListIterator(ITERATOR_END);
			if (listIterator.hasPrevious() == false) {
				return null;
			}
			unit = listIterator.previous();
			difference = Math.abs(startTime - unit.getTimeMilliseconds());
			while (listIterator.hasPrevious()) {
				preceedingUnit = listIterator.previous();
				if (preceedingUnit.getChannelBitmap() != 0 && (preceedingUnit.getChannelBitmap() & channelMap) == 0) {
					continue;
				}
				newdifference = Math.abs(startTime - preceedingUnit.getTimeMilliseconds());
				if (newdifference > difference) {
					return unit;
				} else {
					unit = preceedingUnit;
					difference = newdifference;
				}
			}
			return unit;
		}
	}

	/**
	 * Find the closest data unit to a given time. This method tries to check the
	 * data units for sequence number first, and if there is no sequence number will
	 * try the channel number
	 * 
	 * @param startTime    Start time of data unit (milliseconds)
	 * @param chanOrSeqMap Channel/Sequence map - must be some overlap, not an exact
	 *                     match.
	 * @return closest data unit
	 */
	public Tunit getClosestUnitMillisUsingSeq(long startTime, int chanOrSeqMap) {
		synchronized (synchronizationLock) {
			if (pamDataUnits.size() == 0)
				return null;
			/*
			 * start at the last unit, the work back and if the interval starts getting
			 * bigger again, stop
			 */
			Tunit unit = null;
			Tunit preceedingUnit = null;
			long newdifference;
			long difference;

			ListIterator<Tunit> listIterator = getListIterator(ITERATOR_END);
			if (listIterator.hasPrevious() == false) {
				return null;
			}
			unit = listIterator.previous();
			difference = Math.abs(startTime - unit.getTimeMilliseconds());
			while (listIterator.hasPrevious()) {
				preceedingUnit = listIterator.previous();
				if (preceedingUnit.getSequenceBitmap() != 0
						&& (preceedingUnit.getSequenceBitmap() & chanOrSeqMap) == 0) {
					continue;
				}
				newdifference = Math.abs(startTime - preceedingUnit.getTimeMilliseconds());
				if (newdifference > difference) {
					return unit;
				} else {
					unit = preceedingUnit;
					difference = newdifference;
				}
			}
			return unit;
		}
	}

	/**
	 * @return The sample rate of the data contained in the block
	 */
	public float getSampleRate() {
		return parentProcess.getSampleRate();
	}

	/**
	 * Get the range of frequencies over which the data in the data block are likely
	 * to be present. Note that this is pretty crude and may not reflect the true
	 * range, for example, the click detector will return the limits of it's trigger
	 * filter, and there are plenty of sounds outside of that range which may have
	 * most of their energy well outside of the trigger range of the detector.
	 * 
	 * @return Nominal frequency range for data in this block.
	 */
	public double[] getFrequencyRange() {
		return parentProcess.getFrequencyRange();
	}

	/**
	 * Get the nominal range of durations of sounds that might be detected by this
	 * detector (if applicable). This is pretty crude, but will give an indication
	 * of which detectors might work with which types of sound. <br>
	 * can return null, 0 and Double.Infinity are also acceptable values.
	 * 
	 * @return duration range in seconds of sounds this detector can sensibly detect
	 */
	public double[] getDurationRange() {
		return null;
	}

	/**
	 * @return The DataType of the data in the block (RAW, FFT or DETECTOR)
	 */
	// public DataType getDataType() {
	// return dataType;
	// }

	/**
	 * @return Name of the data in the block.
	 */
	public String getDataName() {
		return dataName;
	}

	/**
	 * Get a slightly longer data name that also contains the module name
	 * 
	 * @return longer data name including module name.
	 */
	public String getLongDataName() {
		if (getParentProcess() == null) {
			return getDataName();
		}
		PamControlledUnit pcu = getParentProcess().getPamControlledUnit();
		if (pcu == null) {
			return getParentProcess().getProcessName() + ", " + getDataName();
		} else {
			return pcu.getUnitName() + ", " + getDataName();
		}
	}

	/**
	 * Sets the sample rate for the block (e.g. call this after opening a sound file
	 * and reading the sample rate from the file header or after altering sound card
	 * settings). All observers of this block (PamProcesses and some views) are
	 * notified, they in turn should tell their own output PamDataBlocks about the
	 * change.
	 * 
	 * @param sampleRate The new sample rate in Hz.
	 * @param notify     set true if Observers should be notified
	 */
	public void setSampleRate(float sampleRate, boolean notify) {
		// this.sampleRate = sampleRate;
		/*
		 * Doesnt notify it's own parent to avoid an infinite loop
		 */
		if (Float.isNaN(sampleRate)) {
			System.out.println("NaN sample rate being set in " + getLongDataName());
		}
		if (notify) {
			for (int i = 0; i < countObservers(); i++) {
				if (getPamObserver(i).getObserverObject() != parentProcess) {
					getPamObserver(i).setSampleRate(sampleRate, notify);
					// pamObservers.get(i).getObserverObject().setSampleRate(sampleRate, notify);
				}
			}
		}
	}

	public void masterClockUpdate(long milliSeconds, long clockSample) {
		masterClockSample = clockSample;
		int n = countObservers();
		for (int i = 0; i < n; i++) {
			if (getPamObserver(i) != parentProcess) {
				getPamObserver(i).masterClockUpdate(milliSeconds, clockSample);
			}
		}
		if (shouldDelete()) {
			if (isNetworkReceive) {
				removeOldUnitsT(milliSeconds);
			} else if (acousticData) {
				removeOldUnitsS(clockSample);
			}
		}
	}

	/**
	 * Tell all observers of this datablock that some control parameters have
	 * changed. Modified July 09 to make sure it doesn't loop through itself when
	 * using threaded observers.
	 */
	public void noteNewSettings() {
		for (int i = 0; i < countObservers(); i++) {
			if (getPamObserver(i).getObserverObject() != parentProcess) {
				getPamObserver(i).noteNewSettings();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDataName();
	}

	/**
	 * @return Finds the original data block, i.e. the one that has a PamProcess
	 *         with no source data block.
	 */
	// PamDataBlock FindMotherDataBlock() {
	// /*
	// * This can be called by ANY PamDataBlock and it will wiggle it's way
	// * back through the different detectors and data blocks until it finds
	// * one which has no parent
	// */
	// if (parentProcess == null) {
	// return this;
	// }
	// PamDataBlock parentDataBlock = parentProcess.GetSourceDataBlock();
	// if (parentDataBlock == null) {
	// return this;
	// }
	// else {
	// return parentDataBlock.FindMotherDataBlock();
	// }
	// }

	/**
	 * Get the natural lifetime in seconds
	 * 
	 * @return lifetime in seconds
	 */
	public int getNaturalLifetime() {
		return getNaturalLifetimeMillis() / 1000;
	}

	/**
	 * Set the natural lifetime in milliseconds
	 * 
	 * @param naturalLifetime
	 */
	public void setNaturalLifetimeMillis(int naturalLifetime) {
		this.naturalLifetime = naturalLifetime;
	}

	/**
	 * Get the natural lifetime in milliseconds
	 * 
	 * @return
	 */
	public int getNaturalLifetimeMillis() {
		return naturalLifetime;
	}

	/**
	 * Set the natural lifetime in seconds of the data if there are no observers
	 * asking to keep it for longer
	 * 
	 * @param naturalLifetime in seconds (NOT milliseconds)
	 */
	public void setNaturalLifetime(int naturalLifetime) {
		this.naturalLifetime = naturalLifetime * 1000;
	}

	public boolean isLinkGpsData() {
		return linkGpsData;
	}

	public void setLinkGpsData(boolean linkGpsData) {
		this.linkGpsData = linkGpsData;
	}

	/**
	 * 
	 * @return Software channel map for the data block.
	 */
	public int getChannelMap() {
		return channelMap;
	}

	/**
	 * Set the software channel map for the data block.
	 * 
	 * @param channelMap channel bitmap
	 */
	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}

	/**
	 * Get the number of separate channel sequences in the data. For nearly all data
	 * this will be the number of bits in the channel map. For beam formed data, or
	 * data derived from beam formed data this will generally be the total number of
	 * beams.
	 * <p>
	 * This should be used to set numbers of display channels, etc.
	 * 
	 * @return number of separate sequences of data in the stream.
	 */
	public int getSequenceCount() {
		// TODO will develop a new parameter to describe this, probably
		// an Integer which can be null, in which case behaviour
		// can revert to the channelMap bit count.
		return PamUtils.getNumChannels(channelMap);
	}

	/**
	 * There may not be a 1:1 mapping of channels to hydrophones
	 * 
	 * @return Hydrophone bit map
	 */
	public int getHydrophoneMap() {

		if (parentProcess == null) {
			return channelMap;
		}
		PamProcess sourceProcess = parentProcess.getSourceProcess();
		if (sourceProcess == null) {
			return channelMap;
		}
		if (AcquisitionProcess.class.isAssignableFrom(sourceProcess.getClass())) {
			AcquisitionControl daqControl = ((AcquisitionProcess) sourceProcess).getAcquisitionControl();
			return daqControl.ChannelsToHydrophones(channelMap);
		} else {
			return channelMap;
		}
	}

	/**
	 * Return the gain applied to any data created into this datablock.
	 * <p>
	 * Example 1: The amplifier module will just return it's gain
	 * <p>
	 * Example 2: The FFT module will return the loss due to windowing the data.
	 * <p>
	 * To convert to dB use 20*log10(Math.abs(getDataGain()));
	 * 
	 * @param iChan channel number
	 * @return gain as a factor (to allow for negative gains)
	 */
	public double getDataGain(int iChan) {
		return 1;
	}

	/**
	 * Get the total gain of this data block and all upstream datablocks.
	 * 
	 * @param iChan channel
	 * @return total gain.
	 */
	public double getCumulativeGain(int iChan) {
		double gain = getDataGain(iChan);
		PamProcess parentProcess = getParentProcess();
		while (parentProcess != null) {
			PamDataBlock parentBlock = parentProcess.getParentDataBlock();
			if (parentBlock == null) {
				break;
			}
			gain *= parentBlock.getDataGain(iChan);
			parentProcess = parentBlock.getParentProcess();
		}
		return gain;
	}

	/**
	 * @param dataName The dataName to set.
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getLoggingName() {
		String unitName = getParentProcess().getPamControlledUnit().getUnitName();
		if (dataName == null) {
			return unitName + "_data";
		}
		if (dataName.startsWith(unitName) && dataName.length() > unitName.length()) {
			return dataName;
		} else {
			return unitName + "_" + dataName;
		}
	}

	/**
	 * @return Returns the parentProcess.
	 */
	public PamProcess getParentProcess() {
		return parentProcess;
	}

	public void setParentProcess(PamProcess newProcess) {
		this.parentProcess = newProcess;
	}

	/**
	 * @return Returns the sourceProcess.
	 */
	public PamProcess getSourceProcess() {
		// if (sourceProcess != null) return sourceProcess;
		// else return parentProcess.GetSourceProcess();
		return parentProcess.getSourceProcess();
	}

	public PamRawDataBlock getRawSourceDataBlock() {
		PamDataBlock dataBlock = getSourceDataBlock();
		if (PamRawDataBlock.class.isAssignableFrom(dataBlock.getClass())) {
			return (PamRawDataBlock) dataBlock;
		}
		return null;
	}

	/**
	 * Get the first raw data block in the chain. This is useful for finding
	 * decimator datablocks.
	 * 
	 * @return the first raw data block in the chain
	 */
	public PamRawDataBlock getFirstRawSourceDataBlock() {
		PamDataBlock<Tunit> parentDB = this;
		PamProcess parentProc;
		while (true) {
			if (parentDB==null) break;
			parentProc = parentDB.getParentProcess();
			if (parentProc == null) {
				break;
			}
			if (parentProc.getParentDataBlock()!=null) {
				if (PamRawDataBlock.class.isAssignableFrom(parentProc.getParentDataBlock().getClass())) {
					return (PamRawDataBlock) parentProc.getParentDataBlock();
				}
			}
			parentDB = parentProc.getParentDataBlock();
		}
		return null;
	}

	/**
	 * Tries to find the raw data block source of the current data block. It does
	 * this by bouncing back and forth from ParentProcess to ParentDataBlock and
	 * back again, until it finds a PamRawDataBlock or either the ParentProcess or
	 * the ParentDataBlock is null.
	 * 
	 * @return the PamRawDataBlock that serves as the source data, or null if no
	 *         data block is found
	 */
	public PamRawDataBlock getRawSourceDataBlock2() {
		PamDataBlock<Tunit> parentDB = this;
		PamProcess parentProc;
		while (true) {
			if (PamRawDataBlock.class.isAssignableFrom(parentDB.getClass())) {
				return (PamRawDataBlock) parentDB;
			}

			parentProc = parentDB.getParentProcess();
			if (parentProc == null) {
				break;
			}

			PamDataBlock<Tunit> prevDB = parentProc.getParentDataBlock();
			if (prevDB == null) {
				break;
			} else {
				parentDB = prevDB;
			}

		}
		if (PamRawDataBlock.class.isAssignableFrom(parentDB.getClass())) {
			return (PamRawDataBlock) parentDB;
		}
		return null;
	}

	@Override
	public void addObserver(PamObserver o) {
		super.addObserver(o);
		if (parentProcess != null) {
			o.setSampleRate(parentProcess.getSampleRate(), true);
		}
	}

	@Override
	public void addObserver(PamObserver o, boolean reThread) {
		super.addObserver(o, reThread);
		if (parentProcess != null) {
			o.setSampleRate(parentProcess.getSampleRate(), true);
		}
	}

	/**
	 * 
	 * @return Class type for the sotred data units in this data block.
	 */
	public Class getUnitClass() {
		return unitClass;
	}

	/**
	 * clean up datablock when it's no longer needed
	 */
	public void dispose() {
		stopTimer();
		clearAll();
	}
	
	/**
	 * Had some issues with the Timer holding a reference to the underlying PamDataBlock 
	 * (RoccaContourDataBlock, in this case) and not releasing it for garbage collection.
	 * Added in this method to force the timer to stop and release it's hold.
	 */
	@Override
	public void stopTimer() {
		super.stopTimer();
		removeTimer.stop();
	}

	public void autoSetDataBlockMixMode() {
		if (AcousticDataUnit.class.isAssignableFrom(unitClass)) {
			// System.out.println(unitClass + " is acoustic data" );
			setMixedDirection(MIX_INTODATABASE);
		} else {
			// System.out.println(unitClass + " is NOT acoustic data" );
			setMixedDirection(MIX_OUTOFDATABASE);
		}
	}

	public int getMixedDirection() {
		return mixedDirection;
	}

	public void setMixedDirection(int mixedDirection) {
		this.mixedDirection = mixedDirection;
	}

	/**
	 * Get Information indicating what localisation information might be available
	 * for the data in this block. Note that a flag being set here is no guarantee
	 * that the data units will have a particular type of localisation data, since
	 * localisation may have failed on individual units.
	 * 
	 * @return localisation flags
	 */
	public LocalisationInfo getLocalisationContents() {
		return localisationContents;
	}

	/**
	 * Set a bitmap of flags indicating what localisation information might be
	 * available for the data in this block. Note that a flag being set here is no
	 * guarantee that the data units will have a particular type of localisation
	 * data, since localisation may have failed on individual units.
	 * 
	 * @param localisationContents bitmap of localisation contents
	 */
	public void setLocalisationContents(int localisationContents) {
		this.localisationContents.setLocContent(localisationContents);
	}

	/**
	 * Add a single flag indicating what localisation information might be available
	 * for the data in this block. Note that a flag being set here is no guarantee
	 * that the data units will have a particular type of localisation data, since
	 * localisation may have failed on individual units.
	 * 
	 * @param localisationContents bitmap of localisation contents
	 */
	public void addLocalisationContents(int localisationContents) {
		this.localisationContents.addLocContent(localisationContents);
	}

	public static final int ITERATOR_END = -1;

	/**
	 * Get a list iterator through the data from a given position. the user of the 
	 * iterator will then have to work out if they go fowards or backwards through the 
	 * data. 
	 * @param whereFrom index in data, or ITERATOR_END (-1) to go to end
	 * @return iterator through data
	 */
	public ListIterator<Tunit> getListIterator(int whereFrom) {
		if (whereFrom < 0) {
			return pamDataUnits.listIterator(pamDataUnits.size());
		} else {
			return pamDataUnits.listIterator(whereFrom);
		}
	}


	/**
	 * Only accept an iterator for a unit that matches the time exactly
	 */
	static final public int MATCH_EXACT = 1;
	/**
	 * If there is not exact time match, set the iterator so that the first element
	 * it returns will be the element before the requested time.
	 */
	static final public int MATCH_BEFORE = 2;
	/**
	 * If there is not exact time match, set the iterator so that the first element
	 * it returns will be the element after the requested time.
	 */
	static final public int MATCH_AFTER = 3;
	/**
	 * Set the iterator so that a call to previous() will give the first wanted
	 * element
	 */
	static final public int POSITION_BEFORE = 1;
	/**
	 * Set the iterator so that a call to next() will give the first wanted element
	 */
	static final public int POSITION_AFTER = 2;

	/**
	 * Get an iterator, positioned at the given startTime.
	 * 
	 * @param startTime Start time in milliseconds.
	 * @param channels  map of channels of interest.
	 * @param match     match criteria = MATCH_EXACT, MATCH_BEFORE, MATCH_AFTER
	 * @param position  where to position the cursor: POSITION_BEFORE,
	 *                  POSITION_AFTER
	 * @return a list iterator ...
	 */
	public ListIterator<Tunit> getListIterator(long startTimeMillis, int channels, int match, int position) {
		if (pamDataUnits.size() == 0) {
			return null;
		}
		// see if were closer to the start or the end
		long firstTime = getFirstUnit().getTimeMilliseconds();
		long lastTime = getLastUnit().getTimeMilliseconds();
		if (lastTime - startTimeMillis < startTimeMillis - firstTime) {
			return getListIteratorFromEnd(startTimeMillis, channels, match, position);
		} else {
			return getListIteratorFromStart(startTimeMillis, channels, match, position);
		}
	}
	
	/**
	 * copy the data using an iterator, positioned at the given startTime.
	 * @param startTime Start time in milliseconds.
	 * @param channels  map of channels of interest.
	 * @param match     match criteria = MATCH_EXACT, MATCH_BEFORE, MATCH_AFTER
	 * @param position  where to position the cursor: POSITION_BEFORE,
	 *                  POSITION_AFTER 
	 * @param startTimeMillis
	 * @param channels
	 * @param match
	 * @param position
	 * @return temporary copy of the data 
	 */
	public ArrayList<Tunit> getDataCopy(long startTimeMillis, int channels, int match, int position) {
		synchronized (getSynchLock()) {
			return getDataCopy(getListIterator(startTimeMillis, channels, match, position));
		}
	}

	/**
	 * Get an iterator, positioned at the given startTime.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * 
	 * @param startTime Start time in milliseconds.
	 * @param channels  map of channels of interest.
	 * @param match     match criteria = MATCH_EXACT, MATCH_BEFORE, MATCH_AFTER
	 * @param position  where to position the cursor: POSITION_BEFORE,
	 *                  POSITION_AFTER
	 * @return a list iterator ...
	 */
	public ListIterator<Tunit> getListIteratorFromStart(long startTime, int channels, int match, int position) {
		ListIterator<Tunit> iterator = getListIterator(0);
		Tunit thisOne = null;
		try {
			while (iterator.hasNext()) {
				thisOne = iterator.next();
				if ((thisOne.getChannelBitmap() & channels) != channels) {
					continue;
				}
				if (thisOne.getTimeMilliseconds() >= startTime) {
					/*
					 * so we know what's going on, get the pointer positioned after the one we want.
					 * That is where it will be if we got an exact match of if we wanted a time
					 * lower or equal to that requests.
					 */
					if (thisOne.getTimeMilliseconds() > startTime) {
						if (match == MATCH_EXACT) {
							return null;
						} else if (match == MATCH_AFTER) {
							// we want the one before this
							// iterator.next();
						} else { // match == MATCH_BEFORE
							iterator.previous();
						}
					} else {
					}
					if (position == POSITION_BEFORE) {
						// need to move back one
						iterator.previous();
					}
					return iterator;
				}
			}
		} catch (NoSuchElementException ex) {

		}
		return null;
	}
	
	/**
	 * copy the data using an iterator, positioned at the given startTime.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * @param startTime Start time in milliseconds.
	 * @param channels  map of channels of interest.
	 * @param match     match criteria = MATCH_EXACT, MATCH_BEFORE, MATCH_AFTER
	 * @param position  where to position the cursor: POSITION_BEFORE,
	 *                  POSITION_AFTER 
	 * @param startTimeMillis
	 * @param channels
	 * @param match
	 * @param position
	 * @return temporary copy of the data 
	 */
	public ArrayList<Tunit> getDataCopyFromStart(long startTimeMillis, int channels, int match, int position) {
		synchronized (getSynchLock()) {
			return getDataCopy(getListIteratorFromStart(startTimeMillis, channels, match, position));
		}
	}

	/**
	 * Get an iterator, positioned at the given startTime.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * 
	 * @param startTime Start time in milliseconds.
	 * @param channels  map of channels of interest.
	 * @param match     match criteria = MATCH_EXACT, MATCH_BEFORE, MATCH_AFTER
	 * @param position  where to position the cursor: POSITION_BEFORE,
	 *                  POSITION_AFTER
	 * @return a list iterator ...
	 */
	public ListIterator<Tunit> getListIteratorFromEnd(long startTime, int channels, int match, int position) {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> iterator = getListIterator(ITERATOR_END);
			Tunit thisOne = null;
			try {
				while (iterator.hasPrevious()) {
					thisOne = iterator.previous();
					if ((thisOne.getChannelBitmap() & channels) != channels) {
						continue;
					}
					if (thisOne.getTimeMilliseconds() <= startTime) {
						if (thisOne.getTimeMilliseconds() < startTime)
							if (match == MATCH_EXACT) {
								return null;
							} else if (match == MATCH_BEFORE) {

							} else { // match == MATCH_AFTER
								iterator.next();
							}
						if (position == POSITION_AFTER) {
							iterator.next();
						}
						return iterator;
					}
				}
			} catch (NoSuchElementException ex) {

			}
			return iterator;
		}
	}	
	
	/**
	 * copy the data using an iterator, positioned at the given startTime.
	 * <p>
	 * Note that this method works specifically on the Channel Map. If this data
	 * block may have sequence numbers or channels, a new method needs to be created
	 * using the getSequenceBitmap call instead of getChannelBitmap.
	 * @param startTime Start time in milliseconds.
	 * @param channels  map of channels of interest.
	 * @param match     match criteria = MATCH_EXACT, MATCH_BEFORE, MATCH_AFTER
	 * @param position  where to position the cursor: POSITION_BEFORE,
	 *                  POSITION_AFTER 
	 * @param startTimeMillis
	 * @param channels
	 * @param match
	 * @param position
	 * @return temporary copy of the data 
	 */
	public ArrayList<Tunit> getDataCopyFromEnd(long startTimeMillis, int channels, int match, int position) {
		synchronized (getSynchLock()) {
			return getDataCopy(getListIteratorFromEnd(startTimeMillis, channels, match, position));
		}
	}

	public void dumpBlockContents() {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> listIterator = getListIterator(0);
			PamDataUnit unit;
			System.out.println(String.format("***** Data Dump from %s *****", getDataName()));
			while (listIterator.hasNext()) {
				unit = listIterator.next();
				System.out.println(String.format("Object %d, Index %d, Time %d, Channels %d, SequenceNums %d",
						unit.hashCode(), unit.getAbsBlockIndex(), unit.getTimeMilliseconds(), unit.getChannelBitmap(),
						unit.getSequenceBitmap()));
			}
		}
	}
	
	/**
	 * Get a complete copy of the data. Using this is often safer than using internal iterators
	 * to go through the data for plotting, etc., since this copy of the data will not need to be 
	 * synchronized, so will not cause thread lockouts. Generally copying the array will be faster than
	 * doing anything with the data within the array. 
	 * @return temporary complete copy of the data in a new array. 
	 */
	public ArrayList<Tunit> getDataCopy() {
		ArrayList<Tunit> copy = null;
		synchronized (getSynchLock()) {
			copy = new ArrayList<>(pamDataUnits);
		}
		return copy;
	}

	/**
	 * copy the contents of an iterator into a new array list. This can be used
	 * to avoid iterating through the data in complex processing or drawing function which 
	 * might cause update / synchronisation lock issues. Note that this function itself
	 * doesn't synch, since the synch needs to be around whatever makes the iterator. <p>
	 * Each of the getListIterator functions is going to get a corresponding function that 
	 * calls this to copy the data. 
	 * @param iterator iterator through data 
	 * @return temporary array list of content of that iterator
	 */
	private ArrayList<Tunit> getDataCopy(ListIterator<Tunit> iterator) {
		ArrayList<Tunit> copy = new ArrayList<>();
		while (iterator.hasNext()) {
			copy.add(iterator.next());
		}
		return copy;
	}
	
	/**
	 * Get a temporary copy of all data between two times (inclusive)
	 * @param t1 first time
	 * @param t2 last time
	 * @param assumeOrder Assume units are in order, so can break as soon as a unit after t2 is reached 
	 * @return temporary copy of the data
	 */
	public ArrayList<Tunit> getDataCopy(long t1, long t2, boolean assumeOrder) {
		ArrayList<Tunit> copy = new ArrayList<>();
		synchronized (getSynchLock()) {
			ListIterator<Tunit> it = pamDataUnits.listIterator();
			while (it.hasNext()) {
				Tunit dataUnit = it.next();
				if (dataUnit.getTimeMilliseconds() < t1) {
					continue;
				}
				if (assumeOrder && dataUnit.getTimeMilliseconds() > t2) {
					break;
				}
				copy.add(dataUnit);
			}
		}
		
		return copy;
	}
	/**
	 * Get a temporary copy of all data between two times (inclusive) using a data selector
	 * @param t1 first time
	 * @param t2 last time
	 * @param assumeOrder Assume units are in order, so can break as soon as a unit after t2 is reached 
	 * @param dataSelector data selector (can be null)
	 * @return temporary copy of the data
	 */
	public ArrayList<Tunit> getDataCopy(long t1, long t2, boolean assumeOrder, DataSelector dataSelector) {
		if (dataSelector == null || dataSelector.getParams().getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE) {
			return getDataCopy(t1, t2, assumeOrder);
		}
		else {
			ArrayList<Tunit> copy = new ArrayList<>();
			synchronized (getSynchLock()) {
				ListIterator<Tunit> it = pamDataUnits.listIterator();
				while (it.hasNext()) {
					Tunit dataUnit = it.next();
					if (dataUnit.getTimeMilliseconds() < t1) {
						continue;
					}
					if (assumeOrder && dataUnit.getTimeMilliseconds() > t2) {
						break;
					}
					if (dataSelector.scoreData(dataUnit) > 0) {
						copy.add(dataUnit);
					}
				}
			}
			
			return copy;
		}
	}

	private Vector<ProcessAnnotation> processAannotations = new Vector<ProcessAnnotation>();

	private Vector<OfflineDataMap> offlineDataMaps = null;

	private SQLLogging logging;

	private JSONObjectDataSource jsonDataSource;
	

	public Vector<ProcessAnnotation> getProcessAnnotations() {
		return processAannotations;
	}

	/**
	 * Gets all the annotations from the parent process and all upstream processes.
	 * 
	 * @return total number of annotations
	 */
	private int createAllProcessAnnotations() {

		return processAannotations.size();
	}

	/**
	 * Copies all annotations over from the source DataBlock, then adds in the new
	 * ones from the new datablock.
	 * 
	 * @param sourceData     source Datablock
	 * @param newAnnotations source of new annotations
	 * @return total number of annotations
	 */
	public int createProcessAnnotations(PamDataBlock sourceData, ProcessAnnotator newAnnotations) {
		return createProcessAnnotations(sourceData, newAnnotations, false);
	}

	/**
	 * Copies all annotations over from the source DataBlock, then adds in the new
	 * ones from the new datablock.
	 * 
	 * @param sourceData       source Datablock
	 * @param newAnnotations   source of new annotations
	 * @param notifydownstream notify downstream modules.
	 * @return total number of annotations
	 */
	public int createProcessAnnotations(PamDataBlock sourceData, ProcessAnnotator newAnnotations,
			boolean notifyDownstream) {
		if (sourceData != null && sourceData.getProcessAnnotations() != null) {
			processAannotations = (Vector<ProcessAnnotation>) sourceData.getProcessAnnotations().clone();
		} else {
			processAannotations = new Vector<ProcessAnnotation>();
		}
		int n = newAnnotations.getNumAnnotations(this);
		for (int i = 0; i < n; i++) {
			processAannotations.add(newAnnotations.getAnnotation(this, i));
		}

		// for (int i = 0; i < countObservers(); i++) {
		// if (pamObservers.get(i).getObserverObject() != parentProcess) {
		// pamObservers.get(i).getObserverObject().setSampleRate(sampleRate, notify);
		// }
		// }
		if (notifyDownstream) {
			PamObserver pamObserver;
			for (int i = 0; i < countObservers(); i++) {
				pamObserver = getPamObserver(i).getObserverObject();

				if (pamObserver == null || pamObserver == parentProcess) {
					continue;
				}
				if (PamProcess.class.isAssignableFrom(pamObserver.getClass())) {
					((PamProcess) pamObserver).createAnnotations(true);
				}
			}
		}

		return processAannotations.size();
	}

	/**
	 * Finds an annotation with the given type and name
	 * 
	 * @param type annotation type
	 * @param name annotation name
	 * @return annotation object
	 */
	public ProcessAnnotation findAnnotation(String type, String name) {
		if (processAannotations == null) {
			return null;
		}
		ProcessAnnotation a;
		for (int i = 0; i < processAannotations.size(); i++) {
			a = processAannotations.get(i);
			if (a.getName().equals(name) && a.getType().equals(type)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Finds an annotation with the same type and name as the template annotation
	 * 
	 * @param template template annotation
	 * @return annotation object
	 */
	public ProcessAnnotation findAnnotation(ProcessAnnotation template) {
		return findAnnotation(template.getType(), template.getName());
	}

	/**
	 * @param recycling the recycling to set
	 */
	public void setRecycling(boolean recycling) {
		this.recycling = recycling;
		recycledUnits = new Vector<Tunit>();
		setRecyclingStoreLength(getRecyclingStoreLength());
	}

	/**
	 * @return the recycling
	 */
	public boolean isRecycling() {
		return recycling;
	}

	/**
	 * @param recyclingStoreLength the recyclingStoreLength to set
	 */
	public void setRecyclingStoreLength(int recyclingStoreLength) {
		this.recyclingStoreLength = recyclingStoreLength;
		recycledUnits.setSize(recyclingStoreLength);
	}

	/**
	 * @return the recyclingStoreLength
	 */
	public int getRecyclingStoreLength() {
		return recyclingStoreLength;
	}

	/**
	 * Receive notifications from the main PamController.
	 * 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.HYDROPHONE_ARRAY_CHANGED:
			clearDataOrigins();
			break;
		}
	}

	/**
	 * Called when the hydrophone array is changed. Will delete all calclulated
	 * origins in the data which should then get recalculated if needed by any part
	 * of PAMGuard.
	 */
	private void clearDataOrigins() {
		synchronized (synchronizationLock) {
			ListIterator<Tunit> it = getListIterator(0);
			while (it.hasNext()) {
				it.next().clearOandAngles();
			}
		}
	}

	/**
	 * @param BinaryDataSource the BinaryDataSource to set
	 */
	public void setBinaryDataSource(BinaryDataSource binaryDataSource) {
		this.binaryDataSource = binaryDataSource;
		// if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
		// binaryDataMap = new OfflineDataMap<Tunit>(this);
		// }
	}

	/**
	 * @return the BinaryDataSource
	 */
	public BinaryDataSource getBinaryDataSource() {
		return binaryDataSource;
	}
	
	/**
	 * Set the data source for exporting as a JSON-formatted string
	 * @param jsonDataSource
	 */
	public void setJSONDataSource(JSONObjectDataSource jsonDataSource) {
		this.jsonDataSource = jsonDataSource;
	}
	
	/**
	 * Get the data source for exporting as a JSON-formatted string
	 * @return
	 */
	public JSONObjectDataSource getJSONDataSource() {
		return jsonDataSource;
	}
	

	public void SetLogging(SQLLogging logging) {

		this.logging = logging;

		// if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
		// databaseDataMap = new OfflineDataMap<Tunit>(this);
		// }

		// System.out.println(this.getClass().toString() + ", set logging, " +
		// this.logging.toString());

	}

	public SQLLogging getLogging() {
		return logging;
	}
	
	/**
	 * Gets a data provider for Tethys. These will probably need
	 * to be bespoke, but for now will autogenerate based on the SQLLogging information. 
	 * @return the tethysDataProvider
	 */
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		return null;
	}

	/**
	 * Get the level of automation employed by the generation of these data. 
	 * Should ideally be completed for everything providing data to Tethys. 
	 * @return level of automation for this data block. 
	 */
	public DataAutomationInfo getDataAutomationInfo() {
		return null;
	}
	
	/**
	 * Get information about species types that may occur within this data 
	 * block.  Primarily for conversion into Tethys compatible data, but may 
	 * prove to have other uses. 
	 * @return Types of species information available within this datablock. 
	 */
	public DataBlockSpeciesManager<Tunit> getDatablockSpeciesManager() {
		return null;
	}

	final public boolean getCanLog() {
		return (logging != null);
	}

	public SQLLogging getUIDRepairLogging() {
		return getLogging();
	}

	/**
	 * Should log the data unit to the database ?
	 * 
	 * @param pamDataUnit dataunit to consider
	 * @return true if data should be logged.
	 */
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		if (isOffline) {
			return false;
		} else {
			return shouldLog;
		}
	}

	/**
	 * In general, should the data block try to log to the database?
	 * 
	 * @return true if data should be logged.
	 */
	public boolean getShouldLog() {
		if (isOffline) {
			return false;
		} else {
			return shouldLog;
		}
	}

	/**
	 * Set if data should be logged to the database.
	 * 
	 * @param shouldLog flag to log data.
	 */
	final public void setShouldLog(boolean shouldLog) {
		this.shouldLog = (shouldLog && getCanLog());
	}

	/**
	 * Get flag to say whether data should be stored in the binary store
	 * 
	 * @param pamDataUnit data unit
	 * @return true if the data unit shoule be saved.
	 */
	public boolean getShouldBinary(PamDataUnit pamDataUnit) {
		return (shouldBinary && getBinaryDataSource() != null);
	}

	/**
	 * Set flag to say if data should be stored in the binary store.
	 * 
	 * @param shouldBinary flag to say data shoule be stored.
	 */
	public void setShouldBinary(boolean shouldBinary) {
		this.shouldBinary = shouldBinary;
	}

	/**
	 * Adds a new offline datamap to the data block
	 * <p>
	 * Note that there may be several maps from different types of storage (although
	 * only one should have anything in it in a sensible world).
	 * <p>
	 * It's also possible that these will be recreated during a run, and we don't
	 * want two of the same type in the same datablock, so check that there isn't
	 * already one and remove it
	 * 
	 * @param offlineDataMap
	 */
	public void addOfflineDataMap(OfflineDataMap offlineDataMap) {
		OfflineDataStore dataSource = offlineDataMap.getOfflineDataSource();
		removeOfflineDataMap(dataSource);
		if (offlineDataMaps == null) {
			offlineDataMaps = new Vector<OfflineDataMap>();
		}
		offlineDataMaps.add(offlineDataMap);
	}

	/**
	 * 
	 * @return the number of different offline data maps
	 */
	public int getNumOfflineDataMaps() {
		if (offlineDataMaps == null) {
			return 0;
		}
		return offlineDataMaps.size();
	}

	/**
	 * 
	 * @param iMap index of map (see getNumOfflineDataMaps)
	 * @return an OfflineDataMap from that index in the list.
	 */
	public OfflineDataMap getOfflineDataMap(int iMap) {
		return offlineDataMaps.get(iMap);
	}

	/**
	 * 
	 * @param dataSource an offline data source (e.g. binaray storage, database
	 *                   storage, etc.
	 * @return the offline data map for a specific OfflineDataSource or null
	 */
	public OfflineDataMap getOfflineDataMap(OfflineDataStore dataSource) {
		if (offlineDataMaps == null) {
			return null;
		}
		Iterator<OfflineDataMap> mapsIterator = offlineDataMaps.iterator();
		OfflineDataMap aMap;
		while (mapsIterator.hasNext()) {
			aMap = mapsIterator.next();
			if (aMap.getOfflineDataSource() == dataSource) {
				return aMap;
			}
		}
		return null;
	}

	/**
	 * New plan - always use the binary store if it has any data at all. If the
	 * binary store doesn't exist or is empty, only then use the database.
	 * 
	 * @return a data map
	 */
	public OfflineDataMap getPrimaryDataMap() {
		if (offlineDataMaps == null) {
			return null;
		}
		Iterator<OfflineDataMap> mapsIterator = offlineDataMaps.iterator();
		OfflineDataMap aMap, bestMap = null;
		int mostData = 0;
		int dataHeight;
		while (mapsIterator.hasNext()) {
			aMap = mapsIterator.next();
			dataHeight = aMap.getDataCount();
			// System.out.println(aMap.getOfflineDataSource().getClass());
			if (aMap.getOfflineDataSource().getClass() == binaryFileStorage.BinaryStore.class && dataHeight > 0) {
				return aMap;
			}
			if (dataHeight > mostData || bestMap == null) {
				bestMap = aMap;
				mostData = dataHeight;
			}
		}
		return bestMap;
	}

	/**
	 * Get a data map which has a datagram
	 * 
	 * @return
	 */
	public OfflineDataMap getDatagrammedMap() {
		if (getDatagramProvider() == null) {
			return null;
		}
		// return the binary data map by default. Can overrride
		// in other data blocks.
		return getPrimaryDataMap();
	}

	/**
	 * remove a no longer needed offline data map.
	 * 
	 * @param dataSource data source (e.g. binary, database, raw data, etc.
	 */
	public void removeOfflineDataMap(OfflineDataStore dataSource) {
		if (offlineDataMaps == null) {
			return;
		}
		Iterator<OfflineDataMap> mapsIterator = offlineDataMaps.iterator();
		OfflineDataMap aMap;
		while (mapsIterator.hasNext()) {
			aMap = mapsIterator.next();
			if (aMap.getOfflineDataSource() == dataSource) {
				mapsIterator.remove();
			}
		}
	}

	/**
	 * Work out some basic information about the elements that need saving from
	 * these data.
	 * 
	 * @param dataStore data source that want's to save the data.
	 * @return an object of information
	 */
	public SaveRequirements getSaveRequirements(OfflineDataStore dataStore) {
		if (dataStore != getPrimaryDataMap().getOfflineDataSource()) {
			return null;
		}
		SaveRequirements sr = new SaveRequirements(this);
		ListIterator<Tunit> li = getListIterator(0);
		Tunit aUnit;
		/*
		 * First go through the main list and see what's been added (0 index) or
		 * updated.
		 */
		while (li.hasNext()) {
			aUnit = li.next();
			if (aUnit.getDatabaseIndex() == 0) {
				sr.numAdditions++;
				continue;
			}
			if (aUnit.getUpdateCount() > 0) {
				sr.addUpdateUnit(aUnit);
			}
		}
		/**
		 * Then go through the list of deleted items and get their indexes.
		 */
		if (removedItems != null) {
			li = removedItems.listIterator();
			while (li.hasNext()) {
				aUnit = li.next();
				if (aUnit.getDatabaseIndex() > 0) {
					sr.addDeletedUnit(aUnit);
				}
			}
		}

		return sr;
	}

	/**
	 * Similar functionality to getOfflineData, but this will launch a separate
	 * worker thread to do the actual work getting the data. The worker thread will
	 * call getOfflineData.
	 * <p>
	 * getOfflineData will probably (if not overridden) be sending data to the
	 * update member function of the observer, but from the worker thread. Once it's
	 * complete, it will send a message to say that data are loaded.
	 * <p>
	 * It's possible that the user will do something which causes this to be called
	 * again before the previous thread completed execution, in which case there are
	 * three options:
	 * <p>
	 * OFFLINE_DATA_INTERRUPT - previous thread will be terminated
	 * <p>
	 * OFFLINE_DATA_WAIT - wait for previous thread to terminate, then start this
	 * load
	 * <p>
	 * OFFLINE_DATA_CANCEL - give up and return
	 * 
	 * @param dataObserver   observer of the loaded data
	 * @param loadObserver   observer to get status information on the load.
	 * @param startMillis    data start time in milliseconds
	 * @param endMillis      data end time in milliseconds.
	 * @param loadKeepLayers number of layers of data to keep in the datablocks.
	 * @param interrupt      interrupt options.
	 */
	public void orderOfflineData(PamObserver dataObserver, LoadObserver loadObserver, long startMillis, long endMillis,
			int loadKeepLayers, int interrupt) {
		offlineDataLoading.orderOfflineData(dataObserver, loadObserver, startMillis, endMillis, loadKeepLayers,
				interrupt, false);
	}

	/**
	 * Similar functionality to getOfflineData, but this will launch a separate
	 * worker thread to do the actual work getting the data. The worker thread will
	 * call getOfflineData.
	 * <p>
	 * getOfflineData will probably (if not overridden) be sending data to the
	 * update member function of the observer, but from the worker thread. Once it's
	 * complete, it will send a message to say that data are loaded.
	 * <p>
	 * It's possible that the user will do something which causes this to be called
	 * again before the previous thread completed execution, in which case there are
	 * three options:
	 * <p>
	 * OFFLINE_DATA_INTERRUPT - previous thread will be terminated
	 * <p>
	 * OFFLINE_DATA_WAIT - wait for previous thread to terminate, then start this
	 * load
	 * <p>
	 * OFFLINE_DATA_CANCEL - give up and return
	 * 
	 * @param offlineDataLoadInfo objerct whihc contains all info needed to load
	 *                            data.
	 */
	public void orderOfflineData(OfflineDataLoadInfo offlineDataLoadInfo) {
		offlineDataLoading.orderOfflineData(offlineDataLoadInfo);
	}

	/**
	 * Similar functionality to getOfflineData, but this will launch a separate
	 * worker thread to do the actual work getting the data. The worker thread will
	 * call getOfflineData.
	 * <p>
	 * getOfflineData will probably (if not overridden) be sending data to the
	 * update member function of the observer, but from the worker thread. Once it's
	 * complete, it will send a message to say that data are loaded.
	 * <p>
	 * It's possible that the user will do something which causes this to be called
	 * again before the previous thread completed execution, in which case there are
	 * three options:
	 * <p>
	 * OFFLINE_DATA_INTERRUPT - previous thread will be terminated
	 * <p>
	 * OFFLINE_DATA_WAIT - wait for previous thread to terminate, then start this
	 * load
	 * <p>
	 * OFFLINE_DATA_CANCEL - give up and return
	 * 
	 * @param dataObserver   observer of the loaded data
	 * @param loadObserver   observer to get status information on the load.
	 * @param startMillis    data start time in milliseconds
	 * @param endMillis      data end time in milliseconds.
	 * @param loadKeepLayers Number of layers of datablock which should hang on to
	 *                       loaded data rather than delete it immediately.
	 * @param interrupt      interrupt options.
	 * @param allowRepeats   allow repeated loads of exactly the same data.
	 * 
	 */
	public void orderOfflineData(PamObserver dataObserver, LoadObserver loadObserver, long startMillis, long endMillis,
			int loadKeepLayers, int interrupt, boolean allowRepeats) {
		offlineDataLoading.orderOfflineData(dataObserver, loadObserver, startMillis, endMillis, loadKeepLayers,
				interrupt, allowRepeats);
	}

	/**
	 * Gets data for offline display, playback, etc.
	 * <p>
	 * This is used to get data from some upstream process which is quite different
	 * to the function loadViewerData(...) which loads data directly associated with
	 * this data block.
	 * <p>
	 * For example, this might be called in the FFT data block by the spectrogram
	 * which wants some data to display. The FFT data block does not have this data,
	 * so it passes the request up to it's process which will in turn pass the
	 * request on until it reaches a module which is capable of loading data into
	 * data units and sending them back down the line.
	 * 
	 * @param observer           data observer which will receive the data
	 * @param startMillis        start time in milliseconds
	 * @param endMillis          end time in milliseconds
	 * @param loadKeepLayers
	 * @param allowRepeats       allow the same data to be loaded a second time.
	 * @param cancellationObject
	 * @return answer: .
	 */
	// public int getOfflineData(PamObserver observer, PamObserver endUser, long
	// startMillis, long endMillis, int loadKeepLayers,
	// boolean allowRepeats, RequestCancellationObject cancellationObject) {
	//
	// return offlineDataLoading.getOfflineData(observer, endUser, startMillis,
	// endMillis, loadKeepLayers, allowRepeats, cancellationObject);
	// }
	//
	/**
	 * Gets data for offline display, playback, etc.
	 * <p>
	 * This is used to get data from some upstream process which is quite different
	 * to the function loadViewerData(...) which loads data directly associated with
	 * this data block.
	 * <p>
	 * For example, this might be called in the FFT data block by the spectrogram
	 * which wants some data to display. The FFT data block does not have this data,
	 * so it passes the request up to it's process which will in turn pass the
	 * request on until it reaches a module which is capable of loading data into
	 * data units and sending them back down the line.
	 * 
	 * @param observer           data observer which will receive the data
	 * @param cancellationObject
	 */
	public int getOfflineData(OfflineDataLoadInfo offlineLoadInfo) {
		return offlineDataLoading.getOfflineData(offlineLoadInfo);
	}

	/**
	 * Cancel the current offline data order
	 */
	public void cancelDataOrder() {
		offlineDataLoading.cancelDataOrder();
	}

	/**
	 * Cancel the current offline data order
	 * 
	 * @param quedItems- true to cancel all low priority threads in the wait que
	 */
	public void cancelDataOrder(boolean quedItems) {
		offlineDataLoading.cancelDataOrder(quedItems);
	}

	public void clearDeletedList() {
		if (removedItems != null) {
			this.removedItems.clear();
		}
	}

	/**
	 * Get the next data start point. i.e. the time of the start of a map point
	 * which is > timeMillis
	 * 
	 * @param timeMillis current time in milliseconds
	 * @return start time of the next data start.
	 */
	public long getNextDataStart(long timeMillis) {
		OfflineDataMap offlineMap = getPrimaryDataMap();
		if (offlineMap == null) {
			return OfflineDataMap.NO_DATA;
		}
		return offlineMap.getNextDataStart(timeMillis);
	}

	/**
	 * Get the previous data end point. i.e. the time of the end of a map point
	 * which is < timeMillis
	 * 
	 * @param timeMillis current time in milliseconds
	 * @return start time of the next data start.
	 */
	public long getPrevDataEnd(long timeMillis) {
		OfflineDataMap offlineMap = getPrimaryDataMap();
		if (offlineMap == null) {
			return OfflineDataMap.NO_DATA;
		}
		return offlineMap.getPrevDataEnd(timeMillis);
	}

	public void setDatagramProvider(DatagramProvider datagramProvider) {
		this.datagramProvider = datagramProvider;
	}

	public DatagramProvider getDatagramProvider() {
		return datagramProvider;
	}

	/**
	 * @return the canClipGenerate
	 */
	public boolean isCanClipGenerate() {
		return canClipGenerate;
	}

	/**
	 * @param canClipGenerate the canClipGenerate to set
	 */
	public void setCanClipGenerate(boolean canClipGenerate) {
		this.canClipGenerate = canClipGenerate;
	}

	/**
	 * Quick integer datablock id which is based on the dataname
	 * 
	 * @return
	 */
	public int getQuickId() {
		String dataName = getDataName();
		if (dataName == null)
			return 0;
		int n = dataName.length();
		if (n < 2)
			return 0;
		int id = dataName.charAt(0) << 24;
		id += dataName.charAt(1) << 16;
		id += dataName.charAt(n - 1) << 8;
		char lastChar = 0;
		for (int i = 0; i < n; i++) {
			lastChar ^= dataName.charAt(i);
		}
		id += lastChar;
		return id;
	}

	/**
	 * Make a better quickId that is more resilient to changes at any point in the
	 * string, i.e. checksum all four bytes.
	 * 
	 * @return and integer made up of exclusive OR's of all bytes making up the long
	 *         data name
	 */
	public int getQuickId2() {
		String name = getLongDataName();
		if (name == null) {
			return 0;
		}
		char[] chars = name.toCharArray();
		int[] bytes = new int[4];
		for (int i = 0; i < chars.length; i++) {
			int ib = i % 4;
			bytes[ib] ^= chars[i];
		}
		int intVal = ((bytes[0] & 0xFF) << 24) + ((bytes[1] & 0xFF) << 16) + ((bytes[2] & 0xFF) << 8)
				+ (bytes[3] & 0xFF);
		return intVal;
	}

	/**
	 * Sorts all the data units in the data block in order of time (unless a a data
	 * unit class chooses to overwrite the compareTo function.
	 */
	public void sortData() {
		Collections.sort(pamDataUnits);
	}

	private PamSymbolManager pamSymbolManager;

	private DataSelectorCreator dataSelectorCreator;

	public void setRecordingTrigger(RecorderTrigger recorderTrigger) {
		this.recorderTrigger = recorderTrigger;
		RecorderControl.registerRecorderTrigger(recorderTrigger);
	}

	public RecorderTrigger getRecordingTrigger() {
		return recorderTrigger;
	}

	/**
	 * Get an iterator which can iterator through only data units with data from
	 * particular channels in a databock.
	 * <p>
	 * In viewer mode, channel iterators are saved for reuse but will be deleted /
	 * cleared if data are removed or added to the datablock to avoid concurrency
	 * exceptions.
	 * 
	 * @param channelMap channel map
	 * @param whereFrom  0, start at start,
	 * @return a channel iterator
	 */
	public ChannelIterator<Tunit> getChannelIterator(int channelMap, int whereFrom) {
		synchronized (synchronizationLock) {
			ChannelIterator<Tunit> channelIterator = null;
			if (isOffline) {
				channelIterator = (ChannelIterator<Tunit>) findIterator(true, channelMap);
				// System.out.println("Channel Iterator: " + channelIterator + " " +
				// storedIterators.size());
				if (channelIterator == null) {
					channelIterator = new ChannelIterator<Tunit>(this, channelMap, whereFrom);
					storedIterators.add(channelIterator);
				}
			} else {
				channelIterator = new ChannelIterator<Tunit>(this, channelMap, whereFrom);
			}
			return channelIterator;
		}
	}

	/**
	 * Get an iterator which can iterator through only data units with data from
	 * particular sequence numbers in a databock. Note that if the sequenceMap ==
	 * null, this will simply default to the channelMap (but will still be classed
	 * as a SequenceIterator)
	 * <p>
	 * In viewer mode, sequence iterators are saved for reuse but will be deleted /
	 * cleared if data are removed or added to the datablock to avoid concurrency
	 * exceptions.
	 * 
	 * @param sequenceMap sequence map
	 * @param whereFrom   0, start at start,
	 * @return a sequence iterator
	 */
	public SequenceIterator<Tunit> getSequenceIterator(int sequenceMap, int whereFrom) {
		synchronized (synchronizationLock) {
			SequenceIterator<Tunit> sequenceIterator = null;
			if (isOffline) {
				sequenceIterator = (SequenceIterator<Tunit>) findIterator(false, sequenceMap);
				// System.out.println("Channel Iterator: " + channelIterator + " " +
				// storedIterators.size());
				if (sequenceIterator == null) {
					sequenceIterator = new SequenceIterator<Tunit>(this, sequenceMap, whereFrom);
					storedIterators.add(sequenceIterator);
				}
			} else {
				sequenceIterator = new SequenceIterator<Tunit>(this, sequenceMap, whereFrom);
			}
			return sequenceIterator;
		}
	}

	/**
	 * Channel iterators are stored in viewer mode (not worth doing this in normal
	 * operation since would get concurrently exceptions). This function is to find
	 * the one you want again for a particular channel/sequence map.
	 * 
	 * @param lookingAtChannels boolean indicating if we are looking for a channel
	 *                          iterator (true) or sequence iterator (false)
	 * @param chanOrSeqMap      channel/sequence map
	 * @return a valid iterator.
	 */
	private PamDataUnitIterator<Tunit> findIterator(boolean lookingAtChannels, int chanOrSeqMap) {
		synchronized (synchronizationLock) {
			for (PamDataUnitIterator<Tunit> anIterator : storedIterators) {
				if ((anIterator.getChanOrSeqMap() & chanOrSeqMap) != 0) {
					if (lookingAtChannels && (anIterator instanceof ChannelIterator)) {
						return anIterator;
					} else if (!lookingAtChannels && (anIterator instanceof SequenceIterator)) {
						return anIterator;
					}
				}
			}
			return null;
		}
	}

	/**
	 * Clears list of both channel and sequence iterators
	 */
	public void clearChannelIterators() {
		synchronized (synchronizationLock) {
			storedIterators.clear();
			// System.out.println("Cleared iterators in " + getDataName());
		}
	}

	/**
	 * Get the number of stored channel/sequence iterators for a datablock.
	 * 
	 * @return the number of channel & sequence iterators currently associated with
	 *         this data block.
	 */
	public int getChannelIteratorCount() {
		if (storedIterators == null)
			return 0;
		return storedIterators.size();
	}

	/**
	 * Flag to say that data should be cleared every time PAMGuard starts. Is true
	 * by default, but some modules may want to set this false for some streams.
	 * 
	 * @return the clearAtStart
	 */
	public boolean isClearAtStart() {
		return clearAtStart;
	}

	/**
	 * Flag to say that data should be cleared every time PAMGuard starts. Is true
	 * by default, but some modules may want to set this false for some streams.
	 * 
	 * @param clearAtStart the clearAtStart to set
	 */
	public void setClearAtStart(boolean clearAtStart) {
		this.clearAtStart = clearAtStart;
	}

	public void setDataSelectCreator(DataSelectorCreator dataSelectorCreator) {
		this.dataSelectorCreator = dataSelectorCreator;
	}
	/**
	 * 
	 * @return an object that can create data selectors to sub select data from
	 *         within this type of data block.
	 */
	public DataSelectorCreator getDataSelectCreator() {
		if (dataSelectorCreator == null) {
			dataSelectorCreator = new NullDataSelectorCreator(this);
		}
		return dataSelectorCreator;
	}

	/**
	 * Convenience method to save programmer from having to call into the creator
	 * all the time.
	 * 
	 * @param selectorName
	 * @param allowScores
	 * @return null or a DataSelector
	 */
	public DataSelector getDataSelector(String selectorName, boolean allowScores) {
		return getDataSelector(selectorName, allowScores, null);
	}

	/**
	 * Convenience method to save programmer from having to call into the creator
	 * all the time.
	 * 
	 * @param selectorName
	 * @param allowScores
	 * @param selectorType Type of selector, generally a ModuleType name, e.g. Map,
	 *                     so that options can be tailored to specific needs
	 * @return null or a DataSelector
	 */
	public DataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType) {
		DataSelector blockDataSelector = null;
		DataSelectorCreator dsc = getDataSelectCreator();
		if (dsc != null) {
			blockDataSelector = dsc.getDataSelector(selectorName, allowScores, selectorType);
		}
		return blockDataSelector;
	}

	/**
	 * Convenience method to save programmer from having to call into the creator
	 * all the time.
	 * 
	 * @param selectorName
	 * @param allowScores
	 * @param selectorType Type of selector, generally a ModuleType name, e.g. Map,
	 *                     so that options can be tailored to specific needs
	 * @param includeAnnotations include options from any annotators of this data stream
	 * @param includeSuperDetections include any possible super detection data selectors. 
	 * @return null or a DataSelector
	 */
	public DataSelector getDataSelector(String selectorName, boolean allowScores, String selectorType,
			boolean includeAnnotations, boolean includeSuperDetections) {
		DataSelector blockDataSelector = null;
		DataSelectorCreator dsc = getDataSelectCreator();
		if (dsc != null) {
			blockDataSelector = dsc.getDataSelector(selectorName, allowScores, selectorType, includeAnnotations, includeSuperDetections);
		}
		return blockDataSelector;
	}

	/**
	 * Get any information from the data block about cross referencing in database
	 * tables.
	 * <p>
	 * This gets used when importing databases, whereby Id's will change, so may
	 * need to be rewritten in related tables. Does not (currently) handle issues
	 * with UID's.
	 * 
	 * @return null or cross reference information.
	 */
	public CrossReference getCrossReferenceInformation() {
		return null;
	}

	/**
	 * @return the nanoTimeCalculator
	 */
	public NanoTimeCalculator getNanoTimeCalculator() {
		return nanoTimeCalculator;
	}

	/**
	 * @param nanoTimeCalculator the nanoTimeCalculator to set
	 */
	public void setNanoTimeCalculator(NanoTimeCalculator nanoTimeCalculator) {
		this.nanoTimeCalculator = nanoTimeCalculator;
	}

	/**
	 * @return the uidHandler
	 */
	public DataBlockUIDHandler getUidHandler() {
		return uidHandler;
	}

	/**
	 * Set the UID handler - very occasionally it's necessary to set this to null.
	 * 
	 * @param uidHandler
	 */
	public void setUidHandler(DataBlockUIDHandler uidHandler) {
		this.uidHandler = uidHandler;
	}

	/**
	 * @return the annotationHandler
	 */
	public AnnotationHandler getAnnotationHandler() {
		return annotationHandler;
	}

	/**
	 * Keep for backwards compatibility
	 * 
	 * @param annotationType
	 */
	public void addDataAnnotationType(DataAnnotationType annotationType) {
		if (annotationHandler == null) {
			annotationHandler = new AnnotationHandler((PamDataBlock<PamDataUnit>) this);
		}
		annotationHandler.addAnnotationType(annotationType);
	}

	/**
	 * @param annotationHandler the annotationHandler to set
	 */
	public void setAnnotationHandler(AnnotationHandler annotationHandler) {
		this.annotationHandler = annotationHandler;
	}


	/**
	 * 
	 * @return aSymbol Manager which manages all symbol shapes and colours for this
	 *         data block.
	 */
	public PamSymbolManager getPamSymbolManager() {
		return pamSymbolManager;
	}

	/**
	 * @param pamSymbolManager the pamSymbolManager to set
	 */
	public void setPamSymbolManager(PamSymbolManager pamSymbolManager) {
		this.pamSymbolManager = pamSymbolManager;
	}

	/**
	 * Check offline datablock UID's, taking the maximum uid from all available data
	 * maps.
	 */
	public void checkOfflineDataUIDs() {
		if (offlineDataMaps == null || uidHandler == null) {
			return;
		}
		long maxUID = 0;
		for (OfflineDataMap dataMap : offlineDataMaps) {
			Long hUID = dataMap.getHighestUID();
			if (hUID != null) {
				maxUID = Math.max(maxUID, hUID);
			}
		}
		uidHandler.setCurrentUID(maxUID);
	}

	/**
	 * As described in dataunit, this is a slightly different map to channelmap to
	 * be used when beamforming, where the map of available outputs may be quite
	 * different to the number of available input channels.
	 * <p>
	 * e.g. if a beam former had 4 input channels and made 6 beams then
	 * thechannelMap would be 0xF, but the sequenceMap would be 0x3F.
	 * <p>
	 * Always defaults to the channel map if it's not been set.
	 * 
	 * @return the sequenceMap
	 */
	public int getSequenceMap() {
		if (sequenceMap != null) {
			return sequenceMap;
		} else {
			return channelMap;
		}
	}

	/**
	 * As described in dataunit, this is a slightly different map to channelmap to
	 * be used when beamforming, where the map of available outputs may be quite
	 * different to the number of available input channels.
	 * <p>
	 * e.g. if a beam former had 4 input channels and made 6 beams then
	 * thechannelMap would be 0xF, but the sequenceMap would be 0x3F.
	 * 
	 * @param sequenceMap the sequenceMap to set
	 */
	public void setSequenceMap(Integer sequenceMap) {
		this.sequenceMap = sequenceMap;
	}

	/**
	 * As described in dataunit, this is a slightly different map to channelmap to
	 * be used when beamforming, where the map of available outputs may be quite
	 * different to the number of available input channels.
	 * <p>
	 * e.g. if a beam former had 4 input channels and made 6 beams then
	 * thechannelMap would be 0xF, but the sequenceMap would be 0x3F.
	 * <p>
	 * As opposed to the getSequenceMap method, this method will return the
	 * sequenceMap object even if it is null
	 * 
	 * @return
	 */
	public Integer getSequenceMapObject() {
		return sequenceMap;
	}

	/**
	 * This method will sort out this PamDataBlock's channel map and sequence map,
	 * depending on the source PamDataBlock that it's getting it's information from.
	 * It should typically be called when created, especially if the
	 * channel/sequence information is a subset of the source data block (such as
	 * when selected in a GroupSourcePanel object). <br>
	 * There are 3 passed parameters: the source channel map, the source sequence
	 * map, and the subset map that this data block should reference. The local
	 * subset map may be the same as the source channel/sequence map, or it may only
	 * be a few of the source's channels or sequences. <br>
	 * If the source data block has <em>no</em> sequence map (sourceSeqMap==null)
	 * then it's a normal FFT data block, and this PamDataBlock should be storing
	 * the local subset map in it's channelMap field and keeping it's sequenceMap
	 * field = null.<br>
	 * If the source data block <em>does</em> have a sequence map (i.e. the source
	 * is the output of a Beamformer), then this PamDataBlock should be storing the
	 * local subset map in it's sequenceMap field and storing the source channel map
	 * in it's channelMap field.
	 * 
	 * @param sourceChanMap
	 * @param sourceSeqMap
	 * @param localSubsetMap
	 */
	public void sortOutputMaps(int sourceChanMap, Integer sourceSeqMap, int localSubsetMap) {
		if (sourceSeqMap == null) {
			this.sequenceMap = null;
			this.channelMap = localSubsetMap;
		} else {
			this.sequenceMap = localSubsetMap;
			this.channelMap = sourceChanMap;
		}
	}

	/**
	 * Given a sequenceMap, this method returns the associated channelMap. By
	 * default, this simply returns passed parameter. For the majority of the
	 * PamDataBlocks this is correct because there won't be a sequenceMap. This
	 * method should be used when the calling function is given a channel but it
	 * does not know whether that is truly a channel, or actually a sequence number.
	 * It can pass the channel to this method; if the PamDataBlock doesn't have
	 * sequence numbers then the number really was a channel and the calling
	 * function will get the same number returned.
	 * <p>
	 * This method MUST BE OVERRIDDEN in any module that actually uses sequences
	 * (e.g. the Beamformer module) to properly map the sequenceMap to the
	 * channelMap.
	 * 
	 * @param sequenceMap
	 * @return the associated channelMap
	 */
	public int getChannelsForSequenceMap(int sequenceMap) {
		return sequenceMap;
	}

	/**
	 * There are times when a module doesn't know whether the channel it is using is
	 * really a channel, or whether it is actually a sequence number. In such a
	 * case, this method should be called on the source data block, and the
	 * channel/sequence number in question should be passed to it. If there is no
	 * sequenceMap, the variable passed into this method really is a channel and it
	 * is simply passed back. If there is a sequenceMap, however, then the value
	 * passed back will be the lowest channel in the channelMap.
	 * 
	 * @param chanOrSeqNum The channel or sequence number in question
	 * @return an actual channel number
	 */
	public int getARealChannel(int chanOrSeqNum) {
		if (sequenceMap == null) {
			return chanOrSeqNum;
		} else {
			return PamUtils.getLowestChannel(channelMap);
		}
	}

	/**
	 * @return A calculator for Time of Arrival Difference Calculations
	 * 
	 */
	public TOADCalculator getTOADCalculator() {
		return null;
	}

	/**
	 * @return the firstViewerUID
	 */
	public long getFirstViewerUID() {
		return firstViewerUID;
	}

	/**
	 * @return the lastViewerUID
	 */
	public long getLastViewerUID() {
		return lastViewerUID;
	}

	/**
	 * The synchronisation lock is a synchronisation object that must be used to
	 * synch all iterators using the data unit list and any function that puts data
	 * into or out of them. 99% of the time it is set to be a reference to the data
	 * block (this) but just occasionally it may be necessary to set it to something
	 * else. It must never be null.
	 * 
	 * @return the synchronisation lock
	 */
	public Object getSynchLock() {
		return synchronizationLock;
	}

	/**
	 * The synchronisation lock is a synchronisation object that must be used to
	 * synch all iterators using the data unit list and any function that puts data
	 * into or out of them. 99% of the time it is set to be a reference to the data
	 * block (this) but just occasionally it may be necessary to set it to something
	 * else. It must never be null.
	 * 
	 * @param synchronizationLock the synchronisation lock to set
	 */
	public void setSynchLock(Object synchLock) {
		this.synchronizationLock = synchLock;
	}

	/**
	 * @param currentViewDataStart the currentViewDataStart to set
	 */
	protected void setCurrentViewDataStart(long currentViewDataStart) {
		this.currentViewDataStart = currentViewDataStart;
	}

	/**
	 * @param currentViewDataEnd the currentViewDataEnd to set
	 */
	protected void setCurrentViewDataEnd(long currentViewDataEnd) {
		this.currentViewDataEnd = currentViewDataEnd;
	}

	/**
	 * @return the superDetectionClass
	 */
	public Class<?> getSuperDetectionClass() {
		return superDetectionClass;
	}

	//	/**
	//	 * @return the subDetectionClass
	//	 */
	//	public Class<?> getSubDetectionClass() {
	//		return subDetectionClass;
	//	}

	/**
	 * Make a soft copy of all of the data in the datablock. Note that this doesn't
	 * copy ANY of the data, just the references into a new list. This can be used
	 * when there is an operation running in a separate thread which is going to
	 * take a long time to get through all the data and can't handle the data list
	 * updating, but at the same time can't lock the list for the amount of time it
	 * may need to operator on the data.
	 * 
	 * @return Array list copy of all data units.
	 */
	public List<Tunit> copyDataList() {
		synchronized (this) {
			List<Tunit> listCopy = new ArrayList<Tunit>();
			listCopy.addAll(pamDataUnits);
			return listCopy;
		}
	}

	public BespokeDataMapGraphic getBespokeDataMapGraphic() {
		return null;
	}

	/**
	 * Get menu items pertinent to one or more data units.
	 * <p>
	 * Note that if multiple units have been selected on some of the generic
	 * displays, it's possible that they may not all be of the same types so may not
	 * all belong to this datablock.
	 * <p>
	 * If it's only a single unit, then its guaranteed that it will be from this
	 * datablock.
	 * 
	 * @param menuParent information about the display requesting these items.
	 * @param dataUnits  List of one or more selected data units.
	 * @return null or one or more menu items.
	 */
	public List<JMenuItem> getDataUnitMenuItems(DataMenuParent menuParent, Point mousePosition,
			PamDataUnit... dataUnits) {
		AnnotationHandler annHandler = getAnnotationHandler();
		if (annHandler != null) {
			return annHandler.getAnnotationMenuItems(menuParent, mousePosition, dataUnits);
		} else {
			return null;
		}
	}

	/**
	 * Get a standard popup menu (or null) using items from getDataUnitMenuItems
	 * @param menuParent
	 * @param mousePosition 
	 * @param dataUnits list of 1 - n data units
	 * @return popup menu or null. 
	 */
	public JPopupMenu getDataUnitPopupMenu(DataMenuParent menuParent, Point mousePosition,
			PamDataUnit... dataUnits) {
		List<JMenuItem> menuItems = getDataUnitMenuItems(menuParent, mousePosition, dataUnits);
		if (menuItems == null || menuItems.size() == 0) {
			return null;
		}
		JPopupMenu popMenu = new JPopupMenu();
		String lab;
		if (dataUnits != null && dataUnits.length == 1) {
			lab = String.format("        %s UID %s ...", getDataName(), dataUnits[0].getUID());
		}
		else {
			lab = String.format("        %s ...", getDataName());
		}
		JLabel label;
		popMenu.add(label = new JLabel(lab, JLabel.CENTER));
		label.setBorder(new EmptyBorder(3, 6, 0, 0));
		for (JMenuItem menuItem : menuItems) {
			popMenu.add(menuItem);
		}
		return popMenu;		
	}

	/**
	 * From a whole load of data units, e.g. as selected by a mark on a display, 
	 * get a list of the ones that belong just to this data block. 
	 * @param randomDataUnits data units which may be of any type
	 * @return list of input units that belong to this data block. 
	 */
	public List<Tunit> getMyDataUnits(List<PamDataUnit> randomDataUnits) {
		if (randomDataUnits == null) {
			return null;
		}
		List<Tunit> myUnits = new ArrayList<>();
		for (PamDataUnit dataUnit : randomDataUnits) {
			if (dataUnit.getParentDataBlock() == this) {
				myUnits.add((Tunit) dataUnit);
			}
		}
		return myUnits;
	}

	/**
	 * Get a list of unique super detections for a load of data units 
	 * @param pamDataUnits list of data units. 
	 * @return list of unique parents. 
	 */
	public List<PamDataUnit> getUniqueParentList(List<PamDataUnit> pamDataUnits) {
		List<PamDataUnit> uniqueSuperDets = new ArrayList<>();
		for (PamDataUnit dataUnit : pamDataUnits) {
			PamDataUnit superDet = dataUnit.getSuperDetection(0);
			if (superDet == null) {
				continue;
			}
			if (uniqueSuperDets.contains(superDet) == false) {
				uniqueSuperDets.add(superDet);
			}
		}
		return uniqueSuperDets;
	}

	/**
	 * @return the isOffline
	 */
	public boolean isOffline() {
		return isOffline;
	}

	/**
	 * @return the backgroundManager
	 */
	public BackgroundManager getBackgroundManager() {
		return backgroundManager;
	}

	/**
	 * @param backgroundManager the backgroundManager to set
	 */
	public void setBackgroundManager(BackgroundManager backgroundManager) {
		this.backgroundManager = backgroundManager;
	}

	/**
	 * Get a brief summary of datablock to include in XML descriptions. 
	 * Basic output is very simple. Expect other datablock to extend this by 
	 * adding additional attributes. 
	 * @param doc
	 * @return XML element with description of data. 
	 */
	public Element getDataBlockXML(Document doc) {
		Element inputEl = doc.createElement("Input");
		if (getParentProcess() != null && getParentProcess().getPamControlledUnit() != null) {
			PamControlledUnit pcu = getParentProcess().getPamControlledUnit();
			inputEl.setAttribute("ModuleType", pcu.getUnitType());
			inputEl.setAttribute("ModuleName", pcu.getUnitName());
		}
		inputEl.setAttribute("Name", getLongDataName());
		inputEl.setAttribute("Channels", String.format("0x%X", getChannelMap()));
		return inputEl;
	}
}
