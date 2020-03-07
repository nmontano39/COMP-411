/* JamVal data definitions (including some Token definitions. */

/** A data object representing a Jam value.  
  * JamVal ::= IntConstant | BoolConstant | JamList | JamFun 
  */
interface JamVal {
  <ResType> ResType accept(JamValVisitor<ResType> jvv);
}

/** A visitor interface for the JamVal type (Jam values).\ */
interface JamValVisitor<ResType> {
  ResType forIntConstant(IntConstant ji);
  ResType forBoolConstant(BoolConstant jb);
  ResType forJamList(JamList jl);
  ResType forJamFun(JamFun jf);
  ResType forJamBox(JamBox jb);
  ResType forUnit(Unit u);
}

/* JamVal classes */

/** Class representing a Jam integer constant as a Token, Constant, and JamVal.  */
class IntConstant implements Token, Constant, JamVal {
  private int value;
  
  IntConstant(int i) { value = i; }
  // duplicates can occur!
  
  public int value() { return value; }
  
  public <ResType> ResType accept(ASTVisitor<ResType> v) { return v.forIntConstant(this); }
  public <ResType> ResType accept(JamValVisitor<ResType> v) { return v.forIntConstant(this); }
  /** redefines equals so that equal integers are recognized as equal */
  public boolean equals(Object other) {
    return (other != null && this.getClass() == other.getClass()) && 
      (value == ((IntConstant)other).value());
  }
  /** computes the obvious hashcode for this consistent with equals */
  public int hashcode() { return value; }
  public String toString() { return String.valueOf(value); }
}

/** A Jam boolean constant, also used to represent a boolean token for parsing */
class BoolConstant implements Token, Constant, JamVal {
  private boolean value;
  private BoolConstant(boolean b) { value = b; }
  
  /** singleton pattern definitions */
  public static final BoolConstant FALSE = new BoolConstant(false);
  public static final BoolConstant TRUE = new BoolConstant(true);
  
  /** factory method that returns BoolConstant corresponding to b */
  public static BoolConstant toBoolConstant(boolean b) { 
    if (b) return TRUE; 
    else return FALSE;
  }
  
  public boolean value() { return value; }
  public BoolConstant not() { if (this == FALSE) return TRUE; else return FALSE; }
  
  public <ResType> ResType accept(ASTVisitor<ResType> av) { return av.forBoolConstant(this); }
  public <ResType> ResType accept(JamValVisitor<ResType> jv) { return jv.forBoolConstant(this); }
  public String toString() { return String.valueOf(value); }
}

interface PureList<ElemType> {
  abstract PureList<ElemType> cons(ElemType e);
  abstract PureList<ElemType> empty();
  abstract <ResType> ResType accept(PureListVisitor<ElemType, ResType> v);
  abstract boolean contains(ElemType e);
  abstract String toStringHelp();
  abstract PureList<ElemType> append(PureList<ElemType> addedElts);
}

/** The visitor interface for the type PureList<T> */
interface PureListVisitor<ElemType, ResType> {
  ResType forEmpty(Empty<ElemType> e);
  ResType forCons(Cons<ElemType> c);
}

/** An abstract class that factors out code common to classes Empty<T> and Cons<T> */
abstract class PureListClass<ElemType> implements PureList<ElemType> {
  public PureListClass<ElemType> cons(ElemType e) { return new Cons<ElemType>(e,this); }
  public PureListClass<ElemType> empty() { return new Empty<ElemType>(); }
  public abstract <ResType> ResType accept(PureListVisitor<ElemType, ResType> v);  
  // preceding DICTATED BY BUG IN JSR-14
}

/** The empty PureList<T> class */
class Empty<ElemType> extends PureListClass<ElemType> {
  public <ResType> ResType accept(PureListVisitor<ElemType,ResType> v) { return v.forEmpty(this); }
  public PureList<ElemType> append(PureList<ElemType> addedElts) { return addedElts; }
  public boolean contains(ElemType e) { return false; }
  
  /** overrides inherited equals because Empty is not a singleton! */
  public boolean equals(Object other) { 
    return (other != null && other.getClass() == this.getClass());
  }
  
  public String toString() { return "()"; }
  public String toStringHelp() { return ""; }
}

/** The non-empty PureList<T> class */
class Cons<ElemType> extends PureListClass<ElemType> {
  ElemType first;
  PureList<ElemType> rest;
  Cons(ElemType f, PureList<ElemType> r) { first = f; rest = r; }
  
  public <ResType> ResType accept(PureListVisitor<ElemType,ResType> v) { return v.forCons(this); }
  public PureList<ElemType> append(PureList<ElemType> addedElts) { 
    return new Cons<ElemType>(first, rest.append(addedElts)); 
  }
  
  public ElemType first() { return first; }
  public PureList<ElemType> rest() { return rest; }
  public boolean contains(ElemType e) {
    if (first.equals(e)) return true;
    else return rest().contains(e);
  }
  
