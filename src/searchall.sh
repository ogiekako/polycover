#!/bin/bash
# Run on polycover directory.
# For problems, only files with extension .no will be iterated.
# Example:
# src/searchall.sh --min_num_cand=2 --max_num_cand=3 problem/hexomino ans/hexomino 2> /dev/null
declare min_num_cand=""
declare max_num_cand=""
declare problemDir=""
declare candDir=""
for o in $@; do
    case $o in
        --min_num_cand=*)
            min_num_cand="--min_num_cand=${o#*=}"
        ;;
        --max_num_cand=*)
            max_num_cand="--max_num_cand=${o#*=}"
        ;;
        *)
            if [ -z $problemDir ]; then
                problemDir=$o
            else
                candDir=$o
            fi
        ;;
    esac
done
if [ -z $problemDir ] || [ -z $candDir ]; then
	echo "Usage: src/searchall.sh [--min_num_cand=o] [--max_num_cand=o] problemDir candDir"
	exit 1;
fi

echo $cp
pushd src
javac -d ../bin cui/Main.java
popd
echo "problem:"
for a in $(find $problemDir -mindepth 1 -type f); do
	if [[ ${a##*.} != "no" ]]; then
		continue
	fi
	echo $a
done
echo "cand:"
for b in $(find $candDir -mindepth 1 -type f); do
	echo $b
done
for a in $(find $problemDir -mindepth 1 -type f); do
	if [[ ${a##*.} != "no" ]]; then
		continue
	fi
	for b in $(find $candDir -mindepth 1 -type f); do
		declare maxD=$(($(head -n 1 $b | cut -d ' ' -f 1)/2+1))
		declare ok=1
		for d in $(seq 1 $maxD); do
		    declare cmd="java -classpath bin cui.Main ${min_num_cand} ${max_num_cand} --maxdepth=$d $a $b"
            echo "cmd: " $cmd >&2
            if [[ $(${cmd}) != "OK" ]]; then
					ok=0
					break
			fi
		done
		if [[ ${ok} == 1 ]]; then
			echo  "OK: " $a $b
		else
			echo "NG: " $a $b $d
		fi
	done
done
