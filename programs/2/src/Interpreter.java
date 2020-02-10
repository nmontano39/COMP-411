import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/** file Interpreter.java **/
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

class EvalVisitor implements ASTVisitor<JamVal>{
    PureList<Binding> env;

    EvalVisitor (PureList<Binding> e) {
        env = e;
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
                if (c.first.var == v) {
                    return c.first.value;
                } else {
                    if (c.rest.equals(new Empty<Binding>())) {
                        return error();
                    }
                    return forCons((Cons<Binding>) c.rest);
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
                return BoolConstant.toBoolConstant(leftJam == rightJam);
            }

            @Override
            public JamVal forOpNotEquals(OpNotEquals op) {
                return BoolConstant.toBoolConstant(leftJam != rightJam);
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
            // Store the arguments in an array.
            JamVal[] argsJam = new JamVal[numArgs];
            for (int i = 0; i < numArgs; i++) {
                argsJam[i] = a.args()[i].accept(this);
            }
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
                    ValBinding b = new ValBinding(mapVars[i], argsJam[i]);
                    ratorEnv = ratorEnv.append(new Cons<>(b, new Empty<>()));
                }
                // Evaluate the body of the Map in the Map's closure. (That's why we have
                // the new EvalVisitor...
                return mapBody.accept(new EvalVisitor(ratorEnv));
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
        return new JamClosure(m, new Empty<>());
    }

    @Override
    public JamVal forIf(If i) {
        AST testAST = i.test();
        JamVal testJam = testAST.accept(this);
        if (testJam instanceof BoolConstant) {
            BoolConstant testBool = (BoolConstant) testJam;
            if (testBool.value()) {
                return i.conseq().accept(this);
            } else {
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
            JamVal dVal = d.rhs().accept(this);
            ValBinding b = new ValBinding(dVar, dVal);
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
        super(v, (JamVal) c);
    }

    @Override
    public JamVal value() {
        // evaluate closure
        return null;
    }
}

class NeedBinding extends Binding{
    Boolean eval;

    NeedBinding(Variable v, JamClosure c) {
        super(v, (JamVal) c);
    }

    @Override
    public JamVal value() {
        // evaluate closure or override with value if previously evaluated
        return null;
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
            if (args.length > 0) {
                return null;
            } else {
                return FunctionPPrim.ONLY;
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
            if (args.length > 0) {
                return null;
            } else {
                return ListPPrim.ONLY;
            }
        }

        @Override
        public JamVal forConsPPrim() {
            if (args.length > 0) {
                return null;
            } else {
                return ConsPPrim.ONLY;
            }
        }

        @Override
        public JamVal forNullPPrim() {
            if (args.length > 0) {
                return null;
            } else {
                return NullPPrim.ONLY;
            }
        }

        @Override
        public JamVal forArityPrim() {
            if (args.length > 0) {
                return null;
            } else {
                return ArityPrim.ONLY;
            }
        }

        @Override
        public JamVal forConsPrim() {
            if (args.length > 0) {
                return null;
            } else {
                return ConsPrim.ONLY;
            }
        }

        @Override
        public JamVal forFirstPrim() {
            if (args.length > 0) {
                return null;
            } else {
                return FirstPrim.ONLY;
            }
        }

        @Override
        public JamVal forRestPrim() {
            if (args.length > 0) {
                return null;
            } else {
                return RestPrim.ONLY;
            }
        }
    }
}

class CallByValFunVisitor<JamVal> implements JamFunVisitor{
    AST[] args;
    EvalVisitor ev;
    PrimFunVisitorFactory primFactory;

    CallByValFunVisitor(EvalVisitor e, AST[] a) {
        args = a;
        ev = e;
    }

    @Override
    public JamVal forJamClosure(JamClosure c) {
        return null;
    }

    @Override
    public JamVal forPrimFun(PrimFun pf) {
        // invoke prim fun with accept method
//        return  pf.accept(primFactory.newVisitor(ev, args));
        return null;
    }
}

class CallByNameFunVisitor<ResType> implements JamFunVisitor{
    @Override
    public Object forJamClosure(JamClosure c) {
        return null;
    }

    @Override
    public Object forPrimFun(PrimFun pf) {
        return null;
    }
}

class CallByNeedFunVisitor<ResType> implements JamFunVisitor{
    @Override
    public Object forJamClosure(JamClosure c) {
        return null;
    }

    @Override
    public Object forPrimFun(PrimFun pf) {
        return null;
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

        return ast.accept(new EvalVisitor(new Empty<Binding>()));
    };

    public JamVal callByName()  {
        return ast.accept(new EvalVisitor(new Empty<Binding>()));
    };

    public JamVal callByNeed()  {
        return ast.accept(new EvalVisitor(new Empty<Binding>()));
    };

}