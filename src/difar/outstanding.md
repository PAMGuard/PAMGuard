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

The 2019 voyage data are the ENRICH sonobuoy data set, AAS 4600, https://doi.org/10.26179/w901-b438
(https://data.aad.gov.au/metadata/AAS_4600_ENRICH_Sonobuoy_Data).

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
1. The queue strip in the Viewer: marked clips did not appear in it until the view reloaded. The
   queue's block tells its observers nothing in the Viewer, so the strip never received the clip.
   Built: on a new queued clip, the strip hands it to its own observer on the block, as Normal mode's
   notice would, reaching no other observer; the saved strip does the same on a save, and rebuilds
   only if its observer cannot be found. If a clip is not yet in the block, the console says so.
   Test owed on the 2013 pilot: a mark appears in the queue at once; a save appears in the saved
   strip at once. First try on the pilot: the strip had no name to find its observer by (it is not a
   user display), so it is now found when the strip is made; new clips showed black until repainted,
   now remade and repainted; and a clip queued and at once taken to be worked could be added after it
   left, so stayed in the strip, now skipped.
1a. From the same pilot session. Built, test owed: one path for Viewer edits, `ViewerEdits` (see
   "One path for edits" in `viewer_editing_design.md`), replacing the strip-by-strip patches. It
   covers the queue and saved strips, the map, writing saves and deletions at once, and the
   database commit. A "Clear match" button in the match selector uses no match for the clip; a
   match chosen or cleared sends `MatchChanged`, which redraws the map and refreshes the DIFARgram's
   buttons ("Save without crossing" had stayed disabled until the view was scrolled). Choosing
   another match now also clears the previous match's other clips.
   First pilot test: queue, match choice, buttons and saves passed; saves wrote their binary files
   at once. Then fixed: crossings' rows were still written only at close, since core writes them
   when the crossing block is saved, so each commit now saves that block too; the match selector
   now empties after a save or delete, and is read only for a saved clip, whose crossing is fixed;
   and a format error in the tracked groups' summary (`%3.0°`, missing its `f`), reached on hovering
   over the groups panel now that tracked groups hear of saved clips.
1b. Follow-up, in priority order (30 September 2026):
   Done on 1 October: items 1, 3 and 4 (`1ab093ec`, `4095541e`, and the next commit).
   1. **Done.** **Marking where the raw audio is not in memory** prints "requested from Raw input data ... have
      not yet arrived" and makes no clip, with nothing shown to the operator. Top of the list.
   2. **Flagged, fit unchanged.** Crossing quality is built: `CrossingAngle` and `OnBuoy` on each
      crossing, errors set to the on-buoy radius (DIFAR settings, 500 m default), and a crossing data
      selector hiding narrow and on-buoy crossings by default. Still open: a fit that does not
      collapse (range floor, rays, or a flatter-topped likelihood).
      **Crossings that collapse onto a buoy.** Four crossings saved on 7 March between 21:20 and
      21:22 sit exactly on buoy 297, with errors of a few millionths of a metre. Buoy 296's bearings
      (about 110 degrees true) point almost straight at 297 (108 degrees from 296), so 296's bearing
      line runs through 297, where 297's own bearing has no meaning: the fit finds a perfect answer
      at the buoy, and its error estimate, from the curvature there, is near zero. Flag it as
      degenerate in crossing quality. For the fit itself: Brian suggests a flatter-topped bearing
      likelihood (sigmoid-like rather than normal or von Mises); a range floor around each buoy, or
      treating bearings as rays from the buoy rather than lines, are other options. Pure DIFAR code.
      The four clips also fall in the span of the audio file damaged until 30 September; delete them.
      A fifth, crossing 14 at 21:33:12 (clips 2000034 on 297, 2000035 on 296 at 109 degrees), was
      saved after the repair and collapsed the same way, so the audio is not the cause.
   3. **Done, except the buoy manager.** **Scrolling.** The queue and saved strips (both scroll bars, vertical and horizontal), and the
      buoy manager's table, scroll too slowly with the mouse wheel or the scroll buttons; dragging is
      fine. The strips' scroll pane is core's
      (`ClipDisplayPanel`), but DIFAR can set its unit increment from outside; the buoy manager is
      DIFAR's (`SonobuoyManagerPanel`).
   4. **Done.** **Classification buttons on clips.** With more than 4 or 5 favourites, the buttons make each
      clip very tall. Spill them into a second column after about 5 rows, and make them narrower
      (`DifarClipDecorations`, a single-column `GridLayout` now).
   5. **New bearings reach the map late.** With "Display only at vessel scroll bar time", the map
      draws a layer up to the scroller's time, the left edge of the spectrogram, so a clip saved on
      screen appears only once it has scrolled off to the left. From `MapPanel` and `SimpleMap`: with a
      layer's "All" ticked, it draws everything from the start up to that time, and "Look Ahead" is
      ignored; with "All" unticked, "Look Ahead" draws from that time forward by the layer's "Time
      (s)", instead of back. So a "Time (s)" equal to the spectrogram's span, with "Look Ahead", draws
      what the spectrogram shows; "Display all selected data" draws the whole loaded period. Tested
      on the pilot: Look Ahead changes nothing. `MapPanel` fetches each layer's units from the
      earliest time up to the scroller's time (`getDataCopy(earliestToPlot, now, ...)`) before
      `shouldPlot` applies Look Ahead, so units ahead are never fetched; `MapPanel`'s own copy of the
      Look Ahead test is commented out. "Display all selected data" does what it says, drawing the
      whole loaded period, but nothing ahead is hidden or faded, so it loses the sense of what was
      just marked. Wanted: the map drawing what the spectrogram shows, with older data fading. So the
      fix is core's: fetch forward when Look Ahead is set, or draw up to the spectrogram's right edge.
2. An auto-compaction setting, writing after every edit. Saves and deletions now write at once;
   only buoy edits, through their offline task, still wait.
3. After an upgrade, skip the clip store's backup question for that session. Done: the upgrade hands
   its backup to the clip store, which adds to it and does not ask. Passed on the full voyage. The
   console line then says "25 original files backed up", counting the files the save reshapes, not
   the copies made; the backup already held them, so none were copied. It now counts copies, and
   says nothing when none were made. Test owed with the queue strip.

Later: deploying and calibrating a buoy in the Viewer (workarounds: import, manual calculation);
putting clips back on the queue; the spectrogram marking clips that belong to a crossing; time
delays from cross-correlating clips' audio.

Viewer compaction works, and writes clips saved in the Viewer. Converting old datasets' crossings is
not planned: the upgrade rematches instead, and keeps the old files in a backup.

## Normal mode lost DIFAR clips at Stop: fixed 12 October 2026

Found on the 2013 pilot's first complete Koogu run (`koogu-auto/runs/2026-10-10_1209_first-complete-run`,
build 7697a9a). In the Viewer, DIFAR clips and triangulations stopped at 2013-03-08 00:30:52, whatever
the load period.

