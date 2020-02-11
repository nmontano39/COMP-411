import java.util.StringTokenizer;


import junit.framework.TestCase;

import java.io.*;

public class Assign2Test extends TestCase {

    public Assign2Test (String name) {
        super(name);
    }

    /**
     * The following 3 check methods create an interpreter object with the
     * specified String as the program, invoke the respective evaluation
     * method (callByValue, callByName, callByNeed), and check that the
     * result matches the (given) expected output.  If the test fails,
     * the method prints a report as to which test failed and how many
     * points should be deducted.
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
        //valueCheck(name, answer, program);
        nameCheck(name, answer, program);
//        needCheck(name, answer, program);
    }



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


    public void testConsP() {
        try {
            String output = "cons?";
            String input = "cons?";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    } //end of func

    public void testConsP2() {
        try {
            String output = "true";
            String input = "cons?(cons(null, null))";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }

    public void testFirst() {
        try {
            String output = "()";
            String input = "first(cons(null, null))";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }

    public void testRest() {
        try {
            String output = "(3 ())";
            String input = "rest(cons(2, cons(3,cons(null, null))))";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }

    public void testCons() {
        try {
            String output = "(1 2 3)";
            String input = "cons(1, cons(2, cons(3, null)))";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }

    public void testAppend2() {
        try {
            String output = "2";
            String input = "(let x := map f to let g := map x to f(2); in g; in x(map z to z))(1)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testAppend3() {
        try {
            String output = "4";
            String input = "(let x := map f to map x to  f(2); in x(map z to 2*z))(1)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testConsPX() {
        try {
            String output = "true";
            String input = "cons?(cons(1,null))";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }//end of func

    public void testNull() {
        try {
            String output = "true";
            String input = "null?(null)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testFunction() {
        try {
            String output = "true";
            String input = "function?(map x to x)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testArity() {
        try {
            String output = "2";
            String input = "arity(map x, y to x+y)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testListP() {
        try {
            String output = "true";
            String input = "cons?(cons(1, null))";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testConsX() {
        try {
            String output = "(1)";
            String input = "cons(1, null)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testFirstX() {
        try {
            String output = "1";
            String input = "first(cons(1, null))";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    } //end of func

    public void testRestX() {
        try {
            String output = "()";
            String input = "rest(cons(1, null))";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
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

    public void testMathOp2() {
        try {
            String output = "9";
            String input = "2 * 3 + (12 / 4)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testDivideByZero() {
        try {
            String output = "mojo";
            String input = "10 / 0";
            allCheck("evalException", output, input );

            fail("evalException did not throw EvalException exception");
        } catch (EvalException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("evalException threw " + e);
        }
    }

    public void testBoolOp() {
        try {
            String output = "true";
            String input = "(5 = false) & (2 > 0) | true";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }


    public void testLet0() {
        try {
            String output = "6";
            String input = "let x := 3; y := 2; z:= 1; in x + y + z";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testLet1() {
        try {
            String output = "9";
            String input = "let x := 1; y := 2 * x; z := 3 * y; in x + y + z";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testLet2() {
        try {
            String output = "1";
            String input = "let x := 1; in x";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testIf0() {
        try {
            String output = "2";
            String input = "if true then let x := 2; in x else let x:= 5; in x";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }


    public void testIf1() {
        try {
            String output = "5";
            String input = "if false then let x := 2; in x else let x:= 5; in x";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testIf3() {
        try {
            String output = "5";
            String input = "if 2 > 4 then let x := 2; in x else let x:= 5; in x";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }

    }

    public void testIf4() {
        try {
            String output = "9";
            String input = "let x := 2; in if x > 4 then x else 9";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }

    }

//    public void testMap0() {
//        try {
//            String output = "(closure: map x to (2 * x))";
//            String input = "map x to 2*x";
//            allCheck("mathOp", output, input );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail("mathOp threw " + e);
//        }
//    }

    public void testApp0() {
        try {
            String output = "8";
            String input = "(map x to (map x to 2 *  x)(1))(4)";
            allCheck("mathOp", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("mathOp threw " + e);
        }
    }

    public void testApp1() {
        try {
            String output = "mojo";
            String input = "(map x to x)()";
            allCheck("evalException", output, input );

            fail("evalException did not throw EvalException exception");
        } catch (EvalException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("evalException threw " + e);
        }
    } //end of func

    public void testApp2() {
        try {
            String output = "mojo";
            String input = "(map x to x)(1, 2)";
            allCheck("evalException", output, input );

            fail("evalException did not throw EvalException exception");
        } catch (EvalException e) {
            //e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            fail("evalException threw " + e);
        }
    } //end of func

//    public void testValName() {
//        try {
//            String output = "should throw error in val, not in name";
//            String input = "let x := 100/0; y := 1; in y";
//            allCheck("mathOp", output, input );
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail("mathOp threw " + e);
//        }
//    }

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

    public void testDeleteThis() {
        try {
            String output = "4";
            String input = "let x := first(cons(null, null)); in if x = null then 4 else 5";
            allCheck("numberP", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("numberP threw " + e);
        }
    }


    public void testAppend() {
        try {
            String output = "(1 2 3 1 2 3)";
            String input =
                    "let Y      := map f to " +
                            "                let g := map x to f(map z1,z2 to (x(x))(z1,z2)); " +
                            "                in g(g); " +
                            "    APPEND := map ap to " +
                            "                map x,y to " +
                            "                  if x = null then y else cons(first(x), ap(rest(x), y)); " +
                            "    l      := cons(1,cons(2,cons(3,null))); " +
                            "in (Y(APPEND))(l,l)";
            allCheck("append", output, input );

        } catch (Exception e) {
            e.printStackTrace();
            fail("append threw " + e);
        }
    } //end of func

    public void testFib() {
        try {
            String output
                    = "((0 1) (1 1) (2 2) (3 3) (4 5) (5 8) (6 13) (7 21) (8 34) (9 55) (10 89))";
            String input =
                    "let Y      := map f to " +
                            "                let g := map x to f(x(x)); " +
                            "                in g(g); " +
                            "      pair := map x,y to cons(x, cons(y, null));" +
                            "   FIBHELP := map fibhelp to map k,fn,fnm1 to if k = 0 then fn else fibhelp(k - 1, fn + fnm1, fn); " +
                            "in let FFIB := map ffib to map n to if n = 0 then 1 else (Y(FIBHELP))(n - 1,1,1); " +
                            "   in let FIBS := map fibs to map k,l to " +
                            "                    let fibk := (Y(FFIB))(k);" +
                            "                    in if k >= 0 then fibs(k - 1, cons(pair(k,fibk), l)) else l; " +
                            "      in (Y(FIBS))(10, null)";
            needCheck("fib-need", output, input);
            nameCheck("fib-name", output, input);
        }
        catch(Exception e) {
            e.printStackTrace();
            fail("[3.00] fib threw " + e);
        }
    } //end
}