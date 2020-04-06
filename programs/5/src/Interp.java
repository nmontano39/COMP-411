/** Nine different interpreters for Jam that differ in binding policy and cons evaluation policy.
 * The binding policy is either: call-by-value, call-by-name, or call-by-need.
 * The cons evaluation policy is either: call-by-value (eager), call-by-name (redundant lazy), or
 * call-by-need (efficient lazy).
 *
 *
 *
 *
 */

import org.omg.CORBA.SystemException;

import java.io.IOException;
import java.io.Reader;

/** The basic Jam binding framework, which is extended by static inner classes in class Interp. */
abstract class Binding implements WithVariable {
  Variable var;
  JamVal value;
  Binding(Variable v, JamVal jv) {
    var = v; value = jv;
  }
  public Variable var() { return var; }

  /** Return the value of the binding which may require evaluation. */
  public abstract JamVal value();

  /** Sets the binding to the value of the specified exp evaluated using ev.  The evaluation may be delayed until
   * value() is called. */
  public abstract void setBinding(AST exp, EvalVisitor ev);
}

/** Interpreter Classes */

/** The exception class for Jam run-time errors. */
class EvalException extends RuntimeException {
  EvalException(String msg) { super(msg); }
}

/** The exception class for Jam run-time errors. */
class TypeException extends RuntimeException {
  TypeException(String msg) { super(msg); }
}

/** The visitor interface for interpreting ASTs. */
interface EvalVisitor extends ASTVisitor<JamVal> {

  EvalVisitor newEvalVisitor(PureList<Binding> e);

  /** Returns the environment embedded in this EvalVisitor. */
  PureList<Binding> env();
}

/** The interface supported by various binding evaluation policies: call-by-value, call-by-name, and call-by-need. */
interface BindingPolicy {

  /** Constructs the appropriate binding object for this, binding var to ast in the evaluator ev. */
  Binding newBinding(Variable var, AST ast, EvalVisitor ev);

  /** Constructs the appropriate dummy binding object for this. */
  Binding newDummyBinding(Variable var);
}

/** Interface containing a factory to build the cons object specified by this ConsPolicy. */
interface ConsPolicy {
  /** Constructs the appropriate cons given the arguments and corresponding EvalVisitor. */
  JamVal evalCons(AST[] args, EvalVisitor ev);
}

/** Interface for classes with a variable field (Variable and the various Binding classes). */
interface WithVariable {
  /** Accessor for the variable. */
  Variable var();
}

/** A lookup visitor class that returns element matching the embedded var. If no match found, returns null. */
class LookupVisitor<ElemType extends WithVariable> implements PureListVisitor<ElemType, ElemType> {
  /** Variable to look up. */
  Variable var;

  // Invariant: the lexer guarantees that there is only one Variable for a given name

  LookupVisitor(Variable v) { var = v; }

  /** Case for empty lists. */
  public ElemType forEmpty(Empty<ElemType> e) { return null; }

  /** Case for non-empty lists. */
  public ElemType forCons(Cons<ElemType> c) {
    ElemType e = c.first();
    if (var == e.var()) return e;
    return c.rest().accept(this);
  }
}

/** Interpreter class supporting nine forms of evaluation for Jam programs.  These forms of evaluation differ in
 * binding policy and cons evaluation policy.
 * The binding policy is either: call-by-value, call-by-name, or call-by-need.
 * The cons evaluation policy is either: call-by-value (eager), call-by-name (redundant lazy), or
 * call-by-need (efficient lazy). . */
class Interpreter {
  /** Parser to use. */
  Parser parser;  // initialized in constructors

  /** Parsed AST. */
  AST prog;       // initialized in constructors

  Interpreter(String fileName) throws IOException {
    parser = new Parser(fileName);
    prog = parser.parseAndCheck();
  }

  Interpreter(Parser p) {
    parser = p;
    prog = parser.parseAndCheck();
  }

  Interpreter(Reader reader) {
    parser = new Parser(reader);
    prog = parser.parseAndCheck();
  }

  static JamVal illegalForwardReference(Variable v) {
    throw new EvalException("Attempt to evaluate variable " + v + " bound to null, indicating an illegal forward reference");
  }

