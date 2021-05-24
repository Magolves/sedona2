add_sedona_kit(platUnix)

if (USE_SEDONA_MT)
    add_dependencies(platUnix all-kits)
else()
    add_dependencies(platUnix sys)
endif()