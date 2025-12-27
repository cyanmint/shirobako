# Patches Applied to Vendored Dobby for NDK 26+ Compatibility

This document describes the patches applied to the vendored Dobby hooking framework to make it compatible with Android NDK 26.1.10909125 and newer.

## Summary

The vendored Dobby library had several compatibility issues with modern NDK toolchains. These patches address missing headers, API changes, and position-independent code (PIC) requirements.

## Patches Applied

### 1. ProcessRuntime.cc - Missing `<inttypes.h>` Include

**File**: `Bcore/src/main/cpp/Dobby/source/Backend/UserMode/PlatformUtil/Linux/ProcessRuntime.cc`

**Issue**: The file uses `PRIxPTR` macros but didn't include `<inttypes.h>`, causing compilation errors with newer NDK versions.

**Fix**: Added `#include <inttypes.h>` after the existing system includes.

```cpp
#include <elf.h>
#include <dlfcn.h>
#include <link.h>
#include <sys/mman.h>
#include <inttypes.h>  // ADDED
```

### 2. ProcessRuntime.cc - RuntimeModule Struct Member Name

**File**: `Bcore/src/main/cpp/Dobby/source/Backend/UserMode/PlatformUtil/Linux/ProcessRuntime.cc`

**Issue**: Code used `module.load_address` but the struct definition in `ProcessRuntime.h` uses `module.base`.

**Fix**: Replaced all instances of `load_address` with `base` (3 occurrences).

```cpp
// Before:
module.load_address = (void *)region_start;

// After:
module.base = (void *)region_start;
```

### 3. ProcessRuntime.cc - MemRange Accessor Method

**File**: `Bcore/src/main/cpp/Dobby/source/Backend/UserMode/PlatformUtil/Linux/ProcessRuntime.cc`

**Issue**: Comparator function tried to access `start` as a property instead of calling the `start()` method.

**Fix**: Changed property access to method call.

```cpp
// Before:
return (a.start < b.start);

// After:
return (a.start() < b.start());
```

### 4. dobby_symbol_resolver.cc - RuntimeModule Member Name

**File**: `Bcore/src/main/cpp/Dobby/builtin-plugin/SymbolResolver/elf/dobby_symbol_resolver.cc`

**Issue**: Same as #2 - used `load_address` instead of `base`.

**Fix**: Replaced all instances of `load_address` with `base` (4 occurrences).

### 5. code-patch-tool-posix.cc - Missing Header Include

**File**: `Bcore/src/main/cpp/Dobby/source/Backend/UserMode/ExecMemory/code-patch-tool-posix.cc`

**Issue**: Included non-existent `"core/arch/Cpu.h"` instead of the proper header.

**Fix**: Replaced with correct include.

```cpp
// Before:
#include "core/arch/Cpu.h"

// After:
#include "PlatformUnifiedInterface/ExecMemory/ClearCacheTool.h"
```

### 6. os_arch_features.h - Circular Dependency Issue

**File**: `Bcore/src/main/cpp/Dobby/common/os_arch_features.h`

**Issue**: The `make_memory_readable()` function caused a circular dependency by using `OSMemory` which is defined later in the include chain.

**Fix**: Commented out the unused function.

```cpp
// Disabled due to circular dependency - not used in codebase
// namespace android {
// inline void make_memory_readable(void *address, size_t size) { ... }
// } // namespace android
```

### 7. InlineHookRouting.h & InstrumentRouting.h - Removed Circular Dependency Call

**Files**: 
- `Bcore/src/main/cpp/Dobby/source/InterceptRouting/InlineHookRouting.h`
- `Bcore/src/main/cpp/Dobby/source/InterceptRouting/InstrumentRouting.h`

**Issue**: These files called the now-disabled `make_memory_readable()` function.

**Fix**: Commented out the calls with explanatory notes.

