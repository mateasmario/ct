# Compilation Techniques Lab

## Introduction

The purpose of this laboratory is to write your own implementation of a compiler. The language used throughout the documentation is a simplified version of C, called **AtomC**. The lexical, syntactical and semantical rules are based on the AtomC definition from Răzvan
Aciu's website, which is no longer to be found publicly, only through a [Web Archive snapshot](https://web.archive.org/web/20220709070356/https://sites.google.com/site/razvanaciu/limbaje-formale-si-tehnici-de-compilare).

The compiling process is split between the ***analysis phase***, which has to do with the code parsing, making sure that the formal language rules are respected, and the ***synthesis phase***, which implies aspects such as code generation and optimization.

Although the phases of a popular compiler are far more advanced than the ones presented in this lab, implementing the requirements below should give you a good grasp on how compilers (and interpreters) do their job.

## Analysis phase

The analysis phase consists of three types of analysis:

1. Lexical analysis
2. Syntactical analysis
3. Semantical analysis

### Lexical analysis

The lexical analyzer's function is to divide the input source code into logical units called "tokens", which are logical units meaningful to a certain programming language. Tokens can be anything from identifiers (e.g. `bar`, `foo`) to reserved keywords (e.g. `int`, `char`, `double`), operators (e.g. `+`, `/`, `&&`) or constants (e.g. `5`, `"username"`, `0x4F`).

The analyzer makes sure that tokens have a correct format and no forbidden symbols have been used in the input. For example, an identifier cannot start with a digit, or a number in a hexadecimal format cannot contain letters that come after "F". The parsing is done according to certain lexical rules, which are written in a [REGEX](https://en.wikipedia.org/wiki/Regular_expression)-like format.

The entire list of AtomC's lexical rules can be found [here](LEXICAL_RULES.md), and should be implemented using a [FSM (Finite-state machine)](https://en.wikipedia.org/wiki/Finite-state_machine). One may start with an initial state, and move to other corresponding states based on every new character read from the input. A certain state only allows a set of possible inputs, leading to the failure of the entire compilation process, if an invalid character is read.

A good start for a FSM approach of your lexical analyzer would be an `INIT` state (you can also represent the states through numbers, or enums), having the following rules:
```
if state is INIT:
  if character is a letter:
    append character to current token value
    next state: WORD
  if character is a digit:
    append character to current token value
    next state: NUMBER
```
then, the rules of the `NUMBER` state should look like this:
```
if state is NUMBER:
  if character is a number:
    append character to current token value
    next state: NUMBER
  if character is a letter:
    failure
```

As you can observe, once a digit has been typed in, no letters are further allowed (e.g. `5h` is considered as an invalid token). Of course, there could be certain combinations of numbers and letters allowed (such as `0x5F` for the hexadecimal representation). You should adapt the lexical rules based on the requirements document.

You should also consider resetting to the initial (`INIT`) state after the current token has ended. A convention could be that a whitespace character (such as space, newline or tab) delimits two tokens. Adapting the rules to register the token and set the next state to `INIT` if any of those delimiting characters is read, should do the job.

```
if state is REAL_NUMBER:
  if character is a whitespace:
    store the token in memory
    next state: INIT
```

The `Token` could be a structured data type, such as a `class` or a `struct`. It should contain both the type of the token (e.g. identifier, keyword, delimiter, operator, number, string) and its value. 

### Syntactical analysis

### Semantical analysis

## Synthesis phase

## Other information