- **A. The queue outlived the binary store.** When the audio files ran out, the binary store closed,
  but DIFAR kept working through its queue. Triangulations still reached the database; their clips
  were written nowhere. About 870 clips were lost.
- **B. DIFAR fell behind the audio without limit.** File audio ran at 80 to 100 times real time. A
  clip is written into whichever binary file is open when DIFAR saves it, and files roll over by the
  master clock, not clip time. The lag grew to about 6 hours, so late clips sat in files hours away
  from their own time, and the Viewer found them only when the load period covered both.

**Cause.** DIFAR worked its queue outside PAMGuard's data flow. A detector's clip was put in the queue
and DIFAR returned at once, so PAMGuard counted it as done. The clip was then demultiplexed on a
Swing worker, and saved by a Swing timer on the Swing thread, one clip at a time. Even with a delay
of 0 s, a save waited for the timer's first tick, 100 ms later, behind display drawing. At 100 times
real time, 100 ms is 10 s of data per clip. PAMGuard never held the audio back for this queue, and
did not wait for it at Stop.

**Fix, on `difar-unattended-save`.** Auto save has two choices in the Automation settings: "With a
delay for review" (as before) and "As fast as possible, with no review"
(`DifarParameters.autoSaveUnattended`, false for older settings). With the second, auto-process on,
in Normal mode, DIFAR processes and saves each classified clip where it is made, on the thread that
made it (`DifarProcess.isUnattended`, `processUnattended`; `DifarControl.saveUnattended`). For the
Koogu trigger this is the deep learning classifier's worker. PAMGuard's existing pause for slow
modules then holds the audio back. No core changes. Saving with a delay for review, at any delay
including 0 s, and the Viewer, work as before.

**Result, 2013 pilot rerun** (`koogu-auto/runs/2026-10-12_1207_unattended-save`, build 8f5cbb0):
- All 2,591 DIFAR clips in the database are in the binary files (footers 5,182, halved). None lost.
- Of 1,632 clips in triangulations, 12 sit in the next file after their own time, each within 25 s
  of the hour boundary: a detector reports a call only after its analysis window. None further out.
- No "Error in DifarProcess.difarTrigger" and no "has no bearing" in the console.
- 745 triangulations (603 two-clip, 142 three-clip), against 749 on the first run.
- About 14 minutes of wall clock for 18 hours of data, 77 times real time on average, dropping to
  about 25 times through continuous three-channel calling.

Still open:
- The deep learning classifier did not wait for its own worker at Stop. On the rerun it wrote three
  results at 06:10:26 after its binary file had closed ("in file null"). **Fix written, not yet
  tested on the pilot** (branch `dl-sound-file-batches`, 19 October 2026): a busy flag in
  `DLTaskThread`, `waitForResults` on the model, and a wait of up to 120 s in
  `DLClassifyProcess.pamStop`. Binary files close in `pamHasStopped`, after every module's
  `pamStop`, so waiting there is enough. PAMGuard restarts the classifier at every file, so the
  wait also runs at each file boundary. Test: rerun the pilot and check for no "in file null" and
  no "still classifying" in the console.
  **Tested 20 October 2026 (run 4, five triggers, three models): not fixed.** Nine "Storing
  binary object ... from DL Model Data in file null with no module header" lines at the end of the
  audio (06:10:08 to 06:10:30), three per model, and no "still classifying". So the wait covers the
  detections but not the Model Data stream, or the end of the audio does not go through the same
  path as Stop. Small: the last few predictions of a run are lost from the binary files.
  **Traced 21 October 2026.** PAMGuard stops in three steps: `pamStop` on every module, then
  `flushDataBlockBuffers` empties the queues between modules (multithreaded runs), then
  `pamHasStopped` closes the binary files. The last 20 s or so of audio is still queued at
  `pamStop`, so it reaches the classifier only in the flush, and nothing waited for it there.
  Detections take the same path, so they can be lost too. Also, `pamStop` ran the leftover input
  buffer without emptying it, so a later batch could classify the same segments again.
  **Fix:** `DLClassifyProcess` overrides `flushDataBlockBuffers` to run the leftover buffer and
  wait for the worker; `runRemainingData` empties the buffer after running it. Test: rerun the
  pilot and check for no "in file null" and no "still classifying" in the console.
