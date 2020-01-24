/** Parser for Assignment 2 */

import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
  * class include a parse() method that will translate the program text in the input stream to the corresponding
  * AST assuming that the program text forms a syntactically valid Jam program.
  */
class Parser {
  
  private Lexer in;
  
  Parser(Lexer i) {
    in = i;
    initParser();
  }
  
  Parser(Reader inputStream) { this(new Lexer(inputStream)); }
  
  Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
  
  Lexer lexer() { return in; }
  
  private void initParser() {
    //initialize Parser
  }
  
  
  /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST. 
    * @throws ParseException if a syntax error is encountered (including lexical errors). 
    */
  public AST parse() throws ParseException {
    //parse text
  }
  
  
  /** Parses:
    *     <exp> :: = if <exp> then <exp> else <exp>
    *              | let <prop-def-list> in <exp>
    *              | map <id-list> to <exp>
    *              | <binary-exp>
    *     <binary-exp> ::=  <term> { <biop> <exp> }*
    * 
    * @return  the corresponding AST.
    */
  private AST parseExp() {
    
    // <binary-exp> ::=  <term> { <biop> <exp> }*
    if (token instanceof Term) {
      Token next = in.peek();
      if (next instanceof Op) {
        Op op = (Op) next;
        if (! op.isBinOp()) error(op,"binary operator");
        return new BinOpApp(op, parseExp());
      }
    }
    
    // if <exp> then <exp> else <exp>
    if (token instanceof If) return parseIf();


    // let <prop-def-list> in <exp>
    if (token instanceof Let) return;

    
    // map <id-list> to <exp>
    if (token instanceof Map) return parseMap();
    
  }
  
  
  /** Parses:
    *  <term>     ::= <unop> <term> | <constant> | <factor> {( <exp-list> )}
    *  <constant> ::= <null> | <int> | <bool>
    * @param token   first token in input stream to be parsed; remainder in Lexer named in.
    */
  private AST parseTerm(Token token) {

    if (token instanceof Op) {
      Op op = (Op) token;
      if (! op.isUnOp()) error(op,"unary operator");
      return new UnOpApp(op, parseTerm(in.readToken()));
    }
    
    if (token instanceof Constant) return (Constant) token;
    AST factor = parseFactor(token);
    Token next = in.peek();
    if (next == LeftParen.ONLY) {
      in.readToken();  // remove next from input stream
      AST[] exps = parseArgs();  // including closing paren
      return new App(factor,exps);
    }
    return factor;
  }
  
  
  /* You may find it helpful to define separate parse methods for <binary-exp>, if expressions, and map expressions.
   * This is a stylistic choice. */
  
  private AST parseIf() {}
  
  private AST parseMap() {}
  
  private AST parseBin() {}

}

