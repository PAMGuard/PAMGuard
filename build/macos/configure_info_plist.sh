#!/bin/bash
#
# Adds the macOS desktop integration keys to PAMGuard.app/Contents/Info.plist.
# The macosappbundler maven plugin can't write these keys, so this runs on the
# bundled app before it is signed (called from build_and_sign.sh).
#
#  - .psfx settings files belong to PAMGuard (LSHandlerRank Owner). Double
#    clicking one starts PAMGuard in normal mode with that configuration.
#  - .sqlite3 / .sqlite databases list PAMGuard under "Open With" but PAMGuard
#    never takes over as their default app (LSHandlerRank Alternate). Opening one
#    with PAMGuard starts viewer mode on that database.
#  - Multiple instances are allowed (LSMultipleInstancesProhibited false). A new
#    instance is started from "Open additional instance of PAMGuard" in the Dock
#    icon's menu, which runs "open -n". Double clicking the app in the Finder
#    while PAMGuard is running only brings the running copy to the front; that
#    is fixed macOS behaviour.
#
# Usage: configure_info_plist.sh /path/to/PAMGuard.app
# The script can be run more than once on the same app.

APP_PATH="$1"
INFO_PLIST="$APP_PATH/Contents/Info.plist"
PB=/usr/libexec/PlistBuddy

if [ ! -f "$INFO_PLIST" ]; then
    echo "⚠️  Warning: Info.plist not found at $INFO_PLIST"
    exit 1
fi

# Remove keys this script owns so that it can be run again.
for KEY in LSMultipleInstancesProhibited CFBundleDocumentTypes UTExportedTypeDeclarations; do
    $PB -c "Delete :$KEY" "$INFO_PLIST" 2>/dev/null
done

# A bundle identifier is needed for Launch Services to register the file types.
if ! $PB -c "Print :CFBundleIdentifier" "$INFO_PLIST" >/dev/null 2>&1; then
    $PB -c "Add :CFBundleIdentifier string org.pamguard.PAMGuard" "$INFO_PLIST"
fi

# Allow more than one copy of PAMGuard to run.
$PB -c "Add :LSMultipleInstancesProhibited bool false" "$INFO_PLIST"

# Declare the PAMGuard settings file type.
$PB -c "Add :UTExportedTypeDeclarations array" \
    -c "Add :UTExportedTypeDeclarations:0 dict" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeIdentifier string org.pamguard.psfx" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeDescription string PAMGuard settings file" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeConformsTo array" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeConformsTo:0 string public.data" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeTagSpecification dict" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeTagSpecification:public.filename-extension array" \
    -c "Add :UTExportedTypeDeclarations:0:UTTypeTagSpecification:public.filename-extension:0 string psfx" \
    "$INFO_PLIST"

# Document types: psfx owned by PAMGuard, sqlite only offered under "Open With".
$PB -c "Add :CFBundleDocumentTypes array" \
    -c "Add :CFBundleDocumentTypes:0 dict" \
    -c "Add :CFBundleDocumentTypes:0:CFBundleTypeName string PAMGuard settings file" \
    -c "Add :CFBundleDocumentTypes:0:CFBundleTypeRole string Editor" \
    -c "Add :CFBundleDocumentTypes:0:LSHandlerRank string Owner" \
    -c "Add :CFBundleDocumentTypes:0:LSItemContentTypes array" \
    -c "Add :CFBundleDocumentTypes:0:LSItemContentTypes:0 string org.pamguard.psfx" \
    -c "Add :CFBundleDocumentTypes:1 dict" \
    -c "Add :CFBundleDocumentTypes:1:CFBundleTypeName string SQLite database" \
    -c "Add :CFBundleDocumentTypes:1:CFBundleTypeRole string Viewer" \
    -c "Add :CFBundleDocumentTypes:1:LSHandlerRank string Alternate" \
    -c "Add :CFBundleDocumentTypes:1:CFBundleTypeExtensions array" \
    -c "Add :CFBundleDocumentTypes:1:CFBundleTypeExtensions:0 string sqlite3" \
    -c "Add :CFBundleDocumentTypes:1:CFBundleTypeExtensions:1 string sqlite" \
    "$INFO_PLIST"

if plutil -lint "$INFO_PLIST" >/dev/null; then
    echo "✅ Info.plist configured: multiple instances, .psfx (owner), .sqlite3 (Open With only)"
else
    echo "❌ Info.plist is not valid after configuration"
    exit 1
fi
