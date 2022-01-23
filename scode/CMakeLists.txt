if (APPLE)
    add_sedona_scode(platMacOs)
endif()

if (UNIX)
    add_sedona_scode(platUnix)
    add_sedona_scode(platUnixMqtt)
endif()

if (WIN32)
    add_sedona_scode(platWin32)
endif()

add_sedona_scode(test-mqtt)