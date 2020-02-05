import java.io.IOException;
import java.io.Reader;

/** file Interpreter.java **/
class EvalException extends RuntimeException {
    EvalException(String msg) { super(msg); }
}

class Interpreter {
    Interpreter(String fileName) throws IOException {

    }

    Interpreter(Reader reader) {

    }

    public JamVal callByValue() {
        return null;
    };

    public JamVal callByName()  {
        return null;
    };

    public JamVal callByNeed()  {
        return null;
    };

}