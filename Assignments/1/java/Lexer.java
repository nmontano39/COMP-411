import java.io.*;
import java.util.*;

/** Jam general AST type */
interface AST {
  public <T> T accept(ASTVisitor<T> v);
}

/** Visitor class for general AST type */
interface ASTVisitor<T> {
  T forBoolConstant(BoolConstant b);
  T forIntConstant(IntConstant i);
  T forNullConstant(NullConstant n);
  T forVariable(Variable v);
  T forPrimFun(PrimFun f);
  T forUnOpApp(UnOpApp u);
  T forBinOpApp(BinOpApp b);
  T forApp(App a);
  T forMap(Map m);
  T forIf(If i);
  T forLet(Let l);
}

/** Jam term AST type */
interface Term extends AST {
  public <T> T accept(ASTVisitor<T> v);
}

/** Jam constant type */
interface Constant extends Term {
  public <T> T accept(ASTVisitor<T> v);
}

enum TokenType {
  BOOL, INT, NULL, PRIM_FUN, VAR, OPERATOR, KEYWORD,
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACK, RIGHT_BRACK,
  LEFT_BRACE, RIGHT_BRACE, COMMA, SEMICOLON;
}

/** Jam token type */
interface Token {
  public TokenType getType();
}

/** Jam Boolean constant class */
class BoolConstant implements Token, Constant {
  private boolean value;
  private BoolConstant(boolean b) { value = b; }

  // ** singleton pattern **
  public static final BoolConstant FALSE = new BoolConstant(false);
  public static final BoolConstant TRUE = new BoolConstant(true);

  public boolean getValue() { return value; }

  public <T> T accept(ASTVisitor<T> v) { return v.forBoolConstant(this); }
  public String toString() { return String.valueOf(value); }
  public TokenType getType() { return TokenType.BOOL; }
}

/** Jam integer constant class */
class IntConstant implements Token, Constant {
  private int value;

  IntConstant(int i) { value = i; }
  // duplicates can occur!

  public int getValue() { return value; }

  public <T> T accept(ASTVisitor<T> v) { return v.forIntConstant(this); }
  public String toString() { return String.valueOf(value); }
  public TokenType getType() { return TokenType.INT; }
}

/** Jam null constant class, which is a singleton */
class NullConstant implements Token, Constant {
  public static final NullConstant ONLY = new NullConstant();
  private NullConstant() {}
  public <T> T accept(ASTVisitor<T> v) { return v.forNullConstant(this); }
  public String toString() { return "null"; }
  public TokenType getType() { return TokenType.NULL; }
}

/** Jam primitive function Class */
class PrimFun implements Token, Term {
  private String name;

  PrimFun(String n) { name = n; }

  public String getName() { return name; }
  public <T> T accept(ASTVisitor<T> v) { return v.forPrimFun(this); }
  public String toString() { return name; }
  public TokenType getType() { return TokenType.PRIM_FUN; }
}

/** Jam variable class */
class Variable implements Token, Term {
  private String name;
  Variable(String n) { name = n; }

  public String getName() { return name; }
  public <T> T accept(ASTVisitor<T> v) { return v.forVariable(this); }
  public String toString() { return name; }
  public TokenType getType() { return TokenType.VAR; }
}

/** Jam operator class */
class Op implements Token {
  private String symbol;
  private boolean isUnOp;
  private boolean isBinOp;
  Op(String s, boolean iu, boolean ib) {
    symbol = s; isUnOp = iu; isBinOp = ib;
  }
  Op(String s) {
    // isBinOp only!
    this(s,false,true);
  }
  public String getSymbol() { return symbol; }
  public boolean isUnOp() { return isUnOp; }
  public boolean isBinOp() { return isBinOp; }
  public String toString() { return symbol; }
  public TokenType getType() { return TokenType.OPERATOR; }
}

class KeyWord implements Token {
  private String name;

  KeyWord(String n) { name = n; }
  public String getName() { return name; }
  public String toString() { return name; }
  public TokenType getType() { return TokenType.KEYWORD; }
}

/** Jam left paren token */
class LeftParen implements Token {
  public String toString() { return "("; }
  private LeftParen() {}
  public static final LeftParen ONLY = new LeftParen();
  public TokenType getType() { return TokenType.LEFT_PAREN; }
}

/** Jam right paren token */
class RightParen implements Token {
  public String toString() { return ")"; }
  private RightParen() {}
  public static final RightParen ONLY = new RightParen();
  public TokenType getType() { return TokenType.RIGHT_PAREN; }
}

/** Jam left bracket token */
class LeftBrack implements Token {
  public String toString() { return "["; }
  private LeftBrack() {}
  public static final LeftBrack ONLY = new LeftBrack();
  public TokenType getType() { return TokenType.LEFT_BRACK; }
}

