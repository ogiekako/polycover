#!/bin/sh
# Usage: src/findpromissing.sh problem/dir
src/searchall.sh $1 ans 2> /dev/null  | sort -r -n -k4 | head -n 20
