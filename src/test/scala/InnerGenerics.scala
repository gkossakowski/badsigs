// No reason a person would expect object Foo to have less
// generic information available than class Foo.

class A[T] {
  // The class 'Foo' is currently made an inner class of A.
  //  public #5= #40 of #10; //Foo=class A$Foo of class A
  class Foo {
    var bar_c: T = _
  }
  // The object 'Foo' is currently lifted to A$Foo$ and not
  // designated an inner class of A.  An instance is stored
  // in A's member var Foo$module.
  //
  // private volatile A$Foo$ Foo$module;
  //
  // Since A$Foo$ is not parameterized on A, and neither
  // is it an inner class of A, it has no access to the necessary
  // generic information except through the scala signature.
  object Foo {
    var bar_o: T = _
  }
}

object Test {
  def main(args: Array[String]): Unit = {
    val a = new A[String]
    val c = new a.Foo
    val o = a.Foo
    
    List(o, c) flatMap (_.getClass.getMethods) filter (_.getName startsWith "bar") foreach { m =>
      println("%10s in %-10s    %s".format(
        m.getName, m.getDeclaringClass.getName, m.toGenericString)
      )
    }
  }
}
//     bar_o in A$Foo$        <java.lang.NullPointerException>
// bar_o_$eq in A$Foo$        <java.lang.NullPointerException>
//     bar_c in A$Foo         public T A$Foo.bar_c()
// bar_c_$eq in A$Foo         public void A$Foo.bar_c_$eq(T)
