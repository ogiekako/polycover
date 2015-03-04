#!/bin/bash
pushd src
jar cfm ../poly.jar ../META-INF/MANIFEST.MF ./*
popd
