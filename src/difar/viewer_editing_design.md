# DIFAR: editing in the Viewer

Brian Miller, Australian Antarctic Division. Branch `difar-crossings`, 27 September 2026. Duplicate
UIDs added 29 September 2026.

## Aim

Reanalysis in the Viewer, with time to correct mistakes: deleting a mistaken clip, which makes editing
a clip delete and re-mark; bulk rematch; upgrading an old dataset so it can be edited; and writing
edits at once if wanted.

## What exists

`ViewerClipStore` already handles most of it.

- It keeps, in memory, the UIDs of clips deleted since the last compaction. A clip saved and deleted
  in the same session is simply forgotten, since it was never written.
- Compaction rewrites the affected binary files without the deleted clips, and deletes their
  database rows by UID. It runs whenever the Viewer saves: on File, Save, before loading new data,
  and on exit.
- Nothing calls `clipDeleted` yet. The existing delete message only removes an unsaved clip from the
  queue.

This covers the proposal of an in-memory deleted marker, skipped at compaction. The marker is the
store's list of UIDs rather than a field on the clip. The clip leaves its data block at once, so
displays, matching and crossings stop seeing it without each checking a flag.

## What deleting a clip does

1. The clip leaves the saved clips block, and its UID goes on the store's deleted list.
2. It leaves its crossing, by the same rule as a claim: a crossing left with two or more clips is
   recalculated from them, else deleted. `alwaysDeleteTrimmedCrossings` deletes instead. This reuses
   the trim code in `CrossingRecorder`, which the Viewer test has shown writes crossing changes to the
   database.
3. The view reloads, so the map and spectrogram redraw without the clip. The viewer saves before
   loading, so compaction then drops the clip from its binary file and deletes its database row at
   once. A clip saved and deleted in the same session was never written, so there is nothing to do.

## How the user deletes a clip

Through the delete controls that already exist: the DIFARgram's button and key, and the saved clips
panel's button. All go through `DifarControl.deleteClip`. For a clip on the queue it does what it did
before. For a saved clip in the Viewer it deletes it as above, after a confirmation naming the clip
and, if it has one, what happens to its crossing. Save controls stay disabled for saved clips.

In the Viewer a data block tells its observers nothing when a unit is added or removed. So the saved
clips panel is told directly: it drops a deleted clip, and rebuilds itself after a save.

## Normal mode

Binary files are open and append-only while PAMGuard runs, so a saved clip cannot leave its file. For
now, deleting saved clips is Viewer only. Normal mode keeps deleting from the queue, as now.

## Old files: read only, upgraded as a dataset

Files at versions 0 to 2 are read only. In the Viewer, saving or deleting a clip while older clips
are loaded is refused, with a message pointing to the upgrade. Without the refusal, saving a clip
would rewrite its file at version 3 and silently lose the crossings stored in it.

The upgrade is "Upgrade old DIFAR files to version 3", in the DIFAR offline tasks. It replaces the
old copy to database task, which rewrote every clip file at version 3 as a side effect, with no
backup and no rematch. The upgrade:

1. Reads every DIFAR file once and checks for clips that share a UID, as set out in the next
   section. It asks before going on if they do.
2. Backs up every DIFAR binary file, as the clip store's backup does, and copies the database file
   into the same backup folder. It does nothing more if either copy fails.
3. Deletes every DIFAR clip row from the database.
4. As each file loads: drops exact duplicates, renumbers the clips if that was chosen, writes one row
   per clip, and rematches, by running `RematchTask`'s steps, making crossing units.
5. Marks each clip from an older file as changed, so PAMGuard rewrites its file at version 3.

It is run over all data, so matching can find partners anywhere in the dataset.

The old crossings were made by buggy code, so they stay in the backup only, as a record of what was
thought at the time. The new crossings are the best current estimate.

This needs bulk rematch, below, which is built first.

## Duplicate UIDs in older datasets

Older DIFAR files can hold different clips with the same UID. In the 2019 voyage data the UID count
restarted each time PAMGuard crashed: 47,713 clips carry only 8,389 distinct UIDs. The code assumes
UIDs are unique in three places. Clip rows are updated by UID. Core reattaches a crossing's clips by
UID alone, within a time window. Compaction keeps one clip per UID. Core's UID check at Viewer startup
looks for missing UIDs, not repeated ones, so these datasets pass it.

**The check.** Before changing anything, the upgrade reads every DIFAR file once, as core's UID
repair does, and counts two things: different clips that share a UID, and exact duplicates. An exact
duplicate is one clip stored twice, with the same UID, channel and start time.

