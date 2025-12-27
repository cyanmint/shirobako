#ifndef CORE_ARCH_CPU_H
#define CORE_ARCH_CPU_H

#include <stdint.h>
#include "core/arch/CpuRegister.h"

// Stub header for compatibility with vendored Dobby
// The original Cpu.h is not present in this version

// ARM instruction types
typedef uint32_t arm_inst_t;
typedef uint16_t thumb1_inst_t;
typedef uint32_t thumb2_inst_t;

// Forward declarations for platform-specific implementations
// Actual implementations are in clear-cache-tool-all.c
#ifdef __cplusplus
extern "C" {
#endif
void ClearCache(void *start, void *end);
#ifdef __cplusplus
}
#endif

namespace CpuFeatures {
  // Forward to actual implementation
  inline void ClearCache(void *start, void *end) {
    ::ClearCache(start, end);
  }
  
  inline void FlushICache(void *start, void *end) {
    // FlushICache typically calls ClearCache on most platforms
    ::ClearCache(start, end);
  }
}

#endif // CORE_ARCH_CPU_H
