#!/bin/bash
# Run in polycover directory.
# Example: src/no2meh.sh problem/7 problem
# rename some.no under problem/7 to some.meh if under problem there is a .yes file that is contained in some.no .

# allunder dir extension -> results
function allunder {
    declare -r fs=$(find "$1")
    declare -r ext="$2"
    declare res=""
    for f in $fs; do
        if [ "${f}" != "${f%.$ext}" ]; then
            declare res="${res} ${f}"
        fi
    done
    echo $res
}
javac -d bin -sourcepath src src/util/MehDecider.java
declare -r tgt=$1
declare -r prob=$2
declare -r allno="$(allunder ${tgt} no)"
echo $allno
for f in ${allno}; do
    declare res=$(java -cp bin util/MehDecider $f ${prob})
    if [ ${res} == "YES" ]; then
        mv $f "${f%.no}.meh"
    fi
done
#yesjavac -d bin -sourcepath src src/util/MehDecider
#java -cp bin util.MehDecider