- If no different clips share a UID, every clip keeps its UID.
- If some do, the upgrade says how many and offers two choices: renumber, or cancel. Cancel changes
  nothing.
- Renumbering is offered only when every DIFAR file is older than version 3 and the database holds no
  crossings. Renumbering would cut the links of crossings already made. Otherwise the upgrade
  refuses, and says why.

**Renumbering.** Every clip gets a new UID, counting from 1. Files load in time order, and each
file's clips are renumbered as it loads, in time order then by channel, before the rematch sees them.
A clip stored just after its file began gets its number with that file, so the order is by file first
and only nearly by time. So
crossings are made with the new UIDs. The Viewer's next UID then follows the highest new one. The old
UIDs survive only in the backup. Other records, such as the MATLAB rematch of the 2019 data, pair with
clips by channel and start time.

**Exact duplicates** are dropped, and counted in the console. The Viewer already skips the second
copy of a clip when both are in the same file; it now checks channel and start time as well as UID,
since different clips can share a UID. But core's Viewer save writes back, from the old file, any
object it cannot find in memory. So the skipped copy would return with its old UID, and after a
renumber that UID may belong to another clip. The upgrade therefore drops it from the rewritten file
as well: while the upgrade runs, the data block notes each second copy, and DIFAR's binary source
returns nothing for a noted copy, which core's save then leaves out. A copy in another file loads
separately; the upgrade removes it from memory and notes it the same way. The notes are cleared when
the upgrade ends, since a normal load stops reading a file at the first object that returns nothing.

**The clip table.** The upgrade rebuilds the DIFAR clip table from the binary files on every run,
renumbered or not. Older tables can be incomplete: in 2019 the storage setting sometimes reverted to
binary only. They can also hold duplicate rows, made by the old copy to database task. The upgrade
deletes every clip row, then writes one row per clip as each file loads, before the rematch updates the
buoy columns. This is why the database is now backed up.

**A risk to check.** Core writes a rewritten file's footer with a UID range taken from counters, not
from the file's contents (see the core issues in `outstanding.md`). After a renumber the ranges may be
wrong. The Viewer's next UID is the highest of these ranges and its own count, so a wrong range can
only leave a gap. The tests look at the footers.

## Bulk rematch

The DIFAR offline tasks rematched every clip over a period until step 4, which changed that task to
relocate existing crossings only. "Rematch clips" restores it, making crossing units, and replaces the
relocating task: a buoy change can make old pairings wrong, so after a buoy edit matches are chosen
again, not just moved. It runs from the DIFAR offline tasks dialog, and automatically after a buoy
edit in the Viewer, over the buoy's period.

The offline task group loads one file of clips at a time. A clip near the start or end of a file
cannot see partners in the next file, as with the old rematch. Loading a margin of clips either side
would fix that, and is worth doing if files are short. In the 2019 data files are hourly, and only 5
of 4,804 MATLAB triangulations span two clock hours.

