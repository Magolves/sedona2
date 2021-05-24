#!/bin/bash
#
#   sedonacmt.sh
#
#   Script to run sedonacmt java program on Unix.
#
# Author: Oliver Wieland
# Creation: 22 May 21
#

which java > /dev/null
if [[ $? != 0 ]]
then
  echo "java is not in the PATH"
  return 1
fi

# Get full path to sedonac script
sedonac_path=`dirname $(cd ${0%/*} && echo $PWD/${0##*/})`

# Determine sedona home by pulling off trailing /bin
sedona_home=${sedonac_path%/bin}

java -Dsedona.home=$sedona_home -cp "$sedona_home/lib/*" sedonacmt/MainMulti "$@"

