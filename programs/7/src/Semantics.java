import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/** The exception class for Jam run-time errors */
class EvalException extends RuntimeException {
  EvalException(String msg) { super(msg); }
}

/** The visitor interface for interpreting AST's */
interface EvalVisitor extends ASTVisitor<JamVal> {
  
  /** Returns the environment embedded in this EvalVisitor */
  Environment env();
  
  /** Constructs a UnOpVisitor with the specified evaluated arg. */
  UnOpVisitor<JamVal> newUnOpVisitor(JamVal arg);

  /** Constructs a BinOpVisitor with the specified unevaluated arguments. */
  BinOpVisitor<JamVal> newBinOpVisitor(AST arg1, AST arg2);

  /** Constructs a FunVisitor with the specified array of unevaluated arguments */
  FunVisitor<JamVal> newFunVisitor(AST[] args);
}

/** Jam closure represention for programs with symbolic variables*/
class VarClosure extends JamFun implements Closure {
  private Map map;
  private SymEvaluator eval;
  VarClosure(Map m, SymEvaluator e) { map = m; eval = e; }
  public int arity() { return map.vars().length; }
  public JamVal apply(JamVal[] args) {
	Variable[] vars = map.vars();
	VarEnv newEnv = eval.env();
	int n = vars.length;
	if (n != args.length) throw new EvalException("closure " + this + " applied to " +
	   args.length + " arguments instead of " + n + " arguments");
	for (int i = n-1 ; i >= 0; i--)
	  newEnv = newEnv.cons(new Binding(vars[i],args[i]));
	return map.body().accept(eval.newEvalVisitor(newEnv));
  }
  public <RtnType> RtnType accept(FunVisitor<RtnType> jfv) { return jfv.forClosure(this); }
  public String toString() { return "(closure: " + map + ")"; }
}

/** Jam closure representation for programs with static distance coordinates */
class SDClosure extends JamFun implements Closure {
  private SMap smap;
  private SDEvaluator eval;
  
  SDClosure(SMap sm, SDEvaluator e) {
  	smap = sm; eval = e;
  }
  
  public int arity() {
  	return smap.arity();
  }
  public JamVal apply(JamVal[] args) {
	SDEnv newEnv = eval.env();
	int n = smap.arity();
	if (n != args.length) throw new EvalException("closure " + this + " applied to " +
	   args.length + " arguments instead of " + n + " arguments");
	  newEnv = newEnv.cons(args);
	return smap.body().accept(eval.newEvalVisitor(newEnv));
  }
  public <RtnType> RtnType accept(FunVisitor<RtnType> jfv) { return jfv.forClosure(this); }
  public String toString() { return "(closure: " + smap + ")"; }
}

class Interpreter {
  
  private Parser parser;
  public static final int HEAPSIZE = 1 << 18;
  private int[] heap;
  
  Interpreter(String fileName) throws IOException { parser = new Parser(fileName); }
  
  Interpreter(Parser p) { parser = p; }
  
  Interpreter(Reader reader) {
  	parser = new Parser(reader);
  	heap = new int[HEAPSIZE];
  }

  Interpreter(StringReader stringReader, int hs) {
	  parser = new Parser(stringReader);
	  heap = new int[HEAPSIZE];
	  // TODO: This initialization to 0 may be unnecessary.
	  for (int i = 0; i < HEAPSIZE; i++) {
	  	heap[i] = 0;
	  }
	  ramEvaluator = new ramEvaluator(heap);
  }

	private void printHeap() {
		String out = "";
		int i = 0;
		while (heap[i] != 0) {
			out = out + heap[i] + " ";
			i++;
		}
		System.out.println(out);
	}
	
	/**
	 * Retursn the heap.
	 */
	public int[] getMemory() {
		return heap;
	}
	
	/**
	 * Returns the JamVal result (decoding the heap-index or pseudo-index) of evaluating the embedded SDAST program using the low-level interpreter
	 */
	public JamVal ramSDEval() {
		SDAST prog = parser.statCheckProg();
		System.out.println(prog);
		Integer out = prog.accept(ramEvaluator);
		return ramCaseEval(out);
	}
	
	/**
	 * Returns the JamVal result (decoding the heap index or pseudo-index) of evaluating the SDAST representation of the embedded program converted to CPS
	 * using the low-level interpreter.
	 */
	public JamVal ramSDCpsEval() {
		// TODO
		return null;
	}

