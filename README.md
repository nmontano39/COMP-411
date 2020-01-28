nbm1
sec14

# Program Organization

We chose to write our parser to be executed by parseExp() which handles 'let', 'if', 'map', and <binary-exp>
cases for a given lexer. If the first token is 'let' the helper method parseLet() is called to parse and return
exp ::= let <prop-def-list> in <exp>. If the first token is 'if' the helper method parseIf() is called to parse and
return exp :== if <exp> then <exp> else <exp>. If the first token is 'map' the helper method parseMap() is called
to parse and return exp :== map <id-list> to <exp>. Otherwise, the first token is a <binary-exp>. In this case, the
method parses and returns <binary-exp> ::= <term> { <biop> <exp> }*.

However, ExpList and IdList are handled by the parsing methods for Term and Exp respectively in order to
cleanly handle empty list cases. These private methods are named like
parse<Symbol>, i.e. parseExp, parsePropIdList, etc.


# Testing Processes

As suggested by the sample, we have test sets for valid ("good") and invalid ("bad")
input programs corresponding to each parsing method, all of which using the top-level parse
method as opposed to directly calling the private parsing methods. We followed this process because, as mentioned
in the assignment, using the top level parse method was a good idea as the parsing methods are interdependent.


For the most part, we used the provided test cases in order to test our program. As we were developing the code,
we wrote unit tests to check our progress along the way. However, we found the provided tests to be more comprehensive,
and so we ended up deciding to use those. We used Intellij's inbuilt plugin to test our code coverage and found that the
tests covered 92% of the code that we have written.