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

#### Theoretical introduction

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

Of course, there may be cases where a whitespace isn't the only character delimiting two tokens. Let's take the example of an if statement:

```java
if(flag == true) { ... }
```

The paranthesis (`(`) comes right after the `if` keyword, and they should be considered as two separate tokens. The same for the identifier (`flag`) coming right after the `LPAR`. You should also take these cases into account. Maybe start with considering whitespace as the only delimiter, and later adapt your code to these special cases. For example, you could think that, while building an identifier token, if any operator is to be found (such as `EQUALS` or `ADD`), add the identifier as a new token, and proceed further with the operator. Either go back one character (if the language allows you to do so) and move to the initial state, or simply continue the operator processing in the state of the identifier.

#### Implementing a FSM in Java

First, you would need to think how the states are going to be represented inside your lexer. The basic "C" way would be to use numerical values assigned for each state.

```java
int state = 0;
```

However, this may become a problem, as at a certain point in the debugging process you won't know if the state of the lexer is the expected one or not. Let's say your lexer is in the state `21`. What does that mean? You would probably need to map those numbers to some strings, or better, to **enums**.

```java
enum LexerState {
  INIT, ... // Incrementally add the next states
}
```

At the start of the Lexer logic, you'll need to initialize a variable depicting the current state on `INIT`:

```java
LexerState currentState = LexerState.INIT;
```

Moving further, the `Token` item could be a structured data type, such as a `class` or a `struct` (if coding in C). It should contain both the type of the token (e.g. identifier, keyword, delimiter, operator, number, string) and its value. For certain tokens, such as keywords or operators, there will be no value. But for constants such as `CT_INT` or `CT_REAL` (see the lexical rules at the beginning), you would also need to store their value for later use (code generation).

```java
class Token {
  private TokenType type;
  private Object value;
}
```

`value` is stored as an `Object`, as it may hold anything including `Integer`, `Double` or `String` (for identifiers) instances. You may need to cast those values to their specific class, at the code generation phase. The casting is done based on the `TokenType` enum, which will define all of the possible token types (again, take a look over the lexical rules). Enums are the best way of implementing this, as you are only allowed to store manually defined values for the field. You could, of course, use integers (e.g. `0`, `1`) or even strings (e.g. `"identifier"`, `"CT_INT"`) for representing the types of the token, but the first case would cause the same problem we've already discussed at the representation of the state, while using strings could lead to storing inconsistent (unexpected) values for the `type` field (you'll store `"ct_int"` because you forgot your CapsLock on, and inside your code you check for `"CT_INT"`, which will obviously fail).

For the FSM part, it would be suggested to have a method that accepts an input (a file, a path to a file, or directly the content of the file, stored as a `String`). You'll read the input character by character and adapt your states, based on that.

```java
public List<Token> analyze(String input) {
  LexerState currentState = LexerState.INIT;

  int i = 0;
  StringBuilder currentToken; // or simply 'String', but StringBuilder has been used for memory efficiency

  while (i < input.length()) { // until end of file
    char ch = input.charAt(i); // current character on position 'i'

    switch(currentState) {
      case INIT -> {
        currentToken = new StringBuilder();

        if (Character.isAlphabetic(ch)) { // allowed: this may be the start of an identifier or keyword
          currentToken.append(ch); // build the current token value
          nextState = LexerState.FIRST_LETTER; // you should now start restrictions: for example, starting with a letter would mean that you cannot append a " character to the existing token
        }
        else if (...) {
          // Do further processing
        }
      }
      case FIRST_LETTER -> {
        ...
      }
    }
}
```

You should now continue adapting your lexical analyzer to the other lexical rules. After feeling confident enough, you should test your lexer on the [test programs](https://github.com/mateasmario/ct/tree/main/test_programs). A proper way to test it would be to print the list of tokens collected after the `analyze` phase. If something's off, go on and use the debugger. Don't be shy!

### Syntactical analysis

### Semantical analysis

## Synthesis phase

## Other information
