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

package com.dmdirc.util.annotations.observable;

import com.dmdirc.util.annotations.Constructor;
import com.dmdirc.util.annotations.Method;
import com.dmdirc.util.annotations.Parameter;
import com.dmdirc.util.annotations.util.SourceFileWriter;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.dmdirc.util.annotations.observable.ObservableModel",})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ObservableProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            for (Element type : roundEnv.getElementsAnnotatedWith(ObservableModel.class)) {
                ObservableModel annotation = type.getAnnotation(ObservableModel.class);
                if (annotation == null) {
                    continue;
                }
                final TypeElement typeElement = (TypeElement) type;
                final String packageName = ((QualifiedNameable) typeElement.getEnclosingElement()).getQualifiedName().toString();
                final String observableClassName = annotation.name().isEmpty() ? "Observable" + typeElement.getSimpleName() : annotation.name();
                final String className = type.toString();

                final List<Constructor> constructors = new ArrayList<>();
                final List<Method> methods = new ArrayList<>();

                for (Element child : type.getEnclosedElements()) {
                    final List<Parameter> params = new ArrayList<>();
                    if (child.getKind() == ElementKind.METHOD) {
                        for (String prefix : annotation.methodPrefixes()) {
                            if (child.getSimpleName().toString().startsWith(prefix)) {
                                ExecutableElement method = (ExecutableElement) child;
                                for (VariableElement element : method.getParameters()) {
                                    final Parameter param = new Parameter(
                                            element.asType().toString(),
                                            element.getSimpleName().toString(),
                                            "");
                                    params.add(param);
                                }
                                methods.add(new Method(child.getSimpleName().toString(),
                                        method.getReturnType().getKind() == TypeKind.VOID
                                        ? "void" : method.getReturnType().getClass().toString(),
                                        params, getTypeNames(method.getThrownTypes()),
                                        method.getModifiers()));
                            }
                        }
                    }
                    if (child.getKind() == ElementKind.CONSTRUCTOR) {
                        ExecutableElement ctor = (ExecutableElement) child;
                        for (VariableElement element : ctor.getParameters()) {
                            final Parameter param = new Parameter(
                                    element.asType().toString(),
                                    element.getSimpleName().toString(),
                                    "");
                            params.add(param);
                        }
                        constructors.add(new Constructor(params, getTypeNames(ctor.getThrownTypes())));
                    }
                }
                writeObserveableModel(annotation, packageName, observableClassName, className, constructors, methods, type);
            }
        }
        return false;
    }

    private void writeObserveableModel(final ObservableModel annotation,
            final String packageName, final String className,
            final String parentClassName, final List<Constructor> constructors,
            final List<Method> methods, final Element... elements) {
        try (SourceFileWriter writer = new SourceFileWriter(processingEnv.getFiler(),
                packageName + (packageName.isEmpty() ? "" : ".") + className, elements)) {
            writer.writePackageDeclaration(packageName);
            writer.writeClassDeclarationStart(className, getClass());
            writer.writeClassExtendsDeclaration(parentClassName);
            writer.writeClassDeclarationEnd();
            writeListenerFields(writer, methods);
            writeConstructors(writer, constructors, methods, className);
            writeWrappedSetters(writer, methods, annotation.oldValue());
            for (Method method : methods) {
                writeListenerManagement(writer, method);
                writeFireListenerMethod(writer, method, annotation.oldValue());
            }
            writeInterfaces(writer, methods, annotation.oldValue());
            writer.writeBlockEnd();
        } catch (Exception ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to write observablemodel file: " + ex.getMessage());
        }
    }

    private void writeListenerFields(final SourceFileWriter writer,
            final List<Method> methods) throws IOException {
        for (Method method : methods) {
            writer.writeField("java.util.List<" + method.getName().substring(3) + "Listener>",
                    method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) + "Listeners",
                    Modifier.PRIVATE, Modifier.FINAL);
        }
    }

    private void writeConstructors(final SourceFileWriter writer,
            final List<Constructor> constructors, final List<Method> methods,
            final String className) throws IOException {
        for (Constructor constructor : constructors) {
            writer.writeConstructorDeclarationStart(className);
            for (Parameter param : constructor.getParameters()) {
                writer.writeMethodParameter(param.getAnnotations(), param.getType(), param.getName(), Modifier.FINAL);
            }
            writer.writeMethodDeclarationEnd();
            writer.writeSuperConstructorStart();
            for (Parameter param : constructor.getParameters()) {
                writer.writeMethodCallParameter(param.getName());
            }
            writer.writeMethodCallEnd();
            for (Method method : methods) {
                writer.writeFieldAssignment(method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) + "Listeners",
                        "new java.util.ArrayList<>()");
            }
            writer.writeBlockEnd();
        }
    }

    private void writeWrappedSetters(final SourceFileWriter writer,
            final List<Method> methods, final boolean oldValue) throws IOException {
        for (Method method : methods) {
            writer.writeMethodDeclarationStart(method.getReturnType(), method.getName(), method.getModifiers().toArray(new Modifier[]{}));
            for (Parameter param : method.getParameters()) {
                writer.writeMethodParameter(param.getAnnotations(), param.getType(), param.getName(), Modifier.FINAL);
            }
            writer.writeMethodDeclarationEnd();
            if (oldValue) {
            writer.writeDeclarationAndAssignment(method.getParameters().get(0).getType(),
                    "oldValue",
                    "get" + method.getName().substring(3) + "()",
                        Modifier.FINAL);
            }
            writer.writeSuperMethodStart(method.getName());
            for (Parameter param : method.getParameters()) {
                writer.writeMethodCallParameter(param.getName());
            }
            writer.writeMethodCallEnd();
            writer.writeDeclarationAndAssignment(method.getParameters().get(0).getType(),
                    "newValue",
                    "get" + method.getName().substring(3) + "()",
                    Modifier.FINAL);
            writer.writeMethodCallStart("fire" + method.getName().substring(3) + "Listener");
            if (oldValue) {
                writer.writeMethodCallParameter("oldValue");
            }
            writer.writeMethodCallParameter("newValue");
            writer.writeMethodCallEnd();
            writer.writeBlockEnd();
        }
    }

    private void writeListenerManagement(final SourceFileWriter writer,
            final Method method) throws IOException {
        writeAddListenerManagementMethod(writer, method, "add", Modifier.PUBLIC);
        writeAddListenerManagementMethod(writer, method, "remove", Modifier.PUBLIC);
    }

    private void writeAddListenerManagementMethod(final SourceFileWriter writer,
            final Method method, final String action, Modifier modifier) throws IOException {
        writer.writeMethodDeclarationStart("void", action + method.getName().substring(3) + "Listener", modifier);
        writer.writeMethodParameter("",
                method.getName().substring(3) + "Listener",
                "listener", Modifier.FINAL);
        writer.writeMethodDeclarationEnd();
        writer.writeMethodCallStart(method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) + "Listeners." + action);
        writer.writeMethodCallParameter("listener");
        writer.writeMethodCallEnd();
        writer.writeBlockEnd();
    }

    private void writeFireListenerMethod(final SourceFileWriter writer, final Method method, final boolean oldValue) throws IOException {
        writer.writeMethodDeclarationStart("void", "fire" + method.getName().substring(3) + "Listener", Modifier.PRIVATE);
        if (oldValue) {
            writer.writeMethodParameter("",
                    method.getParameters().get(0).getType(),
                    "oldValue", Modifier.FINAL);
        }
        writer.writeMethodParameter("",
                method.getParameters().get(0).getType(),
                "newValue", Modifier.FINAL);
        writer.writeMethodDeclarationEnd();
        writer.writeNewForLoopStart(method.getName().substring(3) + "Listener",
                "listener",
                method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) + "Listeners",
                "");
        writer.writeMethodCallStart("listener." + method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) + "Changed");
        if (oldValue) {
            writer.writeMethodCallParameter("oldValue");
        }
        writer.writeMethodCallParameter("newValue");
        writer.writeMethodCallEnd();
        writer.writeForLoopEnd();
        writer.writeBlockEnd();
    }

    private void writeInterfaces(final SourceFileWriter writer,
            final List<Method> methods, final boolean oldValue) throws IOException {
        for (Method method : methods) {
            writeInterface(writer, method, oldValue);
        }
    }

    private void writeInterface(final SourceFileWriter writer,
            final Method method, final boolean oldValue) throws IOException {
        writer.writeInterfaceDeclaration(method.getName().substring(3) + "Listener", getClass(), Modifier.PUBLIC);
        writer.writeMethodDeclarationStart("void", method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) + "Changed");
        if (oldValue) {
            writer.writeMethodParameter("", method.getParameters().get(0).getType(), "oldValue");
        }
        writer.writeMethodParameter("", method.getParameters().get(0).getType(), "newValue");
        writer.writeInterfaceMethodDeclarationEnd();
        writer.writeInterfaceBlockEnd();
    }

    /**
     * Gets a list of fully-qualified type names corresponding to the given
     * mirrors.
     *
     * @param types The set of types to convert to qualified names.
     * @return A matching list containing the qualified name of each type.
     */
    private List<String> getTypeNames(final List<? extends TypeMirror> types) {
        final List<String> res = new ArrayList<>(types.size());

        for (TypeMirror type : types) {
            res.add(type.toString());
        }

        return res;
    }

}
