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
        if (args.length != 3) {
            System.err.println("Usage: generate_ast <base class name> <output directory> <package name>");
            System.exit(1);
        }

        String baseClassName = args[0];
        String outputDir = args[1];
        String packageName = args[2];

        defineAst(outputDir, packageName, baseClassName, Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
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

        writer.println("abstract class " + baseName + " {");
        writer.println();

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

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

        writer.println("    static class " + className + " extends " +
                baseName + " {");
        writer.println();

        // Constructor.
        writer.println("        " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
        writer.println();
    }
}
