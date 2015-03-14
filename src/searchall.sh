#!/bin/bash
# Run on polycover directory.
problemDir=$1
candDir=$2
if [ -z $problemDir ] || [ -z $candDir ]; then
	echo "Usage: src/searchall.sh problemDir candDir"
	exit 1;
fi

echo $cp
pushd src
javac -d ../bin cui/Main.java
popd
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
		declare maxD=$(($(head -n 1 $b | cut -d ' ' -f 1)/2+1))
		declare ok=1
		for d in $(seq 1 $maxD); do
            if [[ $(java -classpath bin cui.Main --maxdepth $d $a $b) != "OK" ]]; then
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