	private JamVal ramCaseEval(Integer idx) {
		int tag = heap[idx];
		printHeap();
		switch (tag) {
			case 0:
				System.out.println("Gets into case 0: Exception case");
				throw new EvalException("ramEval should never encounter case 0");
			case 1:
				System.out.println("Gets into case 1: int");
				return new IntConstant(heap[idx + 1]);
			case 2:
				// TODO: Implement this case.
				System.out.println("Gets into case 2: cons");
			case 3:
				System.out.println("Gets into case 3: ref");
				JamVal arg = ramCaseEval(idx + 1);
				return new JamRef(arg);
			case 4:
				// TODO: Implement this case.
				System.out.println("Gets into case 4: closure");
			case 5:
				// TODO: Implement this case.
				System.out.println("Gets into case 5: activation record");
			case -1:
				System.out.println("Gets into case -1: null");
				return JamEmpty.ONLY;
			case -2:
				System.out.println("Gets into case -2: unit");
				return JamUnit.ONLY;
			case -3:
				System.out.println("Gets into case -3: true");
				return BoolConstant.TRUE;
			case -4:
				System.out.println("Gets into case -4: false");
				return BoolConstant.FALSE;
			case -5:
				System.out.println("Gets into case -5: number?");
				return NumberPPrim.ONLY;
			case -6:
				System.out.println("Gets into case -6: function?");
				return FunctionPPrim.ONLY;
			case -7:
				System.out.println("Gets into case -7: list?");
				return ListPPrim.ONLY;
			case -8:
				System.out.println("Gets into case -8: null?");
				return NullPPrim.ONLY;
			case -9:
				System.out.println("Gets into case -9: cons?");
				return ConsPPrim.ONLY;
			case -10:
				System.out.println("Gets into case -10: ref");
				return RefPPrim.ONLY;
			case -11:
				System.out.println("Gets into case -11: arity");
				return ArityPrim.ONLY;
			case -12:
				System.out.println("Gets into case -12: cons");
				return ConsPrim.ONLY;
			case -13:
				System.out.println("Gets into case -13: first");
				return FirstPrim.ONLY;
			case -14:
				System.out.println("Gets into case -14: rest");
				return RestPrim.ONLY;
			default:
				System.out.println("Gets into default case");
				throw new EvalException("ramEval should never encounter case 0");
		}
	}

	
	/** Parses, checks, and interprets the input embeded in parser */
    public JamVal eval() {
	SymAST prog = parser.checkProg();
	return prog.accept(valueValueVisitor);
  }
  
  /** Parses and checks the input embeded in parser, converts it to SD form, and interprets it. */
  public JamVal SDEval() {
	SDAST prog = parser.statCheckProg();
	return prog.accept(SDEvalVisitor);
  }
  /** Parses, checks, CPS converts, and interprets the input embedded in parser using the SymAST representation. */
  public JamVal cpsEval() {
	SymAST prog = parser.cpsProg();
	return prog.accept(valueValueVisitor);
  }
  /** Parses, checks, CPS converts, SD converts, and interprets the input embedded in parser using the SDAST representation. */
  public JamVal SDCpsEval() {
	SDAST prog = parser.statCpsProg();
	return prog.accept(SDEvalVisitor);
  }
  
  /** Renames variables in parsed program, a SymAST, so no variable is shadowed. */
  public SymAST unshadow() {
	return parser.checkProg();
  }

  /** Returns the CPS form of the embedded proggram. */
  public SymAST convertToCPS() { return parser.cpsProg(); }
  
  /** Returns the SDAST for the embedded program. */
  public SDAST convertToSD() {
	return parser.statCheckProg();
  }
  
  /** Visitor that evaluates programs represented in SymAST form. */
  private SymASTVisitor<JamVal> valueValueVisitor = new SymEvaluator(EmptyVarEnv.ONLY);
  
  /** Visitor that evaluates programs represented in SymAST form. */
  private SDASTVisitor<JamVal> SDEvalVisitor = new SDEvaluator(EmptySDEnv.ONLY);

  private SDASTVisitor<Integer> ramEvaluator;
  
  
}

/* General visitor class for performing interpretation; defines ComASTVistor methods which are common to SymAST and SDAST representations. */
abstract class Evaluator<Env extends Environment> implements EvalVisitor {
  
  /* Assumes that:
   *   OpTokens are unique
   *   Only objects used as boolean values are BoolConstant.TRUE and BoolConstant.FALSE
   * Hence,  == can be used to compare OpTokens, and BoolConstants.
   */
  
  Env env;  // getter defined below
  
  /* Constructor */
  Evaluator(Env e) { env = e; }
  
  private JamVal[] evalArgs(AST[] args) {
	int n = args.length;
	JamVal[] vals = new JamVal[n];
	for (int i = 0; i < n; i++) vals[i] = args[i].accept(this);
	return vals;
  }
  
  /* EvalVisitor methods */
  
  /** Getter for env field */
  public Env env() { return env; }
  
  /* ASTVisitor methods.  EvalVisitor extends ASTVisitor<JamVal>. */
  public UnOpVisitor<JamVal> newUnOpVisitor(JamVal arg) { return new UnOpEvaluator(arg); }
  public BinOpVisitor<JamVal> newBinOpVisitor(AST arg1, AST arg2) { return new BinOpEvaluator(arg1, arg2); }
  public FunVisitor<JamVal> newFunVisitor(AST[] args) {  return new FunEvaluator(evalArgs(args)); }
  
  /* ComASTVisitor<JamVal> methods.  ASTVisitor<JamVal> extends ComASTVisitor<JamVal>. */
  
  /** Method for an aborting error, uned in subclasses */
  /* package */ JamVal forDefault(AST a) { throw new EvalException(a + " is not in the domain of the visitor " + getClass()); }
  public JamVal forBoolConstant(BoolConstant b) { return b; }
  public JamVal forIntConstant(IntConstant i) { return i; }
  public JamVal forNullConstant(NullConstant n) { return JamEmpty.ONLY; }
  public JamVal forPrimFun(PrimFun f) { return f; }
  public JamVal forUnOpApp(UnOpApp u) {
	return u.rator().accept(newUnOpVisitor(u.arg().accept(this)));
  }
  public JamVal forBinOpApp(BinOpApp b) {
	return b.rator().accept(newBinOpVisitor(b.arg1(), b.arg2()));
  }
 