/** Jam right bracket token */
class RightBrack implements Token {
  public String toString() { return "]"; }
  private RightBrack() {}
  public static final RightBrack ONLY = new RightBrack();
  public TokenType getType() { return TokenType.RIGHT_BRACK; }
}

/** Jam left brace token */
class LeftBrace implements Token {
  public String toString() { return "{"; }
  private LeftBrace() {}
  public static final LeftBrace ONLY = new LeftBrace();
  public TokenType getType() { return TokenType.LEFT_BRACE; }
}

/** Jam right brace token */
class RightBrace implements Token {
  public String toString() { return "}"; }
  private RightBrace() {}
  public static final RightBrace ONLY = new RightBrace();
  public TokenType getType() { return TokenType.RIGHT_BRACE; }
}

/** Jam comma token */
class Comma implements Token {
  public String toString() { return ","; }
  private Comma() {}
  public static final Comma ONLY = new Comma();
  public TokenType getType() { return TokenType.COMMA; }
}

/** Jam semi-colon token */
class SemiColon implements Token {
  public String toString() { return ";"; }
  private SemiColon() {}
  public static final SemiColon ONLY = new SemiColon();
  public TokenType getType() { return TokenType.SEMICOLON; }
}


// AST class definitions

/** Jam unary operator application class */
class UnOpApp implements AST {
  private Op rator;
  private AST arg;

  UnOpApp(Op r, AST a) { rator = r; arg = a; }

  public Op getRator() { return rator; }
  public AST getArg() { return arg; }
  public <T> T accept(ASTVisitor<T> v) { return v.forUnOpApp(this); }
  public String toString() { return rator + " " + arg; }
}

/** Jam binary operator application class */
class BinOpApp implements AST {
  private Op rator;
  private AST arg1, arg2;

  BinOpApp(Op r, AST a1, AST a2) { rator = r; arg1 = a1; arg2 = a2; }

  public Op getRator() { return rator; }
  public AST getArg1() { return arg1; }
  public AST getArg2() { return arg2; }
  public <T> T accept(ASTVisitor<T> v) { return v.forBinOpApp(this); }
  public String toString() { 
    return "(" + arg1 + " " + rator + " " + arg2 + ")"; 
  }
}

/** Jam map (closure) class */
class Map implements AST {
  private Variable[] vars;
  private AST body;

  Map(Variable[] v, AST b) { vars = v; body = b; }
  public Variable[] getVars() { return vars; }
  public AST getBody() { return body; }
  public <T> T accept(ASTVisitor<T> v) { return v.forMap(this); }
  public String toString() { 
    return "map " + ToString.toString(vars,",") + " to " + body ;
  }
}  

/** Jam function (PrimFun or Map) application class */
class App implements AST {
  private AST rator;
  private AST[] args;

  App(AST r, AST[] a) { rator = r; args = a; }

  public AST getRator() { return rator; }
  public AST[] getArgs() { return args; }

  public <T> T accept(ASTVisitor<T> v) { return v.forApp(this); }
  public String toString() { 
    if ((rator instanceof Variable) || (rator instanceof PrimFun))
      return rator + "(" + ToString.toString(args,", ") + ")"; 
    else
      return "(" +  rator + ")(" + ToString.toString(args,", ") + ")"; 
  }
}  

/** Jam if expression class */
class If implements AST {
  private AST test, conseq, alt;
  If(AST t, AST c, AST a) { test = t; conseq = c; alt = a; }

  public AST getTest() { return test; }
  public AST getConseq() { return conseq; }
  public AST getAlt() { return alt; }
  public <T> T accept(ASTVisitor<T> v) { return v.forIf(this); }
  public String toString() { 
    return "if " + test + " then " + conseq + " else " + alt ; 
  }
}  

/** Jam let expression class */
class Let implements AST {
  private Def[] defs;
  private AST body;
  Let(Def[] d, AST b) { defs = d; body = b; }

  public <T> T accept(ASTVisitor<T> v) { return v.forLet(this); }
  public Def[] getDefs() { return defs; }
  public AST getBody() { return body; }
  public String toString() { 
    return "let " + ToString.toString(defs," ") + " in " + body; 
  }
}  


/** Jam definition class */
class Def {
  private Variable lhs;
  private AST rhs;  

  Def(Variable l, AST r) { lhs = l; rhs = r; }
  public Variable lhs() { return lhs; }
  public AST rhs() { return rhs; }

  public String toString() { return lhs + " := " + rhs + ";"; }
}

/** String utility class */
class ToString {

  /** prints array a with separator s between elements 
   *  this method does NOT accept a == null, since null
   *  is NOT an array */
  public static String toString(Object[] a, String s) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < a.length; i++) {
      if (i > 0) result.append(s);
      Object elt = a[i];
      String eltString = (elt instanceof Object[]) ?
        toString((Object[]) elt, s) : elt.toString();
      result.append(eltString);
    }
    return result.toString();
  }
}

