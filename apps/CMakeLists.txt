if (APPLE)
    add_sedona_app(macos/platMacOs)
endif()

if (UNIX)
    add_sedona_app(unix/platUnix)
    add_sedona_app(unix/platUnixMqtt)
endif()

if (WIN32)
    add_sedona_app(win32/platWin32)
    add_sedona_app(win32/platWin32hib)
    add_sedona_app(win32/platWin32wdft)
    add_sedona_app(win32/platWin32yield)
endif()

add_sedona_app(./test-mqtt)