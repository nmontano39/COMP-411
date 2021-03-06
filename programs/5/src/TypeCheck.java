import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.List;

/** A visitor class for the type checker. Returns normally unless there is a type error. On a type error,
 * throws a TypeException.
 */
class TypeCheckVisitor implements ASTVisitor<Type> {
    /** Empty symbol table. */
    private static final Empty<TypedVariable> EMPTY_VARS = new Empty<TypedVariable>();

    /** Symbol table to detect free typedvariables. */
    PureList<TypedVariable> env;

    /** Root form of CheckVisitor. */
    public static final TypeCheckVisitor INITIAL = new TypeCheckVisitor(EMPTY_VARS);

    TypeCheckVisitor(PureList<TypedVariable> e) { env = e; }

    /** Helper method that converts an array to a PureList. */
    public static <T> PureList<T> arrayToList(T[] array) {
        int n = array.length;
        PureList<T> list = new Empty<T>();
        for(int i = n - 1; i >= 0; i--) { list = list.cons(array[i]); }
        return list;
    }

    /*  Visitor methods. */
    public IntType forIntConstant(IntConstant i) { return IntType.ONLY; }
    public BoolType forBoolConstant(BoolConstant b) { return BoolType.ONLY; }
    public Type forNullConstant(NullConstant n) { return ((TypedNullConstant) n).type(); }

    // look up variable in environment and returns its type
    public Type forVariable(Variable v) { return env.accept(new LookupVisitor<>(v)).type(); }

    // if a primitive function is not given arguments then throw a TypeException
    public Type forPrimFun(PrimFun f) { throw new TypeException("Primitive function is not followed by an application argument list"); }

