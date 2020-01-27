 /** Parser for Assignment 2 */

/** Parser for Assignment 2 */

import java.io.*;
import java.util.*;

/** Each parser object in this class contains an embedded lexer which contains an embedded input stream.  The
 * class include a parse() method that will translate the program text in the input stream to the corresponding
 * AST assuming that the program text forms a syntactically valid Jam program.
 */
class Parser {

    private Lexer in;

    Parser(Lexer i) { in = i; }

    Parser(Reader inputStream) { this(new Lexer(inputStream)); }

    Parser(String fileName) throws IOException { this(new FileReader(fileName)); }

    Lexer lexer() { return in; }


    /** Parses the program text in the lexer bound to 'in' and returns the corresponding AST.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     */
    public AST parse() throws ParseException {
        // parse text
        return parseExp(in.readToken());
    }


    /** Parses:
     *     <exp> :: = let <prop-def-list> in <exp>
     *              | if <exp> then <exp> else <exp>
     *              | map <id-list> to <exp>
     *              | <binary-exp>
     *     <binary-exp> ::=  <term> { <biop> <exp> }*
     *
     * @return  the corresponding AST.
     */
    private AST parseExp(Token token) {

        // if current token is 'let'
        if (token == Lexer.LET) { return parseLet(in.readToken()); }

        // if current token is 'if'
        if (token == Lexer.IF) return parseIf(in.readToken());

        // if current token is 'map'
        if (token == Lexer.MAP) return parseMap(in.readToken());


        // else current token is a term
        AST term = parseTerm(token);

        // if next token is a BinOp
        if (in.peek() instanceof Op) {
            Op binOp = (Op) in.readToken();
            return new BinOpApp(binOp, term, parseExp(in.readToken()));
        } else {
            return term;
        }
    }

    /** Parses:
     *   <def>  ::= <id> := <exp> ;
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     * @return  the corresponding Def.
     */
    private Def parseDef(Token token) {

        // create Variable out of current token
        Variable v = (Variable) token;
        Token next = in.readToken();

        // if next token is ':='
        if (next == Lexer.BIND) {

            // create exp using next token
            next = in.readToken();
            AST exp = parseExp(next);
            next = in.readToken();

            // if next token is ';'
            if (next instanceof SemiColon) {

                // return new Def using v and exp
                return new Def(v, exp);
            }
        }

        // if we reach here, throw error
        error();
        return null;
    }

    /** Parses:
     *   <exp>  ::= let <prop-def-list> in <exp>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     * @return  the corresponding AST.
     */
    private AST parseLet(Token token) {

        // cycle through Defs
        if (token instanceof Variable) {

            // create new list of Defs using token next
            ArrayList<Def> defs = new ArrayList<Def>();
            defs.add(parseDef(token));
            token = in.readToken();

            // while next token is also a Def
            while (token instanceof Variable) {

                // add that token to defs
                defs.add(parseDef(token));
                token = in.readToken();
            }

            // check next token for 'in'
            if (token == Lexer.IN) {

                // return new Let using defs and exp
                return new Let(defs.toArray(new Def[0]), parseExp(in.readToken()));
            }
        }

        // if we reach here, throw error
        error();
        return null;
    }


    /** Parses:
     *   <exp>  ::= if <exp> then <exp> else <exp>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     * @return  the corresponding AST.
     */
    private AST parseIf(Token token) {

        // create exp0 using current token
        AST exp0 = parseExp(token);
        Token tokenThen =  in.readToken();

        // if token is 'then'
        if (tokenThen == Lexer.THEN) {

            // create exp1 using next token
            Token tokenExp = in.readToken();
            AST exp1 = parseExp(tokenExp);
            Token tokenElse = in.readToken();

            // if token is 'else'
            if (tokenElse == Lexer.ELSE) {

                // create exp2 using next token
                AST exp2 = parseExp(in.readToken());

                // return new If using exp0, exp1, exp2
                return new If(exp0, exp1, exp2);
            }
        }

        // if we reach here, throw error
        error();
        return null;
    }


    /** Parses:
     *   <exp>  ::= map <id-list> to <exp>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     * @return  the corresponding AST.
     */
    private AST parseMap(Token token) {

        // if current token is 'to'
        if (token == Lexer.TO) {

            // return map using empty list and exp
            return new Map(new Variable[0], parseExp(in.readToken()));

        } else {
            // parse id list
            Variable[] idList = parseIdList(token);
            token =  in.readToken();

            // if next token is 'to'
            if (token == Lexer.TO) {

                // return new Map using idlist and exp
                token = in.readToken();
                return new Map(idList, parseExp(token));
            }
        }

        // if we reach here, throw error
        error();
        return null;
    }


