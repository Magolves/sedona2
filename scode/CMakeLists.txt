if (APPLE)
    add_sedona_scode(platMacOs)
endif()

if (UNIX)
    add_sedona_scode(platUnix)
endif()

if (WIN32)
    add_sedona_scode(platWin32)
endif()