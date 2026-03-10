#!/bin/bash

# Load credentials
source "$(dirname "$0")/apple_creds.sh"

# --- CONFIGURATION ---
#BASE_DIR="/Users/jdjm/git/PAMGuard"

SHOULD_NOTARIZE=true  # Set to false to skip Apple's security upload

# This finds the root by going up two levels from build/macos/
BASE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
TARGET_DIR="$BASE_DIR/target"

# Path to the entitlements file (assumes it's in the same folder as this script)
ENTITLEMENTS="$(dirname "$0")/entitlements.plist"

# 1. Automatically find the .app in the target folder
# This finds the first .app file it encounters
APP_PATH=$(ls -d "$TARGET_DIR"/*.app | head -n 1)
APP_NAME=$(basename "$APP_PATH")           # e.g., "Pamguard.app"
NAME_ONLY="${APP_NAME%.*}"                # e.g., "Pamguard"

# 2. Define the DMG path based on the app name
DMG_PATH="$TARGET_DIR/${NAME_ONLY}-signed.dmg"
BUNDLE_DIR="$BASE_DIR/target/bundle"

echo "$APP_PATH"
echo "$DMG_PATH"

echo "--- Starting Signing Process ---"

# Sign all .dylib and .so files inside the app first
# This ensures every part of the Amazon Corretto JRE is signed with your identity
find "$APP_PATH" -name "*.dylib" -or -name "*.so" | xargs codesign --force --options runtime --sign "$CERT_NAME" --entitlements "$ENTITLEMENTS" --timestamp

# 1. Clean and Sign the .app
xattr -cr "$APP_PATH"
#codesign --force --options runtime --sign "$CERT_NAME" "$APP_PATH"
codesign --force --options runtime --sign "$CERT_NAME" --entitlements "$ENTITLEMENTS" --timestamp "$APP_PATH"

# 2. Prepare the Bundle Folder
mkdir -p "$BUNDLE_DIR"
cp -r "$APP_PATH" "$BUNDLE_DIR/"

# This creates a symlink so the folder appears inside the DMG
ln -s /Applications "$BUNDLE_DIR/Applications"

# 3. Create the DMG
rm -f "$DMG_PATH"
hdiutil create -srcfolder "$BUNDLE_DIR" -volname "PAMGuard Install" -fs HFS+ -o "$DMG_PATH"

# Cleanup the bundle folder (including the symlink)
rm -rf "$BUNDLE_DIR"

# 4. Sign the DMG
echo "--- Signing the DMG ---"
codesign --force --sign "$CERT_NAME" "$DMG_PATH"

if [ "$SHOULD_NOTARIZE" = true ]; then
   # 5. Notarize
   echo "--- Submitting for Notarization (Waiting for Apple...) ---"
   xcrun notarytool submit "$DMG_PATH" \
        --apple-id "$APPLE_ID" \
        --password "$APPLE_PASSWORD" \
        --team-id "$TEAM_ID" \
        --wait

   # 6. Staple the Ticket
   echo "--- Waiting 30s for ticket propagation ---"
   sleep 30
   xcrun stapler staple "$DMG_PATH"
else
   echo "--- Skipping Notarization as requested ---"
fi

# 7. Final Verification
spctl --assess --type install --verbose "$DMG_PATH"

echo "Done! $DMG_PATH is ready for website distribution."