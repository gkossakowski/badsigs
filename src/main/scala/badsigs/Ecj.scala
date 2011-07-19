package badsigs
import scala.tools.nsc.io.Directory
import java.io.PrintWriter
import java.io.StringWriter

object Ecj {
  
  def compileJavaFiles(d: Directory, cp: String): Iterator[String] = {
    val javaFiles = d.walkFilter(x => x.isFile && x.hasExtension("java"))
    val errors = javaFiles flatMap { f =>
      import org.eclipse.jdt.core.compiler.batch.BatchCompiler
      val errStr = new StringWriter()
      val err = new PrintWriter(errStr)
      val cmd = "-1.5 -nowarn -d none -classpath %1s %2s".format(cp, f.toString) 
      val success = BatchCompiler.compile(cmd, new PrintWriter(System.out), err, null);
      if (success) None else Some(errStr.toString)
    }
    errors
  }

}
