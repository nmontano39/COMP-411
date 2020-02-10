import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/** file Interpreter.java **/
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

/////////////////////////
////////// OH ///////////
/////////////////////////


class EvalVisitor implements ASTVisitor{
    PureList<Binding> env;

    EvalVisitor (PureList<Binding> e) {
        env = e;
    }

    public PureList<Binding> getEnv() {
        return env;
    }

    public JamVal error() {
//    for (int i = 0; i < 10; i++) {
//      System.out.println(in.readToken());
//    }
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
        return null;
    }

    @Override
    public JamVal forVariable(Variable v) {
        return null;
    }

    @Override
    public JamVal forPrimFun(PrimFun f) {
        return null;
    }

    @Override
    public JamVal forUnOpApp(UnOpApp u) {
        AST term = u.arg();
        JamVal termJam = (JamVal) term.accept(this);
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
        JamVal leftJam = (JamVal) leftTerm.accept(this);
        JamVal rightJam = (JamVal) rightTerm.accept(this);
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
//        a.rator().accept(new CallByValFunVisitor<JamVal>(getEnv(), a.args()) {
//
//        });
        return null;
    }

    @Override
    public JamVal forMap(Map m) {
        return null;
    }

    @Override
    public JamVal forIf(If i) {
        return null;
    }

    @Override
    public JamVal forLet(Let l) {
        return null;
    }
}


interface PrimFunVisitorFactory {
    PrimFunVisitor newVisitor(EvalVisitor ev, AST[] args);
}

class StandardPrimFunVisitorFactory implements PrimFunVisitorFactory{

    @Override
    public PrimFunVisitor newVisitor(EvalVisitor ev, AST[] args) {
        return new StandardPrimFunVisitor(ev, args);
    }

    class StandardPrimFunVisitor implements PrimFunVisitor {

        AST[] args;
        EvalVisitor ev;

        public StandardPrimFunVisitor(EvalVisitor e, AST[] a) {
            args = a;
            ev = e;
        }

        @Override
        public JamVal forFunctionPPrim() {
            return null;
        }

        @Override
        public JamVal forNumberPPrim() {
            return null;
        }

        @Override
        public JamVal forListPPrim() {
            return null;
        }

        @Override
        public JamVal forConsPPrim() {
            return null;
        }

        @Override
        public JamVal forNullPPrim() {
            return null;
        }

        @Override
        public JamVal forArityPrim() {
            return null;
        }

        @Override
        public JamVal forConsPrim() {
            return null;
        }

        @Override
        public JamVal forFirstPrim() {
            return null;
        }

        @Override
        public JamVal forRestPrim() {
            return null;
        }
    }
}

class CallByValFunVisitor<ResType> implements JamFunVisitor{
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
        return (JamVal) pf.accept(primFactory.newVisitor(ev, args));
    }
}

class ValBinding extends Binding{
    ValBinding(Variable v, JamVal jv) {
        super(v, jv);
    }
}


// ------------------------------




////////////////////////////////
///////   Call by name   ///////
////////////////////////////////
//
//
//class CallByNameFunVisitor<ResType> implements JamFunVisitor{
//    @Override
//    public Object forJamClosure(JamClosure c) {
//        return null;
//    }
//
//    @Override
//    public Object forPrimFun(PrimFun pf) {
//        return null;
//    }
//}
//
//class CallByNeedFunVisitor<ResType> implements JamFunVisitor{
//    @Override
//    public Object forJamClosure(JamClosure c) {
//        return null;
//    }
//
//    @Override
//    public Object forPrimFun(PrimFun pf) {
//        return null;
//    }
//}
//
//
//class NameBinding extends ValBinding{
//
//    NameBinding(Variable v, Suspension s) {
//        super(v, (JamVal) s);
//    }
//}
//
//class Suspension {
//    AST ast;
//    EvalVisitor ev;
//
//    // ast - f(1/0
//    // env - definition of f
//    // suspend the evaluation of the AST until its needed
//
//}
/////////////////////////
////////// OH ///////////
/////////////////////////

class Interpreter {

    private Parser par;