  static class ValueBinding extends Binding {
    ValueBinding(Variable v, JamVal jv) { super(v, jv); }
    public JamVal value() {
      if (value == null) illegalForwardReference(var());
      return value;
    }
    public void setBinding(AST exp, EvalVisitor ev) { value = exp.accept(ev); }  // immediate evaluation
    public String toString() { return "[" + var + ", " + value + "]"; }
  }

  /* Inner classes */
  static class NameBinding extends Binding {
    protected Suspension susp;
    NameBinding(Variable v, Suspension s) {
      super(v,null);
      susp = s;
    }
    public JamVal value() { return (susp == null) ? illegalForwardReference(var()) : susp.eval(); }
    public void setBinding(AST exp, EvalVisitor ev) { susp = new Suspension(exp, ev); }
    public String toString() { return "[" + var + ", " + susp + "]"; }
  }

  static class NeedBinding extends NameBinding {
    NeedBinding(Variable v, Suspension s) { super(v,s); }
    public JamVal value() {
      if (value == null) {  // a legitimate JamVal CANNOT be null
        if (susp == null) illegalForwardReference(var());
        else {             // Force the suspension and cache its value
          value = susp.eval();
          susp = null;     // release susp object for GC!
        }
      }
      return value;
    }
    public void setBinding(AST exp, EvalVisitor ev) { susp = new Suspension(exp, ev); value = null; }
    public String toString() { return "[" + var + ", " + value + ", " + susp + "]"; }
  }

  /** Parses and ValueValue interprets the input embeded in parser, returning the result. */
  public JamVal callByValue() { return prog.accept(valueValueVisitor); }

  /** Parses and NameValue interprets the input embeded in parser, returning the result. */
  public JamVal callByName() { return prog.accept(nameValueVisitor); }

  /** Parses and NeedValue interprets the input embeded in parser, returning the result. */
  public JamVal callByNeed() { return prog.accept(needValueVisitor); }

  /** Parses and ValueValue interprets the input embeded in parser, returning the result. */
  public JamVal valueValue() { return prog.accept(valueValueVisitor); }

  /** Parses and ValueName interprets the input embeded in parser, returning the result. */
  public JamVal valueName() { return prog.accept(valueNameVisitor); }

  /** Parses and ValueNeed interprets the input embeded in parser, returning the result. */
  public JamVal valueNeed() {return prog.accept(valueNeedVisitor); }

  /** Parses and NameValue interprets the input embeded in parser, returning the result.  */
  public JamVal nameValue() { return prog.accept(nameValueVisitor); }

  /** Parses and NameName interprets the input embeded in parser, returning the result. */
  public JamVal nameName() { return prog.accept(nameNameVisitor); }

  /** Parses and NameNeed interprets the input embeded in parser, returning the result. */
  public JamVal nameNeed() { return prog.accept(nameNeedVisitor); }

  /** Parses and NeedValue interprets the input embeded in parser, returning the result. */
  public JamVal needValue() { return prog.accept(needValueVisitor); }

  /** Parses and NeedName interprets the input embeded in parser, returning the result. */
  public JamVal needName() { return prog.accept(needNameVisitor); }

  /** Parses and NeedNeed interprets the input embeded in parser, returning the result. */
  public JamVal needNeed() { return prog.accept(needNeedVisitor); }

  /** Parses and NeedName interprets the input embeded in parser, returning the result. */
  public JamVal eagerEval() { return prog.accept(valueValueVisitor); }

  /** Parses and NeedNeed interprets the input embeded in parser, returning the result. */
  public JamVal lazyEval() { return prog.accept(valueNameVisitor); }

