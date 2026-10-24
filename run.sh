#!/bin/sh
set -eu
rm -rf out
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out com.example.propertysnapshot.SnapshotDecisionTest
