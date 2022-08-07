add_sedona_kit(platWin32)

if (USE_SEDONA_MT)
    add_dependencies(platWin32 all-kits)
else()
    add_dependencies(platWin32 sys)
endif()