/** Parsing error class */
class ParseException extends RuntimeException {
  ParseException(String s) {
    super(s);
  }
}

/** Jam lexer class.              
 *  Given a Lexer object, the next token in that input stream being
 *  processed by the Lexer is returned by static method readToken(); it
 *  throws a ParseException (a form of RuntimeException) if it
 *  encounters a syntax error.  Calling readToken() advances the cursor
 *  in the input stream to the next token.

 *  The static method peek() in the Lexer class has the same behavior as
 *  readToken() except for the fact that it does not advance the cursor.
 */
class Lexer extends StreamTokenizer {

  /* short names for StreamTokenizer codes */
  public static final int WORD = StreamTokenizer.TT_WORD; 
  public static final int NUMBER = StreamTokenizer.TT_NUMBER; 
  public static final int EOF = StreamTokenizer.TT_EOF; 
  public static final int EOL = StreamTokenizer.TT_EOL;
  
  /* Keywords */
  public static final KeyWord IF     = new KeyWord("if");
  public static final KeyWord THEN   = new KeyWord("then");
  public static final KeyWord ELSE   = new KeyWord("else");
  public static final KeyWord LET    = new KeyWord("let");
//  public static final KeyWord LETREC = new KeyWord("letrec");   // Used to support letrec extension
  public static final KeyWord IN     = new KeyWord("in");
  public static final KeyWord MAP    = new KeyWord("map");
  public static final KeyWord TO     = new KeyWord("to");
  public static final KeyWord BIND   = new KeyWord(":=");

  // wordtable for classifying words (identifiers/operators) in token stream
  public HashMap<String,Token>  wordTable = new HashMap<String,Token>();

  // Lexer peek cannot be implemented using StreamTokenizer pushBack 
  // because some Tokens are composed of two StreamTokenizer tokens

  Token buffer;  // holds token for peek() operation
 
  /* constructors */

  /** Constructs a Lexer for the specified inputStream */
  Lexer(Reader inputStream) {
    super(new BufferedReader(inputStream));
    initLexer();
  }

  /** Constructs a Lexer for the contents of the specified file */
  Lexer(String fileName) throws IOException {
    this(new FileReader(fileName));
  }

  /** Constructs a Lexer for the default console input stream System.in */  
  Lexer() {
    super(new BufferedReader(new InputStreamReader(System.in)));
    initLexer();
  }

  /* Initializes lexer tables and the StreamTokenizer that the lexer extends */
  private void initLexer() {

    // configure StreamTokenizer portion of this
    resetSyntax();
    parseNumbers();
    ordinaryChar('-');
    slashSlashComments(true);
    wordChars('0','9');
    wordChars('a','z');
    wordChars('A','Z');
    wordChars('_','_');
    wordChars('?','?');
    whitespaceChars(0,' '); 

    // `+' `-' `*' `/' `~' `=' `<' `>' `&' `|' `:' `;' `,' '!'
    // `(' `)' `[' `]' are ordinary characters (self-delimiting)
    

    
    initWordTable();
    buffer = null;  // buffer initially empty
  }

  /** Reads tokens until next end-of-line */
  public void flush() throws IOException {
    eolIsSignificant(true);
    while (nextToken() != EOL) ; // eat tokens until EOL
    eolIsSignificant(false);
  }

  /** Returns the next token in the input stream without consuming it */
  public Token peek() { 
    if (buffer == null) buffer = readToken();
    return buffer;
  }
    
  /** Reads the next token as defined by StreamTokenizer in the input stream 
      (consuming it).  
   */
  private int getToken() {
    // synonymous with nextToken() except for throwing an unchecked 
    // ParseException instead of a checked IOException
    try {
      int tokenType = nextToken();
      return tokenType;
    } catch(IOException e) {
      throw new ParseException("IOException " + e + "thrown by nextToken()");
    }
  }