  public JamVal forApp(App a) {
	JamVal rator = a.rator().accept(this);
	if (rator instanceof JamFun)  {
	  //System.err.println(Evaluator.this);
	  //System.err.println(newFunVisitor(a.args()).getClass());
	  return ((JamFun) rator).accept(newFunVisitor(a.args()));
	}
	throw new EvalException(rator + " appears at head of application " + a  + " but it is not a valid function");
  }
  
  public JamVal forIf(If i) {
	JamVal test = i.test().accept(this);
	if (! (test instanceof BoolConstant))
	  throw new EvalException("non Boolean " + test + " used as test in if");
	if (test == BoolConstant.TRUE) return i.conseq().accept(this);
	return i.alt().accept(this);
  }

  public JamVal forBlock(Block b) {
	AST[] exps = b.exps();
	int n = exps.length;
	for (int i = 0; i < n-1; i++) exps[i].accept(this);
	return exps[n-1].accept(this);
  }
  
  /* Methods common to SymASTVisitor and SDASTVisitor but not semantically shared. */
  
  /** In either a SymAST or SDAST, evaluating Letcc node throws an exception.  In the former case, it is
	* not supported.  In the latter case, it is syntactically illegal but we are forced to include
	* it our definitions in order to share code. */
  public JamVal forLetcc(Letcc host) { return forDefault(host); }
  
  /** Remaining visitor methods are abstract; they are defined differently in SymAST and SDAST evaluation.  For each
	* form of evaluation, some methods below generate run-time errors because the corresponding nodes do not appear
	* in well-formed instances of those AST types. */
  abstract public JamVal forSymVariable(Variable host);
  abstract public JamVal forMap(Map host);
  abstract public JamVal forLet(Let host);
  abstract public JamVal forLetRec(LetRec host);
  
  abstract public JamVal forPair(Pair host);
  abstract public JamVal forSMap(SMap host);
  abstract public JamVal forSLet(SLet host);
  abstract public JamVal forSLetRec(SLetRec host);

  /* Inner classes */
  
  class FunEvaluator implements FunVisitor<JamVal> {

	/** Evaluated arguments */
	JamVal[] vals;

	/** number of arguments */
	int arity;

	FunEvaluator(JamVal[] jvs) {
	  vals = jvs;
	  arity = vals.length;
	  // System.err.println("FunEvaluator created with vals = " + ToString.toString(vals, ","));
	}

	public JamVal forPrimFun(PrimFun primFun) { return primFun.accept(primEvaluator); }

	public JamVal forClosure(Closure c) {
		return c.apply(vals);
	}

	/* Field bound to anonymous inner classes */
	PrimFunVisitor<JamVal> primEvaluator =
	  new PrimFunVisitor<JamVal>() { /* ANONYMOUS CLASS */

	  private JamVal primFunError(String fn) {
		throw new EvalException("Primitive function `" + fn + "' applied to " + arity + " arguments");
	  }

	  private JamCons toJamCons(JamVal val, String fun) {
		if (val instanceof JamCons) return (JamCons) val;
		throw new EvalException("Primitive function `" + fun + "' applied to argument " + val + " that is not a JamCons");
	  }

	  public JamVal forFunctionPPrim() {
		if (arity != 1) return primFunError("function?");
		return BoolConstant.toBoolConstant(vals[0] instanceof JamFun);
	  }

	  public JamVal forNumberPPrim() {
		if (arity != 1) return primFunError("number?");
		return BoolConstant.toBoolConstant(vals[0] instanceof IntConstant);
	  }

	  public JamVal forListPPrim() {
		if (arity != 1) return primFunError("list?");
		return BoolConstant.toBoolConstant(vals[0] instanceof JamList);
	  }

	  public JamVal forConsPPrim() {
		if (arity != 1) return primFunError("cons?");
		return BoolConstant.toBoolConstant(vals[0] instanceof JamCons);
	  }

	  public JamVal forNullPPrim() {
		if (arity != 1) return primFunError("null?");
		return BoolConstant.toBoolConstant(vals[0] instanceof JamEmpty);
	  }

	  public JamVal forConsPrim() {
		if (arity != 2) return primFunError("cons");
		if (! (vals[1] instanceof JamList))
		  throw new EvalException("Second argument " + vals[1] + " to `cons' is not a JamList");
		return new JamCons(vals[0], (JamList) vals[1]);
	  }

	  public JamVal forArityPrim() {
		if (arity != 1) return primFunError("arity");
		if (! (vals[0] instanceof JamFun) ) throw new EvalException("arity applied to argument " +
																	vals[0]);
		return ((JamFun) vals[0]).accept(arityEvaluator);
	  }

	  public JamVal forRefPPrim() {
		if (arity != 1) return primFunError("ref?");
//        System.err.println("ref? applied to " + vals[0] + " =  "+ BoolConstant.toBoolConstant(vals[0] instanceof JamRef));
		return BoolConstant.toBoolConstant(vals[0] instanceof JamRef);
	  }

	  public JamVal forFirstPrim() { return toJamCons(vals[0], "first").first(); }
	  public JamVal forRestPrim() { return toJamCons(vals[0], "rest").rest(); }

	  public JamVal forAsBoolPrim() {
		JamVal val = vals[0];
		if (val instanceof BoolConstant) return val;
		else throw new EvalException("The Jam value " + val + " must be of boolean type");
	  }
	};

