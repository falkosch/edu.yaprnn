#!/usr/bin/env bash

set -x

[ -d .venv-tf ] && rm -rf .venv-tf
python -m venv --copies --clear --upgrade-deps .venv-tf || exit 1
. .venv-tf/Scripts/activate || exit 1
pip install -r requirements-tf.txt || exit 1
