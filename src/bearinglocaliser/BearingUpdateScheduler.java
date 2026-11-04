package bearinglocaliser;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import PamguardMVC.PamDataUnit;
import bearinglocaliser.annotation.BearingAnnotation;
import javafx.application.Platform;

/**
 * Works out bearings for data units which are created or changed interactively,
 * for example spectrogram annotation boxes which the user draws, drags or resizes.
 * <p>
 * Calculating a bearing can take a while - in viewer mode the raw audio under the
 * box may even have to be read from file - so it's never done on the JavaFX or
 * Swing threads where it would make dragging and resizing stutter. Requests are
 * instead put on a single background thread after a short delay. Further requests
 * for the same unit within that delay replace the earlier one, so a burst of
 * updates (e.g. the several annotation types which update a box once the mouse is
 * released) costs one calculation, not several.
 * <p>
 * The geometry of each unit (time, duration, frequency range and channels) is
 * remembered when its bearing is calculated, and an update which leaves that
 * geometry unchanged is ignored. That is what stops the update which the new
 * bearing annotation itself sends out from setting off another calculation, which
 * is why updates used to be ignored altogether.
 *
 * @author Jamie Macaulay
 */
public class BearingUpdateScheduler {

	/**
	 * Delay before a requested calculation runs, in milliseconds.
	 */
	public static final long DEBOUNCE_MILLIS = 150;

	private final BearingLocaliserControl bearingLocaliserControl;

	/**
	 * Geometry of each unit when its bearing was last calculated. Weakly keyed so
	 * units which are unloaded or deleted are not held on to. Data units use
	 * identity equality, which is what's wanted here.
	 */
	private final Map<PamDataUnit, String> lastGeometry = new WeakHashMap<>();

	/**
	 * Calculations which are waiting to run.
	 */
	private final Map<PamDataUnit, ScheduledFuture<?>> pending = new WeakHashMap<>();

	private ScheduledExecutorService executor;

	public BearingUpdateScheduler(BearingLocaliserControl bearingLocaliserControl) {
		this.bearingLocaliserControl = bearingLocaliserControl;
	}

	/**
	 * A new data unit has arrived. Off the GUI threads (normal real time processing)
	 * the bearing is calculated straight away, as it always has been. On a GUI thread
	 * (e.g. a box just drawn on a display) it's queued for the background thread.
	 *
	 * @param dataUnit the new data unit.
	 */
	public void newDataUnit(PamDataUnit dataUnit) {
		if (isGuiThread()) {
			schedule(dataUnit);
			return;
		}
		synchronized (lastGeometry) {
			lastGeometry.put(dataUnit, getGeometry(dataUnit));
		}
		bearingLocaliserControl.estimateBearings(dataUnit);
	}

	/**
	 * A data unit has been updated. The bearing is recalculated in the background
	 * if the unit's geometry has changed since its bearing was last calculated.
	 * <p>
	 * In real time a unit which never had a bearing calculated is left alone, since
	 * new units arrive through {@link #newDataUnit}. In viewer mode new and loaded
	 * units only ever arrive as updates, so those get a bearing too.
	 *
	 * @param dataUnit the updated data unit.
	 */
	public void updatedDataUnit(PamDataUnit dataUnit) {
		String geometry = getGeometry(dataUnit);
		String previous;
		synchronized (lastGeometry) {
			previous = lastGeometry.get(dataUnit);
		}
		if (previous == null && !bearingLocaliserControl.isViewer()) {
			return;
		}
		if (geometry.equals(previous)) {
			return;
		}
		schedule(dataUnit);
	}

	/**
	 * Queue a bearing calculation, replacing any which is already waiting for the
	 * same unit.
	 */
	private void schedule(PamDataUnit dataUnit) {
		synchronized (pending) {
			ScheduledFuture<?> existing = pending.get(dataUnit);
			if (existing != null) {
				existing.cancel(false);
			}
			pending.put(dataUnit, getExecutor().schedule(() -> calculate(dataUnit), DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS));
		}
	}

	/**
	 * Calculate the bearing on the background thread.
	 */
	private void calculate(PamDataUnit dataUnit) {
		synchronized (pending) {
			pending.remove(dataUnit);
		}
		/*
		 * Record the geometry before calculating, so the update sent out when the
		 * bearing annotation is added is recognised as nothing new, whilst a change the
		 * user makes during the calculation still gets its own calculation.
		 */
		synchronized (lastGeometry) {
			lastGeometry.put(dataUnit, getGeometry(dataUnit));
		}
		try {
			BearingProcess bearingProcess = bearingLocaliserControl.getBearingProcess();
			bearingProcess.checkSourceDataLoad(dataUnit);
			if (!bearingProcess.estimateBearings(dataUnit)) {
				removeBearing(dataUnit);
			}
		}
		catch (Exception e) {
			// one failed calculation mustn't stop the thread serving later ones.
			System.err.println("Bearing localiser: unable to update bearing: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Remove a bearing which no longer matches the unit because a new one couldn't be
	 * calculated, rather than leave a wrong one showing.
	 */
	private void removeBearing(PamDataUnit dataUnit) {
		BearingAnnotation bearingAnnotation = (BearingAnnotation) dataUnit.findDataAnnotation(BearingAnnotation.class);
		if (bearingAnnotation == null) {
			return;
		}
		dataUnit.removeDataAnnotation(bearingAnnotation);
		if (dataUnit.getLocalisation() == bearingAnnotation.getBearingLocalisation()) {
			dataUnit.setLocalisation(null);
		}
		if (dataUnit.getParentDataBlock() != null) {
			dataUnit.getParentDataBlock().updatePamData(dataUnit, System.currentTimeMillis());
		}
	}

	/**
	 * Stop the background thread. Anything still waiting is dropped.
	 */
	public synchronized void shutdown() {
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
	}

	private synchronized ScheduledExecutorService getExecutor() {
		if (executor == null) {
			executor = Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, bearingLocaliserControl.getUnitName() + " bearing updates");
				t.setDaemon(true);
				return t;
			});
		}
		return executor;
	}

	private static boolean isGuiThread() {
		if (SwingUtilities.isEventDispatchThread()) {
			return true;
		}
		try {
			return Platform.isFxApplicationThread();
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * The parts of a data unit which affect its bearing.
	 */
	private static String getGeometry(PamDataUnit dataUnit) {
		return dataUnit.getTimeMilliseconds() + "," + dataUnit.getDurationInMilliseconds() + ","
				+ Arrays.toString(dataUnit.getFrequency()) + "," + dataUnit.getChannelBitmap();
	}
}
