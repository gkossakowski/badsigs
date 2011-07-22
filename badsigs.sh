#!/usr/bin/env bash
#

# This is important when you're doing lots of (or any) "rm -rf"ing
# in a script.  It makes it stop on any error.
set -e

DIR=$(cd $(dirname $0) && pwd)
echo "Root is $DIR"

WD=$(mktemp -d -t badsigs)
echo "Working dir is $WD"
mkdir -p "$WD/src" "$WD/classes"

LOCATION="$DIR/${1:-classes}"
FILTER=${2:-""}
BADSIGS_JAR=$(echo "$DIR"/target/badsigs-assembly-*.jar)

if [[ ! -f "$BADSIGS_JAR" ]]; then
  # xsbt assembly
  echo "Compile project with sbt 0.10.x, first. Run \`assembly\` sbt command."
  exit 1
fi

cd "$WD/classes"

if [[ -f "$LOCATION" ]]; then
  jar xf $LOCATION
elif [[ -d "$LOCATION" ]]; then
  cp -R "$LOCATION"/* .
else
  echo "Bad location $LOCATION passed. Pass path to either jar or classes directory."
  exit 1
fi

echo "Checking $LOCATION:"
if [[ -f "$CLASSES/library.properties" ]]; then
  cat $CLASSES/library.properties
  echo ""
fi

echo "Running Main app (will generate Java files and run ecj)"
java -jar "$BADSIGS_JAR" "$WD/classes" "$WD/src" "$FILTER"

exit $?
