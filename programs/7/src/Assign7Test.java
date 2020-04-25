import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

/**
 * testing framework for typed jam
 *
 **/
public class Assign7Test extends TestCase {
  protected int defaultSize = 50000;

  public Assign7Test (String name) {
    super(name);
  }


  private void eagerCheck(String name, String answer, String program) {
    eagerCheck(name,answer,program, defaultSize);
  }
  private void eagerCheck(String name, String answer, String program, int hs) {
    Interpreter interp = new Interpreter(new StringReader(program), hs);
    assertEquals("by-value-value " + name, answer, interp.eval().toString());
  }

  private void cpsCheck(String name, String answer, String program, int hs) {
    Interpreter interp = new Interpreter(new StringReader(program), hs);
    assertEquals("by-value-value " + name, answer, interp.cpsEval().toString());
  }
  /*
    private void SDEagerCheck(String name, String answer, String program, int hs) {
        Interpreter interp = new Interpreter(new StringReader(program), hs);
        assertEquals("by-value-value " + name, answer, interp.SDEval().toString());
    }
  */
  private void GCSDCpsCheck(String name, String answer, String program, int hs) {
    SDCpsCheck(name,answer,program,hs);
  }

  private void SDCpsCheck(String name, String answer, String program, int hs) {
    System.out.printf("%n----: %.7s ----%n", name);
    
    Interpreter interp = new Interpreter(new StringReader(program), hs);
    
    int count = 0;
    
    int[] heap0 = (int[])interp.getMemory().clone(); //shallow copy works here
    assertEquals("by-value-value " + name, answer, interp.SDCpsEval().toString());
    int[] heap1 = (int[])interp.getMemory().clone();

    for (int i=0; i< heap0.length; i++) {
      if (heap0[i] != heap1[i]) { count++; }
    }

    System.out.printf("memory size [%d] : %.7s%n", count, name);
  }


  private void allCheck(String name, String answer, String program) {
    allCheck(name,answer,program, defaultSize);
  }
  private void allCheck(String name, String answer, String program, int hs) {
    eagerCheck(name, answer, program, hs);
    cpsCheck(name, answer, program, hs);
//    SDEagerCheck(name, answer, program, hs);
    SDCpsCheck(name, answer, program, hs);
  }

  /*
    private void nonCPSCheck(String name, String answer, String program, int hs) {
      eagerCheck(name, answer, program, hs);
  //    SDEagerCheck(name, answer, program, hs);
    }
  */
  private void unshadowConvert(String name, String answer, String program) {
    unshadowConvert(name,answer,program, defaultSize);
  }

  private void unshadowConvert(String name, String answer, String program, int hs) {
    Interpreter interp = new Interpreter(new StringReader(program), hs);

    String result = renameVars(interp.unshadow()).toString();
    assertEquals("shadowCheck " + name, answer, result);
  }

  private void cpsConvert(String name, String answer, String program) {
    cpsConvert(name,answer, program, defaultSize);
  }

  private void cpsConvert(String name, String answer, String program, int hs) {
    Interpreter interp = new Interpreter(new StringReader(program), hs);

    String result = renameVars(interp.convertToCPS()).toString();
    assertEquals("shadowCheck " + name, answer, result);
  }

  private void sdConvert(String name, String answer, String program) {
    sdConvert(name,answer,program, 50000);
  }

  private void sdConvert(String name, String answer, String program,int hs) {
    Interpreter interp = new Interpreter(new StringReader(program), hs);

    String result = renameVars(interp.convertToSD()).toString();
    assertEquals("shadowCheck " + name, answer, result);
  }

  private AST renameVars(AST tree) { return tree; }



  public void testBadLetrec() {
    try {
      String output = "!";
      String input = "letrec x:=4; in x";
      allCheck("badLetrec", output, input );

      fail("badLetrec did not throw ParseException exception");
    } catch (ParseException e) {
      //e.printStackTrace();

    } catch (Exception e) {
      fail("badLetrec threw " + e);
    }
  } //end of func


  public void testBadLet() {
    try {
      String output = "!";
      String input = "let x:= map z to y(z);\n             y:= map z to x(z); in x(5)";
      allCheck("badLet", output, input );

      fail("badLet did not throw SyntaxException exception");
    } catch (SyntaxException e) {
      //e.printStackTrace();

    } catch (Exception e) {
      fail("badLet threw " + e);
    }
  } //end of func


  public void testAppend() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "letrec append := map x,y to\n          if x = null then y else cons(first(x), append(rest\n(x), y));\n            in let s := cons(1,cons(2,cons(3,null)));\n          in append(s,s)";
      allCheck("append", output, input );

    } catch (Exception e) {
      fail("append threw " + e);
    }
  } //end of func


  public void testBigfib() {
    try {
      String output = "((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89) (11 144) (12 233) (13 377) (14 610) (15 987) (16 1597) (17 2584) (18 4181) (19 6765) (20 10946) (21 17711) (22 28657) (23 46368) (24 75025) (25 121393) (26 196418) (27 317811) (28 514229) (29 832040) (30 1346269) (31 2178309) (32 3524578) (33 5702887) (34 9227465) (35 14930352) (36 24157817) (37 39088169) (38 63245986) (39 102334155) (40 165580141) (41 267914296) (42 433494437) (43 701408733) (44 1134903170) (45 1836311903))";
      String input = "\nletrec fib :=  map n to if n <= 1 then 1 else fib(n - 1) + fib(n - 2);\n       fibhelp := map k,fn,fnm1 to\n                    if k = 0 then fn\n                    else fibhelp(k - 1, fn + fnm1, fn);\n       pair := map x,y to cons(x, cons(y, null));\nin let ffib := map n to if n = 0 then 1 else fibhelp(n - 1,1,1);\n   in letrec fibs :=  map k,l to \n                        if 0 <= k then \n                        fibs(k - 1, cons(pair(k,ffib(k)), l))\n	                else l;\n      in fibs(45, null)\n";
      allCheck("bigfib", output, input );

    } catch (Exception e) {
      fail("bigfib threw " + e);
    }
  } //end of func


}




