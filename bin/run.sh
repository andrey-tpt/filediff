#!/usr/bin/env bash

set -e

SCRIPT_DIR=$(dirname "$0")
JAR_PATH=${SCRIPT_DIR}/../target/filediff-jar-with-dependencies.jar

java -jar ${JAR_PATH}