//
// Copyright 2012 Tridium, Inc. All Rights Reserved.
// Licensed under the Academic Free License version 3.0
//
// History:
//    2012 Apr 10   Elizabeth McKenney   Adapted from win32 
//

#include "sedona.h"
#include "sedonaPlatform.h"

#ifdef __linux__
// Linux-specific
#include <sys/sysinfo.h>
#endif

// Str PlatformService.doPlatformId()
Cell sys_PlatformService_doPlatformId(SedonaVM* vm, Cell* params)
{         
  Cell result;
  result.aval = PLATFORM_ID;
  return result;
}                      


// Str PlatformService.getPlatVersion()
Cell sys_PlatformService_getPlatVersion(SedonaVM* vm, Cell* params)
{         
  Cell result;
  result.aval = PLAT_BUILD_VERSION;  
  return result;
}


// long PlatformService.getNativeMemAvailable()
int64_t sys_PlatformService_getNativeMemAvailable(SedonaVM* vm, Cell* params)
{
  // FIXME: https://stackoverflow.com/questions/6094444/how-can-i-programmatically-check-free-system-memory-on-mac-like-the-activity-mon
  return 64000L;
}


