import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/** file Interpreter.java **/
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

class EvalVisitor implements ASTVisitor<JamVal>{
    PureList<Binding> env;
    int callBy;

    EvalVisitor (PureList<Binding> e, int call) {
        env = e;
        callBy = call;
    }

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
        return JamEmpty.ONLY;
    }

    @Override
    public JamVal forVariable(Variable v) {


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
        // Factory method for Primitive functions with no args.
        StandardPrimFunVisitorFactory myFac = new StandardPrimFunVisitorFactory();
        PrimFunVisitor<JamVal> myVis = myFac.newVisitor(this, null);
        return f.accept(myVis);
    }

    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        AST term = u.arg();
        JamVal termJam = term.accept(this);
        UnOp operator = u.rator();

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
//        System.out.println("Gets to BinOp: " + b);
        AST leftTerm = b.arg1();
        AST rightTerm = b.arg2();
        JamVal leftJam = leftTerm.accept(this);
        JamVal rightJam = rightTerm.accept(this);
        BinOp operator = b.rator();

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
//                    System.out.println(((IntConstant) leftJam).value());
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

            // Breaking down the closure into
            JamClosure ratorClos = (JamClosure) rator;
            // The Map function.
            Map ratorBod = ratorClos.body();
            // The environment of the Map.
            PureList<Binding> ratorEnv = ratorClos.env();
            // The variables of the Map.
            Variable[] mapVars = ratorBod.vars();
            // The body of the Map.
            AST mapBody = ratorBod.body();

            // Checking that the number of variables = the number of arguments.
            if (mapVars.length == numArgs) {
                // If so, store the variables and their values in the Map's closure.
                for (int i = 0; i < mapVars.length; i++) {

                    Binding b;

                    if (callBy == 0) {
                        System.out.println(mapVars[i]);
                        System.out.println(a.args()[i]);
                        b = new ValBinding(mapVars[i], a.args()[i].accept(this));

                    } else if (callBy == 1) {
//                        b = new NameBinding(mapVars[i], argsJam[i]);
                        b = new NameBinding(mapVars[i], new JamClosure(
                            new Map(new Variable[0], a.args()[i]), env));

                    } else {
                        b = new NeedBinding(mapVars[i], a.args()[i].accept(this));

                    }
//                    ratorEnv = ratorEnv.append(new Cons<>(b, new Empty<>()));
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
            // If # Variables != # Arguments, throw error.
            } else {
                return error();
            }

        // If it is a primitive function, do it the way you did forPrimFun but this
        // time with the arguments.
        } else if (rator instanceof PrimFun) {
            StandardPrimFunVisitorFactory myFac = new StandardPrimFunVisitorFactory();
            PrimFunVisitor<JamVal> myVis = myFac.newVisitor(this, a.args());
            return ((PrimFun) rator).accept(myVis);
        // If it isn't a Map or a PrimFun, throw an error.
        } else {
            return error();
        }
    }

    @Override
    public JamVal forMap(Map m) {
        return new JamClosure(m, env);
    }

    @Override
    public JamVal forIf(If i) {
        AST testAST = i.test();
        JamVal testJam = testAST.accept(this);
        System.out.println(testJam);
        if (testJam instanceof BoolConstant) {
            BoolConstant testBool = (BoolConstant) testJam;
            if (testBool.value()) {
                System.out.println("Reached correct consequence");
                System.out.println(i.conseq());
                return i.conseq().accept(this);
            } else {
                System.out.println("Reached inc consequence");
                System.out.println("Alt is: " + i.alt());
                return i.alt().accept(this);
            }
        } else {
            return error();
        }

    }

    @Override
    public JamVal forLet(Let l) {

        Def[] defs = l.defs();

        // currently coded for valBinding

        for (Def d: defs) {
            Variable dVar = d.lhs();

//            ValBinding b = new ValBinding(dVar, dVal);

            Binding b;

            if (callBy == 0) {
                JamVal dVal = d.rhs().accept(this);
                b = new ValBinding(dVar, dVal);

            } else if (callBy == 1) {
//                b = new NameBinding(dVar, dVal);
                b = new NameBinding(dVar, new JamClosure(new Map(new Variable[0], d.rhs()), env));

            } else {
                JamVal dVal = d.rhs().accept(this);
                b = new NeedBinding(dVar, dVal);

            }

            env = env.append(new Cons<>(b, new Empty<>()));
        }
        return l.body().accept(this);
    }
}

class ValBinding extends Binding{
    ValBinding(Variable v, JamVal jv) {
        super(v, jv);
    }
}

class NameBinding extends Binding{

    NameBinding(Variable v, JamClosure c) {
        super(v, c);
    }

    @Override
    public JamVal value() {
        // evaluate closure
//        return null;
        Map map = ((JamClosure) this.value).body();
        PureList env = ((JamClosure) this.value).env();
        return map.body().accept(new EvalVisitor(env, 1));
    }
}

class NeedBinding extends Binding{
    Boolean eval;

    NeedBinding(Variable v, JamVal c) {
        super(v, c);
    }

