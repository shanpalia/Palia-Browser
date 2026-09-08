#!/usr/bin/env sh
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.3.1
DIST_NAME=gradle-${GRADLE_VERSION}-bin
DIST_DIR="$HOME/.gradle/wrapper/dists/$DIST_NAME"
DIST_ZIP="$DIST_DIR/${DIST_NAME}.zip"
DIST_HOME="$DIST_DIR/gradle-${GRADLE_VERSION}"
if [ ! -x "$DIST_HOME/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  if [ ! -f "$DIST_ZIP" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 -o "$DIST_ZIP" "https://services.gradle.org/distributions/${DIST_NAME}.zip"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$DIST_ZIP" "https://services.gradle.org/distributions/${DIST_NAME}.zip"
    else
      echo "curl or wget is required to download Gradle ${GRADLE_VERSION}." >&2
      exit 1
    fi
  fi
  rm -rf "$DIST_HOME.tmp"
  mkdir -p "$DIST_HOME.tmp"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$DIST_ZIP" -d "$DIST_HOME.tmp"
  else
    echo "unzip is required to install Gradle ${GRADLE_VERSION}." >&2
    exit 1
  fi
  rm -rf "$DIST_HOME"
  mv "$DIST_HOME.tmp/gradle-${GRADLE_VERSION}" "$DIST_HOME"
  rm -rf "$DIST_HOME.tmp"
fi
exec "$DIST_HOME/bin/gradle" "$@"
