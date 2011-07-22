package foo
package s

class C_Outer {
  class C_Foo {
    class C_Bar { class C_Quux }
    object O_Bar
    
    class C_Bar2
    object O_Bar2 { class C_Quux }
  }
  object O_Foo {
    class C_Quux {
      object O_Bippy
    }
    object O_Quux {
      class C_Dingus
    }
  }
  class TParam[B] {
    class C_Pippo
    object O_Pippo
  }
  object TParam {
    class C_Waldo
    object O_Waldo
  }
  
  class LazyVal[C] {
    lazy val C_LV_Fred = new AnyRef { }
  }
  object LazyVal {
    lazy val O_LV_Fred = new AnyRef { }
  }
}


