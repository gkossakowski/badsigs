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

echo "Checking $SCALA_JAR:" | tee -a $LOG
cat $TMP/library.properties | tee -a $LOG
echo "" | tee -a $LOG

echo "Generating Java files"
j=0
for i in `find ./scala -name *.class`; do
  j=$(($j+1))
  t=`expr "$i" : '\./\(.*\)\.class'`
  tt=${t//\//.}
  if [[ $tt =~ .*\$.* ]]
  then
    [ -n "${VERBOSE+x}" ] && echo "Skipping $tt"
    continue
  fi
  #possibly more names should be excluded that are java keywords
  if [[ $tt =~ .*(package|throws).* ]]
  then
    [ -n "${VERBOSE+x}" ] && echo "Skipping $tt"
    continue
  fi
  echo "import $tt;" > $SRC/$j.java
  echo "public class C$j {}" >> $SRC/C$j.java
done
echo ""
cd $CD


echo "Running ecj"
for i in `ls "$SRC"/*.java`; do
  echo "Processing $i"
  java -jar ecj-3.7.jar -nowarn -d none -classpath $SCALA_JAR $i 2>> $LOG
  [ $? -ne 0 ] && echo "" >> $LOG
done
echo ""

echo "Check $LOG for errors."