    /*  Supports the addition of unary operators in Jam */
    public Type forUnOpApp(UnOpApp u) {
        Type argType = u.arg().accept(this);  // may throw a TypeException
        return u.rator().accept(new UnOpVisitor<Type>() {
            @Override
            public Type forUnOpPlus(UnOpPlus op) {
                if (!(argType.equals(IntType.ONLY))) {
                    throw new TypeException("UnOp " + op + " applied on incorrect type " + argType);
                }
                return IntType.ONLY;
            }

            @Override
            public Type forUnOpMinus(UnOpMinus op) {
                if (!(argType.equals(IntType.ONLY))) {
                    throw new TypeException("UnOp " + op + " applied on incorrect type " + argType);
                }
                return IntType.ONLY;
            }

            @Override
            public Type forOpTilde(OpTilde op) {
                if (!(argType.equals(BoolType.ONLY))) {
                    throw new TypeException("UnOp " + op + " applied on incorrect type " + argType);
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpBang(OpBang op) {
                if (!(argType instanceof RefType)) {
                    throw new TypeException("UnOp " + op + " applied on incorrect type " + argType);
                }
                return ((RefType) argType).refType();
            }

            @Override
            public Type forOpRef(OpRef op) {
                return new RefType(argType);
            }
        });
    }

    /*  Supports the addition of binary operators in Jam */
    public Type forBinOpApp(BinOpApp b) {
        Type t1 = b.arg1().accept(this); // may throw a TypeException
        Type t2 = b.arg2().accept(this); // may throw a TypeException
        return b.rator().accept(new BinOpVisitor<Type>() {
            @Override
            public Type forBinOpPlus(BinOpPlus op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return IntType.ONLY;
            }

            @Override
            public Type forBinOpMinus(BinOpMinus op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return IntType.ONLY;
            }

            @Override
            public Type forOpTimes(OpTimes op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return IntType.ONLY;
            }

            @Override
            public Type forOpDivide(OpDivide op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return IntType.ONLY;
            }

            @Override
            public Type forOpEquals(OpEquals op) {
                if (!(t1.equals(t2))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpNotEquals(OpNotEquals op) {
                if (!(t1.equals(t2))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpLessThan(OpLessThan op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpGreaterThan(OpGreaterThan op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpLessThanEquals(OpLessThanEquals op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpGreaterThanEquals(OpGreaterThanEquals op) {
                if (!(t1.equals(IntType.ONLY) && t2.equals(IntType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpAnd(OpAnd op) {
                if (!(t1.equals(BoolType.ONLY) && t2.equals(BoolType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpOr(OpOr op) {
                if (!(t1.equals(BoolType.ONLY) && t2.equals(BoolType.ONLY))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return BoolType.ONLY;
            }

            @Override
            public Type forOpGets(OpGets op) {
                if (!(t1 instanceof RefType)) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                RefType r1 = (RefType) t1;
                if (!(r1.refType().equals(t2))) {
                    throw new TypeException("BinOp " + op + " applied on incorrect type");
                }
                return UnitType.ONLY;
            }
        });
    }

    /*  Supports the addition of apps in Jam */
    public Type forApp(App a) {
        AST[] args = a.args();
        int n = args.length;

        if (a.rator() instanceof PrimFun) {
            PrimFun rator = (PrimFun) a.rator();
            Type firstType = args[0].accept(this);
            ASTVisitor<Type> myVisitor = this;
            return rator.accept(new PrimFunVisitor<Type>() {
                @Override
                public Type forConsPPrim() {
                    return BoolType.ONLY;
                }

                @Override
                public Type forNullPPrim() {
                    return BoolType.ONLY;
                }

                @Override
                public Type forConsPrim() {
                    Type restType = args[1].accept(myVisitor);
                    if (args[1] instanceof TypedNullConstant) {
                        // Changed Null Type to reflect list type rather than just type
                        // (See TypedNullConstant)
                        Type NullType = ((TypedNullConstant) args[1]).type();
                        NullType = ((ListType) NullType).listType();
                        if (NullType.equals(firstType)) {
                            return new ListType(firstType);
                        } else {
                            throw new TypeException("List type not consistent");
                        }
                    } else if (restType instanceof ListType) {
                        if (((ListType) restType).listType().equals(firstType)) {
                            return new ListType(firstType);
                        } else {
                            throw new TypeException("List type not consistent");
                        }
                    } else {
                        throw new TypeException("List type not consistent");
                    }
                }

                @Override
                public Type forFirstPrim() {
                    if (!(firstType instanceof ListType)) {
                        throw new TypeException("Prim fun first applied on something that's not a list");
                    }
                    return ((ListType) firstType).listType();
                }

                @Override
                public Type forRestPrim() {
                    return firstType;
                }
            });
        }

        Type ratorType = a.rator().accept(this);
        FunType ratorFunType = (FunType) ratorType;

        for(int i = 0; i < n; i++) {
            Type funArgType = ratorFunType.paramType()[i];
            Type argType = args[i].accept(this);
            if (!(funArgType.equals(argType))) {
                throw new TypeException("Argument for App has incorrect type");
            }
        }
        return ratorFunType.outType();
    }

    /*  Supports the addition of map in Jam */
    public Type forMap(Map m) {
        ArrayList<Type> listTypes = new ArrayList<>();
        Variable[] vars = m.vars();
        PureList<TypedVariable> newEnv = env;

        // Add each variable to the env
        for (int i = 0; i < vars.length; i++) {
            TypedVariable typedVar = (TypedVariable) vars[i];
            listTypes.add(typedVar.type());
            newEnv = newEnv.cons(typedVar);
        }
        TypeCheckVisitor newVisitor = new TypeCheckVisitor(newEnv);
        Type t = m.body().accept(newVisitor);
        return new FunType(listTypes.toArray(new Type[0]), t);
    }

    /*  Supports the addition of if in Jam */
    public Type forIf(If i) {
        i.test().accept(this);
        i.conseq().accept(this);
        return i.alt().accept(this);
    }

    /*  Supports the addition of let in Jam */
    public Type forLet(Let l) {
        Variable[] vars = l.vars();
        AST[] exps = l.exps();
        int n = vars.length;
        PureList<TypedVariable> newEnv = env;
        TypeCheckVisitor newVisitor = new TypeCheckVisitor(newEnv);
        for (int i = 0; i < n; i++) {
            newEnv = newEnv.cons((TypedVariable) vars[i]);
            newVisitor = new TypeCheckVisitor(newEnv);
            Type expType = exps[i].accept(newVisitor);

            if (!(expType.equals(((TypedVariable) vars[i]).type()))) {
                throw new TypeException("Incorrect type in Def in Let");
            }
        }
        return l.body().accept(newVisitor);
    }

    /*  Supports the addition of blocks to Jam */
    public Type forBlock(Block b) {
        AST[] exps =  b.exps();
        int n = exps.length;
        Type t = exps[0].accept(this);
        for (int i = 1; i < n; i++) {
            t = exps[i].accept(this);
        }
        return t;
    }
}

/** The exception class for Jam run-time errors. */
class TypeException extends RuntimeException {
    TypeException(String msg) { super(msg); }
}