	FunVisitor<IntConstant> arityEvaluator = new FunVisitor<IntConstant>() { /* ANONYMOUS CLASS */
	  public IntConstant forClosure(Closure jc) { return new IntConstant(jc.arity()); }
	  public IntConstant forPrimFun(PrimFun jpf) { return jpf.accept(primArityEvaluator); }
	};

	PrimFunVisitor<IntConstant> primArityEvaluator =
	  new PrimFunVisitor<IntConstant>() { /* ANONYMOUS CLASS */

	  public IntConstant forFunctionPPrim() { return new IntConstant(1); }
	  public IntConstant forNumberPPrim() { return new IntConstant(1); }
	  public IntConstant forListPPrim() { return new IntConstant(1); }
	  public IntConstant forConsPPrim() { return new IntConstant(1); }
	  public IntConstant forNullPPrim() { return new IntConstant(1); }
	  public IntConstant forArityPrim() { return new IntConstant(1); }
	  public IntConstant forConsPrim() { return new IntConstant(2); }
	  public IntConstant forRefPPrim() { return new IntConstant(1); }
	  public IntConstant forFirstPrim() { return new IntConstant(1); }
	  public IntConstant forRestPrim() { return new IntConstant(1); }
	  public IntConstant forAsBoolPrim() { return new IntConstant(1); }
	};
  }
  
  static class UnOpEvaluator implements UnOpVisitor<JamVal> {
	private JamVal val;

	UnOpEvaluator(JamVal jv) { val = jv; }

	private IntConstant checkInteger(UnOp op) {
	  if (val instanceof IntConstant) return (IntConstant) val;
	  throw new EvalException("Unary operator `" + op + "' applied to non-integer " + val);
	}

	private BoolConstant checkEval(UnOp op) {
	  if (val instanceof BoolConstant) return (BoolConstant) val;
	  throw new EvalException("Unary operator `" + op + "' applied to non-boolean " + val);
	}

	private JamRef checkRef(UnOp op) {
	  if (val instanceof JamRef) return (JamRef) val;
	  throw new EvalException("Unary operator `" + op + "' applied to non-reference" + val);
	}

	public JamVal forUnOpPlus(UnOpPlus op) { return checkInteger(op); }
	public JamVal forUnOpMinus(UnOpMinus op) {
	  return new IntConstant(- checkInteger(op).value());
	}
	public JamVal forOpTilde(OpTilde op) { return checkEval(op).not(); }
	public JamVal forOpBang(OpBang op) { return checkRef(op).value(); }
	public JamVal forOpRef(OpRef op) { return new JamRef(val); }
  }
  
  class BinOpEvaluator implements BinOpVisitor<JamVal> {
	private AST arg1, arg2;

	BinOpEvaluator(AST a1, AST a2) { arg1 = a1; arg2 = a2; }

	private IntConstant evalIntegerArg(AST arg, BinOp b) {
	  JamVal val = arg.accept(Evaluator.this);
	  if (val instanceof IntConstant) return (IntConstant) val;
	  throw new EvalException("Binary operator `" + b + "' applied to non-integer " + val);
	}

	private BoolConstant evalBoolArg(AST arg, BinOp b) {
	  JamVal val = arg.accept(Evaluator.this);
	  if (val instanceof BoolConstant) return (BoolConstant) val;
	  throw new EvalException("Binary operator `" + b + "' applied to non-boolean " + val);
	}

//    public JamVal forDefault(BinOp op) { throw new EvalException(op + " is not a supported binary operation"); }

	public JamVal forBinOpPlus(BinOpPlus op) {
	  return new IntConstant(evalIntegerArg(arg1,op).value() + evalIntegerArg(arg2,op).value());
	}
	public JamVal forBinOpMinus(BinOpMinus op) {
	  return new IntConstant(evalIntegerArg(arg1,op).value() - evalIntegerArg(arg2,op).value());
	}

	public JamVal forOpTimes(OpTimes op) {
	  return new IntConstant(evalIntegerArg(arg1,op).value() * evalIntegerArg(arg2,op).value());
	}

	public JamVal forOpDivide(OpDivide op) {
	  int divisor = evalIntegerArg(arg2,op).value();
	  if (divisor == 0) throw new EvalException("Attempt to divide by zero");
	  return new IntConstant(evalIntegerArg(arg1,op).value() / divisor);
	}

	public JamVal forOpEquals(OpEquals op) {
	  return BoolConstant.toBoolConstant(arg1.accept(Evaluator.this).equals(arg2.accept(Evaluator.this)));
	}

	public JamVal forOpNotEquals(OpNotEquals op) {
	  return BoolConstant.toBoolConstant(! arg1.accept(Evaluator.this).equals(arg2.accept(Evaluator.this)));
	}

	public JamVal forOpLessThan(OpLessThan op) {
	  return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() < evalIntegerArg(arg2,op).value());
	}

