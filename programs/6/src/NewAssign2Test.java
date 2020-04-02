import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

public class NewAssign2Test extends TestCase {

  public NewAssign2Test (String name) { super(name); }
  
  /** The following 3 check methods create an interpreter object with the specified String as the program, invoke the 
    * respective evaluation method (callByValue, callByName, callByNeed), and check that the result matches the (given)
    * expected output.  If the test fails, the method prints a report as to which test failed and how many points should
    * be deducted.
    */
 
  private void valueCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value " + name, answer, interp.callByValue().toString());
  }

  private void nameCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-name " + name, answer, interp.callByName().toString());
  }
   
  private void needCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-need " + name, answer, interp.callByNeed().toString());
  }

  private void allCheck(String name, String answer, String program) {
    valueCheck(name, answer, program);
    nameCheck(name, answer, program);
    needCheck(name, answer, program);
  } 

  public void testNumberP() {
    try {
      String output = "number?";
      String input = "number?";
      allCheck("numberP", output, input );
    } 
    catch (Exception e) {
//      e.printStackTrace();
      fail("numberP threw " + e);
    }
  } //end of func
  
  public void testMathOp() {
    try {
      String output = "18";
      String input = "2 * 3 + 12";
      allCheck("mathOp", output, input );
    } 
    catch (Exception e) {
//      e.printStackTrace();
      fail("mathOp threw " + e);
    }
  } //end of func
  
  public void testParseException() {
    try {
      String output = "haha";
      String input = " 1 +";
      allCheck("parseException", output, input );
      fail("parseException did not throw ParseException exception");
    } 
    catch (ParseException e) { /* Success; threw a ParseException */ } 
    catch (Exception e) {
//      e.printStackTrace();
      fail("parseException threw " + e);
    }
  } //end of func

  public void testEvalException() {
    try {
      String output = "mojo";
      String input = "1 + number?";
      allCheck("evalException", output, input );
      fail("evalException did not throw EvalException exception");
      } 
    catch (EvalException e) {  /* Success; threw an EvalException */  }
    catch (Exception e) {
//      e.printStackTrace();
      fail("evalException threw " + e);
    }
  } //end of func
  
  public void testAppend() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = 
        "let    Y := map f to " +
        "              let g := map x to f(map z1,z2 to (x(x))(z1,z2)); " +
        "              in g(g); " +
        "  APPEND := map ap to " +
        "              map x,y to " +
        "                if x = null then y else cons(first(x), ap(rest(x), y)); " +
        "       l := cons(1,cons(2,cons(3,null))); " + 
        "in (Y(APPEND))(l,l)";
      allCheck("append", output, input );
    } 
    catch (Exception e) {
//      e.printStackTrace();
      fail("append threw " + e);
    }
  } //end of func
  
  public void testFib() {
    try {
      String output =
        "((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89) (11 144) (12 233) (13 377) (14 610) " +
        "(15 987) (16 1597) (17 2584) (18 4181) (19 6765) (20 10946) (21 17711) (22 28657) (23 46368) (24 75025) (25 121393))";
      String input = 
        "let     Y := map f to let g := map x to f(x(x)); in g(g); " +
        "      FIB := map fib to map n to if n <= 1 then 1 else fib(n - 1) + fib(n - 2); " +
        "  FIBHELP := map fibhelp to " +
        "               map k,fn,fnm1 to if k = 0 then fn else fibhelp(k - 1, fn + fnm1, fn); " +
        "     pair := map x,y to cons(x, cons(y, null)); " +
        "in let FFIB := map ffib to map n to if n = 0 then 1 else (Y(FIBHELP))(n - 1,1,1); " +
        "   in let ffib := Y(FFIB); " +
        "      in let FIBS := map fibs to map k,l to " +
        "                       let fibk := ffib(k); " +
        "                       in if 0 <= k then fibs(k - 1, cons(pair(k,fibk), l)) else l; " +
        "   in (Y(FIBS))(25, null)";
      needCheck("fib-need", output, input);
      nameCheck("fib-name", output, input);
    }
     catch(Exception e) {
//      e.printStackTrace();
      fail("fib threw " + e);
    }
  } // end test2Fib
}