  /** Overrides inherited equals to perform structural equality testing. */
  public boolean equals(Object other) { 
    if (other == null || ! (other instanceof Cons)) return false;
    Cons otherCons = (Cons) other;
    return first().equals(otherCons.first()) && rest().equals(otherCons.rest());
  }
  /** Overrides hash code in accord with equals. */
  public int hashCode() { return first().hashCode() + rest().hashCode(); }
  
  public String toString() { return "(" + first + rest.toStringHelp() + ")"; }
  public String toStringHelp() { return " " + first + rest.toStringHelp(); }
}

/** A Jam list */
interface JamList extends PureList<JamVal>, JamVal {
  /** Factory method that constructs an empty list. */
  JamEmpty empty();
  /** Factory method that constructs a non-empty list of form cons(v, this). */
  JamCons cons(JamVal v);
  /** Helper method that returns the depth-bounded string representation of this list with a leading blank in front of
    * each element and no enclosing parentheses.  For lists longer than maxDepth, elipsis is printed instead of the 
    * elements. */
  String toStringHelp(int maxDepth);
}

class JamEmpty extends Empty<JamVal> implements JamList {
  public static final JamEmpty ONLY = new JamEmpty();
  private JamEmpty() {}
  
  public JamEmpty empty() { return ONLY; }
  public JamCons cons(JamVal v) { return new JamCons(v, this); }
  public <ResType> ResType accept(JamValVisitor<ResType> v) { return v.forJamList(this); }
  public String toStringHelp(int maxDepth) { return ""; }
}

class JamCons extends Cons<JamVal> implements JamList {
  /** Maximum depth of printing the elements of a potentially lazy stream. */
  private static final int MAX_DEPTH = 1000;

  public JamCons(JamVal v, JamList vList) { super(v, vList); }
  
  /** Factory method that returns an empty list. */
  public JamEmpty empty() { return JamEmpty.ONLY; }
  
  /** Factory method that return a list consisting of cons(v, this). */
  public JamCons cons(JamVal v) { return new JamCons(v, this); }
  
  public <ResType> ResType accept(JamValVisitor<ResType> v) { return v.forJamList(this); }
  
  public JamList rest() { return (JamList) super.rest(); }
  
  /** Returns val if val is a JamList. Otherwise it throws an EvalException. */
  public static JamList checkList(JamVal val) {
    if (val instanceof JamList)  return (JamList)val;
    throw new EvalException("The second argument to lazy cons is `" + val + "' which is not a list");
  }
  
  /* Depth-bounded printing method which must be defined at this level so that ordinary JamCons nodes appearing
   * within lazy lists are printed correctly. */
  
  /** Return the depth-bounded string representation of this. */
  public String toString() { return "(" + first() + rest().toStringHelp(MAX_DEPTH) + ")"; }
  
  /** Return the depth-bounded string representation for this with a leading blank but no enclosing parentheses. */
  public String toStringHelp(int maxDepth) {
    if (maxDepth == 0)  return " ...";
    return " " + first() + rest().toStringHelp(maxDepth - 1);
  }
}

/** A class representing an unevaluated expresssion (together with the corresponding evaluator). */
class Suspension {
  private AST exp;
  private EvalVisitor ev;  
  
  Suspension(AST a, EvalVisitor e) { exp = a; ev = e; }
  
  public AST exp() { return exp; }
  public EvalVisitor ev() { return ev; }
  public void putEv(EvalVisitor e) { ev = e; }
  
  /** Evaluates this suspension. */
  JamVal eval() { 
    // System.err.println("eval() called on the susp with AST = " + exp);
    return exp.accept(ev);  } 
  
  public String toString() { return "<" + exp + ", " + ev + ">"; }
}

/** Class for a lazy cons structure. */
class JamLazyNameCons extends JamCons {
  /** Suspension for first */
  protected Suspension firstSusp;
  
  /** Suspension for rest */
  protected Suspension restSusp;
  
  public JamLazyNameCons(AST f, AST r, EvalVisitor ev) {
    super(null, null);
    firstSusp = new Suspension(f, ev);
    restSusp = new Suspension(r, ev);
  }
  
  public JamVal first() { return firstSusp.eval(); }
  public JamList rest() { return checkList(restSusp.eval()); }
}

/** Class for a lazy cons with optimization. */
class JamLazyNeedCons extends JamLazyNameCons {
  
  public JamLazyNeedCons(AST f, AST r, EvalVisitor ev) { super(f, r, ev); }
  
  public JamVal first() {
    if (first == null) {
      first = firstSusp.eval();
      firstSusp = null;
    }
    return first;
  }
  
  public JamList rest() {
    if (rest == null) {
      rest = checkList(restSusp.eval());
      restSusp = null;
    }
    return (JamList)rest;
  }
}

