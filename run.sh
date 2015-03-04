#!/bin/bash
pushd bin
jar cfm ../poly.jar ../META-INF/MANIFEST.MF ./*
popd
