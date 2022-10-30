#!/usr/bin/sh
#
# Script for building and publishing dProtect
# It requires to mount the source code at /dprotect (-v <src>:/dprotect)
#
set -ex

# clone & build dprotect-core
pushd /dprotect
python3 ./scripts/fetch_dprotect_core.py . /core
pushd /core
gradle :dprotect-core:publishToMavenLocal
gradle :dprotect-core:publishAllPublicationsToGithubRepository
popd
popd

# build dProtect
pushd /dprotect
gradle :gradle:publishAllPublicationsToGithubRepository
popd

