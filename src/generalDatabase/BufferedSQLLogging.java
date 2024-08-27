package generalDatabase;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Version of SQLLogging which buffers up the data units for a few seconds before writing them
 * so that other parts of PAMguard get a chance to modify them before they go into the 
 * database. If a unit is updated AFTER it's been written, it will still get updated in the 
 * normal way  
 * <p>This is causing a few issues with the need to subclass 
 * @author dg50
 *
 */
abstract public class BufferedSQLLogging extends SQLLogging {

	private int bufferSeconds;
	
	private List<BufferedDataUnit> unitsList;
	
	private Timer writeTimer;

	private PamConnection currentCon;
	
	private Object bufferSynch;// = new Object();

	public BufferedSQLLogging(PamDataBlock pamDataBlock, int bufferSeconds) {
		super(pamDataBlock);
		unitsList = Collections.synchronizedList(new LinkedList<BufferedDataUnit>());
//		unitsList = new Hashtable<>();
		this.setBufferSeconds(bufferSeconds);
		writeTimer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				writeTimerAction();
			}
		});
		writeTimer.start();
		bufferSynch = this; // get thread lock up if it's a different class with one with waiting on htis and another on buffersynch
	}

	private void writeTimerAction() {
		checkWriteList(PamCalendar.getTimeInMillis());
	}
	
	/**
	 * Flush the buffer, writing everything to the database. This will have to be
	 * called explicitly by anything using buffered logging to ensure all data
	 * are written, otherwise data may still be in the buffer when the database closes. 
	 */
	public void flushBuffer() {
		checkWriteList(Long.MAX_VALUE - 10000*bufferSeconds);
	}

	/**
	 * Write data units in the list up to the given time - the buffer time. 
	 * @param timeInMillis
	 */
	private void checkWriteList(long timeInMillis) {
		synchronized (bufferSynch) {
			long firstMillis = timeInMillis - 1000*bufferSeconds;
			Iterator<BufferedDataUnit> iter = unitsList.iterator();
			while (iter.hasNext()) {
				BufferedDataUnit bufferedData = iter.next();
				if (bufferedData.dataUnit.getTimeMilliseconds() > firstMillis) {
					break;
				}
				/*
				 * This is getting complicated ... in the interim, while this has sat in it's
				 * queue, it's been logged as a sub detection by something else, so 
				 * needs updating instead. 
				 */
				if (bufferedData.dataUnit.getDatabaseIndex() > 0) {
					super.reLogData(currentCon, bufferedData.dataUnit, bufferedData.superDetection);
				}
				else {
					super.logData(currentCon, bufferedData.dataUnit, bufferedData.superDetection);
				}
				iter.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#logData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
	 */
	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		/*
		 * Don't log the data, just place it into the buffer. 
		 */
		currentCon = con; // can just store this !
		synchronized (bufferSynch) {
			BufferedDataUnit existing = inList(dataUnit, superDetection);
			if (existing == null) {
//			if (unitsList.contains(dataUnit) == false) {
				unitsList.add(new BufferedDataUnit(dataUnit, superDetection)); // just stick it in the list. 
			}
			else {
				// consider updating.
				if (superDetection != null) {
					existing.superDetection = superDetection;
				}
			}
		}
		return true;
	}

	private BufferedDataUnit inList(PamDataUnit dataUnit, PamDataUnit superDetection) {
		Iterator<BufferedDataUnit> iter = unitsList.iterator();
		while (iter.hasNext()) {
			BufferedDataUnit bufferedData = iter.next();
			if (bufferedData.dataUnit == dataUnit) {
				return bufferedData;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#reLogData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
	 */
	@Override
	public synchronized boolean reLogData(PamConnection con, PamDataUnit dataUnit) {
		/*
		 * If the data unit has not yet actually been saved, then there is no need to 
		 * do anything, otherwise, update it in the normal way.
		 */
		synchronized (bufferSynch) {
			if (dataUnit.getDatabaseIndex() <= 0) {
				return true;
			}
			else {
				return super.reLogData(con, dataUnit);
			}
		}
	}

	/**
	 * @return the bufferSeconds
	 */
	public int getBufferSeconds() {
		return bufferSeconds;
	}

	/**
	 * @param bufferSeconds the bufferSeconds to set
	 */
	public void setBufferSeconds(int bufferSeconds) {
		this.bufferSeconds = bufferSeconds;
	}
	
	private class BufferedDataUnit {
		public PamDataUnit dataUnit, superDetection;

		/**
		 * @param dataUnit
		 * @param superDetection
		 */
		public BufferedDataUnit(PamDataUnit dataUnit, PamDataUnit superDetection) {
			super();
			this.dataUnit = dataUnit;
			this.superDetection = superDetection;
		}

	}

}
