# Time delays from cross-correlation

Design note for measuring the time delay between DIFAR clips of the same call by
cross-correlating their spectrograms, in place of the difference of clip start times.
Branch `difar-correlation`, from `difar-match-zoom`. October 2026.

## The problem

Matching locates each candidate group of DIFAR clips from bearings and time delays. The
time delays are the differences of the clip start times, with an error of
`detectionTimingError` (2 s by default) for each clip.

For a detector that places clips loosely, that is all the start times are worth. The deep
learning classifier used for the DCLDE 2024 right whale data has a 2 s window and a 1 s hop.
Its clip start times are whole seconds apart, so delays are known to about 1 s, or 1.5 km of
range. In the first v3 rematch of that dataset, the 11-sonobuoy triangulation at 17:42:55 had
a timing residual of 2.28 s, and dozens of other 10- and 11-sonobuoy groups fitted almost as
well. The bearings show there is a whale; the timing cannot say which DIFAR clips belong
together.

## What PAMGuard already has

- **The crossed bearing localiser** (`group3dlocaliser.algorithm.crossedbearing`) uses
  bearings only. Nothing to reuse for time delays.
- **The Group 3D time delay path** (`GenericTOADCalculator`, its FFT data organisers,
  `Localiser.algorithms.Correlations`) assumes one array on one recorder: channels aligned by
  sample number, maximum delays from hydrophone geometry, waveform correlation, and one best
  delay per pair. DIFAR clips are separate data units on multiplexed channels, with delays of
  up to tens of seconds, and we want several candidate delays.
- **`Correlations`** has what is worth reusing: `getInterpolatedMaxima()`, every local
  maximum with parabolic interpolation, and the correlation route used inside `getDelay()`:
  `FastFFT.rfft`, `ComplexArray.conjTimes`, `FastFFT.realInverse`.
- **`Correlations.getCorrelation()` is inaccurate.** Against a direct sum, its error was about
  14% of the peak height with zero padding, and worse without. The likely cause is how it
  fills the negative frequencies (`fftLength-i-1` rather than `fftLength-i`) and the packed DC
  and Nyquist terms. The `getDelay()` route agreed with the direct sum to 0.4%. To report to
  Doug; not used here.

## Decisions

1. **Spectrograms, not waveforms.** Multipath over kilometres changes a call's waveform, but
   its shape in the spectrogram survives. Each clip's spectrogram is cut to the overlap of the
   two clips' frequency limits, converted to log power, and normalised within each bin to zero
   mean and unit variance over time, so steady noise lines do not dominate.
2. **Seed-referenced delays.** Each candidate's arrival time is the seed's arrival time plus
   its correlation delay. Delays within a group are then consistent by construction, and a
   group of N sonobuoys needs N-1 correlations. The cost: a seed with poor signal-to-noise
   weakens the whole group.
3. **Each DIFAR clip's arrival time is stored with its triangulation**, in a new column of the
   Children table. A triangulation can then be worked out again, and exported, without
   correlating again.
4. **Candidate delays are local maxima** above a threshold, the highest K per pair.
   K defaults to 1. More than one multiplies the groups to localise; see "Several peaks".
5. **Fallback.** With no audio, or no peak above the threshold, a delay falls back to clip
   start times with the old timing error. `Chi2TimeDelays` takes an error for each delay, so
   correlated and uncorrelated delays can be mixed in one fit.

## ClipCorrelator (built)

`src/difar/ClipCorrelator.java`, tests in `src/test/difar/ClipCorrelatorTest.java`. Arrays in,
candidate delays out; no PAMGuard runtime state.

- Correlates bin by bin, sums over the band, and scales so identical spectrograms give 1.
- Searches only shifts within the largest possible delay between the sonobuoys, and where the
  clips overlap by at least a set fraction of the shorter clip.
- Smooths the correlation with a short triangular window, then drops a peak closer than a
  minimum separation to a higher one. In noise, the main peak otherwise splits into two or
  three maxima a few frames apart.
- Settings: threshold, most peaks, minimum overlap, smoothing half width, minimum separation.

Tests use synthetic upcalls (100 to 200 Hz over 1 s, 2 kHz sampling, FFT 256, hop 16 ms):
positive and negative delays, low signal-to-noise over ten noise samples, multipath giving a
second, lower peak, noise alone, the delay limit, a steady tone inside the band, a loud sound
outside it, split peaks, and empty input. Delays are within 3 frames (48 ms). The clip start
times they replace are good to about 1 s.

Peak heights are low by design, about 0.1 to 0.2 for a clear call in a 2.5 s clip, since most
of each clip's band is noise. The threshold has to be set from real data.

## Integration (built, first batch)

- **`ClipDelays`** keeps one spectrogram per clip, from the omni channel (index 0 of the
  demultiplexed audio: om, ew, ns, as `DifarResult.getDataArrays()` returns them), and one delay
  per pair, in caches of limited size. It uses the FFT length and hop of the species settings.
  Its methods are synchronized.
- **`DifarProcess.getCorrelatedArrivals()`** correlates the seed with every candidate that passed
  the existing time and frequency tests, after the cap on candidates per buoy. The delay limit is
  the travel time between the sonobuoys plus the max correlated timing residual.
