nbm1
sec14

# Program Organization

We chose to write our parser completely in the Parser java class. First, the method parseExp()
is called which handles 'let', 'if', 'map', and <binary-exp>. If the first token is 'let'
the helper method parseLet() us called to parse and return exp ::= let <prop-def-list> in <exp>.
If the first token is 'if' the helper method parseIf() is called to parse and return
exp :== if <exp> then <exp> else <exp>. If the first token is 'map' the helper method parseMap()
is called to parse and return exp :== map <id-list> to <exp>. Otherwise, the first token is a
<binary-exp>. In this case, the method parses and returns <binary-exp> ::= <term> { <biop> <exp> }*

parseLet()
parseIf()
parseMap()

parseDef()
parseTerm()
parseFactor()
parseIdList()
parseExpList()

However, ExpList and IdList are handled by the parsing methods for Term and Exp respectively in order to
cleanly handle empty list cases. These private methods are named like
parse<Symbol>, i.e. parseExp, parsePropIdList, etc.


# Testing Processes SAMPLE

We have test sets for valid ("good") and invalid ("bad") input programs
corresponding to each parsing method, all of which using the top-level parse
method as opposed to directly calling the private parsing methods. Using the
top level parse method was a good idea, because the parsing methods are
interdependent. We designed the "good" test sets to cover all the basic
branches through the grammar definition for a symbol. We designed the "bad"
test sets to cover all of the basic ways through the definition of a symbol
that could lead to ungrammatical programs.

The basic mechanism that we used for testing a "good" input was to compare the
string representation of the output AST against the expected string (a string
which is very similar to the input program, except for possible differences in
grouping and whitespace). The basic mechanism that we used for testing a "bad"
input was to ensure that a ParseException was thrown while parsing the input.

We used the EMMA code coverage tool, which reports that our test coverage on
our Parser class (parser.java) is 98.1% (we do not have an automated test for
our Parser(String filename) constructor, although we tested this constructor
manually).

We also extracted all of the provided simple test program files and included
them as strings in our JUnit test file, introducing a "good" files test set,
and a "bad" files test set. Once we implemented the change in the extra credit
portion of the assignment, one of the "good" files (hard/08good) became "bad",
since it violates the new grammar by adding the Exp "map x to x", which is not
a Term.

