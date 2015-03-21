#!/bin/bash
# Run on polycover directory.
# For problems, only files with extension .no will be iterated.
# For cands, only files with extension .ans will be iterated.
# Example:
# src/searchall.sh --min_num_cand=2 --max_num_cand=3 problem/6 ans/6 2> /dev/null
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
javac -d ../bin cui/Judger.java
popd
echo "problem:" >&2
for a in $(find $problemDir -type f); do
	if [[ ${a##*.} != "no" ]]; then
		continue
	fi
	echo $a >&2
done
echo "cand:" >&2
for b in $(find $candDir -type f); do
	echo $b >&2
done
for a in $(find $problemDir -type f); do
	if [[ ${a##*.} != "no" ]]; then
		continue
	fi
	for b in $(find $candDir -type f); do
        if [[ ${b##*.} != "ans" ]]; then
            continue
        fi
		declare maxD=$(($(head -n 1 $b | cut -d ' ' -f 1)/2+1))
		declare n=1
		while [[ $((${maxD}>=${n}*2)) == 1 ]]; do
		    n=$(($n*2))
		done
		declare d=0
		declare ok=1
		while [[ $((${n}>0)) == 1 ]]; do
		    declare nd=$((${d}+${n}))
		    declare cmd="java -classpath bin cui.Judger ${min_num_cand} ${max_num_cand} --maxdepth=$nd $a $b"
            echo "cmd: " $cmd >&2
            if [[ $(${cmd}) != "OK" ]]; then
					ok=0
			else
			    d=${nd}
            fi
			n=$((${n}/2))
		done
		if [[ ${ok} == 1 ]]; then
			echo  "OK: " $a $b 1000
		else
			echo "NG: " $a $b $d
		fi
	done
done