  /** Binding policy for call-by-value. */
  static final BindingPolicy CALL_BY_VALUE = new BindingPolicy() {
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new ValueBinding(var, arg.accept(ev)); }
    public Binding newDummyBinding(Variable var) { return new ValueBinding(var, null); } // null indicates still unbound
  };

  /** Binding policy for call-by-name. */
  static final BindingPolicy CALL_BY_NAME = new BindingPolicy() {
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) {
      return new NameBinding(var, new Suspension(arg, ev));
    }
    public Binding newDummyBinding(Variable var) { return new NameBinding(var, null); } // null indicates still unbound
  };

  /** Binding policy for call-by-need. */
  static final BindingPolicy CALL_BY_NEED = new BindingPolicy() {
    public Binding newBinding(Variable var, AST arg, EvalVisitor ev) {
      return new NeedBinding(var, new Suspension(arg, ev));
    }
    public Binding newDummyBinding(Variable var) { return new NeedBinding(var, null); } // null indicates still unbound
  };

  /** Eager cons evaluation policy. presume that args has exactly 2 elements. */
  public static final ConsPolicy EAGER = new ConsPolicy() {
    public JamVal evalCons(AST[] args, EvalVisitor ev) {
      JamVal val0 = args[0].accept(ev);
      JamVal val1 = args[1].accept(ev);
      if (val1 instanceof JamList) {
        return new JamCons(val0, (JamList)val1);
      }
      throw new EvalException("Second argument " + val1 + " to `cons' is not a JamList");
    }
  };

  /** Call-by-name lazy cons evaluation policy. */
  public static final ConsPolicy LAZYNAME = new ConsPolicy() {
    public JamVal evalCons(AST[] args, EvalVisitor ev) { return new JamLazyNameCons(args[0], args[1], ev); }
  };

  /** Call-by-need lazy cons evaluation policy. */
  public static final ConsPolicy LAZYNEED = new ConsPolicy() {
    public JamVal evalCons(AST[] args, EvalVisitor ev) { return new JamLazyNeedCons(args[0], args[1], ev); }
  };

  /** Value-value visitor. */
  static final ASTVisitor<JamVal> valueValueVisitor = new Evaluator(CALL_BY_VALUE, EAGER);

  /** Value-name visitor. */
  static final ASTVisitor<JamVal> valueNameVisitor = new Evaluator(CALL_BY_VALUE, LAZYNAME);

  /** Value-need visitor. */
  static final ASTVisitor<JamVal> valueNeedVisitor = new Evaluator(CALL_BY_VALUE, LAZYNEED);

  /** Name-value visitor. */
  static final ASTVisitor<JamVal> nameValueVisitor = new Evaluator(CALL_BY_NAME, EAGER);

  /** Name-name visitor. */
  static final ASTVisitor<JamVal> nameNameVisitor = new Evaluator(CALL_BY_NAME, LAZYNAME);

  /** Name-need visitor. */
  static final ASTVisitor<JamVal> nameNeedVisitor = new Evaluator(CALL_BY_NAME, LAZYNEED);

  /** Need-value visitor. */
  static final ASTVisitor<JamVal> needValueVisitor = new Evaluator(CALL_BY_NEED, EAGER);

  /** Need-name visitor. */
  static final ASTVisitor<JamVal> needNameVisitor = new Evaluator(CALL_BY_NEED, LAZYNAME);

  /** Need-need visitor. */
  static final ASTVisitor<JamVal> needNeedVisitor = new Evaluator(CALL_BY_NEED, LAZYNEED);

}

/** Primary visitor class for performing interpretation. */
class Evaluator implements EvalVisitor {

  /* Assumes that:
   *   OpTokens are unique
   *   Variable objects are unique: v1.name.equals(v.name) => v1 == v2
   *   Only objects used as boolean values are BoolConstant.TRUE and BoolConstant.FALSE
   * Hence, == can be used to compare Variable objects, OpTokens, and BoolConstants
   */

  /** Environment. */
  PureList<Binding> env;

  /** Policy to create bindings. */
  BindingPolicy bindingPolicy;

  /** Policy to create cons. */
  ConsPolicy consPolicy;

  private Evaluator(PureList<Binding> e, BindingPolicy bp, ConsPolicy cp) {
    env = e;
    bindingPolicy = bp;
    consPolicy = cp;
  }

  public Evaluator(BindingPolicy bp, ConsPolicy cp) { this(new Empty<Binding>(), bp, cp); }

  /* EvalVisitor methods */

  /** Factory method that constructs a visitor similar to this with environment env. */
  public EvalVisitor newEvalVisitor(PureList<Binding> env) {
    return new Evaluator(env, bindingPolicy, consPolicy);
  }

  /** Getter for env field. */
  public PureList<Binding> env() { return env; }

  public JamVal forBoolConstant(BoolConstant b) { return b; }
  public JamVal forIntConstant(IntConstant i) { return i; }
  public JamVal forNullConstant(NullConstant n) {

    // TODO: allow for typed null
    // null : int    returns ()
    // null : bool   returns ()

    return JamEmpty.ONLY;

  }

