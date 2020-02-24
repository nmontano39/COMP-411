/** Call-by-value, Call-by-name, and Call-by-need Jam interpreter */

import java.io.*;
import java.util.ArrayList;

/** Interpreter Classes */

/** A visitor interface for interpreting Jam AST's */
interface EvalVisitor extends ASTVisitor<JamVal> {
    /** Constructs a new visitor of same class with specified environment e. */
    EvalVisitor newVisitor(PureList<Binding> e);
    PureList<Binding> env();
    Binding newBinding(Variable var, AST ast);
    String consConv();
}

/** A class that implements call-by-value, call-by-name, and call-by-need interpretation of Jam programs. */
class Interpreter {

    Parser parser = null;

    /** Constructor for a program given in a file. */
    Interpreter(String fileName) throws IOException { parser = new Parser(fileName); }

    /** Constructor for a program already embedded in a Parser object. */
    Interpreter(Parser p) { parser = p; }

    /** Constructor for a program embedded in a Reader. */
    Interpreter(Reader reader) { parser = new Parser(reader); }

    /** Parses and CBV interprets the input embeded in parser */
    public JamVal callByValue(String cc) {
        AST prog = parser.parse();
        return prog.accept(new FlexEvalVisitor(CallByValue.ONLY, cc));
    }

    /** Parses and CBNm interprets the input embeded in parser */
    public JamVal callByName(String cc) {
        AST prog = parser.parse();
        return prog.accept(new FlexEvalVisitor(CallByName.ONLY, cc));
    }

    /** Parses and CBNd interprets the input embeded in parser */
    public JamVal callByNeed(String cc) {
        AST prog = parser.parse();
        return prog.accept(new FlexEvalVisitor(CallByNeed.ONLY, cc));
    }

    /** CBV Interprets prog with respect to symbols in lexer */
    public JamVal callByValue(AST prog, String cc) { return prog.accept(new FlexEvalVisitor(CallByValue.ONLY, cc)); }

    /** CBName Interprets prog with respect to symbols in lexer */
    public JamVal callByName(AST prog, String cc) { return prog.accept(new FlexEvalVisitor(CallByName.ONLY, cc)); }

    /** CBNeed Interprets prog with respect to symbols in lexer */
    public JamVal callByNeed(AST prog, String cc) { return prog.accept(new FlexEvalVisitor(CallByNeed.ONLY, cc)); }

    /** A class representing an unevaluated expresssion (together with the corresponding evaluator). */
    static class Suspension {
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

    /** Class representing a binding in CBV evaluation. */
    static class ValueBinding extends Binding {
        ValueBinding(Variable v, JamVal jv) { super(v, jv); }
        public String toString() { return "[" + var + ", " + value + "]"; }
    }

    /** Class representing a binding in CBNm evaluation. */
    static class NameBinding extends Binding {
        protected Suspension susp;
        NameBinding(Variable v, Suspension s) {
            super(v, null);
            susp = s;
        }
        public JamVal value() { return susp.eval(); }
        public String toString() { return "[" + var + ", " + susp + "]"; }
    }

    /** Class representing a binding in CBNd evaluation. */
    static class NeedBinding extends NameBinding {
        NeedBinding(Variable v, Suspension s) { super(v,s); }
        public JamVal value() {
            if (value == null) {  // a legitimate JamVal CANNOT be null
                setValue(susp.eval());
                susp = null;  // release susp object for GC!
            }
            return value;
        }
        public String toString() { return "[" + var + ", " + value + ", " + susp + "]"; }
    }



    // TODO NameCons

    /** Class representing a cons in call by name convention */
    static class NameCons extends JamCons {
        Suspension sus;
        PureList<Suspension> susCons;

        public NameCons(Suspension s, PureList<Suspension> sCons) {
            super(null, null);
            sus = s;
            susCons = sCons;
        }

