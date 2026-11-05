package test.PamguardMVC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataOffline.OfflineDataLoading;

/**
 * Offline data orders from different observers on the same data block must not cancel
 * each other, while a new order from the same observer still interrupts its last one.
 */
public class OfflineDataLoadingTest {

	private static final long LOAD_MILLIS = 400;

	/**
	 * Stands in for an upstream source (e.g. acquisition reading files). Each load takes
	 * LOAD_MILLIS unless cancelled, and records how many loads overlap.
	 */
	static class SlowSource extends PamProcess {

		final AtomicInteger running = new AtomicInteger();
		final AtomicInteger maxRunning = new AtomicInteger();

		SlowSource() {
			super((PamControlledUnit) null, (PamDataBlock) null, "Slow source");
		}

		@Override
		public int getOfflineData(OfflineDataLoadInfo info) {
			int n = running.incrementAndGet();
			maxRunning.accumulateAndGet(n, Math::max);
			try {
				long end = System.currentTimeMillis() + LOAD_MILLIS;
				while (System.currentTimeMillis() < end) {
					if (info.cancel) {
						return OfflineDataLoading.REQUEST_INTERRUPTED;
					}
					Thread.sleep(5);
				}
				return OfflineDataLoading.REQUEST_DATA_LOADED;
			}
			catch (InterruptedException e) {
				return OfflineDataLoading.REQUEST_INTERRUPTED;
			}
			finally {
				running.decrementAndGet();
			}
		}

		@Override
		public void pamStart() {
		}

		@Override
		public void pamStop() {
		}
	}

	static class Display extends PamObserverAdapter {
		final String name;

		Display(String name) {
			this.name = name;
		}

		@Override
		public String getObserverName() {
			// deliberately the same for every display, as SpectrogramDisplay does.
			return "Spectrogam Display";
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
		}
	}

	private PamDataBlock<PamDataUnit> makeBlock(SlowSource source) {
		return new PamDataBlock<PamDataUnit>(PamDataUnit.class, "Test FFT", source, 1, false);
	}

	private CompletableFuture<Integer> order(PamDataBlock<PamDataUnit> block, Display observer, long start, int interrupt) {
		CompletableFuture<Integer> result = new CompletableFuture<Integer>();
		block.orderOfflineData(observer, status -> result.complete(status),
				start, start + 1000, 1, interrupt, true);
		return result;
	}

	private int await(CompletableFuture<Integer> f) throws Exception {
		return f.get(10, TimeUnit.SECONDS);
	}

	/** Four displays on one block all get their data. */
	@Test
	public void differentObserversDoNotCancelEachOther() throws Exception {
		SlowSource source = new SlowSource();
		PamDataBlock<PamDataUnit> block = makeBlock(source);
		CompletableFuture<?>[] f = new CompletableFuture<?>[4];
		for (int i = 0; i < 4; i++) {
			f[i] = order(block, new Display("d" + i), 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		}
		for (int i = 0; i < 4; i++) {
			assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await((CompletableFuture<Integer>) f[i]),
					"display " + i + " should have loaded");
		}
		// they meet at the source one at a time
		assertEquals(1, source.maxRunning.get());
	}

	/** The same display scrolling again interrupts its own previous load. */
	@Test
	public void sameObserverInterruptsItself() throws Exception {
		SlowSource source = new SlowSource();
		PamDataBlock<PamDataUnit> block = makeBlock(source);
		Display d = new Display("d");
		CompletableFuture<Integer> first = order(block, d, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		Thread.sleep(50);
		CompletableFuture<Integer> second = order(block, d, 5000, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		assertEquals(OfflineDataLoading.REQUEST_INTERRUPTED, await(first));
		assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await(second));
	}

	/** Cancelling one display's order leaves the others running. */
	@Test
	public void cancelByOwnerOnlyCancelsThatOwner() throws Exception {
		SlowSource source = new SlowSource();
		PamDataBlock<PamDataUnit> block = makeBlock(source);
		Display a = new Display("a");
		Display b = new Display("b");
		CompletableFuture<Integer> fa = order(block, a, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		CompletableFuture<Integer> fb = order(block, b, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		Thread.sleep(50);
		block.cancelDataOrder(a);
		assertEquals(OfflineDataLoading.REQUEST_INTERRUPTED, await(fa));
		assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await(fb));
	}

	/** OFFLINE_DATA_CANCEL only gives up if the same owner is still loading. */
	@Test
	public void cancelModeOnlyChecksOwnOrder() throws Exception {
		SlowSource source = new SlowSource();
		PamDataBlock<PamDataUnit> block = makeBlock(source);
		Display display = new Display("display");
		Display playback = new Display("playback");
		CompletableFuture<Integer> fd = order(block, display, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		CompletableFuture<Integer> fp = order(block, playback, 0, OfflineDataLoading.OFFLINE_DATA_CANCEL);
		CompletableFuture<Integer> fp2 = order(block, playback, 0, OfflineDataLoading.OFFLINE_DATA_CANCEL);
		assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await(fd));
		assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await(fp));
		Thread.sleep(200);
		assertFalse(fp2.isDone(), "second playback order should have been dropped");
		assertTrue(block.getOfflineDataLoading().isIdle());
	}

	/** An order which waits for its owner's previous order is run afterwards. */
	@Test
	public void waitModeRunsAfterOwnersOrder() throws Exception {
		SlowSource source = new SlowSource();
		PamDataBlock<PamDataUnit> block = makeBlock(source);
		Display d = new Display("d");
		CompletableFuture<Integer> first = order(block, d, 0, OfflineDataLoading.OFFLINE_DATA_WAIT);
		CompletableFuture<Integer> second = order(block, d, 5000, OfflineDataLoading.OFFLINE_DATA_WAIT);
		assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await(first));
		assertEquals(OfflineDataLoading.REQUEST_DATA_LOADED, await(second));
	}
}
