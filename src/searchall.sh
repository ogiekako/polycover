#!/bin/bash
problemDir=$1
candDir=$2
if [ -z $problemDir ] || [ -z $candDir ]; then
	echo "Usage: ./searchall.sh problemDir candDir"
	exit 1;
fi
echo "problem:"
for a in $(find $problemDir -mindepth 1 -maxdepth 1 -type f); do
	echo $a
done
echo "cand:"
for b in $(find $candDir -mindepth 1 -maxdepth 1 -type f); do
	echo $b
done
for a in $(find $problemDir -mindepth 1 -maxdepth 1 -type f); do
	if [[ ${a##*.} != "no" ]]; then
		continue
	fi
	for b in $(find $candDir -mindepth 1 -maxdepth 1 -type f); do
		declare -r maxD=$(($(head -n 1 $b | cut -d ' ' -f 1)/2+1))
		declare ok=1
		for d in $(seq 1 $maxD); do
			if [[ $(java cui.Main --maxdepth $d $a $b) != "OK" ]]; then
					ok=0
					break
			fi
		done
		if [[ ok == 1 ]]; then
			echo  "OK: " $a $b
		else
			echo "NG: " $a $b $d
		fi
	done
done
