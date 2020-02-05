import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/** file Interpreter.java **/
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

class Interpreter {

    private Parser par;

    private AST ast;

    private ASTVisitor<JamVal> astVis = new ASTVisitor<JamVal>() {
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
            return null;
        }

        @Override
        public JamVal forBinOpApp(BinOpApp b) {
            AST leftTerm = b.arg1();
            AST rightTerm = b.arg2();
            JamVal leftJam = leftTerm.accept(this);
            JamVal rightJam = rightTerm.accept(this);
            BinOp operator = b.rator();
//            if (operator == OpTimes.ONLY) {
//                if ((leftJam instanceof IntConstant) && (rightJam instanceof IntConstant)) {
//                    return new IntConstant(((IntConstant) leftJam).value() * ((IntConstant) rightJam).value());
//                }
//            }
            BinOpVisitor<JamVal> bopVis = new BinOpVisitor<JamVal>() {
                @Override
                public JamVal forBinOpPlus(BinOpPlus op) {
                    return new IntConstant(((IntConstant) leftJam).value() +
                                               ((IntConstant) rightJam).value());
                }

                @Override
                public JamVal forBinOpMinus(BinOpMinus op) {
                    return null;
                }

                @Override
                public JamVal forOpTimes(OpTimes op) {
                    return new IntConstant(((IntConstant) leftJam).value() *
                                                      ((IntConstant) rightJam).value());
                }

                @Override
                public JamVal forOpDivide(OpDivide op) {
                    return null;
                }

                @Override
                public JamVal forOpEquals(OpEquals op) {
                    return null;
                }

                @Override
                public JamVal forOpNotEquals(OpNotEquals op) {
                    return null;
                }

                @Override
                public JamVal forOpLessThan(OpLessThan op) {
                    return null;
                }

                @Override
                public JamVal forOpGreaterThan(OpGreaterThan op) {
                    return null;
                }

                @Override
                public JamVal forOpLessThanEquals(OpLessThanEquals op) {
                    return null;
                }

                @Override
                public JamVal forOpGreaterThanEquals(OpGreaterThanEquals op) {
                    return null;
                }

                @Override
                public JamVal forOpAnd(OpAnd op) {
                    return null;
                }

                @Override
                public JamVal forOpOr(OpOr op) {
                    return null;
                }
            };
            return operator.accept(bopVis);
//            error();
//            return null;
        }

        @Override
        public JamVal forApp(App a) {
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
    };


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
        return ast.accept(astVis);
    };

    public JamVal callByName()  {
        return null;
    };

    public JamVal callByNeed()  {
        return null;
    };

    private AST error() {
//    for (int i = 0; i < 10; i++) {
//      System.out.println(in.readToken());
//    }
        throw new EvalException("Error!");
    }

}