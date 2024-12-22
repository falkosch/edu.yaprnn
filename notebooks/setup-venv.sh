#!/usr/bin/env bash

set -x

[ -d .venv ] && rm -rf .venv
python -m venv --copies --clear --upgrade-deps .venv || exit 1
. .venv/Scripts/activate || exit 1
pip install -r requirements.txt || exit 1
