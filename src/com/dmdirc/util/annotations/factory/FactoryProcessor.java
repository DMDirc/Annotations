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

import com.dmdirc.util.annotations.util.SourceFileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

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

            final List<Parameter> boundParameters = new ArrayList<>();
            final List<List<Parameter>> allParameters = new ArrayList<>();

            for (Element child : type.getEnclosedElements()) {
                if (child.getKind() == ElementKind.CONSTRUCTOR) {
                    final List<Parameter> params = new ArrayList<>();

                    ExecutableElement ctor = (ExecutableElement) child;
                    for (VariableElement element : ctor.getParameters()) {
                        final Parameter param = new Parameter(
                                element.asType().toString(),
                                element.getSimpleName().toString(),
                                getAnnotations(element));
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
                final SourceFileWriter writer = new SourceFileWriter(
                        processingEnv.getFiler(),
                        typeElement.getQualifiedName() + "Factory");

                final String factoryName = typeElement.getSimpleName() + "Factory";
                final String methodName = "get" + typeElement.getSimpleName();

                writer.writePackageDeclaration(packageElement.getQualifiedName().toString())
                        .writeClassDeclaration(factoryName, getClass());

                // All the fields we need
                for (Parameter boundParam : boundParameters) {
                    writer.writeField(boundParam.getType(), boundParam.getName(),
                            Modifier.PRIVATE, Modifier.FINAL);
                }

                // Constructor declaration
                writer.writeAnnotationIf("@javax.inject.Inject", annotation.inject())
                        .writeAnnotationIf("@javax.inject.Singleton", annotation.singleton())
                        .writeConstructorDeclarationStart(factoryName, Modifier.PUBLIC);
                writeMethodParameters(writer, boundParameters);
                writer.writeMethodDeclarationEnd();

                // Assign the values to fields
                for (Parameter boundParam : boundParameters) {
                    writer.writeFieldAssignment(boundParam.getName(), boundParam.getName());
                }

                // End of constructor
                writer.writeBlockEnd();

                // Write each factory method out in turn
                for (List<Parameter> params : allParameters) {
                    final List<Parameter> unbound = new ArrayList<>(params);
                    unbound.removeAll(boundParameters);

                    final String[] parameters = new String[params.size()];
                    for (int i = 0; i < parameters.length; i++) {
                        parameters[i] = params.get(i).getName();
                    }

                    writer.writeMethodDeclarationStart(
                            typeElement.getSimpleName().toString(), methodName, Modifier.PUBLIC);
                    writeMethodParameters(writer, unbound);
                    writer.writeMethodDeclarationEnd()
                            .writeReturnStart()
                            .writeNewInstance(typeElement.getSimpleName().toString(), parameters)
                            .writeStatementEnd()
                            .writeBlockEnd();
                }

                // Done!
                writer.writeBlockEnd();
                writer.close();
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to write factory file: " + ex.getMessage(), type);
            }

        }

        return false;
    }

    private void writeMethodParameters(final SourceFileWriter writer, final List<Parameter> parameters) throws IOException {
        for (Parameter param : parameters) {
            writer.writeMethodParameter(
                    param.getAnnotations(), param.getType(),
                    param.getName(), Modifier.FINAL);
        }
    }

    private String getAnnotations(final Element element) {
        final StringBuilder builder = new StringBuilder();

        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().startsWith("com.dmdirc.util.annotations")) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(mirror);
        }

        return builder.toString();
    }

    private static class Parameter {
        private final String type;
        private final String name;
        private final String annotations;

        public Parameter(String type, String name, String annotations) {
            this.type = type;
            this.name = name;
            this.annotations = annotations;
        }

        public Parameter(String type, String name) {
            this(type, name, "");
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getAnnotations() {
            return annotations;
        }

        @Override
        public String toString() {
            return (annotations.isEmpty() ? "" : annotations + " ")
                    + type + " " + name;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + Objects.hashCode(this.type);
            hash = 29 * hash + Objects.hashCode(this.name);
            hash = 29 * hash + Objects.hashCode(this.annotations);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return toString().equals(((Parameter) obj).toString());
        }
    }

}
