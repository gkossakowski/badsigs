package foo.j;

/*

Java disallows this structure:

class Outer {
  class Inner {
    static class Inner2 { }
  }
}

It says "modifier static not allowed here".

Allowed in scala, the sort-of equivalent:

class Outer {
  class Inner {
    object Inner2 { }
  }
}

*/

class C_Outer<A> {  
  class C_Foo {
    class C_Bar { class C_Quux { } }
    // static class O_Bar { }
    
    class C_Bar2 { }
    // static class O_Bar2 {
    //   class C_Quux { }
    // }
  }
  static class O_Foo {
    class C_Quux {
      // static class O_Bippy { }
    }
    static class O_Quux {
      class C_Dingus { }
    }
  }
  class TParam<B> {
    class C_Pippo { }
    // static class O_Pippo { }
  }
  static class TParam$ {
    class C_Waldo { }
    static class O_Waldo { }
  }
}