        public JamVal first() {
//            sus.putEv();
//            System.out.println(sus.exp());
            return sus.exp().accept(sus.ev());
        }

        public JamList rest() {
            return susCons.accept(new PureListVisitor<Suspension, JamList>() {
                @Override
                public JamList forEmpty(Empty<Suspension> e) {
                    // Rest on a list with element returns an empty list
                    return JamEmpty.ONLY;
                }

                @Override
                public JamList forCons(Cons<Suspension> c) {
                    JamVal restJamVal = c.first().exp().accept(c.first().ev());
                    if (restJamVal instanceof JamList) {
                        return (JamList) restJamVal;
                    }
                    throw new EvalException("Rest is not a JamList");
                }
            });
        }

        public String toString() {
            NameCons temp = new NameCons(susCons.);
            return "(" + sus.eval() + rest.toStringHelp() + ")";
        }

        public String toStringHelp() {
            JamVal firstVal = sus.eval();
            JamVal restVal = susCons.accept(new PureListVisitor<Suspension, JamVal>() {
                @Override
                public JamVal forEmpty(Empty<Suspension> e) {
                    return JamEmpty.ONLY;
                }

                @Override
                public JamVal forCons(Cons<Suspension> c) {
                    return c.first().eval();
                }
            });
            if (((JamList) restVal).equals(JamEmpty.ONLY)) {
                return "" + firstVal;
            } else {
                System.out.println(firstVal);
                return firstVal + " " + restVal;
            }

        }
    }

//        public String toString() {
//            return "(" + toStringHelper() + ")";
//        }



    // TODO NeedCons


    /** Class representing a cons in call by need convention */
    static class NeedCons extends JamCons {
        Suspension sus;
        PureList<Suspension> susCons;

        public NeedCons(Suspension s, PureList<Suspension> sCons) {
            super(null, null);
            sus = s;
            susCons = sCons;
        }

        public JamVal first() {
            return null;
        }

        public JamList rest() {
            return null;
        }
    }



    /** Visitor class implementing a lookup method on environments.
     * @return value() for variable var for both lazy and eager environments. */
    static class LookupVisitor implements PureListVisitor<Binding,JamVal> {

        Variable var;   // the lexer guarantees that there is only one Variable for a given name

        LookupVisitor(Variable v) { var = v; }

        /* Visitor methods. */
        public JamVal forEmpty(Empty<Binding> e) { throw new SyntaxException("variable " + var + " appears free in this expression"); }

        public JamVal forCons(Cons<Binding> c) {
            Binding b = c.first();
            if (var == b.var()) return b.value();
            return c.rest().accept(this);
        }
    }

    /** The interface supported by various evaluation policies (CBV, CBNm, CBNd) for map applications and variable
     * lookups. The EvalVisitor parameter appears in each method because the environment is carried by an EvalVisitor.
     * Hence as an EvalPolicy is used to interpret an expression, The passed EvalVisitor will change (!) as the
     * environment changes.  An EvalPolicy should NOT contain an EvalVisitor field.
     */
    interface EvalPolicy {
        /** Evaluates the let construct composed of var, exps, and body */
        JamVal letEval(Variable[] vars, AST[] exps, AST body, EvalVisitor ev);

        /** Constructs a UnOpVisitor with the specified evaluated argument */
        UnOpVisitor<JamVal> newUnOpVisitor(JamVal arg);

        /** Constructs a BinOpVisitor with the specified unevaluated arguments and EvalVisitor */
        BinOpVisitor<JamVal> newBinOpVisitor(AST arg1, AST arg2, EvalVisitor ev);

        /** Constructs a JamFunVisitor with the specified array of unevaluated arguments and EvalVisitor */
        JamFunVisitor<JamVal> newFunVisitor(AST args[], EvalVisitor ev);

        /** Constructs the appropriate binding object for this, binding var to ast in the evaluator ev */
        Binding newBinding(Variable var, AST ast, EvalVisitor ev);
    }

