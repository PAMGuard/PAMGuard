package difar.offline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Counts how clip UIDs are used across a dataset, so the upgrade can tell
 * whether they are unique.
 * <p>
 * A clip is known by its channel and start time. Two stored objects with the
 * same UID, channel and start time are one clip stored twice: an exact
 * duplicate. Two different clips with the same UID share it. Older datasets
 * can do both. In the 2019 voyage data the UID count restarted after each
 * crash.
 * <p>
 * A pure class with no PAMGuard dependencies, so it can be tested alone.
 */
public final class UidCensus {

	/** A clip, known by its channel and start time. */
	private static final class ClipKey {
		private final int channel;
		private final long timeMillis;

		private ClipKey(int channel, long timeMillis) {
			this.channel = channel;
			this.timeMillis = timeMillis;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ClipKey)) {
				return false;
			}
			ClipKey other = (ClipKey) o;
			return channel == other.channel && timeMillis == other.timeMillis;
		}

		@Override
		public int hashCode() {
			return Objects.hash(channel, timeMillis);
		}
	}

	/** Each UID, and the different clips carrying it. */
	private final Map<Long, Set<ClipKey>> clipsByUID = new HashMap<>();
	private int stored;
	private int exactDuplicates;

	/**
	 * Count one stored clip.
	 * @param uid the clip's UID.
	 * @param channelMap the clip's channel map.
	 * @param timeMillis the clip's start time.
	 */
	public void add(long uid, int channelMap, long timeMillis) {
		stored++;
		Set<ClipKey> clips = clipsByUID.computeIfAbsent(uid, k -> new HashSet<>());
		if (!clips.add(new ClipKey(channelMap, timeMillis))) {
			exactDuplicates++;
		}
	}

	/** @return every stored clip, including second copies. */
	public int getStored() {
		return stored;
	}

	/** @return stored clips that repeat another's UID, channel and start time. */
	public int getExactDuplicates() {
		return exactDuplicates;
	}

	/** @return different clips, with second copies counted once. */
	public int getClips() {
		return stored - exactDuplicates;
	}

	/** @return how many UIDs are in use. */
	public int getUIDs() {
		return clipsByUID.size();
	}

	/** @return UIDs carried by two or more different clips. */
	public int getSharedUIDs() {
		int n = 0;
		for (Set<ClipKey> clips : clipsByUID.values()) {
			if (clips.size() > 1) {
				n++;
			}
		}
		return n;
	}

	/** @return different clips whose UID another different clip also carries. */
	public int getClipsSharingUIDs() {
		int n = 0;
		for (Set<ClipKey> clips : clipsByUID.values()) {
			if (clips.size() > 1) {
				n += clips.size();
			}
		}
		return n;
	}

	/** @return true if no two different clips share a UID. */
	public boolean isUnique() {
		return getSharedUIDs() == 0;
	}

	/** @return a one line summary for the console. */
	public String summary() {
		return String.format("%d clips stored, %d of them second copies; %d UIDs, %d of them shared by %d different clips",
				stored, exactDuplicates, getUIDs(), getSharedUIDs(), getClipsSharingUIDs());
	}
}
