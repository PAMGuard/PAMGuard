package difar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import difar.crossings.LegacyCrossing;

/**
 * The module-specific part of a DIFAR clip's binary object, and how it is
 * written and read.
 * <p>
 * Versions:
 * <ol start="0">
 * <li>The first format. The crossing tail has no errors.</li>
 * <li>Adds the tracked group, and x and y errors to the crossing tail.</li>
 * <li>Start sample, channel map and frequency range move to PAMGuard's
 * standard data unit header, so they leave this payload.</li>
 * <li>No crossing tail. Crossings are stored in the database only, as
 * {@link difar.crossings.DifarCrossing} units.</li>
 * </ol>
 * Only the current version is written. Older versions are read, and their
 * crossing tail becomes an inert {@link LegacyCrossing}. Reading looks
 * nothing up and changes no other clip.
 * <p>
 * A pure class with no PAMGuard runtime dependencies, so it can be tested alone.
 */
public class DifarClipPayload {

	/** The version written. */
	public static final int CURRENT_VERSION = 3;

	/** Written in place of a missing DIFAR gain. Read back as it is. */
	public static final float NO_GAIN = -9999.f;

	/** Up to this version, clips carry a crossing tail. */
	private static final int LAST_VERSION_WITH_TAIL = 2;

	/** The three demultiplexed DIFAR channels. */
	private static final int DEMUX_CHANNELS = 3;

	public long clipStartMillis;
	public float displaySampleRate;
	/** Number of samples in each demultiplexed channel, even if the audio is not kept. */
	public int demuxedLength;
	/** Demultiplexed audio, [3][demuxedLength], or null if none was stored. */
	public double[][] demuxData;
	public float amplitude;
	public float gain = NO_GAIN;
	public float selectedAngle;
	public float selectedFrequency;
	public String speciesCode;
	/** Null in version 0 files, which have no tracked group. */
	public String trackedGroup;

	/** Read from versions 0 and 1 only; later versions keep it in the data unit header. */
	public Long startSample;
	/** Read from versions 0 and 1 only; later versions keep it in the data unit header. */
	public Integer channelMap;
	/** Read from versions 0 and 1 only; later versions keep it in the data unit header. */
	public double[] frequencyRange;

	/** The crossing stored with the clip, read from versions 0 to 2 only. */
	public LegacyCrossing legacyCrossing;

	/**
	 * Write the payload at the current version.
	 * @param dos where to write.
	 * @throws IOException if writing fails.
	 */
	public void write(DataOutputStream dos) throws IOException {
		dos.writeLong(clipStartMillis);
		dos.writeFloat(displaySampleRate);
		int length = demuxData == null ? 0 : demuxData[0].length;
		dos.writeInt(length);
		dos.writeFloat(amplitude);
		dos.writeFloat(gain);
		dos.writeFloat(selectedAngle);
		dos.writeFloat(selectedFrequency);
		dos.writeUTF(speciesCode);
		dos.writeUTF(trackedGroup);
		if (demuxData == null) {
			dos.writeFloat(0);
			return;
		}
		double maxVal = 0;
		for (double[] channel : demuxData) {
			for (double v : channel) {
				maxVal = Math.max(maxVal, Math.abs(v));
			}
		}
		dos.writeFloat((float) maxVal);
		for (double[] channel : demuxData) {
			for (double v : channel) {
				dos.writeShort((int) (v * 32767 / maxVal));
			}
		}
	}

	/**
	 * Read a payload of any version.
	 * @param dis where to read from.
	 * @param version the module version of the file.
	 * @return the payload.
	 * @throws IOException if the data are short or unreadable.
	 */
	public static DifarClipPayload read(DataInputStream dis, int version) throws IOException {
		DifarClipPayload p = new DifarClipPayload();
		if (version < 2) {
			p.startSample = dis.readLong();
		}
		p.clipStartMillis = dis.readLong();
		if (version < 2) {
			p.channelMap = dis.readInt();
		}
		p.displaySampleRate = dis.readFloat();
		p.demuxedLength = dis.readInt();
		if (version < 2) {
			p.frequencyRange = new double[] {dis.readFloat(), dis.readFloat()};
		}
		p.amplitude = dis.readFloat();
		p.gain = dis.readFloat();
		p.selectedAngle = dis.readFloat();
		p.selectedFrequency = dis.readFloat();
		p.speciesCode = dis.readUTF();
		if (version >= 1) {
			p.trackedGroup = dis.readUTF();
		}
		double maxVal = dis.readFloat();
		if (p.demuxedLength > 0) {
			p.demuxData = new double[DEMUX_CHANNELS][p.demuxedLength];
			for (int i = 0; i < DEMUX_CHANNELS; i++) {
				for (int j = 0; j < p.demuxedLength; j++) {
					p.demuxData[i][j] = dis.readShort() * maxVal / 32767;
				}
			}
		}
		if (version <= LAST_VERSION_WITH_TAIL) {
			p.legacyCrossing = readTail(dis, version);
		}
		return p;
	}

	/**
	 * Read the crossing tail of an old clip.
	 * @return the crossing, or null if the clip had none.
	 */
	private static LegacyCrossing readTail(DataInputStream dis, int version) throws IOException {
		int nClips = dis.readShort();
		if (nClips <= 0) {
			return null;
		}
		double latitude = dis.readFloat();
		double longitude = dis.readFloat();
		double xError = Double.NaN, yError = Double.NaN;
		if (version >= 1) {
			xError = dis.readFloat();
			yError = dis.readFloat();
		}
		List<LegacyCrossing.Partner> partners = new ArrayList<>();
		for (int i = 0; i < nClips - 1; i++) {
			int channel = dis.readShort();
			long time = dis.readLong();
			partners.add(new LegacyCrossing.Partner(channel, time));
		}
		return new LegacyCrossing(latitude, longitude, xError, yError, partners);
	}
}