    /** An EvalVisitor class where details of behavior are determined by an embedded EvalPolicy. */
    static class FlexEvalVisitor implements EvalVisitor {

        /* The code in this class assumes that:
         * * OpTokens are unique;
         * * Variable objects are unique: v1.name.equals(v.name) => v1 == v2; and
         * * The only objects used as boolean values are BoolConstant.TRUE and BoolConstant.FALSE.
         * Hence,  == can be used to compare Variable objects, OpTokens, and BoolConstants. */

        PureList<Binding> env;  // the embdedded environment
        EvalPolicy evalPolicy;  // the embedded EvalPolicy
        String consConv;        // cons convention

        /** Recursive constructor. */
        private FlexEvalVisitor(PureList<Binding> e, EvalPolicy ep, String cc) {
            env = e;
            evalPolicy = ep;
            consConv = cc;
        }

        /** Top level constructor. */
        public FlexEvalVisitor(EvalPolicy ep, String cc) { this(new Empty<Binding>(), ep, cc); }

        /** factory method that constructs a new visitor of This class with environment env */
        public EvalVisitor newVisitor(PureList<Binding> e) { return new FlexEvalVisitor(e, evalPolicy, consConv); }

        /** Factory method that constructs a Binding of var to ast corresponding to this */
        public Binding newBinding(Variable var, AST ast) { return evalPolicy.newBinding(var, ast, this); }

        /** Getter for env field */
        public PureList<Binding> env() { return env; }

        /** Getter for conv field */
        public String consConv() { return consConv; }

        /* EvalVisitor methods */
        public JamVal forBoolConstant(BoolConstant b) { return b; }
        public JamVal forIntConstant(IntConstant i) { return i; }
        public JamVal forNullConstant(NullConstant n) { return JamEmpty.ONLY; }
        public JamVal forVariable(Variable v) {
            //System.out.println(v);
            return env.accept(new LookupVisitor(v));
        }

        public JamVal forPrimFun(PrimFun f) {
            //System.out.println(f);
            return f; }

        public JamVal forUnOpApp(UnOpApp u) {
            return u.rator().accept(evalPolicy.newUnOpVisitor(u.arg().accept(this)));
        }

        public JamVal forBinOpApp(BinOpApp b) {
            return b.rator().accept(evalPolicy.newBinOpVisitor(b.arg1(), b.arg2(), this));
        }

        public JamVal forApp(App a) {
            JamVal rator = a.rator().accept(this);
            if (rator instanceof JamFun) return ((JamFun) rator).accept(evalPolicy.newFunVisitor(a.args(), this));
            throw new EvalException(rator + " appears at head of application " + a + " but it is not a valid function");
        }

        public JamVal forMap(Map m) {
            //System.out.println(m.body());
            return new JamClosure(m,env); }

        public JamVal forIf(If i) {
            JamVal test = i.test().accept(this);
            if (! (test instanceof BoolConstant)) throw new EvalException("non Boolean " + test + " used as test in if");
            if (test == BoolConstant.TRUE) return i.conseq().accept(this);
            return i.alt().accept(this);
        }

        public JamVal forLet(Let l) {


            // TODO: Make recursive let


            Def[] defs = l.defs();
            int n = defs.length;

            Variable[] vars = new Variable[n];
            for (int i = 0; i < n; i++) vars[i] = defs[i].lhs();

            AST[] exps =  new AST[n];
            for (int i = 0; i < n; i++) exps[i] = defs[i].rhs();

            return evalPolicy.letEval(vars, exps, l.body(), this);
        }
    }

    /** Class that implements the evaluation of function applications given the embedded arguments and evalVisitor. */
    static class StandardFunVisitor implements JamFunVisitor<JamVal> {

        /** Unevaluated arguments */
        AST[] args;

        /** Evaluation visitor */
        EvalVisitor evalVisitor;

