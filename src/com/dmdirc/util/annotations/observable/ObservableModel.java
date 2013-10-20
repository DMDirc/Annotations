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

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Denotes that an observable subclass should be generated.
 */
@Target(ElementType.TYPE)
public @interface ObservableModel {

    /**
     * Custom name for the observable. If not specified, 'Observable' + the class name will be used.
     *
     * @return The name to use for the observable.
     */
    String name() default "";

    /**
     * The prefixes of methods which should be wrapped, and fire change listeners.
     *
     * @return The method prefixes which should be wrapped.
     */
    String[] methodPrefixes() default {"set"};

    /**
     * Whether or not to include the old value of the field in listener calls.
     *
     * @return True to pass the old value, false to just pass the new one.
     */
    boolean oldValue() default true;

}
