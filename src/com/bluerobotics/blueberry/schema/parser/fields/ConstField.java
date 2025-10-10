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
package com.bluerobotics.blueberry.schema.parser.fields;

import java.math.BigDecimal;

/**
 * 
 */
public class ConstField extends AbstractField {
	private final BigDecimal m_value;
	private final String m_string;
	public ConstField(FieldName name, Type type, BigDecimal value, String comment) {
		super(name, type, comment);
		m_value = value;
		m_string = null;
	}
	public ConstField(FieldName name, String string, String comment) {
		super(name, Type.STRING, comment);
		m_value = null;
		m_string = string;
	}
	@Override
	Type checkType(Type t) throws RuntimeException {
		switch(t) {
	
		case FLOAT32:
		case INT16:
		case INT32:
		case INT8:
		case UINT16:
		case UINT32:
		case UINT8:
		case BOOL:
			break;
		case BOOLFIELD:
		case ARRAY:
		case COMPOUND:
		case STRING:
		case SEQUENCE:
		default:
			throw new RuntimeException("Field must only contain base types.");
				
		}
		return t;
	}
	public BigDecimal getValue() {
		return m_value;
	}
	public String getString() {
		return m_string;
	}

}
