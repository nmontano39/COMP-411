/** Parser for Assignment 2 */

import java.io.*;
import java.util.*;

/** Class that represented parsing errors. */
class ParseException extends RuntimeException {
  ParseException(String s) { super(s); }
  ParseException(Throwable t) { super(t); }
}

class Parser {
  
  private Lexer in;
  
  Parser(Lexer i) { in = i; }
  
  Parser(Reader inputStream) { this(new Lexer(inputStream)); }
  
  Parser(String fileName) throws IOException { this(new FileReader(fileName)); }
  
  Lexer lexer() { return in; }
  
  public void initParser() { }
  
  /** Parses the program in the Lexer associated with this. */
  public AST parse() throws ParseException {
    try {
      in.reset();
      AST prog = parseExp();
      Token t = in.readToken();
      if (t == EndOfFile.ONLY) return prog;
      else throw new ParseException("Legal program followed by extra token " + t);
    } 
    finally {
      try { in.rdr.close(); } 
      catch (IOException e) { throw new ParseException(e); }
    }
  }
  
  /** Parses and syntactically checks the program in the Lexer associated with this. */
  public AST parseAndCheck() throws ParseException {
    AST prog = parseExp();
    Token t = in.readToken();
    if (t != EndOfFile.ONLY) throw new ParseException("Legal program '" + prog + "'followed by extra token " + t);
    
    prog.accept(CheckVisitor.INITIAL);   // aborts on an error by throwing an exception

    return prog;
  }
  
  /** Parses:
    *   <exp> :: = if <exp> then <exp> else <exp>
    *            | let <prop-def-list> in <exp>
    *            | map <id-list> to <exp>
    *            | <term> { <biop> <term> }*  // (left associatively!)
   * @return
   */
  private AST parseExp() {
    Token token = in.readToken();
    
    if (token == Lexer.IF) return parseIf();
//    if (token == Lexer.LETREC) return parseLetRec();  // supports addition of letrec
    if (token == Lexer.LET) return parseLet();
    if (token == Lexer.MAP) return parseMap();

    // PROVIDED
    /*  Supports the addition of blocks to Jam */
    if (token == LeftBrace.ONLY) {
      AST[] exps = parseExps(SemiColon.ONLY,RightBrace.ONLY);
      // including closing brace
      if (exps.length == 0) throw new ParseException("Illegal empty block");
      return new Block(exps);
    }
    
    /* Note: the code for the class Block is not included in AST.java */
    
    /* phrase begin with a term */
    AST exp = parseTerm(token);
    
    Token next = in.peek();
    while (next instanceof OpToken) {
      OpToken op = (OpToken) next;
      in.readToken(); // remove next from input stream
      if (! (op.isBinOp())) error(next, "binary operator");
      AST newTerm = parseTerm(in.readToken());
      exp = new BinOpApp(op.toBinOp(), exp, newTerm);
//      System.err.println("exp updated to: " + exp);
      next = in.peek();
    }
//    System.err.println("parseTerm returning " + exp);
    return exp;
  }
  
  /** Parses:
    *   <term>     ::= { <unop> } <term> | <constant> | <factor> {( <exp-list> )}
    *   <constant> ::= <null> | <int> | <bool>
    * @param token   first token in input stream to be parsed; remainder in Lexer named in.
    */
  private AST parseTerm(Token token) {
    
    if (token instanceof OpToken) {
      OpToken op = (OpToken) token;
      if (! op.isUnOp()) error(op,"unary operator");
      return new UnOpApp(op.toUnOp(), parseTerm(in.readToken()));
    }
    
    if (token instanceof Constant) {
      if (token instanceof NullConstant) {
        Token next = in.readToken();
        if (!(next == Colon.ONLY)) {
          throw new ParseException("ParseException: Expecting : but found " + next);
        }
        next = in.readToken();
//        if (!(next instanceof Type)) {
//          throw new ParseException("ParseException: No matching clause (type) for null");
//        }
        System.out.println(next);
        Type type = parseType(next);

        return new TypedNullConstant(type);
      }
      return (Constant) token;
    }
    
    AST factor = parseFactor(token);
    Token next = in.peek();
    if (next == LeftParen.ONLY) {
      in.readToken();  // remove next from input stream
      AST[] exps = parseArgs();  // including closing paren
      return new App(factor,exps);
    }
    return factor;
  }
  
  /** Parses:  <factor>   ::= <prim> | <variable> | ( <exp> )
    * @param token   first token in input stream to be parsed; remainder in Lexer named in.
    */
  private AST parseFactor(Token token) {
    
    if (token == LeftParen.ONLY) { 
      /* Parse a parenthesized expression */
      AST exp = parseExp();
      token = in.readToken();
      if (token != RightParen.ONLY) error(token,"`)'");
      return exp;
    }
    
    if (! (token instanceof PrimFun) && ! (token instanceof Variable))
      error(token,"constant, primitive, variable, or `('");

    // Term = Variable or PrimFun       
    return (Term) token;
  }      
  
  /** Parses `if <exp> then <exp> else <exp>' given that `if' has already been read. */
  private AST parseIf() {
    
    AST test = parseExp();
    Token key1 = in.readToken();
    if (key1 != Lexer.THEN) error(key1,"`then'");
    AST conseq = parseExp();
    Token key2 = in.readToken();
    if (key2 != Lexer.ELSE) error(key2,"`else'");
    AST alt = parseExp();
    return new If(test,conseq,alt);
  }
  
  
  /** Parses `let <prop-def-list> in <exp>' given that `let' has already been read. */ 
  private AST parseLet() {
    Def[] defs = parseDefs(false); 
    // consumes `in'; false means rhs may be non Map
    AST body = parseExp();
    return new Let(defs,body);
  }
  
