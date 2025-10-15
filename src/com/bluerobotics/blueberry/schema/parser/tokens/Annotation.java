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
package com.bluerobotics.blueberry.schema.parser.tokens;

import java.util.ArrayList;

import com.bluerobotics.blueberry.schema.parser.constants.Constant;
import com.bluerobotics.blueberry.schema.parser.fields.FieldName;

/**
 *
 */
public class Annotation  {
	private enum KnownAnnotation {
		 FILE_PATH("file_path"),
		 TOPIC("topic"),
		 MESSAGE_KEY("message_key"),
		 NAMESPACE("namespace"),
		 SERIALIZATION("serialization"),
		 REVISION("revision"),
		 ;
		private final String name;
		private KnownAnnotation(String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
	}

	public static final FieldName FILE_PATH_ANNOTATION = FieldName.fromSnake("file_path");
	public static final FieldName TOPIC_ANNOTATION = FieldName.fromSnake("topic");
	public static final FieldName MESSAGE_KEY_ANNOTATION      = FieldName.fromSnake("message_key");
	public static final FieldName NAMESPACE_ANNOTATION      = FieldName.fromSnake("namespace");
	public static final FieldName SERIALIZATION_ANNOTATION      = FieldName.fromSnake("serialization");
	private final KnownAnnotation m_known;
	private final FieldName m_name;
	private ArrayList<Object> m_parameters = new ArrayList<>();
	public Annotation(FieldName name) {
		m_name = name;
		KnownAnnotation ka = null;
		for(KnownAnnotation kaf : KnownAnnotation.values()) {
			if(kaf.getName().equals(name)) {
				ka = kaf;
				break;
			}
		}
		m_known = ka;
	}
	public boolean isKnown() {
		return m_known != null;
	}
	public FieldName getName() {
		return m_name;
	}
	public String toString() {
		String result = "Annotation(";
		if(isKnown()) {
			result += getClass().getSimpleName()+"("+m_known+")";
		} else {
			result += m_name.toLowerSnake();
		}
		result += ")";
		return result;
	}
	public void addParameter(Object t) {
		if(t != null) {
			m_parameters.add(t);
		}
	}
	public <C extends Object> C getParameter(int i, Class<C> t) {
		C result = null;
		Object c = m_parameters.get(i);
		if(c.getClass() == t) {
			result = t.cast(c);
		}
		return result;
	}


}
