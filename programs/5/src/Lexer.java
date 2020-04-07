import java.util.*;
import java.io.*;

/** The Jam lexer class.
  * Given a Lexer object, the static method readToken() returns the next token in the input stream; readToken()
  * throws a ParseException (an extension of IOException) if it encounters a syntax error.  Calling readToken()
  * advances the cursor in the input stream to the next token.
  * The static method peek() in the Lexer class has the same behavior as readToken() except for the fact that it
  * does not advance the cursor.
  */
class Lexer extends StreamTokenizer {
  
  /**static fields**/

  /** short names for StreamTokenizer codes */
  public static final int WORD = StreamTokenizer.TT_WORD; 
  public static final int NUMBER = StreamTokenizer.TT_NUMBER; 
  public static final int EOF = StreamTokenizer.TT_EOF; 
  public static final int EOL = StreamTokenizer.TT_EOL; 
  
  /* operator Tokens */
  
  // <unop>  ::= <sign> | ~   | ! 
  // <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|" | <- 
  // <sign>  ::= "+" | -
  
  //  Note: there is no class distinction between <unop> and <binop> at
  //  lexical level because of ambiguity; <sign> belongs to both
  
  public static final OpToken PLUS = OpToken.newBothOpToken(UnOpPlus.ONLY, BinOpPlus.ONLY); 
  public static final OpToken MINUS = OpToken.newBothOpToken(UnOpMinus.ONLY, BinOpMinus.ONLY);
  public static final OpToken TIMES = OpToken.newBinOpToken(OpTimes.ONLY);
  public static final OpToken DIVIDE = OpToken.newBinOpToken(OpDivide.ONLY);
  public static final OpToken EQUALS = OpToken.newBinOpToken(OpEquals.ONLY);
  public static final OpToken NOT_EQUALS = OpToken.newBinOpToken(OpNotEquals.ONLY);
  public static final OpToken LESS_THAN = OpToken.newBinOpToken(OpLessThan.ONLY);
  public static final OpToken GREATER_THAN = OpToken.newBinOpToken(OpGreaterThan.ONLY);
  public static final OpToken LESS_THAN_EQUALS = OpToken.newBinOpToken(OpLessThanEquals.ONLY);
  public static final OpToken GREATER_THAN_EQUALS = OpToken.newBinOpToken(OpGreaterThanEquals.ONLY);
  public static final OpToken NOT = OpToken.newUnOpToken(OpTilde.ONLY);
  public static final OpToken AND = OpToken.newBinOpToken(OpAnd.ONLY);
  public static final OpToken OR = OpToken.newBinOpToken(OpOr.ONLY);
  
  /* Used to support reference cells. */
  public static final OpToken BANG = OpToken.newUnOpToken(OpBang.ONLY);
  public static final OpToken GETS = OpToken.newBinOpToken(OpGets.ONLY);
  public static final OpToken REF = OpToken.newUnOpToken(OpRef.ONLY);



  /* Keywords */

  public static final KeyWord IF     = new KeyWord("if");
  public static final KeyWord THEN   = new KeyWord("then");
  public static final KeyWord ELSE   = new KeyWord("else");
  public static final KeyWord LET    = new KeyWord("let");
//  public static final KeyWord LETREC = new KeyWord("letrec");   // Used to support letrec extension in Assignment
  public static final KeyWord IN     = new KeyWord("in");
  public static final KeyWord MAP    = new KeyWord("map");
  public static final KeyWord TO     = new KeyWord("to");
  public static final KeyWord BIND   = new KeyWord(":=");
  public static final KeyWord LIST   = new KeyWord("list");
  public static final KeyWord TYPESET   = new KeyWord("->");


  /**fields**/ 
  
  /** The Reader from which this lexer reads. */
  public final Reader rdr;
  
  /** The wordtable for classifying words (identifiers/operators) in token stream */
  public HashMap<String,Token>  wordTable = new HashMap<String,Token>();

  /** The buffer that optionally holds the next token in the intput stream; it is used to support the peek() operation,
    * which cannot be implemented using StreamTokenizer pushBack because some Tokens are composed of two StreamTokenizer 
    * tokens.  If buffer is null, then the next token is still in the input stream. */
  Token buffer;
 
  /**constructors**/

  /** Primary constructor that takes a specified input stream; all other constructors instantiate this one. */
  Lexer(Reader inputStream) {
    super(new BufferedReader(inputStream));
    rdr = inputStream;
    initLexer();
  }

  /** Constructor that uses a File as the input stream. */
  Lexer(String fileName) throws IOException {
    this(new FileReader(fileName));
  }

  private void initLexer() {

    /* Configure StreamTokenizer portion of this. */
    /* `+' `-' `*' `/' `~' `=' `<' `>' `&' `|' `:' `;' `,' '!' `(' `)' `[' `]' are ordinary characters */
    resetSyntax();             // makes all characters "ordinary"
    parseNumbers();            // makes digits and - "numeric" (which is disjoint from "ordinary")
    ordinaryChar('-');         // eliminates '-' from number parsing and makes it "ordinary"
    slashSlashComments(true);  // enables slash-slash comments as in C++
    slashStarComments(true);   // enables slash-asterisk comments as in C
    /* Identify chars that appear in identifiers (words) */
    wordChars('0', '9');
    wordChars('a', 'z');
    wordChars('A', 'Z');
    wordChars('_', '_');
    wordChars('?', '?');
    /* Identify whitespace */
    whitespaceChars(0, ' '); 

    /* Initialize table of words that function as specific tokens (keywords) including "<=", ">=", "!=" */
    initWordTable();
    
    /* Initialize buffer supporting the peek() operation */
    buffer = null;  // buffer initially empty
  }
  
