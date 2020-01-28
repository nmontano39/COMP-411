//import junit.framework.*;
//import org.junit.jupiter.api.Test;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;


public class Assign1Test extends TestCase {

    String s_00bad = "null + 3 -";
    String s_01good = "function? ()";
    String s_02bad = "if number? then true";
    String s_05good = "1 + 2 * 3";
    String s_06good = "(1 + 2) * 3";
    String s_07bad = "1 * 2 -";
    String s_08bad = "* 3 + 4";
    String s_bad01 = "5;";
    String s_bad02 = "f(a,b,^);";
    String s_bad03 = "map a,b,c 5";
    String s_bad04 = "-";
    String s_bad05 = "8*8;";
    String s_bad06 = "f[5]";
    String m_00good = "f()";
    String m_01good = "f(x)";
    String m_02good = "f(x,y,z)";
    String m_03bad = "f(,x)";
    String m_03good = "+-~+-~~~false";
    String m_04bad =  "f(x,)";
    String m_05bad = "if let x:= 3 in let y := 3 in 4 then true else false";
    String m_06bad = "let to 1 + 2 - 3";
    String m_07good = "map to x + y - z";
    String m_10bad = "f g";
    String m_14bad = "let ; in 3 = 4 + 6";
    String m_15good = "let f := 4 = 6; g := 12 * h(j); h := 50; in x";
    String m_17good = "42-------+++++++---+++-+12";
    String m_badTest5 = "let \n" +
            "  null?:=3;\n" +
            "in\n" +
            "  f(null?)";
    String m_badTest7 = "let \n" +
            "  null?=3;\n" +
            "in\n" +
            "  f(null?)";
    String m_badTest8 = "let\n" +
            "  f:= map x to f((x + y);\n" +
            "in\n" +
            "  if x then y else z";
    String m_badTest9 = "let\n" +
            "  f := map n to if n = 0 then 1; \n" +
            "in\n" +
            "  f(5)";
    String m_good03 = "let x := 2; in\n" +
            "   let y := - x; in\n" +
            "        (map x to x * y)(100)";
    String m_good04 = "(n)";
    String m_goodTest2 = "let\n" +
            "  x := map x to f(true);\n" +
            "in\n" +
            "  1";
    String m_goodTest3 = "let \n" +
            "  x:=3;\n" +
            "  y:=g();\n" +
            "  z:=map to 1;\n" +
            "in\n" +
            "  (true)(false)";
    String m_goodTest4 = "let x:=3;\n" +
            "    y:=4;\n" +
            "    z:=cons?(function?(x * ~y), cons(-arity(x))); \n" +
            "in\n" +
            "    rest(null?(true),list?(false),first(null))";
    String h_08good = "# This input is legal in Jam, only because the embedded map operation is enclosed in\n" +
            "# in parentheses.  A map enclosed in parentheses is a term.\n" +
            "\n" +
            "map x to let x := 3; y := 4; z := x; in 3 + (map x to x)";
    String h_09good = "f(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z)";
    String h_12good = "let f := 4 = 6; in g(4,5)";
    String h_16bad = "let f := 4 = 6; g := 12 * h(j); h := 50 in x";
    String h_badTest6 = "let\n" +
            "  f := map n to if n = 0 then 1 else n * f(n - 1); \n" +
            "in\n" +
            "  let\n" +
            "    f := map n,m,k to if (n <= 0 & n >= 0)\n" +
            "                  | (n < 0 & n > 0 & n != 0) then number?\n" +
            "                                           else m / f(k + 1);\n" +
            "  in\n" +
            "     let x:=3;\n" +
            "         y:=4;\n" +
            "         z:=cons?(function?(x ^ ~y), cons(-arity(x)));\n" +
            "     in\n" +
            "        let x=3;\n" +
            "            y:=4;\n" +
            "            z:=g();\n" +
            "        in\n" +
            "            (g(x,y,z))(null?(true),list?(false),first(null))";
    String h_good02 = "let mapStream := map f,l to\n" +
            "                   if l = null then null\n" +
            "                   else cons(f(first(l)),mapStream(f,rest(l)));\n" +
            "    oddNums := cons(3,mapStream(map i to i+2, oddNums));\n" +
            "    filter := map p,l to\n" +
            "                if l = null then null\n" +
            "                else if p(first(l)) then filter(p,rest(l))\n" +
            "                else cons(first(l),filter(p,rest(l)));\n" +
            "     divides := map a,b to ((b/a)*a) = b;\n" +
            "     initSeg := map l,n to\n" +
            "                  if n <= 0 then null\n" +
            "                  else cons(first(l),initSeg(rest(l),n-1));\n" +
            "     primes := map l to  // l must have form cons(p,l') where p is prime,\n" +
            "                         // l' contains all primes > p, but no numbers divisible\n" +
            "                         // by primes < p\n" +
            "                         //\n" +
            "                 let p := first(l);\n" +
            "                 in let l1 := filter(map x to divides(p,x),rest(l));\n" +
            "                    in cons(p,primes(l1));\n" +
            "\n" +
            "in initSeg(cons(2,primes(oddNums)),10)";
    String h_goodTest1 = "let\n" +
            "  f := map n to if n = 0 then 1 else n * f(n - 1); \n" +
            "in\n" +
            "  let\n" +
            "    f := map n,m,k to if (n <= 0 & n >= 0)\n" +
            "                  | (n < 0 & n > 0 & n != 0) then number?\n" +
            "                                           else m / f(k + 1);\n" +
            "  in\n" +
            "     let x:=3;\n" +
            "         y:=4;\n" +
            "         z:=cons?(function?(x * ~y), cons(-arity(x)));\n" +
            "     in\n" +
            "        let x:=3;\n" +
            "            y:=4;\n" +
            "            z:=g();\n" +
            "        in\n" +
            "            (g(x,y,z))(null?(true),list?(false),first(null))";

