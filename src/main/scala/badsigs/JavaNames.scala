package badsigs

object JavaNames {

  def sourceName(binaryName: String, innerClasses: Map[String, InnerClassEntry]): String = 
    innerClasses.get(binaryName) match {
      case Some(InnerClassEntry(external, outer, name, _)) => sourceName(outer, innerClasses) + "." + name
      case None => binaryName
    }

  def isValid(name: String) = {
    val javaKeywords = Set("package", "strictfp", "switch", "native", "throws", "volatile", "transient")
    val badSuffixes = javaKeywords.map("."+_)
    val badInfixes = javaKeywords.map("."+_+".")
    !(badSuffixes.exists(x => name.endsWith(x)) || badInfixes.exists(x => name.contains(x)))
  }

}
