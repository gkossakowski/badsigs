import AssemblyKeys._

name := "badsigs"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.1"

retrieveManaged := true

libraryDependencies <<= (scalaVersion)(sv => 
  Seq(
    "org.scala-lang" % "scala-compiler" % sv,
    "org.eclipse.jdt.core.compiler" % "ecj" % "3.7"
  )
)

seq(assemblySettings: _*)
