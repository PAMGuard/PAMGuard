# DIFAR: outstanding work

Brian Miller, Australian Antarctic Division. Branch `difar-crossings`, 27 September 2026.

A list of everything known to be unfinished, broken or awkward in the DIFAR module, so none of it depends on remembering. The design for crossings is in `crossings_design.md`.

## Parked, 28 September 2026

Parked to finish other projects. Rebased onto upstream `main` at `e07bdfef`, after Doug merged the
DIFAR refresh (#345), the super-detection loading fix (#348) and the small core fixes (#340, #343,
#344, #346). The branch holds 22 commits after `explore-difar`.

Works now: reanalysis in the Viewer. Marking, saving, deleting and rematching clips, crossings drawn
from their crossing units, and upgrading old datasets to version 3.

Not ready to merge: Normal mode is untested since step 4, clip files change to version 3, and the
pgmatlab reader for version 3 waits on its own pull request. A draft pull request records the status.

To return: read this file, then `viewer_editing_design.md`. For a Viewer test on the simulated data,
reset, then copy rung 8's database and the contents of its `PAMBinary` folder
(`cp -R runs/rung8/PAMBinary/. config/PAMBinary/`). Copying the folder itself nests it.

## Returned, 29 September 2026

Normal mode tests wait. First, compare PAMGuard's rematch of the 2019 voyage data with the MATLAB
rematch used for the 2021 source level paper (`DIFAR_Localisation_rematched_v3.csv`, in
`S:\manuscripts\2019-enrichVoyageReport`). The 2019 files repeat UIDs, so the upgrade must handle
that first: see "Duplicate UIDs in older datasets" in `viewer_editing_design.md`. After that, a gap
analysis for reanalysing the 2013 pilot with an automatic detector and the video tracks.

## Crossings, remaining steps

The aim is reanalysis in the Viewer: new clips from raw audio, with time to correct mistakes. Old
datasets are read only until upgraded, and stay readable by old PAMGuard versions and pgmatlab.

Steps 1 to 5 are done: pure rules, crossing units and tables, Normal mode recording crossings as
clips are saved, clip payload version 3 with buoy edits recalculating crossings, and displays drawing
crossing units. A clip holds only a temporary crossing while it is worked on; once saved, its crossing
is the crossing unit. "Rematch clips" matches clips again over a period, from the DIFAR offline tasks
and after every buoy edit in the Viewer.

Next, in order, as set out in `viewer_editing_design.md`:

0. Duplicate UIDs in older datasets: the upgrade checks, offers to renumber or cancel, drops exact
   duplicates, and rebuilds the clip table from the binary files, with a database backup. Passed on
   a small slice; the full voyage is next.
1. The queue strip in the Viewer: marked clips do not appear in it. Rebuilding it from the queue's
   block was tried and removed, because a clip being worked stays in that block and display settings
   arrive after marking. Send it the add and update notices Normal mode sends, for clips made in the
   Viewer, without reaching observers that write rows. Check which observers each block has first.
2. An auto-compaction setting, writing after every edit. Deletions already write at once.
3. After an upgrade, skip the clip store's backup question for that session.

Later: deploying and calibrating a buoy in the Viewer (workarounds: import, manual calculation);
putting clips back on the queue; the spectrogram marking clips that belong to a crossing; time
delays from cross-correlating clips' audio.

Viewer compaction works, and writes clips saved in the Viewer. Converting old datasets' crossings is
not planned: the upgrade rematches instead, and keeps the old files in a backup.

## Tests owed

- The dataset upgrade, "Upgrade old DIFAR files to version 3", passed on the pilot: refusal on loaded data, then all data
  backed up, 6 clips rewritten, crossings made, and pgmatlab reading both files as version 3.
- Deleting a saved clip passed in the Viewer on the simulated data: an unmatched clip, a clip in a
  crossing of two (the crossing deleted), and a clip saved and deleted in one session. Each left the
  panel, map and spectrogram at once, and the files and database.
- "Rematch clips" passed in the Viewer on the simulated data, run from the DIFAR offline tasks and
  after a buoy edit. Keeping operator crossings is untested: it needs an operator match, as in rung 7.
- Step 5 passed in the Viewer on the simulated data: crossings load and draw, a buoy edit moves them,
  and a new clip claims its partner and deletes the crossing it left with one clip.
- Normal mode buoy edits are untested since step 4. Change a heading after a whale in one session:
  the crossing should keep its clips and move, and clip rows' `TrueBearing` should change.
- The pilot's 2013 files opening in the Viewer after step 4. Not needed for new reanalysis.
- Rung 7b: a deliberately wrong match, chosen in the match selector. The crossing should record
  `OPERATOR` and land away from the others.
- A three-buoy rung, where a third bearing extends a two-buoy crossing. Needs new audio.
- An overlap rung: rung 2 twice without a reset, to reproduce the overlapping deployments below.
- Skipping the core save when a Viewer save is cancelled is untested. It needs a save cancelled by a
  file name clash, which a leftover binary file from an earlier test produces.
- Duplicate UIDs in the upgrade: tests 5 to 8 in `viewer_editing_design.md`, on slices of the 2019
  voyage data. The check passed on 28 to 30 January (1,470 different clips sharing 709 UIDs) and 10 to
  12 February (none). Renumbering and the table rebuild passed on 21 February 00:00 to 06:00: 284
  clips, 17 UIDs shared by 34 clips, renumbered 1 to 284; 47,827 old clip rows replaced by 284 with
  the same UIDs; binary files and database backed up; 21 crossings, every clip found in the MATLAB
  file by channel and start time, 13 of MATLAB's triangulations in that window made with the same
  clips. Still owed: dropping second copies. The census found none in these binary files, so the
  clip MATLAB's file holds twice (old UID 300) may be a duplicate made when MATLAB read the files.
  The full voyage run will show whether any exist.
- Buoy records loaded with each chunk of the rematch and upgrade passed on 10 to 12 February: every
  clip found its buoy, and 596 crossings were made (512 of two clips, 84 of three). Against the MATLAB
  rematch of those days, 471 of its 524 triangulations were made with the same clips, their locations
  a median 0.55 km apart and 90% within 2.2 km. In 40 more, PAMGuard added a third buoy to a MATLAB
  pair. PAMGuard crossed 202 clips MATLAB left alone, mostly blue whale calls; MATLAB crossed 17 that
  PAMGuard did not. Owed: a buoy edit on the simulated data, which runs the same rematch. Normal mode
  now skips matching a clip with no buoy position, instead of throwing; untested.
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

## Rematch on real data

- Core's offline tasks process only clips whose start time lies within their file's time span. A clip
  marked just before a file rolls over and saved just after sits in the next file, and is skipped: not
  rematched, its row not updated, not marked as upgraded. 33 of 3,875 clips on 10 to 12 February. At
  least 7 of the 17 clips MATLAB crossed and PAMGuard did not are in the last minute of an hour. Now
  the rematch and upgrade process these clips themselves, first in each load. Test owed: on 10 to 12
  February, 33 clips reported as stored late, all 3,875 upgraded, and fewer MATLAB-only clips.
- The rematch's console line counted each crossing once, at the clip that started it, and called them
  clips. It now reports crossings and the clips they hold.
- Five crossings on 10 to 12 February have location errors over 100 km, and two are placed far outside
  the survey, one at 131 degrees East. Near-parallel bearings, probably; core printed
  `FunctionEvaluationException` for its error estimate six times. The distance limit does not catch
  them. Consider flagging crossings by error rather than rejecting them.
- Clip rows are updated by UID. The slices use the whole voyage's database, whose clip table holds
  other days' clips with the same UIDs, so the rematch wrote this slice's buoy values into those rows
  too, and `crossing_clips.sql` joined them in (5,979 rows for 1,276 clips). The clip table rebuild
  prevents both. It matters even when the binary files' UIDs are unique.

## Viewer

- A buoy edit in the Viewer loads the buoy's whole period to update clip rows and crossings. For a long
  deployment that may be slow. Ask first if it proves a nuisance.
- A crossing whose clips fall in two load chunks of that task is not recalculated. The console lists
  their UIDs. Rare, since a call spans seconds and chunks are files.

- Scrolling backwards then marking makes a clip that starts before the raw audio in memory. Fix: read
  the clip from the WAV files. The console reports "requested from Raw input data ... have not yet
  arrived".
- Closing the Viewer after a cancelled save loses the clips not yet saved, with only a console
  message. Warn and offer to stay open.
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
- In the Viewer, clips marked for the queue do not appear in the queue strip; they come up in the
  DIFARgram one at a time. Rebuilding the strip from the queue's block was tried and removed: a clip
  being worked stays in that block, so it reappeared in the strip, and a clip is given its display
  settings after it is marked, so the strip showed full bandwidth. Fix: send the queue strip the
  add and update notices normal mode sends, for clips made in the Viewer, without reaching
  observers that write rows, which the clip store already does.
- In the Viewer, `PamDataBlock.shouldNotify()` is false, so adding or removing a unit tells no normal
  observer, and core has no notice for a removed unit at all. DIFAR tells its saved clips panel
  directly, and reloads the view after a deletion so the map and spectrogram redraw.
- `BINOVERLAPWARNING` is one named warning for every module.
- The Viewer never loaded super-detection blocks alongside the blocks they group. Fixed by
  PAMGuard/PAMGuard#348, merged into `main`.
- Core's UID check at Viewer startup finds missing UIDs, not repeated ones. Datasets whose UID count
  restarted after crashes pass it.
- `OfflineTaskGroup.processData` skips loaded units whose start time is outside the file's time span.
  For modules that store a unit after its start time, such as DIFAR, those units are never processed.
- The Viewer save writes back, from the old file, any object it cannot find in memory. A unit dropped
  on load, such as a second copy of a clip, returns in the rewritten file.
- After an upgrade, the first save of new clips asks again whether to back up the files, though the
  upgrade has just backed them up. The upgrade could tell the clip store.
- `OfflineTaskGroup.completeTasks` completes every task, ticked or not, though `prepareTasks` only
  prepares ticked ones. DIFAR's tasks report and reload only if prepared in this run.
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
