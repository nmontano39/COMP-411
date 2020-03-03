import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

public class Assign3Test extends TestCase {

  public Assign3Test (String name) { super(name); }
  
  /** The following 9 check methods create an interpreter object with the specified String as the program, invoke the 
    * respective evaluation method (valueValue, valueName, valueNeed, etc.), and check that the result matches the 
    * (given) expected output.  
    */
 
  private void valueValueCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value-value " + name, answer, interp.valueValue().toString());
  }
  
  private void valueNameCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value-name " + name, answer, interp.valueName().toString());
  }
  
  private void valueNeedCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value-need " + name, answer, interp.valueNeed().toString());
  }
  
  private void nameValueCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value " + name, answer, interp.nameValue().toString());
  }

  private void nameNameCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-name " + name, answer, interp.nameName().toString());
  }
   
  private void nameNeedCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-need " + name, answer, interp.nameNeed().toString());
  }

  private void needValueCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-value " + name, answer, interp.needValue().toString());
  }

  private void needNameCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-name " + name, answer, interp.needName().toString());
  }
   
  private void needNeedCheck(String name, String answer, String program) {
      Interpreter interp = new Interpreter(new StringReader(program));
      assertEquals("by-need " + name, answer, interp.needNeed().toString());
  }

  private void allCheck(String name, String answer, String program) {
    valueValueCheck(name, answer, program);
    valueNameCheck(name, answer, program);
    valueNeedCheck(name, answer, program);
    nameValueCheck(name, answer, program);
    nameNameCheck(name, answer, program);
    nameNeedCheck(name, answer, program);
    needValueCheck(name, answer, program);
    needNameCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }

  private void noNameCheck(String name, String answer, String program) {
    valueValueCheck(name, answer, program);
    valueNameCheck(name, answer, program);
    valueNeedCheck(name, answer, program);
    needValueCheck(name, answer, program);
    needNameCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }

  private void needCheck(String name, String answer, String program) {
    needValueCheck(name, answer, program);
    needNeedCheck(name, answer, program);
  }

  private void lazyCheck(String name, String answer, String program) {
    valueNameCheck(name, answer, program);
    valueNeedCheck(name, answer, program);
    nameNameCheck(name, answer, program);
    nameNeedCheck(name, answer, program);
    needNameCheck(name, answer, program);
    needNeedCheck(name, answer, program);
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
    catch (ParseException e) {  /* Success; ParseException thrown */ }  
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
    catch (EvalException e) {  /* Success; EvalException thrown */ }  
    catch (Exception e) {
//      e.printStackTrace();
      fail("evalException threw " + e);
    }
  } //end of func
  
  public void testMapEquals() {
    try {
      String output = "true";
      String input = "let id := map x to x; in id = id";
      valueValueCheck("[0.75] MapEquals", output, input);
      needValueCheck("[0.75] MapEquals", output, input);
      nameValueCheck("[0.75] MapEquals", "false", input);
    }
    catch(Exception e) {
//      e.printStackTrace();
      fail("[0.75] MapEquals threw " + e);
    }  
  } //end of method
  
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
  

  public void testLetRec() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "let append := map x,y to if x = null then y else cons(first(x), append(rest(x), y)); " +   
                     "         l := cons(1,cons(2,cons(3,null))); " +
                     "in append(l,l)";
      allCheck("letRec", output, input);
    } 
    catch (Exception e) {
//      e.printStackTrace();
      fail("letRec threw " + e);
    }
  } //end of func

  public void testLazyCons() {
    try {
      String output = "0";
      String input = "let zeroes := cons(0,zeroes);in first(rest(zeroes))";
      lazyCheck("lazyCons", output, input );

    } catch (Exception e) {
//      e.printStackTrace();
      fail("lazyCons threw " + e);
    }
  } //end of func
  
  public void testForwardRef() {
    try {    
      String output = "Error";
      String input = "let a := a; in a";
      allCheck("[0.50] forwardRef", output, input);
      fail("[0.50] forwardRef did not throw EvalException");
    } 
    catch (EvalException e) {  /* Success! badLet1 threw a ParseException;  */  }
    catch (Exception e) { 
//      e.printStackTrace();
      fail("forwardRef threw Exception " + e + " rather than an EvalException"); 
    }
  }
  
  public void testBinOpAsFun() {
    try {
      String output = "7";
      String input = "let f := +; in f(3,4)";
      allCheck("+ as PrimFun", output, input);
      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {  /* Success! + rejected as PrimFun  */  }
  }

  public void testRef0() {
    try {
      String output = "(ref (ref 17))";
      String input = "let x := ref 10; in {x <- ref 17; x}";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

  public void testRef1() {
    try {
      String output = "false";
      String input = "let x := ref 10; y := ref 10; in x = y";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

  public void testRef2() {
    try {
      String output = "true";
      String input = "ref?(ref 10)";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

    public void testRef3() {
        try {
            String output = "10";
            String input = "let x := ref ref 10; in ! ! x";
            allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
        }
        catch(Exception e) {
            fail("forwardRef threw Exception " + e + " rather than an EvalException");
        }
    }

  public void testBang0() {
    try {
      String output = "9";
      String input = "let x := ref (map x to x * x)(3); in ! x";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

  public void testUnit0() {
    try {
      String output = "unit";
      String input = "let x := let x := ref 9; in x <- 6; in !x";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }


  public void testBlock0() {
    try {
      String output = "20";
      String input = "({ 10 * 3; 10; 10*2 })";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

  public void testBlock1() {
    try {
      String output = "125";
      String input = "let x := 5; in { x * x; x * x; x * x * x }";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

  public void testBlock2() {
    try {
      String output = "5";
      String input = "let x := ref 10; y := 5; in { x <- y; !x }";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted a/.as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }

  public void testBlock3() {
    try {
      String output = "3";
      String input = "let x := ref 10; y := 3; in { x <- ref y; !!x }";
      allCheck("+ as PrimFun", output, input);
//      fail("+ accepted as PrimFun");
    }
    catch(Exception e) {
      fail("forwardRef threw Exception " + e + " rather than an EvalException");
    }
  }
}





