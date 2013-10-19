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

package com.dmdirc.util.annotations;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.Modifier;

/**
 * Represents a method.
 */
public class Method {

    private final Set<Modifier> modifiers;
    private final String name;
    private final String returnType;
    private final List<Parameter> parameters;
    private final List<String> thrownTypes;

    public Method(String name, String returnType, List<Parameter> parameters, List<String> thrownTypes) {
        this(name, returnType, parameters, thrownTypes, Collections.<Modifier>emptySet());
    }

    public Method(String name, String returnType, List<Parameter> parameters, List<String> thrownTypes, final Set<Modifier> modifiers) {
        this.modifiers = modifiers;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.thrownTypes = thrownTypes;
    }

    public Set<Modifier> getModifiers() {
        return Collections.unmodifiableSet(modifiers);
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public List<String> getThrownTypes() {
        return Collections.unmodifiableList(thrownTypes);
    }

    @Override
    public String toString() {
        return modifiers + " " + returnType + " " + name + "(" + parameters + ") throws " + thrownTypes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.modifiers);
        hash = 47 * hash + Objects.hashCode(this.name);
        hash = 47 * hash + Objects.hashCode(this.returnType);
        hash = 47 * hash + Objects.hashCode(this.parameters);
        hash = 47 * hash + Objects.hashCode(this.thrownTypes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Method other = (Method) obj;
        if (!Objects.equals(this.modifiers, other.getModifiers())) {
            return false;
        }
        if (!Objects.equals(this.name, other.getName())) {
            return false;
        }
        if (!Objects.equals(this.returnType, other.getReturnType())) {
            return false;
        }
        if (!Objects.equals(this.parameters, other.getParameters())) {
            return false;
        }
        if (!Objects.equals(this.thrownTypes, other.getThrownTypes())) {
            return false;
        }
        return true;
    }

}
