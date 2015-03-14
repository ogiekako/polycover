#!/bin/bash
pushd src
mkdir -p ../bin
javac -d ../bin ui/Main.java
cd ../bin
jar cfm ../poly.jar ../META-INF/MANIFEST.MF ./*
popd