	public JamVal forOpGreaterThan(OpGreaterThan op) {
	  return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() > evalIntegerArg(arg2,op).value());
	}

	public JamVal forOpLessThanEquals(OpLessThanEquals op) {
	  return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() <= evalIntegerArg(arg2,op).value());
	}

	public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
	  return BoolConstant.toBoolConstant(evalIntegerArg(arg1,op).value() >= evalIntegerArg(arg2,op).value());
	}

	public JamVal forOpAnd(OpAnd op) {
	  BoolConstant b1 = evalBoolArg(arg1,op);
	  if (b1 == BoolConstant.FALSE) return BoolConstant.FALSE;
	  return evalBoolArg(arg2,op);
	}
	public JamVal forOpOr(OpOr op) {
	  BoolConstant b1 = evalBoolArg(arg1,op);
	  if (b1 == BoolConstant.TRUE) return BoolConstant.TRUE;
	  return evalBoolArg(arg2,op);
	}
	public JamVal forOpGets(OpGets op) {
	  JamVal val1 = arg1.accept(Evaluator.this);
	  if (! (val1 instanceof JamRef)) throw new EvalException("Left argument " + arg1 + " of <- is not a JamRef");
	  ((JamRef) val1).setValue(arg2.accept(Evaluator.this));
	  return JamUnit.ONLY;
	}
  }
}

class SymEvaluator extends Evaluator<VarEnv> {

  public SymEvaluator(VarEnv e) { super(e); }
  
  /* EvalVisitor methods for SymASTs */
  public SymASTVisitor<JamVal> newEvalVisitor(VarEnv env) { return new SymEvaluator(env); }
  public JamVal forSymVariable(Variable v) { return env.lookup(v); }
  public JamVal forMap(Map m) { return new VarClosure(m, this); }
  public JamVal forLet(Let l) {
	/* Extract binding vars and exps (rhs's) from l */
	Variable[] vars = l.vars();
	SymAST[] exps = l.exps();
	int n = vars.length;
	/* Construct newEnv for Let body and exps; vars are bound to values of corresponding exps using newEvalVisitor */
	VarEnv newEnv = env();
	Binding[] bindings = new Binding[n];
	for (int i = n-1; i >= 0; i--) {
	  bindings[i] = new Binding(vars[i], exps[i].accept(this));  // bind var[i] to exps[i] in this evaluator
	  newEnv = newEnv.cons(bindings[i]);
	}
	SymASTVisitor<JamVal> newEvalVisitor = newEvalVisitor(newEnv);
	return l.body().accept(newEvalVisitor);
  }
  
  public JamVal forLetRec(LetRec l) {
	/* Extract binding vars and exps (rhs's) from l */
	Variable[] vars = l.vars();
	SymAST[] exps = l.exps();
	int n = vars.length;
	/* Construct newEnv for Let body and exps; vars are bound to values of corresponding exps using newEvalVisitor */
	VarEnv newEnv = env();

	Binding[] bindings = new Binding[n];
	for (int i = n-1; i >= 0; i--) {
	  bindings[i] = new Binding(vars[i], null);  // bind var[i], setting value to null, which is not a JamVal
	  newEnv = newEnv.cons(bindings[i]);
	}

	SymASTVisitor<JamVal> newEvalVisitor = newEvalVisitor(newEnv);

	// fix up the dummy values
	for (int i = 0; i < n; i++)
	  bindings[i].setBinding(exps[i].accept(newEvalVisitor));  // modifies newEnv and newEvalVisitor

	return l.body().accept(newEvalVisitor);
  }
  
  /* EvalVisitor methods for evaluating SDASTs that are never invoked in the evaluation of well-formed SymASTs. */
  public JamVal forPair(Pair host) { return forDefault(host); }
  public JamVal forSMap(SMap host) { return forDefault(host); }
  public JamVal forSLet(SLet host) { return forDefault(host); }
  public JamVal forSLetRec(SLetRec host) { return forDefault(host); }
}


class varAddress {
	int startIdx;
	int endIdx;

	public varAddress(int s, int e) {
		startIdx = s;
		endIdx = e;
	}
	
	public String toString() {
		return "varAddress: start=" + this.startIdx + " end=" + this.endIdx;
	}
}
/*
 * New visitor that does stuff with the heap and then returns the address of the final result.
 * Then, in the ramXYZ methods, we use this address and the heap to make the desired JamVal and return that as the
 * output to the user/test.
 */
class ramEvaluator implements ASTVisitor<Integer> {
	
	// TODO: do we want this here?
	private ArrayList<SDAST> codeTbl = new ArrayList<>();
	public ArrayList<SDAST> getCodeTbl() {
		return codeTbl;
	}

	private int[] heap;

	private ArrayList<varAddress[]> envLink = new ArrayList<>();

	private int lastIdx = 0;

	ramEvaluator(int[] heap) {
		this.heap = heap;
	}

	public Integer forPair(Pair p) {
		varAddress v = envLink.get(envLink.size() - 1 - p.dist())[p.offset()];
		return v.startIdx;
	}

	public Integer forSMap(SMap sm) {
		// TODO
		return 0;
	}

