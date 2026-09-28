# PAMGuard configurations

Configurations offered by the import wizard. When sound files are dropped into a
blank PAMGuard configuration, the files are scanned and the configurations here
which can be used with them are offered to the user.

Each configuration is a pair of files sharing a base name:

| File | Contents |
|---|---|
| `<name>.psfx` | the modules and their settings, exactly as saved by PAMGuard |
| `<name>.json` | what the configuration is for, and what data it needs |

## Where configurations are read from

Searched in this order, each overriding the one before it, so a user can replace
a shipped configuration by copying it into their own folder and editing it:

1. `<PAMGuard install folder>/configurations` — the ones shipped with PAMGuard
2. `<working directory>/configurations` — used when running from an IDE
3. `<user home>/Pamguard/configurations` — where you add your own

All three are optional. To add a configuration of your own, set PAMGuard up as
you want it, save the settings file, and put it in folder 3 with a JSON file of
the same base name beside it.

## The JSON file

Only `name` is really needed; everything else has a sensible default. Fields
PAMGuard does not recognise are ignored, so a configuration written for a newer
version will still load.

```json
{
  "configType": "default",
  "name": "North Atlantic right whale (deep learning)",
  "description": "What this configuration does, shown next to its name.",
  "version": "1.0",
  "author": "PAMGuard",
  "psfx": "right_whale_dl.psfx",

  "minSampleRate": 2000,
  "maxSampleRate": null,
  "targetSampleRate": 2000,
  "minChannels": 1,
  "maxChannels": null,

  "medium": "water",
  "requiredFileTypes": ["SOUND"],
  "runModes": ["NORMAL"],

  "taxa": [
    { "group": "BALEEN_WHALE",
      "species": ["North Atlantic right whale"],
      "itisCodes": [180537] }
  ],

  "decimatorUnitName": null,
  "keepRawSourceModules": ["soundtrap.STClickControl"]
}
```

| Field | Meaning |
|---|---|
| `configType` | Which loader builds the configuration. Omit it, or use `default`, for the standard behaviour. |
| `minSampleRate` / `maxSampleRate` | The range of sample rates the configuration can be used with, in Hz. There is usually no maximum, because faster data are decimated. |
| `targetSampleRate` | The rate the detectors are designed for. Data at a higher rate are decimated down to it. If omitted, the rate stored in the psfx file is used. |
| `minChannels` / `maxChannels` | Channel counts the configuration can use. |
| `medium` | `water`, `air`, or omit for either. |
| `requiredFileTypes` | What must be present among the imported files: `SOUND`, and/or `SUD_CLICKS` for SoundTrap sud files which contain click detections. |
| `runModes` | `NORMAL` and/or `VIEWER`. Defaults to normal mode. |
| `taxa` | What the configuration targets. `group` is one of `BAT`, `BALEEN_WHALE`, `SPERM_WHALE`, `BEAKED_WHALE`, `DOLPHIN`, `NBHF`, `OTHER`. The wizard shows an icon for every group and picks out the ones listed here. Name specific `species` within the group where it makes sense. |
| `decimatorUnitName` | Only set this if the psfx contains a decimator whose sole job is to set the sample rate; it is then retuned rather than a new one being inserted. Leave it out if the decimator is part of what the configuration does, as it usually is. |
| `keepRawSourceModules` | Modules which must keep reading full rate data from Sound Acquisition when a decimator is inserted. Defaults to the SoundTrap click detector. |

## What happens when a configuration is chosen

1. The psfx is read and its modules are listed.
2. If the imported data are faster than `targetSampleRate`, a decimator is added
   after Sound Acquisition and everything that was reading the acquisition data
   is repointed at it — except the modules in `keepRawSourceModules`.
3. The modules are created and their settings loaded.
4. Sound Acquisition is pointed at the imported files.
5. The binary store and database are moved to the folder the user chose.
6. Anything that did not reconnect is reported to the user.

## Packaging

This folder ships with PAMGuard and must end up beside the installed jar, since
that is where `PamController.getInstallFolder()` looks.

* **Linux** — handled by the `jdeb` `dataSet` in `pom.xml` (`linux-profile`),
  which installs it to `/usr/share/pamguard/configurations`.
* **macOS** — handled by `build/macos/build_and_sign.sh`, which copies it into
  `PAMGuard.app/Contents/` before the bundle is signed.
* **Windows** — the installer is built outside this repository, so it needs
  changing there: ship `configurations\` into the install directory alongside
  the jar, the same way `WMM.COF` and `lib64\` are shipped.

If the folder is missing PAMGuard carries on quite happily; the import wizard
simply offers the blank configuration and nothing else.
