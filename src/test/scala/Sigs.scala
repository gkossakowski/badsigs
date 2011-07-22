package bar {
  class CBar { 
    class CBaz
    object CBaz
  }
  object OBar {
    class OBaz
    object OBaz
  }
}
  
package foo {
  object OBar {
    object Baz { }
    case object Baz2 { }
    class Baz3 { def f = 5 }
    trait Baz4 { def f = 5 }
    case class Baz5(x: Int) { }
  }
  class CBar {
    object Spaz { }
    case object Spaz2 { }
    class Spaz3 { def f = 5 }
    trait Spaz4 { def f = 5 }
    case class Spaz5(x: Int) { }
  }
  class TBar[A] {
    object Yaz { }
    class Yeesh[B] { }
    case object Yaz2 { }
    class Yaz3 { def f = 5 }
    trait Yaz4 { def f = 5 }
    case class Yaz5(x: Int) { }
    
    object Flaz {
      def f(x: A) = x
    }
  }
}