- The classifier also warns, many times a run, that its raw data are shorter than its model
  results' time limits, always by 600 samples (2.4 s at 250 Hz). Nothing is trimmed, so detections
  keep their audio. Cause not traced. A guess: merged segments overlap (window 1,125 samples, hop
  500, so 625 samples of overlap each), and the raw data and the results' span count the overlap
  differently. 625 is close to 600 but not equal, so that is not the whole story. Noted only, not
  reported.
- The threaded observers' pause gives up after about 100 short sleeps, then lets data through. Lag
  stayed under a minute on the pilot, but is not strictly bounded.
- A clip the operator saves by hand on the DIFARgram is not locked against an unattended save. Only
  matters if the operator works the queue during an unattended run.
- **Test.** After a Normal mode run, every child clip in the database must be found in the binary
  files, in the file for its own time. A candidate for PAMGuard's test framework, not only the
  pilot's `run.sh`.

## Deep learning speed: tried, 19 October 2026

Three Koogu models (song, D-call, fin 20 Hz) on 20 busy one-hour files of 1 kHz Kerguelen 2015
audio, with no displays. Times are from `pamguard_timing.py` in the reanalysis repo (first to last
prediction row, so Start to the last result).

| Run | Elapsed |
|---|---|
| Before the queue polling sleeps were shortened | 452 s |
| With shorter sleeps (merged on `difar-unattended-save`) | 281 s |
| Batches of 20 s instead of 1 s, for sound files | 300 s |
| Same, under Java Flight Recorder | 415 s |
| One 250 Hz decimator feeding all three models, profiled (song hop wrongly 2 s) | 372 s |
| Same, database committed every 500 rows instead of 10, profiled | 490 s |

What we learned:
- Larger batches do not help. The fixed cost of each model call is small. Removed.
- Committing less often does not help. Reverted.
- Java code is a small share of the run. Within it, Koogu's resampling (jpamutils spline and IIR
  filter) dominates. It runs per segment and so repeats on overlapping audio: about 4.4 times for
  the song model (22 s window, 5 s hop). Skipped when the input is already at the model's rate, so
  a 250 Hz decimator upstream removes it. The pilot already does this.
- With multithreading, each classifier runs on its own thread, so one model and three models take
  about the same time: the slowest chain sets the pace.
- Native samples count threads waiting on the disk or in TensorFlow as busy, so they show where
  threads spend time, not what limits the run. They pointed at SQLite commits, which were not the
  limit.
- Run times vary widely between runs. Compare settings only with repeat runs.

Not pursued: parallel PAMGuard runs over parts of a dataset (the Batch Processor), and detecting
in Python then importing detections for DIFAR. Both stay options for the full reanalysis.

## Five triggers on the 2013 pilot: 20 and 21 October 2026

Five detectors trigger DIFAR on the 2013 pilot (18 h of 3-channel audio, 8 buoys): Koogu song,
D-call and fin 20 Hz, and spectrogram correlation for song and fin 20 Hz. No displays, "As fast as
possible, with no review", stage timing on (`-Ddifar.stageTiming`, `DifarStageTiming`). The test
and its benchmark log are in `pamguard-tests`, `2013-abwv-pilot/multi-detector`.

| Run | Change | Wall time | Real time | DIFAR busy | Demux | Save | Clips | Triangulations |
|---|---|---|---|---|---|---|---|---|
| 4 | five triggers, AAD time domain demux (`e0c811e3`) | 2457 s | 26 times | 1670 s | 1215 s | 340 s | 6667 | 1156 |
| 5 | AAD fast demux (`52860b34`) | 1242 s | 52 times | 552 s | 111 s | 324 s | 6667 | 1151 |
| 6 | Ishmael minimum time fixed, 16 GB heap, short DIFAR keep times (`04263e57`) | 1361 s | 48 times | 607 s | 112 s | 374 s | 5807 | 1044 |
| 7 | tracked groups removed (`f5efd3d2`) | 710 s | 92 times | 263 s | 96 s | 67 s | 5807 | 1044 |

Run 4's wall time is to its last clip; PAMGuard was closed by hand much later.

What we learned:
- **The demux was most of DIFAR's time.** The new AAD fast demux (`AadFastDemux`, frequency
  domain, at the output rate) is 11 times faster on the pilot and 17 times on one 42 s clip, and
  doubles the speed of the whole run. Bearings match the time domain demux: run 5 against run 4,
  same clips and triangulations, apart from the pinned clips below.
- **Pinned bearings at exactly 180 and 270 degrees** come from clips shorter than the DIFARgram
  FFT. Zero padding makes the Bartlett surface symmetric, and the tie goes to the first maximum.
  Not caused by either demux, and not a missing pilot tone. To handle in the quality gate below.
