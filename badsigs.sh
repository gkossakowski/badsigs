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

CD=`pwd`
WD="$CD/badsigs_working_dir"
[ -d $WD ] && rm -rf $WD
mkdir $WD

TMP="$WD/classes"
SRC="$WD/src"

LOG="$CD/ecj.log"
rm $LOG

mkdir $TMP
mkdir $SRC

cd $TMP
jar xf $SCALA_JAR
cd $CD

echo "Checking $SCALA_JAR:" | tee -a $LOG
cat $TMP/library.properties | tee -a $LOG
echo "" | tee -a $LOG

echo "Generating Java files"
scala -classpath target/scala-2.9.0.1/classes/ badsigs.Main $TMP $SRC
N=`find $SRC -name '*.java' | wc -l`
echo "Generated $N files"

echo "Running ecj"
for i in `find $SRC -name '*.java'`; do
  echo "Processing $i"
  java -jar ecj-3.7.jar -1.5 -nowarn -d none -classpath $SCALA_JAR $i 2>> $LOG
  [ $? -ne 0 ] && echo "" >> $LOG
done
echo ""

echo "Check $LOG for errors."
