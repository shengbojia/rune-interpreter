package com.shengbojia.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Utility command line app used to generate the AST classes. Java (unlike Kotlin) has a lot of boilerplate code when
 * creating data classes so this is just a way to automate the process.
 */
public class GenerateAstCode {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: generate_ast <output directory> <package name>");
            System.exit(1);
        }

        String outputDir = args[0];
        String packageName = args[1];

        // Generate the file for expressions
        defineAst(outputDir, packageName, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Ternary  : Expr left, Token firstOp, Expr middle, Token secondOp, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Unary    : Token operator, Expr right",
                "Lambda   : List<Token> params, List<Stmt> body",
                "Variable : Token name"
        ));

        // Generate the file for statements
        defineAst(outputDir, packageName, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ));
    }

    /**
     * Writes the entire file containing the base abstract expression class, and the static subclasses nested within.
     * One single file instead of separate files per class because the individual AST subclasses are only data classes,
     * and simple enough.
     *
     * @param outputDir   the directory to write the file to
     * @param packageName name of the package the file will be contained in
     * @param baseName    name of the base abstract class
     * @param types       list of subclass names
     * @throws IOException
     */
    private static void defineAst(String outputDir,
                                  String packageName,
                                  String baseName,
                                  List<String> types) throws IOException {

        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package " + packageName + ";");
        writer.println();

        writer.println("import com.shengbojia.lox.token.Token;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();

        writer.println("public abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("    public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    /**
     * Writes the specified AST classes.
     *
     * @param writer    the print writer to use
     * @param baseName  the name of the base abstract class
     * @param className the subclass to write
     * @param fieldList list of fields of the specified subclass
     */
    private static void defineType(PrintWriter writer,
                                   String baseName,
                                   String className,
                                   String fieldList) {

        writer.println();
        writer.println("    public static class " + className + " extends " +
                baseName + " {");
        writer.println();

        // Constructor.
        writer.println("        public " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        // Visitor pattern.
        writer.println();
        writer.println("        public <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" +
                className + baseName + "(this);");
        writer.println("        }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("        public final " + field + ";");
        }

        writer.println("    }");
    }

    /**
     * Writes the code for the visitor interface.
     *
     * @param writer the print write to use
     * @param baseName name of base abstract class
     * @param types list of expression class names
     */
    private static void defineVisitor(PrintWriter writer,
                                      String baseName,
                                      List<String> types) {

        writer.println();
        writer.println("    public interface Visitor<R> {");

        for (String type : types) {
            writer.println();
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }
}
