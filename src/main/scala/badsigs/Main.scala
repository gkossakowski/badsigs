package badsigs

import scala.tools.nsc.io._
import scala.collection.mutable

object Main {
  def main(args: Array[String]) {
    var generatedClassCount = 0
    var errorCount = 0
    val (input, workDir, pred) = parseArgs(args)

    def readClassDefsFromInput(input: Args.Input): Iterator[ClassDef] = input match {
      case Args.ClassDir(name) =>
        val path = Path(name)
        assert(path.exists, "Path %s does not exist".format(path.toAbsolute))
        val classes: Iterator[File] = path.walk.collect {
          case p: Path if p.isFile && p.hasExtension("class") && pred(p.path) => p.toFile
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

    val (anonymous, classes) = readClassDefsFromInput(input).toList.partition(_.anonymous)

//      {
//        val names = anonymous map (x => sourceName(x.name, x.innerClasses))
//        println("Skipping %1d anonymous classes:%2s".format(names.size, names mkString "\n"))
//      }

    def isPublic(x: ClassDef): Boolean = {
      import scala.tools.nsc.symtab.classfile.ClassfileConstants._
      x.hasFlag(JAVA_ACC_PUBLIC)
    }

    def blacklisted(x: ClassDef): Boolean = {
      // this blacklist consists of classes that are known to be not importable from Java
      // we probably should have a better criteria instead of just hard-coded blacklist
      // but I failed at coming up with one
      val blacklist: Set[String] = Set("scala.languageFeature$experimental$macros$",
          "scala.languageFeature$experimental$macros", "scala.reflect.base.Base$build$emptyValDef$")
      blacklist.contains(x.name)
    }

    def validCandidateForJavaImport(x: ClassDef): Boolean = isPublic(x) && !x.notClassMember && !blacklisted(x)


    val importable = classes.filter(validCandidateForJavaImport)

    importable.zipWithIndex foreach {
      case (clazz, i) =>
        val importedType = JavaNames.sourceName(clazz.name, clazz.innerClasses)
        if (JavaNames.isValid(importedType)) {
          val javaClassName = "C" + i
          val f = wd / File(javaClassName + ".java")
          f.createFile(false)
          f.writeAll(generateJavaClass(javaClassName, importedType, clazz.name))
          generatedClassCount += 1
        }
    }

    val classpath = input match {
      case Args.ClassDir(name) => Path(name).toAbsolute.toString
      case Args.JarFile(_) => sys.error("not implemented yet")
    }

    val errors = Ecj.compileJavaFiles(wd, classpath)
    errors foreach { x =>
      errorCount += 1
      Console.err.println(x + "\n")
    }
    if (errorCount == 0) {
      Console.err.println("No errors found among %s classes (%s non-anonymous, %s importable names, %s importable types)".format(
        anonymous.size + classes.size, classes.size, importable.size, generatedClassCount)
      )
    }
    else {
      Console.err.println("Found %1d errors".format(errorCount))
      exit(1)
    }
  }

  def prepareWorkDir(x: Args.WorkDir): Directory = {
    val p = Path(x.name)
    if (p.isDirectory) p.toDirectory.toAbsolute
    else p.createDirectory(true, true).toAbsolute
  }

  object Args {
    sealed abstract class Input

    case class ClassDir(name: String) extends Input
    case class JarFile(name: String) extends Input
    case class WorkDir(name: String)
  }

  def parseArgs(args: Array[String]): (Args.Input, Args.WorkDir, String => Boolean) = {
    import Args._
    args.toList match {
      case input :: workDir :: xs =>
        val arg1 = if (input endsWith ".jar") JarFile(input) else ClassDir(input)
        val arg2 = WorkDir(workDir)
        val arg3: String => Boolean = if (xs.isEmpty) _ => true else _ contains xs.head

        (arg1, arg2, arg3)

      case _ => usage()
    }
  }

  def usage() = {
    println(
"""Usage: badsigs <classDirectory|jarFile> <workDirectory> [filter string]
""")
    sys.exit(1)
  }

}
