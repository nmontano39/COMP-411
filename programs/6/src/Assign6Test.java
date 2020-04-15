import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

/** testing framework for typed jam.  **/
public class Assign6Test extends TestCase {

  public Assign6Test (String name) {
    super(name);
  }  
  private void evalCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.eval().toString());
  }
  private void cpsEvalCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.cpsEval().toString());
  }
  private void SDEvalCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.SDEval().toString());
  }
  private void CpsSDEvalCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value-value " + name, answer, interp.SDCpsEval().toString());
  }

  private void toDeleteCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value-value " + name, answer, interp.convertToCPS().toString());

  }

  private void allEvalCheck(String name, String answer, String program) {
    evalCheck(name, answer, program);
    cpsEvalCheck(name, answer, program);
    SDEvalCheck(name, answer, program);
    CpsSDEvalCheck(name, answer, program);
  }
  private void nonCpsEvalCheck(String name, String answer, String program) {
    evalCheck(name, answer, program);
    SDEvalCheck(name, answer, program);
  }
  
  private void unshadowCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));

      String result = interp.unshadow().toString();
      assertEquals("shadowCheck " + name, answer, result);
  }

  private void cpsCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      String result = interp.convertToCPS().toString();
      assertEquals(name, answer, result);
  }

  private void SDCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      String result = interp.convertToSD().toString();
      assertEquals(name, answer, result);
  }

  public void testBadLetrec() {
    try {
      String output = "!";
      String input = "letrec x:=4; in x";
      allEvalCheck("badLetrec", output, input );

         fail("badLetrec did not throw ParseException exception");
      } catch (ParseException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("badLetrec threw " + e);
    }
  } //end of func
  

  public void testBadLet() {
    try {
      String output = "!";
      String input = "let x:= map z to y(z);\n             y:= map z to x(z); in x(5)";
      allEvalCheck("badLet", output, input );

         fail("badLet did not throw SyntaxException exception");
      } catch (SyntaxException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("badLet threw " + e);
    }
  } //end of func
  
  public void testSimpleCmap() {
    try {
      String output = "(map x to x)(let x:1 := 1; in x:1)";
      String input = "(map x to x)(1)";
      cpsCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }

  }

  public void testUuop() {
    try {
      String output = "(3 + 3)";
      String input = "3 + 3";
      unshadowCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  } //end of func

  public void testUnOpSimple() {
    try {
      String output = "(map :0 to (map x to x)(- :0))(let x:1 := 1; in x:1)";
      String input = "-((map x to x)(1))";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testUnOpComplex() {
    try {
      String output = "let square:1 := map x:1,:1 to :1((x:1 * x:1)); in square:1(2, map :0 to (map x to x)(- :0))";
      String input = "-(let square := map x to x*x; in square(2))";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testBinOpSimple() {
    try {
      String output = "(map :0 to (map :1 to (map x to x)((:0 * :1)))(let x:1 := 10; in x:1))(let z:1 := 5; in z:1)";
      String input = "((map z to z)(5))*((map x to x)(10))";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testBinOpComplex() {
    try {
      String output = "let square:1 := map x:1,:3 to :3((x:1 * x:1)); in square:1(2, map :2 to (map :0 to let p:1 := ref 73; in (map :1 to (map x to x)((:0 * :1)))(let w:2 := ! p:1; in w:2))(- :2))";
      String input = "-(let square := map x to x*x; in square(2)) * (let p := ref 73; in (map w to w)(!p))";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testIfSimple() {
    try {
      String output = "if ~ false then (map x to x)(let x:1 := 7; in x:1) else (map x to x)(50)";
      String input = "if ~false then (map x to x)(7) else 50";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testIfComplex1() {
    try {
      String output = "(map :0 to (map x to x)(if :0 then 15 else 50))(let x:1 := false; in ~ x:1)";
      String input = "if (map x to ~x)(false) then 15 else 50";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testIfComplex2() {
    try {
      String output = "let square:1 := map x:1,y:1,:2 to :2(if x:1 then asBool(y:1) else false); in square:1(true, false, map :1 to (map :0 to (map x to x)(if :0 then 23 else 13))(~ :1))";
      String input = "if ~(let square := map x, y to x & y; in square(true, false)) then 23 else 13";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testBlockSimple() {
    try {
      String output = "(map x to x)({map x:1,:0 to :0(x:1); map y:1,:1 to :1((y:1 * y:1)); let simple:1 := 5; in simple:1})";
      String input = "{map x to x; map y to y*y; let simple := 5; in simple}";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }

  public void testBlockComplex() {
    try {
      String output = "let a:1 := ref 5; in let func:1 := map x:1,y:1,:1 to :1((x:1 * y:1)); in if (! a:1 < 10) then func:1(! a:1, ! a:1, map :0 to (map x to x)(:0)) else func:1(0, 0, map :0 to (map x to x)(:0))";
      String input = "{let a := ref 5; func := map x, y to x * y; in if !a < 10 then func(!a, !a) else func(0,0)}";
      toDeleteCheck("Uuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uuop threw " + e);
    }
  }


  public void testSuop() {
    try {
      String output = "(3 + 3)";
      String input = "3 + 3";
      SDCheck("Suop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Suop threw " + e);
    }
  } //end of func
  

  public void testCuop() {
    try {
      String output = "(map x to x)((3 + 3))";
      String input = "3 + 3";
      cpsCheck("Cuop", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cuop threw " + e);
    }
  } //end of func
  

  public void testBinOpApp() {
    try {
      String output = "6";
      String input = "3 + 3";
      allEvalCheck("uop", output, input );  // for Assignment 6, should be allEvalCheck

    } catch (Exception e) {
      e.printStackTrace();
      fail("uop threw " + e);
    }
  } //end of func
  

  public void testUdeep() {
    try {
      String output = "let x:1 := map x:1 to letrec x:2 := map x:3 to x:3; in x:2(x:2); y:1 := let x:1 := 5; in x:1; in x:1(y:1)";
      String input = "let x:= map x to \n     letrec x:=map x to x; in x(x);\n    y:= let x:=5; in x;\n  in x(y)";
      unshadowCheck("Udeep", output, input );
    } catch (Exception e) {
      e.printStackTrace();
      fail("Udeep threw " + e);
    }
  } //end of func

  public void testSdeep() {
    try {
      String output = "let [*2*] map [*1*] to letrec [*1*] map [*1*] to [0,0]; in [0,0]([0,0]); " +
                                "let [*1*] 5; in [0,0]; in [0,0]([0,1])";
      String input = "let x:= map x to letrec x := map x to x; in x(x); " +
                         "y:= let x:=5; in x; " +
                     "in x(y)";
      SDCheck("Sdeep", output, input );
    } catch (Exception e) {
      e.printStackTrace();
      fail("Sdeep threw " + e);
    }
  } //end of func
  
  public void testUlet() {
    try {
      String output = "let id:1 := map x:1 to x:1; in id:1(10)";
      String input = "let id := map x to x; in id(10)";
      unshadowCheck("[0.50] ulet", output, input );
    } catch (Exception e) {
      e.printStackTrace(System.err);
      fail("[0.50] ulet threw " + e);
    }
  } //end of func

  public void testUmap() {
    try {
      String output = "map z:1 to z:1";
      String input = "map z to z";
      unshadowCheck("Umap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Umap threw " + e);
    }
  } //end of func

// /* TODO: testing SD */
  public void testSmap() {
    try {
      String output = "(closure: map [*2*] to [0,1]([0,0]))";
      String input = "map z to z";
      CpsSDEvalCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  } //end of func

  public void testSmapEval() {
    try {
      String output = "(closure: map [*1*] to [0,0])";
      String input = "map z to z";
      SDEvalCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  } //end of func

  public void testSmap2() {
    try {
      String output = "map [*2*] to ([0,0] + [0,1])";
      String input = "map x,y to x + y";
      SDCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSmap2Eval() {
    try {
      String output = "(closure: map [*2*] to ([0,0] + [0,1]))";
      String input = "map x,y to x + y";
      SDEvalCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSmap3() {
    try {
      String output = "map [*1*] to map [*2*] to (([1,0] + [0,0]) + [0,1])";
      String input = "map x to map y, z to x + y + z";
      SDCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSmap4() {
    try {
      String output = "map [*1*] to map [*1*] to map [*1*] to (([2,0] + [1,0]) + [0,0])";
      String input = "map x to map y to map z to x + y + z";
      SDCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSmap4Eval() {
    try {
      String output = "(closure: map [*1*] to map [*1*] to map [*1*] to (([2,0] + [1,0]) + [0,0]))";
      String input = "map x to map y to map z to x + y + z";
      SDEvalCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSmapLet() {
    try {
      String output = "let [*1*] map [*1*] to map [*1*] to ([1,0] + [0,0]); in ([0,0](2))(3)";
      String input = "let x:= map y to map z to y + z; in (x(2))(3)";
      SDCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSLetEval() {
    try {
      String output = "2";
      String input = "let x := 1; y:= 2; in if x > 0 then y else y+2";
      allEvalCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }



  public void testSIf1() {
    try {
      String output = "let [*1*] 1; in if ([0,0] > 0) then 5 else 10";
      String input = "let x := 1; in if x > 0 then 5 else 10";
      SDCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }

  public void testSIfEval1() {
    try {
      String output = "5";
      String input = "let x := 1; in if x > 0 then 5 else 10";
      allEvalCheck("Smap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Smap threw " + e);
    }
  }


  public void testCmap() {
    try {
      String output = "(map x to x)(map z:1,:0 to :0(z:1))";
      String input = "map z to z";
      cpsCheck("Cmap", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cmap threw " + e);
    }
  } //end of func
  

  public void testCarity() {
    try {
      String output = "(map x to x)(map x,k to k((arity(x) - 1)))";
      String input = "arity";
      cpsCheck("Carity", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Carity threw " + e);
    }
  } //end of func
  

  public void testCfirst() {
    try {
      String output = "(map x to x)(map x,k to k(first(x)))";
      String input = "first";
      cpsCheck("Cfirst", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cfirst threw " + e);
    }
  } //end of func
  

  public void testCcons() {
    try {
      String output = "(map x to x)(map x,y,k to k(cons(x, y)))";
      String input = "cons";
      cpsCheck("Ccons", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Ccons threw " + e);
    }
  } //end of func
  

  public void testClist() {
    try {
      String output = "(map x to x)(first(rest(rest(cons(1, cons(2, cons(3, null)))))))";
      String input = "first(rest(rest(cons(1, cons(2, cons(3, null))))))";
      cpsCheck("Clist", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Clist threw " + e);
    }
  } //end of func
  

  public void testUappend() {
    try {
      String output = "letrec append:1 := map x:2,y:2 to if (x:2 = null) then y:2 else cons(first(x:2), append:1(rest(x:2), y:2)); in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      unshadowCheck("Uappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uappend threw " + e);
    }
  } //end of func

// /* TODO: testing SD and letrec*/
  public void testSappend() {
    try {
      String output = "letrec [*1*] " +
                        "map [*2*] to if ([0,0] = null) then [0,1] else cons(first([0,0]), [1,0](rest([0,0]), [0,1])); " +
                      "in let [*1*] cons(1, cons(2, cons(3, null))); " +
                         "in [1,0]([0,0], [0,0])";
      String input = "letrec append := map x,y to \n" +
                         "if (x = null) then y else cons(first(x), append(rest(x), y)); \n" +
                         "in let s := cons(1, cons(2, cons(3, null))); in append(s, s)";
      SDCheck("Sappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sappend threw " + e);
    }
  } //end of func

  public void testSappendEval() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "letrec append := map x,y to \n" +
                         "if (x = null) then y else cons(first(x), append(rest(x), y)); \n" +
                         "in let s := cons(1, cons(2, cons(3, null))); in append(s, s)";
      allEvalCheck("Sappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sappend threw " + e);
    }
  }

  public void testCappend() {
    try {
      String output = "letrec append:1 := map x:2,y:2,:0 to if (x:2 = null) then :0(y:2) " +
                                                           "else let :1 := first(x:2); " +
                                                                "in append:1(rest(x:2), y:2, map :2 to :0(cons(:1, :2))); " +
                      "in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2, map x to x)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      cpsCheck("Cappend", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cappend threw " + e);
    }
  } //end of func
  

  public void testAppend() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      allEvalCheck("append", output, input );  // should be allEvalCheck for Assignment 6

    } catch (Exception e) {
      e.printStackTrace();
      fail("append threw " + e);
    }
  } //end of func

  public void testLetRecCPS() {
    try {
      String output = "letrec append:1 := map x:2,y:2,:0 to if (x:2 = null) then :0(y:2) else let :1 := first(x:2); in append:1(rest(x:2), y:2, map :2 to :0(cons(:1, :2))); in let s:2 := cons(1, cons(2, cons(3, null))); in append:1(s:2, s:2, map x to x)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      cpsCheck("append", output, input );  // should be allEvalCheck for Assignment 6

    } catch (Exception e) {
      e.printStackTrace();
      fail("append threw " + e);
    }
  }


  public void testUappend1() {
    try {
      String output = "letrec appendz1:1 := map xz2:2,yz2:2,z0:2 to if (xz2:2 = null) then z0:2(yz2:2) else let z1:3 := first(xz2:2); in appendz1:1(rest(xz2:2), yz2:2, map z3:4 to z0:2(let z2:5 := z3:4; in cons(z1:3, z2:5))); in let sz2:2 := cons(1, cons(2, cons(3, null))); in appendz1:1(sz2:2, sz2:2, map x:3 to x:3)";
      String input = "letrec appendz1 := map xz2,yz2,z0 to if (xz2 =null) then z0(yz2) else let z1 := first(xz2); in appendz1(rest(xz2), yz2, map z3 to z0(let z2 := z3; in cons(z1, z2))); in let sz2 := cons(1, cons(2, cons(3, null))); in appendz1(sz2, sz2, map x to x)";
      unshadowCheck("Uappend1", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uappend1 threw " + e);
    }
  } //end of func

  public void testCappend1() {
    try {
      String output = "letrec appendz1:1 := map xz2:2,yz2:2,z0:2,:0 to if (xz2:2 = null) then z0:2(yz2:2, :0) else let z1:3 := first(xz2:2); in appendz1:1(rest(xz2:2), yz2:2, map z3:4,:1 to z0:2(let z2:5 := z3:4; in cons(z1:3, z2:5), :1), :0); in let sz2:2 := cons(1, cons(2, cons(3, null))); in appendz1:1(sz2:2, sz2:2, map x:3,:2 to :2(x:3), map x to x)";
      String input = "letrec appendz1 := map xz2,yz2,z0 to if (xz2 =null) then z0(yz2) else let z1 := first(xz2); in appendz1(rest(xz2), yz2, map z3 to z0(let z2 := z3; in cons(z1, z2))); in let sz2 := cons(1, cons(2, cons(3, null))); in appendz1(sz2, sz2, map x to x)";
      cpsCheck("Cappend1", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cappend1 threw " + e);
    }
  } //end of func

  public void testSfact() {
    try {
      String output = "let [*1*] 6; " +
                      "in letrec [*1*] map [*1*] to let [*1*] map [*1*] to [1,0](map [*1*] to ([1,0]([1,0]))([0,0])); " +
                                                    "in [0,0]([0,0]); " +
                         "in let [*1*] map [*1*] to map [*1*] to if ([0,0] = 0) then 1 else ([0,0] * [1,0](([0,0] - 1))); " +
                            "in ([1,0]([0,0]))([2,0])";
      String input = "let n:= 6; " +
                     "in letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g); " +
                        "in let FACT := map f to map n to if n = 0 then 1 else n * f(n - 1); " +
                           "in (Y(FACT))(n)";
      SDCheck("Sfact", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sfact threw " + e);
    }
  } //end of func

  public void testSfactEval() {
    try {
      String output = "720";
      String input = "let n:= 6; " +
                         "in letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g); " +
                         "in let FACT := map f to map n to if n = 0 then 1 else n * f(n - 1); " +
                         "in (Y(FACT))(n)";
      allEvalCheck("Sfact", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Sfact threw " + e);
    }
  } //end of func


  public void testCfact() {
    try {
      String output = "let Y:1 := map f:1,:0 to " +
                                   "let g:2 := map x:2,:1 to f:1(map z:3,:2 to x:2(x:2, map :3 to let :4 := z:3; in :3(:4, :2)), :1); " +
                                   "in g:2(g:2, :0); " +
                      "in let FACT:1 := map f:1,:5 to :5(map n:2,:6 to if (n:2 = 0) then :6(1) else let :7 := n:2; in f:1((n:2 - 1), map :8 to :6((:7 * :8)))); " +
                         "in Y:1(FACT:1, map :9 to let :10 := 6; in :9(:10, map x to x))";
      String input = "let Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);" +
                         "FACT := map f to map n to if n = 0 then 1 else n * f(n - 1); " +
                     "in (Y(FACT))(6)";
      cpsCheck("Cfact", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cfact threw " + e);
    }
  } //end of func
  

  public void testFact() {
    try {
      String output = "720";
      String input = "let n:= 6; in\n   letrec Y := map f to let g := map x to f(map z to (x(x))(z)); in g(g);\n   in \n    let \n       FACT := map f to map n to if n = 0 then 1 else n * f(n - 1);\n      in (Y(FACT))(n)";
      allEvalCheck("fact", output, input );  // for Assignment 6, allEvalCheck should be used.

    } catch (Exception e) {
      e.printStackTrace();
      fail("fact threw " + e);
    }
  } //end of func
  

  public void testUletcc() {
    try {
      String output = "letcc x:1 in if true then x:1(5) else 3";
      String input = "letcc x in  if true then  x(5)  else 3";
      unshadowCheck("Uletcc", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Uletcc threw " + e);
    }
  } //end of func

  public void testCletcc() {
    try {
      String output = "let x:1 := map :0,:1 to (map x to x)(:0); in if true then x:1(5, map x to x) else (map x to x)(3)";
      String input = "letcc x in  if true then  x(5)  else 3";
      cpsCheck("Cletcc", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("Cletcc threw " + e);
    }
  } //end of func

    public void testLetcc1() {
    cpsEvalCheck("<letcc1>", "1", "letcc x in 1");
  }

  public void testLetcc2() {
    cpsEvalCheck("<letcc2>", "1", "letcc x in x(1)");
  }
  public void testLetcc3() {
    cpsEvalCheck("<letcc3>", "10",
             "let prod := map s1 to " +
             "              letcc escape " +
             "              in letrec prodHelp := " +
             "                 map s to if null?(s) then 1 " +
             "                          else if ~number?(first(s)) then escape(0) " +
             "                          else first(s) * prodHelp(rest(s)); " +
             "               in prodHelp(s1); " +
             "in prod(cons(2,cons(5,null)))");
  }
}





