import java.io.*;
import java.util.*;

/** AST class definitions.  These definitions anticipate the definition and implementation of an interpreter based
  * on static distance coordinates (with diferennt variable, map, let nodes) which will be done in Assignment 7.  In 
  * Assignment 6, we will not use any SDxxx interfaces or SDAST classes. The ASTxxx interfaces from earlier 
  * assignments are now named SymASTxxx and the AST type includes both SymAST (identified with the AST type from 
  * Assignmens 1-4). Some of the executable code in this interpreter mentions the types AST and 
  * SDAST and related types to support anticipated code sharing in Assignment 7. The three types ASTVisitor, 
  * SymASTVisitor, and SDASTVisitor are ugly.  We could eliminate SymASTVisitor and SDASTVisitor at the cost of less 
  * precise static type checking (which has already been severely compromised by Java's weak support for generics).  
  * This approach is a defensible (and perhaps simpler) design if you are using your own solutions to earlier 
  * assignments as the starting point for Assignment 6 instead of this code base. */

/** AST ::= SymAST | SDAST */
interface AST {
  /** This interface includes both SymASTs and SDASTs, which is unattractive from the perspective of type checking but
    * it fosters code sharing.  Unfortunately, Java generics are restrictive so we cannot both (i) specify the data 
    * domains that we really would like and (ii) aggressively share code.  To reduce the amount of code required for 
    * Assignments 6 and 7 we decided (ii) was a higher priority and compromised the quality of type checking. If Java
    * had a richer type system, we could pass the intended AST subtype (either SymAST or SDAST) as a type parameter and 
    * use to tailor shared code the appropriate behavior while accurately checking types but Java restricts the use of 
    * type parameters, e.g., no arrays of type T[] or casts of the form (T) where T is a type parameter.  So we live 
    * with the weaker type AST (instead of SymAST or SDAST) to share code and accept the weak static type checking 
    * inherent in this design.  For typing purposes, an ASTVisitor supports both SymASTs and SDASTs. In practice, only 
    * SymAST or SDAST methods are well-defined in a concrete ASTVisitor class; the others throw exceptions. We do not
    * support ASTs that intermix SymAST and SDAST nodes. */
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v);
}

/** The AST type for conventional abstract syntax (with variables) */
interface SymAST extends AST {
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v);
}

/** The AST type for static distance coordinate abstract syntax */
interface SDAST extends AST {
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v);
}

/** The methods in SymASTVisitor + SDASTVisitor; none of our visitors actually works on both, but this approach assigns
  * a common type to what are really SymASTVisitors and SDASTVisitors */
interface ASTVisitor<RtnType> extends SymASTVisitor<RtnType>, SDASTVisitor<RtnType> {}

/** The common methods in SymASTVisitor and SDASTVisitor.  This type is not used in Assignment 6. */
interface ComASTVisitor<RtnType> {
//  RtnType forDefault(AST a);
  RtnType forBoolConstant(BoolConstant b);
  RtnType forIntConstant(IntConstant i);
  RtnType forNullConstant(NullConstant n);
  RtnType forPrimFun(PrimFun f);
  RtnType forUnOpApp(UnOpApp u);
  RtnType forBinOpApp(BinOpApp b);
  RtnType forApp(App a);
  RtnType forIf(If i); 
  RtnType forBlock(Block i);
}

interface SymASTVisitor<RtnType> extends ComASTVisitor<RtnType> {
  RtnType forSymVariable(Variable v);
  RtnType forMap(Map m);
  RtnType forLet(Let l);
  RtnType forLetRec(LetRec l);
  RtnType forLetcc(Letcc host);
}

interface SDASTVisitor<RtnType> extends ComASTVisitor<RtnType> {
  RtnType forPair(Pair p); 
  RtnType forSMap(SMap sm);
  RtnType forSLet(SLet sl);
  RtnType forSLetRec(SLetRec slr);
}

/** An important SubType of the AST Type but it does not have its own visitor interface.  Visited as part of AST type.
  * Term ::= Constant | PrimFun | Variable 
  */
interface Term extends SymAST, SDAST {}

/** The subtype of Term consisting of atomic constants.
  * Constant ::= IntConstant | BoolConstant | NullConstant 
  */
interface Constant extends Term {}

/** interface for classes with a variable field  */
interface WithVariable {
  Variable var();
}

/** The class that represents symbolic Jam variables; they are only created by the Lexer and the SConverter which 
  * guarantees that they are unique.  Note: this class should perhaps be called SymVariable. This class has a
  * coarse typing because the code for toString() is shared by SymAST and SDASTs and requires all variables (including
  * Pairs) to be Terms. */
class Variable implements Token, Term, SymAST, WithVariable {
  private String name;
  Variable(String n) { name = n; }
  Variable(Variable v) { name = v.name(); }
  
  public Variable var() { return this; }
  public String name() { return name; }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forSymVariable(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { 
    throw new SyntaxException("Variable " + this + "is being traversed by SDASTVisitor " + v);
  }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forSymVariable(this); }
  public String toString() { return name; }
}

/** Op ::= UnOp | BinOp */
interface Op {}

abstract class UnOp implements Op {
  String name;
  public UnOp(String s) { name = s; }
  public String toString() { return name; }
  public abstract <RtnType> RtnType accept(UnOpVisitor<RtnType> v);
}

interface UnOpVisitor<RtnType> {
//  RtnType forDefault(UnOp op);
  RtnType forUnOpPlus(UnOpPlus op);
  RtnType forUnOpMinus(UnOpMinus op);
  RtnType forOpTilde(OpTilde op);
  RtnType forOpBang(OpBang op);
  RtnType forOpRef(OpRef op);
}

abstract class BinOp implements Op {
  String name;
  public BinOp(String s) { name = s; }
  public String toString() { return name; }
  public abstract <RtnType> RtnType accept(BinOpVisitor<RtnType> v);
}

interface BinOpVisitor<RtnType> {
//  RtnType forDefault(BinOp op);
  RtnType forBinOpPlus(BinOpPlus op);
  RtnType forBinOpMinus(BinOpMinus op);
  RtnType forOpTimes(OpTimes op);
  RtnType forOpDivide(OpDivide op);
  RtnType forOpEquals(OpEquals op);
  RtnType forOpNotEquals(OpNotEquals op);
  RtnType forOpLessThan(OpLessThan op);
  RtnType forOpGreaterThan(OpGreaterThan op);
  RtnType forOpLessThanEquals(OpLessThanEquals op);
  RtnType forOpGreaterThanEquals(OpGreaterThanEquals op);
  RtnType forOpAnd(OpAnd op);
  RtnType forOpOr(OpOr op);
  RtnType forOpGets(OpGets op);
}

class UnOpPlus extends UnOp {
  public static final UnOpPlus ONLY = new UnOpPlus();
  private UnOpPlus() { super("+"); }
  public <RtnType> RtnType accept(UnOpVisitor<RtnType> v) { return v.forUnOpPlus(this); }
}

class UnOpMinus extends UnOp {
  public static final UnOpMinus ONLY = new UnOpMinus();
  private UnOpMinus() { super("-"); }
  public <RtnType> RtnType accept(UnOpVisitor<RtnType> v) { return v.forUnOpMinus(this); }
}

class OpTilde extends UnOp {
  public static final OpTilde ONLY = new OpTilde();
  private OpTilde() { super("~"); }
  public <RtnType> RtnType accept(UnOpVisitor<RtnType> v) { return v.forOpTilde(this); }
}

class OpBang extends UnOp {
  public static final OpBang ONLY = new OpBang();
  private OpBang() { super("!"); }
  public <RtnType> RtnType accept(UnOpVisitor<RtnType> v) {
    return v.forOpBang(this); 
  }
}

class OpRef extends UnOp {
  public static final OpRef ONLY = new OpRef();
  private OpRef() { super("ref"); }
  public <RtnType> RtnType accept(UnOpVisitor<RtnType> v) {
    return v.forOpRef(this); 
  }
}

class BinOpPlus extends BinOp {
  public static final BinOpPlus ONLY = new BinOpPlus();
  private BinOpPlus() { super("+"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forBinOpPlus(this); 
  }
}

class BinOpMinus extends BinOp {
  public static final BinOpMinus ONLY = new BinOpMinus();
  private BinOpMinus() { super("-"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forBinOpMinus(this); 
  }
}
class OpTimes extends BinOp {
  public static final OpTimes ONLY = new OpTimes();
  private OpTimes() { super("*"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpTimes(this); 
  }
}

class OpDivide extends BinOp {
  public static final OpDivide ONLY = new OpDivide();
  private OpDivide() { super("/"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpDivide(this); 
  }
}

class OpEquals extends BinOp {
  public static final OpEquals ONLY = new OpEquals();
  private OpEquals() { super("="); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpEquals(this); 
  }
}

class OpNotEquals extends BinOp {
  public static final OpNotEquals ONLY = new OpNotEquals();
  private OpNotEquals() { super("!="); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpNotEquals(this); 
  }
}

class OpLessThan extends BinOp {
  public static final OpLessThan ONLY = new OpLessThan();
  private OpLessThan() { super("<"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpLessThan(this); 
  }
}

class OpGreaterThan extends BinOp {
  public static final OpGreaterThan ONLY = new OpGreaterThan();
  private OpGreaterThan() { super(">"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpGreaterThan(this); 
  }
}

class OpLessThanEquals extends BinOp {
  public static final OpLessThanEquals ONLY = new OpLessThanEquals();
  private OpLessThanEquals() { super("<="); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpLessThanEquals(this); 
  }
}

class OpGreaterThanEquals extends BinOp {
  public static final OpGreaterThanEquals ONLY = new OpGreaterThanEquals();
  private OpGreaterThanEquals() { super(">="); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpGreaterThanEquals(this); 
  }
}

class OpAnd extends BinOp {
  public static final OpAnd ONLY = new OpAnd();
  private OpAnd() { super("&"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpAnd(this); 
  }
}

class OpOr extends BinOp {
  public static final OpOr ONLY = new OpOr();
  private OpOr() { super("|"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) {
    return v.forOpOr(this); 
  }
}

class OpGets extends BinOp {
  public static final OpGets ONLY = new OpGets();
  private OpGets() { super("<-"); }
  public <RtnType> RtnType accept(BinOpVisitor<RtnType> v) { return v.forOpGets(this); }
}

/* UnOpApp is a Term because it does not need enclosing parentheses when appearing in a binary expression */
class UnOpApp implements Term {
  private UnOp rator;
  private AST arg;
  
