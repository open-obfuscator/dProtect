#!/usr/bin/env python
"""
Script used to clone the right version of dProtect-Core associated
with the current dProtect repo.

Usage: python ./scripts/fetch_dprotect_core.py . ~/dev/core
"""

import argparse
import subprocess
import sys
import shutil
import string
from pathlib import Path

GIT = shutil.which("git")
PROP_KEYWORD = "dprotectCoreCommit"

REPO = "https://github.com/open-obfuscator/dProtect-core"

UPSTREAM_KEYWORD = "latest"

def is_commit(value: str):
    if len(value) != len("f581758ec484dd99405172327e2a41833c003d33") and len(value) != len("f581758"):
        return False
    return all(x.lower() in string.hexdigits for x in value)

def git_clone(dst: Path, branch: str = None, commit: str = None, shallow: bool = True, force: bool = False):
    if dst.is_dir() and force:
        shutil.rmtree(dst)

    command = [
        shutil.which("git"), "clone", "-j8", "--single-branch"
    ]
    if commit is not None:
        shallow = False

    if branch is not None:
        command += [
            "--branch", branch
        ]

    if shallow:
        command += [
            "--depth", "1"
        ]

    command += [
        REPO, dst.resolve().absolute().as_posix()
    ]

    subprocess.run(command, check=True)

    if commit is not None:
        command = [
            GIT, "checkout", commit
        ]
        subprocess.run(command, check=True, cwd=dst)


def get_core_version(dprotect_path: Path):
    file = dprotect_path / "gradle.properties"
    if not file.is_file():
        print(f"{file} does not exist!")
        return None
    properties = file.read_text().splitlines()
    for line in properties:
        if not PROP_KEYWORD in line:
            continue
        _, version = line.split("=")
        version = version.strip().lower()
        version = version.replace('"', '')
        return version

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("src")
    parser.add_argument("dst")
    parser.add_argument("--force", "-f")

    args = parser.parse_args()

    if GIT is None:
        print("Git not found!")
        return

    dprotect_path = Path(args.src)
    if not dprotect_path.is_dir():
        print(f"Error: {dprotect_path} is not a directory!")
        return 1

    version = get_core_version(dprotect_path)

    if version is None:
        print(f"Error: Can't read dProtectCore version")
        return 1

    print(f"Using dProtect-core version: '{version}'")

    # Prepare args for git_clone()
    branch = "main"
    commit = None

    if version.lower() != UPSTREAM_KEYWORD:
        if is_commit(version):
            branch = None
            commit = version
        else:
            branch = version
            commit = None
    git_clone(Path(args.dst), branch=branch, commit=commit)
    return 0

if __name__ == "__main__":
    sys.exit(main())