    private AST ast;

//    private ASTVisitor<JamVal> astVis = new ASTVisitor<JamVal>() {
//        @Override
//        public JamVal forBoolConstant(BoolConstant b) {
//            return b;
//        }
//
//        @Override
//        public JamVal forIntConstant(IntConstant i) {
//            return i;
//        }
//
//        @Override
//        public JamVal forNullConstant(NullConstant n) {
//            return null;
//        }
//
//        @Override
//        public JamVal forVariable(Variable v) {
//            return null;
//        }
//
//        @Override
//        public JamVal forPrimFun(PrimFun f) {
//            return null;
//        }
//
//        @Override
//        public JamVal forUnOpApp(UnOpApp u) {
//            AST term = u.arg();
//            JamVal termJam = term.accept(this);
//            UnOp operator = u.rator();
//
//            UnOpVisitor<JamVal> uopVis = new UnOpVisitor<JamVal>() {
//                @Override
//                public JamVal forUnOpPlus(UnOpPlus op) {
//                    if (termJam instanceof IntConstant) {
//                        return termJam;
//                    }
//
//                    return error();
//                }
//
//                @Override
//                public JamVal forUnOpMinus(UnOpMinus op) {
//                    if (termJam instanceof IntConstant) {
//                        return new IntConstant(-1 * ((IntConstant) termJam).value());
//                    }
//
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpTilde(OpTilde op) {
//                    if (termJam instanceof BoolConstant) {
//                        return BoolConstant.toBoolConstant(!((BoolConstant) termJam).value());
//                    }
//
//                    return error();
//                }
//            };
//            return operator.accept(uopVis);
//        }
//
//        @Override
//        public JamVal forBinOpApp(BinOpApp b) {
//            AST leftTerm = b.arg1();
//            AST rightTerm = b.arg2();
//            JamVal leftJam = leftTerm.accept(this);
//            JamVal rightJam = rightTerm.accept(this);
//            BinOp operator = b.rator();
//
//            BinOpVisitor<JamVal> bopVis = new BinOpVisitor<JamVal>() {
//                @Override
//                public JamVal forBinOpPlus(BinOpPlus op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return new IntConstant(((IntConstant) leftJam).value() +
//                                                   ((IntConstant) rightJam).value());
//                    }
//                    return error();
//
//                }
//
//                @Override
//                public JamVal forBinOpMinus(BinOpMinus op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return new IntConstant(((IntConstant) leftJam).value() -
//                                                   ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpTimes(OpTimes op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return new IntConstant(((IntConstant) leftJam).value() *
//                                                   ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpDivide(OpDivide op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant) &&
//                            (((IntConstant) rightJam).value() != 0)) {
//                        return new IntConstant(((IntConstant) leftJam).value() /
//                                                   ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpEquals(OpEquals op) {
//                    return BoolConstant.toBoolConstant(leftJam == rightJam);
//                }
//
//                @Override
//                public JamVal forOpNotEquals(OpNotEquals op) {
//                    return BoolConstant.toBoolConstant(leftJam != rightJam);
//                }
//
//                @Override
//                public JamVal forOpLessThan(OpLessThan op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() <
//                                                   ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpGreaterThan(OpGreaterThan op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() >
//                                                               ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpLessThanEquals(OpLessThanEquals op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() <=
//                                                               ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
//                    if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                        return BoolConstant.toBoolConstant(((IntConstant) leftJam).value() >=
//                                                               ((IntConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpAnd(OpAnd op) {
//                    if ((leftJam instanceof BoolConstant) && (rightJam instanceof BoolConstant)) {
//                        return BoolConstant.toBoolConstant(((BoolConstant) leftJam).value() &&
//                                                               ((BoolConstant) rightJam).value());
//                    }
//                    return error();
//                }
//
//                @Override
//                public JamVal forOpOr(OpOr op) {
//                    if ((leftJam instanceof BoolConstant) && (rightJam instanceof BoolConstant)) {
//                        return BoolConstant.toBoolConstant(((BoolConstant) leftJam).value() ||
//                                                               ((BoolConstant) rightJam).value());
//                    }
//                    return error();
//                }
//            };
//            return operator.accept(bopVis);
//        }
//
//        @Override
//        public JamVal forApp(App a) {
//            a.rator().accept(new CallByValFunVisitor<JamVal>(a.args(), ev) {
//
//            });
//            return null;
//        }
//
//        @Override
//        public JamVal forMap(Map m) {
//            return null;
//        }
//
//        @Override
//        public JamVal forIf(If i) {
//            return null;
//        }
//
//        @Override
//        public JamVal forLet(Let l) {
//            return null;
//        }
//    };


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
//        return ast.accept(astVis);
        return null;
    };

    public JamVal callByName()  {
        return null;
    };

    public JamVal callByNeed()  {
        return null;
    };

}