    public Assign1Test (String name) {
        super(name);
    }

    protected void checkString(String name, String answer, String program) {
        Parser p = new Parser(new StringReader(program));
        assertEquals(name, answer, p.parse().toString());
    }


    protected void checkFile(String name,
                             String answerFilename,
                             String programFilename) {
        try {
            File answerFile = new File(answerFilename);
            InputStream fin = new BufferedInputStream(new FileInputStream(answerFile));

            int size = (int) answerFile.length();
            byte[] data = new byte[size];
            fin.read(data,0,size);
            String answer = new String(data);


            Parser p = new Parser(programFilename);
            assertEquals(name, answer, p.parse().toString());
        } catch (IOException e) {
            fail("Critical error: IOException caught while reading input file");
            e.printStackTrace();
        }

    }


//
//    public void testAdd() {
//        try {
//            String output = "(2 + 3)";
//            String input = "2+3";
//            checkString("add", output, input );
//
//        } catch (Exception e) {
//            fail("add threw " + e);
//        }
//    } //end of func
//
//
//    public void testPrim  () {
//        try {
//            String output = "first";
//            String input = "first";
//            checkString("prim  ", output, input );
//
//        } catch (Exception e) {
//            fail("prim threw " + e);
//        }
//    } //end of func
//
//
//    public void testParseException() {
//        try {
//            String output = "doh!";
//            String input = "map a, to 3";
//            checkString("parseException", output, input );
//
//            fail("parseException did not throw ParseException exception");
//        } catch (ParseException e) {
//            //e.printStackTrace();
//
//        } catch (Exception e) {
//            fail("parseException threw " + e);
//        }
//    } //end of func
//
//
//    public void testLet() {
//        try {
//            String output = "let a := 3; in (a + a)";
//            String input = "let a:=3; in a + a";
//            checkString("let", output, input );
//
//        } catch (Exception e) {
//            fail("let threw " + e);
//        }
//    } //end of func
//
//
//    public void testMap() {
//        try {
//            String output = "map f to (map x to f(x(x)))(map x to f(x(x)))";
//            String input = "map f to (map x to f( x( x ) ) ) (map x to f(x(x)))";
//            checkString("map", output, input );
//
//        } catch (Exception e) {
//            fail("map threw " + e);
//        }
//    } //end of func

    public void testFile(String file, boolean good) {
        String input = file;

        if (good) {
            try {
                String output = file;
                checkString("map", output, input );

            } catch (Exception e) {
                fail("map threw " + e);
            }
        } else  {
            try {
                String output = "doh!";
                checkString("parseException", output, input );

                fail("parseException did not throw ParseException exception");
            } catch (ParseException e) {
                //e.printStackTrace();

            } catch (Exception e) {
                fail("parseException threw " + e);
            }
        }
    }

    public void testFile(String file, String answer) {
        try {
            String input = file;
            String output = answer;
            checkString("map", output, input);

        } catch (Exception e) {
            fail("map threw " + e);
        }
    }


    // Simple

    public void testS_00bad() {
        testFile(s_00bad, false);
    } //end of func

    public void testS_01good() {
        testFile(s_01good, true);
    } //end of func

    public void testS_02bad() {
        testFile(s_02bad, false);
    } //end of func