  /** Reads the next Token in the input stream (consuming it) */
  public Token readToken() {
  
    // uses getToken() to read next token
    // constructs Token object representing that token
    // NOTE: token representations for all Token classes except
    //   IntConstant are unique; a HashMap is used to avoid duplication
    //   Hence, == can safely be used to compare all Tokens except IntConstants
    //   for equality

    if (buffer != null) {
      Token token = buffer;
      buffer = null;          // clear buffer
      return token;
    }
    
    int tokenType = getToken();
    switch (tokenType) {
    case NUMBER:
      int value = (int) nval;
      if (nval == (double) value) return new IntConstant(value);
      throw 
        new ParseException("The number " + nval + " is not a 32 bit integer");
    case WORD:
      Token regToken = wordTable.get(sval);
      if (regToken == null) {
        // must be new variable name
        Variable newVar = new Variable(sval);
        wordTable.put(sval,newVar);
        return newVar;
      }
      return regToken;
    case EOF: return null;
    case '(': return LeftParen.ONLY;
    case ')': return RightParen.ONLY;
    case '[': return LeftBrack.ONLY;
    case ']': return RightBrack.ONLY;
 // case '{': return LeftBrace.ONLY;
 // case '}': return RightBrace.ONLY;
    case ',': return Comma.ONLY;
    case ';': return SemiColon.ONLY;

    case '+': return wordTable.get("+");  
    case '-': return wordTable.get("-");  
    case '*': return wordTable.get("*");  
    case '/': return wordTable.get("/");  
    case '~': return wordTable.get("~");  
    case '=': return wordTable.get("=");  
    case '<': 
      tokenType = getToken();
      if (tokenType == '=') return wordTable.get("<=");  
      // if (tokenType == '-') return wordTable.get("<-");  
      pushBack();
      return wordTable.get("<");  
    case '>': 
      tokenType = getToken();
      if (tokenType == '=') return wordTable.get(">=");  
      pushBack();
      return wordTable.get(">"); 
      case '!': 
        tokenType = getToken();
        if (tokenType == '=') return wordTable.get("!=");  
        else throw new ParseException("!" + ((char) tokenType) + " is not a legal token");

        /*
         * // this alternate else clause will be added in later assignments
         * pushBack();
         * return wordTable.get("!");  
         */
    case '&': return wordTable.get("&");  
    case '|': return wordTable.get("|");  
    case ':': {
      tokenType = getToken();
      if (tokenType == '=') return wordTable.get(":=");  
      pushBack();
      throw new ParseException("`:' is not a legalken");
    }
    default:  
      throw new 
        ParseException("`" + ((char) tokenType) + "' is not a legal token");
    }
  }
    
  /** Initializes the table of Strings used to recognize Tokens */
  private void initWordTable() {
    // initialize wordTable

    // constants
    // <null>  ::= null
    // <bool>  ::= true | false

    wordTable.put("null",  NullConstant.ONLY);
    wordTable.put("true",  BoolConstant.TRUE);
    wordTable.put("false", BoolConstant.FALSE);

    // Install operator symbols constructed from self-delimiting characters

    // operators
    // <unop>  ::= <sign> | ~   | ! 
    // <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|" |
    //             <- 
    // <sign>  ::= "+" | -

    //  Note: there is no class distinction between <unop> and <binop> at 
    //  lexical level because of ambiguity; <sign> belongs to both

    wordTable.put("+",   new Op("+",true,true)); 
    wordTable.put("-",   new Op("-",true,true)); 
    wordTable.put("~",   new Op("~",true,false)); 
    wordTable.put("!",   new Op("!",true,false)); 

    wordTable.put("*",  new Op("*")); 
    wordTable.put("/",  new Op("/")); 
    wordTable.put("=",  new Op("=")); 
    wordTable.put("!=", new Op("!=")); 
    wordTable.put("<",  new Op("<")); 
    wordTable.put(">",  new Op(">")); 
    wordTable.put("<=", new Op("<=")); 
    wordTable.put(">=", new Op(">=")); 
    wordTable.put("&",  new Op("&")); 
    wordTable.put("|",  new Op("|")); 
    wordTable.put("<-", new Op("<-")); 
    
    // Install keywords
    
    wordTable.put("if",   IF);
    wordTable.put("then", THEN);
    wordTable.put("else", ELSE);
    wordTable.put("let",  LET);
    wordTable.put("in",   IN);
    wordTable.put("map",  MAP);
    wordTable.put("to",   TO);
    wordTable.put(":=",   BIND);


    // Install primitive functions
    // <prim>  ::= number? | function? | list? | null? 
    //           | cons? | cons | first | rest | arity

    wordTable.put("number?",   new PrimFun("number?"));
    wordTable.put("function?", new PrimFun("function?"));
    // wordTable.put("ref?",      new PrimFun("ref?"));
    wordTable.put("list?",     new PrimFun("list?"));
    wordTable.put("null?",     new PrimFun("null?"));
    wordTable.put("cons?",     new PrimFun("cons?"));
    wordTable.put("arity",     new PrimFun("arity"));
    wordTable.put("cons",      new PrimFun("cons"));
    wordTable.put("first",     new PrimFun("first"));
    wordTable.put("rest",      new PrimFun("rest"));
  }       

  /** Provides a command line interface to the lexer */
  public static void main(String[] args) throws IOException {
    // check for legal argument list 
    Lexer in;
    if (args.length == 0) {
      in = new Lexer();
    }
    else in = new Lexer(args[0]);
    do {
      Token t = in.readToken();
      if (t == null) break;
      System.out.println("Token " + t + " in " + t.getClass());
    } while (true);
  }
}
