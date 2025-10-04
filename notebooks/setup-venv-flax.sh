#!/usr/bin/env bash

set -x

VENV_SUFFIX="-flax"
VENV_DIR=".venv${VENV_SUFFIX}"

[ -d "${VENV_DIR}" ] && rm -rf "${VENV_DIR}"
python -m venv --copies --clear --upgrade-deps "${VENV_DIR}" || exit 1
. "${VENV_DIR}"/Scripts/activate || exit 1
pip install -r "requirements${VENV_SUFFIX}.txt" || exit 1
