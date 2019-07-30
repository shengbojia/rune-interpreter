# Rune Interpreter

Rune is a dynamically-typed, OOP "mini-language" I made to learn interpreters and compilers.

## Table of Contents
* [The Interpreter](#the-interpreter)
  * [Setup](#setup)
  * [Running the interpreter](#running-the-interpreter)
* [The Rune Language](#the-rune-language)
  * [Intro to Rune](#an-introduction-to-rune)
  * [Data Types](#data-types)
  * [Expressions](#expressions)
    * [Arithmetic](#arithmetic)
    * [Comparison & Equality](#comparison--equality)
      * [Truthiness in Rune](#what-is-truthy-in-rune)

## The Interpreter

### Setup
Clone or download the project:
```sh
$ git clone https://github.com/shengbojia/rune-interpreter.git
```
Build it using your IDE or command line. **Requires jdk 8+ as I used <> operator a lot**:
```sh
$ javac src/main/java/com/shengbojia/rune/*.java
```

Great! Now that the interpreter is built, let's move on to actually running it.
### Running the interpreter
There are two ways to use the interpreter.
1. Execute it without any arguments:
```sh
$ java Rune
```
This will start a read-eval-print loop (REPL) where you can enter and execute Rune code, one line at a time.
```cmd
Entered REPL mode:
rune>>
```
For example, entering some Rune code and running it:
```cmd
Entered REPL mode:
rune>> print "Hello, world.";
Hello, world.
rune>>
```

2. Execute with one argument, the path to the ```.rune``` file you want to interpret:
```sh
$ java Rune <path-to-rune-file>
```
Say for example we wanted to run one of the ```.rune``` files in this repo.
hello_world.rune
```C
print "Hello, world.";
```
Then we would do it like so:
```sh
$ java Rune src/main/res/RuneFiles/hello_world.rune
```
Which outputs:
```cmd
Hello, world.

Process finished with exit code 0
```
That's all there is to using the interpreter.

## The Rune Language

### An Introduction to Rune
```C
// Example code, notice the C-style comments and semi-colon ending
print "Hello, world.";
```
A dynamically-typed language with operators and syntax very similar to C.

### Data Types
Some fundamental, built-in data types.
#### Boolean
Dedicated boolean type with two literal values, true and false.
```C
true;
false;
```
#### Numbers
Rune only has double-precision floats, which represent both integers and decimals.
```C
42;   // an integer
4.2;  // a decimal
```
#### Strings
String literals are enclosed in double-quotes just like in C.
```C
"This is a string.";
"42"; // string, not a number
"";   // empty string
```
#### Nil
```nil``` is the Rune equivalent of Java or C's ```null```

### Expressions
Common expressions found in all sorts of languages.
#### Arithmetic
```C
3 + x;
y - 5;
i * j;
4 / 2;
-negateMe;
```
All these operators work on numbers and it is an error to pass any other types to them. The exception being ```+``` which can take strings and concatenate them.

#### Comparison & Equality
```C
1 < 2;   // true
3 <= 3;  // true
4 > 4;   // false
-1 >= 0; // false
```
And ```==``` works similar to java's ```equals()```:
```C
1 == 2;         // false
"cat" == "cat"; // true (remember '==' is like java's equals() rather than java's '==')
42 == "cat";    // false
42 == "42";     // false (different types are never equal)
nil == nil;     // true (nil is ONLY equal to nil)
```

#### Logical Operators
The not operator `!` returns `false` if the operand is true, and vice versa:
```C
!true;  // evaluates to false
!false; // evaluates to true
```
Rune uses keywords ```and``` and ```or``` to represent similar C operators ```&&``` and ```||```:
```Kotlin
true and false; // false
true and true;  // true

false or false; // false
false or true;  // true
```
Both operators **short circuit**, meaning if the left operand of ```and``` is false, the expression automatically evalutes to false and the right operand is discarded without even being evaluated. In a similar fashion, for ```or``` if the left operand is true then the expression automatically evalutes to true and the right oprand is discarded.

##### What is truthy in Rune?
Rune follows Ruby's rule: `false` and `nil` are *falsey* and everything else is *truthy*. (meaning treated as `false` or `true` by logical operators, respectively)

#### Precedence
Operator precendence is very similar to C, as can be seen in this precendence table:

<table>
    <thead>
        <tr>
            <th>Precendence</th>
            <th>Operator</th>
            <th>Description</th>
            <th>Associativity</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=2>1</td>
            <td>()</td>
            <td>Function call</td>
            <td rowspan=2>left-to-right</td>
        </tr>
        <tr>
            <td>.</td>
            <td>Member access</td>
        </tr>
        <tr>
            <td rowspan=2>2</td>
            <td>!</td>
            <td>Logical NOT</td>
            <td rowspan=2>right-to-left</td>
        </tr>
        <tr>
            <td>-</td>
            <td>Unary minus</td>
        </tr>
        <tr>
            <td>3</td>
            <td>* &nbsp;/</td>
            <td>Multiplication and division</td>
            <td rowspan=6>left-to-right</td>
        </tr>
        <tr>
          <td>4</td>
          <td>+ &nbsp;-</td>
          <td>Addition and subtraction</td>
        </tr>
        <tr>
          <td>5</td>
          <td>&lt; &nbsp;&lt;=<br>&gt; &nbsp;&gt;= </td>
          <td>Comparison operators</td>
        </tr>
        <tr>
          <td>6</td>
          <td>== &nbsp;!=</td>
          <td>Equals and not equals.</td>
        </tr>
        <tr>
          <td>7</td>
          <td>and</td>
          <td>Logical AND</td>
        </tr>
        <tr>
          <td>8</td>
          <td>or</td>
          <td>Logical OR</td>
        </tr>
        <tr>
          <td>9</td>
          <td>lambda</td>
          <td>Lambda expression</td>
          <td>n/a</td>
        </tr>
        <tr>
          <td>10</td>
          <td>?:</td>
          <td>Ternary conditional</td>
          <td rowspan=2>right-to-left</td>
        </tr>
        <tr>
          <td>11</td>
          <td>=</td>
          <td>Assignment</td>
        </tr>
        <tr>
          <td>12</td>
          <td>,</td>
          <td>C-style comma operator</td>
          <td>left-to-right</td>
        </tr>
    </tbody>
</table>
