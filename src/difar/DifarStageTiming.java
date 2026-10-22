package difar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import PamUtils.PamCalendar;

/**
 * Times each stage of unattended DIFAR processing, one CSV row per clip.
 * <p>
 * Off unless PAMGuard is started with the Java option
 * {@code -Ddifar.stageTiming=<file.csv>}. Rows are appended, so several runs
 * can share one file; the run column tells them apart.
 * <p>
 * wallTime is the computer's clock (UTC) when the clip finished, so DIFAR's work can be
 * lined up with the run's progress. Stages, in milliseconds of wall time:
 * <ul>
 * <li>wait: waiting for the DIFAR clip lock (other triggers' clips ahead)</li>
 * <li>demux: demultiplexing and decimating the clip</li>
 * <li>gram: calculating the DIFARgram</li>
 * <li>match: matching with clips on other buoys and triangulating</li>
 * <li>amplitude: the clip's amplitude</li>
 * <li>save: saving the clip and its triangulation (only for saved clips)</li>
 * </ul>
 * Only clips processed with no operator are timed. All stages run inside the
 * clip lock, so one record at a time is in use.
 */
public class DifarStageTiming {

	public static final String PROPERTY = "difar.stageTiming";

	private static final String HEADER =
			"run,clipTime,channelMap,trigger,thread,outcome,wait,demux,gram,match,amplitude,save,total,wallTime";

	private final File file;

	private PrintWriter writer;

	/** Wall-clock start of the current run, set at each Start. */
	private String run = PamCalendar.formatDBDateTime(System.currentTimeMillis());

	/** Clip being timed, or null. */
	private DifarDataUnit clip;

	private long tStart, tLast;

	private long wait, demux, gram, match, amplitude, save;

	/**
	 * @return a timer, or null if timing is not switched on.
	 */
	public static DifarStageTiming create() {
		String path = System.getProperty(PROPERTY);
		if (path == null || path.isBlank()) {
			return null;
		}
		return new DifarStageTiming(new File(path));
	}

	private DifarStageTiming(File file) {
		this.file = file;
		System.out.println("DIFAR stage timing written to " + file.getAbsolutePath());
	}

	/**
	 * Start timing a clip, once its thread holds the clip lock.
	 * @param clip the clip
	 * @param lockRequested System.nanoTime() when its thread asked for the lock
	 */
	public void begin(DifarDataUnit clip, long lockRequested) {
		this.clip = clip;
		demux = gram = match = amplitude = save = 0;
		tStart = lockRequested;
		tLast = System.nanoTime();
		wait = tLast - tStart;
	}

	/** @return true while a clip is being timed. */
	public boolean isTiming(DifarDataUnit unit) {
		return clip != null && clip == unit;
	}

	private long lap() {
		long now = System.nanoTime();
		long dt = now - tLast;
		tLast = now;
		return dt;
	}

	public void endDemux() { demux = lap(); }
	public void endGram() { gram = lap(); }
	public void endMatch() { match = lap(); }
	public void endAmplitude() { amplitude = lap(); }

	/**
	 * Called after the clip is saved, or left in the queue. Writes the row.
	 * @param outcome saved, no bearing, or error
	 */
	public void end(String outcome) {
		if (clip == null) {
			return;
		}
		save = lap();
		long total = tLast - tStart;
		write(String.format("%s,%s,%d,%s,%s,%s,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%s",
				run,
				PamCalendar.formatDBDateTime(clip.getTimeMilliseconds(), true),
				clip.getChannelBitmap(),
				clip.triggerName == null ? "" : clip.triggerName.replace(',', ';'),
				Thread.currentThread().getName().replace(',', ';'),
				outcome,
				ms(wait), ms(demux), ms(gram), ms(match), ms(amplitude), ms(save), ms(total),
				PamCalendar.formatDBDateTime(System.currentTimeMillis(), true)));
		clip = null;
	}

	private static double ms(long nanos) {
		return nanos / 1e6;
	}

	private void write(String row) {
		try {
			if (writer == null) {
				boolean isNew = !file.exists() || file.length() == 0;
				writer = new PrintWriter(new FileWriter(file, true));
				if (isNew) {
					writer.println(HEADER);
				}
			}
			writer.println(row);
			writer.flush();
		}
		catch (IOException e) {
			System.out.println("DIFAR stage timing: cannot write " + file + ": " + e.getMessage());
		}
	}

	/** Mark the start of a run, so its rows can be told from earlier runs'. */
	public void startRun() {
		run = PamCalendar.formatDBDateTime(System.currentTimeMillis());
	}

	/** Close the file, at the end of a run. */
	public void close() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}
}
