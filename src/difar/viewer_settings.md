# DIFAR in the Viewer: settings that must be right

Brian Miller, Australian Antarctic Division. Branch `difar-crossings`, started 30 September 2026.

Settings the Viewer needs for DIFAR reanalysis, gathered as they are found. Several look like faults
when missed. Check this list before reporting a bug. It will go into the help page
`src/help/localisation/difar/difarLocalisation/docs/difar_PostProcessingTutorial.html` before the
pull request.

## Opening a dataset

- **Choose the dataset's own database.** The Viewer offers the last database used. Opening a slice or
  copy with another dataset's database pairs its binary files with the wrong clip rows and crossings.
  Symptom: counts that belong to another dataset, such as crossings in a database that should have
  none.

## Map

- **Tick "all data" in the map's plot options for buoys to appear.** Without it the map draws only
  buoy records inside the view's time window, and a buoy deployed before the view starts is not
  drawn. Symptom: no buoys on the map, though bearings and crossings load.

## Buoy manager

- **Open the bottom panel for the time window controls.** It starts collapsed. Symptom: no way to
  change which buoys are listed.

## Offline tasks

- **Run "Upgrade old DIFAR files to version 3" over all data.** The upgrade refuses any other data
  choice, since matching must find partners anywhere in the dataset.