  public JamVal forVariable(Variable v) {
    Binding match = env.accept(new LookupVisitor<Binding>(v));
    if (match == null) {
      throw new EvalException("variable " + v + " is unbound");
    }
    return match.value();
  }

  public JamVal forPrimFun(PrimFun f) { return f; }
  public JamVal forUnOpApp(UnOpApp u) { return u.rator().accept(new UnOpEvaluator(u.arg().accept(this))); }
  public JamVal forBinOpApp(BinOpApp b) { return b.rator().accept(new BinOpEvaluator(b.arg1(), b.arg2())); }

  public JamVal forApp(App a) {
    JamVal rator = a.rator().accept(this);
    if (rator instanceof JamFun)
      return ((JamFun)rator).accept(new FunEvaluator(a.args()));
    throw new EvalException(rator + " appears at head of application " + a + " but it is not a valid function");
  }

  public JamVal forMap(Map m) { return new JamClosure(m, env); }

  public JamVal forIf(If i) {
    JamVal test = i.test().accept(this);
    if (!(test instanceof BoolConstant))  throw new EvalException("non Boolean " + test + " used as test in if");
    if (test == BoolConstant.TRUE)  return i.conseq().accept(this);
    return i.alt().accept(this);
  }

  /* recursive let semantics */
  public JamVal forLet(Let l) {

    /* Extract binding vars and exps (rhs's) from l */
    Variable[] vars = l.vars();
    AST[] exps = l.exps();
    int n = vars.length;

    /* Construct newEnv for Let body and exps; vars are bound to values of corresponding exps using newEvalVisitor. */
    PureList<Binding> newEnv = env();
    Binding[] bindings = new Binding[n];
    for(int i = n - 1; i >= 0; i--) {
      bindings[i] = bindingPolicy.newDummyBinding(vars[i]);  // bind var[i] to illegal value null
      newEnv = newEnv.cons(bindings[i]);                     // add new Binding to newEnv; it is shared!
    }
    EvalVisitor newEV = newEvalVisitor(newEnv);

    /* Fix up the dummy values. */
    for(int i = 0; i < n; i++) {
      System.out.println(exps[i]);
      bindings[i].setBinding(exps[i], newEV);  // modifies newEnv and newEvalVisitor
    }
    return l.body().accept(newEV);
  }

  // forBlock
  public JamVal forBlock(Block b) {

    JamVal jv = null;

    // set list of expressions
    AST[] exps = b.exps();
    int n = exps.length;
    // evaluate each expression
    for (AST exp : exps) {
      jv = exp.accept(this);
    }
    // return the rightmost expresssion
    return jv;
  }

  /* Inner classes */

  /** Function evaluator. */
  class FunEvaluator implements JamFunVisitor<JamVal> {
    /** Unevaluated arguments */
    AST[] args;

    FunEvaluator(AST[] asts) { args = asts; }

    /* Support for JamFunVisitor<JamVal> interface */

    public JamVal forJamClosure(JamClosure closure) {
      Map map = closure.body();
      int n = args.length;
      Variable[] vars = map.vars();
      if (vars.length != n) {
        throw new EvalException("closure " + closure + " applied to " + n + " arguments");
      }
      /* Construct newEnv for JamClosure body using JamClosure env. */
      PureList<Binding> newEnv = closure.env();
      for(int i = n - 1; i >= 0; i--) {
        newEnv = newEnv.cons(bindingPolicy.newBinding(vars[i], args[i], Evaluator.this));
      }
      return map.body().accept(newEvalVisitor(newEnv));
    }
    public JamVal forPrimFun(PrimFun primFun) { return primFun.accept(primEvaluator); }

