if [ -z $1 ]
then
  echo "Pass path to scala-library.jar as a first argument."
  exit 1
fi

SCALA_JAR="$1"
if [ ! -f "$SCALA_JAR" ]
then
  echo "Couldn't find jar at $SCALA_JAR"
  exit 1
fi

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

TMP="$WD/classes"
SRC="$WD/src"

mkdir $TMP

cd $TMP
jar xf $SCALA_JAR
cd $CD

echo "Checking $SCALA_JAR:"
cat $TMP/library.properties
echo ""

echo "Running Main app (will generate Java files and run ecj)"
java -jar "$BADSIGS_JAR" $TMP $SRC

exit $?