  /** Resets the reader embedded in this lexer.  The same file may be scanned multiple times in tests. */
  public void reset() throws ParseException { 
    try { rdr.reset(); }
    catch(IOException e) { throw new ParseException(e); }
  }

  public void flush() throws IOException {
    eolIsSignificant(true);
    while (nextToken() != EOL) ; // eat tokens until EOL
    eolIsSignificant(false);
  }

  public Token peek() {
    if (buffer == null) buffer = readToken();
    return buffer;
  }

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


  public Token readToken() {

    /* This method uses getToken() to read next token.  It constructs Token objects representing Jam tokens.
     * Note: the token representations for all Token classes except IntConstant have unique instances for each possible
     * token value; a HashMap is used to avoid duplication.  Hence, == can safely be used to compare all Tokens except 
     * IntConstants for equality.  All Token classes other than IntConstant and Variable are singletons.
     */

    if (buffer != null) {
      Token token = buffer;
      buffer = null;          // clear buffer
      return token;
    }

    int tokenType = getToken();
    switch (tokenType) {
      //case TYPE:
        //int t = (Type) sval;
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
      case EOF: return EndOfFile.ONLY;
      case '(': return LeftParen.ONLY;
      case ')': return RightParen.ONLY;
      case '[': return LeftBrack.ONLY;
      case ']': return RightBrack.ONLY;
      case '{': return LeftBrace.ONLY;   // Supports the addition of blocks to Jam
      case '}': return RightBrace.ONLY;  // Supports the addition of blocks to Jam
      case ',': return Comma.ONLY;
      case ';': return SemiColon.ONLY;

      case '+': return PLUS;
      case '-': {
        if (getToken() == '>') {
          return TYPESET;
        } else {
          return MINUS;
        }
      }
      case '*': return TIMES;
      case '/': return DIVIDE;
      case '~': return NOT;
      case '=': return EQUALS;
      case '<':
        tokenType = getToken();
        if (tokenType == '=') return LESS_THAN_EQUALS;
        if (tokenType == '-') return GETS;  // Support the addition of ref cells to Jam
        pushBack();
        return LESS_THAN;
      case '>':
        tokenType = getToken();
        if (tokenType == '=') return GREATER_THAN_EQUALS;
        pushBack();
        return GREATER_THAN;
      case '!':
        tokenType = getToken();
        if (tokenType == '=') return NOT_EQUALS;
        // Support the addition of ref cells to Java (remove preceding statement)
         pushBack();
         return BANG;
      case '&': return AND;
      case '|': return OR;

      case ':': {
        tokenType = getToken();
        if (tokenType == '=') return BIND;
        pushBack();
        return Colon.ONLY;
        //throw new ParseException("`:' is not a legal token");
      }
      default:
        throw new
        ParseException("`" + ((char) tokenType) + "' is not a legal token");
    }
  }

  private void initWordTable() {
    // initialize wordTable

    // constants
    // <null>  ::= null
    // <bool>  ::= true | false
    wordTable.put("int", IntType.ONLY);
    wordTable.put("bool", BoolType.ONLY);
    wordTable.put("unit", UnitType.ONLY);
    wordTable.put("null", NullConstant.ONLY);
    wordTable.put("true",  BoolConstant.TRUE);
    wordTable.put("false", BoolConstant.FALSE);

    /*  Install symbols constructed from self-delimiting characters
     * 
     * operators
     *   <unop>  ::= <sign> | ~   // formerly | ! | ref
     *   <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|"
     *   <sign>  ::= "+" | -
     * 
     * Note: there is no class distinction between <unop> and <binop> at the lexical level because of ambiguity; 
     * <sign> belongs to both.
     * 
     * Install primitive functions
     *   <prim>  ::= number? | function? | list? | null? | cons? ref? | arity | cons | first | rest
     */

    wordTable.put("null?",     NullPPrim.ONLY);
    wordTable.put("cons?",     ConsPPrim.ONLY);
    wordTable.put("cons",      ConsPrim.ONLY);
    wordTable.put("first",     FirstPrim.ONLY);
    wordTable.put("rest",      RestPrim.ONLY);
       
    // keywords: if then else let in map to := 
    wordTable.put("if",   Lexer.IF);
    wordTable.put("then", Lexer.THEN);
    wordTable.put("else", Lexer.ELSE);
    wordTable.put("let",  Lexer.LET);
    wordTable.put("in",   Lexer.IN);
    wordTable.put("map",  Lexer.MAP);
    wordTable.put("to",   Lexer.TO);
    wordTable.put("ref",  Lexer.REF);
    wordTable.put("list",  Lexer.LIST);
  }

  public static void main(String[] args) throws IOException {
    /* Check for legal argument list. */
    if (args.length == 0) {
      System.out.println("Usage: java Lexer <filename>");
      return;
    }
    Lexer in = new Lexer(args[0]);
    do {
      Token t = in.readToken();
      if (t == null) break;
      System.out.println("Token " + t + " in " + t.getClass());
    } while (true);
  }
}
