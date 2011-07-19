name := "badsigs"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.0-1"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.9.0-1"

//If someone can point me to public repo where I can get ecj from I'd be grateful
libraryDependencies += "org.eclipse.jdt.core" % "ecj" % "3.7" from "http://dl.dropbox.com/u/12870350/badsigs/ecj-3.7.jar"
