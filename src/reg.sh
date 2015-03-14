#!/bin/bash
# Usage: src/reg.sh path/to/prob.no another/path/to/some.ans
#   prob.no is renamed to prob.yes and
#   path/to/prob.dup is created as a copy of some.ans
p=$1
a=$2
if [ ! -e $p ]; then
    echo "$p does not exist" >&2
    exit 1
fi
if [ ! -e $a ]; then
    echo "$a does not exist" >&2
    exit 1
fi
#if [ ! $p =~ *.no ]; then
#    exit 0
#fi
if [[ $a =~ *.dup ]]; then
    exit 0
fi
p2=${p%%.*}
echo $p "${p2}.yes"
cp $a "ans/${p2##problem/}.dup"