  UnOpApp(UnOp r, AST a) { rator = r; arg = a; }
  
  public UnOp rator() { return rator; }
  public AST arg() { return arg; }
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forUnOpApp(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forUnOpApp(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forUnOpApp(this); }
  public String toString() { return rator + " " + arg; }
}

class BinOpApp implements Term {
  private BinOp rator;
  private AST arg1, arg2;
  
  BinOpApp(BinOp r, AST a1, AST a2) { rator = r; arg1 = a1; arg2 = a2; }
  
  public BinOp rator() { return rator; }
  public AST arg1() { return arg1; }
  public AST arg2() { return arg2; }
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forBinOpApp(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forBinOpApp(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forBinOpApp(this); }
  public String toString() {
    return "(" + toString(arg1) + " " + rator + " " + toString(arg2) + ")"; 
  }
  private String toString(AST arg) {
    String argString = arg.toString();
    if (! (arg instanceof Term)) return "(" + argString + ")";
    else return argString;
  }
}

class Map implements SymAST {
  private Variable[] vars;
  private SymAST body;
  
  Map(Variable[] v, SymAST b) { vars = v; body = b; }
  public Variable[] vars() { return vars; }
  public SymAST body() { return body; }
  
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forMap(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forMap(this); }
  
  public String toString() { 
    return "map " + ToString.toString(vars,",") + " to " + body ;
  }
}  

class App implements Term {
  private AST rator;
  private AST[] args;
  
  App(AST r, AST[] a) { rator = r; args = a; }
  
  public AST rator() { return rator; }
  public AST[] args() { return args; }
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forApp(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forApp(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forApp(this); }
  
  public String toString() { 
    if ((rator instanceof PrimFun) || (rator instanceof Variable) || (rator instanceof Pair))
      return rator + "(" + ToString.toString(args,", ") + ")"; 
    else
      return "(" +  rator + ")(" + ToString.toString(args,", ") + ")"; 
  }
}  

class If implements SymAST, SDAST {
  private AST test, conseq, alt;
  If(AST t, AST c, AST a) { test = t; conseq = c; alt = a; }
  
  public AST test() { return test; }
  public AST conseq() { return conseq; }
  public AST alt() { return alt; }
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forIf(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forIf(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forIf(this); }
  public String toString() { 
    return "if " + test + " then " + conseq + " else " + alt ; 
  }
}  

class Let implements SymAST {
  private Def[] defs;
  private SymAST body;
  Let(Def[] d, SymAST b) { defs = d; body = b; }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forLet(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forLet(this); }
  public Def[] defs() { return defs; }
  public SymAST body() { return body; }
  
  /** Gets the defined vars */
  public Variable[] vars() { 
    int n = defs.length;
    Variable[] vars = new Variable[n];
    for (int i = 0; i < n; i++) vars[i] = defs[i].lhs();
    return vars;
  }
  
  /** Gets the exps (rhs's) of the defs */
  public SymAST[] exps() {     
    int n = defs.length;
    SymAST[] exps =  new SymAST[n];
    for (int i = 0; i < n; i++) exps[i] = defs[i].rhs();
    return exps;
  }
  
  public String toString() { 
    return "let " + ToString.toString(defs," ") + " in " + body; 
  }
}  

class LetRec implements SymAST {
  private Def[] defs;
  private SymAST body;
  LetRec(Def[] d, SymAST b) { defs = d; body = b; }
 
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forLetRec(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forLetRec(this); }
  
  public Def[] defs() { return defs; }
  public SymAST body() { return body; }
  
  /** Gets the defined vars */
  public Variable[] vars() { 
    int n = defs.length;
    Variable[] vars = new Variable[n];
    for (int i = 0; i < n; i++) vars[i] = defs[i].lhs();
    return vars;
  }
  
  /** Gets the exps (rhs's) of the defs */
  public SymAST[] exps() {     
    int n = defs.length;
    SymAST[] exps =  new SymAST[n];
    for (int i = 0; i < n; i++) exps[i] = defs[i].rhs();
    return exps;
  }
  
  public String toString() { 
    return "letrec " + ToString.toString(defs," ") + " in " + body; 
  }
}

class Block implements  SymAST, SDAST {
  /** Invariant: exps.length > 0 */
  private AST[] exps;  
  
  /** Initialize definitions and body fields */
  Block(AST[] es) { exps = es; }
  
  /** Applies the visitor v to this. */
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forBlock(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forBlock(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forBlock(this); }
  /** Gets the definitions field */
  public AST[] exps() { return exps; }
  
  public String toString() {
    return "{" + ToString.toString(exps,"; ") + "}";
  }
}

class Letcc implements SymAST {
  private Variable var;
  private SymAST body;
  Letcc(Variable v, SymAST b) { var = v; body = b; }
 
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forLetcc(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forLetcc(this); }
  
 
  /** Getters */
  public Variable var() { return var; }
  public SymAST body() { return body; }
  
  public String toString() { 
    return "letcc " + var + " in " + body; 
  }
}

class Def {
  private Variable lhs;
  private SymAST rhs;  
  
  Def(Variable l, SymAST r) { lhs = l; rhs = r; }
  public Variable lhs() { return lhs; }
  public SymAST rhs() { return rhs; }
  public void setRhs(SymAST r) { rhs = r; }
  
  public String toString() { return lhs + " := " + rhs + ";"; }
  
  public static Def[] makeDefs(Variable[] vars, SymAST[] exps) {
    int n = vars.length;
    if (exps.length != n) throw 
      new SyntaxException("Attempt to build Defs[] with mismatched Variable[] " + vars + " and SymAST[] " + exps);
    Def[] newDefs = new Def[n];
    for (int i = 0; i < n; i++) newDefs[i] = new Def(vars[i], exps[i]);
    return newDefs;
  }
  
  static Def[] tail(Def[] in) {
    int n = in.length;
    Def[] out = new Def[n-1];
    for (int i = 0; i < n-1; i++) out[i] = in[i+1];
    return out;
  }
}

/** SDAST representation for a variable */
class Pair implements Term, SDAST {
  private int dist;
  private int offset;
  
  Pair(int d, int o) { dist = d; offset = o; }
  public int dist() { return dist; }
  public int offset() { return offset; }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { 
    throw new SyntaxException("Pair " + this + " is being traversed by SymASTVisitor " + v);
  } 
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return  v.forPair(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return  v.forPair(this); } 
  public String toString() { return "[" + dist + "," + offset + "]"; }
}

/** SDAST representation for a map */
class SMap implements SDAST {
  private int arity;
  private SDAST body;
  SMap(int a, SDAST b) { arity = a; body = b; }
  public int arity() { return arity; }
  public SDAST body() { return body; }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forSMap(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forSMap(this); }
  public String toString() { 
    return "map [*" + arity + "*] to " + body ;
  }
}  

/** SDAST for a Jam (raw) let */
class SLet implements SDAST {
  private SDAST[] rhss;
  private SDAST body;
  SLet(SDAST[] r, SDAST b) { rhss = r; body = b; }
  public SDAST[] rhss() { return rhss; }
  public SDAST body() { return body; }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forSLet(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forSLet(this); }
  public String toString() { 
    return "let [*" + rhss.length + "*] " + ToString.toString(rhss,"; ") + "; in " + body; 
  }
}  


/** SDAST for a Jam recursive let */
class SLetRec implements SDAST {
  private SDAST[] rhss;
  private SDAST body;
  SLetRec(SDAST[] r, SDAST b) { rhss = r; body = b; }
  public SDAST[] rhss() { return rhss; }
  public SDAST body() { return body; }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forSLetRec(this); }
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forSLetRec(this); }
  public String toString() { 
    return "letrec [*" +rhss.length +"*] " + ToString.toString(rhss,"; ") + "; in " + body; 
  }
}

class ToString {
  
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

class ParseException extends RuntimeException {
  ParseException(String s) {
    super(s);
  }
}

class CPSException extends RuntimeException {
  CPSException(String s) { super(s); }
}

/** JamVal and Token Data Definitions */
/** JamVal ::= IntConstant | BoolConstant | JamList | JamFun | JamVoid | JamRef */

/** a data object representing a Jam value */
interface JamVal {
  <RtnType> RtnType accept(JamValVisitor<RtnType> jvv);
}

/** a visitor object for Jam values */
interface JamValVisitor<RtnType> {
  RtnType forIntConstant(IntConstant ji);
  RtnType forBoolConstant(BoolConstant jb);
  RtnType forJamList(JamList jl);
  RtnType forJamFun(JamFun jf);
  RtnType forJamVoid(JamVoid jf);
  RtnType forJamRef(JamRef jr);
  RtnType forJamUnit(JamUnit ju);
}

/** A data object representing a Jam token */
interface Token {}

/** JamVal classes */

/* The singleton class representing the Jam token "null" */
class NullConstant implements Token, Constant {
  public static final NullConstant ONLY = new NullConstant();
  private NullConstant() {}
  
  public <T> T accept(ASTVisitor<T> v) { return v.forNullConstant(this); } 
  public <T> T accept(SymASTVisitor<T> v) { return v.forNullConstant(this); }
  public <T> T accept(SDASTVisitor<T> v) { return v.forNullConstant(this); }
 
  public String toString() { return "null"; }
}

/** a Jam integer constant, also used to represent an integer token for parsing  */
class IntConstant implements Token, Constant, JamVal {
  private int value;
  
  IntConstant(int i) { value = i; } // duplicates can occur!
  