/** a Jam function (closure or primitive function) */
abstract class JamFun implements JamVal {
  public <ResType> ResType accept(JamValVisitor<ResType> jvv) { return jvv.forJamFun(this); }
  abstract public <ResType> ResType accept(JamFunVisitor<ResType> jfv);
}

/** The visitor interface for the JamFun type */
interface JamFunVisitor<ResType> {
  ResType forJamClosure(JamClosure c);
  ResType forPrimFun(PrimFun pf);
}

/** A Jam closure */
class JamClosure extends JamFun {
  private Map body;
  private PureList<Binding> env;
  
  JamClosure(Map b, PureList<Binding> e) { body = b; env = e; }
  Map body() { return body; }
  PureList<Binding> env() { return env; }
  public <ResType> ResType accept(JamFunVisitor<ResType> jfv) { return jfv.forJamClosure(this); }
}

/** A Jam Primitive Function.  It is a variant of the JamFun, Token, and Term types.  It is a subtype of JamVal.
  * In JamValVisitor, all PrimFuns are handled by a single visitor method.  PrimFun has its own visitor interface.
  * Invariant: there is only one copy of each primitive. 
  */
abstract class PrimFun extends JamFun implements Token, Term {
  private String name;
  PrimFun(String n) { name = n; }
  public String name() { return name; }
  public <ResType> ResType accept(ASTVisitor<ResType> v) { return v.forPrimFun(this); }
  public <ResType> ResType accept(JamFunVisitor<ResType> v) { return v.forPrimFun(this); }
  abstract public <ResType> ResType accept(PrimFunVisitor<ResType> pfv);
  public String toString() { return name; }
}

/** The visitor interface for type PrimFun. */
interface PrimFunVisitor<ResType> {
  ResType forFunctionPPrim();
  ResType forNumberPPrim();
  ResType forListPPrim();
  ResType forConsPPrim();
  ResType forNullPPrim();
//  /* Supports addition of ref cells to Jam *;/
  ResType forRefPPrim();
  
  ResType forArityPrim();
  ResType forConsPrim();
  ResType forFirstPrim();
  ResType forRestPrim();
}

/** The class representing the function? primitive. */
class FunctionPPrim extends PrimFun {
  public static final FunctionPPrim ONLY = new FunctionPPrim();
  private FunctionPPrim() { super("function?"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forFunctionPPrim(); }
}

/** The class representing the number? primitive. */
class NumberPPrim extends PrimFun {
  public static final NumberPPrim ONLY = new NumberPPrim();
  private NumberPPrim() { super("number?"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forNumberPPrim(); }
}

/** The class representing the list? primitive. */
class ListPPrim extends PrimFun {
  public static final ListPPrim ONLY = new ListPPrim();
  private ListPPrim() { super("list?"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forListPPrim(); }
}

/** The class representing the function? primitive. */
class ConsPPrim extends PrimFun {
  public static final ConsPPrim ONLY = new ConsPPrim();
  private ConsPPrim() { super("cons?"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forConsPPrim(); }
}
class NullPPrim extends PrimFun {
  public static final NullPPrim ONLY = new NullPPrim();
  private NullPPrim() { super("null?"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forNullPPrim(); }
}
///* Supports the addition of ref to Jam. */
class RefPPrim extends PrimFun {
  public static final RefPPrim ONLY = new RefPPrim();
  private RefPPrim() { super("ref?"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forRefPPrim(); }
}

class ArityPrim extends PrimFun {
  public static final ArityPrim ONLY = new ArityPrim();
  private ArityPrim() { super("arity"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forArityPrim(); }
}
class ConsPrim extends PrimFun {
  public static final ConsPrim ONLY = new ConsPrim();
  private ConsPrim() { super("cons"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forConsPrim(); }
}
class FirstPrim extends PrimFun {
  public static final FirstPrim ONLY = new FirstPrim();
  private FirstPrim() { super("first"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forFirstPrim(); }
}
class RestPrim extends PrimFun {
  public static final RestPrim ONLY = new RestPrim();
  private RestPrim() { super("rest"); }
  public <ResType> ResType accept(PrimFunVisitor<ResType> pfv) { return pfv.forRestPrim(); }
}


class JamBox implements JamVal {

  JamVal value;

  public JamBox(JamVal val) {
    this.value = val;
  }

  public JamVal getValue() {
    return value;
  }

  public void setValue(JamVal newVal) {
    this.value = newVal;
  }


  public <ResType> ResType accept(JamValVisitor<ResType> v) { return v.forJamBox(this); }

  public String toString() {
    return "(" + "ref " + getValue() + ")";
  }

//  public <ResType> ResType accept(ASTVisitor<ResType> av) { return av.forBox(this); }

}

class Unit implements JamVal {

  // Empty constructor because Unit does nothing.

  public static final JamVal ONLY = new Unit();

  private Unit() {}

  public <ResType> ResType accept(JamValVisitor<ResType> v) { return v.forUnit(this); }

  public String toString() {
    return "unit";
  }

}