- **`DIFARTargetMotionInformation.setArrivalTimes()`** uses those arrival times. A correlated
  arrival has an error of the correlation error over root two, so a delay between two of them
  has the correlation error. A detection without one keeps its detection time and error.
- **`DifarLocalisationResiduals`** reports the largest residual of correlated delays and of
  detection-time delays separately. `DifarMatchSelector` holds the first to the new max
  correlated timing residual and the second to the old max timing residual.
- **`CrossingLocaliser`** correlates again from the first clip of a triangulation when it works
  one out again, so edited triangulations keep their correlated delays. The first clip need not
  be the seed it was matched from, so its delays can differ a little.
- **Settings** in the Localisation panel of the Advanced tab: time delays by correlation (off by
  default), correlation threshold, correlation timing error, max correlated timing residual.
  Smoothing, minimum separation and minimum overlap are settings in `DifarParameters`, not shown.
- **The match selector** shows how many of each group's other clips were correlated, and says
  when a fitting group holds a clip already in another triangulation.
- **The rematch summary** reports pairs looked up, how many were correlated and how long that
  took, how many came from the cache, how many passed the threshold and their mean peak height,
  how many fell below it, and how many could not be correlated.
- **The best peak is kept whatever its height,** and the threshold applied afterwards, so the
  triangulation residual CSV can report the peak height of every candidate in a triangulation,
  with whether it passed, fell below the threshold, or could not be correlated, and which clip was
  the seed. A new threshold then needs no new correlation.
- **Help pages:** the triangulation page and the localisation settings.

Not yet built:

- **Each clip's arrival time in the Children table.** Triangulations are worked out again by
  correlating again, which needs the clips' audio in memory.
- **The demultiplexed audio is taken to start at the clip's time.** True for AMMC. The
  Greeneridge demultiplexer trims audio where it lost lock and does not record the trim.
- **Clearing the caches** when clips are reloaded or demultiplexed again. Keys include each
  clip's UID and time, so a changed clip with the same UID and time would use a stale spectrogram.

## Timing errors and limits

The defaults of 0.05 s for the correlation timing error and 0.5 s for the max correlated timing
residual were placeholders. Drift since deployment can bias a delay by seconds while the
correlation peak stays clean, so a correlated delay's error is not just its measurement error. The
plan, in `outstanding.md` under "Timing errors and sonobuoy positions": first log every
triangulation's residuals (built: a CSV beside the database after each rematch), then give each
delay an error that includes sonobuoy position and drift, and set limits in units of that error.

## Several peaks

With K above 1, one DIFAR clip can arrive at several times. The match selector would then work
on arrivals (a DIFAR clip and an arrival time) rather than DIFAR clips, with at most one
arrival per sonobuoy in a group. With 11 sonobuoys and 3 peaks each, the groups to localise
grow by up to 3^10. This waits until K = 1 has been measured on real data, and needs a cap on
the groups localised for each seed.

## Threads and cost

Where matching runs now:

- **Normal mode, automatic:** in the demultiplexing worker's background thread
  (`DifarDemuxWorker.doInBackground`), not on the Swing event thread.
- **Clicking the DIFARGram:** `setAngleAndFrequency()` matches again on the Swing event thread.
- **Viewer, rematch and upgrade:** on the offline task thread.

Correlation does not depend on the bearing chosen in the DIFARGram, only on the two clips'
audio and frequency limits. So delays are worked out once per pair and cached. A click on the
DIFARGram then localises again with the cached delays, as fast as now, and never correlates on
the Swing event thread.

Measured cost: about 1.1 ms per pair for two 2.5 s clips (149 frames by 40 bins), not counting
the spectrograms. A seed with 50 candidate pairs takes about 55 ms. Over the 456 DIFAR clips of
the DCLDE day this adds roughly as much as the whole rematch now takes (22 s). Within one seed
the pairs are independent and could run in parallel; seeds cannot, because the rematch is order
dependent. The first version runs them in sequence and reports the time spent correlating in
the console summary, so any parallel work is chosen from measurements.

Localising the groups is likely to cost more than correlating them, especially with K above 1.

## Where a GPU would be worth it

Not for clip correlation at this scale: each pair is a few dozen small FFTs. A GPU pays when
the same large operation runs over many channels or a large grid:

- correlating continuous streams across every pair of sonobuoys, rather than detected clips:
  the localise-first approach discussed in the DCLDE 2024 writeup;
- grid searches of location likelihood over many groups;
- azigrams or beamforming of all channels of a 32-sonobuoy array.

`ClipCorrelator` takes and returns arrays, so a batched implementation could replace it
without changing its callers.

## Tests owed

- JUnit for the integration: arrival times feeding `calculateTimeDelays()`, the fallback, and
  mixed errors.
- The simulated two-sonobuoy fixture: delays match the simulation.
- DCLDE 2024, run 2 against run 1: the timing residual of the 11-sonobuoy triangulation falls
  from 2.28 s to under about 0.1 s, and fewer competing groups fit almost as well.