Each chunk of clips needs the buoy records in force at its time. The rematch and the upgrade declare
the buoy records (the array's streamer data) as required, so core loads them with each chunk, and the
records before it, as the Viewer does for a view. Without this, a task saw only the buoys loaded for
the current view, and clips outside it had no buoy position. A clip with no buoy position in force is
left unmatched and counted in the console.

Core processes only clips that start within their file's time span. A clip saved just after a file
rolls over starts before its file does, so core skips it. A file cut short by a crash can end before
its last clip, and core stops there. The rematch and the upgrade process both kinds themselves:
clips before the file at the start of each load, so clips are still taken in time order, and clips
after it once core has finished, before the file is saved. When every clip in a file starts before
it, core processes nothing and returns without finishing the load or saving; the rematch and the
upgrade then finish and save that load themselves.

Within a file, clips are matched in time order: each gets the best viable match, chosen
automatically, and its crossing is recorded by the same `CrossingRecorder` rules as saving a clip.
That is how an operator works through the queue.

Crossings chosen by the operator are kept by default, and worked out again from their clips, since
their buoys may have changed. A setting replaces them too. Clips' database rows get their buoy's
current values.

The result depends on the order. A clip takes its best match among the clips still free, and a clip
matched earlier in the run is not free, so the first match stands. Of two clips of the same call on
one buoy, the earlier, or the better scored, is matched, and a buoy edit can change which. The code
and its documentation say so. Ways to reduce the dependence, for later:

- Match all loaded clips together, choosing the set of crossings that fits best overall.
- Weigh bearing residuals and time-delay residuals together when two matches compete.
- Prefer a later match, or one with more buoys, when a clip could join either.

## One path for edits

Added 30 September 2026. In the Viewer, core's data blocks announce updates to their observers but
not additions or removals, since Viewer data are normally only loaded. So displays never heard of a
clip marked, saved or deleted, and each display (queue strip, saved strip, map, buttons) had been
caught up by its own patch. Every interactive edit now goes through one class, `ViewerEdits`:

- **Added:** a clip joining a block is announced to the block's displays, as in Normal mode.
  Displays are the observers that are not processes; processes observe blocks to process data and
  must not re-process or write rows because of an edit. Processes that consume saved clips are told
  explicitly once registered; DIFAR's tracked groups is the first.
- **Changed:** core's own update notice, which reaches every observer: a new match or crossing.
- **Removed:** displays are told to redraw once a clip has gone.
- **Commit:** the saved clips' binary files are written and the database committed, straight away.

The calls are made where the edits happen, on the Swing thread, so notices arrive in the order of
the edits: a mark joining the queue (before the queue is announced, since that may take the clip
straight off it), a clip leaving the queue, a save, a saved clip deleted, and a match chosen or
cleared in the match selector (a new DIFAR message, `MatchChanged`, which also refreshes the
DIFARgram's buttons). Saves and deletions commit.

## Auto-compaction

Saves and deletions in the Viewer now write at once, through the one path above. The setting to
write after every edit is therefore only still needed for buoy edits, which rematch and write
through their offline task.

## Compaction

Compaction was parked for a while because it dropped cross-match partners. That came from the
crossing tail, which version 3 removed, and it has since written Viewer clips in the step 5 test.
Deleting clips is the first use of its deletion path, so the tests below cover it.

## Tests

On the simulated data, in the Viewer:

1. Delete a clip in a crossing of two. The crossing is deleted. After saving, the query shows no
   crossing, and the clip's row is gone.
2. Delete a clip saved in the same session, before saving. It never reaches the files or database.
3. Open the pilot's version 2 files in the Viewer and delete a clip. The upgrade is offered. After it,
   the files are at version 3, the backup holds the originals, and the query shows crossings made by
   the rematch. The deleted clip is gone.
4. With auto-compaction on, save a clip, then end PAMGuard from Eclipse without closing it. The clip
   and its crossing are in the files and the query.

A crossing of three, trimmed to two and recalculated, needs the three-buoy audio.

On copies of the 2019 voyage data, each slice in its own scratch folder with the March 2019
database. The data are the ENRICH sonobuoy data set, AAS 4600, https://doi.org/10.26179/w901-b438. The counts below come from the MATLAB rematch file, and should match what the check reports.

5. 28 to 30 January: 2,284 clips, 1,470 of them sharing 709 UIDs. The upgrade reports the shared UIDs.
   Cancel: no backup folder, and the files and database unchanged. Run again and renumber: the backup
   holds the binary files and the database; the files are at version 3; UIDs run 1 to 2,284 in time
   order; the clip table has one row per clip, with the same UIDs; crossings are made.
6. 21 February: 2,413 clips stored, one of them twice (old UID 300, channel 2, 00:59:15.855); 441
   different clips sharing 206 UIDs. After renumbering the clip stored twice appears once, in the
   files and in the table.
7. 10 to 12 February: 3,876 clips, none sharing a UID, none stored twice. No question is asked, and every clip keeps its
   UID. The clip table is still rebuilt.
8. After 5, close and reopen the Viewer. Clips load with their new UIDs, crossings draw, and no
   "skipped a second copy" lines appear. Save a new clip: its UID is above 2,284. Read the rewritten
   files' footers with pgmatlab, and note their UID ranges.

## Order of work

1. Bulk rematch into crossing units. Done.
2. Deleting a saved clip. Done.
3. The dataset upgrade, which uses 1. Built, passed on the pilot.
4. Duplicate UIDs and the clip table rebuild, in the upgrade. Needed to compare with the MATLAB
   rematch of the 2019 data. The check: done. Renumbering, dropping clips stored twice, and the
   table rebuild: built, test owed.
5. The auto-compaction setting.

## Also for the feature list

- Time delays from cross-correlating the clips' audio, rather than from clip start times.
- The auto-compaction setting, above.
- The spectrogram marks clips that belong to a crossing, now that each clip can answer
  `getCrossing()`.