    /** Parses:
     *  <term>     ::= <unop> <term> | <constant> | <factor> {( <exp-list> )}
     *  <constant> ::= <null> | <int> | <bool>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @return  the corresponding AST.
     */
    private AST parseTerm(Token token) {

        // if current token is Op
        if (token instanceof Op) {
            Op op = (Op) token;
            if (! op.isUnOp()) error();
            return new UnOpApp(op, parseTerm(in.readToken()));
        }

        // if current token is Constant
        if (token instanceof Constant) return (Constant) token;

        // else current token is Term
        AST factor = parseFactor(token);
        Token next = in.peek();
        if (next == LeftParen.ONLY) {
            in.readToken();  // remove next from input stream
            AST[] exps = parseExpList(in.readToken());  // including closing paren
            return new App(factor,exps);
        }
        // return single factor
        return factor;
    }


    /** Parses:
     *   <factor>  ::= ( <exp> ) | <prim> | <id>
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     * @return  the corresponding AST.
     */
    private AST parseFactor(Token token) {

        // if current token is left paren
        if (token == LeftParen.ONLY) {
            token = in.readToken();

            // reads exp
            AST exp = parseExp(token);

            // reads right paren
            in.readToken();

            return exp;
        }

        // if token is a Prim
        if (token instanceof PrimFun) {
            return (PrimFun) token;
        }

        // if token is a Def
        if (token instanceof Variable) {
          return (Variable) token;
        }

        // if we reach here, throw error
        error();
        return null;
    }


    /** Parses:
     *   <exp-list>  ::= { <prop-exp-list> }
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @return  the corresponding AST[]
     */
    private AST[] parseExpList(Token token) {
        return parsePropExpList(token);
    }

    /** Parses:
     *   <prop-exp-list>  ::= <exp> { , <exp> }*
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @return  the corresponding AST[]
     */
    private AST[] parsePropExpList(Token token) {

        // create new exp list and add exp using current token
        ArrayList<AST> exps = new ArrayList<AST>();
        exps.add((AST) parseExp(token));

        // while next token is a comma
        while (in.readToken() instanceof Comma) {

            // add exp using current token
            token = in.readToken();
            exps.add((AST) parseExp(token));
        }

        // return AST list using exp list
        return (AST[]) exps.toArray(new AST[0]);
    }


    /** Parses:
     *   <id-list>  ::= { <prop-id-list> }
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @return  the corresponding AST[]
     */
    private Variable[] parseIdList(Token token) {
        return parsePropIdList(token);
    }

    /** Parses:
     *   <prop-id-list>  ::= <id> { , <id> }*
     * @param token   first token in input stream to be parsed; remainder in Lexer named in.
     * @return  the corresponding AST[]
     */
    private Variable[] parsePropIdList(Token token) {

        // create new id list
        ArrayList<Variable> ids = new ArrayList<Variable>();

        // add id using current token
        if (token instanceof Variable) {
            ids.add((Variable) token);
        } else {
          error();
        }

//      token = in.readToken();
        if (in.peek() instanceof Comma) {
          token = in.readToken();
          System.out.println("This should be a comma " + token.toString());
          while (token instanceof Comma) {
            if (in.peek() instanceof Variable) {
              token = in.readToken();
              System.out.println("This should be a variable " + token.toString());
              ids.add((Variable) token);

              if (in.peek() instanceof Comma) {
                token = in.readToken();
                System.out.println("This should be a comma " + token.toString());
              }
            } else {
              System.out.println("Error in while" + token.toString());
              error();
            }

            // add id using current token
//            token = in.readToken();
//            in.readToken();
//            if (in.peek() instanceof Variable) {
//            ids.add((Variable) in.readToken());
//            }
//            token = in.readToken();
          }

        }

        // while next token is a comma


        // return Variable list using id list
        return (Variable[]) ids.toArray(new Variable[0]);

    }


    /**
     * @throws ParseException if a syntax error is encountered (including lexical errors).
     */
    private void error() throws ParseException {
        throw new ParseException("error");
    }

}
