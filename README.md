# Lox-Interpreter

Lox is a dynamically-typed, scripting "mini-language" specifically used as a tool to learn interpreters and compilers. This project is a Lox interpreter in java named
'jlox'.

## Setup
Clone or download the project:
```sh
$ git clone https://github.com/shengbojia/Lox-Interpreter.git
```
Build it using your IDE or command line. **Requires jdk 8+ as I used <> operator a lot**:
```sh
$ javac src/main/java/com/shengbojia/lox/*.java
```

Great! Now that the interpreter is built, let's move on to actually running it.
## Running the interpreter
There are two ways to use the interpreter.
1. Execute it without any arguments:
```sh
$ java Lox
```
This will start a read-eval-print loop (REPL) where you can enter and execute Lox code, one line at a time.
```cmd
Entered REPL mode:
jlox>>
```
For example, entering some Lox code and running it:
```cmd
Entered REPL mode:
jlox>> print "Hello, world.";
Hello, world.
jlox>>
```

2. Execute with one argument, the path to the Lox file you want to interpret:
```sh
$ java Lox <path-to-lox-file>
```
Say for example we wanted to run one of the .lox files in this repo.
hello_world.lox
```python
print "Hello, world.";
```
Then we would do it like so:
```sh
$ java Lox src/main/res/LoxFiles/hello_world.lox
```
Which outputs:
```cmd
Hello, world.

Process finished with exit code 0
```
That's all there is to it. Feel free to add more features to the Lox language if you like.
