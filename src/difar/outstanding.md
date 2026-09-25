# DIFAR: outstanding work

Brian Miller, Australian Antarctic Division. Branch `difar-crossings`, 25 September 2026.

Everything known to be unfinished, broken or awkward in the DIFAR module, so none of it depends on
memory. The design for crossings is in `crossings_design.md`.

## Crossings, remaining steps

Steps 1 to 3 are done: pure rules, crossing units and tables, and Normal mode recording crossings as
clips are saved. Still to do, in order:

1. Clip payload version 3, without the crossing tail. Read versions 0 to 2 into a legacy record. Buoy
   edits recalculate crossing units, and update the derived columns of clip rows.
2. Viewer: load crossings, reattach them to clips, convert old crossings in memory. Move displays to
   crossing units. Delete `DIFARCrossingInfo` and the temporary crossing on each clip.
3. An offline task that converts a whole dataset's old crossings, tried on a copy of the pilot.
4. Viewer compaction for clips, then deleting clips, with a checkbox for
   `alwaysDeleteTrimmedCrossings`.

## Tests owed

- Rung 7b: a deliberately wrong match, chosen in the match selector. The crossing should record
  `OPERATOR` and land away from the others.
- A three-buoy rung, where a third bearing extends a two-buoy crossing. Needs new audio.
- An overlap rung: rung 2 twice without a reset, to reproduce the overlapping deployments below.
- `pgmatlab` read one compacted file as 11 objects when it held 3. The file was discarded. Recheck
  once compaction resumes.

The rungs, the reset script and the expected results are in the simulated sonobuoy test folder.

## Sonobuoy deployments

These three probably share one solution: a sonobuoy origin for streamers, managed by DIFAR.

- **Overlapping deployments.** In Normal mode, previous deployments are loaded from the database at
  startup. When the audio comes from a file, the data clock restarts at the file's start time, so old
  deployments sit at the same data times as new ones. Lookups then pick whichever is latest before a
  clip, which may be the old buoy. Real reprocessing of voyage audio in Normal mode hits this too.
  Needs a guard: warn at Deploy, or at the start of a run, when later deployments exist on the channel.
- **A startup record at wall-clock time.** PAMGuard writes a streamer record when the configuration
  loads, stamped with the wall clock. In a file-based run that is after the data, and the deploy menu
  shows it as the buoy in force.
- **The Array Manager deploys every buoy.** Adding or editing a streamer calls
  `Streamer.makeStreamerDataUnit()` for every channel, so each channel gets a new deployment at the
  current time or at the configured position. Proposed fix: a sonobuoy origin method, registered by
  DIFAR through `HydrophoneOriginMethods.registerMethod`, that takes positions and headings from the
  buoy manager. Plus a small core hook so the dialog makes no record for streamers whose origin
  manages its own. Suggested before, never done.
- **Memory and database disagree.** After an Array Manager edit, the buoy manager showed deployment
  UID 4 with heading 45.0, while its database row held 119.5. Streamer records clone their streamer,
  so the cause is still unknown.
- **Physical buoy identity.** Moving a buoy to a new channel means finishing it, deploying again, and
  typing its position and variation by hand, with a decimal suffix on the name. A link from a new
  deployment to an earlier one would do this properly.

## Calibration

- The choice of mean, mode or mean near mode is only in a right-click menu on the histogram. Make it
  radio buttons in the dialog.

## Viewer

- Scrolling backwards then marking makes a clip that starts before the raw audio in memory. Fix: read
  the clip from the WAV files.
- The Saved strip does not show newly saved clips until a reload.
- The map draws bearings only once they scroll off the spectrogram's left edge. The map takes the
  scroller start as now. Core Map code.
- Queued clips are dropped when the window changes, because the queue clears.
- Sample numbers in Viewer-written headers count from 1970. Nothing in DIFAR reads them.
- The parked compactor (commit `ff1a2c86`) drops cross-match partners. Do not run it on real data
  until crossings are separate units.

## Tidying

- Detections from reanalysed WAV files should reference the file and the sample within it. Check what
  the WAV annotation already records.
- `canMark()` in DIFAR's spectrogram observer is dead code; its caller is commented out.
- `ClearCrossingTask` is unused. Delete it with `DIFARCrossingInfo`.
- Tracked groups are old and unused. Drop them, or replace them with a general tracking tool.
- The simulated sonobuoy test document lives in the data folder. Consider moving it into the repo.

## Core issues for Doug

- `PamController.updateDataMap()` throws without a binary store.
- The NMEA simulator keeps writing GPS records in Viewer.
- `GPSControl` heading interpolation uses the "before" heading twice.
- `PamFFTProcess` overwrites the saved FFT channel map when the source lacks channels.
- "Offline Sound Files are not enabled: false" is printed when files are enabled.
- `BinaryHeader.writeHeader()` declares a length 4 bytes short.
- `BinaryOutputStream` counts each stored object twice, once in each `storeData` overload. Footers
  report double the object count, and new units get an `indexInFile` of 0, 2, 4.
- `BinaryOutputStream.writeFooter` takes the lowest UID from the stream's start counter and the
  highest from the block's latest, not from the file's contents.
- `BinaryOutputStream.createIndexFile` needs a footer that only `writeFooter` sets.
- `ClipDisplayPanel` skips `updatePanel()` for new data in Viewer.
- `BINOVERLAPWARNING` is one named warning for every module.
- A unit moved between blocks keeps its first block as parent, since `addPamData` only sets a missing
  parent. DIFAR now sets it on save.
- The GPS import dialog's null mouse position is fixed on `fix-gps-import-dialog`. The pull request is
  not yet opened.
