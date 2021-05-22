if (APPLE)
    add_sedona_app(platMacOs)
endif()

if (UNIX)
    add_sedona_app(platUnix)
endif()

if (WIN32)
    add_sedona_app(platWin32)
    add_sedona_app(platWin32hib)
    add_sedona_app(platWin32wdft)
    add_sedona_app(platWin32yield)
endif()