  public int value() { return value; }
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forIntConstant(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forIntConstant(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forIntConstant(this); }
  
  public <RtnType> RtnType accept(JamValVisitor<RtnType> v) { return v.forIntConstant(this); }
  
  /** redefines equals so that equal integers are recognized as equal */
  public boolean equals(Object other) {
    return (other != null && this.getClass() == other.getClass()) && 
      (value == ((IntConstant)other).value());
  }
  /** computes the obvious hashcode for this consistent with equals */
  public int hashcode() { return value; }
  public String toString() { return String.valueOf(value); }
}

/** a Jam boolean constant, also used to represent a boolean token for parsing */
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
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forBoolConstant(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forBoolConstant(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forBoolConstant(this); }
  
  public <RtnType> RtnType accept(JamValVisitor<RtnType> v) { return v.forBoolConstant(this); }
  public String toString() { return String.valueOf(value); }
}

/** Immutable List and Binding Classes */

interface PureList<ElemType> {
  PureList<ElemType> cons(ElemType o);
  PureList<ElemType> empty();
  <RtnType> RtnType accept(PureListVisitor<ElemType, RtnType> v);
  String toStringHelp();
  PureList<ElemType> append(PureList<ElemType> addedElts);
  boolean hasMember(ElemType e);
}

/** The visitor interface for the type PureList<T> */
interface PureListVisitor<ElemType, RtnType> {
  RtnType forEmpty(Empty<ElemType> e);
  RtnType forCons(Cons<ElemType> c);
}

/** An abstract class that factors out code common to classes Empty<T> and Cons<T> */
abstract class PureListClass<ElemType> implements PureList<ElemType> {
  public Cons<ElemType> cons(ElemType o) { return new Cons<ElemType>(o,this); }
  public Empty<ElemType> empty() { return new Empty<ElemType>(); }
  public static <T> PureListClass<T> arrayToList(T[] array) {
    int n = array.length;
    PureListClass<T> list = new Empty<T>();
    for (int i = n-1; i >= 0;  i--)  list = list.cons(array[i]);
    return list;
  }
}

/** The empty PureList<T> class */
class Empty<ElemType> extends PureListClass<ElemType> {
  public <RtnType> RtnType accept(PureListVisitor<ElemType,RtnType> v) { return v.forEmpty(this); }
  public PureList<ElemType> append(PureList<ElemType> addedElts) { return addedElts; }
  public boolean hasMember(ElemType e) { return false; }
  
  /** overrides inherited equals because Empty is not a singleton! */
  public boolean equals(Object other) { 
    return (other != null && other.getClass() == this.getClass());
  }
  
  public String toString() { return "()"; }
  public String toStringHelp() { return ""; }
}

/** The non-empty PureList<T> class */
class Cons<ElemType> extends PureListClass<ElemType> {
  protected ElemType first;
  protected PureList<ElemType> rest;
  Cons(ElemType f, PureList<ElemType> r) { first = f; rest = r; }
  
  public <RtnType> RtnType accept(PureListVisitor<ElemType,RtnType> v) { return v.forCons(this); }
  public PureList<ElemType> append(PureList<ElemType> addedElts) { 
    return new Cons<ElemType>(first, rest.append(addedElts)); 
  }
  
  public ElemType first() {  return first; }
  public PureList<ElemType> rest() { return rest; }
  public boolean hasMember(ElemType e) { 
    if (first().equals(e)) return true;
    return rest().hasMember(e);
  }
  public boolean equals(Object other) { 
    if (other == null || this.getClass() != other.getClass()) return false;
    Cons otherCons = (Cons) other;
    return first().equals(otherCons.first()) && rest().equals(otherCons.rest());
  }
  
  public String toString() { return "(" + first + rest.toStringHelp() + ")"; }
  public String toStringHelp() { return " " + first + rest.toStringHelp(); }
}

/* A Environment of Variable Bindings */
interface VarEnv extends Environment, PureList<Binding> {
  EmptyVarEnv empty();
  ConsVarEnv cons(Binding b);
}

class EmptyVarEnv extends Empty<Binding> implements VarEnv {
  public static final EmptyVarEnv ONLY = new EmptyVarEnv();
  private EmptyVarEnv() {};
  public EmptyVarEnv empty() { return ONLY; }
  public ConsVarEnv cons(Binding b) { return new ConsVarEnv(b,this); }
  public JamVal lookup(Object key) {
    throw new SyntaxException("Variable " + key + " not bound");
  } 
}

class ConsVarEnv extends Cons<Binding> implements VarEnv {
  public ConsVarEnv(Binding b, VarEnv e) { super(b,e); }
  public EmptyVarEnv empty() { return EmptyVarEnv.ONLY; }
  public ConsVarEnv cons(Binding b) { return new ConsVarEnv(b,this); }
  public JamVal lookup(Object key) {
    Binding match = accept(new LookupVisitor<Binding>((Variable) key));
    if (match == null) throw new SyntaxException("Variable " + key + " not bound"); 
    return match.value();
  }
}

/* A Environment of SD Distance Bindings */
interface SDEnv extends Environment, PureList<JamVal[]> {
  EmptySDEnv empty();
  ConsSDEnv cons(JamVal[] vals);
}

class EmptySDEnv extends Empty<JamVal[]> implements SDEnv {
  public static final EmptySDEnv ONLY = new EmptySDEnv();
  private EmptySDEnv() {};
  public EmptySDEnv empty() { return ONLY; }
  public ConsSDEnv cons(JamVal[] vals) { return new ConsSDEnv(vals,this); }
  public JamVal lookup(Object key) {
    throw new SyntaxException("Variable " + key + " not bound");
  } 
}

class ConsSDEnv extends Cons<JamVal[]> implements SDEnv {
  public ConsSDEnv(JamVal[] vals, SDEnv e) { super(vals,e); }
  public EmptySDEnv empty() { return EmptySDEnv.ONLY; }
  public ConsSDEnv cons(JamVal[] vals) { return new ConsSDEnv(vals,this); }
  public JamVal lookup(Object key) {
    Pair p = (Pair) key;
    Cons<JamVal[]> env = this;
    int k = p.dist();
    for (int i = 0; i < k; i++) env = (Cons<JamVal[]>) env.rest();
    return env.first()[p.offset()];
  }
}

/** A Jam list */
interface JamList extends PureList<JamVal>, JamVal {
  JamEmpty empty();
  JamCons cons(JamVal v);
  String toStringHelp(int maxDepth);
}

class JamEmpty extends Empty<JamVal> implements JamList {
  public static final JamEmpty ONLY = new JamEmpty();
  private JamEmpty() {}
  public JamEmpty empty() { return ONLY; }
  public JamCons cons(JamVal v) { return new JamCons(v, this); }
  public <RtnType> RtnType accept(JamValVisitor<RtnType> v) { return v.forJamList(this); }
  public String toStringHelp(int maxDepth) { return ""; }
}

class JamCons extends Cons<JamVal> implements JamList {
  private static final int MAX_DEPTH = 100;
  public JamCons(JamVal f, JamList r) { super(f, r); }
  public JamEmpty empty() { return JamEmpty.ONLY; }
  public JamCons cons(JamVal v) { return new JamCons(v, this); }
  
  public <RtnType> RtnType accept(JamValVisitor<RtnType> v) { return v.forJamList(this); }
  
//  public JamVal first() { return first; }  // work-around for bridge method bug in compiler (since fixed)
  public JamList rest() { return (JamList) rest; }
  
  public String toString() { return "(" + first() + rest().toStringHelp(MAX_DEPTH) + ")"; }
  public String toStringHelp(int maxDepth) { 
    if (maxDepth == 0) return " ...";
    return " " + first() + rest().toStringHelp(maxDepth - 1); }
}

/** A Jam binding */
class Binding implements WithVariable {
  private Variable var;
  protected JamVal value;
  Binding(Variable v, JamVal jv) { 
    var = v; value = jv;
  }
  public Variable var() { return var; }
  public JamVal value() { 
    if (value != null) return value;
    else throw new EvalException("Forward reference in letrec to an unbound variable");
  }
  public void setBinding(JamVal v) { value = v; }
  public String toString() { return "[" + var + ", " + value + "]"; }
}

/** Other JamVal classes */

/** a Jam function (closure or primitive function) */
abstract class JamFun implements JamVal {
  public <RtnType> RtnType accept(JamValVisitor<RtnType> jvv) { return jvv.forJamFun(this); }
  abstract public <RtnType> RtnType accept(FunVisitor<RtnType> jfv);
}

/** a Jam reference */
class JamRef implements JamVal {
  private JamVal value;
  public JamRef(JamVal v) { value = v; }
  public JamVal value() { return value; }
  public void setValue(JamVal v) { value = v; }
  public <RtnType> RtnType accept(JamValVisitor<RtnType> jr) { return jr.forJamRef(this); }
  public String toString() { return "(ref " + value + ")"; }
}

/** A degenerate Jam value for the result of JamRef <- */
class JamUnit implements JamVal {
  public static final JamUnit ONLY = new JamUnit();
  private JamUnit() {}
  public <RtnType> RtnType accept(JamValVisitor<RtnType> jvv) { return jvv.forJamUnit(this); }
  public String toString() { return "unit"; }
}

/** The visitor interface for the JamFun type */
interface FunVisitor<RtnType> {
  RtnType forClosure(Closure c);
  RtnType forPrimFun(PrimFun pf);
}

interface Environment {
  JamVal lookup(Object key);
}

interface Closure {
  int arity();
  JamVal apply(JamVal[] arg);
}

/** A Jam Primitive Function.  It is a variant of the JamFun, Token, and Term types.  It is a subtype of JamVal.
  * In JamValVisitor, all PrimFuns are handled by a single visitor method.  PrimFun has its own visitor interface.
  * Invariant: there is only one copy of each primitive. 
  */
abstract class PrimFun extends JamFun implements Token, Term {
  private String name;
  PrimFun(String n) { name = n; }
  public String name() { return name; }
  
  public <RtnType> RtnType accept(ASTVisitor<RtnType> v) { return v.forPrimFun(this); }
  public <RtnType> RtnType accept(SymASTVisitor<RtnType> v) { return v.forPrimFun(this); }
  public <RtnType> RtnType accept(SDASTVisitor<RtnType> v) { return v.forPrimFun(this); }
  
