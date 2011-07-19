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
    
    def generateJavaClass(name: String, importedType: String, binaryName: String): String =
      """|import %1s;
         |//binaryName=%2s
         |public class %3s {}
         |""".format(importedType, binaryName, name).stripMargin
      

      val wd = prepareWorkDir(workDir)
      val badsigsDir = wd
      
      val (anonymous, classes) = readClassDefsFromInput(input).toList.partition(_.anonymous)
      
//      {
//        val names = anonymous map (x => sourceName(x.name, x.innerClasses))
//        println("Skipping %1d anonymous classes:%2s".format(names.size, names mkString "\n"))
//      }

      def isPublic(x: ClassDef): Boolean = {
        import scala.tools.nsc.symtab.classfile.ClassfileConstants._
        x.hasFlag(JAVA_ACC_PUBLIC)
      }

      classes.filter(isPublic).zipWithIndex foreach {
        case (clazz, i) =>
          val importedType = JavaNames.sourceName(clazz.name, clazz.innerClasses)
          if (JavaNames.isValid(importedType)) {
            val javaClassName = "C" + i
            val f = badsigsDir / File(javaClassName + ".java")
            f.createFile(false)
            f.writeAll(generateJavaClass(javaClassName, importedType, clazz.name))
          }
      }
    
    val classpath = input match {
      case Args.ClassDir(name) => Path(name).toAbsolute.toString
      case Args.JarFile(_) => sys.error("not implemented yet")
    }
    
    val errors = Ecj.compileJavaFiles(badsigsDir, classpath)
    
    var i = 0
    errors foreach { x =>
      i = i + 1
      Console.err.println(x)
      Console.err.println
    }
    println("Found %1d errors".format(i))
    if (i > 0)
      exitCode = 1
      
    exit(exitCode)
  }

  def prepareWorkDir(x: Args.WorkDir): Directory = {
    val p = Path(x.name)
    p.deleteRecursively()
    p.createDirectory(true, true).toAbsolute
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
