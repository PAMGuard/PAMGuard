package difar.crossings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The crossing a clip carried in DIFAR files up to module version 2, as it
 * was stored: a location, two errors, and each partner clip's channel and
 * time.
 * <p>
 * It is inert. Reading it looks nothing up and changes no other clip.
 * Turning these records into {@link DifarCrossing} units is a separate step,
 * done by replaying them with {@link CrossingSets#replay}.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 */
public final class LegacyCrossing {

	/** A partner clip, named as old files name it. */
	public static final class Partner {

		private final int channel;
		private final long timeMillis;

		public Partner(int channel, long timeMillis) {
			this.channel = channel;
			this.timeMillis = timeMillis;
		}

		/** @return the partner's channel. */
		public int getChannel() {
			return channel;
		}

		/** @return the partner's time in milliseconds. */
		public long getTimeMillis() {
			return timeMillis;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Partner)) {
				return false;
			}
			Partner p = (Partner) o;
			return channel == p.channel && timeMillis == p.timeMillis;
		}

		@Override
		public int hashCode() {
			return 31 * channel + Long.hashCode(timeMillis);
		}

		@Override
		public String toString() {
			return "ch " + channel + " at " + timeMillis;
		}
	}

	private final double latitude;
	private final double longitude;
	private final double xError;
	private final double yError;
	private final List<Partner> partners;

	/**
	 * @param latitude cross latitude.
	 * @param longitude cross longitude.
	 * @param xError x error in metres, or NaN where the file stored none.
	 * @param yError y error in metres, or NaN where the file stored none.
	 * @param partners the other clips in the crossing, not the clip holding it.
	 */
	public LegacyCrossing(double latitude, double longitude, double xError, double yError, List<Partner> partners) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.xError = xError;
		this.yError = yError;
		this.partners = Collections.unmodifiableList(new ArrayList<>(partners));
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	/** @return x error in metres, or NaN where the file stored none. */
	public double getXError() {
		return xError;
	}

	/** @return y error in metres, or NaN where the file stored none. */
	public double getYError() {
		return yError;
	}

	/** @return the other clips in the crossing, not the clip holding it. */
	public List<Partner> getPartners() {
		return partners;
	}

	/** @return the number of clips in the crossing, the holder included. */
	public int getClipCount() {
		return partners.size() + 1;
	}
}