  public <RtnType> RtnType accept(FunVisitor<RtnType> v) { return v.forPrimFun(this); }
  abstract public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv);
  public String toString() { return name; }
}

/** a dummy Jam value used to implement recursive let */
class JamVoid implements JamVal {
  public static final JamVoid ONLY = new JamVoid();
  private JamVoid() {}
  public <RtnType> RtnType accept(JamValVisitor<RtnType> jvv) { return jvv.forJamVoid(this); }
}

/** a visitor for PrimFun classes */
interface PrimFunVisitor<RtnType> {
  RtnType forFunctionPPrim();
  RtnType forNumberPPrim();
  RtnType forListPPrim();
  RtnType forConsPPrim();
  RtnType forNullPPrim();
  RtnType forArityPrim();
  RtnType forConsPrim();
  RtnType forRefPPrim();
  RtnType forFirstPrim();
  RtnType forRestPrim();
  RtnType forAsBoolPrim();
}

class FunctionPPrim extends PrimFun {
  public static final FunctionPPrim ONLY = new FunctionPPrim();
  private FunctionPPrim() { super("function?"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forFunctionPPrim(); }
}
class NumberPPrim extends PrimFun {
  public static final NumberPPrim ONLY = new NumberPPrim();
  private NumberPPrim() { super("number?"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forNumberPPrim(); }
}
class ListPPrim extends PrimFun {
  public static final ListPPrim ONLY = new ListPPrim();
  private ListPPrim() { super("list?"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forListPPrim(); }
}
class ConsPPrim extends PrimFun {
  public static final ConsPPrim ONLY = new ConsPPrim();
  private ConsPPrim() { super("cons?"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forConsPPrim(); }
}
class NullPPrim extends PrimFun {
  public static final NullPPrim ONLY = new NullPPrim();
  private NullPPrim() { super("null?"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forNullPPrim(); }
}
class RefPPrim extends PrimFun {
  public static final RefPPrim ONLY = new RefPPrim();
  private RefPPrim() { super("ref?"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forRefPPrim(); }
}
class ArityPrim extends PrimFun {
  public static final ArityPrim ONLY = new ArityPrim();
  private ArityPrim() { super("arity"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forArityPrim(); }
}
class ConsPrim extends PrimFun {
  public static final ConsPrim ONLY = new ConsPrim();
  private ConsPrim() { super("cons"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forConsPrim(); }
}
class FirstPrim extends PrimFun {
  public static final FirstPrim ONLY = new FirstPrim();
  private FirstPrim() { super("first"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forFirstPrim(); }
}
class RestPrim extends PrimFun {
  public static final RestPrim ONLY = new RestPrim();
  private RestPrim() { super("rest"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forRestPrim(); }
}
class AsBoolPrim extends PrimFun {
  public static final AsBoolPrim ONLY = new AsBoolPrim();
  private AsBoolPrim() { super("asBool"); }
  public <RtnType> RtnType accept(PrimFunVisitor<RtnType> pfv) { return pfv.forAsBoolPrim(); }
} 

/** Token classes */

/** The illegal token class used by the lexer for reserved words and ops that are disabled */
class VoidToken implements Token {
  static final VoidToken ONLY = new VoidToken();
  private VoidToken() {}
}

/** The class that represents Jam variables with depth count; each instance refers to 
 *  the corresponding unique variable within scope.  There may be duplicate DepthVariables.
 */
class DepthVariable extends Variable {
  private Variable var;  /* corresponding raw Variable */
  private int depth;
  private Variable newVar; /* corresponding Variable with depth suffix */
  
  DepthVariable(Variable v, int d) { 
    super(v); 
    var = v; 
    depth = d; 
    newVar = new Variable(name() + ":" + d);  /* not interned anywhere; duplicates are possible in other scopes. */
  }
  
  public Variable var() { return var; }
  public Variable rename() { return newVar; }
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

class Bind implements Token {
  public String toString() { return ":="; }
  private Bind() { }
  public static final Bind ONLY = new Bind();
}

/** Jam lexer class.              
  * Given a Lexer object, the next token in that input stream being processed by the Lexer is returned by static method
  * readToken(); it throws a ParseException (an extension of IOException) if it encounters a syntax error.  Calling 
  * readToken() advances the cursor in the input stream to the next token.  The static method peek() in the Lexer class 
  * has the same behavior as readToken() except for the fact that it does not advance the cursor.
  */
class Lexer extends StreamTokenizer {
    
  /** Static Fields **/
  
  /* Short names for StreamTokenizer codes */
  
  public static final int WORD = StreamTokenizer.TT_WORD; 
  public static final int NUMBER = StreamTokenizer.TT_NUMBER; 
  public static final int EOF = StreamTokenizer.TT_EOF; 
  public static final int EOL = StreamTokenizer.TT_EOL;
  
  /** operator Tokens:
  
     <unop>  ::= <sign> | ~   | ! 
     <binop> ::= <sign> | "*" | / | = | != | < | > | <= | >= | & | "|" | <- 
     <sign>  ::= "+" | -
  
    Note: there is no class distinction between <unop> and <binop> at lexical level because of ambiguity; <sign> 
    belongs to both. */
  
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
  
  /** Keywords **/

  public static final KeyWord IF     = new KeyWord("if");
  public static final KeyWord THEN   = new KeyWord("then");
  public static final KeyWord ELSE   = new KeyWord("else");
  public static final KeyWord LET    = new KeyWord("let");
  public static final KeyWord LETREC = new KeyWord("letrec");   // Used to support letrec extension in Assignment
  public static final KeyWord IN     = new KeyWord("in");
  public static final KeyWord MAP    = new KeyWord("map");
  public static final KeyWord TO     = new KeyWord("to");
  public static final KeyWord LETCC   = new KeyWord("letcc");
  
  /** Fields **/ 
  
  /** The Reader from which this lexer reads. */
  public final Reader rdr;
  
  /** The wordtable for classifying words (identifiers/operators) in token stream */
  public HashMap<String,Token>  wordTable = new HashMap<String,Token>();
  
  /* Lexer peek cannot be implemented using StreamTokenizer pushBack because some Jam Tokens are composed of two 
   * StreamTokenizer tokens. */
  
  Token buffer;  // saves token for peek() operation
  
  /* Constructors */
  
  /** Primary constructor that takes a specified input stream; all other constructors instantiate this one. */
  Lexer(Reader inputStream) {
    super(new BufferedReader(inputStream));
    rdr = inputStream;
    initLexer();
  }

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
  
  public void flush() throws IOException {
    eolIsSignificant(true);
    while (nextToken() != EOL) ; // eat tokens until EOL
    eolIsSignificant(false);
  }
  
  public Token peek() { 
    if (buffer == null) buffer = readToken();
    return buffer;
  }
  
  /** Find variable with name sval; create one if none exists */
  public Token intern(String sval) {
    Token regToken = (Token) wordTable.get(sval);
    if (regToken == null) { // sval must be new variable name
      Variable newVar = new Variable(sval);
      wordTable.put(sval,newVar);
      return newVar;
    }
    return regToken;
  }
  
  /** Wrapper method for nextToken that converts IOExceptions thrown by nextToken to ParseExceptions. */
  private int getToken() {
    try {
      int tokenType = nextToken();
      return tokenType;
    } catch(IOException e) {
      throw new ParseException("IOException " + e + "thrown by nextToken()");
    }
  }
  
   /* Uses getToken() to read next token and constructs Token object representing that token. */
  public Token readToken() {
    
    /* NOTE: the token representations for all Token classes except IntConstant are unique; a HashMap is used to avoid 
     * duplication. Hence, == can safely be used to compare all Tokens except IntConstants for equality
     */
    
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
        throw new ParseException("The number " + nval + " is not a 32 bit integer");
        
      case WORD:
        Token regToken = wordTable.get(sval);
        if (regToken == null) { // sval must be new variable name
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
      case '{': return LeftBrace.ONLY;
      case '}': return RightBrace.ONLY;
      case ',': return Comma.ONLY;
      case ';': return SemiColon.ONLY;
      
      case '+': return PLUS;
      case '-': return MINUS;
      case '*': return TIMES;  
      case '/': return DIVIDE;
      
      case '~': return NOT;  
      case '=': return EQUALS;  
      case '<': 
        tokenType = getToken();
        if (tokenType == '=') return LESS_THAN_EQUALS;  
        if (tokenType == '-') return GETS;  
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
        pushBack();
        return BANG;  
      case '&': return AND;  
      case '|': return OR;  
      case ':': {
        tokenType = getToken();
        if (tokenType == '=') return Bind.ONLY;  
        pushBack();
        throw new ParseException("`:' is not a legal token");
      }
      default:  
        throw new 
        ParseException("`" + ((char) tokenType) + "' is not a legal token");
    }
  }
  
