import java.io.IOException;
import java.io.Reader;

/** file Interpreter.java **/
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

/** EvalVisitor Class, given callBy type **/
class EvalVisitor implements ASTVisitor<JamVal>{
    // environment stored as PureList of Bindings
    PureList<Binding> env;
    int callBy;

    // create environment e given callBy type call
    EvalVisitor (PureList<Binding> e, int call) {
        env = e;
        callBy = call;
    }

    // throw eval exception
    public JamVal error() {
        throw new EvalException("Error!");
    }

    @Override
    public JamVal forBoolConstant(BoolConstant b) {
        return b;
    }

    @Override
    public JamVal forIntConstant(IntConstant i) {
        return i;
    }

    @Override
    public JamVal forNullConstant(NullConstant n) {
        // NullConstant = JamEmpty
        return JamEmpty.ONLY;
    }

    @Override
    public JamVal forVariable(Variable v) {
        // visitor call for either Empty or Cons of Bindings
        return env.accept(new PureListVisitor<Binding, JamVal>() {
            @Override
            public JamVal forEmpty(Empty<Binding> e) {
                return error();
            }

            @Override
            public JamVal forCons(Cons<Binding> c) {
                if (c.first().var() == v) {
                    return c.first().value();
                } else {
                    if (c.rest().equals(new Empty<Binding>())) {
                        return error();
                    }
                    return forCons((Cons<Binding>) c.rest());
                }
            }
        });
    }