    /** Evaluator for PrimFuns. */
    PrimFunVisitor<JamVal> primEvaluator = new PrimFunVisitor<JamVal>() {
      /** Evaluates args using evaluation visitor in whose closure this object is. */
      private JamVal[] evalArgs() {
        int n = args.length;
        JamVal[] vals = new JamVal[n];
        for(int i = 0; i < n; i++) {
          vals[i] = args[i].accept(Evaluator.this);
        }
        return vals;
      }

      /** Throws an error.*/
      private void primFunError(String fn) {
        throw new EvalException("Primitive function `" + fn + "' applied to " + args.length + " arguments");
      }

      /** Evaluates an argument that has to be a Jam cons. */
      private JamCons evalJamConsArg(AST arg, String fun) {
        JamVal val = arg.accept(Evaluator.this);
        if (val instanceof JamCons) {
          return (JamCons)val;
        }
        throw new
                EvalException("Primitive function `" + fun + "' applied to argument " + val + " that is not a JamCons");
      }

      //TODO: removed for p5

      /* Visitor methods. */
//      public JamVal forFunctionPPrim() {
//        JamVal[] vals = evalArgs();
//        if (vals.length != 1) primFunError("function?");
//        return BoolConstant.toBoolConstant(vals[0] instanceof JamFun);
//      }

//      public JamVal forNumberPPrim() {
//        JamVal[] vals = evalArgs();
//        if (vals.length != 1) primFunError("number?");
//        return BoolConstant.toBoolConstant(vals[0] instanceof IntConstant);
//      }

//      public JamVal forListPPrim() {
//        JamVal[] vals = evalArgs();
//        if (vals.length != 1) primFunError("list?");
//        return BoolConstant.toBoolConstant(vals[0] instanceof JamList);
//      }

      public JamVal forConsPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) primFunError("cons?");
        return BoolConstant.toBoolConstant(vals[0] instanceof JamCons);
      }

      public JamVal forNullPPrim() {
        JamVal[] vals = evalArgs();
        if (vals.length != 1) primFunError("null?");
        return BoolConstant.toBoolConstant(vals[0] instanceof JamEmpty);
      }

//      public JamVal forRefPPrim() {
//        JamVal[] vals = evalArgs();
//        if (vals.length != 1) primFunError("ref?");
//        return BoolConstant.toBoolConstant(vals[0] instanceof JamBox);
//      }

      public JamVal forConsPrim() {
        if (args.length != 2) primFunError("cons");
        return consPolicy.evalCons(args, Evaluator.this);   // Evaluation strategy determined by consEp
      }

//      public JamVal forArityPrim() {
//        JamVal[] vals = evalArgs();
//        if (vals.length != 1) primFunError("arity");
//        if (!(vals[0] instanceof JamFun))  throw new EvalException("arity applied to argument " +  vals[0]);
//
//        return ((JamFun)vals[0]).accept(new JamFunVisitor<IntConstant>() {
//          public IntConstant forJamClosure(JamClosure jc) { return new IntConstant(jc.body().vars().length); }
//          public IntConstant forPrimFun(PrimFun jpf) { return new IntConstant(jpf instanceof ConsPrim ? 2 : 1); }
//        });
//      }

