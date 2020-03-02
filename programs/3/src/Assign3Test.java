import java.util.StringTokenizer;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;

public class Assign3Test extends TestCase {

    public Assign3Test (String name) {
        super(name);
    }

    /**
     * The following 9 check methods create an interpreter object with the
     * specified String as the program, invoke the respective evaluation
     * method (valueValue, valueName, valueNeed, etc.), and check that the
     * result matches the (given) expected output.
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

    private void noValueCheck(String name, String answer, String program) {
        nameValueCheck(name, answer, program);
        nameNameCheck(name, answer, program);
        nameNeedCheck(name, answer, program);
        needValueCheck(name, answer, program);
        needNameCheck(name, answer, program);
        needNeedCheck(name, answer, program);
    }

    private void noNeedCheck(String name, String answer, String program) {
        valueValueCheck(name, answer, program);
        valueNameCheck(name, answer, program);
        valueNeedCheck(name, answer, program);
        nameValueCheck(name, answer, program);
        nameNameCheck(name, answer, program);
        nameNeedCheck(name, answer, program);
    }


    private void lazyCheck(String name, String answer, String program) {
        valueNameCheck(name, answer, program);
        valueNeedCheck(name, answer, program);
        nameNameCheck(name, answer, program);
        nameNeedCheck(name, answer, program);
        needNameCheck(name, answer, program);
        needNeedCheck(name, answer, program);
    }

    public void testSyntaxExceptionExists() {
        new SyntaxException("Is it defined?");
    } //end of func

    public void testNumberP() {
        try {
            String output = "number?";
            String input = "number?";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    } //end of func


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


    public void testFib() {
        try {
            String output = "((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89) (11 144) (12 233) (13 377) (14 610) (15 987) (16 1597) (17 2584) (18 4181) (19 6765) (20 10946) (21 17711) (22 28657) (23 46368) (24 75025) (25 121393))";
            String input = "let Y    := map f to\n" +
                    "              let g := map x to f(x(x));\n" +
                    "              in g(g);\n" +
                    "    FIB  := map fib to\n" +
                    "              map n to if n <= 1 then 1 else fib(n - 1) + fib(n - 2);\n" +
                    " FIBHELP := map fibhelp to\n" +
                    "              map k,fn,fnm1 to\n" +
                    "                if k = 0 then fn\n" +
                    "                else fibhelp(k - 1, fn + fnm1, fn);\n" +
                    "    pair := map x,y to cons(x, cons(y, null));\n" +
                    "in let FFIB := map ffib to\n" +
                    "                 map n to if n = 0 then 1 else (Y(FIBHELP))(n - 1,1,1);\n" +
                    "   in let ffib := Y(FFIB);\n" +
                    "      in let FIBS := map fibs to\n" +
                    "                       map k,l to\n" +
                    "                         let fibk := ffib(k);\n" +
                    "                         in if 0 <= k then\n" +
                    "                              fibs(k - 1, cons(pair(k,fibk), l))\n" +
                    "                            else l;\n" +
                    "         in (Y(FIBS))(25, null)";
            noValueCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }


    public void testIn1() {
        try {
            String output = "6";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(map z to (x(x))(z));\n" +
                    "\t    in g(g);\n" +
                    "    FACT := map f to \n" +
                    "\t      map n to if n = 0 then 1 else n * f(n - 1);\n" +
                    "in (Y(FACT))(3)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testIn2() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(map z1,z2 to (x(x))(z1,z2));\n" +
                    "\t    in g(g);\n" +
                    "    APPEND := map ap to \n" +
                    "\t        map x,y to \n" +
                    "                  if x = null then y else cons(first(x), ap(rest(x), y));\n" +
                    "    l      := cons(1,cons(2,cons(3,null)));\t\n" +
                    "in (Y(APPEND))(l,l)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testName1() {
        try {
            String output = "6";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(x(x));\n" +
                    "\t    in g(g);\n" +
                    "    FACT := map f to \n" +
                    "\t      map n to if n = 0 then 1 else n * f(n - 1);\n" +
                    "in (Y(FACT))(3)";
            noValueCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testName2() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to \n" +
                    "              let g := map x to f(x(x));\n" +
                    "\t    in g(g);\n" +
                    "    APPEND := map ap to \n" +
                    "\t        map x,y to \n" +
                    "                  if x = null then y else cons(first(x), ap(rest(x), y));\n" +
                    "    l      := cons(1,cons(2,cons(3,null)));\t\n" +
                    "in (Y(APPEND))(l,l)";
            noValueCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }



    public void testEvalException() {
        try {
            String output = "mojo";
            String input = "1 + number?";
            allCheck("evalException", output, input );

            fail("evalException did not throw EvalException exception");
        } catch (EvalException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("evalException threw " + e);
        }
    } //end of func


    public void testAppend() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let Y    := map f to              let g := map x to f(map z1,z2 to (x(x))(z1,z2));     in g(g);  APPEND := map ap to            map x,y to               if x = null then y else cons(first(x), ap(rest(x), y)); l      := cons(1,cons(2,cons(3,null))); in (Y(APPEND))(l,l)";
            allCheck("append", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("append threw " + e);
        }
    } //end of func


    public void testLetRec() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input = "let append := map x,y to if x = null then y else cons(first(x), append(rest(x), y)); l := cons(1,cons(2,cons(3,null))); in append(l,l)";
            noNeedCheck("letRec", output, input );
//            allCheck("letRec", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("letRec threw " + e);
        }
    } //end of func


    public void testLazyCons() {
        try {
            String output = "0";
            String input = "let zeroes := cons(0,zeroes);in first(rest(zeroes))";
            lazyCheck("lazyCons", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("lazyCons threw " + e);
        }
    } //end of func


    public void testContextCheck0() {
        try {
            String output = "did not throw SyntaxException: Variable x declared more than once in let";
            String input = "let x := 1; y := 2; z := 4; x := 9; in x";
            allCheck("lazyCons", output, input );

            fail("did not throw SyntaxException");
        } catch (SyntaxException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("syntaxException threw " + e);
        }
    } //end of func

    public void testContextCheck1() {
        try {
            String output = "did not throw SyntaxException: Variable x appears free in this expression";
            String input = "list?(cons(1, null)) | cons(x, 1)";
            allCheck("lazyCons", output, input );

            fail("did not throw SyntaxException");
        } catch (SyntaxException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("syntaxException threw " + e);
        }
    } //end of func


    public void testContextCheck2() {
        try {
            String output = "did not throw SyntaxException: Variable x appears free in this expression";
            String input = "false & (x + 2)";
            allCheck("lazyCons", output, input );

            fail("did not throw SyntaxException");
        } catch (SyntaxException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("syntaxException threw " + e);
        }
    } //end of func


    public void testContextCheck3() {
        try {
            String output = "did not throw SyntaxException: Variable x appears free in this expression";
            String input = "x";
            allCheck("lazyCons", output, input );

            fail("did not throw SyntaxException");
        } catch (SyntaxException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("syntaxException threw " + e);
        }
    } //end of func

    public void testContextCheck4() {
        try {
            String output = "did not throw SyntaxException: Variable y appears free in this expression";
            //String input = "map x to x + y";
            String input = "(map x to x + y)(1)";
            allCheck("lazyCons", output, input );

            fail("did not throw SyntaxException");
        } catch (SyntaxException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("syntaxException threw " + e);
        }
    } //end of func


}




