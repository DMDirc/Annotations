/*
 * Copyright (c) 2006-2013 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.util.annotations.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

/**
 * Utility class for writing Java source files.
 */
public class SourceFileWriter implements Closeable {

    /**
     * Constant for inserting a carriage return and line feed.
     */
    private static final String CRLF = "\r\n";

    /**
     * Number of columns to indent by.
     */
    private static final int INDENT_WIDTH = 4;

    /**
     * The writer to actually write to.
     */
    private final BufferedWriter writer;

    /**
     * The indentation level to write at.
     */
    private int indent;

    /**
     * Whether we've writing the first parameter to a method or not.
     */
    private boolean firstParameter;

    /**
     * Creates a new instance of {@link SourceFileWriter}.
     *
     * @param filer The filer to use to create the source file.
     * @param fileName The name of the file to create.
     * @param elements The element(s) responsible for this file being written.
     * @throws IOException If the file couldn't be created.
     */
    public SourceFileWriter(final Filer filer, final String fileName,
            final Element... elements) throws IOException {
        final JavaFileObject fileObject = filer.createSourceFile(fileName, elements);
        writer = new BufferedWriter(fileObject.openWriter());
    }

    /**
     * Writes a package declaration to the file.
     *
     * @param packageName The name of the package. If empty, no declaration will
     * be written.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writePackageDeclaration(final String packageName) throws IOException {
        if (packageName != null && !packageName.isEmpty()) {
            writeIndent()
                    .append("package ")
                    .append(packageName)
                    .append(';')
                    .append(CRLF)
                    .append(CRLF);
        }
        return this;
    }

    /**
     * Writes a class declaration to the file.
     *
     * @param className The name of the class to write.
     * @param generator The class generating the source.
     * @param modifiers The modifiers of the class, if any.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeClassDeclaration(
            final String className,
            final Class<?> generator,
            final Modifier... modifiers) throws IOException {
        writeClassDeclarationStart(className, generator, modifiers);
        writeClassDeclarationEnd();
        return this;
    }

    /**
     * Writes an extends declaration to a constructor.
     *
     * @param name The name of the class being extended
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeClassExtendsDeclaration(
            final String name) throws IOException {
        writer.append(" extends ")
                .append(name);
        return this;
    }

    /**
     * Writes an implements declaration to a constructor.
     *
     * @param names The name(s) of class being implemented
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeClassImplementsDeclaration(
            final String... names) throws IOException {
        writer.append(" implements ");
        for (String name : names) {
            if (!firstParameter) {
                write(",");
            }
            writer.append(name);
        }
        return this;
    }

    /**
     * Writes the start of a class declaration.
     *
     * @param className The name of the class to write.
     * @param generator The class generating the source.
     * @param modifiers The modifiers of the class, if any.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeClassDeclarationStart(
            final String className,
            final Class<?> generator,
            final Modifier... modifiers) throws IOException {
        writeIndent()
                .append("@javax.annotation.Generated(\"")
                .append(generator.getCanonicalName())
                .append("\")")
                .append(CRLF);
        writeIndent();
        writeModifiers(modifiers)
                .append("class ")
                .append(className);
        firstParameter = true;
        indent++;
        return this;
    }

    /**
     * Writes the end of a class declaration.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeClassDeclarationEnd() throws IOException {
        writer.append(" {")
                .append(CRLF)
                .append(CRLF);
        firstParameter = false;
        return this;
    }

    /**
     * Writes an annotation for a class, field, or other non-nested element, if
     * the given condition is true.
     *
     * @param annotation The annotation to write.
     * @param condition If true the annotation will be written; if false no
     * operation is performed.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeAnnotationIf(
            final String annotation,
            final boolean condition) throws IOException {
        if (condition) {
            writeAnnotation(annotation);
        }
        return this;
    }

    /**
     * Writes an annotation for a class, field, or other non-nested element.
     *
     * @param annotation The annotation to write.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeAnnotation(final String annotation) throws IOException {
        writeIndent()
                .append(annotation)
                .append(CRLF);
        return this;
    }

    /**
     * Writes a field declaration.
     *
     * @param type The fully-qualified type of the field.
     * @param name The name of the field.
     * @param modifiers The field modifiers, if any.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeField(
            final String type,
            final String name,
            final Modifier... modifiers) throws IOException {
        writeIndent();
        writeModifiers(modifiers);

        write(type)
                .write(" ")
                .write(name)
                .write(";")
                .write(CRLF)
                .write(CRLF);

        return this;
    }

    /**
     * Writes the start of a constructor declaration.
     *
     * <p>
     * This should be followed by 0 or more calls to
     * {@link #writeMethodParameter}, and then a single call to
     * {@link #writeMethodDeclarationEnd}.
     *
     * @param name The name of the method.
     * @param modifiers The method modifiers, if any.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeConstructorDeclarationStart(
            final String name,
            final Modifier... modifiers) throws IOException {
        writeIndent();
        writeModifiers(modifiers)
                .append(name)
                .append("(");
        indent += 2;
        firstParameter = true;
        return this;
    }

    /**
     * Writes the start of a method declaration.
     *
     * <p>
     * This should be followed by 0 or more calls to
     * {@link #writeMethodParameter}, and then a single call to
     * {@link #writeMethodDeclarationEnd}.
     *
     * @param returnType The return type of the method.
     * @param name The name of the method.
     * @param modifiers The method modifiers, if any.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeMethodDeclarationStart(
            final String returnType,
            final String name,
            final Modifier... modifiers) throws IOException {
        writeIndent();
        writeModifiers(modifiers)
                .append(returnType)
                .append(" ")
                .append(name)
                .append("(");
        indent += 2;
        firstParameter = true;
        return this;
    }

    /**
     * Writes the start of a super constructor call.
     *
     * This should be followed by 0 or more calls to
     * {@link #writeMethodCallParameter}, and then a single call to
     * {@link #writeMethodCallEnd()}.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeSuperConstructorStart() throws IOException {
        writeIndent();
        write("super(");
        firstParameter = true;
        return this;
    }

    /**
     * Writes the start of a super method call.
     *
     * This should be followed by 0 or more calls to
     * {@link #writeMethodCallParameter}, and then a single call to
     * {@link #writeMethodCallEnd()}.
     *
     * @param name Name of the method to call
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeSuperMethodStart(final String name) throws IOException {
        writeIndent();
        write("super.");
        write(name);
        write("(");
        firstParameter = true;
        return this;
    }

    /**
     * Writes the start of a super method call.
     *
     * This should be followed by 0 or more calls to
     * {@link #writeMethodCallParameter}, and then a single call to
     * {@link #writeMethodCallEnd()}.
     *
     * @param name Name of the method to call
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeMethodCallStart(final String name) throws IOException {
        writeIndent();
        write(name);
        write("(");
        firstParameter = true;
        return this;
    }

    /**
     * Writes a method call parameter.
     *
     * @param name The name of the parameter.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeMethodCallParameter(final String name) throws IOException {
        if (!firstParameter) {
            write(",");
        }
        firstParameter = false;

        write(name);
        return this;
    }

    /**
     * Writes the end of a method call declaration
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeMethodCallEnd() throws IOException {
        write(");");
        write(CRLF);
        return this;
    }

    /**
     * Writes a method parameter.
     *
     * @param annotations The annotations for the parameter, if any.
     * @param type The fully-qualified type of the parameter.
     * @param name The name of the parameter.
     * @param modifiers The parameter modifiers, if any.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeMethodParameter(
            final String annotations,
            final String type,
            final String name,
            final Modifier... modifiers) throws IOException {
        if (!firstParameter) {
            write(",");
        }
        firstParameter = false;

        write(CRLF);
        writeIndent();
        writeModifiers(modifiers)
                .append(annotations)
                .append(annotations.isEmpty() ? "" : " ")
                .append(type)
                .append(" ")
                .append(name);
        return this;
    }

    /**
     * Writes the end of a method declaration.
     *
     * @param throwTypes The types thrown by the method, if any.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeMethodDeclarationEnd(final String... throwTypes) throws IOException {
        write(")");

        if (throwTypes.length > 0) {
            write(" throws").write(CRLF);
            for (int i = 0; i < throwTypes.length; i++) {
                if (i > 0) {
                    write(",").write(CRLF);
                }
                writeIndent().write(throwTypes[i]);
            }
        }

        write(" {")
                .write(CRLF);
        indent--;
        return this;
    }

    /**
     * Writes an assignment statement.
     *
     * @param target The field or variable to be assigned.
     * @param value The new value to assign to it.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeAssignment(
            final String target,
            final String value) throws IOException {
        writeIndent()
                .append(target)
                .append(" = ")
                .append(value)
                .append(";")
                .append(CRLF);
        return this;
    }

    /**
     * Writes a field assignment statement.
     *
     * @param target The field to be assigned.
     * @param value The new value to assign to it.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeFieldAssignment(
            final String target,
            final String value) throws IOException {
        return writeAssignment("this." + target, value);
    }

    /**
     * Writes the end of a block.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeBlockEnd() throws IOException {
        indent--;
        writeIndent()
                .append("}")
                .append(CRLF)
                .append(CRLF);
        return this;
    }

    /**
     * Writes the start of a return statement.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeReturnStart() throws IOException {
        writeIndent().append("return ");
        return this;
    }

    /**
     * Writes a new instance invocation.
     *
     * @param name The fully-qualified name of the type to create a new instance
     * of.
     * @param parameters The value of any parameters to pass in.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeNewInstance(
            final String name, final String... parameters) throws IOException {
        write("new ")
                .write(name)
                .write("(");
        indent += 2;
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                write(",");
            }
            write(CRLF);
            writeIndent().append(parameters[i]);
        }
        indent -= 2;
        write(")");
        return this;
    }

    /**
     * Writes the end of a generic statement.
     *
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter writeStatementEnd() throws IOException {
        write(";").write(CRLF);
        return this;
    }

    /**
     * Writes an arbitrary string to the file, as-is.
     *
     * @param string The string to write.
     * @return A reference to this writer, for convenience.
     * @throws IOException If the operation failed.
     */
    public SourceFileWriter write(final String string) throws IOException {
        writer.append(string);
        return this;
    }

    /**
     * Writes the set of modifiers to the file.
     *
     * @param modifiers The modifiers to be written.
     * @return The writer used, for convenient chaining.
     * @throws IOException If the operation failed.
     */
    private BufferedWriter writeModifiers(final Modifier[] modifiers) throws IOException {
        for (Modifier modifier : modifiers) {
            write(modifier.toString()).write(" ");
        }
        return writer;
    }

    /**
     * Writes sufficient whitespace to satisfy the indentation level.
     *
     * @return The writer used, for convenient chaining.
     * @throws IOException If the write operation failed.
     */
    private BufferedWriter writeIndent() throws IOException {
        for (int i = 0; i < indent * INDENT_WIDTH; i++) {
            writer.append(' ');
        }
        return writer;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException If the writer couldn't be closed successfully.
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }

}
