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
  private AST parseExp(Token token) {
    
    // <binary-exp> ::=  <term> { <biop> <exp> }*
    if (token instanceof Term) return parseBin(token);
    
    // let <prop-def-list> in <exp>
    // check fist token for 'let'
    if (token instanceof Let) {
      // cyle through Defs
      while(in.peek() instanceOf Def) {
        Token next = in.readToken();
        Def def = (Def) next;
      }
      // check next token for 'in'
      Token next = in.readToken();
      if(next instanceof In) {
        next = in.readToken();
        return parseExp(next);
      } else {
        return error();
      }
      // need to throw exceptions
    }
    
    // if <exp> then <exp> else <exp>
    if (token instanceof If) return parseIf(token);
    
    // map <id-list> to <exp>
    if (token instanceof Map) return parseMap(token);
    
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
  
  // token instanceof 'if'
  private AST parseIf(Token token) {
    Token next = in.readToken();
    // next token instanceof Exp
    if(next instanceof Exp) {
      AST exp0 = parseExp(next);
      next =  in.readToken();
      
      // next token instanceof 'then' and next next instance of Exp
      if(next instanceof Then && in.peek() instanceof Exp) {
        AST exp1 = parseExp(in.readToken());
        next =  in.readToken();
        next = in.readToken();
        
        // next token instanceof 'else' and next next instance of Exp
        if(next instanceof Else && in.peek() instanceof Exp) {
          AST exp2 = parseExp(in.readToken());
          return new App(exp0, new App(exp1, exp2));
          
        } else {
          error();
        }
      } else {
        error();
      }
    } else {
      error();
    }
  }
  
  // token instanceof 'map'
  private AST parseMap(Token token) {
    Token next = in.readToken();
    // next token instanceof IdList
    if(next instanceof IdList) {
      AST idList = parseIdList(next);
      next =  in.readToken();
      
      // next token instanceof 'to' and next next instance of Exp
      if(next instanceof To && in.peek() instanceof Exp) {
        AST exp = parseExp(in.readToken());
        return new App(idList, exp);
      } else {
        error();
      }
      
    } else {
      error();
    }
  }
  
  private AST parseBin(Token token) {
    AST term = parseTerm(token)
    Token next = in.peek();
    if (next instanceof Op) {
      Op op = (Op) next;
      if (! op.isBinOp()) error(op,"binary operator");
      return new App(term, new BinOpApp(op, parseExp()));
    } else {
      return term;
    }
  }
  
  private AST parseFactor(Token token) {
    
  }
  
  private AST parseExpList(Token token) {
    
  }
  
  private AST parsePropExpList(Token token) {
    
  }
  
  private AST parseIDList(Token token) {
    
  }
  
  private AST parsePropIDList(Token token) {
    
  }
  
  

}

