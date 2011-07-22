package cc {
  class Outer {
    class A {
      class B
      object B
    }
  }
}
// cc/Outer$A$B$.class
// cc/Outer$A$B.class
// cc/Outer$A.class
// cc/Outer.class

package co {
  class Outer {
    object A {
      class B
      object B
    }
  }
}
// co/Outer$A$.class
// co/Outer$A$B$.class
// co/Outer$A$B.class
// co/Outer.class

package oc {
  object Outer {
    class A {
      class B
      object B
    }
  }
}
// oc/Outer$.class
// oc/Outer$A$B$.class
// oc/Outer$A$B.class
// oc/Outer$A.class
// oc/Outer.class

package oo {
  object Outer {
    object A {
      class B
      object B
    }
  }
}
// oo/Outer$.class
// oo/Outer$A$.class
// oo/Outer$A$B$.class
// oo/Outer$A$B.class
// oo/Outer.class

/**

All of these arrangements use the same names for class B and object B.
Outer$A$B$.class
Outer$A$B.class

So keeping in mind that the inner class arrangement must be completely
consistent, how should they be laid out? Consistent means that each
class has an inner class table listing the class it is contained in and
any classes which it contains, and that for any such entry in one class
there is a corresponding entry in another class which points back at it.

**/
