package src; /** Parser for Assignment 2 */

/** Parser for Assignment 2 */

import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
 * class include a parse() method that will translate the program text in the input stream to the corresponding
 * AST assuming that the program text forms a syntactically valid Jam program.
 */
class Parser {

    private Lexer in;

    Parser(Lexer i) {
        in = i;
        initParser();
    }

    Parser(Reader inputStream) { this(new Lexer(inputStream)); }

    Parser(String fileName) throws IOException { this(new FileReader(fileName)); }

    Lexer lexer() { return in; }

    private void initParser() {
        //initialize Parser

    }


    /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     */
    public AST parse() throws ParseException {
        //parse text
        return parseExp(in.readToken());
    }


    /** Parses:
     *     <exp> :: = if <exp> then <exp> else <exp>
     *              | let <prop-def-list> in <exp>
     *              | map <id-list> to <exp>
     *              | <binary-exp>
     *     <binary-exp> ::=  <term> { <biop> <exp> }*
     *
     * @return  the corresponding AST.
     */
    private AST parseExp(Token token) {

        // let <prop-def-list> in <exp>
        if (token == Lexer.LET) {
            // cycle through Defs
            Token next = in.readToken();

            if (next instanceof Variable) {

                // Changed this to array list because above isn't the way to initialize an array.
                ArrayList<Def> defs = new ArrayList<Def>();
                defs.add(parseDef(next));
                next = in.readToken();

                while (next instanceof Def) {
                    defs.add(parseDef(next));
                    next = in.readToken();
                }

                // check next token for 'in'
                if (next == Lexer.IN) {
                    Def[] newDef = defs.toArray(new Def[0]);
                    return new Let(newDef, parseExp(in.readToken()));
                }
            }
        }

        // if <exp> then <exp> else <exp>
        if (token == Lexer.IF) return parseIf(in.readToken());

        // map <id-list> to <exp>
        if (token == Lexer.MAP) return parseMap(in.readToken());


        // <binary-exp> ::=  <term> { <biop> <exp> }*
        AST term = parseTerm(token);

        if (in.peek() instanceof Op) {
            Op binOp = (Op) in.readToken();
            return new BinOpApp(binOp, term, parseExp(in.readToken()));
        }
        return term;

    }


    private Def parseDef(Token token) {
        Variable v = (Variable) token;
        Token next = in.readToken();

        if (next == Lexer.BIND) {

            next = in.readToken();
            AST exp = parseExp(next);

            next = in.readToken();

            if (next instanceof SemiColon) {
                return new Def(v, exp);
            }
        }
        error();
        return null;
    }



    /* You may find it helpful to define separate parse methods for <binary-exp>, if expressions, and map expressions.
     * This is a stylistic choice. */

    // token instanceof 'if'
    private AST parseIf(Token token) {

        AST exp0 = parseExp(token);
        Token tokenThen =  in.readToken();

        if (tokenThen == Lexer.THEN) {
          Token tokenExp = in.readToken();
          AST exp1 = parseExp(tokenExp);
          Token tokenElse = in.readToken();

          if (tokenElse == Lexer.ELSE) {
            AST exp2 = parseExp(in.readToken());
            return new If(exp0, exp1, exp2);
          }
        }
        error();
        return null;
    }


    // token instanceof 'map'
    private AST parseMap(Token token) {
        if (token == Lexer.TO) {
            return new Map(new Variable[0], parseExp(in.readToken()));
        } else {
            Variable[] idList = parseIdList(token);
            token =  in.readToken();

            // next token instanceof 'to' and next next instance of Exp
            if (token == Lexer.TO) {
                token = in.readToken();
                return new Map(idList, parseExp(token));
            }
        }
        error();
        return null;
    }

    // token instanceof binop
    private AST parseBin(Token token) {
    Op op = (Op) token;
    if (! op.isBinOp()) error();
    return new BinOpApp(op, parseExp(token), parseExp(in.readToken()));
    }


    // Factor  ::= ( Exp ) | Prim | Id
    private AST parseFactor(Token token) {
        // reads left paren
        if (token == LeftParen.ONLY) {
            token = in.readToken();

            // reads exp
            AST exp = parseExp(token);

            // reads right paren
            in.readToken();

            return exp;
        }
        if (token instanceof PrimFun) {
            return (PrimFun) token;
        }
        if (token instanceof Variable) {
          return (Variable) token;
        }
        error();
        return null;
    }

    private AST[] parseExpList(Token token) {
        return parsePropExpList(token);
    }

    private AST[] parsePropExpList(Token token) {

        ArrayList<AST> exps = new ArrayList<AST>();
        exps.add((AST) parseExp(token));

        while (in.readToken() instanceof Comma) {
          token = in.readToken();
          exps.add((AST) parseExp(token));
        }

        return (AST[]) exps.toArray(new AST[0]);
    }

    private Variable[] parseIdList(Token token) {
        return parsePropIdList(token);
    }

    private Variable[] parsePropIdList(Token token) {

        ArrayList<Variable> ids = new ArrayList<Variable>();

        if (token instanceof Variable) {
            ids.add((Variable) token);
        }

        while (in.peek() instanceof Comma) {
            token = in.readToken();

            if (in.peek() instanceof Variable) {
                ids.add((Variable) token);
            }

            token = in.readToken();
        }

        return (Variable[]) ids.toArray(new Variable[0]);

    }


    /** Parses:
     *  <term>     ::= <unop> <term> | <constant> | <factor> {( <exp-list> )}
     *  <constant> ::= <null> | <int> | <bool>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     */
    private AST parseTerm(Token token) {

        if (token instanceof Op) {
            Op op = (Op) token;
            if (! op.isUnOp()) error();
            return new UnOpApp(op, parseTerm(in.readToken()));
        }

        if (token instanceof Constant) return (Constant) token;

        AST factor = parseFactor(token);
        Token next = in.peek();
        if (next == LeftParen.ONLY) {
            in.readToken();  // remove next from input stream
            AST[] exps = parseExpList(in.readToken());  // including closing paren
            return new App(factor,exps);
        }
        return factor;
    }

    private void error() throws ParseException {
        throw new ParseException("error");
    }
}
