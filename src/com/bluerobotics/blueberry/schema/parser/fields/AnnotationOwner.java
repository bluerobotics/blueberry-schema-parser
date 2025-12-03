/*
Copyright (c) 2025  Blue Robotics

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.bluerobotics.blueberry.schema.parser.fields;

import java.util.List;
import java.util.function.Consumer;

import com.bluerobotics.blueberry.schema.parser.tokens.Annotation;

/**
 * 
 */
public interface AnnotationOwner {
	/**
	 * adds an annotation to this field.
	 * @param as
	 */
	public void addAnnotation(Annotation... as);
	/**
	 * adds an annotation to this field.
	 * @param as
	 */
	void addAnnotation(List<Annotation> as);
	/**
	 * gets the annotation value for the specified name
	 * @param name
	 * @return
	 */
	Annotation getAnnotation(SymbolName name);
	
	/**
	 * scans through the annotations and applies the specified consumer to each
	 * @param c
	 */
	void scanAnnotations(Consumer<Annotation> c);

}
