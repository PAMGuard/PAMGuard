#!/bin/bash

# macOS App Signing & Notarization Script for PAMGuard
# This script signs all binaries (including those inside JARs), removes i386 architectures,
# and prepares the app for notarization by Apple.

# Note: We don't use "set -e" because we want to continue even if individual
# signing operations fail, as long as the main app bundle signs successfully

# Load credentials
source "$(dirname "$0")/apple_creds.sh"

# --- CONFIGURATION ---
SHOULD_NOTARIZE=true  
BASE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
TARGET_DIR="$BASE_DIR/target"
ENTITLEMENTS="$(dirname "$0")/entitlements.plist"

APP_PATH=$(ls -d "$TARGET_DIR"/*.app | head -n 1)

if [ -z "$APP_PATH" ]; then
    echo "Error: No .app file found in $TARGET_DIR"
    exit 1
fi

APP_NAME=$(basename "$APP_PATH")
NAME_ONLY="${APP_NAME%.*}"
DMG_PATH="$TARGET_DIR/${NAME_ONLY}-signed.dmg"
BUNDLE_DIR="$TARGET_DIR/bundle"

echo "=========================================="
echo "Starting Signing Process for: $APP_NAME"
echo "Certificate: $CERT_NAME"
echo "=========================================="

# 0. CONFIGURE APP FOR MULTIPLE INSTANCES
echo ""
echo "--- Configuring app to allow multiple instances ---"
INFO_PLIST="$APP_PATH/Contents/Info.plist"

if [ -f "$INFO_PLIST" ]; then
    # Add LSMultipleInstancesProhibited key set to false
    # This allows right-click > "New Instance" in Finder
    /usr/libexec/PlistBuddy -c "Add :LSMultipleInstancesProhibited bool false" "$INFO_PLIST" 2>/dev/null || \
    /usr/libexec/PlistBuddy -c "Set :LSMultipleInstancesProhibited false" "$INFO_PLIST" 2>/dev/null
    
    echo "✅ Multiple instances enabled"
    echo "   Users can now right-click PAMGuard icon and select 'New Instance'"
else
    echo "⚠️  Warning: Info.plist not found at $INFO_PLIST"
fi

echo ""

# Function to process and sign a native library
process_native_lib() {
    local LIB="$1"
    local MODIFIED=false
    
    if [ ! -f "$LIB" ]; then
        return
    fi
    
    # Check if this is a Mach-O binary
    if ! file "$LIB" | grep -q "Mach-O"; then
        return
    fi
    
    # Check if this is an i386-only binary (needs to be deleted entirely)
    if lipo -info "$LIB" 2>/dev/null | grep -q "Non-fat file"; then
        ARCH=$(lipo -info "$LIB" 2>/dev/null | grep "Non-fat" | awk '{print $NF}')
        if [ "$ARCH" = "i386" ]; then
            echo "    ❌ Removing i386-only: $(basename "$LIB")"
            rm "$LIB"
            return
        fi
    fi
    
    # Check if this is a fat binary containing i386
    if lipo -info "$LIB" 2>/dev/null | grep -q "i386"; then
        echo "    🔧 Stripping i386 from: $(basename "$LIB")"
        lipo "$LIB" -remove i386 -output "${LIB}.tmp" 2>/dev/null
        if [ $? -eq 0 ]; then
            mv "${LIB}.tmp" "$LIB"
            MODIFIED=true
        else
            rm -f "${LIB}.tmp"
        fi
    fi

    # Sign the binary with runtime hardening and timestamp
    if [ -f "$LIB" ]; then
        echo "    ✍️  Signing: $(basename "$LIB")"
        SIGN_OUTPUT=$(codesign --force --options runtime --timestamp --sign "$CERT_NAME" "$LIB" 2>&1)
        SIGN_RESULT=$?
        if [ $SIGN_RESULT -eq 0 ]; then
            MODIFIED=true
        else
            echo "    ⚠️  WARNING: Failed to sign $(basename "$LIB")"
            echo "$SIGN_OUTPUT" | grep -v "replacing existing signature" | sed 's/^/        /'
        fi
    fi
    
    # Return status through exit code
    if [ "$MODIFIED" = true ]; then
        return 0
    else
        return 1
    fi
}

# 1. PROCESS AND SIGN BINARIES INSIDE JARs
echo ""
echo "=========================================="
echo "STEP 1: Processing JARs"
echo "=========================================="

# Find all JARs and create a list
JAR_LIST=$(find "$APP_PATH/Contents/Java" -name "*.jar")

for JAR_PATH in $JAR_LIST; do
    # Check if JAR contains native libraries
    if unzip -l "$JAR_PATH" 2>/dev/null | grep -qE "\.dylib|\.jnilib|\.so"; then
        echo ""
        echo "📦 Processing: $(basename "$JAR_PATH")"
        
        TMP_DIR=$(mktemp -d)
        unzip -q "$JAR_PATH" -d "$TMP_DIR"
        
        MODIFIED=false
        
        # Find all native libraries
        while IFS= read -r LIB; do
            if process_native_lib "$LIB"; then
                MODIFIED=true
            fi
        done < <(find "$TMP_DIR" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \))
        
        # Rebuild JAR if modified
        if [ "$MODIFIED" = true ]; then
            echo "  📝 Rebuilding JAR..."
            rm -f "$JAR_PATH"
            (cd "$TMP_DIR" && zip -r -q "$JAR_PATH" .)
        else
            echo "  ✅ No changes needed"
        fi
        
        rm -rf "$TMP_DIR"
    fi
done

# Remove libraries compiled with old SDKs (Apple rejects SDK < 10.9)
echo ""
echo "Removing libraries compiled with old SDKs..."

# List of old SDK binaries to remove (can be in separate JARs or the main fat JAR)
OLD_SDK_BINARIES=(
    "netlib-native_ref-osx-x86_64.jnilib"
    "netlib-native_system-osx-x86_64.jnilib"
)

# Search all JARs (including the main PAMGuard fat JAR)
echo "Scanning for JARs..."
ALL_JARS=$(find "$APP_PATH/Contents/Java" -name "*.jar" -type f)
TOTAL_JARS=$(echo "$ALL_JARS" | wc -l | tr -d ' ')
CURRENT_JAR=0

# JARs to skip (known to be large and not contain the old SDK binaries we're looking for)
SKIP_JARS=(
    "datasets-"
    "stats-"
    "graphics-"
    "grDevices-"
)

echo "Found $TOTAL_JARS JARs to check"
echo ""

for JAR_PATH in $ALL_JARS; do
    ((CURRENT_JAR++))
    JAR_NAME=$(basename "$JAR_PATH")
    
    # Check if this JAR should be skipped
    SKIP=false
    for SKIP_PATTERN in "${SKIP_JARS[@]}"; do
        if [[ "$JAR_NAME" == *"$SKIP_PATTERN"* ]]; then
            echo "[$CURRENT_JAR/$TOTAL_JARS] Skipping: $JAR_NAME (Renjin data, no native code)"
            SKIP=true
            break
        fi
    done
    
    if [ "$SKIP" = true ]; then
        continue
    fi
    
    JAR_SIZE=$(du -h "$JAR_PATH" | awk '{print $1}')
    echo -n "[$CURRENT_JAR/$TOTAL_JARS] Checking: $JAR_NAME ($JAR_SIZE) ... "
    
    # Check if JAR file is actually readable
    if [ ! -r "$JAR_PATH" ]; then
        echo "⚠️  Not readable, skipping"
        continue
    fi
    
    TMP_DIR=$(mktemp -d)
    
    # Extract JAR - capture any errors
    UNZIP_OUTPUT=$(unzip -q "$JAR_PATH" -d "$TMP_DIR" 2>&1)
    UNZIP_EXIT=$?
    
    if [ $UNZIP_EXIT -ne 0 ]; then
        # Check if it's because the JAR is empty or corrupted
        if echo "$UNZIP_OUTPUT" | grep -q "End-of-central-directory signature not found"; then
            echo "⚠️  Corrupted/empty JAR, skipping"
        else
            echo "⚠️  Extraction failed (exit $UNZIP_EXIT), skipping"
        fi
        rm -rf "$TMP_DIR"
        continue
    fi
    
    MODIFIED=false
    
    # Check for and remove old SDK binaries (with timeout on find for safety)
    for BINARY_NAME in "${OLD_SDK_BINARIES[@]}"; do
        # Find with timeout to handle very large directory trees
        FOUND_BINARIES=$(find "$TMP_DIR" -name "$BINARY_NAME" -type f 2>/dev/null)
        if [ -n "$FOUND_BINARIES" ]; then
            if [ "$MODIFIED" = false ]; then
                echo ""
                echo "  🎯 FOUND old SDK binaries!"
            fi
            while IFS= read -r BINARY; do
                echo "    ❌ Removing: $(basename "$BINARY")"
                rm "$BINARY"
                MODIFIED=true
            done <<< "$FOUND_BINARIES"
        fi
    done
    
    # Rebuild JAR if we removed anything
    if [ "$MODIFIED" = true ]; then
        echo "    📝 Rebuilding JAR..."
        rm -f "$JAR_PATH"
        (cd "$TMP_DIR" && zip -r -q "$JAR_PATH" . 2>/dev/null)
        echo "    ✅ Done - rebuilt without old SDK binaries"
    else
        echo "OK"
    fi
    
    rm -rf "$TMP_DIR"
done

echo ""
echo "Old SDK library scan complete"


# 2. SIGN ALL LOOSE DYLIBS AND BINARIES
echo ""
echo "=========================================="
echo "STEP 2: Signing loose libraries"
echo "=========================================="

# Clear extended attributes (suppress permission errors on read-only files)
echo "Clearing extended attributes..."
xattr -cr "$APP_PATH" 2>/dev/null || true
# Make files writable if needed for signing
chmod -R u+w "$APP_PATH" 2>/dev/null || true

# Sign all dylibs and jnilib files not in JARs
echo "Looking for .dylib and .jnilib files..."
while IFS= read -r LIB; do
    process_native_lib "$LIB" || true
done < <(find "$APP_PATH" -type f \( -name "*.dylib" -o -name "*.jnilib" \) | grep -v "\.jar")

# 3. SIGN JAVA RUNTIME
echo ""
echo "=========================================="
echo "STEP 3: Signing Java Runtime"
echo "=========================================="

# Sign all executables in the JRE
if [ -d "$APP_PATH/Contents/runtime/Contents/Home/bin" ]; then
    echo "Signing JRE executables..."
    while IFS= read -r BIN; do
        if file "$BIN" | grep -q "executable"; then
            echo "  ✍️  $(basename "$BIN")"
            SIGN_OUTPUT=$(codesign --force --options runtime --timestamp --sign "$CERT_NAME" "$BIN" 2>&1)
            if [ $? -ne 0 ]; then
                echo "  ⚠️  WARNING: Failed to sign $(basename "$BIN")"
                echo "$SIGN_OUTPUT" | grep -v "replacing existing signature" | sed 's/^/      /'
            fi
        fi
    done < <(find "$APP_PATH/Contents/runtime/Contents/Home/bin" -type f)
fi

# Sign any dylibs in the JRE
if [ -d "$APP_PATH/Contents/runtime" ]; then
    echo "Signing JRE libraries..."
    while IFS= read -r LIB; do
        process_native_lib "$LIB" || true
    done < <(find "$APP_PATH/Contents/runtime" -type f \( -name "*.dylib" -o -name "*.jnilib" \))
fi

# Sign the Java runtime bundle itself
if [ -d "$APP_PATH/Contents/runtime" ]; then
    echo "Signing embedded Java runtime bundle..."
    codesign --force --options runtime --timestamp --sign "$CERT_NAME" "$APP_PATH/Contents/runtime"
fi

# 4. SIGN FRAMEWORKS (if any)
if [ -d "$APP_PATH/Contents/Frameworks" ]; then
    echo ""
    echo "=========================================="
    echo "STEP 4: Signing Frameworks"
    echo "=========================================="
    while IFS= read -r FRAMEWORK; do
        echo "  ✍️  $(basename "$FRAMEWORK")"
        codesign --force --options runtime --timestamp --sign "$CERT_NAME" "$FRAMEWORK"
    done < <(find "$APP_PATH/Contents/Frameworks" -name "*.framework" -o -name "*.dylib")
fi

# 5. SIGN THE MAIN APP BUNDLE
echo ""
echo "=========================================="
echo "STEP 5: Signing main .app bundle"
echo "=========================================="
codesign --force --options runtime --timestamp --sign "$CERT_NAME" --entitlements "$ENTITLEMENTS" "$APP_PATH"

# Verify the signature
echo ""
echo "Verifying signature..."
if codesign --verify --deep --strict --verbose=2 "$APP_PATH" 2>&1; then
    echo "✅ Signature verification successful"
else
    echo "❌ Signature verification failed"
    exit 1
fi

# 6. CREATE DMG
echo ""
echo "=========================================="
echo "STEP 6: Creating DMG"
echo "=========================================="
mkdir -p "$BUNDLE_DIR"

# Use rsync instead of cp to handle permissions better
echo "Copying app to bundle directory..."
rsync -a --chmod=u+w "$APP_PATH/" "$BUNDLE_DIR/$(basename "$APP_PATH")/"

ln -s /Applications "$BUNDLE_DIR/Applications"
rm -f "$DMG_PATH"

echo "Creating DMG..."
hdiutil create -srcfolder "$BUNDLE_DIR" -volname "PAMGuard Installer" -fs HFS+ -o "$DMG_PATH"
rm -rf "$BUNDLE_DIR"

# 7. SIGN DMG
echo ""
echo "=========================================="
echo "STEP 7: Signing DMG"
echo "=========================================="
codesign --force --timestamp --sign "$CERT_NAME" "$DMG_PATH"

# Verify DMG signature
if codesign --verify --verbose "$DMG_PATH" 2>&1; then
    echo "✅ DMG signature verified"
else
    echo "❌ DMG signature verification failed"
    exit 1
fi

# 8. NOTARIZATION
echo ""
echo "=========================================="
echo "STEP 8: Notarization"
echo "=========================================="
echo "Notarization setting: SHOULD_NOTARIZE=$SHOULD_NOTARIZE"

if [ "$SHOULD_NOTARIZE" = true ]; then
    echo "Submitting to Apple (this may take several minutes)..."
    
    SUBMISSION_OUTPUT=$(xcrun notarytool submit "$DMG_PATH" \
        --apple-id "$APPLE_ID" \
        --password "$APPLE_PASSWORD" \
        --team-id "$TEAM_ID" \
        --wait)
    
    echo "$SUBMISSION_OUTPUT"
    
    # Extract submission ID for logging
    SUBMISSION_ID=$(echo "$SUBMISSION_OUTPUT" | grep "id:" | head -1 | awk '{print $2}')
    
    # Check if notarization succeeded
    if echo "$SUBMISSION_OUTPUT" | grep -q "status: Accepted"; then
        echo "✅ Notarization successful!"
        
        echo ""
        echo "Stapling ticket to DMG..."
        for i in {1..6}; do
            if xcrun stapler staple "$DMG_PATH"; then
                echo "✅ Ticket stapled successfully"
                break
            else
                echo "⏳ Waiting for CloudKit propagation... ($i/6)"
                sleep 30
            fi
        done
    else
        echo "❌ Notarization failed!"
        echo ""
        echo "To view the full notarization log, run:"
        echo "xcrun notarytool log $SUBMISSION_ID --apple-id \"$APPLE_ID\" --password \"$APPLE_PASSWORD\" --team-id \"$TEAM_ID\""
        exit 1
    fi
else
    echo "⏭️  Notarization skipped (SHOULD_NOTARIZE=false)"
    echo ""
    echo "To enable notarization, edit the script and set:"
    echo "SHOULD_NOTARIZE=true"
fi

# 9. FINAL VERIFICATION
echo ""
echo "=========================================="
echo "STEP 9: Final Gatekeeper Check"
echo "=========================================="
if spctl --assess --type install --verbose "$DMG_PATH" 2>&1; then
    echo "✅ Gatekeeper assessment passed"
else
    echo "⚠️  Gatekeeper assessment returned warnings (may still work)"
fi

echo ""
echo "=========================================="
echo "✅ SUCCESS!"
echo "=========================================="
echo "Generated: $DMG_PATH"
echo ""
