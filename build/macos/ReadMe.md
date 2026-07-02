# PAMGuard macOS Code Signing & Notarization

Complete solution for signing and notarizing PAMGuard for macOS distribution.

---

## Quick Start

### Prerequisites
- macOS with Xcode Command Line Tools installed
- **Developer ID Application** certificate (not "Apple Distribution")
- Apple Developer account with App-Specific Password
- PAMGuard built (`.app` file in `../../target/`)

### Configuration

Edit `apple_creds.sh` with your credentials:
```bash
CERT_NAME="Developer ID Application: Your Name (TEAM_ID)"
APPLE_ID="your-email@apple.com"
APPLE_PASSWORD="xxxx-xxxx-xxxx-xxxx"  # App-specific password
TEAM_ID="XXXXXXXXXX"
```

### Run Signing & Notarization

```bash
# From the PAMGuard/build/macos directory:
./build_and_sign.sh
```

That's it! The script will:
1. Sign all native libraries (including those inside JARs)
2. Remove incompatible binaries (i386, old SDK)
3. Create a signed DMG
4. Submit to Apple for notarization (~15 min)
5. Staple the notarization ticket

---

## What the Script Does

### Phase 0: Configure Multiple Instances
Modifies the `Info.plist` to allow users to launch multiple instances of PAMGuard:

- Sets `LSMultipleInstancesProhibited` to `false`
- Enables "New Instance" option in right-click menu
- Applied before signing

**Why:** Allows users to run multiple PAMGuard instances simultaneously for monitoring different audio streams or comparing configurations.

### Phase 1: JAR Processing
Recursively finds all JAR files and processes those containing native libraries:

- **Extracts** each JAR to a temporary directory
- **Identifies** all native binaries (`.dylib`, `.jnilib`, `.so`)
- **Removes** i386 (32-bit) binaries entirely
- **Strips** i386 architecture from fat binaries
- **Signs** each binary with:
  - `--options runtime` (hardened runtime)
  - `--timestamp` (Apple timestamp server)
  - Your Developer ID certificate
- **Rebuilds** the JAR with signed binaries

**Why:** Apple requires all binaries to be signed, even those inside JARs.

### Phase 2: Old SDK Library Removal
Removes native libraries compiled with SDKs older than macOS 10.9:

- `netlib-native_ref-osx-x86_64.jnilib`
- `netlib-native_system-osx-x86_64.jnilib`

These libraries fall back to pure Java implementations (fully functional, slightly slower).

**Why:** Apple rejects binaries compiled with pre-2013 SDKs.

### Phase 3: Loose Library Signing
Signs all `.dylib` and `.jnilib` files not inside JARs:

- Clears extended attributes (`xattr`)
- Makes files writable if needed
- Removes/strips i386 binaries
- Signs each library

### Phase 4: Java Runtime Signing
Signs the embedded JRE (if present):

- All executables in `Contents/runtime/Contents/Home/bin/`
- All libraries in the runtime
- The runtime bundle itself

### Phase 5: Main App Signing
Signs the app bundle with entitlements:

- Uses `entitlements.plist` (JIT, unsigned memory, audio input)
- Signs `PAMGuard.app` with all previous signatures preserved

### Phase 6: DMG Creation & Signing
Creates and signs a distributable DMG:

- Creates installer with Applications folder symlink
- Signs the DMG file
- Verifies both app and DMG signatures

### Phase 7: Notarization (if enabled)
Submits to Apple and waits for approval:

- Uploads DMG to Apple's notarization service
- Waits for processing (~5-15 minutes)
- Retrieves result
- Staples ticket to DMG (if approved)

### Phase 8: Verification
Final Gatekeeper check to ensure the DMG will be accepted by user systems.

---

## Files in This Directory