- **The Ishmael minimum time was applied in FFT slices, not seconds,** in PAMGuard 2.02.15 to
  2.02.19 (PR #159). Fixed on `ishmael-peak-picker` (see `IshPeakPicker`). Run 6 against run 5:
  the only change is 855 song and 5 fin spectrogram correlation clips gone, all shorter than
  their minimum time; matched bearings and positions identical. Old results can be reproduced
  on a fixed PAMGuard by setting the minimum time to the old value divided by the FFT hop.
- **Now the save step is slowest, and it slows as the run goes.** In run 6 the median save per
  clip grows from 4 ms to 178 ms over the first 15 minutes of wall time, for every trigger alike,
  then drops to 18 ms and grows again. Run 5 shows the same, less smoothly. Memory is not the
  cause (16 GB heap, plenty free). Demux, DIFARgram and match times stay flat all run.
- **Cause: tracked groups.** The tracked group process observes the saved clips, so its work is
  timed as part of the save. A tracked group should summarise one group's bearings from one buoy,
  but `TrackedGroupDataUnit` never set its buoy start time (the lines were commented out), so no
  clip found its group and every clip made a new one: 5,807 rows in the run 6 tracked groups
  table, one per clip. Each new clip then refitted a position over every group unit held (8 hours
  of data time). Up to 15 minutes into run 6, the median save grows close to linearly with the
  clips held, about 0.05 ms per clip. The drop at 16 minutes, with the clips held unchanged, is
  not explained; nor why, late in the run, some threads are slow while others are fast. Perhaps a
  clip waits for the tracked group thread only when its queue falls far enough behind.
- **Fix: tracked groups removed** (branch `difar-remove-tracked-groups`, 21 October 2026). They
  were unused, and slated for removal. The DIFAR binary format stays at version 3: the tracked
  group slot is written empty and ignored on reading. The `TrackedGroup` column of the clip table
  stays, empty in new rows. A database with an old tracked groups table gets a warning at startup
  naming the last version that reads it (`DifarControl.LAST_VERSION_WITH_TRACKED_GROUPS`, 2.02.19).
  The stage timing loses its `group` column.
- **Run 7 (21 October 2026) confirms it.** Twice as fast as run 6 (710 s against 1361 s, 92
  times real time). DIFAR's save totals 67 s against 374 s; the median save per clip stays under
  15 ms through the run instead of climbing to 160 ms. Clips and triangulations are identical
  to run 6 (5,807 and 1,044). The old configuration loaded, and was re-saved at the end without
  the group list. A smaller rise remains, from about 6 to 14 ms, through the busiest calling
  hours: probably matching against more clips. Not pursued.
- The warning first fired on every launch of this test: its starting database has an empty
  tracked groups table, made by an older PAMGuard. It now fires only when the table has rows,
  and once per database: at startup, and when the Viewer loads a different database.
- Run 7 also had one `IndexOutOfBoundsException` at startup, in core
  (`PamObservable.getPamObserver`, from the Deep Learning segmenter's thread setting sample
  rates). Probably a race between threads over the observer list. Not in run 6, no DIFAR code
  in the stack, and the run was unaffected. Watch for it in later runs.

### DIFAR quality gate (parked)

Not started. In unattended mode, a clip that fails a check should still be saved, without a
bearing, and not used in triangulation. Checks to start with: clip shorter than the DIFARgram
FFT (the pinned bearings), and pilot tone SNR too low. Brian's idea for later: such a clip could
still join a triangulation by time delay alone. How often each check fires decides whether that
is worth it.

## Code, before or after the pull request

- Reprocessing over existing binaries. A Normal mode rerun over the same data time overwrites every
  module's binary files (core warns at startup), but DIFAR's database rows and triangulations for the
  overwritten period remain, pointing at DIFAR clips that are gone. Seen in a rerun of rung 9,
  5 October 2026. Options, after the pull request:
  1. A core "keep both" option in the binary store, writing a unique file name on a collision: a
     question for Doug, since every module overwrites, not only DIFAR.
  2. DIFAR keeping its tables consistent with its binaries: remove the DIFAR rows and triangulations
     for an overwritten period, or at least report orphaned rows at Viewer mode startup, as the UID
     check does.
  3. Reviving the hour-grid compaction (`TimeGrid`, `ClipMerge`, `CompactionPlan`, parked in
     `d8100ca6`) to consolidate several DIFAR files per hour, for Viewer mode edits and kept reruns.
     It was parked because DIFAR clips carried their triangulations; that obstacle is gone now that
     triangulations live in the database.

- Rename UI labels to match the help's terms: the settings' "Max candidates per buoy" and
  "On-buoy radius", to triangulation and sonobuoy. Leave database table and column names. The
  data selector's labels are done ("Triangulation quality").

## Tests owed

- Clean up, organise and document the manual DIFAR tests: the simulated two-sonobuoy exercise and
  its harness (`S:\Data\2019-difar-retest`: `rung.sh`, `runs/`, several configuration copies),
  the 2019 Viewer fixtures, the 2013 pilot checks, and the batch test sheets. Decide what lives in
  the repo and what stays with the data.
  Always `./rung.sh reset` before a Normal mode run of the simulated exercise; otherwise the run
  overwrites the previous binaries and leaves their database rows behind.

- Crossing quality passed on the 2013 pilot (1 October 2026): the setting, rematch counts, the new
  columns, on-buoy errors at the radius, and the selector's three settings on the map. Owed: rematch
  the full 2019 voyage and compare the narrow count with the design's 458 of 6,037 (that count
  included on-buoy crossings, which are now counted separately).
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
- The full voyage, 2019-enrich-full, upgraded once: 47,853 clips renumbered 1 to 47,853, no second
  copies, every clip in MATLAB's file found by channel and start time, plus 141 on 24 February that
  MATLAB's file lacks. 6,037 crossings against MATLAB's 4,804 triangulations; 3,893 of those made with
  the same clips (81%; 64% for fin whale 20 Hz pulses, about 80% for blue whale calls), locations a
  median 0.62 km apart. 107 crossings have errors over 100 km. Two files have unreadable headers
  (`20190126_150000`, `20190220_230752`), yet their clips are in the table. 289 clips had no buoy in
  force, 215 of them on channel 1. The run left 15 clips with old UIDs (above); rerun from the backup.
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

