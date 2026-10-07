package difar.offline;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.ThreadedObserver;
import difar.DifarControl;
import difar.DifarDataUnit;
import difar.crossings.DifarCrossing;
import generalDatabase.DBControlUnit;

/**
 * The one path by which an interactive DIFAR edit in the viewer reaches
 * everything that must know of it: displays, the processes that consume
 * saved clips, the binary files and the database.
 * <p>
 * In the viewer, core's data blocks announce updates to their observers but
 * not additions or removals, since viewer data are normally only loaded. So
 * displays never heard of a clip marked, saved or deleted there, and each
 * display had to be caught up separately. Every edit now calls this class
 * instead:
 * <ul>
 * <li>{@link #added} tells a block's displays of a new clip, as the block
 * does in normal mode. Displays are the observers that are not processes:
 * processes observe blocks to process data, and should not re-process or
 * write rows because of an edit. Processes that consume clips, such as
 * tracked groups, are told explicitly, once registered with
 * {@link #addConsumer}.</li>
 * <li>{@link #changed} uses core's own update notice, which reaches every
 * observer.</li>
 * <li>{@link #removed} tells displays to redraw once a clip has gone.</li>
 * <li>{@link #commit} writes the saved clips' binary files and commits the
 * database, so files and rows match the screen straight away.</li>
 * </ul>
 * Calls are made on the Swing thread, where marks and saves happen, so
 * notices arrive in the order of the edits.
 */
public class ViewerEdits {

	private final DifarControl difarControl;

	/** Processes told of clips added to the saved clips. */
	private final List<PamObserver> consumers = new ArrayList<>();

	/**
	 * @param difarControl the DIFAR module.
	 */
	public ViewerEdits(DifarControl difarControl) {
		this.difarControl = difarControl;
	}

	/**
	 * Tell a process of each clip saved in the viewer, as it would hear in
	 * normal mode. For processes that consume localisations, such as tracked
	 * groups.
	 * @param consumer the process.
	 */
	public void addConsumer(PamObserver consumer) {
		if (consumer != null && !consumers.contains(consumer)) {
			consumers.add(consumer);
		}
	}

	/**
	 * A clip has joined a block.
	 * @param block the block.
	 * @param clip the clip.
	 */
	public void added(PamDataBlock block, PamDataUnit clip) {
		onSwing(() -> {
			for (PamObserver display : displays(block)) {
				display.addData(block, clip);
			}
			if (block == difarControl.getDifarProcess().getProcessedDifarData()) {
				for (PamObserver consumer : consumers) {
					consumer.addData(block, clip);
				}
			}
		});
	}

	/**
	 * A clip has changed: a new bearing, match or crossing.
	 * @param block the clip's block.
	 * @param clip the clip.
	 */
	@SuppressWarnings("unchecked")
	public void changed(PamDataBlock block, PamDataUnit clip) {
		onSwing(() -> block.updatePamData(clip, System.currentTimeMillis()));
	}

	/**
	 * Each clip of a crossing has changed.
	 * @param crossing the crossing, or null.
	 */
	public void crossingChanged(DifarCrossing crossing) {
		if (crossing == null) {
			return;
		}
		PamDataBlock block = difarControl.getDifarProcess().getProcessedDifarData();
		for (int i = 0; i < crossing.getSubDetectionsCount(); i++) {
			PamDataUnit clip = crossing.getSubDetection(i);
			if (clip instanceof DifarDataUnit) {
				changed(block, clip);
			}
		}
	}

	/**
	 * A clip has left a block. Displays are told to redraw without it.
	 * @param block the block.
	 * @param clip the clip.
	 */
	public void removed(PamDataBlock block, PamDataUnit clip) {
		onSwing(() -> {
			for (PamObserver display : displays(block)) {
				display.updateData(block, clip);
			}
		});
	}

	/**
	 * Write the saved clips' binary files and the crossings' rows, and commit
	 * the database, so both match what the displays show. Crossings live in
	 * the database only, and core writes their rows when their block is
	 * saved, which otherwise happens only at a reload or on closing.
	 */
	public void commit() {
		onSwing(() -> {
			difarControl.getDifarProcess().getProcessedDifarData().saveViewerData();
			difarControl.getDifarProcess().getCrossingDataBlock().saveViewerData();
			DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
			if (dbControl != null) {
				dbControl.commitChanges();
			}
		});
	}

	/**
	 * @param block a block.
	 * @return its observers that are not processes, unwrapped from any
	 * threading wrapper.
	 */
	private List<PamObserver> displays(PamDataBlock block) {
		List<PamObserver> displays = new ArrayList<>();
		for (int i = 0; i < block.countObservers(); i++) {
			PamObserver observer = block.getPamObserver(i);
			if (observer instanceof ThreadedObserver) {
				observer = ((ThreadedObserver) observer).getObserverObject();
			}
			if (observer == null || observer instanceof PamProcess || consumers.contains(observer)) {
				continue;
			}
			displays.add(observer);
		}
		return displays;
	}

	/**
	 * Run now if on the Swing thread, else as soon as it is free.
	 * @param job the job.
	 */
	private void onSwing(Runnable job) {
		if (SwingUtilities.isEventDispatchThread()) {
			job.run();
		}
		else {
			SwingUtilities.invokeLater(job);
		}
	}
}
