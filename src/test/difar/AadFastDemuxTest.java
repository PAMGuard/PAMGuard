package test.difar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import difar.demux.AmmcDemux;
import difar.demux.AadFastDemux;
import difar.demux.DifarResult;
import fftManager.FastFFT;

/**
 * Tests for the frequency-domain AAD demultiplexer, against known signals and against
 * AmmcDemux.
 * <p>
 * Clips are synthetic DIFAR multiplexes at 48 kHz, decimated by 192 to 250 Hz as for
 * the blue whale classifications: an omni signal of two tones (26.5 and 60.25 Hz), a
 * 7.5 kHz frequency pilot, a 15 kHz phase pilot locked to it, and the directional
 * channels (NS = omni cos(bearing), EW = omni sin(bearing)) multiplexed on the phase
 * pilot so that AmmcDemux returns them. No PAMGuard runtime is needed.
 */
public class AadFastDemuxTest {

	private static final double FS = 48000;
	private static final int D = 192;

	/** A synthetic clip, and the omni, NS and EW it carries. */
	private static class Clip {
		final double[] x, om, ns, ew;

		Clip(double seconds, double bearingDeg) {
			int n = (int) (FS * seconds);
			x = new double[n];
			om = new double[n];
			ns = new double[n];
			ew = new double[n];
			double th = Math.toRadians(bearingDeg);
			for (int i = 0; i < n; i++) {
				double t = i / FS;
				double o = Math.cos(2 * Math.PI * 26.5 * t) + 0.5 * Math.cos(2 * Math.PI * 60.25 * t + 1);
				double phi75 = 2 * Math.PI * 7500.2 * t;
				double phi = 2 * phi75 + 0.7;
				om[i] = o;
				ns[i] = o * Math.cos(th);
				ew[i] = o * Math.sin(th);
				x[i] = o + 0.3 * Math.cos(phi75) + 0.6 * Math.cos(phi) - ew[i] * Math.cos(phi) - ns[i] * Math.sin(phi);
			}
		}
	}

	/** Bearing from the omni-directional products over the middle 80% of the output. */
	private static double bearing(DifarResult r) {
		double[] om = r.getOm(), ew = r.getEw(), ns = r.getNs();
		int k = om.length, e = k / 10;
		double sew = 0, sns = 0;
		for (int j = e; j < k - e; j++) {
			sew += ew[j] * om[j];
			sns += ns[j] * om[j];
		}
		double b = Math.toDegrees(Math.atan2(sew, sns));
		return b < 0 ? b + 360 : b;
	}

	private static double angleDiff(double a, double b) {
		double d = Math.abs(a - b) % 360;
		return Math.min(d, 360 - d);
	}

	@Test
	void recoversOmniAndDirectionalChannels() {
		Clip c = new Clip(10, 250);
		DifarResult r = new AadFastDemux().processClip(c.x, FS, D, null, null);
		double scale = (double) FastFFT.nextBinaryExp(c.x.length) / c.x.length;
		int k = r.getOm().length, e = k / 10;
		assertEquals(c.x.length / D, k);
		double errOm = 0, errEw = 0, errNs = 0;
		for (int j = e; j < k - e; j++) {
			errOm = Math.max(errOm, Math.abs(r.getOm()[j] - c.om[j * D]));
			errEw = Math.max(errEw, Math.abs(r.getEw()[j] - scale * c.ew[j * D]));
			errNs = Math.max(errNs, Math.abs(r.getNs()[j] - scale * c.ns[j * D]));
		}
		// signals peak at 1.5; directional peaks at 1.5 * scale
		assertTrue(errOm < 0.01, "omni error " + errOm);
		assertTrue(errEw < 0.05 * 1.5 * scale, "EW error " + errEw);
		assertTrue(errNs < 0.05 * 1.5 * scale, "NS error " + errNs);
	}

	@Test
	void bearingsMatchAmmcDemux() {
		AmmcDemux old = new AmmcDemux();
		AadFastDemux fast = new AadFastDemux();
		for (double b : new double[] {0, 30, 135, 250, 310}) {
			Clip c = new Clip(7.3, b);
			double bOld = bearing(old.processClip(c.x, FS, D, null, null));
			double bFast = bearing(fast.processClip(c.x, FS, D, null, null));
			assertTrue(angleDiff(bFast, b) < 2, "fast " + bFast + " for true " + b);
			assertTrue(angleDiff(bFast, bOld) < 2, "fast " + bFast + " old " + bOld + " for true " + b);
		}
	}

	@Test
	void silentClipGivesZerosNotNaN() {
		DifarResult r = new AadFastDemux().processClip(new double[48000], FS, D, null, null);
		for (double v : r.getEw()) {
			assertEquals(0, v);
		}
	}

	@Test
	void fasterThanAmmcDemux() {
		Clip c = new Clip(41.7, 310);   // a long Koogu song clip
		AmmcDemux old = new AmmcDemux();
		AadFastDemux fast = new AadFastDemux();
		old.processClip(c.x, FS, D, null, null);   // warm up both
		fast.processClip(c.x, FS, D, null, null);
		long t0 = System.nanoTime();
		for (int i = 0; i < 3; i++) {
			old.processClip(c.x, FS, D, null, null);
		}
		long t1 = System.nanoTime();
		for (int i = 0; i < 3; i++) {
			fast.processClip(c.x, FS, D, null, null);
		}
		long t2 = System.nanoTime();
		double msOld = (t1 - t0) / 3e6, msFast = (t2 - t1) / 3e6;
		System.out.printf("41.7 s clip: AmmcDemux %.1f ms, AadFastDemux %.1f ms (%.1f times faster)%n",
				msOld, msFast, msOld / msFast);
		assertTrue(msFast < msOld, "fast " + msFast + " ms, old " + msOld + " ms");
	}
}