```
macos/
├── README.md                   # This file - start here! ⭐
├── build_and_sign.sh           # Signing & notarization script ⭐
├── apple_creds.sh              # Your credentials (configure this)
├── entitlements.plist          # Required entitlements for JVM
├── utils/                      # Utility scripts
│   ├── check_certs.sh          # Verify certificate setup
│   ├── diagnose.sh             # Check app signing state
│   ├── fix_netlib_sdk.sh       # Fix old SDK libraries manually
│   └── test_signing.sh         # Test without Apple submission
└── docs/                       # Documentation
    ├── FIXES_SUMMARY.md        # Technical details of fixes
    ├── NOTARIZATION_GUIDE.md   # Comprehensive troubleshooting
    ├── OLD_SDK_FIX.md          # Old SDK library issue
    ├── SIGNING_ERRORS.md       # Common signing errors
    └── VISUAL_GUIDE.md         # Visual explanation
```

---

## Usage Examples

### Standard Notarization
```bash
./build_and_sign.sh
```

### Test Signing Without Notarization
Edit `build_and_sign.sh` and change:
```bash
SHOULD_NOTARIZE=false
```

Or use the test script:
```bash
./utils/test_signing.sh
```

### Verify Certificate Setup
```bash
./utils/check_certs.sh
```

### Check App Status
```bash
./utils/diagnose.sh ../../target/PAMGuard.app
```

---

## Troubleshooting

### Certificate Not Found
**Error:** `no identity found`

**Solution:**
```bash
# List your certificates
security find-identity -v -p codesigning

# Update apple_creds.sh with exact name
CERT_NAME="Developer ID Application: Your Name (TEAM_ID)"
```

### Wrong Certificate Type
You need **"Developer ID Application"** for notarization.

**"Apple Distribution"** is only for Mac App Store.

### Notarization Failed
**Get the detailed log:**
```bash
xcrun notarytool log SUBMISSION_ID \
    --apple-id "your@email.com" \
    --password "xxxx-xxxx-xxxx-xxxx" \
    --team-id "TEAM_ID"
```

The script will show you this command with your submission ID.

---

## Common Issues & Solutions

| Problem | Solution |
|---------|----------|
| No .app file found | Run: `cd ../.. && mvn clean deploy` |
| Certificate not found | Use `./utils/check_certs.sh` to verify |
| Notarization rejected | Get log and check for unsigned binaries |
| Old SDK errors | Script now handles automatically |
| Permission errors | Automatically suppressed (harmless) |

---

## Getting Your Credentials

### Developer ID Certificate
1. Go to https://developer.apple.com/account
2. Certificates, Identifiers & Profiles
3. Create: **Developer ID Application** certificate
4. Download and install in Keychain

### App-Specific Password
1. Go to https://appleid.apple.com/account/manage
2. Sign in with your Apple ID
3. App-Specific Passwords section
4. Generate new password
5. Copy and save in `apple_creds.sh`

### Team ID
Found in your Apple Developer account at https://developer.apple.com/account

---

## Expected Timeline

| Step | Duration |
|------|----------|
| JAR processing | 2-5 minutes |
| Library signing | 2-3 minutes |
| App signing | 1 minute |
| DMG creation | 1 minute |
| Apple notarization | 5-15 minutes |
| Ticket stapling | 30 seconds |
| **Total** | **~15-25 minutes** |

---

## Getting Help

### Quick Diagnostics
```bash
./utils/check_certs.sh    # Verify certificate setup
./utils/diagnose.sh       # Check app state
```

### Documentation
- `docs/NOTARIZATION_GUIDE.md` - Comprehensive troubleshooting
- `docs/SIGNING_ERRORS.md` - Common signing error messages
- `docs/OLD_SDK_FIX.md` - Old SDK library issues
- `docs/VISUAL_GUIDE.md` - Visual explanation of fixes

### Test Without Notarization
```bash
./utils/test_signing.sh
```

---

## Success Indicators

When the script completes successfully, you'll see:

```
✅ Signature verification successful
✅ DMG signature verified
✅ Notarization successful!
✅ Ticket stapled successfully
✅ Gatekeeper assessment passed
✅ SUCCESS!
```

The signed DMG at `target/PAMGuard-VERSION-signed.dmg` is ready for distribution!

---

## License & Credits

Part of the PAMGuard project: www.pamguard.org

Developed to solve macOS notarization requirements for complex Java applications with native dependencies.
