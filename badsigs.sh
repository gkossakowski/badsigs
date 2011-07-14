SCALA_JAR='/Users/grek/scala/scala-trunk/build/pack/lib/scala-library.jar'

CD=`pwd`
TMP="$CD/tmp"
SRC="$CD/src"
LOG="$CD/ecj.log"

rm -rf $TMP
mkdir $TMP
rm -rf $SRC
mkdir $SRC
rm $LOG

cd $TMP
jar xf $SCALA_JAR

echo "Checking $SCALA_JAR:"
cat $TMP/library.properties
echo ""

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
