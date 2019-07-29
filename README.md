# Rune Interpreter

Rune is a dynamically-typed, OOP "mini-language" I made to learn interpreters and compilers.

## Table of Contents
* [The Interpreter](#the-interpreter)
  * [Setup](#setup)
  * [Running the interpreter](#running-the-interpreter)
* [The Rune Language](#the-rune-language)

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

2. Execute with one argument, the path to the .rune file you want to interpret:
```sh
$ java Rune <path-to-rune-file>
```
Say for example we wanted to run one of the .rune files in this repo.
hello_world.rune
```python
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
```
// Example code, notice the C-style comments and semi-colon ending
print "Hello, world.";
```
A dynamically-typed language with operators and syntax very similar to C.

### Data Types
#### Boolean
Dedicated boolean type with two literal values, true and false.
```
true;
false;
```
#### Numbers
Rune only has double-precision floats, which represent both integers and decimals.
```
42;   // an integer
4.2;  // a decimal
```
#### Strings
String literals are enclosed in double-quotes just like in C.
```
"This is a string.";
"42"; // string, not a number
"";   // empty string
```
