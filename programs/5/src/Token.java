import java.util.*;

/* Token classes.  Note: some AST classes and AST component classes are also Tokens. */

/** A data object representing a Jam token */
interface Token {}

interface Type extends Token{

  public String toString();

  public Boolean equals(Type otherType);
}

class IntType implements Type {
  public static final IntType ONLY = new IntType();

  private IntType() {}

  public String toString() {
    return "int";
  }

  public Boolean equals(Type otherType) {
    // TODO: Make sure this is the correct way to do equals.
    return otherType instanceof IntType;
  }
}

class BoolType implements Type {
  public static final BoolType ONLY = new BoolType();

  private BoolType() {}

  public String toString() {
    return "bool";
  }

  public Boolean equals(Type otherType) {
    return otherType instanceof BoolType;
  }
}

class UnitType implements Type {
  public static final UnitType ONLY = new UnitType();

  private UnitType() {}

  public String toString() {
    return "unit";
  }

  public Boolean equals(Type otherType) {
    return otherType instanceof UnitType;
  }
}

class ListType implements Type {

    private Type listType;

    public ListType(Type listType) {
        this.listType = listType;
    }

    public String toString() {
        return "list " + listType;
    }

    public Type listType() {
        return this.listType;
    }

    public Boolean equals(Type otherType) {
        return otherType instanceof ListType && this.listType().equals(((ListType) otherType).listType());
    }
}

class RefType implements Type {

    private Type refType;

    public RefType(Type refType) {
        this.refType = refType;
    }

    public String toString() {
        return "ref " + refType;
    }

    public Type refType() {
        return this.refType;
    }

    public Boolean equals(Type otherType) {
        return otherType instanceof RefType && this.refType().equals(((RefType) otherType).refType());
    }
}

class FunType implements Type {

    private Type[] paramType;
    private Type outType;

    public FunType(Type[] paramType, Type outType) {
        this.paramType = paramType;
        this.outType = outType;
    }

    public String toString() {
        return "fun " + outType;
    }

    public Type[] paramType() {
        return this.paramType;
    }

    public Type outType() {
        return this.outType;
    }

    public Boolean equals(Type otherType) {
        if (!(otherType instanceof FunType)) {
            return false;
        }

        FunType other = (FunType) otherType;

        if (!(this.outType().equals(other.outType()))) {
            return false;
        }

        if (this.paramType().length != other.paramType().length) {
            return false;
        }

        for (int i = 0; i < this.paramType().length; i++) {
            if (!(this.paramType()[i].equals(other.paramType()[i]))) {
                return false;
            }
        }
        return true;
    }
}


/** Null constant class. Part of AST composite hierarchy. */
class NullConstant implements Token, Constant {
  public static final NullConstant ONLY = new NullConstant();
  public NullConstant() { }
  public <T> T accept(ASTVisitor<T> v) { return v.forNullConstant(this); }
  public String toString() { return "null"; }
}


class TypedNullConstant extends NullConstant {
  private Type nullType;
  public TypedNullConstant(Type nullType) {
    super();
    this.nullType = nullType;
  }
    public Type type() { return nullType; }

    public String toString() {
      return "null:" + nullType;
    }
}

class Variable implements Token, Term, WithVariable {
  private String name;
  Variable(String n) { name = n; }
  
  public String name() { return name; }
  
  /** Method in WithVariable interface; trivial in this class. */
  public Variable var() { return this; }
  
  public <ResType> ResType accept(ASTVisitor<ResType> v) { return v.forVariable(this); }
  public String toString() { return name; }

  public boolean equals(Object o) {
      if (!(o instanceof Variable)) {
          return false;
      } else {
          return ((Variable) o).name().equals(this.name());
      }
  }
}

class TypedVariable extends Variable {
  private Type varType;

  TypedVariable(String n, Type varType) {
    super(n);
    this.varType = varType;
  }

  public Type type() { return varType; }

  // TODO: not sure about this because we compare names of variables to names of typedvariables
  public String toString() {
      return super.toString() + ":" + varType;
  }
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

class Colon implements Token {
  public String toString() { return ":"; }
  private Colon() {}
  public static final Colon ONLY = new Colon();
}