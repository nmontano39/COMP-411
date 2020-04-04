import java.util.StringTokenizer;
import junit.framework.TestCase;
import java.io.*;

/**
 * testing framework for typed jam
 *
 **/
public class Assign5Test extends TestCase {

  public Assign5Test (String name) {
    super(name);
  }


  private void eagerCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value-value " + name, answer, interp.eagerEval().toString());
  }

  private void lazyCheck(String name, String answer, String program) {
    Interpreter interp = new Interpreter(new StringReader(program));
    assertEquals("by-value-name " + name, answer, interp.lazyEval().toString());
  }

  private void allCheck(String name, String answer, String program) {
    eagerCheck(name, answer, program);
    lazyCheck(name, answer, program);
  }



  public void testMathOp() {
    try {
      String output = "18";
      String input = "2 * 3 + 12";
      allCheck("mathOp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("mathOp threw " + e);
    }
  } //end of func


  public void testParseException() {
    try {
      String output = "haha";
      String input = " 1 +";
      allCheck("parseException", output, input );

      fail("parseException did not throw ParseException exception");
    } catch (ParseException e) {
      //e.printStackTrace();

    } catch (Exception e) {
      e.printStackTrace();
      fail("parseException threw " + e);
    }
  } //end of func


  public void testAppend() {
    try {
      String output = "(1 2 3 1 2 3)";
      String input = "let append:  (list int, list int -> list int) :=       map x: list int, y: list int to         if x = null: int then y else cons(first(x), append(rest(x), y));     s: list int := cons(1,cons(2,cons(3,null: int))); in append(s,s)";
      allCheck("append", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("append threw " + e);
    }
  } //end of func


  public void testNull() {
    try {
      String output = "()";
      String input = "null: list (int, bool, list ref int -> unit)";
      allCheck("null", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("null threw " + e);
    }
  } //end of func

  public void testNull2() {
    try {
      String output = "SHOULD RETURN -> ParseException: Expecting : but found end of input";
      String input = "null";
      allCheck("null", output, input );
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      fail("did not return ParseException");
    }
  } //end of func

  public void testNull3() {
    try {
      String output = "()";
      String input = "null : int";
      allCheck("null", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("null threw " + e);
    }
  } //end of func

  public void testNull4() {
    try {
      String output = "true";
      String input = "null : int = null : int)";
      allCheck("null", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("null threw " + e);
    }
  } //end of func

  public void testNull5() {
    try {
      String output = "SHOULD RETURN -> TypeException: Expected type list int, but found list bool";
      String input = "null : int = null : bool";
      allCheck("null", output, input );

    } catch (TypeException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      fail("did not return TypeException");
    }
  } //end of func


  public void testList() {
    try {
      String output = "1";
      String input = "first(cons(1, null : int))";
      allCheck("null", output, input );

    } catch (TypeException e) {
      e.printStackTrace();
      fail("null in list threw" + e);
    }
  } //end of func

  public void testList2() {
    try {
      String output = "SHOULD RETURN -> TypeException: cons arg type mismatch: {}, {}intlist bool";
      String input = "first(cons(1, null : bool))";
      allCheck("null", output, input );

    } catch (TypeException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      fail("did not return TypeException");
    }
  } //end of func

  public void testType() {
    try {
      String output = "9";
      String input = "let x: int := 2; in x+7";
      allCheck("null", output, input );

    } catch (TypeException e) {
      e.printStackTrace();
      fail("null in list threw" + e);
    }
  } //end of func

  public void testType2() {
    try {
      String output = "SHOULD RETURN -> TypeException: Declared type bool doesn't match expression type int";
      String input = "let x: bool := 0; in x = true";
      allCheck("null", output, input );

    } catch (TypeException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
      fail("did not return TypeException");
    }
  } //end of func


  public void testEmptyBlock() {
    try {
      String output = "0";
      String input = "{ }";
      allCheck("emptyBlock", output, input );

      fail("emptyBlock did not throw ParseException exception");
    } catch (ParseException e) {
      //e.printStackTrace();

    } catch (Exception e) {
      e.printStackTrace();
      fail("emptyBlock threw " + e);
    }
  } //end of func


  public void testBlock() {
    try {
      String output = "1";
      String input = "{3; 2; 1}";
      allCheck("block", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("block threw " + e);
    }
  } //end of func


  public void testDupVar() {
    try {
      String output = "ha!";
      String input = "let x: int :=3; x:int :=4; in x";
      allCheck("dupVar", output, input );

      fail("dupVar did not throw SyntaxException exception");
    } catch (SyntaxException e) {
      //e.printStackTrace();

    } catch (Exception e) {
      e.printStackTrace();
      fail("dupVar threw " + e);
    }
  } //end of func


  public void testRefApp() {
    try {
      String output = "(ref 17)";
      String input = "let x: ref int := ref 10; in {x <- 17; x}";
      allCheck("refApp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("refApp threw " + e);
    }
  } //end of func


  public void testBangApp() {
    try {
      String output = "10";
      String input = "let x: ref int := ref 10; in !x";
      allCheck("bangApp", output, input );

    } catch (Exception e) {
      e.printStackTrace();
      fail("bangApp threw " + e);
    }
  } //end of func


  public void testAssign() {
    try {
      String output = "true";
      String input = "let x: int :=5; y: bool :=true; in x !=y";
      allCheck("assign", output, input );

      fail("assign did not throw TypeException exception");
    } catch (TypeException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("assign threw " + e);
    }
  } //end of func


  public void testBadAssign() {
    try {
      String output = "0";
      String input = "let x: int := 10; in x <- 5";
      allCheck("badAssign", output, input );

      fail("badAssign did not throw TypeException exception");
    } catch (TypeException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail("badAssign threw " + e);
    }
  } //end of func


}




