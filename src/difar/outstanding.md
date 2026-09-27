# DIFAR: outstanding work

Brian Miller, Australian Antarctic Division. Branch `difar-crossings`, 27 September 2026.

Everything known to be unfinished, broken or awkward in the DIFAR module, so none of it depends on
memory. The design for crossings is in `crossings_design.md`.

## Crossings, remaining steps

The aim is reanalysis in the Viewer: new clips from raw audio, with time to correct mistakes. Old
datasets stay as they are, readable by old PAMGuard versions and pgmatlab.

Steps 1 to 5 are done: pure rules, crossing units and tables, Normal mode recording crossings as
clips are saved, clip payload version 3 with buoy edits recalculating crossings, and displays drawing
crossing units. A clip holds only the match proposed while it is worked on; once saved, its crossing
is the crossing unit. Still to do, in order:

1. Deleting a clip, in both modes, with the trim rule and a checkbox for
   `alwaysDeleteTrimmedCrossings`. Editing a clip is then delete and re-mark. Needs a short design
   note first, since binary files are append-only.
2. Later: deploying and calibrating a buoy in the Viewer (workarounds: import, manual calculation);
   bulk rematch into crossing units, or putting clips back on the queue.
3. Shelved: converting old datasets' crossings, and Viewer compaction.

The "DIFAR Data Export" offline task used to rematch every clip over a period. It now only updates
clip rows and relocates existing crossings, the same as after a buoy edit, until bulk rematch is
rebuilt on crossing units.

## Tests owed

- Step 5 passed in the Viewer on the simulated data: crossings load and draw, a buoy edit moves them,
  and a new clip claims its partner and deletes the crossing it left with one clip.
- Normal mode buoy edits are untested since step 4. Change a heading after a whale in one session:
  the crossing should keep its clips and move, and clip rows' `TrueBearing` should change.
- The pilot's 2013 files opening in the Viewer after step 4. Not needed for new reanalysis.
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

- A buoy edit in the Viewer loads the buoy's whole period to update clip rows and crossings. For a long
  deployment that may be slow. Ask first if it proves a nuisance.
- A crossing whose clips fall in two load chunks of that task is not recalculated. The console lists
  their UIDs. Rare, since a call spans seconds and chunks are files.

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

- R and MATLAB wrappers around `crossing_clips.sql`.
- pgmatlab: branch `difar-v3-no-tail` in `C:\analysis\pgmatlab` reads version 3 clips, tested on
  the pilot's version 2 files and a real version 3 file. Pushed to the fork. Open its pull request
  alongside the next DIFAR pull request to PAMGuard.

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
- The Viewer never loaded super-detection blocks alongside the blocks they group: a loop in
  `ViewerScrollerManager` queued the sub-detection block again instead of each super-detection
  block. Fixed on `fix-superdet-viewer-load` (one commit on upstream `ddbb6374`, pushed, pull request
  not yet opened) and cherry-picked into `difar-crossings`. DIFAR crossings do not load in the
  Viewer without it.
- An offline task group takes each task's on or off state from the dialog's saved selection, off by
  default, even when the group is run from code. `runCrossingTasks` switches its task on. The buoy-edit
  group also shares its settings name with the DIFAR offline tasks group, and registers settings
  each time it is made.
- Text columns are padded with spaces to their declared width on insert (`PamTableItem.getPackedValue`)
  but not on update, so one table holds both forms. `crossing_clips.sql` trims them.
- A unit moved between blocks keeps its first block as parent, since `addPamData` only sets a missing
  parent. DIFAR now sets it on save.
- The GPS import dialog's null mouse position is fixed on `fix-gps-import-dialog`. The pull request is
  open.
