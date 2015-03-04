#!/bin/bash
coveredDir=$1
coverDir=$2
if [ -z $coveredDir ] || [ -z $coverDir ]; then
	echo "Usage: ./searchall.sh coveredDir coverDir"
	exit 1;
fi
echo "covered:"
for a in $(find $coveredDir -mindepth 1 -maxdepth 1 -type f); do
	echo $a
done
echo "cover:"
for b in $(find $coverDir -mindepth 1 -maxdepth 1 -type f); do
	echo $b
done
for a in $(find $coveredDir -mindepth 1 -maxdepth 1 -type f); do
	for b in $(find $coverDir -mindepth 1 -maxdepth 1 -type f); do
		if [ $(java cui.Main $a $b) == "OK" ]; then
			echo "OK: " $a $b
		fi
	done
done
