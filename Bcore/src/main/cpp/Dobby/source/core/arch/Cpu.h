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

namespace CpuFeatures {
  // Stub function declarations
  inline void ClearCache(void *start, void *end) {
    // Implementation is in platform-specific files
  }
  
  inline void FlushICache(void *start, void *end) {
    // Implementation is in platform-specific files  
  }
}

#endif // CORE_ARCH_CPU_H