	public Integer forSLet(SLet sl) {
		heap[lastIdx] = 5;
		lastIdx++;
		
		//TODO: just set first env's parent to -100 to avoid any conflicts with the existing tags.
		// May need to change this later.
		heap[lastIdx] = envLink.size() == 0 ? -100 : envLink.size() - 1;
		lastIdx++;
		heap[lastIdx] = sl.rhss().length;
		lastIdx++;
		varAddress[] thisEnv = new varAddress[sl.rhss().length];
		for (int i = 0; i < sl.rhss().length; i++) {
			Integer startIdx = sl.rhss()[i].accept(this);
			int endIdx = lastIdx - 1;
			thisEnv[i] = new varAddress(startIdx, endIdx);
		}
		envLink.add(thisEnv);

		return sl.body().accept(this);
	}

	public Integer forSLetRec(SLetRec slr) {
		// TODO
		return 0;
	}

	public Integer forBoolConstant(BoolConstant b) {
		int temp = lastIdx;
		int boolVal = b.value() ? -3 : -4;
		heap[lastIdx] = boolVal;
		lastIdx++;
		return temp;
	}

	public Integer forIntConstant(IntConstant i) {
		int temp = lastIdx;
		heap[lastIdx] = 1;
		lastIdx++;
		heap[lastIdx] = i.value();
		lastIdx++;
		return temp;
	}

	public Integer forNullConstant(NullConstant n) {
		int temp = lastIdx;
		heap[lastIdx] = -1;
		lastIdx++;
		return temp;
	}

	public Integer forPrimFun(PrimFun pf) {
		Integer tag = pf.accept(new PrimFunVisitor<Integer>() {
			@Override
			public Integer forFunctionPPrim() {
				return -6;
			}

			@Override
			public Integer forNumberPPrim() {
				return -5;
			}

			@Override
			public Integer forListPPrim() {
				return -7;
			}

			@Override
			public Integer forConsPPrim() {
				return -9;
			}

			@Override
			public Integer forNullPPrim() {
				return -8;
			}

			@Override
			public Integer forArityPrim() {
				return -11;
			}

			@Override
			public Integer forConsPrim() {
				return -12;
			}

			@Override
			public Integer forRefPPrim() {
				return -10;
			}

			@Override
			public Integer forFirstPrim() {
				return -13;
			}

			@Override
			public Integer forRestPrim() {
				return -14;
			}

			@Override
			public Integer forAsBoolPrim() {
				return -15;
			}
		});
		int temp = lastIdx;
		heap[lastIdx] = tag;
		lastIdx++;
		return temp;
	}

	public Integer forUnOpApp(UnOpApp u) {
		Integer argTagIdx = u.arg().accept(this);
		return u.rator().accept(new UnOpVisitor<Integer>() {
			@Override
			public Integer forUnOpPlus(UnOpPlus op) {
				if (heap[argTagIdx] == 1) {
					int temp = lastIdx;
					heap[lastIdx] = 1;
					lastIdx++;
					heap[lastIdx] = +heap[argTagIdx + 1];
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("arg: " + u.arg() + " is not an integer");
				}
			}
			
			@Override
			public Integer forUnOpMinus(UnOpMinus op) {
				if (heap[argTagIdx] == 1) {
					int temp = lastIdx;
					heap[lastIdx] = 1;
					lastIdx++;
					heap[lastIdx] = -heap[argTagIdx + 1];
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("arg: " + u.arg() + " is not an integer");
				}
			}
			
			@Override
			public Integer forOpTilde(OpTilde op) {
				if (heap[argTagIdx] == -3) {
					int temp = lastIdx;
					heap[lastIdx] = -4;
					lastIdx++;
					return temp;
				} else if (heap[argTagIdx] == -4) {
					int temp = lastIdx;
					heap[lastIdx] = -3;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("Arg: " + u.arg() + " is not a boolean");
				}
			}
			
			@Override
			public Integer forOpBang(OpBang op) {
				if (heap[argTagIdx] != 3) {
					throw new EvalException("Operator ! applied to " + u.arg() + " which is not a ref");
				}
				return argTagIdx + 1;
			}
			
			@Override
			public Integer forOpRef(OpRef op) {
				// the way we do things here, we're pushing the arguement information on the heap twice, because
				// we evaluate the argument before this visitor. It seems correct but redundant.
				for (int i = lastIdx; i > argTagIdx; i--) {
					heap[i] = heap[i - 1];
				}
				heap[argTagIdx] = 3;
				lastIdx++;

				return argTagIdx;
			}
		});
	}

