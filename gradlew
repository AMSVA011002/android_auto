#!/bin/sh
# Gradle wrapper bootstrap (wrapper jar is generated, not committed — see README).
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  if command -v gradle >/dev/null 2>&1; then
    echo "gradle-wrapper.jar not found — generating it with installed Gradle..."
    (cd "$APP_HOME" && gradle wrapper --gradle-version 8.5 --no-daemon >/dev/null 2>&1 || true)
  fi
fi
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: gradle/wrapper/gradle-wrapper.jar is missing." >&2
  echo "Install Gradle 8.5+ and run:  gradle wrapper --gradle-version 8.5" >&2
  echo "Or open this project in Android Studio (it generates the wrapper for you)." >&2
  exit 1
fi
exec java -jar "$WRAPPER_JAR" "$@"
