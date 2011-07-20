if [ -z $1 ]
then
  echo "Pass path to a jar or classes directory as a first argument."
  exit 1
fi
LOCATION="$1"

BADSIGS_JAR="target/badsigs-assembly-0.1-SNAPSHOT.jar"
if [ ! -f "$BADSIGS_JAR" ]
then
  echo "Compile project with sbt 0.10.x, first. Run \`assembly\` sbt command."
  exit 1
fi

CD=`pwd`
WD="$CD/badsigs_working_dir"
if [ -d $WD ]
then
  echo "Cleaning up $WD"
  rm -rf $WD
fi
mkdir $WD

CLASSES="$WD/classes"
mkdir "$CLASSES"

if [ -f "$LOCATION" ]
then
  cd $CLASSES
  jar xf $LOCATION
  cd $CD
elif [ -d "$LOCATION" ]
then
  cp -R "$LOCATION"/* $CLASSES
else
  echo "Bad location $LOCATION passed. Pass path to either jar or classes directory."
  exit 1
fi

SRC="$WD/src"

echo "Checking $LOCATION:"
if [ -f "$CLASSES/library.properties" ]
then
  cat $CLASSES/library.properties
  echo ""
fi

echo "Running Main app (will generate Java files and run ecj)"
java -jar "$BADSIGS_JAR" $CLASSES $SRC

exit $?