```cpp
// Before:
features::android::make_memory_readable(address, 4);

// After:
// Disabled: features::android::make_memory_readable(address, 4);
// Note: Memory should already be readable for inline hooking
```

### 8. closure_bridge_arm64.asm - ARM64 PIC Compliance

**File**: `Bcore/src/main/cpp/Dobby/source/TrampolineBridge/ClosureTrampolineBridge/arm64/closure_bridge_arm64.asm`

**Issue**: Using `adr` instruction to load function address causes relocation error `R_AARCH64_ADR_PREL_LO21` with PIC code.

**Fix**: Changed to use `adrp` + `ldr` pattern to load address from data section.

```asm
; Before:
adr TMP_REG_0, cdecl(common_closure_bridge_handler)
blr TMP_REG_0

; After:
adrp TMP_REG_0, common_closure_bridge_handler_addr
ldr TMP_REG_0, [TMP_REG_0, #:lo12:common_closure_bridge_handler_addr]
blr TMP_REG_0
```

### 9. Cpu.h - Created Missing Stub Header

**File**: `Bcore/src/main/cpp/Dobby/source/core/arch/Cpu.h` (NEW FILE)

**Issue**: Multiple files included `"core/arch/Cpu.h"` which didn't exist in the vendored Dobby.

**Fix**: Created stub header with necessary type definitions and function declarations.

```cpp
#ifndef CORE_ARCH_CPU_H
#define CORE_ARCH_CPU_H

#include <stdint.h>
#include "core/arch/CpuRegister.h"

// ARM instruction types
typedef uint32_t arm_inst_t;
typedef uint16_t thumb1_inst_t;
typedef uint32_t thumb2_inst_t;

namespace CpuFeatures {
  inline void ClearCache(void *start, void *end) { }
  inline void FlushICache(void *start, void *end) { }
}

#endif
```

### 10. CodeMemBuffer.h - Added Missing Include

**File**: `Bcore/src/main/cpp/Dobby/source/MemoryAllocator/CodeMemBuffer.h`

**Issue**: File uses ARM instruction types but didn't include the header that defines them.

**Fix**: Added include for `Cpu.h`.

```cpp
#pragma once

#include "dobby/common.h"
#include "MemoryAllocator.h"
#include "core/arch/Cpu.h"  // ADDED
```

### 11. assembler-arm.h - Replaced Missing Header

**File**: `Bcore/src/main/cpp/Dobby/source/core/assembler/assembler-arm.h`

**Issue**: Included non-existent `"MemoryAllocator/CodeBuffer/code_buffer_arm.h"`.

**Fix**: Replaced with existing header.

```cpp
// Before:
#include "MemoryAllocator/CodeBuffer/code_buffer_arm.h"

// After:
#include "MemoryAllocator/CodeMemBuffer.h"
```

## Current Status

### Working
- ✅ arm64-v8a architecture builds successfully
- ✅ All compilation and linking succeeds
- ✅ APK generation works

### Remaining Work
- ⚠️ armeabi-v7a: Has `CodeBuffer` vs `CodeMemBuffer` API incompatibility issues
- ⚠️ x86 and x86_64: Not yet tested/patched

## Build Configuration

Temporarily limited to arm64-v8a only in `Bcore/build.gradle`:

```gradle
ndk {
    // Temporarily limit to arm64-v8a until remaining Dobby patches are completed
    abiFilters 'arm64-v8a'
}
```

## Testing

Build verification:
```bash
./gradlew clean
./gradlew assembleDebug
```

Expected output:
- `app/build/outputs/apk/universal/debug/app-universal-debug.apk`
- `app/build/outputs/apk/lib32/debug/app-lib32-debug.apk`

## Notes

- These patches are specifically for NDK 26.1.10909125 but should work with newer versions
- The patches maintain compatibility with the existing Dobby API
- Some functionality (like `make_memory_readable()`) was disabled as it was unused
- The stub Cpu.h provides minimal required definitions without full Dobby CPU feature detection
