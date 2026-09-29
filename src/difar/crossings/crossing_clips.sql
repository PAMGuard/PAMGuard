-- DIFAR crossings with their clips: one row per clip in each crossing.
--
-- The crossing's location and errors come from the crossing table. Each clip's
-- time, buoy and bearings come from the clip's own row, which is the only copy
-- of anything derived from the buoy and is kept current after buoy edits. The
-- children table only links the two.
--
-- Clip rows are matched by UID and by time, within a second. Older datasets
-- can give different clips the same UID, so UID alone can join the wrong rows.
--
-- Text columns are trimmed, since PAMGuard pads text with spaces when it
-- inserts a row but not when it updates one.
--
-- Table names follow the DIFAR module's name. These are for a module named
-- "DIFAR Localisation"; change the three names if yours differs.
--
-- From WSL, to CSV:
--   sqlite3 -header -csv database.sqlite3 < crossing_clips.sql > crossing_clips.csv

SELECT
    x.UID           AS CrossingUID,
    x.UTC           AS CrossingStart,
    x.ClipCount,
    x.Latitude      AS CrossingLatitude,
    x.Longitude     AS CrossingLongitude,
    x.XError,
    x.YError,
    TRIM(x.MatchChoice) AS MatchChoice,
    c.UID           AS ClipUID,
    c.UTC           AS ClipTime,
    c.Channel,
    TRIM(c.BuoyName) AS BuoyName,
    c.DeploymentUID,
    c.BuoyLatitude,
    c.BuoyLongitude,
    c.BuoyHeading,
    c.DIFARBearing,
    c.TrueBearing,
    c.DifarFrequency,
    TRIM(c.Species) AS Species
FROM DIFAR_Localisation_Crossings AS x
JOIN DIFAR_Localisation_Crossings_Children AS k ON k.parentUID = x.UID
JOIN DIFAR_Localisation AS c ON c.UID = k.UID
    AND ABS(julianday(c.UTC) - julianday(k.UTC)) < 1.0 / 86400
ORDER BY x.UTC, x.UID, c.UTC;
