#!/usr/bin/sh
#
# Script for building dProtect withing the docker image: openobfuscator/dprotect-build
# It requires to mount the source code at /dprotect (-v <src>:/dprotect)
#
set -ex

export GRADLE_OPTS="-Dorg.gradle.project.buildDir=/tmp/dprotect-build"

# clone & build dprotect-core
pushd /dprotect
python3 ./scripts/fetch_dprotect_core.py . /core
pushd /core
gradle :dprotect-core:publishToMavenLocal
popd
popd

# build dProtect
pushd /dprotect
gradle distZip
popd

# copy the zip file in /dist
cp /tmp/dprotect-build/distributions/dprotect-*.zip /dprotect/dist/
