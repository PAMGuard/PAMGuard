# DIFAR: editing in the Viewer

Brian Miller, Australian Antarctic Division. Branch `difar-crossings`, 27 September 2026.

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

Files at versions 0 to 2 are read only. The first edit to an old dataset in the Viewer, a saved or
deleted clip, offers to upgrade the whole dataset, and edits are refused until it is upgraded. The
upgrade:

1. Backs up every DIFAR binary file, as compaction already does.
2. Rewrites them at version 3, with no crossing tail.
3. Rematches: in time order, each clip gets the best viable match, chosen automatically, and the
   crossing is recorded by the same `CrossingRecorder` rules as saving a clip.

The old crossings were made by buggy code, so they stay in the backup only, as a record of what was
thought at the time. The new crossings are the best current estimate.

The whole dataset is upgraded at once because matching looks for partner clips close in time, which
may sit in the next file. Upgrading one file at a time would miss those, and leave a dataset in mixed
versions.

This needs bulk rematch, below, which is built first.

## Bulk rematch

The DIFAR offline tasks rematched every clip over a period until step 4, which changed that task to
relocate existing crossings only. "Rematch clips" restores it, making crossing units, and replaces the
relocating task: a buoy change can make old pairings wrong, so after a buoy edit matches are chosen
again, not just moved. It runs from the DIFAR offline tasks dialog, and automatically after a buoy
edit in the Viewer, over the buoy's period.

The offline task group loads one file of clips at a time. A clip near the start or end of a file
cannot see partners in the next file, as with the old rematch. Loading a margin of clips either side
would fix that, and is worth doing if files are short. Within a file, clips are matched in time order: each gets the best viable match, chosen
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

## Auto-compaction

By default, edits are written when the Viewer saves: on File, Save, before loading new data, and on
exit. A setting, off by default, writes after every edit instead: each saved or deleted clip, and each
buoy edit, is followed at once by compaction, the crossing changes it caused, and a database commit.
It writes more often, but a crash then loses at most the edit in progress.

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

## Order of work

1. Bulk rematch into crossing units. Done.
2. Deleting a saved clip. Done.
3. The dataset upgrade, which uses 1.
4. The auto-compaction setting.

## Also for the feature list

- Time delays from cross-correlating the clips' audio, rather than from clip start times.
- The auto-compaction setting, above.
- The spectrogram marks clips that belong to a crossing, now that each clip can answer
  `getCrossing()`.
