# Rune Interpreter

Rune is a dynamically-typed, OOP "mini-language" I made to learn interpreters and compilers.

## Setup
Clone or download the project:
```sh
$ git clone https://github.com/shengbojia/rune-interpreter.git
```
Build it using your IDE or command line. **Requires jdk 8+ as I used <> operator a lot**:
```sh
$ javac src/main/java/com/shengbojia/rune/*.java
```

Great! Now that the interpreter is built, let's move on to actually running it.
## Running the interpreter
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
That's all there is to it. Feel free to add more features to the Rune language if you like.