    public void testS_05good() {
        testFile(s_05good, "(1 + (2 * 3))");
    } //end of func

    public void testS_06good() {
        testFile(s_06good, "((1 + 2) * 3)");
    } //end of func

    public void testS_07bad() {
        testFile(s_07bad, false);
    } //end of func

    public void testS_08bad() {
        testFile(s_08bad, false);
    } //end of func

    public void testS_bad01() {
        testFile(s_bad01, false);
    } //end of func

    public void testS_bad02() {
        testFile(s_bad02, false);
    } //end of func

    public void testS_bad03() {
        testFile(s_bad03, false);
    } //end of func

    public void testS_bad04() {
        testFile(s_bad04, false);
    } //end of func

    public void testS_bad05() {
        testFile(s_bad05, false);
    } //end of func

    public void testS_bad06() {
        testFile(s_bad06, false);
    } //end of func

    // Medium

    public void testM_00good() {
        testFile(m_00good, true);
    } //end of func

    public void testM_01good() {
        testFile(m_01good, true);
    } //end of func

    public void testM_02good() {
        testFile(m_02good, "f(x, y, z)");
    } //end of func

    public void testM_03bad() {
        testFile(m_03bad, false);
    } //end of func

    public void testM_03good() {
        testFile(m_03good, "+ - ~ + - ~ ~ ~ false");
    } //end of func

    public void testM_04bad() {
        testFile(m_04bad, false);
    } //end of func

    public void testM_05bad() {
        testFile(m_05bad, false);
    } //end of func

    public void testM_06bad() {
        testFile(m_06bad, false);
    } //end of func

    public void testM_07good() {
        testFile(m_07good, "map  to (x + (y - z))");
    } //end of func

    public void testM_10bad() {
        testFile(m_10bad, false);
    } //end of func

    public void testM_14bad() {
        testFile(m_14bad, false);
    } //end of func

    public void testM_15good() {
        testFile(m_15good, "let f := (4 = 6); g := (12 * h(j)); h := 50; in x");
    } //end of func

    public void testM_17good() {
        testFile(m_17good, "(42 - - - - - - - + + + + + + + - - - + + + - + 12)");
    } //end of func

    public void testM_badTest5() {
        testFile(m_badTest5, false);
    } //end of func

    public void testM_badTest7() {
        testFile(m_badTest7, false);
    } //end of func

    public void testM_badTest8() {
        testFile(m_badTest8, false);
    } //end of func

    public void testM_badTest9() {
        testFile(m_badTest9, false);
    } //end of func

    public void testM_good03() {
        testFile(m_good03, "let x := 2; in let y := - x; in (map x to (x * y))(100)");
    } //end of func

    public void testM_good04() {
        testFile(m_good04, "n");
    } //end of func

    public void testM_goodTest2() {
        testFile(m_goodTest2, "let x := map x to f(true); in 1");
    } //end of func

    public void testM_goodTest3() {
        testFile(m_goodTest3, true);
    } //end of func

    public void testM_goodTest4() {
        testFile(m_goodTest4, "let x := 3; y := 4; z := cons?(function?((x * ~ y)), cons(- arity(x))); in rest(null?(true), list?(false), first(null))");
    } //end of func

    // Hard

    public void testH_08good() {
        testFile(h_08good, true);
    } //end of func

    public void testH_09good() {
        testFile(h_09good, "f(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z)");
    } //end of func

    public void testH_12good() {
        testFile(h_12good, "let f := (4 = 6); in g(4, 5)");
    } //end of func

    public void testH_16bad() {
        testFile(h_16bad, false);
    } //end of func

    public void testH_badTest6() {
        testFile(h_badTest6, false);
    } //end of func

    public void testH_good02() {
        testFile(h_good02, "let mapStream := map f,l to if (l = null) then null else cons(f(first(l)), mapStream(f, rest(l))); oddNums := cons(3, mapStream(map i to (i + 2), oddNums)); filter := map p,l to if (l = null) then null else if p(first(l)) then filter(p, rest(l)) else cons(first(l), filter(p, rest(l))); divides := map a,b to (((b / a) * a) = b); initSeg := map l,n to if (n <= 0) then null else cons(first(l), initSeg(rest(l), (n - 1))); primes := map l to let p := first(l); in let l1 := filter(map x to divides(p, x), rest(l)); in cons(p, primes(l1)); in initSeg(cons(2, primes(oddNums)), 10)");
    } //end of func

    public void testH_goodTest1() {
        testFile(h_goodTest1, true);
    } //end of func

}