  private void initWordTable() {
    /* Initialize wordTable */
    
    /* Constants
       <null>  ::= null
       <bool>  ::= true | false
     */
    
    wordTable.put("null",  NullConstant.ONLY);
    wordTable.put("true",  BoolConstant.TRUE);
    wordTable.put("false", BoolConstant.FALSE);
    
    /* Primitive functions + ref unary operator:    
     * <prim>  ::= number? | function? | list? | null? | cons? | ref? | arity | cons | first | rest  
     * Note: ref is not <prim>; it is a unary operator */
    
    wordTable.put("number?",   NumberPPrim.ONLY);
    wordTable.put("function?", FunctionPPrim.ONLY);
    wordTable.put("list?",     ListPPrim.ONLY);
    wordTable.put("null?",     NullPPrim.ONLY);
    wordTable.put("cons?",     ConsPPrim.ONLY);
    wordTable.put("ref?",      RefPPrim.ONLY);   // Supports addition of ref cells to Jam.
    wordTable.put("arity",     ArityPrim.ONLY);
    wordTable.put("cons",      ConsPrim.ONLY);
    wordTable.put("first",     FirstPrim.ONLY);
    wordTable.put("rest",      RestPrim.ONLY);
    
    /* "ref' is the only unary operator that is an identifier */
    wordTable.put("ref",       REF);             // Supports addition of ref cells to Jam.
    
    /* keywords: if then else let letrec in map to letcc*/
    wordTable.put("if",   IF);
    wordTable.put("then", THEN);
    wordTable.put("else", ELSE);
    wordTable.put("let",  LET);
    wordTable.put("letrec", LETREC);             // Supports addition of separate letrec to Jam
    wordTable.put("in",   IN);
    wordTable.put("map",  MAP);
    wordTable.put("to",   TO);
    wordTable.put("letcc", LETCC);               // Supports addition of letcc to Jam    
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

class Test {
  static String s1 = "let f :=  map n to if n = 0 then 1 else n * f(n - 1); in f(3)";
  static StringReader in1 = new StringReader(s1);
}

class Parser {
  
  private Lexer in;
  
  /** Parsed program */
  SymAST prog; 
  
  /** Checked and renamed program */
  SymAST checkProg; 
  
  SDAST statCheckProg;
  
   /** CPS'ed program */
  SymAST cpsProg; 
  
   /** CPS'ed program */
  SDAST statCpsProg; 
  
  private HashMap<PrimFun, SymAST> primTable = new HashMap<PrimFun, SymAST> ();
  private HashMap<Op, SymAST> opTable = new HashMap<Op, SymAST> ();
  
  private Token ifKey;
  private Token thenKey;
  private Token elseKey;
  private Token letKey;
  private Token letrecKey;
  private Token inKey;
  private Token mapKey;
  private Token toKey;
  private Token assignKey;
  private Token letccKey;
  
  /** Counter for generated variable names in CPS conversion */
  private int genVarCtr;
  
  /** Fixed variable names used in CPS conversion */
  private Variable x; 
  private Variable y;
  private Variable k;
  
  /** identity fn and visitors for CPS conversion */
  private SymAST identity;
  private SymASTVisitor<SymAST> convertToCPS;
  SymASTVisitor<Boolean> isSimple;
  SymASTVisitor<SymAST> reshape;
  SConverter sConverter;
  
  private PrimFun arityFun;
  private BinOp minusOp;
  private IntConstant one;
  
  Parser(Lexer i) {
    in = i;
    initParser();
  }
  
  Parser(Reader inputStream) {
    this(new Lexer(inputStream));
  }
  
  Parser(String fileName) throws IOException {
    this(new FileReader(fileName));
  }
  
  Lexer lexer() { return in; }
  
  private void initParser() {
    ifKey     = in.wordTable.get("if");
    thenKey   = in.wordTable.get("then");
    elseKey   = in.wordTable.get("else");
    letKey    = in.wordTable.get("let");
    letrecKey = in.wordTable.get("letrec");
    inKey     = in.wordTable.get("in");
    mapKey    = in.wordTable.get("map");
    toKey     = in.wordTable.get("to");
    assignKey = in.wordTable.get(":=");
    letccKey  = in.wordTable.get("letcc");
    
    arityFun  = ArityPrim.ONLY;
    minusOp   = BinOpMinus.ONLY;
    
    one       = new IntConstant(1);
    
    
    
    /* Counter for fresh variable generation. */
    genVarCtr = -1;  // incremented prior to use; first value will be zero
    
    /* Fixed variable names used in CPS transformation and identity, */
    x = (Variable) in.intern("x");
    y = (Variable) in.intern("y");
    k = (Variable) in.intern("k");
    
    /* Insert only binary primitive in primTable. */
    insertBinPrim(ConsPrim.ONLY);                              
                                                               
    /* CPS translation of arity is a special case, because CPS affects arity! */
    SymAST arityExp = new BinOpApp(minusOp, new App(arityFun, new SymAST[] {x}), one);
    primTable.put(arityFun, new Map(new Variable[]{x,k}, new App(k, new SymAST[] {arityExp})));
    
    /* Symbolic abstract syntax for identity function */
    identity = new Map(new Variable[]{x},x);
    
    insertUnaryPrim(NumberPPrim.ONLY);
    insertUnaryPrim(FunctionPPrim.ONLY);
    insertUnaryPrim(ListPPrim.ONLY);
    insertUnaryPrim(NullPPrim.ONLY);
    insertUnaryPrim(ConsPPrim.ONLY);
    insertUnaryPrim(RefPPrim.ONLY);
    insertUnaryPrim(FirstPrim.ONLY);
    insertUnaryPrim(RestPrim.ONLY);
    
    insertBinOp(BinOpPlus.ONLY);
    insertBinOp(BinOpMinus.ONLY);
    insertBinOp(OpTimes.ONLY);
    insertBinOp(OpDivide.ONLY);
    insertBinOp(OpEquals.ONLY);
    insertBinOp(OpNotEquals.ONLY);
    insertBinOp(OpLessThan.ONLY);
    insertBinOp(OpGreaterThan.ONLY);
    insertBinOp(OpLessThanEquals.ONLY);
    insertBinOp(OpGreaterThanEquals.ONLY);
    insertBinOp(OpGets.ONLY);
    
    insertUnOp(OpRef.ONLY);
    insertUnOp(OpBang.ONLY);
    insertUnOp(OpTilde.ONLY);
    insertUnOp(UnOpPlus.ONLY);
    insertUnOp(UnOpMinus.ONLY);
//    System.err.println(opTable.get(UnOpMinus.ONLY));
    
    /* Visitors that perform syntactic processing. */
    convertToCPS = new ConvertToCPS(identity);
    reshape = new Reshape();
    isSimple = new IsSimple();
    sConverter = new SConverter();
  }
  
  private Variable genVariable() {
    /* Assert that generated name is fresh if all input read by parser is legal Jam source text */
    genVarCtr++;
    return (Variable) in.intern(":" + genVarCtr);
  }
  
  /* Parses and checks embedded programin and returns the SymAST for the program. */
  public SymAST checkProg() {
    if (checkProg != null) return checkProg;
    SymAST prog = parseProg();
    checkProg = prog.accept(CheckVisitor.INITIAL);   // aborts on an error by throwing an exception
    return checkProg;
  }
  
  /* Parses embedded program to a SYMAST, checks it, converts it to CPS, and returns result. */
  public SymAST cpsProg() {
    if (cpsProg != null) return cpsProg;
    checkProg();
    cpsProg = convertToCPS(checkProg, identity);
    return cpsProg;
  }
  
  /* Parses and checks the input program embedded in the Parser and returns the corresponding SD representation. */
  public SDAST statCheckProg() {
    if(statCheckProg != null) return statCheckProg;
    SymAST prog = checkProg();
    statCheckProg = sConverter.convert(prog);
//    System.err.println(statCheckProg);
    return statCheckProg;
  }
 
  /* Parses embedded program to a SYMAST, checks it, converts it to CPS, converts it to SD form, and returns it. */
  public SDAST statCpsProg() {
    if (statCpsProg != null) return statCpsProg;
    cpsProg();
    statCpsProg = sConverter.convert(cpsProg);
//    System.err.println(statCpsProg);
    return statCpsProg;
  }

  /* Parses the input program into a SymAST */
  public SymAST parseProg() {
    if (prog != null) return prog;
    prog = parseExp();
    Token t = in.readToken();
    if (t == null) return prog;
    else throw new ParseException("Legal program \n" + prog + "\n followed by extra token " + t);
  }
  
  /* Parses the next Jam expression in the input stream (assuming no tokne in that expression has yet been read) */
  private SymAST parseExp() {
    
    Token token = in.readToken();
    
    /* <exp> :: = if <exp> then <exp> else <exp>
                | let <prop-def-list> in <exp>
                | map <id-list> to <exp>
                | <term> { <biop> <term> }*  // (left associatively!)
    */
    
    if (token == ifKey) return parseIf();
    if (token == letrecKey) return parseLetRec();
    if (token == letccKey) return parseLetcc();
    if (token == letKey) return parseLet();
    if (token == mapKey) return parseMap();
    
    if (token == LeftBrace.ONLY) {
      SymAST[] exps = parseExps(SemiColon.ONLY,RightBrace.ONLY);   // including closing brace
      if (exps.length == 0) throw new ParseException("Illegal empty block");
      return new Block(exps);
    }
    
    SymAST exp = parseTerm(token);
    
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
  
  private SymAST parseTerm(Token token) {
    
    /* <term>     ::= { <unop> } <term> | <constant> | <factor> {( <exp-list> )} 
       <constant> ::= <null> | <int> | <bool>
     */
    
    if (token instanceof OpToken) {
      OpToken op = (OpToken) token;
      if (! op.isUnOp()) error(op,"unary operator");
      return new UnOpApp(op.toUnOp(), parseTerm(in.readToken()));
    }
    
    if (token instanceof Constant) return (Constant) token;
    
    SymAST factor = parseFactor(token);
    
    Token next = in.peek();
    if (next == LeftParen.ONLY) {
      in.readToken();  // remove next from input stream
      SymAST[] exps = parseArgs();  // including closing paren
      return new App(factor,exps);
    }
    return factor;
  }
  
  private SymAST parseFactor(Token token) {
    
    // <factor>   ::= <prim> | <variable> | ( <exp> )
    
    if (token == LeftParen.ONLY) {
      SymAST exp = parseExp();
      token = in.readToken();
      if (token != RightParen.ONLY) error(token,"`)'");
      return exp;
    }
    
    if (! (token instanceof PrimFun) && ! (token instanceof Variable))
      error(token,"constant, primitive, variable, or `('");
    
    // Term\Constant = Variable or PrimFun       
    return (SymAST) token;
  }      
  
  private SymAST parseIf() {
    /* Parses 'if <exp> then <exp> else <exp>' given that 'if' has already been read. */
    
    SymAST test = parseExp();
    Token key1 = in.readToken();
    if (key1 != thenKey) error(key1,"`then'");
    SymAST conseq = parseExp();
    Token key2 = in.readToken();
    if (key2 != elseKey) error(key2,"`else'");
    SymAST alt = parseExp();
    return new If(test,conseq,alt);
  }
  
  private SymAST parseLet() {
    /* Parses 'let <prop-def-list> in <exp>' given that 'let' has already been read */
    
    Def[] defs = parseDefs(false);  // consumes `in'; false means rhs may be non Map
    SymAST body = parseExp();
    return new Let(defs,body);
  }
  
  private SymAST parseLetRec() {
    /* Parses 'letrec <prop-def-list> in <exp>' given that `letrec' has already been read.. */
    
    Def[] defs = parseDefs(true); // consumes `in'; true means each rhs must be a Map
    SymAST body = parseExp();
    return new LetRec(defs,body);
  }
  
  private SymAST parseLetcc() {
    /* Parses 'letcc <var> in <exp>' given that 'letcc' has already been read. */ 

    Token var = in.readToken();
    if (! (var instanceof Variable)) error(var,"variable");
    Token t = in.readToken();
    if (t != inKey) error(t,"`in'");
    SymAST body = parseExp();
    return new Letcc((Variable) var, body);
  }

  private SymAST parseMap() {
    /* parses 'map <id-list> to <exp>' given that `map' has already been read. */ 
    
    Variable[] vars = parseVars(); // consumes the delimiter `to'
    SymAST body = parseExp();
    return new Map(vars, body);
  }
  
  private SymAST[] parseExps(Token separator, Token delim) {
    /* Parses '<exp-list> <delim>' where 
         <exp-list>      ::= <empty> | <prop-exp-list>
         <empty> ::=  
         <prop-exp-list> ::= <exp> | <exp> <separator> <prop-exp-list>
     */
    
    LinkedList<SymAST> exps = new LinkedList<SymAST>();
    Token next = in.peek();
    
    if (next == delim) {
      in.readToken(); // consume RightParen
      return new SymAST[0];
    }
    
    /* next is still at front of input stream */
    
    do {
      SymAST exp = parseExp();
      exps.addLast(exp);
      next = in.readToken();
    } while (next == separator);
    
    if (next != delim) error(next,"`,' or `)'");
    return exps.toArray(new SymAST[0]);
  }
  
  private SymAST[] parseArgs() { return parseExps(Comma.ONLY,RightParen.ONLY); }
  
  private Variable[] parseVars() {
    
    /* Parses <id-list> where
         <id-list>       ::= <empty> | <prop-id-list>
         <prop-id-list>  ::= <id> | <id> , <id-list> 
    
       NOTE: consumes `to' following <id-list> */
    
    LinkedList<Variable> vars = new LinkedList<Variable>();
    Token t = in.readToken();
    if (t == toKey) return new Variable[0];
    
    do {
      if (! (t instanceof Variable)) error(t,"variable");
      vars.addLast((Variable)t);
      t = in.readToken();
      if (t == toKey) break; 
      if (t != Comma.ONLY) error(t,"`to' or `,'");
      /* Comma found, read next variable */
      t = in.readToken();
    } while (true);
    return (Variable[]) vars.toArray(new Variable[0]);
  }
  
  private Def[] parseDefs(boolean forceMap) {
    /* Parses  `<prop-def-list> in' where
        <prop-def-list> ::= <def> | <def> <def-list> 
    
       NOTE: consumes `in' following <prop-def-list>
     */
    
    LinkedList<Def> defs = new LinkedList<Def>();
    Token t = in.readToken();
    
    do {
      Def d = parseDef(t);        
      if (forceMap && (! (d.rhs() instanceof Map)))
        throw new ParseException("right hand side of definition `" + d
                                   + "' is not a map expression");
      defs.addLast(d);
      t = in.readToken();
    } while (t != inKey);
    
    return (Def[]) defs.toArray(new Def[0]);
  }
  
  private Def parseDef(Token var) {
    /* Parses <id> := <exp> ;
       which is <def> given that first token var has been read.
     */
    
    if (! (var instanceof Variable)) error(var,"variable");
    
    Token bind = in.readToken();
    if (bind != Bind.ONLY) error (bind,"`:='");
    
    SymAST exp = parseExp();
    
    Token semi = in.readToken();
    if (semi != SemiColon.ONLY) error(semi,"`;'");
    return new Def((Variable) var, exp);
  }
  
  private SymAST error(Token found, String expected) {
    for (int i = 0; i < 10; i++) {
      System.out.println(in.readToken());
    }
    throw new ParseException("Token `" + found + "' appears where " + expected + " was expected");
  }
  
  /* Parser members suppporting CPS transformation;
   * x,y,k are private members bound to the Variables with names "x","y","k". */
  
  private void insertUnaryPrim(PrimFun f) {
    primTable.put(f, new Map(new Variable[]{x,k}, 
                             new App(k, new SymAST[] { new App(f, new SymAST[]{x}) })));
  }
  private void insertBinPrim(PrimFun f) {
    primTable.put(f, 
      new Map(new Variable[]{x,y,k}, 
              new App(k, new SymAST[] { new App(f,new SymAST[]{x,y}) })));
  }
  private void insertUnOp(UnOp f) {
    opTable.put(f, 
      new Map(new Variable[]{x,k}, new App(k, 
              new SymAST[] { new UnOpApp(f,x) })));
  }

  private void insertBinOp(BinOp f) {
    opTable.put(f, 
      new Map(new Variable[]{x,y,k}, new App(k, 
              new SymAST[] { new BinOpApp(f,x,y) })));
  }


  class Reshape implements SymASTVisitor<SymAST> {
    
    Reshape() {}
    
//    private SymAST forDefault(AST host)   { 
//      throw new CPSException("host " + host + " not supported by ReShape visitor");
//    }
    public SymAST forIntConstant(IntConstant host)   { return host; };
    public SymAST forBoolConstant(BoolConstant host) { return host; };
    public SymAST forNullConstant(NullConstant host) { return host; };
    public SymAST forSymVariable(Variable host)      { return host; }
    public SymAST forPrimFun(PrimFun host)           { return primTable.get(host); }
    public SymAST forUnOpApp(UnOpApp u) { 
      SymAST arg = (SymAST) u.arg();
      return new UnOpApp(u.rator(), arg.accept(this));
    }
    public SymAST forBinOpApp(BinOpApp b) {
      SymAST arg1 = (SymAST) b.arg1();
      SymAST arg2 = (SymAST) b.arg2();
      return new BinOpApp(b.rator(), arg1.accept(this), arg2.accept(this));
    }
    
    public SymAST forApp(App a) {
      if (! (a.rator() instanceof PrimFun))
        throw new ParseException("non primitive application `" + a + "' passed to Reshape");
      SymAST[] args = (SymAST[]) a.args();
      int n = args.length;
      SymAST[] newArgs = new SymAST[n];
      for (int i = 0; i < n; i++) newArgs[i] = args[i].accept(this);
      App app = new App(a.rator(), newArgs);
      if (a.rator() == ArityPrim.ONLY) {
        return new BinOpApp(BinOpMinus.ONLY, app, new IntConstant(1));
      }
      else return app;
    }
    
    public SymAST forMap(Map m) { 
      
      int n = m.vars().length;
      Variable[] newVars = new Variable[n+1];
      Variable newVar = genVariable();
      for (int i = 0; i < n; i++) newVars[i] = m.vars()[i];
      newVars[n] = newVar;
      return new Map(newVars, convertToCPS(m.body(), newVar));
    }
    
    public SymAST forIf(If i) { 
      SymAST test = (SymAST) i.test();
      SymAST conseq = (SymAST) i.conseq();
      SymAST alt = (SymAST) i.alt();
      return new If(test.accept(this), conseq.accept(this), alt.accept(this));
    }
    
    public SymAST forLet(Let l) {
      int n = l.defs().length;
      Def[] newDefs = new Def[n];
      
      for (int i = 0; i < n; i++) {
        Def oldDef = l.defs()[i];
        newDefs[i] = new Def(oldDef.lhs(), oldDef.rhs().accept(this));
      }
      return new Let(newDefs, l.body().accept(this));
    }
    
    /* Note the repeated code in forLet and forLetRe; should refactor. */
    
    public SymAST forLetRec(LetRec l) {
      int n = l.defs().length;
      Def[] newDefs = new Def[n];
      
      for (int i = 0; i < n; i++) {
        Def oldDef = l.defs()[i];
        newDefs[i] = new Def(oldDef.lhs(), oldDef.rhs().accept(this));
      }
      return new LetRec(newDefs, l.body().accept(this));
    }
    public SymAST forLetcc(Letcc host) { 
      throw new CPSException("Attempt to reshape the letcc expression " + host);
    }
    public SymAST forBlock(Block b) {
      int n = b.exps().length;
      SymAST[] exps = (SymAST[]) b.exps();
      SymAST[] newExps = new SymAST[n];
      for (int i = 0; i < n; i++) newExps[i] = exps[i].accept(this);
      
      return new Block(newExps);
    }
  }
  
  static Boolean TRUE = Boolean.TRUE;
  static Boolean FALSE = Boolean.FALSE;
  
  /** Visitor class representing an operation that determines if an expression only
   *  involves local allocation; TRUE means it requires no external allocation 
   */
  class IsSimple implements SymASTVisitor<Boolean> {
    
//    public Boolean forDefault(AST host) { 
//      throw new CPSException("host " + host + " not supported by IsSimple visitor"); 
//    }
    
    public Boolean forIntConstant(IntConstant i)   { return TRUE; }
    public Boolean forNullConstant(NullConstant n) { return TRUE; }
    public Boolean forBoolConstant(BoolConstant b) { return TRUE; }
    public Boolean forSymVariable(Variable v) { return TRUE; }
    public Boolean forPrimFun(PrimFun f)  { return TRUE; }  
 
    public Boolean forUnOpApp(UnOpApp u) { 
      SymAST arg = (SymAST) u.arg();
      return arg.accept(this); 
    }  

    public Boolean forBinOpApp(BinOpApp b) {
      SymAST arg1 = (SymAST) b.arg1();
      SymAST arg2 = (SymAST) b.arg2();
      if ((arg1.accept(this) == TRUE) && (arg2.accept(this) == TRUE)) return TRUE;
      return FALSE;
    } 
 
    public Boolean forApp(App a) { 
      if (! (a.rator() instanceof PrimFun)) return FALSE;
      SymAST[] args = (SymAST[]) a.args();
      int n = args.length;
      for (int i = 0; i < n; i++) {
        if (args[i].accept(this) == FALSE) return FALSE;
      }
      return TRUE; 
    }
    
    public Boolean forMap(Map m) { return TRUE; }
    
    public Boolean forIf(If i) {
      SymAST test = (SymAST) i.test();
      SymAST conseq = (SymAST) i.conseq();
      SymAST alt = (SymAST) i.alt();
      if ((test.accept(this) == TRUE) && (conseq.accept(this) == TRUE) && (alt.accept(this) == TRUE)) 
        return TRUE;
      else return FALSE;
    }
    
    public Boolean forLet(Let l) { 
      Def[] defs = l.defs();
      int n = defs.length;
      
      for (int i = 0; i < n; i++) {
        SymAST rhs = (SymAST) defs[i].rhs();
        if (rhs.accept(this) == FALSE) return FALSE;
      }
      if (((SymAST) l.body()).accept(this) == FALSE) return FALSE;
      return TRUE;
    } 
    
    public Boolean forLetRec(LetRec l) {
      Def[] defs = l.defs();
      int n = defs.length;
      
      for (int i = 0; i < n; i++) {
        SymAST rhs = (SymAST) defs[i].rhs();
        if (rhs.accept(this) == FALSE) return FALSE;
      }
      if (((SymAST) l.body()).accept(this) == FALSE) return FALSE;
      return TRUE;
    }
    
    public Boolean forLetcc(Letcc l) { return FALSE; }
    
    public Boolean forBlock(Block b) {
      SymAST[] exps = (SymAST[]) b.exps();
      int n = exps.length;
      for (int i = 0; i < n; i++) {
        if (exps[i].accept(this) == FALSE) return FALSE;
      }
      return TRUE;
    }
  }
  
  /** Reshapes the arguments and adds the continuation as the final argument */
  private SymAST[] reshape(SymAST[] args, SymAST cont) {
    int n = args.length;
    SymAST[] newArgs = new SymAST[n+1];
    for (int i = 0; i < n; i++) 
      newArgs[i] = args[i].accept(reshape);
    newArgs[n] = cont;
    return newArgs;
  }
  
  /* Convert exp,cont to correponding CPS'ed program */ 
  public SymAST convertToCPS(SymAST exp, SymAST cont) {
    if (exp.accept(isSimple) == TRUE) {
      return new App(cont, new SymAST[] {exp.accept(reshape)});
    }
    return exp.accept(new ConvertToCPS(cont));
  }
      
  /** Converts a non-simple expression to CPS form */
  class ConvertToCPS implements SymASTVisitor<SymAST> {
    
    SymAST cont;

    // Counter to assign variable names as ":i".
    int varcount = 0;

    ConvertToCPS(SymAST c) { cont = c; }
    
    /* No longer used. */
//    private SymAST internalError(SymAST v) {
//      throw new CPSException("This ConvertToCPS visitor should never reach this form of SymAST: " + v);
//    }
    
//    public SymAST forDefault(AST host)   { 
//      throw new CPSException("host " + host + " not supported by CPS visitor");
//    }
    
    /* None of the following block of methods should ever be executed if the host is not simple. */
    public SymAST forIntConstant(IntConstant i) {  return new App(cont, new SymAST[] {i}); }
    public SymAST forNullConstant(NullConstant n) { return new App(cont, new SymAST[] {n}); }
    public SymAST forBoolConstant(BoolConstant b) { return new App(cont, new SymAST[] {b}); }
    public SymAST forSymVariable(Variable v) { return new App(cont, new SymAST[] {v}); }
    public SymAST forPrimFun(PrimFun f) { 
      return new App(cont, new SymAST[] {f.accept(reshape)}); 
    }
    public SymAST forMap(Map m) { 
      return new App(cont, new SymAST[] {m.accept(reshape)});
    }
    
    public SymAST forUnOpApp(UnOpApp u) {
      // We think that if it gets to here, then we already know that it isn't simple.
      // We know this is for SymAST so I cast to SymAST.
      System.out.println("Gets to UnOpApp: " + u);
      // Check that this is what you want to do.
      if (u.accept(isSimple) == TRUE) {
        return new App(cont, new SymAST[] {u.accept(reshape)});
      }
      UnOp s = u.rator();
      SymAST e1 = (SymAST) u.arg();
      Variable v1 = new Variable(":" + varcount);
      Def def = new Def(v1, e1);
      varcount++;
      Def[] arr = new Def[] {def};
      Let let = new Let(arr, new UnOpApp(s, v1));
      // And I pass this on to let as per case 5.
      return let.accept(this);
    }
    public SymAST forBinOpApp(BinOpApp b) {
      // Check that this is what you want to do.
      if (b.accept(isSimple) == TRUE) {
        return new App(cont, new SymAST[] {b.accept(reshape)});
      }
      Def def1 = new Def(new Variable(":" + varcount), (SymAST) b.arg1());
      varcount++;
      Def def2 = new Def(new Variable(":" + varcount), (SymAST) b.arg2());
      varcount++;
      Def[] arr = new Def[] {def1, def2};
      Let let = new Let(arr, new BinOpApp(b.rator(), def1.lhs(), def2.lhs()));
      return let.accept(this);
    }

    private boolean allArgsSimple(App a) {
      for (int i = 0; i < a.args().length; i++) {
        if (!(((SymAST) a.args()[i]).accept(isSimple))) {
          return false;
        }
      }
      return true;
    }

    private SymAST case4(App a) {
      // All of a's arg + 1 for k
      SymAST[] newArgs = new SymAST[a.args().length + 1];
      //
      SymAST newRator = ((SymAST) a.rator()).accept(reshape);
      for (int i = 0; i < a.args().length; i++) {
        newArgs[i] = ((SymAST) a.args()[i]).accept(reshape);
      }
      newArgs[a.args().length] = cont;
      return new App(newRator, newArgs);
    }

    public SymAST forApp(App a) {
      // Check that this is what you want to do.
      if (a.accept(isSimple) == TRUE) {
        return new App(cont, new SymAST[] {a.accept(reshape)});
      }



      Def[] arr = new Def[a.args().length];
      Variable[] varArr = new Variable[a.args().length];
      for (int i = 0; i < a.args().length; i++) {
        varArr[i] = new Variable(":" + varcount);
        arr[i] = new Def(varArr[i], (SymAST) a.args()[i]);
        varcount++;
      }
      Let let = new Let(arr, new App(a.rator(), varArr));
      return let.accept(this);
    }
    public SymAST forIf(If i) { /* . . . */ return null; /* This is a STUB. */ }
    public SymAST forLet(Let l) {
      System.out.println("Gets to Let");
      if (l.accept(isSimple) == TRUE) {
        return new App(cont, new SymAST[] {l.accept(reshape)});
      }
      Def firstDef = l.defs()[0];
      if (firstDef.rhs().accept(isSimple)) {
        // Case 11
        if (l.defs().length > 1) {
          System.out.println("Gets to case 11");
          return null;
        // Case 10
        } else {
          System.out.println("Gets to case 10");
          System.out.println("Let l: " + l);
          Def def = new Def(firstDef.lhs(), firstDef.rhs().accept(reshape));
          System.out.println("Def in let is: "+ def);
          System.out.println("Body in let is: "+ l.body());
          Def[] arr = new Def[] {def};
          return new Let(arr, l.body().accept(this));
        }
      } else {
        // Case 13
        if (l.defs().length > 1) {
          System.out.println("Gets to case 13");
          return null;
        // Case 12
        } else {
          System.out.println("Gets to case 12");
          System.out.println("Body in case 12: " + l.body());

          Map map = new Map(new Variable[] {firstDef.lhs()}, l.body().accept(this));
          System.out.println("Gets past creating the map: " + map);
          System.out.println("firstDef rhs: " + firstDef.rhs());
          return firstDef.rhs().accept(new ConvertToCPS(map));
//          return null;
        }
      }

//      System.out.println("Gets to here");

//      return null; /* This is a STUB. */
    }
    /* Assumes body is non-simple. Otherwise 'this' would be simple since RHSs are all maps. */
    public SymAST forLetRec(LetRec l) { /* . . . */ return null; /* This is a STUB. */ } 
    public SymAST forLetcc(Letcc l) { /* . . . */ return null; /* This is a STUB. */ }
    public SymAST forBlock(Block b) { /* . . . */ return null; /* This is a STUB. */ }
  }

  public static void main(String[] args) throws IOException {
    /* Check for a legal argument list. */ 
    if (args.length == 0) {
      System.out.println("Usage: java Parser <filename>");
      return;
    }
    Parser p = new Parser(args[0]);
    System.out.println("Parsed and checked program is: " + p.checkProg());
    System.out.println("CPSed program is: " + p.cpsProg());
  }
}

/** A visitor class  that performs syntax checking and unshadowing. It returns asyntax tree (with new variable names)
  * unless there is a syntax error. On a syntax error, throws a SyntaxException. */
class CheckVisitor implements SymASTVisitor<SymAST> {
  
  private static final Empty<DepthVariable> emptyVars = new Empty<DepthVariable>();
  
  /** Symbol table used to detect free variables */
  private PureList<DepthVariable> env;
  
  /** Lexical depth in symbol table */
  private int depth;
  
  public static final CheckVisitor INITIAL = new CheckVisitor(emptyVars,0);
  
  /** Initializes the env and depth fields of a new Check Visitor object */
  private CheckVisitor(PureList<DepthVariable> e, int d) { env = e; depth = d; }
  
  /** Constructs a new Check Visitor object with specified environment and incremented depth */
  CheckVisitor newVisitor(PureList<DepthVariable> env) { return new CheckVisitor(env, depth+1);  }
  
//  public SymAST forDefault(AST host)   { 
//    throw new CPSException("host " + host + " not supported by Check visitor");
//  }
  public SymAST forIntConstant(IntConstant host) { return host; }
  public SymAST forBoolConstant(BoolConstant host) { return host; }
  public SymAST forNullConstant(NullConstant host) { return host; }
  
  public SymAST forSymVariable(Variable host) { 
    DepthVariable match = env.accept(new LookupVisitor<DepthVariable>(host));
    if (match == null) throw new 
      SyntaxException("variable " + host + " is unbound");
    return match.rename();
  }
  
  public SymAST forPrimFun(PrimFun host) { return host; }
  
  public SymAST forUnOpApp(UnOpApp host) {
    SymAST arg = (SymAST) host.arg();
    SymAST newArg = arg.accept(this);  // throws an exception on error
    return new UnOpApp(host.rator(), newArg);
  }
  
  public SymAST forBinOpApp(BinOpApp host) {
    SymAST arg1 = (SymAST) host.arg1();
    SymAST arg2 = (SymAST) host.arg2();
    BinOp rator = host.rator();
    SymAST newArg1 = arg1.accept(this);  // throws an exception on error
    SymAST newArg2 = arg2.accept(this);  // throws an exception on error
    if (rator == OpAnd.ONLY) {  // second argument must be Boolean!
      SymAST conseq = new App(AsBoolPrim.ONLY, new SymAST[]{newArg2}); // performs run-time check
      return new If(newArg1, conseq, BoolConstant.FALSE);
    }
    if (rator == OpOr.ONLY) {  // second argument must be Boolean!
      SymAST alt = new App(AsBoolPrim.ONLY, new SymAST[]{newArg2}); // performs run-time check
      return new If(newArg1, BoolConstant.TRUE, alt);
    }
    return new BinOpApp(rator, newArg1, newArg2);
  }
  
  public SymAST forApp(App a) {
    SymAST rator = (SymAST) a.rator();
    SymAST newRator = rator.accept(this); // throws an exception on error
    SymAST[] args = (SymAST[]) a.args();
    int n = args.length;
    SymAST[] newArgs = new SymAST[n];
    for (int i = 0; i < n; i++) 
      newArgs[i] = args[i].accept(this); // throws an exception on error
    return new App(newRator, newArgs);
  }
  
  /* Checka for duplicates in Map vars, increment depth, construct new visitor for Map body, and translate body. */
  public SymAST forMap(Map m) {
    Variable[] vars = m.vars();
    PureList<Variable> varList = PureListClass.arrayToList(vars);
//    System.err.println("variable list is: " + varList);
    varList.accept(AnyDuplicatesVisitor.ONLY);  // throws exception on an error
    
    int n = vars.length;
    
    PureList<DepthVariable> newEnv = env;
    Variable[] newVars = new Variable[n];
//    System.out.println("map:" + m + " depth:" + depth);
    for (int i = n-1; i >= 0; i--) {
      DepthVariable newDepthVar = new DepthVariable(vars[i], depth+1);
      newEnv = newEnv.cons(newDepthVar);
      newVars[i] = newDepthVar.rename();
    }    
    
    SymAST newBody = m.body().accept(newVisitor(newEnv));  // increments depth
    return new Map(newVars,newBody);
  }
  
  public SymAST forIf(If i) {
    SymAST test = (SymAST) i.test();
    SymAST conseq = (SymAST) i.conseq();
    SymAST alt = (SymAST) i.alt();
      
    SymAST newTest = test.accept(this);
    SymAST newConseq = conseq.accept(this);
    SymAST newAlt = alt.accept(this);
    return new If(newTest, newConseq, newAlt);
  }
  
  public SymAST forLetRec(LetRec l) {
    /* Check for duplicates in LetRec vars, rename vars, construct newEnv, check LetRec rhs's and body in newEnv, 
     * translate rhs's and body */
    Variable[] vars = l.vars();
    SymAST[] exps =  l.exps();
    
    PureList<Variable> varList = PureListClass.arrayToList(vars);
    varList.accept(AnyDuplicatesVisitor.ONLY);  // throws exception on an error
    
    int n = vars.length;    
    
    /** DUPLICATED CODE; see above */
    PureList<DepthVariable> newEnv = env;
    Variable[] newVars = new Variable[n];
    
    for (int i = n-1; i >= 0; i--) {
      DepthVariable newDepthVar = new DepthVariable(vars[i], depth+1);
      newEnv = newEnv.cons(newDepthVar);
      newVars[i] = newDepthVar.rename();
    }
    
    CheckVisitor newVisitor = newVisitor(newEnv);  // increments depth
    //System.out.println("forLetrec: new visitor:" + newVisitor.depth);

    SymAST[] newExps = new SymAST[n];
    for (int i = 0; i < n; i++) 
      newExps[i] = exps[i].accept(newVisitor); 
    
    SymAST newBody = l.body().accept(newVisitor); // throws an exception on error
    return new LetRec(Def.makeDefs(newVars,newExps), newBody);
  }
  
  public SymAST forLet(Let l) {
    /* Check for duplicates in Let vars; rename vars to eliminate shadowing; check and translate RHSs of defs;
     * construct new symbol table (newEnv) extending the old one (env); check and transform body. */
    Variable[] vars = l.vars();
    PureList<Variable> varList = PureListClass.arrayToList(vars);
    varList.accept(AnyDuplicatesVisitor.ONLY);  // throws exception on an error
    
    SymAST[] exps = l.exps();
    int n = vars.length;    
    SymAST[] newExps = new SymAST[n];

    for (int i = 0; i < n; i++)  
      newExps[i] = exps[i].accept(this);  
        
    /** More DUPLICATED Code */
    PureList<DepthVariable> newEnv = env;
    Variable[] newVars = new Variable[n];
    
    for (int i = n-1; i >= 0; i--) {
      DepthVariable newDepthVar = new DepthVariable(vars[i], depth+1);
      newEnv = newEnv.cons(newDepthVar);
      newVars[i] = newDepthVar.rename();
    }
    
    CheckVisitor newVisitor = newVisitor(newEnv);  // embeds new symbol table in newVisitor 
    //System.out.println("forLet: new visitor:" + newVisitor.depth);
    SymAST newBody = l.body().accept(newVisitor); // throws an exception on error
    return new Let(Def.makeDefs(newVars, newExps), newBody);
  }
  
  public SymAST forLetcc(Letcc host) {
    DepthVariable newDepthVar = new DepthVariable(host.var(), depth+1);
    PureList<DepthVariable> newEnv = env.cons(newDepthVar);
    return new Letcc(newDepthVar.rename(), host.body().accept(newVisitor(newEnv)));
  }
  
  public SymAST forBlock(Block b) {
    // Check each exp 
    SymAST[] exps =  (SymAST[]) b.exps();
    int n = exps.length;    
    SymAST[] newExps = new SymAST[n];
    for (int i = 0; i < n; i++) newExps[i] = exps[i].accept(this); 
    return new Block(newExps);
  }
}

/** Visitor that checks for duplicate variables in a symbol table (a singleton) 
 *  Throws an SyntaxException if it encounters such an error; otherwise returns null
 */
class AnyDuplicatesVisitor implements PureListVisitor<Variable,Void> {
  /* Create singleton instance. */
  public static AnyDuplicatesVisitor ONLY = new AnyDuplicatesVisitor();
  private AnyDuplicatesVisitor() {}
  
  public Void forEmpty(Empty<Variable> host) { return null; }
  public Void forCons(Cons<Variable> host) { 
    if (host.rest().hasMember(host.first())) throw new
      SyntaxException(host.first() + " is declared twice in the same scope");
    host.rest().accept(this);
    return null;
  }
}
  
/** A lookup visitor class that returns element matching the embedded var. If no match found, returns null. */
class LookupVisitor<ElemType extends WithVariable> implements 
  PureListVisitor<ElemType,ElemType> {
  
  Variable var;  // the lexer guarantees that there is only one Variable object for a given name
  
  LookupVisitor(Variable v) { var = v; }
  
  public ElemType forEmpty(Empty<ElemType> e) { return null; }
  
  public ElemType forCons(Cons<ElemType> c) {
//    System.err.println("forCons in LookUpVisitor invoked; c = " + c);
    ElemType e = c.first();
    if (var == e.var()) return e;
    return c.rest().accept(this);
  }
}

class SyntaxException extends RuntimeException {
  SyntaxException(String s) { super(s); }
}

class SConvertException extends RuntimeException {
  SConvertException(String s) { super(s); }
}

/* Class for converting a SymAST (symbol variables in AST) to an SDAST (static distance coordinates in AST) */
class SConverter {
    
  SymbolTable symbolTable;
  SymASTVisitor<SDAST> convert;   // visitor that performs the conversion
  
  SConverter() {
    symbolTable = new SymbolTable();
    convert = new SConvert(0);
  }
  
  SDAST convert(SymAST prog) { return prog.accept(convert); }
  
  /** Visitor class for performing the static distance conversion; it modifies symbolTable during traversal, 
    * but restores it on exit */
  class SConvert implements SymASTVisitor<SDAST> {
    
    int depth; // lexical depth of expression being visited
    
    SConvert(int d) { depth = d; }
    
    /** Returns Pair containing (depth - [dist for v in symbolTable], offset for v in symbolTable).
      * Note: programs are assumed to be well-formed. */ 
    Pair lookup(Variable v) {
      Pair match = (Pair) symbolTable.get(v);
      if (match == null) 
        throw new SConvertException("Variable " + v + " not found in Symbol Table");
      return new Pair(depth - match.dist(), match.offset());
    }
  
    private SDAST forDefault(AST host) { throw new SyntaxException(host + " is not a legal input to SConvert"); }
    public SDAST forIntConstant(IntConstant i) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forNullConstant(NullConstant n) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forBoolConstant(BoolConstant b) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forSymVariable(Variable v) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forPrimFun(PrimFun f) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forUnOpApp(UnOpApp u) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forBinOpApp(BinOpApp b) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forApp(App a) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forMap(Map m) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forIf(If i) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forLet(Let l) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forLetRec(LetRec l) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forLetcc(Letcc host) { /* . . . */ return null; /* This is a STUB. */ }
    public SDAST forBlock(Block b) { /* . . . */ return null; /* This is a STUB. */ }
  }
  
  public static void main(String[] args) throws IOException  {
    /* Check for legal argument list. */ 
    if (args.length == 0) {
      System.out.println("Usage: java SConverter <filename>");
      return;
    }
    Parser p = new Parser(args[0]);
    SConverter s = new SConverter();
    SymAST prog = p.checkProg();
    System.out.println("Parsed program is:\n" + prog);
    try { System.out.println("\nConverted form is:\n" + s.convert(prog)); }  
    catch (Exception e) { e.printStackTrace(); }
    
    SymAST cpsProg = p.cpsProg();
    System.out.println("CPSed program is:\n" + cpsProg);
    System.out.println("\nConverted form is:\n" + s.convert(cpsProg));
  }
  static class SymbolTable {    
    /* Table mapping variables to LinkedLists of Pair (depth,offset) */
    HashMap<Variable,LinkedList<Pair>> table = new HashMap<Variable,LinkedList<Pair>>();  
    
    Pair get(Variable v) {
      LinkedList<Pair> vStack = table.get(v);
      if (v == null) return null;
      return (Pair) vStack.getLast();
    }
    
    void put(Variable v, Pair p) {
      LinkedList<Pair> vStack = table.get(v);
      if (vStack == null) {
        vStack = new LinkedList<Pair>();
        table.put(v,vStack);
      }
      vStack.addLast(p);
    }
    
    void remove(Variable v) {
      LinkedList<Pair> vStack = table.get(v);
      if (vStack == null) throw new SConvertException("Variable " + v + " not available in symbol table to delete");
      vStack.removeLast();
    }
  }
}
