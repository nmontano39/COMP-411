import java.util.*;

/* Token classes.  Note: some AST classes and AST component classes are also Tokens. */

/** A data object representing a Jam token */
interface Token {}


// TODO: modify null constant class with type
/** Null constant class. Part of AST and Token composite hierarchies. */
class NullConstant implements Token, Constant {
  public static final NullConstant ONLY = new NullConstant();
  private NullConstant() {}
  public <T> T accept(ASTVisitor<T> v) { return v.forNullConstant(this); }
  public String toString() { return "null"; }
}

class Variable implements Token, Term, WithVariable {
  private String name;
  Variable(String n) { name = n; }
  
  public String name() { return name; }
  
  /** Method in WithVariable interface; trivial in this class. */
  public Variable var() { return this; }
  
  public <ResType> ResType accept(ASTVisitor<ResType> v) { return v.forVariable(this); }
  public String toString() { return name; }
}

class OpToken implements Token {
  private String symbol;
  private boolean isUnOp;
  private boolean isBinOp;
  /** the corresponding unary operator in UnOp */
  private UnOp unOp;
  /** the corresponding binary operator in BinOp */
  private BinOp binOp;
  
  private OpToken(String s, boolean iu, boolean ib, UnOp u, BinOp b) {
    symbol = s; isUnOp = iu; isBinOp = ib; unOp = u; binOp = b; 
  }
  
  /** factory method for constructing OpToken serving as both UnOp and BinOp */
  public static OpToken newBothOpToken(UnOp u, BinOp b) {
    return new OpToken(u.toString(), true, true, u, b);
  }
  
  /** factory method for constructing OpToken serving as BinOp only */
  public static OpToken newBinOpToken(BinOp b) {
    return new OpToken(b.toString(), false, true, null, b);
  }
  
  /** factory method for constructing OpToken serving as UnOp only */
  public static OpToken newUnOpToken(UnOp u) {
    return new OpToken(u.toString(), true, false, u, null);
  }
  
  public String symbol() { return symbol; }
  public boolean isUnOp() { return isUnOp; }
  public boolean isBinOp() { return isBinOp; }
  public UnOp toUnOp() { 
    if (unOp == null) 
      throw new NoSuchElementException("OpToken " + this + " does not denote a unary operator");
    return unOp;
  }
  
  public BinOp toBinOp() { 
    if (binOp == null) 
      throw new NoSuchElementException("OpToken " + this + " does not denote a binary operator");
    return binOp; 
  }
  public String toString() { return symbol; }
}

class KeyWord implements Token {
  private String name;
  
  KeyWord(String n) { name = n; }
  public String name() { return name; }
  public String toString() { return name; }
}

class LeftParen implements Token {
  public String toString() { return "("; }
  private LeftParen() {}
  public static final LeftParen ONLY = new LeftParen();
}

class RightParen implements Token {
  public String toString() { return ")"; }
  private RightParen() {}
  public static final RightParen ONLY = new RightParen();
}

class LeftBrack implements Token {
  public String toString() { return "["; }
  private LeftBrack() {}
  public static final LeftBrack ONLY = new LeftBrack();
}

class RightBrack implements Token {
  public String toString() { return "]"; }
  private RightBrack() {}
  public static final RightBrack ONLY = new RightBrack();
}

/* Supports the addition of blocks to Jam */
class LeftBrace implements Token {
  public String toString() { return "{"; }
  private LeftBrace() {}
  public static final LeftBrace ONLY = new LeftBrace();
}

class RightBrace implements Token {
  public String toString() { return "}"; }
  private RightBrace() {}
  public static final RightBrace ONLY = new RightBrace();
}

class Comma implements Token {
  public String toString() { return ","; }
  private Comma() {}
  public static final Comma ONLY = new Comma();
}

class SemiColon implements Token {
  public String toString() { return ";"; }
  private SemiColon() {}
  public static final SemiColon ONLY = new SemiColon();
}

class EndOfFile implements Token {
  public String toString() { return "*EOF*"; }
  private EndOfFile() {}
  public static final EndOfFile ONLY = new EndOfFile();
}