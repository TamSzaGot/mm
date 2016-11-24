package main;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public class Main {
  public static void main(String[] args) {
    Var REQUIRE = RT.var("clojure.core", "require");
    Var APPLY = RT.var("clojure.core", "apply");
    REQUIRE.invoke(Symbol.intern("backend.main"));
    APPLY.invoke(RT.var("backend.main", "-main"), args);
  }
}

