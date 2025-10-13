/*
Copyright (c) 2024  Blue Robotics

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
package com.bluerobotics.blueberry.schema.parser.types;

import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.fields.FieldName;
import com.bluerobotics.blueberry.schema.parser.writers.WriterUtils;

/**
 *
 */
public class EnumType extends AbstractType {
	public class NameValue {
		FieldName name;
		long value;
		boolean isValue;
		String comment;
		public NameValue(FieldName n, long v, String c) {
			name = n;
			value = v;
			comment = c;
		}
		public FieldName getName() {
			return name;
		}
		public long getValue() {
			return value;
		}
		public String getComment() {
			return comment;
		}

		public String toString() {
			return getClass().getSimpleName() + "(" + name + " = " + value + ")";
		}
		public String getValueAsHex() {
			return WriterUtils.formatAsHex(getValue());
		}
	}

	private final ArrayList<NameValue> m_nameValues = new ArrayList<NameValue>();

	public EnumType(FieldName typeName, TypeId id, String comment) {
		super(id, typeName, comment);

	}

	public void addNameValue(FieldName name, long value, String comment) {
		m_nameValues.add(new NameValue(name,value, comment));
	}


	public List<NameValue> getNameValues(){
		return m_nameValues;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result =  super.equals(obj);
		if(result) {
			EnumType ef = (EnumType)obj;

			if(!ef.getTypeName().equals(getTypeName())) {
				result = false;
			}
		}
		return result;
	}







}