    @Override
    public JamVal value() {
        // evaluate closure or override with value if previously evaluated
        Map map = ((JamClosure) this.value).body();
        PureList env = ((JamClosure) this.value).env();


        eval = true;

        if (eval) {

            return map.body().accept(new EvalVisitor(env, 1));
        } else {
            return null;
        }
    }

}

interface PrimFunVisitorFactory {
    PrimFunVisitor newVisitor(EvalVisitor env, AST[] args);
}

class StandardPrimFunVisitorFactory {

    public PrimFunVisitor newVisitor(EvalVisitor env, AST[] args) {
        return new StandardPrimFunVisitor(env, args);
    }

    class StandardPrimFunVisitor implements PrimFunVisitor {

        AST[] args;
        EvalVisitor env;

        public StandardPrimFunVisitor(EvalVisitor e, AST[] a) {
            args = a;
            env = e;
        }

        public JamVal error() {
            throw new EvalException("Error!");
        }

        @Override
        public JamVal forFunctionPPrim() {
            if (args == null) {
                return FunctionPPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];

                JamVal jam = a.accept(env);

                if (jam instanceof JamFun) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }

            } else {
                return error();
            }
        }

        @Override
        public JamVal forNumberPPrim() {
            if (args == null) {
                return NumberPPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];

                JamVal jam = a.accept(env);

                if (jam instanceof IntConstant) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            } else {
                return error();
            }
        }

        @Override
        public JamVal forListPPrim() {
            if (args == null) {
                return ListPPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];

                JamVal jam = a.accept(env);

                if (jam instanceof JamFun) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }

            } else {
                return error();
            }
        }

        @Override
        public JamVal forConsPPrim() {
            if (args == null) {
                return ConsPPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];

                JamVal jam = a.accept(env);

                if (jam instanceof JamCons) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }
            } else {
                return error();
            }
        }

        @Override
        public JamVal forNullPPrim() {
            if (args == null) {
                return NullPPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];

                JamVal jam = a.accept(env);

                if (jam instanceof JamEmpty) {
                    return BoolConstant.toBoolConstant(true);
                } else {
                    return BoolConstant.toBoolConstant(false);
                }

            } else {
                return error();
            }
        }

        @Override
        public JamVal forArityPrim() {
            if (args == null) {
                return ArityPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];

                JamVal jam = a.accept(env);

                if (jam instanceof PrimFun) {
                    return new IntConstant(1);
                } else if (jam instanceof JamClosure) {
                    JamClosure jamClos = (JamClosure) jam;
                    Map jamMap = jamClos.body();
                    return new IntConstant(jamMap.vars().length);
                } else {
                    return error();
                }

            } else {
                return error();
            }
        }

        @Override
        public JamVal forConsPrim() {
            if (args == null) {
                return ConsPrim.ONLY;
            } else if (args.length == 2) {
                AST first = args[0];

                System.out.println("first =>  " + first);
                System.out.println("second =>  " + args[1]);

//                JamVal firstJam = first.accept(env);
//                System.out.println("firstjam =>  " + firstJam);

                AST rem = args[1];

                System.out.println("rem => " + rem);
                JamVal remJam = rem.accept(env);

                System.out.println("remjam =>  " + remJam);
                if (remJam instanceof JamEmpty) {
                    System.out.println("Reached here");
                    JamList consList = new JamCons(first.accept(env), JamEmpty.ONLY);
                    return consList;
//                    return new Cons<>(firstJam, new Empty<>());
                } else if (remJam instanceof JamCons){
                    System.out.println("Do I get here?");
                    return new JamCons(first.accept(env), (JamCons) remJam);
                } else {
                    return error();
                }
            } else {
                return error();
            }
        }

        @Override
        public JamVal forFirstPrim() {
            if (args == null) {
                return FirstPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];
                JamVal jam = a.accept(env);

                if (jam instanceof Cons) {
                    Cons jamCon = (Cons) jam;
                    return (JamVal) jamCon.first();
                } else {
                    return error();
                }

            } else {
                return error();
            }
        }

        @Override
        public JamVal forRestPrim() {
            if (args == null) {
                return RestPrim.ONLY;
            } else if (args.length == 1) {
                AST a = args[0];
                JamVal jam = a.accept(env);

                if (jam instanceof Cons) {
                    Cons jamCon = (Cons) jam;
                    return (JamVal) jamCon.rest();
                } else {
                    return error();
                }

            } else {
                return error();
            }
        }
    }
}


/////////////////////////////////////////
/////////// Interpreter Class ///////////
/////////////////////////////////////////



class Interpreter {

    private Parser par;

    private AST ast;



    Interpreter(String fileName) throws IOException {
        par = new Parser(fileName);
        ast = par.parse();
    }

    Interpreter(Reader reader) {
        par = new Parser(reader);
        ast = par.parse();
    }

    public JamVal callByValue() {
//        System.out.println(ast);

        return ast.accept(new EvalVisitor(new Empty<Binding>(), 0));
    };

    public JamVal callByName()  {
        return ast.accept(new EvalVisitor(new Empty<Binding>(), 1));
    };

    public JamVal callByNeed()  {
        return ast.accept(new EvalVisitor(new Empty<Binding>(), 2));
    };

}