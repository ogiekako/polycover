#!/bin/bash
declare cur=$(pwd)
declare -r bin="$(mktemp -d /tmp/temp.XXXXXX)"
javac -d $bin -sourcepath src src/ui/Main.java

cp -r resource/* $bin
pushd $bin
jar cfm "$cur/poly.jar" "$cur/META-INF/MANIFEST.MF" ./*
popd

