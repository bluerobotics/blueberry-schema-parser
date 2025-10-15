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

import java.util.ArrayList;
import java.util.List;

import com.bluerobotics.blueberry.schema.parser.types.TypeId;
import com.bluerobotics.blueberry.schema.parser.writers.WriterUtils;
import com.bluerobotics.blueberry.schema.parser.constants.Number;
/**
 * 
 */
public class EnumField extends AbstractField {
	public class NameValue {
		FieldName name;
		Number value;
		boolean isValue;
		String comment;
		public NameValue(FieldName n, Number v, String c) {
			name = n;
			value = v;
			comment = c;
		}
		public FieldName getName() {
			return name;
		}
		public Number getValue() {
			return value;
		}
		public String getComment() {
			return comment;
		}

		public String toString() {
			return getClass().getSimpleName() + "(" + name.toUpperSnake() + " = " + value + ")";
		}
		public String getValueAsHex() {
			return WriterUtils.formatAsHex(getValue().asLong());
		}
	}

	private final ArrayList<NameValue> m_nameValues = new ArrayList<NameValue>();
	public EnumField(FieldName name, FieldName type, TypeId id, String comment) {
		super(name, type, id, comment);
	}
	
	public void addNameValue(FieldName name, Number value, String comment) {
		m_nameValues.add(new NameValue(name,value, comment));
	}


	public List<NameValue> getNameValues(){
		return m_nameValues;
	}
	public String toString() {
		String result = getClass().getSimpleName();
		result += "(";
		result += getTypeName().toUpperCamel();
		result += ")";
		return result;
	}

}
