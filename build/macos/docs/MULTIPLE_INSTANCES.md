# Multiple Instances Feature

## What This Does

Enables users to launch multiple instances of PAMGuard from the Finder by right-clicking on the app icon.

## How It Works

The script modifies the `Info.plist` file in the app bundle to set:
```xml
<key>LSMultipleInstancesProhibited</key>
<false/>
```

This tells macOS that the application **allows** multiple instances to run simultaneously.

## User Experience

### Before (Default macOS Behavior)
When you have PAMGuard already running and try to open it again:
- Double-clicking the icon brings the existing window to front
- No way to launch a second instance from Finder
- Must use command line: `open -n PAMGuard.app`

### After (With This Feature)
When you right-click the PAMGuard icon in Finder:
```
PAMGuard
├── Open
├── Show Package Contents
├── New Instance          ← NEW OPTION!
├── Move to Trash
└── Get Info
```

Selecting "New Instance" launches a completely separate instance of PAMGuard.

## Technical Details

### What Gets Modified
- **File:** `PAMGuard.app/Contents/Info.plist`
- **Key:** `LSMultipleInstancesProhibited`
- **Value:** `false`

### When It Happens
- **Step 0** of the build_and_sign.sh script
- Before any signing occurs
- Applied to the .app bundle

### Implementation
Uses macOS built-in `PlistBuddy` utility:
```bash
/usr/libexec/PlistBuddy -c "Add :LSMultipleInstancesProhibited bool false" Info.plist
```

If the key already exists, it updates it:
```bash
/usr/libexec/PlistBuddy -c "Set :LSMultipleInstancesProhibited false" Info.plist
```

## Use Cases for PAMGuard

This is particularly useful for PAMGuard users who need to:
- Monitor multiple audio streams simultaneously
- Run different configurations side-by-side
- Compare live and recorded data
- Test different settings in parallel
- Run viewer mode while recording

## Testing

After running `build_and_sign.sh`:

1. **Install the signed DMG**
2. **Launch PAMGuard** normally (double-click)
3. **Right-click the PAMGuard icon** in Finder (not the Dock!)
4. **Look for "New Instance"** in the context menu
5. **Click "New Instance"** to launch a second PAMGuard

Both instances will run independently with separate windows and configurations.

## Verifying the Setting

Check if it's enabled in the signed app:
```bash
/usr/libexec/PlistBuddy -c "Print :LSMultipleInstancesProhibited" \
    /Applications/PAMGuard.app/Contents/Info.plist
```

Should print: `false`

If the key doesn't exist, macOS defaults to allowing multiple instances, but setting it explicitly to `false` ensures the context menu item appears.

## Alternative: Command Line

Users can also launch multiple instances from Terminal:
```bash
# Launch a new instance (works even without this feature)
open -n /Applications/PAMGuard.app

# Launch with arguments
open -n /Applications/PAMGuard.app --args -v
```

But the right-click menu option is much more user-friendly!

## Disabling This Feature

If you want to go back to single-instance behavior, edit `build_and_sign.sh` and comment out the plist modification:

```bash
# /usr/libexec/PlistBuddy -c "Add :LSMultipleInstancesProhibited bool false" "$INFO_PLIST"
```

Or set it to `true`:
```bash
/usr/libexec/PlistBuddy -c "Set :LSMultipleInstancesProhibited true" "$INFO_PLIST"
```

## Integration with Signing

This modification happens **before** signing, so:
- ✅ The Info.plist is modified first
- ✅ Then the entire app bundle is signed
- ✅ The signature includes the modified plist
- ✅ Apple's notarization sees the modified plist

This ensures the feature works correctly in the distributed, notarized app.

## Output

When running the script, you'll see:
```
--- Configuring app to allow multiple instances ---
✅ Multiple instances enabled
   Users can now right-click PAMGuard icon and select 'New Instance'
```

## Compatibility

- **macOS Version:** All supported macOS versions
- **Dock Behavior:** The Dock icon still shows one running app (but with multiple windows)
- **Resource Usage:** Each instance uses separate memory and resources
- **Data Files:** Each instance can open different data files
- **Settings:** Each instance has independent settings (unless using shared config files)

## Best Practices for Users

When running multiple instances:
- Each instance should use different sound cards (if recording)
- Monitor system resources (CPU, memory)
- Use different configuration files to avoid conflicts
- Be aware that both instances access the same PAMGuard settings directory

## Summary

✅ **Simple:** Just 3 lines added to the script
✅ **User-Friendly:** Adds right-click menu option
✅ **Safe:** Applied before signing
✅ **Compatible:** Works with all macOS versions
✅ **Useful:** Enables parallel PAMGuard workflows

The feature is now enabled by default in all builds! 🎉
