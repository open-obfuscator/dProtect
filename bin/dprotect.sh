#!/bin/sh
#
# Start-up script for dProtect
#
# Note: when passing file names containing spaces to this script,
#       you'll have to add escaped quotes around them, e.g.
#       "\"/My Directory/My File.txt\""

# Account for possibly missing/basic readlink.
# POSIX conformant (dash/ksh/zsh/bash).
DPROTECT=`readlink -f "$0" 2>/dev/null`
if test "$DPROTECT" = ''
then
  DPROTECT=`readlink "$0" 2>/dev/null`
  if test "$DPROTECT" = ''
  then
    DPROTECT="$0"
  fi
fi

DPROTECT_HOME=`dirname "$DPROTECT"`/..

java -jar "$DPROTECT_HOME/lib/dprotect.jar" "$@"
