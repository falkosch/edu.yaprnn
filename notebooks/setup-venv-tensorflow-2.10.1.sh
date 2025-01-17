#!/usr/bin/env bash

set -x

VENV_SUFFIX="-tensorflow-2.10.1"
VENV_DIR=".venv-tf"

[ -d "${VENV_DIR}" ] && rm -rf "${VENV_DIR}"
python -m venv --copies --clear --upgrade-deps "${VENV_DIR}" || exit 1
. "${VENV_DIR}"/Scripts/activate || exit 1
pip install -r "requirements${VENV_SUFFIX}.txt" || exit 1