    @Override
    public JamVal forPrimFun(PrimFun f) {
        // Factory method for Primitive functions with no args
        PrimFunVisitorFactory myFac = new PrimFunVisitorFactory();
        PrimFunVisitor<JamVal> myVis = myFac.newVisitor(this, null);
        return f.accept(myVis);
    }

    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        // get argument as JamVal
        AST term = u.arg();
        JamVal termJam = term.accept(this);
        UnOp operator = u.rator();
        //visitor call to operator
        UnOpVisitor<JamVal> uopVis = new UnOpVisitor<JamVal>() {
            @Override
            public JamVal forUnOpPlus(UnOpPlus op) {
                if (termJam instanceof IntConstant) {
                    return termJam;
                }
                return error();
            }

            @Override
            public JamVal forUnOpMinus(UnOpMinus op) {
                if (termJam instanceof IntConstant) {
                    return new IntConstant(-1 * ((IntConstant) termJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpTilde(OpTilde op) {
                if (termJam instanceof BoolConstant) {
                    return BoolConstant.toBoolConstant(!((BoolConstant) termJam).value());
                }
                return error();
            }
        };
        return operator.accept(uopVis);
    }

    @Override
    public JamVal forBinOpApp(BinOpApp b) {
        // get left and right arguments as JamVals
        AST leftTerm = b.arg1();
        AST rightTerm = b.arg2();
        JamVal leftJam = leftTerm.accept(this);
        JamVal rightJam = rightTerm.accept(this);
        BinOp operator = b.rator();
        //visitor call to operator
        BinOpVisitor<JamVal> bopVis = new BinOpVisitor<JamVal>() {
            @Override
            public JamVal forBinOpPlus(BinOpPlus op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return new IntConstant(((IntConstant) leftJam).value() +
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forBinOpMinus(BinOpMinus op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return new IntConstant(((IntConstant) leftJam).value() -
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpTimes(OpTimes op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return new IntConstant(((IntConstant) leftJam).value() *
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpDivide(OpDivide op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant) &&
                        (((IntConstant) rightJam).value() != 0)) {
                    return new IntConstant(((IntConstant) leftJam).value() /
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpEquals(OpEquals op) {
                return BoolConstant.toBoolConstant(leftJam.equals(rightJam));
            }

            @Override
            public JamVal forOpNotEquals(OpNotEquals op) {
                return BoolConstant.toBoolConstant(!(leftJam.equals(rightJam)));
            }

            @Override
            public JamVal forOpLessThan(OpLessThan op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() <
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpGreaterThan(OpGreaterThan op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() >
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpLessThanEquals(OpLessThanEquals op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() <=
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
                    return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() >=
                            ((IntConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpAnd(OpAnd op) {
                if ((leftJam instanceof BoolConstant) && (rightJam instanceof BoolConstant)) {
                    return BoolConstant.toBoolConstant(((BoolConstant) leftJam).value() &&
                            ((BoolConstant) rightJam).value());
                }
                return error();
            }

            @Override
            public JamVal forOpOr(OpOr op) {
                if ((leftJam instanceof BoolConstant) && (rightJam instanceof BoolConstant)) {
                    return BoolConstant.toBoolConstant(((BoolConstant) leftJam).value() ||
                            ((BoolConstant) rightJam).value());
                }
                return error();
            }
        };
        return operator.accept(bopVis);
    }

    @Override
    public JamVal forApp(App a) {
        // Get the rator and convert it to a Jam Val.
        JamVal rator = a.rator().accept(this);

        // If it is a closure (aka Map)
        if (rator instanceof JamClosure) {
            // Get the number of arguments.
            int numArgs = a.args().length;
            // Breaking down the closure
            JamClosure ratorClos = (JamClosure) rator;
            // The Map function
            Map ratorBod = ratorClos.body();
            // The environment of the Map
            PureList<Binding> ratorEnv = ratorClos.env();
            // The variables of the Map
            Variable[] mapVars = ratorBod.vars();
            // The body of the Map
            AST mapBody = ratorBod.body();

            // Checking that the number of variables = the number of arguments.
            if (mapVars.length == numArgs) {
                // If so, store the variables and their values in the Map's closure.
                for (int i = 0; i < mapVars.length; i++) {
                    // abstract Binding class
                    Binding b;

                    // callBy => value (0), name (1), need (2)
                    if (callBy == 0) {
                        // use ValBinding with value
                        b = new ValBinding(mapVars[i], a.args()[i].accept(this));
                    } else if (callBy == 1) {
                        // use NameBinding with closure
                        b = new NameBinding(mapVars[i], new JamClosure(
                            new Map(new Variable[0], a.args()[i]), env));
                    } else {
                        // use NeedBinding with closure
                        b = new NeedBinding(mapVars[i], new JamClosure(
                                new Map(new Variable[0], a.args()[i]), env));
                    }

                    // visitor call to operator
                    ratorEnv = ratorEnv.accept(new PureListVisitor<Binding, Cons<Binding>>() {
                        @Override
                        public Cons<Binding> forEmpty(Empty<Binding> e) {
                            return new Cons<>(b, new Empty<>());
                        }

                        @Override
                        public Cons<Binding> forCons(Cons<Binding> c) {
                            if (c.first().var() == b.var()) {
                                return new Cons<>(b, new Cons<>(c.first(), c.rest()));
                            } else {
                                if (c.rest().equals(new Empty<Binding>())) {
                                    return new Cons<>(c.first(), new Cons<>(b, new Empty<>()));
                                }
                                return new Cons<>(c.first(), forCons((Cons<Binding>) c.rest()));
                            }
                        }
                    });
                }
                // Evaluate the body of the Map in the Map's closure. (That's why we have
                // the new EvalVisitor...
                return mapBody.accept(new EvalVisitor(ratorEnv, callBy));
            }
        }

        // If it is a primitive function, do it the way you did forPrimFun but this
        // time with the arguments.
        if (rator instanceof PrimFun) {
            PrimFunVisitorFactory myFac = new PrimFunVisitorFactory();
            PrimFunVisitor<JamVal> myVis = myFac.newVisitor(this, a.args());
            return ((PrimFun) rator).accept(myVis);
        }

        // if we reach here throw error
        return error();
    }

    @Override
    public JamVal forMap(Map m) {
        // return JamClosure given Map and environment
        return new JamClosure(m, env);
    }

    @Override
    public JamVal forIf(If i) {
        AST testAST = i.test();
        JamVal testJam = testAST.accept(this);
        // if true
        if (testJam instanceof BoolConstant) {
            BoolConstant testBool = (BoolConstant) testJam;
            //then
            if (testBool.value()) {
                return i.conseq().accept(this);
            }
            //else
            else {
                return i.alt().accept(this);
            }
        } else {
            return error();
        }
    }

    @Override
    public JamVal forLet(Let l) {
        // list of Defs
        Def[] defs = l.defs();

        // for each Def d
        for (Def d: defs) {
            Variable dVar = d.lhs();
            Binding b;

            // callBy => value (0), name (1), need (2)
            if (callBy == 0) {
                // evaluate the value and set to ValBinding
                JamVal dVal = d.rhs().accept(this);
                b = new ValBinding(dVar, dVal);
            } else if (callBy == 1) {
                // give closure to NameBinding
                b = new NameBinding(dVar, new JamClosure(new Map(new Variable[0], d.rhs()), env));
            } else if (callBy == 2) {
                // give closure to NeedBinding
                b = new NeedBinding(dVar, new JamClosure(new Map(new Variable[0], d.rhs()), env));
            } else {
                return error();
            }
            // add binding to environment
            env = env.append(new Cons<>(b, new Empty<>()));
        }
        return l.body().accept(this);
    }
}

/** Binds Variable to value, given to callByVal **/
class ValBinding extends Binding{

    ValBinding(Variable v, JamVal jv) {
        super(v, jv);
    }
}

/** Binds Variable to closure, given to callByName **/
class NameBinding extends Binding{

    NameBinding(Variable v, JamClosure c) {
        super(v, c);
    }

    @Override
    public JamVal value() {
        // evaluate closure only when this method is called
        Map map = ((JamClosure) this.value).body();
        PureList env = ((JamClosure) this.value).env();
        return map.body().accept(new EvalVisitor(env, 1));
    }
}

/** Binds Variable to value, given to callByNeed **/
class NeedBinding extends Binding{

    // true if eval has already been evaluated
    Boolean eval;

    NeedBinding(Variable v, JamVal c) {
        super(v, c);
        eval = false;
    }

    @Override
    public JamVal value() {
        // if not previously evaluated evaluate closure or override with value
        if (!eval) {
            eval = true;
            // if given closure
            if (this.value instanceof JamClosure) {
                // evaluate closure
                Map map = ((JamClosure) this.value).body();
                // get environment
                PureList env = ((JamClosure) this.value).env();
                // make new JamVal
                JamVal newVal = map.body().accept(new EvalVisitor(env, 2));
                // set value to newly evaluated closure
                super.setValue(newVal);
            }
        }
        // else return value
        return value;
    }
}

/** Factory to make PrimFunVisitors **/
class PrimFunVisitorFactory {

    // method to return a new PrimFunVisitor
    public PrimFunVisitor newVisitor(EvalVisitor env, AST[] args) {
        return new StandardPrimFunVisitor(env, args);
    }

    // concrete class for PrimFunVisitor
    class StandardPrimFunVisitor implements PrimFunVisitor {

        // given list of arguments and an environment
        AST[] args;
        EvalVisitor env;

        public StandardPrimFunVisitor(EvalVisitor e, AST[] a) {
            args = a;
            env = e;
        }

        // throw eval exception
        public JamVal error() {
            throw new EvalException("Error!");
        }

        @Override
        public JamVal forFunctionPPrim() {
            // if no arguments
            if (args == null) {
                return FunctionPPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a function
                if (jam instanceof JamFun) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forNumberPPrim() {
            // if no arguments
            if (args == null) {
                return NumberPPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a int
                if (jam instanceof IntConstant) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forListPPrim() {
            // if no arguments
            if (args == null) {
                return ListPPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a function
                if (jam instanceof JamFun) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forConsPPrim() {
            // if no arguments
            if (args == null) {
                return ConsPPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a cons
                if (jam instanceof JamCons) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forNullPPrim() {
            // if no arguments
            if (args == null) {
                return NullPPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is null
                if (jam instanceof JamEmpty) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forArityPrim() {
            // if no arguments
            if (args == null) {
                return ArityPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a function
                if (jam instanceof PrimFun) {
                    return new IntConstant(1);
                }
                // if it is a closure
                if (jam instanceof JamClosure) {
                    JamClosure jamClos = (JamClosure) jam;
                    Map jamMap = jamClos.body();
                    return new IntConstant(jamMap.vars().length);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forConsPrim() {
            // if no arguments
            if (args == null) {
                return ConsPrim.ONLY;
            }
            // if exactly 2 arguments
            if (args.length == 2) {
                // get second argument as JamVal
                AST first = args[0];
                AST rem = args[1];
                JamVal remJam = rem.accept(env);
                // if it is an empty list
                if (remJam instanceof JamEmpty) {
                    JamList consList = new JamCons(first.accept(env), JamEmpty.ONLY);
                    return consList;
                }
                // if it is a cons
                if (remJam instanceof JamCons){
                    return new JamCons(first.accept(env), (JamCons) remJam);
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forFirstPrim() {
            // if no arguments
            if (args == null) {
                return FirstPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a cons
                if (jam instanceof Cons) {
                    Cons jamCon = (Cons) jam;
                    return (JamVal) jamCon.first();
                }
            }
            // if we reach here throw an error
            return error();
        }

        @Override
        public JamVal forRestPrim() {
            // if no arguments
            if (args == null) {
                return RestPrim.ONLY;
            }
            // if exactly 1 argument
            if (args.length == 1) {
                // get argument as JamVal
                AST a = args[0];
                JamVal jam = a.accept(env);
                // if it is a sons
                if (jam instanceof Cons) {
                    Cons jamCon = (Cons) jam;
                    return (JamVal) jamCon.rest();
                }
            }
            // if we reach here throw an error
            return error();
        }
    }
}

/** Interpreter class **/
class Interpreter {

    // given parse and ast
    private Parser par;
    private AST ast;

    // set ast equal to parsed String fileName
    Interpreter(String fileName) throws IOException {
        par = new Parser(fileName);
        ast = par.parse();
    }

    // set ast equal to parsed Reader reader
    Interpreter(Reader reader) {
        par = new Parser(reader);
        ast = par.parse();
    }

    // call by value
    public JamVal callByValue() {
        return ast.accept(new EvalVisitor(new Empty<Binding>(), 0));
    };

    // call by name
    public JamVal callByName()  {
        return ast.accept(new EvalVisitor(new Empty<Binding>(), 1));
    };

    // call by need
    public JamVal callByNeed()  {
        return ast.accept(new EvalVisitor(new Empty<Binding>(), 2));
    };

}