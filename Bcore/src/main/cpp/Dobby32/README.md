# Dobby32 - Prebuilt Dobby Binaries for 32-bit Architectures

This directory contains prebuilt Dobby hooking framework binaries for 32-bit Android architectures.

## Purpose

The vendored Dobby source code (in `Dobby64/`) has API incompatibilities with NDK 26+ when building for 32-bit architectures (armeabi-v7a, x86). These incompatibilities would require extensive refactoring of the assembler code.

To support QEMU emulation for apps with 32-bit native libraries, we use prebuilt Dobby binaries for 32-bit architectures instead of building from source.

## Architecture Support

- **armeabi-v7a**: Prebuilt static library (`libdobby.a`)
- **x86**: Not currently included (can be added if needed)

## Source

Prebuilt binaries obtained from: https://github.com/3equals3/DobbyHook

This repository provides tested, stable Dobby binaries for both ARM32 and ARM64 architectures.

## Usage

The CMakeLists.txt automatically detects the target architecture:
- **32-bit** (armeabi-v7a, x86): Uses prebuilt binaries from this directory
- **64-bit** (arm64-v8a, x86_64): Builds from patched source in `Dobby64/`

## QEMU Integration

These 32-bit binaries enable hooking support for apps running under QEMU emulation, allowing BlackBox to run and hook into apps with different native library architectures than the host device supports natively.

## License

Dobby is released under the Apache License 2.0. See https://github.com/jmpews/Dobby for details.