      public JamVal forFirstPrim() {
        if (args.length != 1) primFunError("first");
        return evalJamConsArg(args[0], "first").first();
      }
      public JamVal forRestPrim() {
        if (args.length != 1) primFunError("rest");
        return evalJamConsArg(args[0], "rest").rest();
      }
    };
  }

  /** Evaluator for unary operators. */
  static class UnOpEvaluator implements UnOpVisitor<JamVal> {
    /** Value of the operand. */
    private JamVal val;

    UnOpEvaluator(JamVal jv) { val = jv; }

    /** Returns the value of the operand if it is an IntConstant; otherwise throw an exception. */
    private IntConstant checkInteger(UnOp op) {
      if (val instanceof IntConstant)  return (IntConstant)val;
      throw new EvalException("Unary operator `" + op + "' applied to non-integer " + val);
    }

    /** Returns the value of the operand if it is a BoolConstant; otherwise throw an exception. */
    private BoolConstant checkBoolean(UnOp op) {
      if (val instanceof BoolConstant)  return (BoolConstant)val;
      throw new EvalException("Unary operator `" + op + "' applied to non-boolean " + val);
    }

    private JamVal checkReference(UnOp op) {
        if (val instanceof JamBox) return ((JamBox) val).getValue();
        throw new EvalException("Unary operator `" + op + "' applied to non-reference (box) " + val);
    }

    /* Visitor methods */
    public JamVal forUnOpPlus(UnOpPlus op) { return checkInteger(op); }
    public JamVal forUnOpMinus(UnOpMinus op) { return new IntConstant(-checkInteger(op).value()); }
    public JamVal forOpTilde(OpTilde op) { return checkBoolean(op).not(); }
    public JamVal forOpBang(OpBang op) {return checkReference(op);}
    public JamVal forOpRef(OpRef op) {return new JamBox(val);}
  }

  /** Evaluator for binary operators. */
  class BinOpEvaluator implements BinOpVisitor<JamVal> {
    /** Unevaluated arguments. */
    private AST arg1, arg2;

    BinOpEvaluator(AST a1, AST a2) {
      arg1 = a1;
      arg2 = a2;
    }

    /** Returns the value of arg if it is an IntConstant; otherwise throw an exception, */
    private IntConstant evalIntegerArg(AST arg, BinOp b) {
      JamVal val = arg.accept(Evaluator.this);
      if (val instanceof IntConstant)  return (IntConstant)val;
      throw new EvalException("Binary operator `" + b + "' applied to non-integer " + val);
    }

    /** Returns the value of the argument if it is a BoolConstant, otherwise throw an exception, */
    private BoolConstant evalBooleanArg(AST arg, BinOp b) {
      JamVal val = arg.accept(Evaluator.this);
      if (val instanceof BoolConstant)  return (BoolConstant)val;
      throw new EvalException("Binary operator `" + b + "' applied to non-boolean " + val);
    }

    private JamBox evalRefArg(AST arg, BinOp b) {
        JamVal val = arg.accept(Evaluator.this);
        if (val instanceof JamBox) return (JamBox) val;
        throw new EvalException("Binary operator `" + b + "' applied to non-reference (box) " + val);
    }

    /* Visitor methods */
    public JamVal forBinOpPlus(BinOpPlus op) {
      return new IntConstant(evalIntegerArg(arg1, op).value() + evalIntegerArg(arg2, op).value());
    }

    public JamVal forBinOpMinus(BinOpMinus op) {
      return new IntConstant(evalIntegerArg(arg1, op).value() - evalIntegerArg(arg2, op).value());
    }

    public JamVal forOpTimes(OpTimes op) {
      return new IntConstant(evalIntegerArg(arg1, op).value() * evalIntegerArg(arg2, op).value());
    }

    public JamVal forOpDivide(OpDivide op) {
      int divisor = evalIntegerArg(arg2, op).value();
      if (divisor == 0) {
        throw new EvalException("Attempt to divide by zero");
      }
      return new IntConstant(evalIntegerArg(arg1, op).value() / divisor);
    }

    public JamVal forOpEquals(OpEquals op) {
      return BoolConstant.toBoolConstant(arg1.accept(Evaluator.this).equals(arg2.accept(Evaluator.this)));
    }

    public JamVal forOpNotEquals(OpNotEquals op) {
      return BoolConstant.toBoolConstant(!arg1.accept(Evaluator.this).equals(arg2.accept(Evaluator.this)));
    }

    public JamVal forOpLessThan(OpLessThan op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1, op).value() < evalIntegerArg(arg2, op).value());
    }

    public JamVal forOpGreaterThan(OpGreaterThan op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1, op).value() > evalIntegerArg(arg2, op).value());
    }

    public JamVal forOpLessThanEquals(OpLessThanEquals op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1, op).value() <= evalIntegerArg(arg2, op).value());
    }

    public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
      return BoolConstant.toBoolConstant(evalIntegerArg(arg1, op).value() >= evalIntegerArg(arg2, op).value());
    }

    public JamVal forOpAnd(OpAnd op) {
      BoolConstant b1 = evalBooleanArg(arg1, op);
      if (b1 == BoolConstant.FALSE)  return BoolConstant.FALSE;
      return evalBooleanArg(arg2, op);
    }

    public JamVal forOpOr(OpOr op) {
      BoolConstant b1 = evalBooleanArg(arg1, op);
      if (b1 == BoolConstant.TRUE) return BoolConstant.TRUE;
      return evalBooleanArg(arg2, op);
    }

    public JamVal forOpGets(OpGets op) {
      JamBox left = evalRefArg(arg1, op);
      JamVal right = arg2.accept(Evaluator.this);
      left.setValue(right);
      return Unit.ONLY;
    }
  }
}