        /** PrimFun visitor */
        PrimFunVisitorFactory primFunFactory;

        StandardFunVisitor(AST[] asts, EvalVisitor ev, PrimFunVisitorFactory pff) {
            args = asts;
            evalVisitor = ev;
            primFunFactory = pff;
        }

        /* Visitor methods. */
        public JamVal forJamClosure(JamClosure closure) {
            Map map = closure.body();

            int n = args.length;
            Variable[] vars = map.vars();
            if (vars.length != n)
                throw new EvalException("closure " + closure + " applied to " + n + " arguments");

            // construct newEnv for JamClosure body using JamClosure env
            PureList<Binding> newEnv = closure.env();
            for (int i = n-1; i >= 0; i--) {
                newEnv = newEnv.cons(evalVisitor.newBinding(vars[i], args[i]));
            }

            return map.body().accept(evalVisitor.newVisitor(newEnv));
        }

        public JamVal forPrimFun(PrimFun primFun) {
            int n = args.length;
      /* JamVal[] vals = new JamVal[n];
       for (int i = 0; i < n; i++)
       vals[i] = args[i].accept(evalVisitor); */
            return primFun.accept(primFunFactory.newVisitor(evalVisitor, args));
        }
    }

    abstract static class CommonEvalPolicy implements EvalPolicy {

        public JamVal letEval(Variable[] vars, AST[] exps, AST body, EvalVisitor evalVisitor) {
            /* let semantics */

            int n = vars.length;

            ArrayList<Variable> varList = new ArrayList<>();


            // construct newEnv for Let body; vars are bound to values of corresponding exps using evalVisitor
            PureList<Binding> newEnv = evalVisitor.env();
            for (int i = n-1; i >= 0; i--) {
                if (varList.contains(vars[i])) {
                    throw new SyntaxException("Variable" + vars[i] + " declared more than once in let");
                }

                varList.add(vars[i]);
                newEnv = newEnv.cons(evalVisitor.newBinding(vars[i], exps[i]));
            }

            EvalVisitor newEvalVisitor = evalVisitor.newVisitor(newEnv);

            return body.accept(newEvalVisitor);
        }

        public UnOpVisitor<JamVal> newUnOpVisitor(JamVal arg) {
            return new StandardUnOpVisitor(arg);
        }
        public BinOpVisitor<JamVal> newBinOpVisitor(AST arg1, AST arg2, EvalVisitor ev) {
            return new StandardBinOpVisitor(arg1, arg2, ev);
        }

        public JamFunVisitor<JamVal> newFunVisitor(AST[] args, EvalVisitor ev) {
            return new StandardFunVisitor(args, ev, StandardPrimFunFactory.ONLY);
        }
    }

    static class CallByValue extends CommonEvalPolicy {

        public static final CallByValue ONLY = new CallByValue();
        private CallByValue() { }

        /** Inherited letEval works because newBinding method is customized! */

        /** Constructs binding of var to value of arg in ev */
        public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new ValueBinding(var, arg.accept(ev)); }
    }

    static class CallByName extends CommonEvalPolicy {
        public static final CallByName ONLY = new CallByName();
        private CallByName() {}

        /** Inherited letEval works because newBinding method is customized! */

