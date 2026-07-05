#!/bin/bash

# Certificate checker for macOS code signing
# Helps verify you have the correct certificates for notarization

echo "=========================================="
echo "Code Signing Certificate Checker"
echo "=========================================="
echo ""

# Check if credentials file exists
if [ ! -f "apple_creds.sh" ]; then
    echo "❌ apple_creds.sh not found!"
    echo ""
    echo "Create this file with:"
    echo "  CERT_NAME=\"Developer ID Application: Your Name (TEAM_ID)\""
    echo "  APPLE_ID=\"your-email@example.com\""
    echo "  APPLE_PASSWORD=\"app-specific-password\""
    echo "  TEAM_ID=\"YOUR_TEAM_ID\""
    exit 1
fi

source apple_creds.sh

echo "Configured certificate: $CERT_NAME"
echo ""

echo "Available signing identities:"
echo "=============================="
security find-identity -v -p codesigning
echo ""

# Check if the configured cert exists
if security find-identity -v -p codesigning | grep -q "$CERT_NAME"; then
    echo "✅ Configured certificate found in keychain"
else
    echo "❌ Configured certificate NOT found in keychain"
    echo ""
    echo "Available certificates:"
    security find-identity -v -p codesigning | grep "Developer ID" || echo "  (none)"
    echo ""
    echo "Update CERT_NAME in apple_creds.sh to match one of the above"
    exit 1
fi
echo ""

# Check certificate type
if echo "$CERT_NAME" | grep -q "Developer ID Application"; then
    echo "✅ Certificate type: Developer ID Application (correct for notarization)"
elif echo "$CERT_NAME" | grep -q "3rd Party Mac Developer Application"; then
    echo "❌ Certificate type: 3rd Party Mac Developer (WRONG - this is for App Store only)"
    echo ""
    echo "For notarization, you need: Developer ID Application"
    echo "Create one at: https://developer.apple.com/account/resources/certificates/list"
    exit 1
else
    echo "⚠️  Unknown certificate type"
fi
echo ""

# Check Apple ID
echo "Apple ID: $APPLE_ID"
if [ -z "$APPLE_ID" ]; then
    echo "❌ Apple ID not configured"
    exit 1
else
    echo "✅ Apple ID configured"
fi
echo ""

# Check Team ID
echo "Team ID: $TEAM_ID"
if [ -z "$TEAM_ID" ]; then
    echo "❌ Team ID not configured"
    exit 1
else
    echo "✅ Team ID configured"
fi
echo ""

# Check app-specific password
echo "App-specific password: ${APPLE_PASSWORD:0:4}****"
if [ -z "$APPLE_PASSWORD" ]; then
    echo "❌ App-specific password not configured"
    echo ""
    echo "Create one at: https://appleid.apple.com/account/manage"
    echo "Look for 'App-Specific Passwords' section"
    exit 1
else
    echo "✅ App-specific password configured"
fi
echo ""

# Test the credentials (without actually submitting anything)
echo "Testing notarytool credentials..."
if xcrun notarytool history --apple-id "$APPLE_ID" --password "$APPLE_PASSWORD" --team-id "$TEAM_ID" 2>&1 | grep -qE "^  createdDate|^Successfully received submission"; then
    echo "✅ Credentials are valid!"
    echo ""
    echo "Recent submissions:"
    xcrun notarytool history --apple-id "$APPLE_ID" --password "$APPLE_PASSWORD" --team-id "$TEAM_ID" | head -10
else
    echo "❌ Credentials test failed"
    echo ""
    echo "Check your Apple ID, password, and Team ID"
    echo ""
    echo "Get your Team ID from: https://developer.apple.com/account"
    echo "Create app-specific password at: https://appleid.apple.com/account/manage"
fi
echo ""

echo "=========================================="
echo "Certificate Information"
echo "=========================================="
echo ""

# Get more details about the certificate
if security find-identity -v -p codesigning | grep -q "$CERT_NAME"; then
    # Extract the hash
    CERT_HASH=$(security find-identity -v -p codesigning | grep "$CERT_NAME" | awk '{print $2}')
    
    echo "Certificate hash: $CERT_HASH"
    echo ""
    
    # Show certificate details
    echo "Certificate details:"
    security find-certificate -c "$CERT_NAME" -p | openssl x509 -noout -subject -dates 2>/dev/null || echo "  (Could not retrieve details)"
    echo ""
fi

echo "=========================================="
echo "Summary"
echo "=========================================="
echo ""

ALL_OK=true

if security find-identity -v -p codesigning | grep -q "$CERT_NAME"; then
    echo "✅ Certificate: OK"
else
    echo "❌ Certificate: NOT FOUND"
    ALL_OK=false
fi

if echo "$CERT_NAME" | grep -q "Developer ID Application"; then
    echo "✅ Certificate Type: OK (Developer ID Application)"
else
    echo "❌ Certificate Type: WRONG (need Developer ID Application)"
    ALL_OK=false
fi

if [ -n "$APPLE_ID" ]; then
    echo "✅ Apple ID: Configured"
else
    echo "❌ Apple ID: Not configured"
    ALL_OK=false
fi

if [ -n "$TEAM_ID" ]; then
    echo "✅ Team ID: Configured"
else
    echo "❌ Team ID: Not configured"
    ALL_OK=false
fi

if [ -n "$APPLE_PASSWORD" ]; then
    echo "✅ App Password: Configured"
else
    echo "❌ App Password: Not configured"
    ALL_OK=false
fi

echo ""

if [ "$ALL_OK" = true ]; then
    echo "🎉 Everything looks good! You should be able to notarize."
    echo ""
    echo "Next steps:"
    echo "  1. Build: cd ../.. && mvn clean deploy"
    echo "  2. Sign:  ./build_and_sign.sh"
else
    echo "⚠️  Please fix the issues above before attempting notarization."
fi
echo ""
