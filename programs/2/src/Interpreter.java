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
            System.out.println("Reached int constant");
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
            return null;
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

}