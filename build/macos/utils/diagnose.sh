#!/bin/bash

# Diagnostic script to check the state of PAMGuard.app before/after signing
# Usage: ./diagnose.sh [path-to-app]

APP_PATH="${1:-../../target/PAMGuard.app}"

if [ ! -d "$APP_PATH" ]; then
    echo "❌ App not found: $APP_PATH"
    echo "Usage: $0 [path-to-app]"
    exit 1
fi

echo "=========================================="
echo "PAMGuard App Diagnostic Report"
echo "App: $APP_PATH"
echo "=========================================="
echo ""

# 1. Check for i386 binaries
echo "1. Checking for i386 (32-bit) binaries..."
echo "   (These will cause notarization to fail)"
echo ""
I386_COUNT=0
find "$APP_PATH" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \) | while read -r LIB; do
    if lipo -info "$LIB" 2>/dev/null | grep -q "i386"; then
        echo "   ❌ i386 found: ${LIB#$APP_PATH/}"
        ((I386_COUNT++))
    fi
done

if [ $I386_COUNT -eq 0 ]; then
    echo "   ✅ No i386 binaries found (good!)"
fi
echo ""

# 2. Check JARs with native libraries
echo "2. JARs containing native libraries:"
echo ""
find "$APP_PATH/Contents/Java" -name "*.jar" | while read -r JAR_PATH; do
    if unzip -l "$JAR_PATH" 2>/dev/null | grep -qE "\.dylib|\.jnilib|\.so"; then
        JAR_NAME=$(basename "$JAR_PATH")
        echo "   📦 $JAR_NAME"
        
        # Extract and check signing of one binary
        TMP_DIR=$(mktemp -d)
        unzip -q "$JAR_PATH" -d "$TMP_DIR"
        FIRST_LIB=$(find "$TMP_DIR" -type f \( -name "*.dylib" -o -name "*.jnilib" -o -name "*.so" \) -print -quit)
        
        if [ -n "$FIRST_LIB" ]; then
            LIB_NAME=$(basename "$FIRST_LIB")
            
            # Check if Mach-O
            if file "$FIRST_LIB" | grep -q "Mach-O"; then
                # Check signing
                if codesign -dvv "$FIRST_LIB" 2>&1 | grep -q "Signature"; then
                    echo "      ✅ Sample binary is signed: $LIB_NAME"
                else
                    echo "      ❌ Sample binary NOT signed: $LIB_NAME"
                fi
                
                # Check architecture
                ARCH_INFO=$(lipo -info "$FIRST_LIB" 2>&1)
                if echo "$ARCH_INFO" | grep -q "i386"; then
                    echo "      ❌ Contains i386: $LIB_NAME"
                else
                    echo "      ✅ No i386: $LIB_NAME"
                fi
            fi
        fi
        
        rm -rf "$TMP_DIR"
    fi
done
echo ""

# 3. Check app signature
echo "3. Overall app signature:"
echo ""
if codesign -dvvv "$APP_PATH" 2>&1 | grep -q "Signature"; then
    echo "   ✅ App is signed"
    codesign -dvvv "$APP_PATH" 2>&1 | grep "Authority" | head -3
    
    # Check for hardened runtime
    if codesign -dvvv "$APP_PATH" 2>&1 | grep -q "runtime"; then
        echo "   ✅ Hardened runtime enabled"
    else
        echo "   ❌ Hardened runtime NOT enabled"
    fi
    
    # Check for timestamp
    if codesign -dvvv "$APP_PATH" 2>&1 | grep -q "Timestamp"; then
        echo "   ✅ Timestamp present"
    else
        echo "   ⚠️  No timestamp (may cause issues)"
    fi
else
    echo "   ❌ App is NOT signed"
fi
echo ""

# 4. Check Java runtime
echo "4. Java Runtime:"
echo ""
if [ -d "$APP_PATH/Contents/runtime" ]; then
    if codesign -dvv "$APP_PATH/Contents/runtime" 2>&1 | grep -q "Signature"; then
        echo "   ✅ JRE is signed"
    else
        echo "   ❌ JRE is NOT signed"
    fi
    
    # Count executables
    BIN_COUNT=$(find "$APP_PATH/Contents/runtime/Contents/Home/bin" -type f 2>/dev/null | wc -l)
    echo "   📊 JRE executables found: $BIN_COUNT"
else
    echo "   ⚠️  No embedded JRE found"
fi
echo ""

# 5. List problematic JARs (from known failures)
echo "5. Known problematic JARs (from previous failures):"
echo ""
PROBLEM_JARS=(
    "jnativehook"
    "jSerialComm"
    "flatlaf"
    "netlib-native"
    "onnxruntime"
)

for PATTERN in "${PROBLEM_JARS[@]}"; do
    FOUND=$(find "$APP_PATH/Contents/Java" -name "*${PATTERN}*.jar" | head -1)
    if [ -n "$FOUND" ]; then
        echo "   📦 $(basename "$FOUND")"
        
        # Quick check if it has native libs
        LIB_COUNT=$(unzip -l "$FOUND" 2>/dev/null | grep -E "\.dylib|\.jnilib|\.so" | wc -l)
        echo "      Native libraries: $LIB_COUNT"
    fi
done
echo ""

# 6. Summary
echo "=========================================="
echo "Summary & Recommendations"
echo "=========================================="
echo ""

# Count unsigned binaries
UNSIGNED=0
find "$APP_PATH" -type f \( -name "*.dylib" -o -name "*.jnilib" \) | while read -r LIB; do
    if ! codesign -dvv "$LIB" 2>&1 | grep -q "Signature"; then
        ((UNSIGNED++))
    fi
done

echo "Next steps:"
echo ""
echo "1. Run build_and_sign.sh to sign the app"
echo "2. Re-run this diagnostic to verify changes"
echo "3. Test with: spctl --assess --type install -vv [dmg-file]"
echo "4. Submit for notarization"
echo ""
