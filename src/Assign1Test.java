//import junit.framework.*;
//import org.junit.jupiter.api.Test;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;

public class Assign1Test extends TestCase {

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

    public void testFile() {

        try {
            String output = "(2 + 3)";
            String input = "let mapStream := map f,l to\n" +
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
//            String input = "let f := 4 = 6; g := 12; in x";
            checkString("add", output, input );

        } catch (Exception e) {
            fail("add threw " + e);
        }

    }


    public void testAdd() {
    try {
      String output = "(2 + 3)";
      String input = "2+3";
      checkString("add", output, input );

    } catch (Exception e) {
      fail("add threw " + e);
    }
  } //end of func

  public void testPrim  () {
    try {

      String output = "first";
      String input = "first";
      checkString("prim  ", output, input );

    } catch (Exception e) {
      fail("prim   threw " + e);
    }
  } //end of func
  
  public void testParseException() {
    try {
      String output = "doh!";
      String input = "map a, to 3";
      checkString("parseException", output, input );

         fail("parseException did not throw ParseException exception");
      } catch (ParseException e) {   
         //e.printStackTrace();
      
    } catch (Exception e) {
      fail("parseException threw " + e);
    }
  } //end of func
  
  public void testLet() {
    try {
      String output = "let a := 3; in (a + a)";
      String input = "let a:=3; in a + a";
      checkString("let", output, input );

    } catch (Exception e) {
      fail("let threw " + e);
    }
  } //end of func

  public void testMap() {
    try {
      String output = "map f to (map x to f(x(x)))(map x to f(x(x)))";
      String input = "map f to (map x to f( x( x ) ) ) (map x to f(x(x)))";
      checkString("map", output, input );

    } catch (Exception e) {
      fail("map threw " + e);
    }
  } //end of func   
}
