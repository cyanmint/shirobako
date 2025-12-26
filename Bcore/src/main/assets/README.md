# QEMU User Mode Assets

This directory contains QEMU user-mode static binaries and Android runtime libraries for cross-architecture execution.

## Directory Structure

```
assets/
├── qemu/
│   ├── arm64-v8a/       # QEMU binaries compiled for arm64 hosts
│   │   ├── qemu-arm-static      # For running arm32 apps
│   │   └── qemu-x86_64-static   # For running x86_64 apps
│   ├── armeabi-v7a/     # QEMU binaries compiled for arm32 hosts
│   │   ├── qemu-aarch64-static  # For running arm64 apps
│   │   └── qemu-x86_64-static   # For running x86_64 apps
│   └── x86_64/          # QEMU binaries compiled for x86_64 hosts
│       ├── qemu-aarch64-static  # For running arm64 apps
│       └── qemu-arm-static      # For running arm32 apps
└── runtime/
    ├── arm64-v8a/       # Android runtime for arm64
    │   ├── linker64     # Dynamic linker
    │   ├── libc.so      # Bionic C library
    │   ├── libm.so      # Math library
    │   └── libdl.so     # Dynamic loading library
    ├── armeabi-v7a/     # Android runtime for arm32
    │   ├── linker
    │   ├── libc.so
    │   ├── libm.so
    │   └── libdl.so
    └── x86_64/          # Android runtime for x86_64
        ├── linker64
        ├── libc.so
        ├── libm.so
        └── libdl.so
```

## How to Populate Assets

### 1. Download QEMU Static Binaries

QEMU static binaries can be obtained from Debian packages:

**For arm64 host binaries:**
```bash
# Download qemu-user-static package for arm64
wget http://ftp.debian.org/debian/pool/main/q/qemu/qemu-user-static_8.2.2+ds-1_arm64.deb

# Extract binaries
ar x qemu-user-static_8.2.2+ds-1_arm64.deb
tar xf data.tar.xz

# Copy needed binaries
cp usr/bin/qemu-arm-static Bcore/src/main/assets/qemu/arm64-v8a/
cp usr/bin/qemu-x86_64-static Bcore/src/main/assets/qemu/arm64-v8a/
```

**For x86_64 host binaries:**
```bash
# Download qemu-user-static package for amd64
wget http://ftp.debian.org/debian/pool/main/q/qemu/qemu-user-static_8.2.2+ds-1_amd64.deb

# Extract binaries
ar x qemu-user-static_8.2.2+ds-1_amd64.deb
tar xf data.tar.xz

# Copy needed binaries
cp usr/bin/qemu-aarch64-static Bcore/src/main/assets/qemu/x86_64/
cp usr/bin/qemu-arm-static Bcore/src/main/assets/qemu/x86_64/
```

**For arm32 host binaries:**
```bash
# Download qemu-user-static package for armhf
wget http://ftp.debian.org/debian/pool/main/q/qemu/qemu-user-static_8.2.2+ds-1_armhf.deb

# Extract binaries
ar x qemu-user-static_8.2.2+ds-1_armhf.deb
tar xf data.tar.xz

# Copy needed binaries
cp usr/bin/qemu-aarch64-static Bcore/src/main/assets/qemu/armeabi-v7a/
cp usr/bin/qemu-x86_64-static Bcore/src/main/assets/qemu/armeabi-v7a/
```

### 2. Extract Android Runtime from Redroid Container

Android runtime libraries (linker, libc, etc.) can be extracted from the official Redroid container:

**Extract arm64 runtime:**
```bash
# Pull the redroid container
docker pull docker.io/redroid/redroid:16.0.0-latest

# Create a temporary container
docker create --name redroid-temp docker.io/redroid/redroid:16.0.0-latest

# Extract arm64 libraries
docker cp redroid-temp:/system/bin/linker64 Bcore/src/main/assets/runtime/arm64-v8a/
docker cp redroid-temp:/system/lib64/libc.so Bcore/src/main/assets/runtime/arm64-v8a/
docker cp redroid-temp:/system/lib64/libm.so Bcore/src/main/assets/runtime/arm64-v8a/
docker cp redroid-temp:/system/lib64/libdl.so Bcore/src/main/assets/runtime/arm64-v8a/

# Extract arm32 libraries
docker cp redroid-temp:/system/bin/linker Bcore/src/main/assets/runtime/armeabi-v7a/
docker cp redroid-temp:/system/lib/libc.so Bcore/src/main/assets/runtime/armeabi-v7a/
docker cp redroid-temp:/system/lib/libm.so Bcore/src/main/assets/runtime/armeabi-v7a/
docker cp redroid-temp:/system/lib/libdl.so Bcore/src/main/assets/runtime/armeabi-v7a/

# Extract x86_64 libraries (if available in multiarch build)
docker cp redroid-temp:/system/lib64/x86_64/libc.so Bcore/src/main/assets/runtime/x86_64/ 2>/dev/null || \
docker cp redroid-temp:/system/lib64/libc.so Bcore/src/main/assets/runtime/x86_64/

# Clean up
docker rm redroid-temp
```

**Note:** The redroid container may not have x86_64 libraries by default. You may need to:
- Use a x86_64 variant of redroid if available
- Extract from an Android x86_64 system image
- Build from AOSP source for x86_64

### 3. Verify Binaries

After populating the assets, verify the binaries:

```bash
# Check QEMU binaries exist and are executable
file Bcore/src/main/assets/qemu/*/qemu-*-static

# Check runtime libraries exist
file Bcore/src/main/assets/runtime/*/lib*.so
file Bcore/src/main/assets/runtime/*/linker*

# Check sizes (QEMU static binaries are large, typically 5-20MB each)
du -sh Bcore/src/main/assets/qemu/*/qemu-*-static
```

## Architecture Support Matrix

Based on Redroid limitations, only these architectures are supported:
- **arm64-v8a** (aarch64)
- **armeabi-v7a** (arm32)
- **x86_64** (amd64)

Note: x86 (i386) is NOT supported as it's not available in redroid containers.

## QEMU Usage Strategy

The app will automatically detect the host architecture and use QEMU for non-native architectures:

1. **arm64 + arm32 capable host** → Use QEMU for x86_64 apps
2. **arm64 only host** → Use QEMU for arm32 and x86_64 apps
3. **x86_64 only host** → Use QEMU for arm64 and arm32 apps

## License

QEMU binaries are licensed under GPL v2. The Android runtime libraries are from AOSP and licensed under Apache 2.0.
