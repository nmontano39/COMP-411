nbm1
sec14

# Program Organization

We chose to write our parser to be executed by parseExp() which handles 'let', 'if', 'map', and <binary-exp>
cases for a given lexer. If the first token is 'let' the helper method parseLet() is called to parse and return
exp ::= let <prop-def-list> in <exp>. If the first token is 'if' the helper method parseIf() is called to parse and
return exp :== if <exp> then <exp> else <exp>. If the first token is 'map' the helper method parseMap() is called
to parse and return exp :== map <id-list> to <exp>. Otherwise, the first token is a <binary-exp>. In this case, the
method parses and returns <binary-exp> ::= <term> { <biop> <exp> }*. ExpList and IdList are handled by the parsing
methods for Term and Exp respectively in order to cleanly handle empty list cases. All private methods are named
like parse<Symbol>, i.e. parseExp, parsePropIdList, etc.

exp ::= let <prop-def-list> in <exp>
The helper method parseLet() cycles through one or more Defs and finally makes a recursive call to parseExp().

exp :== if <exp> then <exp> else <exp>
The helper method parseIf() makes three recursive calls to parseExp().

exp :== map <id-list> to <exp>
The helper method parseMap() calls parsePropIdList() makes a recursive call to parseExp().

<binary-exp> ::= <term> { <biop> <exp> }*
This procedure calls helper method parseTerm() to parse and return <term> and optionally checks for a binary
operator before finally making a recursive call to parseExp().



# Testing Processes

As suggested by the sample, we have test sets for valid ("good") and invalid ("bad") input programs corresponding
to each file. In the beginning, we used the provided test cases in order to independently test our private parser
methods. As we were developing  the code, we wrote unit tests to check our progress along the way for rare cases.
However, we found the provided tests to be more comprehensive, and so we ended up deciding to use the test files
given as Strings. We used Intellij's inbuilt plugin to test our code coverage and found that the tests covered 92%
of the code that we have written.
