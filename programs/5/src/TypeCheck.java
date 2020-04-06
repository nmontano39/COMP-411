/** A visitor class for the type checker. Returns normally unless there is a type error. On a type error,
 * throws a TypeException.
 */
class TypeCheckVisitor implements ASTVisitor<Void> {
    /** Empty symbol table. */
    private static final Empty<Variable> EMPTY_VARS = new Empty<Variable>();

    /** Symbol table to detect free variables. */
    PureList<Variable> env;

    /** Root form of CheckVisitor. */
    public static final TypeCheckVisitor INITIAL = new TypeCheckVisitor(EMPTY_VARS);

    TypeCheckVisitor(PureList<Variable> e) { env = e; }

    /** Helper method that converts an array to a PureList. */
    public static <T> PureList<T> arrayToList(T[] array) {
        int n = array.length;
        PureList<T> list = new Empty<T>();
        for(int i = n - 1; i >= 0; i--) { list = list.cons(array[i]); }
        return list;
    }

    /*  Visitor methods. */
    public Void forIntConstant(IntConstant i) { return null; }
    public Void forBoolConstant(BoolConstant b) { return null; }
    public Void forNullConstant(NullConstant n) { return null; }

    public Void forVariable(Variable v) {
//    System.out.println(v);
        Variable match = env.accept(new LookupVisitor<Variable>(v));
        if (match == null)  throw new SyntaxException("variable " + v + " is unbound");
        return null;
    }

    public Void forPrimFun(PrimFun f) { return null;  }

    public Void forUnOpApp(UnOpApp u) {
        u.arg().accept(this);  // mzy throw a SyntaxException
        return null;
    }

    public Void forBinOpApp(BinOpApp b) {
        Type t1 = b.arg1() instanceof TypedVariable ? ((TypedVariable) b.arg1()).type()
                      : ((TypedNullConstant) b.arg1()).type();
        Type t2 = b.arg1() instanceof TypedVariable ? ((TypedVariable) b.arg2()).type()
                      : ((TypedNullConstant) b.arg2()).type();
        if (! (t1.equals(t2)) ) {
            throw new TypeException("TypeException: Expected type " + t1 + ", but found " + t2);
        }

        b.arg1().accept(this); // may throw a SyntaxException
        b.arg2().accept(this); // may throw a SyntaxException
        return null;
    }

    public Void forApp(App a) {
        a.rator().accept(this); // may throw a SyntaxException
        AST[] args = a.args();
        int n = args.length;
        for(int i = 0; i < n; i++) { args[i].accept(this); } // may throw a SyntaxException
        return null;
    }

    public Void forMap(Map m) {
        // Check for duplicates in Map vars & construct newEnv for Map body
        Variable[] vars = m.vars();
        PureList<Variable> varList = arrayToList(vars);
        varList.accept(AnyDuplicatesVisitor.ONLY);  // may throw a SyntaxException
        int n = vars.length;
        PureList<Variable> newEnv = env;
        for(int i = n - 1; i >= 0; i--) { newEnv = newEnv.cons(vars[i]); }
        m.body().accept(new CheckVisitor(newEnv));  // may throw a SyntaxException
        return null;
    }

    public Void forIf(If i) {
        i.test().accept(this);
        i.conseq().accept(this);
        i.alt().accept(this);
        return null;
    }

    public Void forLet(Let l) {
        // Check for duplicates in Let vars, check Let rhs's and body using newEnv including let vars
        Variable[] vars = l.vars();
        PureList<Variable> varList = arrayToList(vars);
        varList.accept(AnyDuplicatesVisitor.ONLY);  // may throw a SyntaxException
        AST[] exps = l.exps();
        int n = vars.length;
        PureList<Variable> newEnv = env;
        for(int i = n - 1; i >= 0; i--) { newEnv = newEnv.cons(vars[i]); }
        CheckVisitor newVisitor = new CheckVisitor(newEnv);
        for(int i = 0; i < n; i++) { exps[i].accept(newVisitor); } // may throw a SyntaxException
        l.body().accept(newVisitor); // may throw a SyntaxException
        return null;
    }

    // PROVIDED
    /*  Supports the addition of blocks to Jam */
    public Void forBlock(Block b) {
        AST[] exps =  b.exps();
        int n = exps.length;
        for (int i = 0; i < n; i++) exps[i].accept(this);  // may throw a SyntaxException
        return null;
    }
}

/** The exception class for Jam run-time errors. */
class TypeException extends RuntimeException {
    TypeException(String msg) { super(msg); }
}
