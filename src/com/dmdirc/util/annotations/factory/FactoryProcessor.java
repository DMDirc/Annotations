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

package com.dmdirc.util.annotations.factory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Processor for the {@link Factory} annotation.
 */
@SupportedAnnotationTypes("com.dmdirc.util.annotations.factory.Factory")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FactoryProcessor extends AbstractProcessor {

    /** {@inheritDoc} */
    @Override
    public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnv) {
        for (Element type : roundEnv.getElementsAnnotatedWith(Factory.class)) {
            Factory annotation = type.getAnnotation(Factory.class);

            final TypeElement typeElement = (TypeElement) type;
            final PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();

            final List<String> boundParameters = new ArrayList<>();
            final List<List<String>> allParameters = new ArrayList<>();

            for (Element child : type.getEnclosedElements()) {
                if (child.getKind() == ElementKind.CONSTRUCTOR) {
                    final List<String> params = new ArrayList<>();

                    ExecutableElement ctor = (ExecutableElement) child;
                    for (VariableElement element : ctor.getParameters()) {
                        final String param = element.asType().toString() + " " + element.getSimpleName();
                        if (element.getAnnotation(Unbound.class) == null) {
                            if (!boundParameters.contains(param)) {
                                boundParameters.add(param);
                            }
                        }
                        params.add(param);
                    }

                    allParameters.add(params);
                }
            }

            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(typeElement.getQualifiedName() + "Factory");
                BufferedWriter bw = new BufferedWriter(jfo.openWriter());

                if (!packageElement.getQualifiedName().contentEquals("")) {
                    // Don't write a package line for default packages
                    bw.append("package " + packageElement.getQualifiedName() + ";");
                    bw.newLine();
                    bw.newLine();
                }

                final String factoryName = typeElement.getSimpleName() + "Factory";
                final String methodName = "get" + typeElement.getSimpleName();

                bw.append("@javax.annotation.Generated(\"" + getClass().getCanonicalName() + "\")");
                bw.newLine();
                bw.append("public class " + factoryName + " {");
                bw.newLine();
                bw.newLine();

                for (String boundParam : boundParameters) {
                    bw.append("    private final " + boundParam + ";");
                    bw.newLine();
                    bw.newLine();
                }

                if (annotation.inject()) {
                    bw.append("    @javax.inject.Inject");
                    bw.newLine();
                }

                bw.append("    public " + factoryName + "(");
                writeMethodParameters(bw, boundParameters);
                bw.append(") {");
                bw.newLine();
                for (String boundParam : boundParameters) {
                    final String name = boundParam.split(" ", 2)[1];
                    bw.append("        this." + name + " = " + name + ";");
                    bw.newLine();
                }
                bw.append("    }");
                bw.newLine();
                bw.newLine();

                for (List<String> params : allParameters) {
                    final List<String> unbound = new ArrayList<>(params);
                    unbound.removeAll(boundParameters);

                    bw.append("    public " + typeElement.getSimpleName() + " " + methodName + "(");
                    writeMethodParameters(bw, unbound);
                    bw.append(") {");
                    bw.newLine();

                    bw.append("        return new " + typeElement.getSimpleName() + "(");

                    boolean first = true;
                    for (String param : params) {
                        if (!first) {
                            bw.append(',');
                        }
                        first = false;

                        bw.newLine();
                        final String name = param.split(" ", 2)[1];
                        bw.append("                " + name);
                    }

                    bw.append(");");
                    bw.newLine();

                    bw.append("    }");
                    bw.newLine();
                    bw.newLine();
                }

                bw.append("}");
                bw.newLine();
                bw.close();
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to write factory file: " + ex.getMessage(), type);
            }

        }

        return true;
    }

    private void writeMethodParameters(final BufferedWriter bw, final List<String> parameters) throws IOException {
        boolean first = true;
        for (String boundParam : parameters) {
            if (!first) {
                bw.append(',');
            }
            first = false;

            bw.newLine();
            bw.append("            final " + boundParam);
        }
    }

}
