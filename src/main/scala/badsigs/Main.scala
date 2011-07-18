package badsigs

import scala.tools.nsc.io._
import scala.collection.mutable

object Main {
  
  var exitCode = 0;
    
  def main(args: Array[String]) {
    val (input, workDir) = parseArgs(args)
    
    def readClassDefsFromInput(input: Args.Input): Iterator[ClassDef] = input match {
      case Args.ClassDir(name) =>
        val path = Path(name)
        assert(path.exists, "Path %1s does not exist".format(path.toAbsolute.toString))
        val classes: Iterator[File] = path.walk.collect {
          case p: Path if p.isFile && p.hasExtension("class") => p.toFile
        }
        classes.map { f =>
          val parser = new ClassfileParser
          parser.parse(AbstractFile.getFile(f))
        }
      case Args.JarFile(name) => sys.error("not implemented yet")
    }
    
    def generateJavaClass(name: String, importedType: String): String = 
      """|import %1s;
         |
         |public class %2s {}
         |""".format(importedType, name).stripMargin
      
    
//    try {
      val wd = prepareWorkDir(workDir)
//      println("Working directory is " + wd)
//      val badsigsDir = (wd / Directory("badsigs")).createDirectory(true, false)
      val badsigsDir = wd
      
      val classes = readClassDefsFromInput(input)
      
      classes.zipWithIndex foreach {
        case (clazz, i) =>
          val importedType = sourceName(clazz.name, clazz.innerClasses)
          val javaClassName = "C" + i
          val f = badsigsDir / File(javaClassName + ".java")
          f.createFile(false)
          f.writeAll(generateJavaClass(javaClassName, importedType))
      }
      
      exit(exitCode)
   /*} catch {
      case e: java.io.IOException => 
        println("Error reading file %s: %s".format(args(1), e.getMessage))
    }*/
  }

  def prepareWorkDir(x: Args.WorkDir): Directory = Path(x.name).createDirectory(true, false).toAbsolute
  
  def sourceName(binaryName: String, innerClasses: Map[String, InnerClassEntry]): String = 
    innerClasses.get(binaryName) match {
      case Some(InnerClassEntry(external, outer, name, _)) => sourceName(outer, innerClasses) + "." + name
      case None => binaryName
    }
  
  object Args {
    sealed abstract class Input
    case class ClassDir(name: String) extends Input
    case class JarFile(name: String) extends Input
    
    case class WorkDir(name: String)
  }
  
  def parseArgs(args: Array[String]): (Args.Input, Args.WorkDir) = {
    import Args._
    args.toList match {
      case input :: workDir :: Nil =>
        (if (input endsWith ".jar") JarFile(input) else ClassDir(input), WorkDir(workDir))
      case _ => usage()
    }
  }
  
  def usage() = {
    println(
"""Usage: badsigs <classDirectory|jarFile> <workDirectory>
""")
    sys.exit(1)
  }

}
