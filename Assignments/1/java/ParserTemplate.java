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
    Parser p = new Parser(in);
    p.parse();
  }
  
  
  /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST. 
    * @throws ParseException if a syntax error is encountered (including lexical errors). 
    */
  public AST parse() throws ParseException {
    //parse text
    parseExp(in.readToken());
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
    
    // let <prop-def-list> in <exp>
    if (token instanceof Let) {
      // cyle through Defs
      Token next = in.readToken();
      if (next instanceof Def) {
        Def[] defs = {};
        defs.append((Def) next);
        next = in.readToken();
        
        while (next instanceof Def){
          defs.append((Def) next);
          next = in.readToken();
        }
        
        // check next token for 'in'
        if (next instanceof In) {
          next = in.readToken();
          return new Let(defs, parseExp(next));
        }
      }
    }
    
    // if <exp> then <exp> else <exp>
    if (token instanceof If) return parseIf(in.readToken());
    
    // map <id-list> to <exp>
    if (token instanceof Map) return parseMap(in.readToken());
    
    
    // <binary-exp> ::=  <term> { <biop> <exp> }*
    AST term = parseTerm(token);
    token = in.readToken();
    if (token instanceof Op) {
      return new App(term, parseBin(token));
    }
    return term;
   
  }
  
  
  
  /* You may find it helpful to define separate parse methods for <binary-exp>, if expressions, and map expressions.
   * This is a stylistic choice. */
  
  // token instanceof 'if'
  private AST parseIf(Token token) {
   
    AST exp0 = parseExp(token);
    tokenThen =  in.readToken();
   
    if (tokenThen instanceof Then) {
      tokenExp = in.readToken();
      AST exp1 = parseExp(tokenExp);
      tokenElse = in.readToken();
        
      if (tokenElse instanceof Else) {
        AST exp2 = parseExp(in.readToken());
        return new If(exp0, exp1, exp2);
      }
    }
    error();
  }
  
  // token instanceof 'map'
  private AST parseMap(Token token) {
    if (token instanceof IdList) {
      AST[] idList = parseIdList(token);
      token =  in.readToken();
      
      // next token instanceof 'to' and next next instance of Exp
      if (token instanceof To && in.peek() instanceof Exp) {
        return new Map(idList, parseExp(in.readToken()));
      }
    }
    error();
  }
  
  // token instanceof binop
  private AST parseBin(Token token) {
    Op op = (Op) token;
    if (! op.isBinOp()) error(op,"binary operator");
    return new BinOpApp(op, parseExp(in.readToken()));
  }
  
  
  // Factor  ::= ( Exp ) | Prim | Id
  private AST parseFactor(Token token) {
    if (token == LeftParen.ONLY) {
      token = in.readToken();
      if (in.peek() != RightParen.ONLY) {
        error();
      }
      return parseExp(token);
    }
    if (token instanceof PrimFun) {
      return (PrimFun) token;
    }
    if (token instanceof Id) {
      return (Id) token;
    }
    error();
  }
  
  private AST[] parseExpList(Token token) {
    return parsePropExpList(token);
  }
  
  private AST[] parsePropExpList(Token token) {
    
    if (in.peek != ",") return parseExp(token);
    
    while (in.readToken() == ",") {
      token = in.readToken();
      return parseExp(token) + parsePropExpList(in.readToken());
    }
      
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
      AST[] exps = parseExpList();  // including closing paren
      return new App(factor,exps);
    }
    return factor;
}

