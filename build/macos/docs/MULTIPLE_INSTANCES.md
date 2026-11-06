# Multiple Instances and File Associations (macOS)

## Opening another instance of PAMGuard

macOS only ever brings the running copy of an app to the front when its icon is
double clicked in the Finder or Applications folder, and the Finder has no "New
Instance" menu item. `LSMultipleInstancesProhibited = false` allows a second copy to
run, but something still has to ask for one. PAMGuard does this from the Dock:

- Right click (or hold) the PAMGuard icon in the Dock and choose
  **Open additional instance of PAMGuard**.

This runs `open -n PAMGuard.app`, so the new copy starts with the normal start up
dialog. Each instance gets its own Dock icon. From Terminal the same thing is:

```bash
open -n /Applications/PAMGuard.app
```

The Dock menu is added in `src/pamguard/MacOSIntegration.java`.

## File associations

`configure_info_plist.sh` (run by `build_and_sign.sh` before signing) adds these to
`Info.plist`, because the macosappbundler plugin can't write them:

| File | LSHandlerRank | What happens |
|------|---------------|--------------|
| `.psfx` | Owner | Double click opens PAMGuard in **normal mode** with that configuration |
| `.sqlite3`, `.sqlite` | Alternate | PAMGuard is listed under **Open With** but is not made the default. Opening one with PAMGuard starts **viewer mode** on that database |

It also exports the `org.pamguard.psfx` file type and sets
`LSMultipleInstancesProhibited` to false. `CFBundleIdentifier` (`org.pamguard.PAMGuard`)
is set in the pom.

Note: Alternate means PAMGuard never replaces another app's claim on `.sqlite3`. On a
Mac with no other SQLite app installed, macOS may still use PAMGuard for a double
click because it is the only app that can open the file.

### How a file reaches PAMGuard

macOS doesn't put the file on the command line. It sends an "open file" event,
which `MacOSIntegration` handles:

- **PAMGuard not running:** the file that launched it is picked up at start up
  (PAMGuard waits up to a second for the event), the start up dialog is skipped and
  the run mode comes from the file type.
- **PAMGuard already running:** a new instance is launched for the file with
  `open -n PAMGuard.app --args -openfile <file>`, so the running session is left alone.

The `-openfile <file>` option also works on the command line on any platform.

## Checking a build

```bash
/usr/libexec/PlistBuddy -c "Print :CFBundleDocumentTypes" /Applications/PAMGuard.app/Contents/Info.plist
# After installing, re-register with Launch Services if Open With looks stale:
/System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister -f /Applications/PAMGuard.app
```