	public Integer forBinOpApp(BinOpApp b) {
		Integer argTagIdx1 = b.arg1().accept(this);
		Integer argTagIdx2 = b.arg2().accept(this);
		System.out.printf("BinOpApp arg1: %s, idx1: %d arg2: %s, idx2: %d\n", b.arg1(), argTagIdx1, b.arg2(), argTagIdx2);
		return b.rator().accept(new BinOpVisitor<Integer>() {
			@Override
			public Integer forBinOpPlus(BinOpPlus op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					heap[lastIdx] = 1;
					lastIdx++;
					heap[lastIdx] = heap[argTagIdx1 + 1] + heap[argTagIdx2 + 1];
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forBinOpMinus(BinOpMinus op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					heap[lastIdx] = 1;
					lastIdx++;
					heap[lastIdx] = heap[argTagIdx1 + 1] - heap[argTagIdx2 + 1];
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forOpTimes(OpTimes op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					heap[lastIdx] = 1;
					lastIdx++;
					heap[lastIdx] = heap[argTagIdx1 + 1] * heap[argTagIdx2 + 1];
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forOpDivide(OpDivide op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					heap[lastIdx] = 1;
					lastIdx++;
					heap[lastIdx] = heap[argTagIdx1 + 1] / heap[argTagIdx2 + 1];
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forOpEquals(OpEquals op) {
				int temp = lastIdx;
				// If tags are different, then output must be false.
				if (heap[argTagIdx1] != heap[argTagIdx2]) {
					heap[lastIdx] = -4; // false
					lastIdx++;
					return temp;
				// If tags are same but tag is ref, output is false. (Try ref 10 = ref 10 in reference interpreter).
				} else if (heap[argTagIdx1] == 3) {
					heap[lastIdx] = -4; // false
					lastIdx++;
					return temp;
				// If tags are negative, then this is primitive so return true (since both tags are the same).
				} else if (heap[argTagIdx1] < 0) {
					heap[lastIdx] = -3; // true
					lastIdx++;
					return temp;
				// Remaining cases:
				} else {
					// We don't have a reliable way of finding arg1Len because there could be stuff between arg1
					// and arg2. So we just find arg2 (we can do this reliably) and compare element by element.
					int arg2Len = (lastIdx - 1) - argTagIdx2;
					int arg1start = argTagIdx1 + 1;
					int arg2start = argTagIdx2 + 1;
					for (int i = 0; i < arg2Len; i++) {
						// If any are unequal
						if (heap[arg1start + i] != heap[arg2start + i]) {
							// Return false case
							heap[lastIdx] = -4; // false
							lastIdx++;
							return temp;
						}
					}
					// Everything equal so return true case.
					heap[lastIdx] = -3; // true
					lastIdx++;
					return temp;
				}
			}

			@Override
			public Integer forOpNotEquals(OpNotEquals op) {
				int temp = lastIdx;
				// If tags are different, then output must be true.
				if (heap[argTagIdx1] != heap[argTagIdx2]) {
					heap[lastIdx] = -3; // true
					lastIdx++;
					return temp;
					// If tags are same but tag is ref, output is true. (Try ref 10 = ref 10 in reference interpreter).
				} else if (heap[argTagIdx1] == 3) {
					heap[lastIdx] = -3; // true
					lastIdx++;
					return temp;
					// If tags are negative, then this is primitive so return false (since both tags are the same).
				} else if (heap[argTagIdx1] < 0) {
					heap[lastIdx] = -4; // false
					lastIdx++;
					return temp;
					// Remaining cases:
				} else {
					// We don't have a reliable way of finding arg1Len because there could be stuff between arg1
					// and arg2. So we just find arg2 (we can do this reliably) and compare element by element.
					int arg2Len = (lastIdx - 1) - argTagIdx2;
					int arg1start = argTagIdx1 + 1;
					int arg2start = argTagIdx2 + 1;
					for (int i = 0; i < arg2Len; i++) {
						// If any are unequal
						if (heap[arg1start + i] != heap[arg2start + i]) {
							// Return true case
							heap[lastIdx] = -3; // true
							lastIdx++;
							return temp;
						}
					}
					// Everything equal so return fa;se case.
					heap[lastIdx] = -4; // false
					lastIdx++;
					return temp;
				}

			}

			@Override
			public Integer forOpLessThan(OpLessThan op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					int boolVal = heap[argTagIdx1 + 1] < heap[argTagIdx2 + 1] ? -3 : -4;
					heap[lastIdx] = boolVal;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forOpGreaterThan(OpGreaterThan op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					int boolVal = heap[argTagIdx1 + 1] > heap[argTagIdx2 + 1] ? -3 : -4;
					heap[lastIdx] = boolVal;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forOpLessThanEquals(OpLessThanEquals op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					int boolVal = heap[argTagIdx1 + 1] <= heap[argTagIdx2 + 1] ? -3 : -4;
					heap[lastIdx] = boolVal;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			@Override
			public Integer forOpGreaterThanEquals(OpGreaterThanEquals op) {
				if (heap[argTagIdx1] == 1 && heap[argTagIdx2] == 1) {
					int temp = lastIdx;
					int boolVal = heap[argTagIdx1 + 1] >= heap[argTagIdx2 + 1] ? -3 : -4;
					heap[lastIdx] = boolVal;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an integer");
				}
			}

			private boolean bothBoolean(int l, int r) {
				if ((l != -3) && (l != -4)) {
					return false;
				}
				return (r == -3) || (r == -4);
			}

			@Override
			public Integer forOpAnd(OpAnd op) {
				//TODO: Come back to this later. The parser changes x & y to if x then y else false
				// so not sure if we ever actually get here.
				System.out.println("Arg1: " + b.arg1() + " Arg: " + b.arg2());
				if (bothBoolean(heap[argTagIdx1], heap[argTagIdx2])) {
					int temp = lastIdx;
					int boolVal = heap[argTagIdx1 + 1] == -3 && heap[argTagIdx2 + 1] == -3 ? -3 : -4;
					heap[lastIdx] = boolVal;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an boolean");
				}
			}

			@Override
			public Integer forOpOr(OpOr op) {
				//TODO: Come back to this later. The parser changes x & y to if x then y else false
				// so not sure if we ever actually get here.
				if (bothBoolean(heap[argTagIdx1], heap[argTagIdx2])) {
					int temp = lastIdx;
					int boolVal = heap[argTagIdx1 + 1] == -3 || heap[argTagIdx2 + 1] == -3 ? -3 : -4;
					heap[lastIdx] = boolVal;
					lastIdx++;
					return temp;
				} else {
					throw new EvalException("One of arg1: "+ b.arg1() + " arg2: " + b.arg2() + " is not an boolean");
				}
			}

			@Override
			public Integer forOpGets(OpGets op) {
				// TODO: The binary operation E1 <- E2 evaluates the expression E1 to produce a value V1 that must be
				//  a box, evaluates E2 to produce an arbitrary value V2, stores V2 in box V1, and returns the special
				//  value unit (which is a legal Jam value, unlike the "undefined" value used in call-by-value recursive
				//  let). If V1 is not a box, then the interpreter generates a run-time error.
				
				System.out.println("\n<-------- forOpGets start");
				varAddress[] arr = envLink.get(0);
				System.out.println("numVarAddresses = " + arr.length);
				for(int i = 0; i < arr.length; i++) {
					System.out.println(i + " " + arr[i]);
					arr[i] = new varAddress(lastIdx-3, lastIdx);
					System.out.println(i + " " + arr[i]);
				}
				
				int temp = lastIdx;
				heap[lastIdx] = -2;
				lastIdx++;
				
				System.out.println("<-------- forOpGets end\n");
				return temp;
			}
		});
	}

	public Integer forApp(App a) {
		// TODO
		System.out.println("Reaches app: " + a);
		return 0;
	}

	public Integer forIf(If i) {
		Integer t = i.test().accept(this);
		if (heap[t] == -3) {
			System.out.println(i.conseq());
			return i.conseq().accept(this);
		} else {
			return i.alt().accept(this);
		}
	}

	public Integer forBlock(Block b) {
		for (int i = 0; i < b.exps().length -1; i++) {
			b.exps()[i].accept(this);
		}
		return b.exps()[b.exps().length - 1].accept(this);
	}

	Integer forDefault(AST a) { throw new EvalException(a + " is not in the domain of the visitor " + getClass()); }
	public Integer forSymVariable(Variable host) { return forDefault(host); }
	public Integer forMap(Map host) { return forDefault(host); }
	public Integer forLet(Let host) { return forDefault(host); }
	public Integer forLetRec(LetRec host) { return forDefault(host); }
	public Integer forLetcc(Letcc host) { return forDefault(host);}
	
}

class SDEvaluator extends Evaluator<SDEnv> implements SDASTVisitor<JamVal> {
  
    SDEvaluator(SDEnv env) { super(env);}
  
	/*  EvalVisitor methods for evaluating SDASTs. */
	public SDASTVisitor<JamVal> newEvalVisitor(SDEnv env) { return new SDEvaluator(env); }
	public JamVal forPair(Pair p)  { return env.lookup(p); }
	
	public JamVal forSMap(SMap sm) {
		return new SDClosure(sm, this);
	}
	
	public JamVal forSLet(SLet sl) {
	/* Extract binding vars and exps (rhs's) from l */
	
	SDAST[] rhss = sl.rhss();
	JamVal[] jArr = new JamVal[rhss.length];
	int n = rhss.length;
	/* Construct newEnv for Let body and exps; vars are bound to values of corresponding exps using newEvalVisitor */
	SDEnv newEnv = env();
	for (int i = n-1; i >= 0; i--) {
	  jArr[i] = rhss[i].accept(this);
	}
	newEnv = newEnv.cons(jArr);
	SDASTVisitor<JamVal> newEvalVisitor = newEvalVisitor(newEnv);
	return sl.body().accept(newEvalVisitor);
	}
	public JamVal forSLetRec(SLetRec slr) {
	SDAST[] rhss = slr.rhss();
	//    SymAST[] exps = l.exps();
	int n = rhss.length;
	/* Construct newEnv for Let body and exps; vars are bound to values of corresponding exps using newEvalVisitor */
	SDEnv newEnv = env();
	
	JamVal[] jArr = new JamVal[n];
	for (int i = n-1; i >= 0; i--) {
	//      bindings[i] = new Binding(vars[i], null);  // bind var[i], setting value to null, which is not a JamVal
	  jArr[i] = null;
	}
	
	newEnv = newEnv.cons(jArr);
	
	SDASTVisitor<JamVal> newEvalVisitor = newEvalVisitor(newEnv);
	
	// fix up the dummy values
	for (int i = 0; i < n; i++)
	  jArr[i] = (rhss[i].accept(newEvalVisitor));  // modifies newEnv and newEvalVisitor
	
	return slr.body().accept(newEvalVisitor);
	}
	
//	/* Methods that are never invoked in the evaluation of well-formed SymASTs */
	public JamVal forSymVariable(Variable host) { return forDefault(host); }
	public JamVal forMap(Map host) { return forDefault(host); }
	public JamVal forLet(Let host) { return forDefault(host); }
	public JamVal forLetRec(LetRec host) { return forDefault(host); }
}