        public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NameBinding(var, new Suspension(arg, ev)); }
    }

    static class CallByNeed extends CommonEvalPolicy {
        public static final CallByNeed ONLY = new CallByNeed();
        private CallByNeed() {}

        /** Inherited letEval works because newBinding method is customized! */

        public Binding newBinding(Variable var, AST arg, EvalVisitor ev) { return new NeedBinding(var, new Suspension(arg, ev)); }
    }

    static class StandardUnOpVisitor implements UnOpVisitor<JamVal> {
        private JamVal val;
        StandardUnOpVisitor(JamVal jv) { val = jv; }

        private IntConstant checkInteger(UnOp op) {
            if (val instanceof IntConstant) return (IntConstant) val;
            throw new EvalException("Unary operator `" + op + "' applied to non-integer " + val);
        }

        private BoolConstant checkBoolean(UnOp op) {
            if (val instanceof BoolConstant) return (BoolConstant) val;
            throw new EvalException("Unary operator `" + op + "' applied to non-boolean " + val);
        }

        public JamVal forUnOpPlus(UnOpPlus op) { return checkInteger(op); }
        public JamVal forUnOpMinus(UnOpMinus op) {
            return new IntConstant(- checkInteger(op).value());
        }
        public JamVal forOpTilde(OpTilde op) { return checkBoolean(op).not(); }
        // public JamVal forOpBang(OpBang op) { return ... ; }  // Supports addition of ref cells to Jam
        // public JamVal forOpRef(OpRef op) { return ... ; }    // Supports addition of ref cells to Jam
    }

    static class StandardBinOpVisitor implements BinOpVisitor<JamVal> {
        private AST arg1, arg2;
        private EvalVisitor evalVisitor;

        StandardBinOpVisitor(AST a1, AST a2, EvalVisitor ev) { arg1 = a1; arg2 = a2; evalVisitor = ev; }

        private IntConstant evalIntegerArg(AST arg, BinOp b) {
            JamVal val = arg.accept(evalVisitor);
            if (val instanceof IntConstant) return (IntConstant) val;
            throw new EvalException("Binary operator `" + b + "' applied to non-integer " + val);
        }

        private BoolConstant evalBooleanArg(AST arg, BinOp b) {
            JamVal val = arg.accept(evalVisitor);
            if (val instanceof BoolConstant) return (BoolConstant) val;
            throw new EvalException("Binary operator `" + b + "' applied to non-boolean " + val);
        }

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
            return new IntConstant(evalIntegerArg(arg1,op).value() / evalIntegerArg(arg2,op).value());
        }

        public JamVal forOpEquals(OpEquals op) {
            return BoolConstant.toBoolConstant(arg1.accept(evalVisitor).equals(arg2.accept(evalVisitor)));
        }

        public JamVal forOpNotEquals(OpNotEquals op) {
            return BoolConstant.toBoolConstant(! arg1.accept(evalVisitor).equals(arg2.accept(evalVisitor)));
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
            BoolConstant b1 = evalBooleanArg(arg1,op);
            BoolConstant b2 = evalBooleanArg(arg2, op);
            if (b1 == BoolConstant.TRUE && b2 == BoolConstant.TRUE) {
                return BoolConstant.TRUE;
            } else {
                return BoolConstant.FALSE;
            }
        }
        public JamVal forOpOr(OpOr op) {
            BoolConstant b1 = evalBooleanArg(arg1,op);
            BoolConstant b2 = evalBooleanArg(arg2, op);
            if (b1 == BoolConstant.TRUE || b2 == BoolConstant.TRUE) {
                return BoolConstant.TRUE;
            } else {
                return BoolConstant.FALSE;
            }
        }
        // public JamVal forOpGets(OpGets op) { return ... ; }  // Supports addition of ref cells to Jam
    }

    /** Interface for a factory that constructs a PrimFunVisitor with a given EvalVisitor and args. */
    interface PrimFunVisitorFactory {
        PrimFunVisitor<JamVal> newVisitor(EvalVisitor ev, AST[] args);
    }

    static class StandardPrimFunFactory implements PrimFunVisitorFactory {

        public static StandardPrimFunFactory ONLY = new StandardPrimFunFactory();
        private StandardPrimFunFactory() {}

        public PrimFunVisitor<JamVal> newVisitor(EvalVisitor ev, AST[] args) {
            return new StandardPrimFunVisitor(ev, args);
        }

        static private class StandardPrimFunVisitor implements PrimFunVisitor<JamVal> {

            EvalVisitor evalVisitor;
            AST[] args;

            StandardPrimFunVisitor(EvalVisitor ev, AST[] asts) { evalVisitor = ev; args = asts; }

            private JamVal[] evalArgs() {
                int n = args.length;
                JamVal[] vals = new JamVal[n];
                for (int i=0; i < n; i++) vals[i] = args[i].accept(evalVisitor);
                return vals;
            }


            private JamVal primFunError(String fn) {
                throw new EvalException("Primitive function `" + fn + "' applied to " +
                        args.length + " arguments");
            }

            private JamCons evalJamConsArg(AST arg, String fun) {

                // TODO: implement cons convention

                JamVal val = arg.accept(evalVisitor);
                if (val instanceof JamCons) {
                    if (evalVisitor.consConv().equals("name")) {
                        ((NameCons) val).sus.putEv(evalVisitor);
                        Suspension
                            susConsElement =
                            ((NameCons) val).susCons.accept(new PureListVisitor<Suspension, Suspension>() {
                                public Suspension forEmpty(Empty<Suspension> e) {
//                                throw new EvalException("Cons is empty");
                                    return null;
                                }

                                public Suspension forCons(Cons<Suspension> c) {
                                    return c.first();
                                }
                            });
                        if (susConsElement != null) {
                            susConsElement.putEv(evalVisitor);
                        }
                        return (NameCons) val;
                    }
                    return (JamCons) val;
                }
                throw new EvalException("Primitive function `" + fun + "' applied to argument " + val +
                                            " that is not a JamCons");
            }

            public JamVal forFunctionPPrim() {
                JamVal[] vals = evalArgs();
                if (vals.length != 1) return primFunError("function?");
                return BoolConstant.toBoolConstant(vals[0] instanceof JamFun);
            }

            public JamVal forNumberPPrim() {
                JamVal[] vals = evalArgs();
                if (vals.length != 1) return primFunError("number?");
                return BoolConstant.toBoolConstant(vals[0] instanceof IntConstant);
            }

            public JamVal forListPPrim() {
                JamVal[] vals = evalArgs();
                if (vals.length != 1) return primFunError("list?");
                return BoolConstant.toBoolConstant(vals[0] instanceof JamList);
            }

            public JamVal forConsPPrim() {
                JamVal[] vals = evalArgs();
                if (vals.length != 1) return primFunError("cons?");
                return BoolConstant.toBoolConstant(vals[0] instanceof JamCons);
            }

            public JamVal forNullPPrim() {
                JamVal[] vals = evalArgs();
                if (vals.length != 1) return primFunError("null?");
                return BoolConstant.toBoolConstant(vals[0] instanceof JamEmpty);
            }

            public JamVal forConsPrim() {

                // TODO: implement cons convention

                if (evalVisitor.consConv().equals("name")) {
                    if (args.length != 2) {
                        return primFunError("cons");
                    } else {
                        System.out.println("This gets where I want it to get");
                        // get first and second Suspension from args[0] and args[1]
                        Suspension sus = new Suspension(args[0], evalVisitor);
                        Suspension sus2 = new Suspension(args[1], evalVisitor);

                        NameCons nameCons = new NameCons(sus, new Cons<>(sus2, new Empty<>()));
                        return nameCons;
                    }
                }

                if (evalVisitor.consConv().equals("need")) {
                    if (args.length != 2) {
                        return primFunError("cons");
                    } else {
                        Suspension sus = new Suspension(args[0], evalVisitor);
                        Suspension sus2 = new Suspension(args[1], evalVisitor);
                        NeedCons needCons = new NeedCons(sus, new Cons<>(sus2, new Empty<>()));
                        return needCons;
                    }
                }

                JamVal[] vals = evalArgs();
                if (vals.length != 2) return primFunError("cons");
                if (vals[1] instanceof JamList) return new JamCons(vals[0], (JamList) vals[1]);
                throw new EvalException("Second argument " + vals[1] + " to `cons' is not a JamList");
            }

            public JamVal forArityPrim() {
                JamVal[] vals = evalArgs();
                if (vals.length != 1) return primFunError("arity");
                if (! (vals[0] instanceof JamFun) ) throw new EvalException("arity applied to argument " +
                        vals[0]);
                return ((JamFun) vals[0]).accept(ArityVisitor.ONLY);
            }

            // TODO check cons convention when calling first or last
            public JamVal forFirstPrim() {
                System.out.println("Do we get here?");
                if (evalVisitor.consConv().equals("name")){
                    NameCons myNameCons = (NameCons) evalJamConsArg(args[0], "first");
                    return myNameCons.first();
                }
                return evalJamConsArg(args[0], "first").first();

            }
            public JamVal forRestPrim() {

                if (evalVisitor.consConv().equals("name")){
                    NameCons myNameCons = (NameCons) evalJamConsArg(args[0], "rest");
                    return myNameCons.rest();
                }
                return evalJamConsArg(args[0], "rest").rest();

            }

            /** Visitor class that implements the Jam arity method. */
            static private class ArityVisitor implements JamFunVisitor<IntConstant> {
                static public ArityVisitor ONLY = new ArityVisitor();
                private ArityVisitor() {}
                public IntConstant forJamClosure(JamClosure jc) { return new IntConstant(jc.body().vars().length); }
                public IntConstant forPrimFun(PrimFun jpf) { return jpf.accept(PrimArityVisitor.ONLY); }
            }

            /** Visitor class that implements the Jam arity method on primitive functions. */
            static private class PrimArityVisitor implements PrimFunVisitor<IntConstant> {
                static public PrimArityVisitor ONLY = new PrimArityVisitor();
                private PrimArityVisitor() {}

                public IntConstant forFunctionPPrim() { return new IntConstant(1); }
                public IntConstant forNumberPPrim() { return new IntConstant(1); }
                public IntConstant forListPPrim() { return new IntConstant(1); }
                public IntConstant forConsPPrim() { return new IntConstant(1); }
                public IntConstant forNullPPrim() { return new IntConstant(1); }
                public IntConstant forArityPrim() { return new IntConstant(1); }
                public IntConstant forConsPrim() { return new IntConstant(2); }
                public IntConstant forFirstPrim() { return new IntConstant(1); }
                public IntConstant forRestPrim() { return new IntConstant(1); }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        Parser p;
        if (args.length == 0) {
            System.out.println("Usage: java Interpreter <filename> { value | name | need }");
            return;
        }
        p = new Parser(args[0]);
        AST prog = p.parse();
        System.out.println("AST is: " + prog);
        Interpreter i = new Interpreter(p);
//        if (args.length == 1) {
//            System.out.println("Call-by-value Answer is: " + i.callByValue(prog));
//            System.out.println("Call-by-name Answer is: " + i.callByName(prog));
//            System.out.println("Call-by-need Answer is: " + i.callByNeed(prog));
//        }
//        else if (args[1].equals("value")) {
//            System.out.println("Call-by-value Answer is: " + i.callByValue(prog));
//        }
//        else if (args[1].equals("need")) {
//            System.out.println("Call-by-need Answer is: " + i.callByNeed(prog));
//        }
//        else
//            System.out.println("Call-by-name Answer is: " + i.callByName(prog));
    }

    public JamVal valueValue() { return callByValue("value"); }

    public JamVal valueName() { return callByValue("name"); }

    public JamVal valueNeed() { return callByValue("need"); }

    public JamVal nameValue() { return callByName("value"); }

    public JamVal nameName() { return callByName("name"); }

    public JamVal nameNeed() { return callByName("need"); }

    public JamVal needValue() { return callByNeed("value"); }

    public JamVal needName() { return callByNeed("name"); }

    public JamVal needNeed() { return callByNeed("need"); }
}

class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

class SyntaxException extends RuntimeException {
    SyntaxException(String msg) { super(msg); }
}