## Viewer display, from the full voyage

Settings the Viewer needs, such as "all data" on the map, are listed in `viewer_settings.md`. The
items below are about making fewer of them necessary.

- Buoys appear on the map only with "all data" ticked in the map's plot options. A buoy deployed
  before the view starts is outside the view's time window. Buoys in force during the view should
  show without it.
- The buoy manager's time window controls sit in a bottom panel that starts collapsed. Show it by
  default. Not essential: use the empty space to its left as a status line, giving the number of
  buoys in the manager, the first deploy time and last end time, and the first and last buoy names
  and channels in the loaded window.
- After an upgrade, the Viewer warned at the next start that data units lack UIDs ("Binary Store
  missing UID's in Processed DIFAR Data"). The files were right: deleting `serialisedBinaryMap.data`
  cleared it. Core updates a rewritten file's map entry with the old file's footer; crash files have
  none, so the entry lost its UID range, and core saved that to the cache at close. The upgrade now
  re-reads such entries from disk at its end. Passed on the full voyage: 2 entries refreshed, and a
  restart keeping the cache gave no warning.
- On the full voyage after the upgrade, a clip saved in the Viewer did not appear in the saved strip
  until the view reloaded. Now handed to the saved strip directly (item 1 above); if it recurs, the
  console line naming a clip not yet in the saved clips shows why. The saved strip is meant to
  rebuild after a save. Check whether the save button now defers the write to the reload, and
  whether the strip is told of the new clip.

## Calibration

- The choice of mean, mode or mean near mode is only in a right-click menu on the histogram. Make it
  radio buttons in the dialog.
- Calibration exists in Normal mode only: "Start calibration" takes `vesselClipNumber` clips of
  `vesselClipLength` seconds, `vesselClipSeparation` seconds apart, in the Vessel classification's
  band, compares their bearings with the ship's from GPS, and sets the buoy's correction from the
  histogram (`DifarProcess.startBuoyCalibration`, `doBuoyCalibration`). It reads samples by absolute
  sample number, which counts from 1970 in the Viewer, so it will not work there as it stands.
- Idea, 30 September 2026: automatic calibration. The ship's position and the buoy's are known, and
  so is the ship's band. While the ship is within a set distance of a buoy, take calibration clips
  in that band automatically, and propose the correction. Useful live, and as an offline task in the
  Viewer, where it would give every buoy of an old voyage a measured correction in one pass. Open
  questions: minimum as well as maximum distance (near field, the ship's length); rejecting clips
  whose bearing is dominated by a whale or another source; whether the correction is applied or only
  proposed; and where calibration sits in Viewer reanalysis (before matching, so buoy edits do not
  rematch everything twice).

## Rematch on real data

- Core's offline tasks process only clips whose start time lies within their file's time span. A clip
  marked just before a file rolls over and saved just after sits in the next file, and is skipped: not
  rematched, its row not updated, not marked as upgraded. 33 of 3,875 clips on 10 to 12 February. At
  least 7 of the 17 clips MATLAB crossed and PAMGuard did not are in the last minute of an hour. The
  rematch and upgrade now process these clips themselves, first in each load; passed on 10 to 12
  February. Core also stops at the first clip after its file's end time, which files cut short by a
  crash can have: on the full voyage 15 clips were renumbered but never processed, so their files
  were rewritten with their old UIDs, clashing with new ones. These are now processed after core has
  finished, before the save. On the rerun every clip was processed, yet 13 clips in 10 files kept
  their old UIDs and their files stayed at version 2. In each of those files every clip starts
  before the file does. Core counts no clip in the file's span, and returns before calling
  loadedDataComplete and before saving, so the clips processed at the start of the load never reach
  their file. The rematch and upgrade now finish such a load themselves. Test owed: a third full
  voyage run from its backup, then pgmatlab: every readable file at version 3, 47,853 distinct UIDs.
- The rematch's console line now counts clips in crossings from the crossings themselves. Counting as
  it went, a clip left unmatched and later taken as a partner stayed counted as unmatched: 12,524
  clips reported against 12,943 in the crossings, on the full voyage.
- Five crossings on 10 to 12 February have location errors over 100 km, and two are placed far outside
  the survey, one at 131 degrees East. Near-parallel bearings, probably; core printed
  `FunctionEvaluationException` for its error estimate six times. The distance limit does not catch
  them. Consider flagging crossings by error rather than rejecting them.
  On the full voyage's first run, the 107 crossings with errors over 100 km are near-parallel
  bearings: their largest angle between bearings has a median of 1.1 degrees, against 26.5 for the
  rest. Of 161 crossings whose bearings meet at 2 degrees or less, 65 have errors over 100 km; of
  1,637 meeting at more than 45 degrees, none do. 25 crossings lie over 1,000 km from a buoy. A
  minimum angle between bearings, as a matching parameter or a flag, would remove nearly all of them.
  Decided: a flag and a data selector, with minimum angle and maximum error; see "Crossing quality"
  in `crossings_design.md`. Not built.
- The clip data selector ("show only cross bearings", species, channels) listed channels 0 to n-1,
  where n was the number of channels in the source's channel map when its parameters were first made
  and saved. With the 2019 data it offered only channels 0 and 1. It now lists, by channel number,
  the channels of the source, one per array hydrophone, those of loaded clips, and any already
  hidden; saved parameters grow to fit. Its "crosses only" test already reads the crossing unit.
  Passed on the full voyage: channels 0, 1 and 2 listed, and hiding channel 2 hides its clips.
- Console noise in long runs. `Simplex2D` now counts failed error estimates instead of printing each
  one, and the rematch summary reports the count. The upgrade's per-clip "dropped a second copy" line
  is gone; its summary counts them. The per-clip "skipped a second copy" in ordinary loads stays.
- Clip rows are updated by UID. The slices use the whole voyage's database, whose clip table holds
  other days' clips with the same UIDs, so the rematch wrote this slice's buoy values into those rows
  too, and `crossing_clips.sql` joined them in (5,979 rows for 1,276 clips). The clip table rebuild
  prevents both. It matters even when the binary files' UIDs are unique.

## Viewer

- A buoy edit in the Viewer loads the buoy's whole period to update clip rows and crossings. For a long
  deployment that may be slow. Ask first if it proves a nuisance.
- A crossing whose clips fall in two load chunks of that task is not recalculated. The console lists
  their UIDs. Rare, since a call spans seconds and chunks are files.

- Marked clips ended early, cut off at the right edge compared with the box marked on the
  spectrogram. Core's spectrogram drew each FFT column at its window's start, so the picture sat half
  a window early: 0.5 s for the pilot's 256 points at 250 Hz. Fixed in core on its own branch,
  `spectrogram-centred-columns`, which draws columns at the window centre in Normal mode and the
  Viewer, and cherry-picked here. DIFAR's interim half-window shift of marks is removed.
- Clear match is not remembered. A DIFAR clip saved with Clear match has no record that the operator
  rejected its matches, so a later rematch (Rematch clips, or the rematch after a sonobuoy edit in
  Viewer mode) can match it anyway. Not yet seen in practice. Fix: record the operator's "no match" on the clip, and have rematching respect it as it keeps
  OPERATOR triangulations.
- A saved clip's classification cannot be changed in place. The species, Vessel and Other buttons on
  saved clips are disabled; delete the clip and mark the call again. A real edit (reclassify,
  reprocess, save through `ViewerEdits`, rematch if the bearing moves) waits until the need is shown.
- Closing the Viewer after a cancelled save loses the clips not yet saved, with only a console
  message. Warn and offer to stay open.
- The map draws bearings only once they scroll off the spectrogram's left edge. The map takes the
  scroller start as now. Core Map code.
- Queued clips are dropped when the window changes, because the queue clears.
- Sample numbers in Viewer-written headers count from 1970. Nothing in DIFAR reads them.
- The parked compactor (commit `ff1a2c86`) drops cross-match partners. Do not run it on real data
  until crossings are separate units.
- A single click in the DIFARgram on a saved clip counts as an edit (`DIFARGram.java:763` updates the
  clip), so the next scroll asks to save. Seen on the 2013 pilot, 11 October 2026, after a stray
  click. Clicks on saved clips should change nothing until the operator acts.
- The Viewer save dialog does not say what changed, and Cancel leaves the change pending, so the
  dialog returns at every scroll. Name the changed clips, and offer to discard.
- Changing the load period silently rewrote, in place, every DIFAR file holding a clip in a
  triangulation, with no edits. Seen again on 12 October after A and B were fixed; only timestamps
  changed (footers identical). Cause, confirmed with a breakpoint: the triangulations reload as new
  objects, clips still loaded refer to the old copies, and core's `SuperDetection.addSubDetection`
  unlinked each clip from its old copy (`checkExistingSuperDetections`), which in the viewer marks
  the clip as changed (`PamDataUnit.removeSuperDetection`). Linking already skips that in the
  viewer. Fixed in core on `difar-unattended-save` (0761825): a reloaded copy of the same super
  detection (same UID, same data block) is no longer unlinked. Rechecked 12 October: no data files
  changed after several load period changes. Next: a small core pull request for Doug, separate
  from DIFAR.

## Documentation

- Help updated October 2026: a new page, `difar_ViewerReanalysis.html`, and changes to
  `difar_CrossingBearings.html`, `difar_ConfigureProcessing.html`, `difar_DifarOutput.html` and
  `difar_Overview.html`, with the contents and map entries. Terms made consistent on those pages:
  DIFAR clip, bearing, match, triangulation, sonobuoy, Viewer mode. Screenshots are marked in HTML
  comments ("Screenshot:") for Brian to take from the pilot.
- Relative links fixed across all 16 DIFAR help pages (they were one level too shallow, so the
  stylesheet and links to other modules' help did not resolve). Check styling in PAMGuard's Help.
- Australian Marine Mammal Centre references replaced with the Australian Antarctic Division; the
  demultiplexer is named as it appears in the settings, AMMC_EXPERIMENTAL. MS Access references in
  the post-processing tutorial replaced with SQLite.
- Importing sonobuoy deployments and GPS tracks into a Viewer database is described in the
  post-processing tutorial (Import of Data and Metadata), now linked from the Viewer page. The
  tutorial is a Word export written for Mixed mode in 2014; still worth a review.
- The older DIFAR pages still say "clip" about 180 times; a terminology pass over them is a
  separate job.
- Dead or doubtful external links: the tutorial's `ftp://ftp.aad.gov.au/...` data link, and the
  Overview's Southern Ocean Research Partnership link (`marinemammals.gov.au/sorp`).
- The help's search index (`src/help/JavaHelpSearch`) is built separately; ask Doug how it is rebuilt.

## Panels, low priority

- Merge the buoy manager into the DIFAR localisation panel, perhaps as part of its actions. Make
  the buoy manager and the calibration panel collapsible. Then let the user choose one consolidated
  DIFAR localisation panel, with everything DIFAR on it, or the separate panels as now: they are
  meant to be composable.

## Tidying

- R and MATLAB wrappers around `crossing_clips.sql`.
- pgmatlab: branch `difar-v3-no-tail` in `C:\analysis\pgmatlab` reads version 3 clips, tested on
  the pilot's version 2 files and a real version 3 file. Pushed to the fork. Open its pull request
  alongside the next DIFAR pull request to PAMGuard.

- Detections from reanalysed WAV files should reference the file and the sample within it. Check what
  the WAV annotation already records.
- `canMark()` in DIFAR's spectrogram observer is dead code; its caller is commented out.
- `ClearCrossingTask` is unused. Delete it with `DIFARCrossingInfo`.
- Tracked groups: removed on 21 October 2026 (branch `difar-remove-tracked-groups`; see "Five
  triggers on the 2013 pilot"). A tracker could replace them later, for example PAMGuard's existing
  multiple hypothesis tracking. `ViewerEdits.addConsumer` is kept for such a tracker in the Viewer.
- The simulated sonobuoy test document lives in the data folder. Consider moving it into the repo.

## Timing errors and sonobuoy positions

From the DCLDE 2024 rematch, 5 October 2026. Both timing limits are absolute times, and neither
suits every dataset. The max timing residual of 6 s came from the median sonobuoy separation of the
2019 voyage; the DCLDE sonobuoys are much closer. The max correlated timing residual of 0.5 s was a
placeholder with no error budget behind it. A delay's error has three parts: how well it is
measured (correlation: tens of ms; start times: about a second), where each sonobuoy is (100 m is
0.07 s), and drift since deployment (0.5 knots for 3 hours is 2.8 km, nearly 2 s). Correlation
cannot see drift: a clean peak gives a precise but biased delay.

1. **Measure first.** The rematch writes every triangulation's residuals, one row per delay, to
   `<database>_triangulation_residuals_<time>.csv` beside the database: separation, measured and
   predicted delay, residual, whether correlated, hours since each sonobuoy's deployment, and the
   bearing residual at each end. Built. Use it to see whether drift dominates, and to choose the
   settings below.
2. **An error for each delay, and limits in units of it.** sigma_ij^2 = sigma_t,i^2 + sigma_t,j^2 +
   (sigma_p,i^2 + sigma_p,j^2) / c^2, where sigma_t is the timing error (detection or correlation)
   and sigma_p a sonobuoy's position error, growing with time since deployment: sigma_p = sigma_p0 +
   drift rate x hours deployed. Reject a fit when any residual exceeds k sigma, k about 3; the same
   for bearings. The candidate window in `getMatchingUnits` uses the same sigma instead of borrowing
   the residual limit. Two physical settings, position error and drift rate, replace two arbitrary
   limits, and the rule works for close sonobuoys, old sonobuoys and correlated delays alike.
3. **Separation as a diagnostic.** Where the travel time between two sonobuoys is under about
   3 sigma, timing cannot tell a right match from a wrong one; say so in the console.
4. **Later: a model of sonobuoy positions.** A Kalman or particle filter with each sonobuoy's
   position, velocity and compass correction as its state, updated from the residuals of confident
   triangulations and from the research vessel as a source of known position. Matching then uses
   its predicted positions and their covariances as sigma_p, and matching and the filter can
   alternate, EM style. The state-space idea from September, now with a defined place to plug in.

## Functional tests of correlation (next)

From parking the DCLDE 2024 reanalysis, 6 October 2026: test time delays by
correlation on data whose timing is known before relying on it on 32 loosely
synchronised sonobuoys. Notes on what DCLDE taught are in
S:\manuscripts\2023-DCLDE\TODO.md and DATA_ISSUES.md.

1. **Synthetic first.** Simulated DIFAR clips (simulateDifar, with w15 in double
   precision) of one call at known positions, through ClipDelays and the match
   selector: correlated arrival times should reproduce the simulated delays to a
   frame or two, and a deliberately wrong pairing should fail the correlated
   residual. The ClipCorrelator unit tests cover spectrograms only, not the path
   through matching.
2. **AAD recordings.** All channels share one multichannel A/D converter, so
   delays between sonobuoys are exact to a few samples. A voyage with a research
   vessel track (ENRICH 2019 or later) gives a known source: correlated delays
   on vessel noise or calibration transmissions should match those predicted
   from the vessel and sonobuoy positions.
3. **Then real calls on AAD data:** peak heights for blue whale and other call
   types, the threshold, and how often correlation and start-time delays
   disagree.

## Core issues for Doug

- A data unit has one database index, but a sub-detection logged in its own table and also linked
  from a super-detection's subtable has two rows. `SQLLogging.logSubtableData()` writes and
  updates the subtable row through the unit's index and leaves the unit holding the subtable row's
  index (the lines that saved and restored it are commented out). The unit's next update is then
  written into its own table at the subtable row's index, over a different unit. In the 2013 pilot
  Normal mode run of 12 October 2026, 417 of 2,591 DIFAR clip rows were overwritten this way, each
  time a later triangulation claimed clips from an earlier one. Fixed for DIFAR in
  `DifarCrossingSubLogging.logSubtableData()`, which hands each clip its subtable index only while
  the standard method runs. Any other module whose sub-detections have their own table (click
  detector clicks in offline events, if clicks are logged) may have the same problem. A core fix
  would make DIFAR's override unnecessary. Check: `check_run_db.py` in the 2013 pilot test
  folder, `S:\brian_mil\Documents\Pamguard\2013-abwv-pilot`.
  Tested 12 October 2026: a Normal mode rerun of the pilot passed it, with 2,591 clip rows, all
  with their own UID, and all 1,632 link rows finding their clip. Detections and triangulations
  were unchanged (745). Viewer saves use the same code but have not been retested.

- Settings were saved and loaded with quadratic string building. `Ascii6Bit.createStringData()`
  added one character at a time with `+=`, and `LogSettings` joined stored rows the same way, so
  each addition recopied everything. Closing the Viewer on the DCLDE 2024 database took about ten
  minutes and looked like a hang. Fixed with `StringBuilder` on branch `settings-string-building`:
  output identical, 3 MB of settings now encodes in 0.13 s. Test: `test/PamUtils/Ascii6BitTest`.
- The streamer import added the current array to the Array Manager's list once per row imported,
  and `ArrayManager.addArray` did not check for repeats. In memory these were references to one
  array, but settings are cloned array by array when they load, so each became a full copy. The
  DCLDE 2024 database held 150 arrays (three imports of 49 rows, each a 32-streamer array), 2.3 MB
  of settings. Fixed on branch `streamer-import-duplicates`: the import no longer adds the array,
  and `addArray` ignores an array already listed. Existing copies stay until deleted.
- `Correlations.getCorrelation()` is off by about 14% of the peak against a direct sum, even with
  zero padding. The likely cause is how it fills the negative frequencies (`fftLength-i-1` rather
  than `fftLength-i`) and the packed DC and Nyquist terms. The route inside `getDelay()` is
  accurate. DIFAR's `ClipCorrelator` uses that route instead.
- The audio file loader stops at a gap between files longer than a second (`WavAudioFile`, "don't
  carry on if there is a file gap"). A spectrogram window that starts before a gap stays blank after
  it, though audio follows. The 2013 pilot has eight gaps, from recorder restarts; at 22:55 a window
  starting at 22:55:44 drew only a 0.3 s file and stopped, though audio resumes at 22:56:27. Marks
  there still make clips, since DIFAR loads each mark's own span.
- The spectrogram drew each FFT column at its window's start, not its centre, so the picture sat
  half a window early. Fixed on branch `spectrogram-centred-columns`, for a pull request. The JavaFX
  spectrogram is not covered.
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
- `OfflineTaskGroup` prints "No. viewer units: N" for every file it loads, about a thousand lines for a
  voyage. Could go to `Debug.out`.
- The Viewer never loaded super-detection blocks alongside the blocks they group. Fixed by
  PAMGuard/PAMGuard#348, merged into `main`.
- Core's UID check at Viewer startup finds missing UIDs, not repeated ones. Datasets whose UID count
  restarted after crashes pass it.
- The map's "Look Ahead" does nothing: `MapPanel` fetches a layer's units only up to the scroller's
  time, before `SimpleMap.shouldPlot` applies Look Ahead.
- `OfflineTaskGroup.processData` returns before `loadedDataComplete` and before saving when no loaded
  unit starts within the file's time span, so a task that processes units outside the span cannot
  save them.
- `OfflineTaskGroup.processData` skips loaded units whose start time is outside the file's time span.
  For modules that store a unit after its start time, such as DIFAR, those units are never processed.
- `BinaryStore.saveData(File, ...)` updates the rewritten file's map entry with the old file's footer,
  not the one just written. For a file with no footer, the entry loses its UID range, and the cached
  map saved at close makes the next start warn that units lack UIDs.
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
- Stored settings override `-wavfilefolder` in the Viewer. The command line sets the folder, then
  restored settings set it back to the stored path (`Setting offline wav file folder to ...`). If
  that path is gone, Sound Files show no data. Seen on the 2013 pilot, 12 October 2026.
- The map's DIFAR data selectors fail to load their settings at Viewer startup: `CompoundParams
  cannot be cast to DifarSelectParameters`, for Queued and Processed DIFAR Data. Possibly core now
  wraps the selector in a compound one, since clips have a parent triangulation. Check whether DIFAR
  or core should handle it.
- The data map's time axis rarely scales to the data. Normal mode file runs write settings and other
  rows at wall-clock time, and the start defaults to 1970. Settings changes made outside the data's
  time would be better on their own list than on the data axis.
- Coupled scrollers overflowed the stack once in the Viewer (`ViewerScrollerManager.followCoupledScroller`
  and `PamScrollSlider.valueSetMillis` calling each other), after a cancelled DIFAR save dialog.
  Not reproduced on a fresh load. Watch for it.
- Storage Options keeps any data with a binary output out of the database by default
  (`StorageOptions.setBlockOptions`), overriding DIFAR's own request (`DifarProcess`,
  `setShouldLog(true)`). So DIFAR clips go to binary files only unless the user ticks Database. The
  triangulation design treats clip rows in the database as the one source of truth for buoy-derived
  values, so DIFAR wants them logged. A question for Doug: let a data block declare its preferred
  storage default.