  /* Supports the parsing of 'letrec' */
//  /** Parses `letrec <prop-def-list> in <exp>' given that `letrec' has already been read. */
//  private AST parseLetRec() {
//    
//    Def[] defs = parseDefs(true);
//    // consumes `in'; true means each rhs must be a Map
//    AST body = parseExp();
//    return new LetRec(defs,body);
//  }
  
  /* Parses `map <id-list> to <exp>' given that `map' has already been read. */
  private AST parseMap() {
    
    Variable[] vars = parseVars(); // consumes the delimiter `to'
    AST body = parseExp();
    return new Map(vars, body);
  }
  
  /** Parses `<exp-list> <delim>' where
    *  <exp-list>      ::= <empty> | <prop-exp-list>
    *  <empty> ::=
    *  <prop-exp-list> ::= <exp> | <exp> <separator> <prop-exp-list> 
    */
  private AST[] parseExps(Token separator, Token delim) {
    
    LinkedList<AST> exps = new LinkedList<AST>();
    Token next = in.peek();
    
    if (next == delim) {
      in.readToken(); // consume RightParen
      return new AST[0];
    }
    
    // next is still at front of input stream
    
    do {
      AST exp = parseExp();
      exps.addLast(exp);
      next = in.readToken();
    } while (next == separator);
    
    if (next != delim) error(next,"`,' or `)'");
    return (AST[]) exps.toArray(new AST[0]);
  }
  
  private AST[] parseArgs() { return parseExps(Comma.ONLY,RightParen.ONLY); }
  
  /** Parses <id-list> where
    *   <id-list>       ::= <empty> | <prop-id-list>
    *   <prop-id-list>  ::= <id> | <id> , <id-list> 
    *  NOTE: consumes `to' following <id-list>
    */
  private Variable[] parseVars() {
    
    LinkedList<Variable> vars = new LinkedList<Variable>();
    Token t = in.readToken();
    if (t == Lexer.TO) return new Variable[0];
    
    do {
      if (! (t instanceof Variable)) error(t,"variable");
      vars.addLast((Variable)t);
      t = in.readToken();
      if (t == Lexer.TO) break; 
      if (t != Comma.ONLY) error(t, "`to' or `, '");
      // Comma found, read next variable
      t = in.readToken();
    } while (true);
    return (Variable[]) vars.toArray(new Variable[0]);
  }
  
  /** Parses a proper list of definitions, more technically parses
    *   <prop-def-list> ::= <def> | <def> <def-list> 
    *   NOTE: consumes `in' following <prop-def-list> */
  
  private Def[] parseDefs(boolean forceMap) {
    LinkedList<Def> defs = new LinkedList<Def>();
    Token t = in.readToken();
    
    do {
      Def d = parseDef(t);        
      if (forceMap && (! (d.rhs() instanceof Map)))
        throw new ParseException("right hand side of definition `" + d + "' is not a map expression");
      defs.addLast(d);
      t = in.readToken();
    } while (t != Lexer.IN);
    
    return (Def[]) defs.toArray(new Def[0]);
  }
  
  /** Parses 
    *   <id> := <exp> ;
    * which is <def> given that first token var has been read.
    */
  private Def parseDef(Token var) {
    
    if (! (var instanceof Variable)) error(var, "variable");

    Token next = in.readToken();
    if (!(next == Colon.ONLY)) {
      throw new ParseException("ParseException: Expecting : but found " + next);
    }
    next = in.readToken();

    Type type = parseType(next);
    
    Token key = in.readToken();
    if (key != Lexer.BIND) error (key,"`:='");
    
    AST exp = parseExp();
    
    Token semi = in.readToken();
    if (semi != SemiColon.ONLY) error(semi,"`;'");
    return new Def( new TypedVariable(var.toString(), type), exp);
  }

  public Type parseType(Token tok) {
    System.out.println("token => " + tok);
    if (tok instanceof Type) {
      System.out.println("reaches 3");
      return (Type) tok;
    } else if (tok == Lexer.REF) {
      return new RefType(parseType(in.readToken()));
    } else if (tok == Lexer.LIST) {
      System.out.println("reaches 1");
      Token next = in.readToken();
      if (next == LeftParen.ONLY) {
        return parseTypeList(in.readToken());
      }
      System.out.println("reaches 2");
      return new ListType(parseType(next));
    } else {
      if (tok == LeftParen.ONLY) {
        return parseTypeList(in.readToken());
      }
      throw new ParseException("ParseException: Invalid Type");
    }
  }

  public Type parseTypeList(Token next) {
    ArrayList<Type> listTypes = new ArrayList<>();
    listTypes.add(parseType(next));
    next = in.readToken();

    while (next == Comma.ONLY) {
      next = in.readToken();
      listTypes.add(parseType(next));
      next = in.readToken();
    }
    if (next != Lexer.TYPESET) {
      throw new ParseException("ParseException: Could not parse the TypeList ->");
    }
    Type myType = parseType(in.readToken());
    in.readToken();
    return new ListType(myType, listTypes);
  }

  
  private AST error(Token found, String expected) {
//    for (int i = 0; i < 10; i++) {
//      System.out.println(in.readToken());
//    }
    throw new ParseException("Token `" + found + "' appears where " + expected + " was expected");
  }
  
  public static void main(String[] args) throws IOException {
    // check for legal argument list 
    if (args.length == 0) {
//      System.out.println("Usage: java Parser <filename>");
      return;
    }
    Parser p = new Parser(args[0]);
    AST prog = p.parse();
//    System.out.println("Parse tree is: " + prog);
  }
}

