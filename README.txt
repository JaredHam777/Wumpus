Name: Jared Hamilton
CWID: 10807225
Project: graded (but I may submit 4th project as well)
Language: java
OS: Windows


Command to run: "java -jar Build.jar"
Should already be compiled.

I spent more time on this project than any other project (or final) in my life.  
I implemented a lexer, parser, and expression builder from scratch.
The expression is in the form of an abstract syntax tree.
I then implemented an algorithm to convert any logical expression to CNF form
Once an expression is in CNF from, it runs the DPLL algorithm to see if an expression
in CNF form is logically consistent.  We do this for both (KB & statement) and the (KB & -(statement)).

Unfortunately, I ran out of time, and not all of the test cases pass :( I believe my DPLL algorithm
may have been wrongly implemented.
